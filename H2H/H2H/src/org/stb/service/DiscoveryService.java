package org.stb.service;

import org.hive2hive.core.api.interfaces.IH2HNode;

/**
 * The Interface DiscoveryService.
 * 
 * @author Aneesh.n
 */
public interface DiscoveryService {

	/**
	 * Connect to bootstrap nodes.
	 *
	 * @param nodeID the node id
	 * @param bootstrapIps the bootstrap node ips
	 * @return true, if any of the ips were successfully connected
	 */
	IH2HNode connectToBootstrapNodes(String nodeID, String... bootstrapIps);
	
	/**
	 * Start dht network.
	 *
	 * @param nodeID the node id
	 * @return the i h2 h node
	 */
	IH2HNode startDHTNetwork(String nodeID);
	
}
