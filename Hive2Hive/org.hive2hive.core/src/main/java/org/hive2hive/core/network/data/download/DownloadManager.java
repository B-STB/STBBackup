package org.hive2hive.core.network.data.download;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.hive2hive.core.CompletableExecutorService;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.model.MetaChunk;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.processes.files.download.dht.DownloadChunkRunnableDHT;
import org.hive2hive.core.processes.files.download.dht.DownloadTaskDHT;
import org.hive2hive.core.processes.files.download.direct.DownloadChunkRunnableDirect;
import org.hive2hive.core.processes.files.download.direct.DownloadTaskDirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A download manager handling downloads. Downloading chunks happens
 * concurrently. It is possible to download multiple files at a time. The number
 * of concurrent downloads is configurable over the
 * {@link H2HConstants#CONCURRENT_DOWNLOADS} field. <br>
 * Downloaded chunks are stored in a temporary folder and assembled when all
 * chunks are downloaded.
 * 
 * @author Nico
 * 
 */
public class DownloadManager {

	private static final Logger logger = LoggerFactory.getLogger(DownloadManager.class);

	private final NetworkManager networkManager;
	private final IFileConfiguration fileConfig;
	private final Set<BaseDownloadTask> openTasks;

	private CompletableExecutorService executor;

	public DownloadManager(NetworkManager networkManager, IFileConfiguration fileConfig) {
		this.networkManager = networkManager;
		this.fileConfig = fileConfig;
		this.openTasks = Collections.newSetFromMap(new ConcurrentHashMap<BaseDownloadTask, Boolean>());
		// start executor
		// this.executor = Executors.newCachedThreadPool();
		this.executor = CompletableExecutorService
				.completable(Executors.newFixedThreadPool(H2HConstants.CONCURRENT_DOWNLOADS));
		// this.executor = new
		// ThreadPoolExecutor(H2HConstants.CONCURRENT_DOWNLOADS,
		// H2HConstants.CONCURRENT_DOWNLOADS, 0L, TimeUnit.MILLISECONDS, new
		// LinkedBlockingQueue<Runnable>(15));;
	}

	/**
	 * Add a new task to download a file. The download is automatically started
	 * in the background
	 */
	public void submit(BaseDownloadTask task) throws NoPeerConnectionException {
		logger.debug("Submitted to download {}", task.getDestinationName());

		// store the task for possible later recovery
		openTasks.add(task);

		// add a listener
		task.addListener(new DownloadListener());

		// start the execution
		schedule(task);
	}

	private void schedule(BaseDownloadTask task) throws NoPeerConnectionException {
		List<MetaChunk> openChunks = task.getOpenChunks();
		if (task.isDirectDownload()) {
			// first get the locations of all users having access to this file
			DownloadTaskDirect directTask = (DownloadTaskDirect) task;
			directTask.startFetchLocations(networkManager.getDataManager());

			// then download all chunks in separate threads
			for (MetaChunk chunk : openChunks) {
				DownloadChunkRunnableDirect runnable = new DownloadChunkRunnableDirect(directTask, chunk,
						networkManager.getMessageManager(), fileConfig);
				executor.submit(runnable);
			}
		} else {
			// submit each chunk as a separate thread
			// for (MetaChunk chunk : task.getOpenChunks()) {
			// DownloadChunkRunnableDHT runnable = new
			// DownloadChunkRunnableDHT((DownloadTaskDHT) task, chunk,
			// networkManager.getDataManager(), networkManager.getEncryption());
			// executor.submit(runnable);
			// }
			// List<Callable<Boolean>> chunkGroup = new ArrayList<>();
			executeDownload(task, openChunks);

		}
	}

	private void executeDownload(BaseDownloadTask task, List<MetaChunk> openChunks) throws NoPeerConnectionException {
		executor.submit(new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				// TODO Auto-generated method stub
				int totalChunkCount = openChunks.size();
				LinkedBlockingQueue<MetaChunk> queue = new LinkedBlockingQueue<>(openChunks);
				AtomicInteger countSuccessChunks = new AtomicInteger();
				AtomicBoolean isContinueTransfer = new AtomicBoolean(true);
				while (isContinueTransfer.get()) {
					try {
						MetaChunk chunk = queue.take();
						DownloadChunkRunnableDHT runnable = new DownloadChunkRunnableDHT((DownloadTaskDHT) task, chunk,
								networkManager.getDataManager(), networkManager.getEncryption());
						CompletableFuture<Boolean> completableFuture = executor.submit(runnable);
						Function<Boolean, Throwable> bifuntion = (result) -> {
							if (result != null && result) {
								int successCount = countSuccessChunks.incrementAndGet();
								if (successCount == totalChunkCount) {
									isContinueTransfer.set(false);
									//TODO handle success/failure result
									Thread.currentThread().interrupt();
								}
							} else {
								try {
									// IF not successful put it back to queue
									queue.put(chunk);
								} catch (Exception e) {
									logger.warn(e.getMessage(), e);
								}
							}
							return null;
						};
						completableFuture.thenApplyAsync(bifuntion);

					} catch (InterruptedException e) {
						// TODO ANEESH handle incase of failure during download
						e.printStackTrace();
						logger.warn(e.getMessage(), e);
					}
				}

				logger.info("Final counter value {}", totalChunkCount);
				return null;
			}
		});
	}

	/**
	 * Stop the downloads
	 */
	public void stopBackgroundProcesses() {
		executor.shutdownNow();
		logger.debug("All downloads stopped");
	}

	/**
	 * Start / continue the downloads
	 */
	public void startBackgroundProcess() throws NoPeerConnectionException {
		executor = CompletableExecutorService
				.completable(Executors.newFixedThreadPool(H2HConstants.CONCURRENT_DOWNLOADS));
		for (BaseDownloadTask task : openTasks) {
			schedule(task);
		}
	}

	/**
	 * Listens for a download to finish and removes it from the open list
	 */
	private class DownloadListener implements IDownloadListener {

		@Override
		public void downloadFinished(BaseDownloadTask task) {
			// remove it from the task list
			openTasks.remove(task);
			logger.debug("Task for downloading '{}' finished.", task.getDestinationName());
		}

		@Override
		public void downloadFailed(BaseDownloadTask task, String reason) {
			// remove it from the task anyway
			openTasks.remove(task);
			logger.debug("Task for downloading '{}' failed.", task.getDestinationName());
		}

	}
}
