/*
 * Mars Simulation Project
 * WorkshopProcessPanel.java
 * @date 2021-09-20
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.utils;

import java.awt.FlowLayout;
import java.awt.Insets;

import javax.swing.BoundedRangeModel;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import com.mars_sim.core.manufacture.WorkshopProcess;
import com.mars_sim.core.tool.Conversion;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MarsPanelBorder;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.EntityLabel;

/**
 * A panel showing information about a Workshop process.
 */
@SuppressWarnings("serial")
class WorkshopProcessPanel extends JPanel {
	// Data members
	private WorkshopProcess process;
	private BoundedRangeModel workBarModel;
	private BoundedRangeModel timeBarModel;

	/**
	 * Constructor
	 * @param process the manufacturing process.
	 * @param showBuilding is the building name shown?
	 * @param processStringWidth the max string width to display for the process name.
	 * @param context Context of the UI
	 */
	public WorkshopProcessPanel(WorkshopProcess process, boolean showBuilding, int processStringWidth, UIContext context) {

		// Initialize data members.
		this.process = process;

        boolean showProcessTime = process.getInfo().getProcessTimeRequired() > 0;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Set border
        setBorder(new MarsPanelBorder());

        // Prepare name panel.
        JPanel namePane = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 0));
        add(namePane);

        // Prepare cancel button.
        JButton cancelButton = new JButton(ImageLoader.getIconByName("action/cancel"));
        cancelButton.setMargin(new Insets(0, 0, 0, 0));
        cancelButton.addActionListener(event ->
                    getProcess().stopProcess(true));
        cancelButton.setToolTipText("Cancel process");
        namePane.add(cancelButton);

        // Prepare name label.
        String name = Conversion.trim(process.getInfo().getName(), processStringWidth);
        JLabel nameLabel = new JLabel(name, SwingConstants.CENTER);
        namePane.add(nameLabel);

        if (showBuilding) {
        	// Prepare building name label.
        	var building = process.getWorkshop().getBuilding();
        	var buildingNameLabel = new EntityLabel(building, context);
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
        if (showProcessTime) {
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
        }

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
        if (timeBarModel != null) {
            double timeRequired = process.getInfo().getProcessTimeRequired();
            double timeRemaining = process.getProcessTimeRemaining();
            int timeProgress = 100;
            if (timeRequired > 0D) timeProgress = (int) (100D * (timeRequired - timeRemaining) / timeRequired);
            timeBarModel.setValue(timeProgress);
        }
    }

    /**
     * Gets the manufacture process.
     * 
     * @return process
     */
    public WorkshopProcess getProcess() {
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
