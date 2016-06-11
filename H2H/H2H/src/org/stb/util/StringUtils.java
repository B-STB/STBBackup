package org.stb.util;

public final class StringUtils {

	private static final StringUtils INSTANCE = new StringUtils();

	private StringUtils() {
		// NO OP
	}

	public static StringUtils getInstance() {
		return INSTANCE;
	}

}