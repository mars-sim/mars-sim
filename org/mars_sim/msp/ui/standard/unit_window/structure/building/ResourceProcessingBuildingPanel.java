/**
 * Mars Simulation Project
 * ResourceProcessingBuildingPanel.java
 * @version 2.87 2009-10-04
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.standard.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.mars_sim.msp.simulation.structure.building.function.ResourceProcess;
import org.mars_sim.msp.simulation.structure.building.function.ResourceProcessing;
import org.mars_sim.msp.ui.standard.MainDesktopPane;
import org.mars_sim.msp.ui.standard.MarsPanelBorder;


/**
 * The ResourceProcessingBuildingPanel class is a building function panel representing 
 * the resource processes of a settlement building.
 */
public class ResourceProcessingBuildingPanel extends BuildingFunctionPanel {
    
	// Data members
	private ResourceProcessing processor;
	private List<JLabel> processLabels;
	private ImageIcon greenDot;
	private ImageIcon redDot;
	
    /**
     * Constructor
     *
     * @param processor the resource processing building this panel is for.
     * @param desktop The main desktop.
     */
    public ResourceProcessingBuildingPanel(ResourceProcessing processor, MainDesktopPane desktop) {
        
        // Use BuildingFunctionPanel constructor
        super(processor.getBuilding(), desktop);
        
        // Initialize variables.
        this.processor = processor;
        
        // Set layout
        setLayout(new BorderLayout());
        
        // Prepare resource processes label
        JLabel resourceProcessesLabel = new JLabel("Resource Processes", JLabel.CENTER);
        add(resourceProcessesLabel, BorderLayout.NORTH);
        
        // Get all processes at building.
        List<ResourceProcess> processes = processor.getProcesses();
        
        // Prepare resource processes list panel.
        JPanel resourceProcessesListPanel = new JPanel(new GridLayout(processes.size(), 1, 0, 0));
        resourceProcessesListPanel.setBorder(new MarsPanelBorder());
        add(resourceProcessesListPanel, BorderLayout.CENTER);
        
        // Load green and red dots.
        greenDot = new ImageIcon("images/GreenDot.png", "Process is running.");
        redDot = new ImageIcon("images/RedDot.png", "Process is not running");
        
        // For each resource process, add a label.
        processLabels = new ArrayList<JLabel>(processes.size());
        Iterator<ResourceProcess> i = processes.iterator();
        while (i.hasNext()) {
            ResourceProcess process = i.next();
            JLabel processLabel = new JLabel(process.getProcessName(), JLabel.LEFT);
            
            if (process.isProcessRunning()) {
            	processLabel.setIcon(greenDot);
            	processLabel.setToolTipText(process.getProcessName() + " process is running.");
            }
            else {
            	processLabel.setIcon(redDot);
            	processLabel.setToolTipText(process.getProcessName() + " process is not running.");
            }
            
            resourceProcessesListPanel.add(processLabel);
            processLabels.add(processLabel);
        }
    }
    
    /**
     * Update this panel
     */
    public void update() {
    
    	List<ResourceProcess> processes = processor.getProcesses();
    	for (int x=0; x < processes.size(); x++) {
    		ResourceProcess process = processes.get(x);
    		JLabel processLabel = (JLabel) processLabels.get(x);
    		if (process.isProcessRunning()) {
    			processLabel.setIcon(greenDot);
            	processLabel.setToolTipText(process.getProcessName() + " process is running.");
    		}
    		else {
            	processLabel.setIcon(redDot);
            	processLabel.setToolTipText(process.getProcessName() + " process is not running.");
            }
    	}
    }
}