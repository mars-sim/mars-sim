/*
 * Mars Simulation Project
 * SalvagePanel.java
 * @date 2021-09-20
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.utils;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.manufacture.SalvageProcess;
import com.mars_sim.core.manufacture.SalvageProcessInfo;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MarsPanelBorder;

import javax.swing.*;
import java.awt.*;

/**
 * A panel displaying information about a salvage process.
 */
@SuppressWarnings("serial")
public class SalvagePanel extends JPanel {

    // Data members
    private SalvageProcess process;
    private BoundedRangeModel workBarModel;
    
    /**
     * Constructor
     * @param process the salvage process.
     * @param showBuilding is the building name shown?
     * @param processStringWidth the max string width to display for the process name.
     */
    public SalvagePanel(SalvageProcess process, boolean showBuilding, int processStringWidth) {
        // Call JPanel constructor
        super();
        
        // Initialize data members.
        this.process = process;
        
        // Set layout
        if (showBuilding) setLayout(new GridLayout(3, 1, 0, 0));
        else setLayout(new GridLayout(2, 1, 0, 0));

        // Set border
        setBorder(new MarsPanelBorder());
        
        // Prepare name panel.
        JPanel namePane = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 0));
        add(namePane);
        
        // Prepare cancel button.
        JButton cancelButton = new JButton(ImageLoader.getIconByName("action/cancel"));
        cancelButton.setMargin(new Insets(0, 0, 0, 0));
        cancelButton.addActionListener(event ->
                        getSalvageProcess().getWorkshop().endSalvageProcess(getSalvageProcess(), true));
        cancelButton.setToolTipText("Cancel salvage process");
        namePane.add(cancelButton);
        
        // Prepare name label.
        String name = process.toString() + ": " + process.getSalvagedUnit().getName();
        if (name.length() > 0) {
            String firstLetter = name.substring(0, 1).toUpperCase();
            name = " " + firstLetter + name.substring(1);
        }
        if (name.length() > processStringWidth) name = name.substring(0, processStringWidth) + "...";
        JLabel nameLabel = new JLabel(name, SwingConstants.CENTER);
        namePane.add(nameLabel);

        if (showBuilding) {
            // Prepare building name label.
            String buildingName = process.getWorkshop().getBuilding().getBuildingType();
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
        
        // Update progress bars.
        update();
        
        // Add tooltip.
        setToolTipText(getToolTipString(process, process.getInfo(), process.getWorkshop().getBuilding()));
    }
    
    /**
     * Updates the panel's information.
     */
    public void update() {
        // Update work progress bar.
        double workTimeRequired = process.getInfo().getWorkTimeRequired();
        double workTimeRemaining = process.getWorkTimeRemaining();
        int workProgress = 100;
        if (workTimeRequired > 0D) workProgress = 
            (int) (100D * (workTimeRequired - workTimeRemaining) / workTimeRequired);
        workBarModel.setValue(workProgress);
    }
    
    /**
     * Gets the salvage process.
     * 
     * @return process
     */
    public SalvageProcess getSalvageProcess() {
        return process;
    }
    
    /**
     * Gets a tool tip string for a salvage process.
     * 
     * @param process the salvage process.
     * @param building the manufacturing building (or null if none).
     */
    public static String getToolTipString(SalvageProcess process, SalvageProcessInfo processInfo, 
            Building building) {
        StringBuilder result = new StringBuilder("<html>");

        result.append("Salvage Process: ").append(processInfo.getName()).append("<br>");
        if (building != null) result.append("Manufacture Building: ").append(building.getBuildingType()).append("<br>");
        result.append("Effort Time Req'd: ").append(processInfo.getWorkTimeRequired()).append(" millisols<br>");
        result.append("Building Tech Level Req'd: ").append(processInfo.getTechLevelRequired()).append("<br>");
        result.append("Materials Science Skill Level Req'd: ").append(processInfo.getSkillLevelRequired()).append("<br>");
        
        // Add salvaged item.
        if (process != null) result.append("Salvaged Item: ").append(process.getSalvagedUnit().getName()).append("<br>");
        else result.append("Salvaged Item Type: ").append(processInfo.getItemName()).append("<br>");
        
        // Add process outputs.
        result.append("Possible Parts Returned:<br>");
        for(var partSalvage : processInfo.getOutputList()) {
            result.append("&nbsp;&nbsp;").append(partSalvage.getName()).append(": ").append(partSalvage.getAmount()).append("<br>");
        }
        
        result.append("</html>");
        
        return result.toString();
    }
    
	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		process = null;
	    workBarModel = null;
	}
}
