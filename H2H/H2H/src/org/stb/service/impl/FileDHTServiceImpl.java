package org.stb.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hive2hive.client.util.FileObserver;
import org.hive2hive.client.util.FileObserverListener;
import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.processes.files.list.FileNode;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stb.service.FileDHTService;
import org.stb.util.FileUtils;
import org.stb.util.PropertyReader;

public class FileDHTServiceImpl implements FileDHTService {

	Logger LOGGER = LoggerFactory.getLogger(FileDHTServiceImpl.class);

	@Override
	public List<String> getFileList(IH2HNode node) throws NoPeerConnectionException, NoSessionException,
			InvalidProcessStateException, ProcessExecutionException {
		IProcessComponent<FileNode> fileListProcess = node.getFileManager().createFileListProcess();
		FileNode root = fileListProcess.execute();
		List<String> fileOnSTBList = collectRecursively(root, 0);
		return fileOnSTBList;
	}

	@Override
	public void addFile(IH2HNode node, File file) throws NoPeerConnectionException, NoSessionException,
			IllegalArgumentException, InvalidProcessStateException, ProcessExecutionException {
		IProcessComponent<Void> addFileProcess = node.getFileManager().createAddProcess(file);
		addFileProcess.execute();
	}

	@Override
	public void downloadFile(IH2HNode node, File file) throws NoPeerConnectionException, NoSessionException,
			IllegalArgumentException, InvalidProcessStateException, ProcessExecutionException {
		IProcessComponent<Void> updateFileProcess = node.getFileManager().createDownloadProcess(file);
		updateFileProcess.execute();
	}

	@Override
	public FileObserver startObserver(IH2HNode node, File root) throws Exception {
		// TODO get root and Interval from props
		FileObserver fileObserver = new FileObserver(root);
		String ignoreVals = PropertyReader.getValue("stb.ignoreList");
		List<String> ignoreList = Arrays.asList(ignoreVals.split(","));
		FileObserverListener listener = new FileObserverListener(node.getFileManager(), ignoreList);
		fileObserver.addFileObserverListener(listener);

		fileObserver.start();
		LOGGER.info("Observer started for: {}", root);
		return fileObserver;
	}

	@Override
	public void stopObserver(FileObserver fileObserver) throws Exception {
		fileObserver.stop();
	}

	private List<String> collectRecursively(FileNode node, int level) {

		List<String> fileList = new ArrayList<>();
		if (node.getParent() != null) {
			// skip the root node
			
			fileList.add(node.getFile().getAbsolutePath());
			
			//TODO check
			StringBuilder spaces = new StringBuilder("*");
			for (int i = 0; i < level; i++) {
				spaces.append(" ");
			}
			String nodeName = spaces.append(node.getName()).toString();
			print(nodeName);
		}

		if (node.isFolder()) {
			for (FileNode child : node.getChildren()) {
				fileList.add(child.getFile().getAbsolutePath());
				collectRecursively(child, level + 1);
			}
		}
		return fileList;
	}

	public void print(String message) {
		LOGGER.info(message);
	}

	@Override
	public void syncFilesWithDHT(IH2HNode node, List<String> fileListOnDHT, File rootDir) throws NoPeerConnectionException,
			NoSessionException, IllegalArgumentException, InvalidProcessStateException, ProcessExecutionException {
		LOGGER.info("Root folder: {}", rootDir);
		List<String> filesInSTBList = FileUtils.getListOfFilesInDirectory(rootDir);
		LOGGER.info("Files on DHT: {}", fileListOnDHT);
		LOGGER.info("Files in STB: {}", filesInSTBList);
		Set<String> filesInSTB = new HashSet<>(filesInSTBList);

		List<String> fileOnDHTNotOnSTB = new ArrayList<>();

		if (fileListOnDHT != null) {
			for (String fileOnDHT : fileListOnDHT) {
				if (filesInSTB != null && filesInSTB.contains(fileOnDHT)) {
					filesInSTB.remove(fileOnDHT); // LEFT over would be list of
													// files not on the DHT
				} else {
					fileOnDHTNotOnSTB.add(fileOnDHT); // LEFT over would be
														// list of files not on
														// the STB
				}
			}
		}
		LOGGER.info("Files to download: {}", fileOnDHTNotOnSTB);
		if (fileOnDHTNotOnSTB != null) {
			for (String fileInStb : fileOnDHTNotOnSTB) {
				File workingFile = new File(fileInStb);
				downloadFile(node, workingFile);
			}
		}

		LOGGER.info("Files to upload: {}", filesInSTB);
		if (filesInSTB != null) {
			for (String fileForStb : filesInSTB) {
				File workingFile = new File(fileForStb);
				LOGGER.debug("File: {} is exist: {}", workingFile, workingFile.exists());
				addFile(node, workingFile);
			}
		}
	}

}
