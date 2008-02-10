/**
 * Mars Simulation Project
 * ManufacturePanel.java
 * @version 2.83 2008-02-09
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.unit_window.structure;

import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.BoundedRangeModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.mars_sim.msp.simulation.manufacture.ManufactureProcess;
import org.mars_sim.msp.ui.standard.MarsPanelBorder;

public class ManufacturePanel extends JPanel {
	
	// Data members
	private ManufactureProcess process;
	private BoundedRangeModel workBarModel;
	private BoundedRangeModel timeBarModel;
	
	/**
	 * Constructor
	 * @param process the manufacturing process.
	 */
	public ManufacturePanel(ManufactureProcess process) {
		// Call JPanel constructor
		super();
		
		// Initialize data members.
		this.process = process;
		
        // Set layout
        setLayout(new GridLayout(3, 1, 0, 0));

        // Set border
        setBorder(new MarsPanelBorder());
        
        // Prepare name label.
        String name = process.getInfo().getName();
        if (name.length() > 0) {
        	String firstLetter = name.substring(0, 1).toUpperCase();
        	name = firstLetter + name.substring(1);
        }
        JLabel nameLabel = new JLabel(name, JLabel.CENTER);
        add(nameLabel);
        
        // Prepare work panel.
        JPanel workPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        add(workPane);
        
        // Prepare work label.
        JLabel workLabel = new JLabel("Work: ", JLabel.LEFT);
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
        JLabel timeLabel = new JLabel("Time: ", JLabel.LEFT);
        timePane.add(timeLabel);
        
        // Prepare time progress bar.
        JProgressBar timeBar = new JProgressBar();
        timeBarModel = timeBar.getModel();
        timeBar.setStringPainted(true);
        timePane.add(timeBar);
        
        // Update progress bars.
        update();
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
     * @return process
     */
    public ManufactureProcess getManufactureProcess() {
    	return process;
    }
}