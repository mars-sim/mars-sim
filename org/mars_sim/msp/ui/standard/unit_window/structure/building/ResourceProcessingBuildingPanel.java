/**
 * Mars Simulation Project
 * ResourceProcessingBuildingPanel.java
 * @version 2.75 2004-04-05
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.standard.unit_window.structure.building;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import org.mars_sim.msp.simulation.structure.building.function.*;
import org.mars_sim.msp.ui.standard.*;

/**
 * The ResourceProcessingBuildingPanel class is a building function panel representing 
 * the resource processes of a settlement building.
 */
public class ResourceProcessingBuildingPanel extends BuildingFunctionPanel {
    
    /**
     * Constructor
     *
     * @param processor the resource processing building this panel is for.
     * @param desktop The main desktop.
     */
    public ResourceProcessingBuildingPanel(ResourceProcessing processor, MainDesktopPane desktop) {
        
        // Use BuildingFunctionPanel constructor
        super(processor.getBuilding(), desktop);
        
        // Set layout
        setLayout(new BorderLayout());
        
        // Prepare resource processes label
        JLabel resourceProcessesLabel = new JLabel("Resource Processes", JLabel.CENTER);
        add(resourceProcessesLabel, BorderLayout.NORTH);
        
        // Get all processes at building.
        Collection processes = processor.getProcesses();
        
        // Prepare resource processes list panel.
        JPanel resourceProcessesListPanel = new JPanel(new GridLayout(processes.size(), 1, 0, 0));
        resourceProcessesListPanel.setBorder(new MarsPanelBorder());
        add(resourceProcessesListPanel, BorderLayout.CENTER);
        
        // For each resource process, add a label.
        Iterator i = processes.iterator();
        while (i.hasNext()) {
            ResourceProcess process = (ResourceProcess) i.next();
            JLabel processLabel = new JLabel(process.getProcessName(), JLabel.CENTER);
            resourceProcessesListPanel.add(processLabel);
        }
    }
    
    /**
     * Update this panel
     */
    public void update() {}
}
