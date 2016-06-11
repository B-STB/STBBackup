package org.stb.service;

import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.stb.vo.UserCredential;

/**
 * The Interface CredentialRegisterService.
 * 
 * @author Aneesh.n
 */
public interface CredentialRegisterService {

	/**
	 * Register credential.
	 *
	 * @param node
	 *            the node
	 * @param userCredential
	 *            the user credential
	 * @return true, if successful
	 * @throws NoPeerConnectionException
	 *             the no peer connection exception
	 * @throws InvalidProcessStateException
	 *             the invalid process state exception
	 */
	boolean registerCredential(IH2HNode node, UserCredential userCredential)
			throws NoPeerConnectionException, InvalidProcessStateException;

}
