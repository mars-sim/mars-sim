/**
 * Mars Simulation Project
 * ManufactureTabPanel.java
 * @version 2.83 2008-02-17
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.mars_sim.msp.simulation.Unit;
import org.mars_sim.msp.simulation.manufacture.ManufactureProcess;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.Building;
import org.mars_sim.msp.simulation.structure.building.BuildingException;
import org.mars_sim.msp.simulation.structure.building.function.Manufacture;
import org.mars_sim.msp.ui.standard.MainDesktopPane;
import org.mars_sim.msp.ui.standard.MarsPanelBorder;
import org.mars_sim.msp.ui.standard.unit_window.TabPanel;

public class ManufactureTabPanel extends TabPanel {

	// Data members
	private Settlement settlement;
	private JPanel manufactureListPane;
	private List<ManufactureProcess> processCache;
	
    /**
     * Constructor
     * @param unit the unit to display.
     * @param desktop the main desktop.
     */
	public ManufactureTabPanel(Unit unit, MainDesktopPane desktop) {
        // Use the TabPanel constructor
        super("Manu", null, "Manufacturing", unit, desktop);
        
        settlement = (Settlement) unit;
        
        // Create topPanel.
        JPanel topPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topContentPanel.add(topPane);
        
        // Create manufacture label.
        JLabel manufactureLabel = new JLabel("Manufacturing", JLabel.CENTER);
        topPane.add(manufactureLabel);
        
		// Create scroll panel for manufacture list pane.
        JScrollPane manufactureScrollPane = new JScrollPane();
        manufactureScrollPane.setPreferredSize(new Dimension(220, 280));
        topContentPanel.add(manufactureScrollPane, BorderLayout.CENTER);  
		
        // Prepare manufacture outer list pane.
        JPanel manufactureOuterListPane = new JPanel(new BorderLayout(0, 0));
        manufactureOuterListPane.setBorder(new MarsPanelBorder());
        manufactureScrollPane.setViewportView(manufactureOuterListPane);
        
        // Prepare malfunctions list pane.
        manufactureListPane = new JPanel();
        manufactureListPane.setLayout(new BoxLayout(manufactureListPane, BoxLayout.Y_AXIS));
        manufactureOuterListPane.add(manufactureListPane, BorderLayout.NORTH);
        
        processCache = getManufactureProcesses();
        Iterator<ManufactureProcess> i = processCache.iterator();
        while (i.hasNext()) manufactureListPane.add(new ManufacturePanel(i.next(), true));
	}
	
	@Override
	public void update() {
		
		// Update processes if necessary.
		List<ManufactureProcess> processes = getManufactureProcesses();
		if (!processCache.equals(processes)) {
			
			// Add manufacture panels for new processes.
			Iterator<ManufactureProcess> i = processes.iterator();
			while (i.hasNext()) {
				ManufactureProcess process = i.next();
				if (!processCache.contains(process)) 
					manufactureListPane.add(new ManufacturePanel(process, true));
			}
			
			// Remove manufacture panels for old processes.
			Iterator<ManufactureProcess> j = processCache.iterator();
			while (j.hasNext()) {
				ManufactureProcess process = j.next();
				if (!processes.contains(process)) {
					ManufacturePanel panel = getManufacturePanel(process);
					if (panel != null) manufactureListPane.remove(panel);
				}
			}
			
			// Update processCache
			processCache.clear();
			processCache.addAll(processes);
		}
		
		// Update all process panels.
		Iterator<ManufactureProcess> i = processes.iterator();
		while (i.hasNext()) {
			ManufacturePanel panel = getManufacturePanel(i.next());
			if (panel != null) panel.update();
		}
	}
	
	/**
	 * Gets all the manufacture processes at the settlement.
	 * @return list of manufacture processes.
	 */
	private List<ManufactureProcess> getManufactureProcesses() {
		List<ManufactureProcess> result = new ArrayList<ManufactureProcess>();
		
		Iterator<Building> i = settlement.getBuildingManager().getBuildings(Manufacture.NAME).iterator();
		while (i.hasNext()) {
			try {
				Manufacture workshop = (Manufacture) i.next().getFunction(Manufacture.NAME);
				result.addAll(workshop.getProcesses());
			}
			catch (BuildingException e) {}
		}
		
		return result;
	}
	
	/**
	 * Gets the panel for a manufacture process.
	 * @param process the manufacture process.
	 * @return manufacture panel or null if none.
	 */
	private ManufacturePanel getManufacturePanel(ManufactureProcess process) {
		ManufacturePanel result = null;
		
		for (int x = 0; x < manufactureListPane.getComponentCount(); x++) {
			Component component = manufactureListPane.getComponent(x);
			if (component instanceof ManufacturePanel) {
				ManufacturePanel panel = (ManufacturePanel) component;
				if (panel.getManufactureProcess().equals(process)) result = panel;
			}
		}
		
		return result;
	}
}