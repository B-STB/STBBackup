package org.stb.service;

import org.hive2hive.client.util.FileObserver;
import org.hive2hive.core.api.interfaces.IH2HNode;

/**
 * The Interface ShutDownService.
 */
public interface ShutDownService {

	/**
	 * Stop.
	 *
	 * @param node the node
	 * @param fileObserver the file observer
	 */
	void stop(IH2HNode node, FileObserver fileObserver);
}
