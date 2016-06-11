package org.stb.service.impl;

import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.api.interfaces.IUserManager;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stb.service.CredentialRegisterService;
import org.stb.vo.UserCredential;

public class CredentialRegisterServiceImpl implements CredentialRegisterService {

	Logger LOGGER = LoggerFactory.getLogger(CredentialRegisterServiceImpl.class);

	@Override
	public boolean registerCredential(IH2HNode node, UserCredential credentials)
			throws NoPeerConnectionException, InvalidProcessStateException {

		IUserManager userManager = node.getUserManager();
		UserCredentials userCredentials = new UserCredentials(credentials.getUserName(),
				String.valueOf(credentials.getPassword()), credentials.getPin());
		if (!userManager.isRegistered(userCredentials.getUserId())) {
			LOGGER.info("User - {} is not Registered", userCredentials.getUserId());
			IProcessComponent<Void> registerProcess = userManager.createRegisterProcess(userCredentials);
			try {
				registerProcess.execute();
				return true;
			} catch (ProcessExecutionException e) {
				LOGGER.info("User - {} could not be Registered as ", userCredentials.getUserId(), e.getMessage());
				return false;
			}
		} else {
			LOGGER.info("User - {} is Already Registered", userCredentials.getUserId());
			return true;
		}
	}

}
