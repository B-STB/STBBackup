package org.stb.service;

import java.io.File;

import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.stb.vo.UserCredential;

/**
 * The Interface LoginService.
 * 
 * @author Aneesh.n
 */
public interface LoginService {

	/**
	 * Login to dht.
	 *
	 * @param node            the node
	 * @param userCredential            the user credential
	 * @param root the root
	 * @return true, if successful
	 * @throws InvalidProcessStateException             the invalid process state exception
	 * @throws ProcessExecutionException             the process execution exception
	 * @throws NoPeerConnectionException             the no peer connection exception
	 * @throws Exception             the exception
	 */
	boolean loginToDHT(IH2HNode node, UserCredential userCredential, File root)
			throws InvalidProcessStateException, ProcessExecutionException, NoPeerConnectionException, Exception;

}
