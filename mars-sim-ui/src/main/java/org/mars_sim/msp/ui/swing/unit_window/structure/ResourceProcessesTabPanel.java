/**
 * Mars Simulation Project
 * ResourceProcessTabTabPanel.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_window.structure;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.ResourceProcess;
import org.mars_sim.msp.core.structure.building.function.ResourceProcessing;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;

/**
 * A tab panel for displaying all of the resource processes in a settlement.
 */
public class ResourceProcessesTabPanel extends TabPanel {

	// Data members
	private List<Building> processingBuildings;
	private JScrollPane processesScrollPane;
	private JPanel processListPanel;
	private JCheckBox overrideCheckbox;
	
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
		processesScrollPane = new JScrollPane();
		processesScrollPane.setPreferredSize(new Dimension(220, 280));
		// increase vertical mousewheel scrolling speed for this one
		processesScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		centerContentPanel.add(processesScrollPane,BorderLayout.CENTER);         
        
        // Prepare process list panel.
        processListPanel = new JPanel(new GridLayout(0, 1, 5, 2));
        processListPanel.setBorder(new MarsPanelBorder());
        processesScrollPane.setViewportView(processListPanel);
        populateProcessList();
        
        // Create override check box panel.
        JPanel overrideCheckboxPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topContentPanel.add(overrideCheckboxPane,BorderLayout.SOUTH);
        
        // Create override check box.
        overrideCheckbox = new JCheckBox("Override resource process toggling");
        overrideCheckbox.setToolTipText("Prevents settlement inhabitants from " +
        		"toggling on/off resource processes.");
        overrideCheckbox.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		setResourceProcessOverride(overrideCheckbox.isSelected());
        	}
        });
        overrideCheckbox.setSelected(settlement.getManufactureOverride());
        overrideCheckboxPane.add(overrideCheckbox);
    }
    
    /**
     * Populates the process list panel with all building processes.
     */
    private void populateProcessList() {
    	// Clear the list.
    	processListPanel.removeAll();
    	
//    	try {
    		// Add a label for each process in each processing building.
    		Iterator<Building> i = processingBuildings.iterator();
    		while (i.hasNext()) {
    			Building building = i.next();
    			ResourceProcessing processing = (ResourceProcessing) building.getFunction(ResourceProcessing.NAME);
    			Iterator<ResourceProcess> j = processing.getProcesses().iterator();
    			while (j.hasNext()) {
    				ResourceProcess process = j.next();
    				processListPanel.add(new ResourceProcessPanel(process, building));
    			}
    		}
//    	}
//    	catch (BuildingException e) {
//    		e.printStackTrace(System.err);
//    	}
    }
	
	@Override
	public void update() {
		
		// Check if building list has changed.
		Settlement settlement = (Settlement) unit;
		List<Building> tempBuildings = settlement.getBuildingManager().getBuildings(ResourceProcessing.NAME);
		if (!tempBuildings.equals(processingBuildings)) {
			// Populate process list.
			processingBuildings = tempBuildings;
			populateProcessList();
			processesScrollPane.validate();
		}
		else {
			// Update process list.
			Component[] components = processListPanel.getComponents();
            for (Component component : components) {
                ResourceProcessPanel panel = (ResourceProcessPanel) component;
                panel.update();
            }
		}
	}
	
	/**
	 * Sets the settlement resource process override flag.
	 * @param override the resource process override flag.
	 */
	private void setResourceProcessOverride(boolean override) {
		Settlement settlement = (Settlement) unit;
		settlement.setResourceProcessOverride(override);
	}
	
	/**
	 * An internal class for a resource process panel.
	 */
	private static class ResourceProcessPanel extends JPanel {
		
		// Data members.
		private ResourceProcess process;
		private JLabel label;
		private JButton toggleButton;
		private ImageIcon greenDot;
		private ImageIcon redDot;
		private DecimalFormat decFormatter = new DecimalFormat("0.00");
		
		/**
		 * Constructor
		 * @param process the resource process.
		 * @param building the building the process is in.
		 */
		ResourceProcessPanel(ResourceProcess process, Building building) {
			// Use JPanel constructor.
			super();
			
			setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			
			this.process = process;
			
	        toggleButton = new JButton();
	        toggleButton.setMargin(new Insets(0, 0, 0, 0));
	        toggleButton.addActionListener(new ActionListener() {
	        	public void actionPerformed(ActionEvent event) {
	        		ResourceProcess process = getProcess();
	        		process.setProcessRunning(!process.isProcessRunning());
	        		update();
	        	}
	        });
	        toggleButton.setToolTipText("Toggle process on/off");
	        add(toggleButton);
	        
	        label = new JLabel(" " + building.getName() + ": " + process.getProcessName());
	        add(label);
			
			// Load green and red dots.
	        greenDot = ImageLoader.getIcon("GreenDot");
	        redDot = ImageLoader.getIcon("RedDot");
	        
			if (process.isProcessRunning()) toggleButton.setIcon(greenDot);
			else toggleButton.setIcon(redDot);
			
			setToolTipText(getToolTipString(building));
		}
		
		private String getToolTipString(Building building) {
            StringBuilder result = new StringBuilder("<html>");

            result.append("Resource Process: ").append(process.getProcessName()).append("<br>");
            result.append("Building: ").append(building.getName()).append("<br>");

            result.append("Power Required: ").append(decFormatter.format(process.getPowerRequired())).append(" kW<br>");
			
			result.append("Process Inputs:<br>");
			Iterator<AmountResource> i = process.getInputResources().iterator();
			while (i.hasNext()) {
				AmountResource resource = i.next();
				double rate = process.getMaxInputResourceRate(resource) * 1000D;
				String rateString = decFormatter.format(rate);
				result.append("&nbsp;&nbsp;");
				if (process.isAmbientInputResource(resource)) result.append("* ");
                result.append(resource.getName()).append(": ").append(rateString).append(" kg/sol<br>");
			}
			
			result.append("Process Outputs:<br>");
			Iterator<AmountResource> j = process.getOutputResources().iterator();
			while (j.hasNext()) {
				AmountResource resource = j.next();
				double rate = process.getMaxOutputResourceRate(resource) * 1000D;
				String rateString = decFormatter.format(rate);
                result.append("&nbsp;&nbsp;").append(resource.getName()).append(": ").append(rateString).append(" kg/sol<br>");
			}
			
			result.append("</html>");
			
			return result.toString();
		}
		
		/**
		 * Update the label.
		 */
		void update() {
			if (process.isProcessRunning()) toggleButton.setIcon(greenDot);
			else toggleButton.setIcon(redDot);
		}
		
		private ResourceProcess getProcess() {
			return process;
		}
	}
}