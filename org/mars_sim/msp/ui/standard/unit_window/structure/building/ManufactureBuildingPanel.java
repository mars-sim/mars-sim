/**
 * Mars Simulation Project
 * ManufactureBuildingPanel.java
 * @version 2.83 2008-02-10
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.mars_sim.msp.simulation.manufacture.ManufactureProcess;
import org.mars_sim.msp.simulation.structure.building.function.Manufacture;
import org.mars_sim.msp.ui.standard.MainDesktopPane;
import org.mars_sim.msp.ui.standard.unit_window.structure.ManufacturePanel;

public class ManufactureBuildingPanel extends BuildingFunctionPanel {

	private Manufacture workshop; // The manufacture building.
	private JPanel processListPane; // Panel for displaying process panels.
	private List<ManufactureProcess> processCache; // List of manufacture processes in building.
	
	/**
	 * Constructor
	 * @param workshop the manufacturing building function.
	 * @param desktop the main desktop.
	 */
	public ManufactureBuildingPanel(Manufacture workshop, MainDesktopPane desktop) {
		// Use BuildingFunctionPanel constructor.
		super(workshop.getBuilding(), desktop);
		
		// Initialize data model.
		this.workshop = workshop;
		
        // Set panel layout
        setLayout(new BorderLayout());
        
        // Prepare label panel
        JPanel labelPane = new JPanel(new GridLayout(3, 1, 0, 0));
        add(labelPane, BorderLayout.NORTH);
        
        // Prepare manufacturing label
        JLabel manufactureLabel = new JLabel("Manufacturing", JLabel.CENTER);
        labelPane.add(manufactureLabel);
        
        // Prepare tech level label
        JLabel techLabel = new JLabel("Tech Level: " + workshop.getTechLevel(), JLabel.CENTER);
        labelPane.add(techLabel);
        
        // Prepare processCapacity label
        JLabel processCapacityLabel = new JLabel("Process Capacity: " + workshop.getConcurrentProcesses(), JLabel.CENTER);
        labelPane.add(processCapacityLabel);
        
        // Create scroll pane for manufacturing processes
        JScrollPane processScrollPane = new JScrollPane();
        processScrollPane.setPreferredSize(new Dimension(170, 90));
        add(processScrollPane, BorderLayout.CENTER);
        
        // Create process list main panel
        JPanel processListMainPane = new JPanel(new BorderLayout(0, 0));
        processScrollPane.setViewportView(processListMainPane);
        
        // Create process list panel
        processListPane = new JPanel();
        processListPane.setLayout(new BoxLayout(processListPane, BoxLayout.Y_AXIS));
        processListMainPane.add(processListPane, BorderLayout.NORTH);
        
        // Create process panels
        processCache = new ArrayList<ManufactureProcess>(workshop.getProcesses());
        Iterator<ManufactureProcess> i = processCache.iterator();
        while (i.hasNext()) processListPane.add(new ManufacturePanel(i.next()));
	}
	
	
	@Override
	public void update() {
		
		// Update processes if necessary.
		List<ManufactureProcess> processes = workshop.getProcesses();
		if (!processCache.equals(processes)) {
			
			// Add process panels for new processes.
			Iterator<ManufactureProcess> i = processes.iterator();
			while (i.hasNext()) {
				ManufactureProcess process = i.next();
				if (!processCache.contains(process)) 
					processListPane.add(new ManufacturePanel(process));
			}
			
			// Remove process panels for old processes.
			Iterator<ManufactureProcess> j = processCache.iterator();
			while (j.hasNext()) {
				ManufactureProcess process = j.next();
				if (!processes.contains(process)) {
					ManufacturePanel panel = getManufacturePanel(process);
					if (panel != null) processListPane.remove(panel);
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
	 * Gets the panel for a manufacture process.
	 * @param process the manufacture process.
	 * @return manufacture panel or null if none.
	 */
	private ManufacturePanel getManufacturePanel(ManufactureProcess process) {
		ManufacturePanel result = null;
		
		for (int x = 0; x < processListPane.getComponentCount(); x++) {
			Component component = processListPane.getComponent(x);
			if (component instanceof ManufacturePanel) {
				ManufacturePanel panel = (ManufacturePanel) component;
				if (panel.getManufactureProcess().equals(process)) result = panel;
			}
		}
		
		return result;
	}
}