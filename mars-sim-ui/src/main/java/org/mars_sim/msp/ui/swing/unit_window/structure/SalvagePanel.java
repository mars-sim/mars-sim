/**
 * Mars Simulation Project
 * SalvagePanel.java
 * @version 2.90 2010-02-23
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.BoundedRangeModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.mars_sim.msp.core.manufacture.PartSalvage;
import org.mars_sim.msp.core.manufacture.SalvageProcess;
import org.mars_sim.msp.core.manufacture.SalvageProcessInfo;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;

/**
 * A panel displaying information about a salvage process.
 */
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
        JButton cancelButton = new JButton(ImageLoader.getIcon("CancelSmall"));
        cancelButton.setMargin(new Insets(0, 0, 0, 0));
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    getSalvageProcess().getWorkshop().endSalvageProcess(getSalvageProcess(), true);
                }
                catch (BuildingException e) {}
            }
        });
        cancelButton.setToolTipText("Cancel salvage process");
        namePane.add(cancelButton);
        
        // Prepare name label.
        String name = process.toString() + ": " + process.getSalvagedUnit().getName();
        if (name.length() > 0) {
            String firstLetter = name.substring(0, 1).toUpperCase();
            name = " " + firstLetter + name.substring(1);
        }
        if (name.length() > processStringWidth) name = name.substring(0, processStringWidth) + "...";
        JLabel nameLabel = new JLabel(name, JLabel.CENTER);
        namePane.add(nameLabel);

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
     * @return process
     */
    public SalvageProcess getSalvageProcess() {
        return process;
    }
    
    /**
     * Gets a tool tip string for a salvage process.
     * @param process the salvage process.
     * @param building the manufacturing building (or null if none).
     */
    public static String getToolTipString(SalvageProcess process, SalvageProcessInfo processInfo, 
            Building building) {
        StringBuffer result = new StringBuffer("<html>");
        
        result.append("Salvage Process: " + processInfo.toString() + "<br>");
        if (building != null) result.append("Manufacture Building: " + building.getName() + "<br>");
        result.append("Effort Time Required: " + processInfo.getWorkTimeRequired() + " millisols<br>");
        result.append("Building Tech Level Required: " + processInfo.getTechLevelRequired() + "<br>");
        result.append("Materials Science Skill Level Required: " + processInfo.getSkillLevelRequired() + "<br>");
        
        // Add salvaged item.
        if (process != null) result.append("Salvaged Item: " + process.getSalvagedUnit().getName() + "<br>");
        else result.append("Salvaged Item Type: " + processInfo.getItemName() + "<br>");
        
        // Add process outputs.
        result.append("Possible Parts Returned:<br>");
        Iterator<PartSalvage> j = processInfo.getPartSalvageList().iterator();
        while (j.hasNext()) {
            PartSalvage partSalvage = j.next();
            result.append("&nbsp;&nbsp;" + partSalvage.getName() + ": " + partSalvage.getNumber() + "<br>");
        }
        
        result.append("</html>");
        
        return result.toString();
    }
}