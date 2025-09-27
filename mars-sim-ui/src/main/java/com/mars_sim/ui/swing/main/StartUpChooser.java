/*
 * Mars Simulation Project
 * StartUpChooser.java
 * @date 2025-09-23
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.SimulationRuntime;
import com.mars_sim.core.configuration.Scenario;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.MainWindow;

/**
 * This class is a simple dialog to let user choose how to start the simulation.
 */
public class StartUpChooser extends JDialog {
    /**
     * Files filter for simulation files.
     */
    private static class SimFileFilter extends FileFilter {
        @Override
        public boolean accept(java.io.File f) {
            if (f.isDirectory()) {
                return true;
            }
            String name = f.getName().toLowerCase();
            return name.endsWith(Simulation.SAVE_FILE_EXTENSION);
        }

        @Override
        public String getDescription() {
            return "Saved simulation file (" + Simulation.SAVE_FILE_EXTENSION + ")";
        }
    }

    static final int NEW_SIM = 0;
    static final int LOAD_SIM = 1;
    static final int EDIT_SCENARIO = 2;
    static final int SCENARIO = 3;
    static final int TEMPLATE = 4;
    static final int EXIT = 5;
    static final int NEW_SOCIETY = 6;
    
    private String simFile;
    private int selected = -1;

    StartUpChooser() {
        super(new JFrame());

		setSize(250, 300);
        setIconImage(MainWindow.getIconImage());
        setResizable(false);
		setTitle(Msg.getString("StartUpChooser.title")); // -NLS-1$

        JPanel contentPanel = new JPanel(new BorderLayout());
        add(contentPanel);

        JLabel instructions = new JLabel(Msg.getString("StartUpChooser.instructions"));
        instructions.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
                            BorderFactory.createEmptyBorder(5,5,5,5)));
        contentPanel.add(instructions, BorderLayout.NORTH);

		// Sets the dialog content panel.
		JPanel buttonPanel = new JPanel(new FlowLayout());
        contentPanel.add(buttonPanel, BorderLayout.CENTER);

        addStartButton(buttonPanel, "newSim", e -> choiceMade(NEW_SIM));
        addStartButton(buttonPanel, "load", e -> selectSimFile());
        addStartButton(buttonPanel, "loadDefault", e -> choiceMade(LOAD_SIM));
        addStartButton(buttonPanel, "loadScenario", e -> selectScenario());
        addStartButton(buttonPanel, "loadTemplate", e -> selectTemplate());
        addStartButton(buttonPanel, "editScenario", e -> choiceMade(EDIT_SCENARIO));
        
    	// Set the location of the dialog at the center of the screen.
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((screenSize.width - getWidth()) / 2, (screenSize.height - getHeight()) / 2);
		setVisible(true);
        addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				choiceMade(EXIT);
			}
		});
    }

    private void addStartButton(JPanel panel, String labelKey, ActionListener action) {
        JButton button = new JButton(Msg.getString("StartUpChooser." + labelKey));
        button.setToolTipText(Msg.getString("StartUpChooser." + labelKey + ".tooltip"));
        button.addActionListener(action);
        panel.add(button);
    }

    private void selectTemplate() {
        choiceMade(TEMPLATE);
    }

    private void selectScenario() {
        choiceMade(SCENARIO);
    }

    private synchronized void choiceMade(int choice) {
        selected = choice;
        dispose();
		notifyAll();
	}

    /**
	 * Performs the process of loading a simulation.
	 */
	private void selectSimFile() {
		JFileChooser chooser = new JFileChooser(SimulationRuntime.getSaveDir());
        chooser.setFileFilter(new SimFileFilter());
		chooser.setDialogTitle(Msg.getString("MainWindow.dialogLoadSavedSim")); // -NLS-1$
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			simFile = chooser.getSelectedFile().getAbsolutePath();
            choiceMade(LOAD_SIM);
		}
	}

    /**
     * Gets the choice made by the user. This method blocks until a choice is made.
     * @return
     */
    public synchronized int getChoice() {
        while (selected == -1) {
            try {
                wait();
            } catch (InterruptedException e)  {
                Thread.currentThread().interrupt();
            }
        }
        return selected;
    }

    /**
     * Gets the selected file if the user chose to load a simulation.
     * @return
     */
    public String getSelectedFile() {
        return simFile;
    }

    public Scenario getScenario() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getScenario'");
    }

}
