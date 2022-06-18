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

/**
 * A singleton that controls where the simulation files reside on the file system
 */
public class SimulationFiles {
	private static final String USERCONFIG_DIR = "conf";
	private static final String HOME_DIR = ".mars-sim";
	private static final String BACKUP_DIR = "backup";
	private static final String SAVE_DIR = "saved";
	private static final String XML_DIR = "xml";
	private static final String AUTOSAVE_DIR = "autosave";
	private static final String LOG_DIR = "logs";
	
	private static String dataDir = System.getProperty("user.home") + //$NON-NLS-1$
					File.separator + HOME_DIR;
	/**
	 * Private constructor prevents instantiation
	 */
	private SimulationFiles() {
	}
	
	public static void setDataDir(String newDir) {
		dataDir = newDir;
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
	 * Purge any old samed simulation files fromt eh auto save dir
	 */
    public static void purgeAutoSave(int retainedCount, String saveFileExtension) {
		File[] files = (new File(getAutoSaveDir())).listFiles((d, name) -> name.endsWith(saveFileExtension));
		Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());

		for(int i = retainedCount; i < files.length; i++) {
			try {
				files[i].delete();
			}
			catch(Exception e) {
				// Pretty fatal
				System.err.println("Failed to remove old sim file " + files[i]);
			}
		}
    }
}
