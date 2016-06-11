package org.stb.service.impl;

import java.io.File;

import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.api.interfaces.IUserManager;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stb.service.LoginService;
import org.stb.util.STBFileAgent;
import org.stb.vo.UserCredential;

/**
 * The Class LoginServiceImpl.
 */
public class LoginServiceImpl implements LoginService {

	/** The logger. */
	Logger LOGGER = LoggerFactory.getLogger(LoginServiceImpl.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.stb.service.LoginService#loginToDHT(org.hive2hive.core.api.interfaces
	 * .IH2HNode, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public boolean loginToDHT(IH2HNode node, UserCredential credentials, File root) throws Exception {
		LOGGER.info("Logging In with Credentials {} and root {}", credentials, root);
		IUserManager userManager = node.getUserManager();
		if (userManager == null) {
			throw new Exception("User Manager Cannot be Null");
		}

		STBFileAgent fileAgent = new STBFileAgent(root);

		UserCredentials userCredentials = new UserCredentials(credentials.getUserName(),
				String.valueOf(credentials.getPassword()), credentials.getPin());
		IProcessComponent<Void> loginProcess = userManager.createLoginProcess(userCredentials, fileAgent);
		try {
			loginProcess.execute();
			return true;
		} catch (ProcessExecutionException e) {
			LOGGER.info("User - {} couldn't login as {}", userCredentials.getUserId(), e.getMessage());
			return false;
		}
	}

}
