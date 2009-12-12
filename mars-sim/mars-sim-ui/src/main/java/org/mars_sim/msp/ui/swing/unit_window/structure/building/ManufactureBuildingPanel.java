/**
 * Mars Simulation Project
 * ManufactureBuildingPanel.java
 * @version 2.85 2008-07-12
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.mars_sim.msp.core.manufacture.ManufactureProcess;
import org.mars_sim.msp.core.manufacture.ManufactureProcessInfo;
import org.mars_sim.msp.core.manufacture.ManufactureUtil;
import org.mars_sim.msp.core.structure.building.function.Manufacture;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.structure.ManufacturePanel;

public class ManufactureBuildingPanel extends BuildingFunctionPanel {

	private static String CLASS_NAME = 
	    "org.mars_sim.msp.ui.standard.unit_window.structure.building.ManufactureBuildingPanel";
	private static Logger logger = Logger.getLogger(CLASS_NAME);
	
	private Manufacture workshop; // The manufacture building.
	private JPanel processListPane; // Panel for displaying process panels.
	private JScrollPane processScrollPane;
	private List<ManufactureProcess> processCache; // List of manufacture processes in building.
	private JComboBox processSelection; // Process selector.
	private Vector<ManufactureProcessInfo> processSelectionCache; // List of available processes.
	private JButton newProcessButton; // Process selection button.
	
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
        processScrollPane = new JScrollPane();
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
        while (i.hasNext()) processListPane.add(new ManufacturePanel(i.next(), false, 23));
        
        // Create interaction panel.
        JPanel interactionPanel = new JPanel(new GridLayout(2, 1, 0, 0));
        add(interactionPanel, BorderLayout.SOUTH);
        
        // Create new manufacture process selection.
        processSelectionCache = getAvailableProcesses();
        processSelection = new JComboBox(processSelectionCache);
        processSelection.setRenderer(new ManufactureSelectionListCellRenderer());
        processSelection.setToolTipText("Select an available manufacturing process");
        interactionPanel.add(processSelection);
        
        // Create new process button.
        newProcessButton = new JButton("Create New Process");
        newProcessButton.setEnabled(processSelection.getItemCount() > 0);
        newProcessButton.setToolTipText("Create a new manufacturing process");
        newProcessButton.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent event) {
        		try {
        			ManufactureProcessInfo selectedProcess = (ManufactureProcessInfo) 
        					processSelection.getSelectedItem();
        			if (selectedProcess != null) {
        				if (ManufactureUtil.canProcessBeStarted(selectedProcess, getWorkshop())) {
        					getWorkshop().addProcess(new ManufactureProcess(selectedProcess, getWorkshop()));
        					update();
        				}
        			}
        		}
        		catch (Exception e) {
        			logger.log(Level.SEVERE, "new process button", e);
        		}
        	}
        });
        interactionPanel.add(newProcessButton);
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
					processListPane.add(new ManufacturePanel(process, false, 23));
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
			
			processScrollPane.validate();
		}
		
		// Update all process panels.
		Iterator<ManufactureProcess> i = processes.iterator();
		while (i.hasNext()) {
			ManufacturePanel panel = getManufacturePanel(i.next());
			if (panel != null) panel.update();
		}
		
		// Update process selection list.
		Vector<ManufactureProcessInfo> newProcesses = getAvailableProcesses();
		if (!newProcesses.equals(processSelectionCache)) {
			processSelectionCache = newProcesses;
			ManufactureProcessInfo currentSelection = (ManufactureProcessInfo) 
					processSelection.getSelectedItem();
			processSelection.removeAllItems();
			Iterator<ManufactureProcessInfo> j = processSelectionCache.iterator();
			while (j.hasNext()) processSelection.addItem(j.next());
			
			if (currentSelection != null) {
				if (processSelectionCache.contains(currentSelection)) 
					processSelection.setSelectedItem(currentSelection);
			}
		}
		
		// Update new process button.
		newProcessButton.setEnabled(processSelection.getItemCount() > 0);
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
	
	/**
	 * Gets all manufacturing processes available at the workshop.
	 * @return vector of processes.
	 */
	private Vector<ManufactureProcessInfo> getAvailableProcesses() {
		Vector<ManufactureProcessInfo> result = new Vector<ManufactureProcessInfo>();
		
		if (workshop.getProcesses().size() < workshop.getConcurrentProcesses()) {
			try {
				Iterator<ManufactureProcessInfo> i = Collections.unmodifiableList(
					ManufactureUtil.getManufactureProcessesForTechLevel(
							workshop.getTechLevel())).iterator();
				while (i.hasNext()) {
					ManufactureProcessInfo process = i.next();
					if (ManufactureUtil.canProcessBeStarted(process, workshop)) 
						result.add(process);
				}
			}
			catch (Exception e) {
				// Note: Exceptions here are due to concurrency errors between
				// this UI thread querying an Inventory object and the simulation
				// thread changing it at the same time.
				// logger.log(Level.SEVERE, "get available processes", e);
			}
		}
		
		return result;
	}
	
	/**
	 * Gets the workshop for this panel.
	 * @return workshop
	 */
	private Manufacture getWorkshop() {
		return workshop;
	}
	
	/**
	 * Inner class for the manufacture selection list cell renderer.
	 */
	private class ManufactureSelectionListCellRenderer extends DefaultListCellRenderer {
		
		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, 
				boolean isSelected, boolean cellHasFocus) {
			Component result = super.getListCellRendererComponent(list, value, index, isSelected, 
					cellHasFocus);
			ManufactureProcessInfo info = (ManufactureProcessInfo) value;
			if (info != null) {
				String processName = info.getName();
				if (processName.length() > 28) processName = processName.substring(0, 28) + "...";
				((JLabel) result).setText(processName);
				((JComponent) result).setToolTipText(ManufacturePanel.getToolTipString(info, null));
			}
			return result;
		}
	}
}