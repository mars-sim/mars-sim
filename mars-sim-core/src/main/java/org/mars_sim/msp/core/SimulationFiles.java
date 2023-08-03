/*
 * Mars Simulation Project
 * SimulationFiles.java
 * @date 2022-06-17
 * @author Barry Evans
 */
package org.mars_sim.msp.core;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

import org.mars.sim.mapdata.common.FileLocator;

/**
 * A singleton that controls where the simulation files reside on the file system.
 */
public class SimulationFiles {
	private static final String USERCONFIG_DIR = "conf";
	private static final String HOME_DIR = ".mars-sim";
	private static final String BACKUP_DIR = "backup";
	private static final String SAVE_DIR = "saved";
	private static final String XML_DIR = "xml";
	private static final String AUTOSAVE_DIR = "autosave";
	private static final String LOG_DIR = "logs";
	
	private static String dataDir = null;

	static {
		setDataDir(System.getProperty("user.home") + File.separator + HOME_DIR);
	};

	/**
	 * Private constructor prevents instantiation.
	 */
	private SimulationFiles() {
	}
	
	public static void setDataDir(String newDir) {
		dataDir = newDir;
		FileLocator.setBaseDir(newDir);
	}
	
	public static String getDataDir() {
		return dataDir;
	}
	
	public static String getUserConfigDir() {
		return dataDir + File.separator + USERCONFIG_DIR;
	}
	
	
	public static String getBackupDir() {
		return dataDir + File.separator + BACKUP_DIR;
	}
	
	public static String getSaveDir() {
		return dataDir + File.separator + SAVE_DIR;
	}
	
	public static String getXMLDir() {
		return dataDir + File.separator + XML_DIR;
	}
	
	public static String getAutoSaveDir() {
		return dataDir + File.separator + AUTOSAVE_DIR;
	}

	public static String getLogDir() {
		return dataDir + File.separator + LOG_DIR;
	}

	/**
	 * Purges any old files from a directory.
	 * @param dir Directory to scan
	 * @param retianedCount Number of files to retain.
	 * @param extension Optional file extension to filter file list
	 */
    public static void purgeOldFiles(String dir, int retainedCount, String extension) {
		File[] files = (new File(dir)).listFiles((d, name) -> (extension == null)
																|| name.endsWith(extension));
		Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());

		for (int i = retainedCount; i < files.length; i++) {
			File child = files[i];

			try {
				if ((child.isDirectory() && !deleteDirectory(child)) 
						|| !files[i].delete())
					System.err.println("Failed to delete old file " + child);
			}
			catch(Exception e) {
				// Pretty fatal
				System.err.println("Failed to remove old file " + child);
			}
		}
    }

	/*
	* Deletes a non empty directory.
	*/
	private static boolean deleteDirectory(File dir) {
		File[] children = dir.listFiles();
		for (File child : children) {
			if (child.isDirectory() && !deleteDirectory(child)) {
				return false;
			}
			if (!child.delete()) {
				return false;
			}
		}
		return true;
	}
}
