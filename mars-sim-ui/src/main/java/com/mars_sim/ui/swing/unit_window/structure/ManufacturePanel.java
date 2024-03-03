/*
 * Mars Simulation Project
 * ManufacturePanel.java
 * @date 2021-09-20
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.structure;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;

import javax.swing.BoundedRangeModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import com.mars_sim.core.manufacture.ManufactureProcess;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MarsPanelBorder;
import com.mars_sim.ui.swing.utils.ProcessInfoRenderer;

/**
 * A panel showing information about a manufacturing process.
 */
@SuppressWarnings("serial")
public class ManufacturePanel extends JPanel {

	// Data members
	private ManufactureProcess process;
	private BoundedRangeModel workBarModel;
	private BoundedRangeModel timeBarModel;

	/**
	 * Constructor
	 * @param process the manufacturing process.
	 * @param showBuilding is the building name shown?
	 * @param processStringWidth the max string width to display for the process name.
	 */
	public ManufacturePanel(ManufactureProcess process, boolean showBuilding, int processStringWidth) {
		// Call JPanel constructor
		super();

		// Initialize data members.
		this.process = process;

        // Set layout
		if (showBuilding) setLayout(new GridLayout(4, 1, 0, 0));
		else setLayout(new GridLayout(3, 1, 0, 0));

        // Set border
        setBorder(new MarsPanelBorder());

        // Prepare name panel.
        JPanel namePane = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 0));
        add(namePane);

        // Prepare cancel button.
        JButton cancelButton = new JButton(ImageLoader.getIconByName("action/cancel"));
        cancelButton.setMargin(new Insets(0, 0, 0, 0));
        cancelButton.addActionListener(event ->
                    getManufactureProcess().getWorkshop().endManufacturingProcess(getManufactureProcess(), true));
        cancelButton.setToolTipText("Cancel manufacturing process");
        namePane.add(cancelButton);

        // Prepare name label.
        String name = process.getInfo().getName();
        if (name.length() > 0) {
        	String firstLetter = name.substring(0, 1).toUpperCase();
        	name = " " + firstLetter + name.substring(1);
        }
        if (name.length() > processStringWidth) name = name.substring(0, processStringWidth) + "...";
		// Capitalize process names
        JLabel nameLabel = new JLabel(name, SwingConstants.CENTER);
        namePane.add(nameLabel);

        if (showBuilding) {
        	// Prepare building name label.
        	String buildingName = process.getWorkshop().getBuilding().getName();
        	JLabel buildingNameLabel = new JLabel(buildingName, SwingConstants.CENTER);
        	add(buildingNameLabel);
        }

        // Prepare work panel.
        JPanel workPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        add(workPane);

        // Prepare work label.
        JLabel workLabel = new JLabel("Work: ", SwingConstants.LEFT);
        workPane.add(workLabel);

        // Prepare work progress bar.
        JProgressBar workBar = new JProgressBar();
        workBarModel = workBar.getModel();
        workBar.setStringPainted(true);
        workPane.add(workBar);

        // Prepare time panel.
        JPanel timePane = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        add(timePane);

        // Prepare time label.
        JLabel timeLabel = new JLabel("Time: ", SwingConstants.LEFT);
        timePane.add(timeLabel);

        // Prepare time progress bar.
        JProgressBar timeBar = new JProgressBar();
        timeBarModel = timeBar.getModel();
        timeBar.setStringPainted(true);
        timePane.add(timeBar);

        // Update progress bars.
        update();

        // Add tooltip.
        setToolTipText(ProcessInfoRenderer.getToolTipString(process.getInfo()));
	}

    /**
     * Updates the panel's information.
     */
    public void update() {		
    	// Update work progress bar.
    	double workTimeRequired = process.getInfo().getWorkTimeRequired();
        double workTimeRemaining = process.getWorkTimeRemaining();
        int workProgress = 100;
        if (workTimeRequired > 0D) workProgress = (int) (100D * (workTimeRequired - workTimeRemaining) / workTimeRequired);
        workBarModel.setValue(workProgress);

        // Update time progress bar.
        double timeRequired = process.getInfo().getProcessTimeRequired();
        double timeRemaining = process.getProcessTimeRemaining();
        int timeProgress = 100;
        if (timeRequired > 0D) timeProgress = (int) (100D * (timeRequired - timeRemaining) / timeRequired);
        timeBarModel.setValue(timeProgress);
    }

    /**
     * Gets the manufacture process.
     * 
     * @return process
     */
    public ManufactureProcess getManufactureProcess() {
    	return process;
    }

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
	    process = null;
		workBarModel = null;
		timeBarModel = null;
	}
}
