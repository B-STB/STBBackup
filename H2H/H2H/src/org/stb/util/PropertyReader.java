/**
 * 
 */
package org.stb.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * The Class PropertyReader.
 *
 * @author Ajitesh.k
 */
public final class PropertyReader {

	/** The props. */
	private static Properties props = new Properties();

	/**
	 * Instantiates a new property reader.
	 */
	private PropertyReader() {
		
	}
	
	static {
		try (InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("stb.properties")) {
			props.load(input);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Gets the value.
	 *
	 * @param key the prop key
	 * @return the value
	 */
	public static String getValue(String key) {
		return props.getProperty(key);
	}
}
