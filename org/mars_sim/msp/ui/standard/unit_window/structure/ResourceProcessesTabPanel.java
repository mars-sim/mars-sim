/**
 * Mars Simulation Project
 * ResourceProcessTabTabPanel.java
 * @version 2.81 2007-05-24
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.unit_window.structure;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.Iterator;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.mars_sim.msp.simulation.Unit;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.Building;
import org.mars_sim.msp.simulation.structure.building.BuildingException;
import org.mars_sim.msp.simulation.structure.building.function.ResourceProcess;
import org.mars_sim.msp.simulation.structure.building.function.ResourceProcessing;
import org.mars_sim.msp.ui.standard.MainDesktopPane;
import org.mars_sim.msp.ui.standard.MarsPanelBorder;
import org.mars_sim.msp.ui.standard.unit_window.TabPanel;

/**
 * A tab panel for displaying all of the resource processes in a settlement.
 */
public class ResourceProcessesTabPanel extends TabPanel {

	// Data members
	private List processingBuildings;
	private JScrollPane processesScrollPanel;
	private JPanel processListPanel;
	
    /**
     * Constructor
     * @param unit the unit to display.
     * @param desktop the main desktop.
     */
    public ResourceProcessesTabPanel(Unit unit, MainDesktopPane desktop) { 
        
        // Use the TabPanel constructor
        super("Processes", null, "Resource Processes", unit, desktop);
        
        Settlement settlement = (Settlement) unit;
        processingBuildings = settlement.getBuildingManager().getBuildings(ResourceProcessing.NAME);
        
        // Prepare resource processes label panel.
        JPanel resourceProcessesLabelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topContentPanel.add(resourceProcessesLabelPanel);
        
        // Prepare esource processes label.
        JLabel resourceProcessesLabel = new JLabel("Resource Processes", JLabel.CENTER);
        resourceProcessesLabelPanel.add(resourceProcessesLabel);
        
		// Create scroll panel for the outer table panel.
		processesScrollPanel = new JScrollPane();
		processesScrollPanel.setPreferredSize(new Dimension(220, 280));
		topContentPanel.add(processesScrollPanel);         
        
        // Prepare process list panel.
        processListPanel = new JPanel(new GridLayout(0, 1, 5, 2));
        processListPanel.setBorder(new MarsPanelBorder());
        processesScrollPanel.setViewportView(processListPanel);
        populateProcessList();
    }
    
    /**
     * Populates the process list panel with all building processes.
     */
    private void populateProcessList() {
    	// Clear the list.
    	processListPanel.removeAll();
    	
    	try {
    		// Add a label for each process in each processing building.
    		Iterator i = processingBuildings.iterator();
    		while (i.hasNext()) {
    			Building building = (Building) i.next();
    			ResourceProcessing processing = (ResourceProcessing) building.getFunction(ResourceProcessing.NAME);
    			Iterator j = processing.getProcesses().iterator();
    			while (j.hasNext()) {
    				ResourceProcess process = (ResourceProcess) j.next();
    				processListPanel.add(new ResourceProcessLabel(process, building));
    			}
    		}
    	}
    	catch (BuildingException e) {
    		e.printStackTrace(System.err);
    	}
    }
	
	@Override
	public void update() {
		
		// Check if building list has changed.
		Settlement settlement = (Settlement) unit;
		List tempBuildings = settlement.getBuildingManager().getBuildings(ResourceProcessing.NAME);
		if (!tempBuildings.equals(processingBuildings)) {
			// Populate process list.
			processingBuildings = tempBuildings;
			populateProcessList();
			processesScrollPanel.validate();
		}
		else {
			// Update process list.
			Component[] components = processListPanel.getComponents();
			for (int x = 0; x < components.length; x++) {
				ResourceProcessLabel label = (ResourceProcessLabel) components[x];
				label.update();
			}
		}
	}
	
	/**
	 * An internal class for a resource process label.
	 */
	private class ResourceProcessLabel extends JLabel {
		
		// Data members.
		private ResourceProcess process;
		private ImageIcon greenDot;
		private ImageIcon redDot;
		
		/**
		 * Constructor
		 * @param process the resource process.
		 * @param building the building the process is in.
		 */
		ResourceProcessLabel(ResourceProcess process, Building building) {
			// Use JLabel constructor.
			super();
			
			this.process = process;
			
			// Load green and red dots.
	        greenDot = new ImageIcon("images/GreenDot.gif", "Process is running.");
	        redDot = new ImageIcon("images/RedDot.gif", "Process is not running");
			
			setText(building.getName() + ": " + process.getProcessName());
			
			if (process.isProcessRunning()) {
				setIcon(greenDot);
				setToolTipText(getText() + " process is running.");
			}
			else {
				setIcon(redDot);
				setToolTipText(getText() + " process is not running.");
			}
		}
		
		/**
		 * Update the label.
		 */
		void update() {
			if (process.isProcessRunning()) {
				setIcon(greenDot);
				setToolTipText(getText() + " process is running.");
			}
			else {
				setIcon(redDot);
				setToolTipText(getText() + " process is not running.");
			}
		}
	}
}