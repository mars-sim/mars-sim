/*
 * Mars Simulation Project
 * SaveDialog.java
 * @date 2025-12-21
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.SimulationRuntime;
import com.mars_sim.core.tool.Msg;

/**
 * Utility class for saving simulations for the UI
 */
public class SaveDialog {
    private SaveDialog() {
        // Prevent instantiation
    }

    /**
	 * Performs the process of saving a simulation.
	 * Note: if defaultFile is false, displays a FileChooser to select the
	 * location and new filename to save the simulation.
	 *
	 * @param defaultFile is the default.sim file be used
     * @param frame Parent frame for any dialogs
     * @param sim Simulation to be saved
	 */
	public static void create(JFrame frame, Simulation sim,boolean defaultFile) {
		File fileLocn = null;
		if (!defaultFile) {
			JFileChooser chooser = new JFileChooser(SimulationRuntime.getSaveDir());
			chooser.setDialogTitle(Msg.getString("MainWindow.dialogSaveSim"));
			if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
				fileLocn = chooser.getSelectedFile();
			} else {
				return;
			}
		}

		// Request the save
		sim.requestSave(fileLocn, null);
	}
}
