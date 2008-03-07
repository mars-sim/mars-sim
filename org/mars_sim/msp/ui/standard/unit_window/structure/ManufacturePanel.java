/**
 * Mars Simulation Project
 * ManufacturePanel.java
 * @version 2.83 2008-02-18
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.unit_window.structure;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.Iterator;

import javax.swing.BoundedRangeModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.mars_sim.msp.simulation.manufacture.ManufactureProcess;
import org.mars_sim.msp.simulation.manufacture.ManufactureProcessInfo;
import org.mars_sim.msp.simulation.manufacture.ManufactureProcessItem;
import org.mars_sim.msp.ui.standard.MarsPanelBorder;

public class ManufacturePanel extends JPanel {
	
	// Data members
	private ManufactureProcess process;
	private BoundedRangeModel workBarModel;
	private BoundedRangeModel timeBarModel;
	
	/**
	 * Constructor
	 * @param process the manufacturing process.
	 * @param showBuilding is the building name shown?
	 */
	public ManufacturePanel(ManufactureProcess process, boolean showBuilding) {
		// Call JPanel constructor
		super();
		
		// Initialize data members.
		this.process = process;
		
        // Set layout
		if (showBuilding) setLayout(new GridLayout(4, 1, 0, 0));
		else setLayout(new GridLayout(3, 1, 0, 0));

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

        if (showBuilding) {
        	// Prepare building name label.
        	String buildingName = process.getWorkshop().getBuilding().getName();
        	JLabel buildingNameLabel = new JLabel(buildingName, JLabel.CENTER);
        	add(buildingNameLabel);
        }
        
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
        
        // Add tooltip.
        setToolTipText(getToolTipString());
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
    
    /**
     * Adds the tool tip for the manufacturing panel.
     */
    private String getToolTipString() {
    	StringBuffer result = new StringBuffer("<html>");
    	
    	ManufactureProcessInfo info = process.getInfo();
    	result.append("Manufacturing Process: " + info.getName() + "<br>");
    	result.append("Manufacture Building: " + process.getWorkshop().getBuilding().getName() + "<br>");
    	result.append("Effort Time Required: " + info.getWorkTimeRequired() + " millisols<br>");
    	result.append("Process Time Required: " + info.getProcessTimeRequired() + " millisols<br>");
    	result.append("Building Tech Level Required: " + info.getTechLevelRequired() + "<br>");
    	result.append("Materials Science Skill Level Required: " + info.getSkillLevelRequired() + "<br>");
    	
    	// Add process inputs.
    	result.append("Process Inputs:<br>");
    	Iterator<ManufactureProcessItem> i = info.getInputList().iterator();
    	while (i.hasNext()) {
    		ManufactureProcessItem item = i.next();
    		result.append("&nbsp;&nbsp;" + item.getName() + ": " + getItemAmountString(item) + "<br>");
    	}
    	
    	// Add process outputs.
    	result.append("Process Outputs:<br>");
    	Iterator<ManufactureProcessItem> j = info.getOutputList().iterator();
    	while (j.hasNext()) {
    		ManufactureProcessItem item = j.next();
    		result.append("&nbsp;&nbsp;" + item.getName() + ": " + getItemAmountString(item) + "<br>");
    	}
    	
    	result.append("</html>");
    	
    	return result.toString();
    }
    
    private String getItemAmountString(ManufactureProcessItem item) {
    	String result = "";
    	if (ManufactureProcessItem.AMOUNT_RESOURCE.equals(item.getType())) 
			result = item.getAmount() + " kg";
		else result = Integer.toString((int) item.getAmount());
    	return result;
    }
}