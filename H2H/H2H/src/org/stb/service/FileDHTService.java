package org.stb.service;

import java.io.File;
import java.util.List;

import org.hive2hive.client.util.FileObserver;
import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;

/**
 * The Interface FileDHTService.
 * 
 * @author Aneesh.n
 */
public interface FileDHTService {

	/**
	 * Gets the file list.
	 *
	 * @param node
	 *            the node
	 * @return the file list
	 * @throws NoPeerConnectionException
	 *             the no peer connection exception
	 * @throws NoSessionException
	 *             the no session exception
	 * @throws InvalidProcessStateException
	 *             the invalid process state exception
	 * @throws ProcessExecutionException
	 *             the process execution exception
	 */
	List<String> getFileList(IH2HNode node) throws NoPeerConnectionException, NoSessionException,
			InvalidProcessStateException, ProcessExecutionException;

	/**
	 * Adds the file.
	 *
	 * @param node the node
	 * @param file            the file name
	 * @throws NoPeerConnectionException the no peer connection exception
	 * @throws NoSessionException the no session exception
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws InvalidProcessStateException the invalid process state exception
	 * @throws ProcessExecutionException the process execution exception
	 */
	void addFile(IH2HNode node, File file) throws NoPeerConnectionException, NoSessionException, IllegalArgumentException, InvalidProcessStateException, ProcessExecutionException;

	/**
	 * Download file.
	 *
	 * @param node the node
	 * @param file            the file
	 * @throws NoPeerConnectionException the no peer connection exception
	 * @throws NoSessionException the no session exception
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws InvalidProcessStateException the invalid process state exception
	 * @throws ProcessExecutionException the process execution exception
	 */
	void downloadFile(IH2HNode node, File file) throws NoPeerConnectionException, NoSessionException, IllegalArgumentException, InvalidProcessStateException, ProcessExecutionException;

	/**
	 * Start observer.
	 *
	 * @param node            the node
	 * @param root            the root
	 * @return the file observer
	 * @throws Exception             the exception
	 */
	FileObserver startObserver(IH2HNode node, File root) throws Exception;

	/**
	 * Stop observer.
	 *
	 * @param fileObserver the file observer
	 * @throws Exception the exception
	 */
	void stopObserver(FileObserver fileObserver) throws Exception;

	/**
	 * Sync files with dht.
	 *
	 * @param node the node
	 * @param fileListOnDHT the file list on dht
	 * @param file the file
	 * @throws NoPeerConnectionException the no peer connection exception
	 * @throws NoSessionException the no session exception
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws InvalidProcessStateException the invalid process state exception
	 * @throws ProcessExecutionException the process execution exception
	 */
	void syncFilesWithDHT(IH2HNode node, List<String> fileListOnDHT, File file) throws NoPeerConnectionException,
			NoSessionException, IllegalArgumentException, InvalidProcessStateException, ProcessExecutionException;

}
