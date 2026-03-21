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
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.SimulationListener;
import com.mars_sim.core.SimulationRuntime;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ContentManager;

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

	
	/**
	 * Prompts user to exit simulation.
	 */
	public static void createEndSimulation(Simulation sim, ContentManager mainWindow) {
		if (!sim.isSavePending()) {
			Object[] options = {"Yes without Save",
                    "Yes and Save",
                    "No"};

			int reply = JOptionPane.showOptionDialog(mainWindow.getTopFrame(),
				"Are you sure you want to exit?", "Exiting the Simulation", JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,
				options, options[2]);	
			
			// What has the user choosen ?
			if (reply == JOptionPane.NO_OPTION) {
				// Same in default
				sim.requestSave(null, new SimulationListener() {
					@Override
					public void eventPerformed(String action) {
						if (SAVE_COMPLETED.equals(action)) {
							SwingUtilities.invokeLater(() -> {
								endSimulation(sim, mainWindow);
							});
						}
						else if (SAVE_FAILED.equals(action)) {
							JOptionPane.showMessageDialog(mainWindow.getTopFrame(), "Save failed. Simulation will not be exited.", "Save Failed", JOptionPane.ERROR_MESSAGE);
						}
					}
				});
			} 
			else if (reply == JOptionPane.YES_OPTION) {
				// Save the UI configuration.
				endSimulation(sim, mainWindow);
			}
		}
	}

	/**
	 * Perform the end of simualtion seqeunce. This involves savign the UI config as well as stopping the Simulatiin.
	 * @param sim Simulation to end.
	 * @param mainWindow Main window of the application.
	 */
	private static void endSimulation(Simulation sim, ContentManager mainWindow) {
		sim.endSimulation();
		mainWindow.getConfig().saveFile(mainWindow);
		mainWindow.shutdown();
		System.exit(0);
	}
}
