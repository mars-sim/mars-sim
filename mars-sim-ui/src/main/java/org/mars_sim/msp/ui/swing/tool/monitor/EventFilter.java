/**
 * Mars Simulation Project
 * EventFilter.java
 * @version 3.1.0 2017-02-03
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.events.HistoricalEventCategory;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;

import com.alee.laf.checkbox.WebCheckBox;
import com.alee.laf.desktoppane.WebInternalFrame;
import com.alee.laf.panel.WebPanel;

/**
 * The EventFilter class is a internal dialog window for filtering 
 * historical events by category in the EventTab.
 */
@SuppressWarnings("serial")
public class EventFilter
extends WebInternalFrame
implements ActionListener {

	// Data members
	private EventTableModel model;
	private WebCheckBox malfunctionCheck;
	private WebCheckBox medicalCheck;
	private WebCheckBox missionCheck;
	private WebCheckBox taskCheck;
	private WebCheckBox transportCheck;
	private WebCheckBox hazardCheck;

	/**
	 * Constructor.
	 * @param model the event table model
	 * @param desktop the main desktop
	 */
	public EventFilter(EventTableModel model, MainDesktopPane desktop) {

		// Use JInternalFrame constructor.
		super(Msg.getString("EventFilter.title"), false, true); //$NON-NLS-1$

		// Initialize data members.
		this.model = model;

		// Prepare content pane
		WebPanel mainPane = new WebPanel();
		mainPane.setLayout(new BorderLayout());
		mainPane.setBorder(MainDesktopPane.newEmptyBorder());
		setContentPane(mainPane);

		// Create category pane
		WebPanel categoryPane = new WebPanel(new GridLayout(5, 1));
		categoryPane.setBorder(new MarsPanelBorder());
		mainPane.add(categoryPane, BorderLayout.CENTER);

		// Create transport events checkbox.
		hazardCheck = new WebCheckBox(HistoricalEventCategory.HAZARD.getName());
		hazardCheck.setSelected(model.getDisplayHazard());
		hazardCheck.addActionListener(this);
		categoryPane.add(hazardCheck);
		
		// Create mechanical events checkbox.
		malfunctionCheck = new WebCheckBox(HistoricalEventCategory.MALFUNCTION.getName());
		malfunctionCheck.setSelected(model.getDisplayMalfunction());
		malfunctionCheck.addActionListener(this);
		categoryPane.add(malfunctionCheck);

		// Create medical events checkbox.
		medicalCheck = new WebCheckBox(HistoricalEventCategory.MEDICAL.getName());
		medicalCheck.setSelected(model.getDisplayMedical());
		medicalCheck.addActionListener(this);
		categoryPane.add(medicalCheck);

		// Create mission events checkbox.
		missionCheck = new WebCheckBox(HistoricalEventCategory.MISSION.getName());
		missionCheck.setSelected(model.getDisplayMission());
		missionCheck.addActionListener(this);
		categoryPane.add(missionCheck);

		// Create task events checkbox.
		taskCheck = new WebCheckBox(HistoricalEventCategory.TASK.getName());
		taskCheck.setSelected(model.getDisplayTask());
		taskCheck.addActionListener(this);
		categoryPane.add(taskCheck);

		// Create transport events checkbox.
		transportCheck = new WebCheckBox(HistoricalEventCategory.TRANSPORT.getName());
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
	@Override
	public void actionPerformed(ActionEvent event) {

		WebCheckBox check = (WebCheckBox) event.getSource();

		if (check == taskCheck)
			model.setDisplayTask(taskCheck.isSelected());
		else if (check == malfunctionCheck) 
			model.setDisplayMalfunction(malfunctionCheck.isSelected());
		else if (check == medicalCheck)
			model.setDisplayMedical(medicalCheck.isSelected());
		else if (check == missionCheck)
			model.setDisplayMission(missionCheck.isSelected());
		else if (check == transportCheck)
			model.setDisplayTransport(transportCheck.isSelected());
		else if (check == hazardCheck)
			model.setDisplayHazard(hazardCheck.isSelected());
	}
	
	public void destroy() {
		model = null;
		hazardCheck = null;
		malfunctionCheck = null;
		medicalCheck = null;
		missionCheck = null;
		taskCheck = null;
		transportCheck = null;
	}
}