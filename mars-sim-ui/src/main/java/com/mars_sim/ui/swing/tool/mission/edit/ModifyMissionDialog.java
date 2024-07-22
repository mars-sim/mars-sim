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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.MarsPanelBorder;
import com.mars_sim.ui.swing.ModalInternalFrame;
import com.mars_sim.ui.swing.tool.mission.MissionWindow;


/**
 * The modify mission dialog for the mission tool.
 */
public class ModifyMissionDialog extends ModalInternalFrame {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    
	// Private members
	private Mission mission;
	private EditPanel editPane;
	protected MainDesktopPane desktop;
	private MissionWindow missionWindow;
	
	/**
	 * Constructor
	 * @param owner the owner frame.
	 * @param mission the mission to edit.
	 */
	public ModifyMissionDialog(MainDesktopPane desktop, Mission mission, MissionWindow missionWindow) {
		// Use ModalInternalFrame constructor
        super("Modify Mission");
        this.missionWindow = missionWindow;
        
		// Initialize data members.
		this.mission = mission;
		this.desktop = desktop;
		
		// Set the layout.
		setLayout(new BorderLayout(0, 0));
		
		// Set the border.
		((JComponent) getContentPane()).setBorder(new MarsPanelBorder());
        
		// Create the edit panel.
        editPane = new EditPanel(mission, desktop, this);
        add(editPane, BorderLayout.CENTER);
        
        // Create the button panel.
        JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        add(buttonPane, BorderLayout.SOUTH);
        
        // Create the modify button.
        JButton modifyButton = new JButton("Execute");
        modifyButton.addActionListener(
        		new ActionListener() {
        			public void actionPerformed(ActionEvent e) {
        				// Commit the mission modification and close dialog.
        				modifyMission();
        				dispose();
        			}
				});
        buttonPane.add(modifyButton);
        
        // Create the cancel button.
        JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(
				new ActionListener() {
        			public void actionPerformed(ActionEvent e) {
        				// Close the dialog.
        				dispose();
        			}
				});
        buttonPane.add(cancelButton);
		
		// Finish and display dialog.
		//pack();
		//setLocationRelativeTo(owner);
        
		setResizable(false);

        desktop.add(this);
	    
        setSize(new Dimension(400, 400));
		Dimension desktopSize = desktop.getParent().getSize();
	    Dimension jInternalFrameSize = this.getSize();
	    int width = (desktopSize.width - jInternalFrameSize.width) / 2;
	    int height = (desktopSize.height - jInternalFrameSize.height) / 2;
	    setLocation(width, height);
	    
	    //setModal(true);
	    setVisible(true);
	}
	
	/**
	 * Commits the modification of the mission.
	 */
	private void modifyMission() {
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
			mission.abortMission("Return home");
			// Q: but what's the principle reason for returning home ?
		}
	}
	
	/**
	 * Sets the mission members.
	 */
	private void setWorkers() {
		// Add new members.
		for (int x = 0; x < editPane.memberListModel.size(); x++) {
		    Worker member = (Worker) editPane.memberListModel.elementAt(x);
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
