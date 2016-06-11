package org.stb.service.impl;

import org.hive2hive.client.util.FileObserver;
import org.hive2hive.core.api.interfaces.IH2HNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stb.service.ShutDownService;

/**
 * The Class LoginServiceImpl.
 */
public class ShutDownServiceImpl implements ShutDownService {

	/** The logger. */
	Logger LOGGER = LoggerFactory.getLogger(ShutDownServiceImpl.class);

	/* (non-Javadoc)
	 * @see org.stb.service.ShutDownService#stop(org.hive2hive.core.api.interfaces.IH2HNode, org.hive2hive.client.util.FileObserver)
	 */
	@Override
	public void stop(IH2HNode node, FileObserver fileObserver) {

		if (node != null && node.isConnected()) {
			LOGGER.info("Disconnecting from the network...");
			node.disconnect();
			node.getPeer().shutdown();
		}else{
			LOGGER.info("Node was already disconnected");
		}

		if (fileObserver != null && fileObserver.isRunning()) {
			LOGGER.info("Stopping the file observer...");
			try {
				fileObserver.stop(1000L);
				//TODO check what other clean up needs to be done
			} catch (Exception e) {
				LOGGER.error("Cannot Gracefully Shutdown the STB");
			} finally {
				//System.exit(0);
			}
		}else{
			LOGGER.info("File Observer was Not Running");
		}

	}

}
