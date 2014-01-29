/**
 * Mars Simulation Project
 * EventFilter.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

import org.mars_sim.msp.core.events.HistoricalEventManager;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * The EventFilter class is a internal dialog window for filtering 
 * historical events by category in the EventTab.
 */
public class EventFilter extends JInternalFrame implements ActionListener {

	// Data members
	private EventTableModel model;
	private JCheckBox malfunctionCheck;
	private JCheckBox medicalCheck;
	private JCheckBox missionCheck;
	private JCheckBox taskCheck;
	private JCheckBox transportCheck;

	/**
	 * Constructor
	 * @param model the event table model
	 * @param desktop the main desktop
	 */
	public EventFilter(EventTableModel model, MainDesktopPane desktop) {
		
		// Use JInternalFrame constructor.
		super("Event Category Filter", false, true);
		
		// Initialize data members.
		this.model = model;
		
		// Prepare content pane
		JPanel mainPane = new JPanel();
		mainPane.setLayout(new BorderLayout());
		mainPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(mainPane);
		
		// Create category pane
		JPanel categoryPane = new JPanel(new GridLayout(5, 1));
		categoryPane.setBorder(new MarsPanelBorder());
		mainPane.add(categoryPane, BorderLayout.CENTER);
		
		// Create mechanical events checkbox.
		malfunctionCheck = new JCheckBox(HistoricalEventManager.MALFUNCTION);
		malfunctionCheck.setSelected(model.getDisplayMalfunction());
		malfunctionCheck.addActionListener(this);
		categoryPane.add(malfunctionCheck);
		
		// Create medical events checkbox.
		medicalCheck = new JCheckBox(HistoricalEventManager.MEDICAL);
		medicalCheck.setSelected(model.getDisplayMedical());
		medicalCheck.addActionListener(this);
		categoryPane.add(medicalCheck);
		
		// Create mission events checkbox.
		missionCheck = new JCheckBox(HistoricalEventManager.MISSION);
		missionCheck.setSelected(model.getDisplayMission());
		missionCheck.addActionListener(this);
		categoryPane.add(missionCheck);
		
		// Create task events checkbox.
		taskCheck = new JCheckBox(HistoricalEventManager.TASK);
		taskCheck.setSelected(model.getDisplayTask());
		taskCheck.addActionListener(this);
		categoryPane.add(taskCheck);
		
		// Create transport events checkbox.
		transportCheck = new JCheckBox(HistoricalEventManager.TRANSPORT);
		transportCheck.setSelected(model.getDisplayTransport());
		transportCheck.addActionListener(this);
		categoryPane.add(transportCheck);
		
		pack();
		desktop.add(this);
	}
	
	/**
	 * React to action event.
	 * @see java.awt.event.ActionListener
	 * @param event the action event
	 */
	public void actionPerformed(ActionEvent event) {
		
		JCheckBox check = (JCheckBox) event.getSource();
		
		if (check == malfunctionCheck) 
			model.setDisplayMalfunction(malfunctionCheck.isSelected());
		else if (check == medicalCheck)
			model.setDisplayMedical(medicalCheck.isSelected());
		else if (check == missionCheck)
			model.setDisplayMission(missionCheck.isSelected());
		else if (check == taskCheck)
			model.setDisplayTask(taskCheck.isSelected());
		else if (check == transportCheck)
			model.setDisplayTransport(transportCheck.isSelected());
	}
}