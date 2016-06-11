package org.stb.main;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stb.control.STBController;
import org.stb.util.PropertyReader;

/**
 * The Class Main.
 * 
 * @author aneesh.n
 */
public class Main {

	/** The Constant STB_STATUS. */
	private static final String STB_STATUS = "stb.status";

	/** The Constant LOCK_DIR. */
	private static final String LOCK_DIR = PropertyReader.getValue("stb.lock");

	/** The Constant STB_LOCK. */
	private static final String STB_LOCK = "stb.lock";

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(STBController.class);

	/** The watcher. */
	private static WatchService watcher;

	private static ExecutorService executor = Executors.newSingleThreadExecutor();

	private static Future<?> shutdownThreadFuture;

	/**
	 * The main method.
	 *
	 * @param args
	 *            the arguments
	 * @throws Exception
	 *             the exception
	 */
	public static void main(String[] args) throws Exception {
		lockAndLoad();
	}

	/**
	 * Lock and load.
	 */
	private static void lockAndLoad() {
		// Get a file channel for the file
		Path lockFilePath = Paths.get(LOCK_DIR, STB_LOCK);
		Path statusFilePath = Paths.get(LOCK_DIR, STB_STATUS);
		Path lockDirPath = Paths.get(LOCK_DIR);
		try {
			Files.deleteIfExists(statusFilePath);
			if (Files.exists(lockDirPath) && !Files.isDirectory(lockDirPath)) {
				Files.delete(lockDirPath);
			}
			if (Files.notExists(lockDirPath)) {
				Files.createDirectory(lockDirPath);
			}
			if (!Files.exists(lockFilePath)) {
				Files.write(lockFilePath, "LOCK".getBytes());
			}
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			return;
		}
		FileLock lock = null;
		try (FileChannel channel = new RandomAccessFile(lockFilePath.toFile(), "rw").getChannel()) {
			// Use the file channel to create a lock on the file.
			// This method blocks until it can retrieve the lock.
			// lock = channel.tryLock();

			/*
			 * use channel.lock OR channel.tryLock();
			 */

			// Try acquiring the lock without blocking. This method returns
			// null or throws an exception if the file is already locked.
			// lock = channel.tryLock();

			// if (lock != null) {
			Files.write(statusFilePath, "RUNNING".getBytes());
			startShutDownWatcher(lockDirPath);
			STBController.getInstance().start();
			// } else {
			// LOGGER.error("Could not start service as lock could not be
			// acquired.");
			// }
		} catch (OverlappingFileLockException e) {
			// File is already locked in this thread or virtual machine
			LOGGER.error("Could not start service as lock could not be acquired.");
		} catch (Exception e) {
			LOGGER.error("Could not start service due to error: ", e);
		} finally {
			// Release the lock - if it is not null!
			if (lock != null) {
				try {
					lock.release();
					Files.deleteIfExists(lockDirPath);
				} catch (IOException e) {
					LOGGER.error(e.getMessage(), e);
				}
			}
		}
	}

	/**
	 * Start shut down watcher.
	 *
	 * @param lockDirPath
	 *            the status file path
	 */
	private static void startShutDownWatcher(Path lockDirPath) {
		try {
			watcher = FileSystems.getDefault().newWatchService();
			lockDirPath.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

			LOGGER.info("Watch Service to look for graceful shutdown registered for dir: " + lockDirPath.getFileName());
			shutdownThreadFuture = executor.submit(new Runnable() {

				@Override
				public void run() {

					while (true) {
						WatchKey key;
						try {
							key = watcher.take();
						} catch (InterruptedException ex) {
							return;
						}

						for (WatchEvent<?> event : key.pollEvents()) {
							WatchEvent.Kind<?> kind = event.kind();

							@SuppressWarnings("unchecked")
							WatchEvent<Path> ev = (WatchEvent<Path>) event;
							Path fileName = ev.context();

							System.out.println(kind.name() + ": " + fileName);

							if (kind == ENTRY_MODIFY && fileName.toString().equals(STB_STATUS)) {
								LOGGER.info("STB status file has changed! STB will be stopped.");
								try {
									STBController.getInstance().stop();
								} catch (Exception e) {
									LOGGER.error(e.getMessage(), e);
								}
								break;
							} else if (kind == ENTRY_DELETE && fileName.toString().equals(STB_STATUS)) {
								LOGGER.info("STB status file has been deleted! STB will be stopped.");
								try {
									STBController.getInstance().stop();
								} catch (Exception e) {
									LOGGER.error(e.getMessage(), e);
								}
								break;
							}
						}

						boolean valid = key.reset();
						if (!valid) {
							break;
						}
					}
				}
			});
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}
	
	/**
	 * Shutdown watcher.
	 */
	public static void shutdownWatcher() {
		if (watcher != null) {
			try {
				watcher.close();
			} catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
		if (!shutdownThreadFuture.isDone() || shutdownThreadFuture.isCancelled()) {
			shutdownThreadFuture.cancel(true);
		}
		executor.shutdown();
	}

}
