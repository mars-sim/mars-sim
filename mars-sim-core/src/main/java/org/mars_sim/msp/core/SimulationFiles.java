package org.mars_sim.msp.core;

import java.io.File;

/**
 * A singleton that controls where the simulation files reside on the file system
 */
public class SimulationFiles {
	private static String dataDir = System.getProperty("user.home") + //$NON-NLS-1$
					File.separator + Msg.getString("Simulation.homeFolder");
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
	
	public static String getBackupDir() {
		return dataDir + File.separator + Msg.getString("Simulation.backupFolder"); //$NON-NLS-1$
	}
	
	public static String getSaveDir() {
		return dataDir + File.separator + Msg.getString("Simulation.saveDir"); //$NON-NLS-1$
	}
	
	public static String getXMLDir() {
		return dataDir + File.separator + Msg.getString("Simulation.xmlFolder"); //$NON-NLS-1$
	}
	
	public static String getAutoSaveDir() {
		return dataDir + File.separator + Msg.getString("Simulation.saveDir.autosave"); //$NON-NLS-1$
	}

	public static String getLogDir() {
		return dataDir + File.separator + Msg.getString("Simulation.logDir"); //$NON-NLS-1$
	}
}
