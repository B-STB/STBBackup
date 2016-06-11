package org.stb.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FileUtils {

	private static final FileUtils INSTANCE = new FileUtils();

	Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);
	private FileUtils() {
		// NO OP
	}

	public static FileUtils getInstance() {
		return INSTANCE;
	}

	public static List<String> getListOfFilesInDirectory(File file){
		List<String> filesInSTBDirectory =displayDirectoryContents(file);
		return filesInSTBDirectory;
	}
	
	private static List<String> displayDirectoryContents(File dir) {
		List<String> filesInSTBDirectory= new ArrayList<>();
		try {
			File[] files = dir.listFiles();
			for (File file : files) {
				if (file.isDirectory()) {
					System.out.println("directory:" + file.getCanonicalPath());
					displayDirectoryContents(file);
				} else {
					filesInSTBDirectory.add(file.getCanonicalPath());
					System.out.println("file:" + file.getCanonicalPath());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return filesInSTBDirectory;
	}
}