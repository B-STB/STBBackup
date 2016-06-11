package org.stb.util;

import java.io.File;
import java.io.IOException;

import org.hive2hive.core.file.IFileAgent;

/**
 * The Class STBFileAgent.
 */
public class STBFileAgent implements IFileAgent {

	/** The root. */
	private final File root;
	
	/** The cache. */
	private final File cache;

	/**
	 * Instantiates a new STB file agent.
	 *
	 * @param root the root
	 */
	public STBFileAgent(File root) {
		this.root = root;
		this.cache = new File(org.apache.commons.io.FileUtils.getTempDirectory(), "H2H-Cache");
	}

	/* (non-Javadoc)
	 * @see org.hive2hive.core.file.IFileAgent#getRoot()
	 */
	@Override
	public File getRoot() {
		return root;
	}

	/* (non-Javadoc)
	 * @see org.hive2hive.core.file.IFileAgent#writeCache(java.lang.String, byte[])
	 */
	@Override
	public void writeCache(String key, byte[] data) throws IOException {
		org.apache.commons.io.FileUtils.writeByteArrayToFile(new File(cache, key), data);
	}

	/* (non-Javadoc)
	 * @see org.hive2hive.core.file.IFileAgent#readCache(java.lang.String)
	 */
	@Override
	public byte[] readCache(String key) throws IOException {
		return org.apache.commons.io.FileUtils.readFileToByteArray(new File(cache, key));
	}
}
