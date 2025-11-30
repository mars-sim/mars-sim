/*
 * Mars Simulation Project
 * ModifyMissionDialog.java
 * @date 2024-07-21
 * @author Scott Davis
 */

package com.mars_sim.ui.swing.tool.mission.edit;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionStatus;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.tool.mission.MissionWindow;


/**
 * The modify mission dialog for the mission tool.
 */
public class ModifyMissionDialog extends JDialog {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    
	// Private members
	private Mission mission;
	private EditPanel editPane;
	private MissionWindow missionWindow;
	
	/**
	 * Constructor
	 * @param mission the mission to edit
	 * @param context the UI context
	 * @param missionWindow the mission window
	 */
	public ModifyMissionDialog(Mission mission, UIContext context, MissionWindow missionWindow) {
		// Use JDialog constructor
        super(context.getTopFrame(), "Modify Mission", true); // true for modal
        this.missionWindow = missionWindow;
        
		// Initialize data members.
		this.mission = mission;
		
		// Set the layout.
		setLayout(new BorderLayout(0, 0));
        
		// Create the edit panel.
        editPane = new EditPanel(mission, context);
        add(editPane, BorderLayout.CENTER);
        
        // Create the button panel.
        JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        add(buttonPane, BorderLayout.SOUTH);
        
        // Create the modify button.
        JButton modifyButton = new JButton("Execute");
        modifyButton.addActionListener(
        		e -> {
					// Commit the mission modification and close dialog.
					modifyMission();
					dispose();
				});
        buttonPane.add(modifyButton);
        
        // Create the cancel button.
        JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(
				e -> dispose());
        buttonPane.add(cancelButton);
	    
        var dim = new Dimension(400, 400);
        setSize(dim);
		setPreferredSize(dim);
        
        setLocationRelativeTo(context.getTopFrame());
	}
	
	/**
	 * Commits the modification of the mission.
	 */
	private void modifyMission() {
		// Note: should check the new name against existing names. If found being used, 
		// should not allow it to proceed.
		
		// Set the mission description.
		mission.setName(editPane.descriptionField.getText());
		
		// Change the mission's action.
		setAction((String) editPane.actionDropDown.getSelectedItem());
		
		// Set mission members.
		setWorkers();
	}
	
	/**
	 * Sets the mission action.
	 * 
	 * @param action the action string.
	 */
	private void setAction(String action) {
		if (action.equals(EditPanel.ACTION_CONTINUE)) endEVAPhase();
		else if (action.equals(EditPanel.ACTION_HOME)) returnHome();
	}
	
	/**
	 * Ends the mission EVA phase at the current site.
	 */
	private void endEVAPhase() {
		if (mission != null) {
			mission.abortPhase();
		}
	}
	
	/**
	 * Aborts the mission and have everyone return home and end collection phase if necessary.
	 */
	private void returnHome() {
		if (mission != null) {
			mission.abortMission(MissionStatus.createResourceStatus("Returning Home"));
			// Q: but what's the principle reason for returning home ?
		}
	}
	
	/**
	 * Sets the mission members.
	 */
	private void setWorkers() {
		// Add new members.
		for (int x = 0; x < editPane.memberListModel.size(); x++) {
		    Worker member = editPane.memberListModel.elementAt(x);
			if (!mission.getMembers().contains(member)) {
			    member.setMission(mission);
			}
		}
		
		// Remove old members.
		Iterator<Worker> i = mission.getMembers().iterator();
		while (i.hasNext()) {
			Worker member = i.next();
			if (!editPane.memberListModel.contains(member)) {
			    member.setMission(null);
			}
		}
	}
	
	public EditPanel getInfoPanel() {
		return editPane;
	}
	
	public MissionWindow getMissionWindow() {
		return missionWindow;
	}
}
