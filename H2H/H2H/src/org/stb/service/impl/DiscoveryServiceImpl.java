package org.stb.service.impl;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.UUID;

import org.hive2hive.client.util.FileEventListener;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.api.H2HNode;
import org.hive2hive.core.api.configs.FileConfiguration;
import org.hive2hive.core.api.configs.NetworkConfiguration;
import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.api.interfaces.IH2HNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stb.service.DiscoveryService;

public class DiscoveryServiceImpl implements DiscoveryService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DiscoveryServiceImpl.class);
	private BigInteger maxFileSize = H2HConstants.DEFAULT_MAX_FILE_SIZE;
	private int maxNumOfVersions = H2HConstants.DEFAULT_MAX_NUM_OF_VERSIONS;
	private BigInteger maxSizeAllVersions = H2HConstants.DEFAULT_MAX_SIZE_OF_ALL_VERSIONS;
	private int chunkSize = H2HConstants.DEFAULT_CHUNK_SIZE;
	String bootstrapPort = "default";

	private IH2HNode node;

	@Override
	public IH2HNode connectToBootstrapNodes(String nodeID, String... bootstrapIps) {

		LOGGER.debug("Connecting to the bootstrap nodes : {} ", Arrays.toString(bootstrapIps));
		boolean isNodeJoined = false;
		for (String bootstrapIp : bootstrapIps) {
			if(joinNode(bootstrapIp, nodeID)) {
				isNodeJoined = true;
				break;
			}
		}
		if(isNodeJoined) {
			return node;
		}
		return null;
	}

	@Override
	public IH2HNode startDHTNetwork(String nodeID) {
		return createNode(nodeID);
	}

	private IH2HNode createNode(String nodeID) {
		LOGGER.info("Connect a node : {} ", nodeID);
		buildNode();
		if(connectNode(NetworkConfiguration.createInitial(nodeID))) {
			return node;
		} else {
			return null;
		}
	}

	private void buildNode() {
		// TODO : If required we can create default fileConfig as well...
		IFileConfiguration fileConfig = FileConfiguration.createCustom(maxFileSize, maxNumOfVersions,
				maxSizeAllVersions, chunkSize);
		node = H2HNode.createNode(fileConfig);
	}

	private boolean connectNode(NetworkConfiguration networkConfig) {
		boolean isNodeConnected;
		if (node.connect(networkConfig)) {
			LOGGER.info("Network connection successfully established.");
			// connect the event bus
			node.getFileManager().subscribeFileEvents(new FileEventListener(node.getFileManager()));
			isNodeConnected = true;
		} else {
			LOGGER.error("Unable to establish the network connction");
			isNodeConnected = false;
		}
		return isNodeConnected;
	}

	private boolean joinNode(String bootstrapIP, String nodeID) {
		LOGGER.info("Connect to Existing Network {} ", bootstrapIP);

		InetAddress bootstrapAddress;
		try {
			bootstrapAddress = InetAddress.getByName(bootstrapIP);

			buildNode();
			NetworkConfiguration config = NetworkConfiguration.create(nodeID, bootstrapAddress);
			if (!"default".equalsIgnoreCase(bootstrapPort)) {
				config.setBootstrapPort(Integer.parseInt(bootstrapPort));
			}
			return connectNode(config);
		} catch (UnknownHostException e) {
			LOGGER.error("Failed to join a node.");
			return false;
		}
	}
}
