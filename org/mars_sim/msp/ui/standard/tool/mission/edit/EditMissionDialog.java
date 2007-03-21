/**
 * Mars Simulation Project
 * EditMissionDialog.java
 * @version 2.80 2007-03-21
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.tool.mission.edit;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.person.PersonIterator;
import org.mars_sim.msp.simulation.person.ai.mission.CollectResourcesMission;
import org.mars_sim.msp.simulation.person.ai.mission.Mission;
import org.mars_sim.msp.simulation.person.ai.mission.MissionException;
import org.mars_sim.msp.simulation.person.ai.mission.NavPoint;
import org.mars_sim.msp.simulation.person.ai.mission.TravelMission;
import org.mars_sim.msp.simulation.person.ai.mission.VehicleMission;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.ui.standard.MarsPanelBorder;

/**
 * The edit mission dialog for the mission tool.
 */
public class EditMissionDialog extends JDialog {

	// Private members
	private Mission mission;
	private InfoPanel infoPane;
	
	/**
	 * Constructor
	 * @param owner the owner frame.
	 * @param mission the mission to edit.
	 */
	public EditMissionDialog(Frame owner, Mission mission) {
		// Use JDialog constructor
		super(owner, "Edit Mission", true);
	
		// Initialize data members.
		this.mission = mission;
		
		// Set the layout.
		setLayout(new BorderLayout(0, 0));
		
		// Set the border.
		((JComponent) getContentPane()).setBorder(new MarsPanelBorder());
        
		// Create the info panel.
        infoPane = new InfoPanel(mission, this);
        add(infoPane, BorderLayout.CENTER);
        
        // Create the button panel.
        JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        add(buttonPane, BorderLayout.SOUTH);
        
        // Create the modify button.
        JButton modifyButton = new JButton("Modify");
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
		pack();
		setLocationRelativeTo(owner);
		setResizable(false);
		setVisible(true);
	}
	
	/**
	 * Commit the modification of the mission.
	 */
	private void modifyMission() {
		// Set the mission description.
		mission.setDescription(infoPane.descriptionField.getText());
		
		// Change the mission's action.
		setAction((String) infoPane.actionDropDown.getSelectedItem());
		
		// Set mission members.
		setMissionMembers();
	}
	
	/**
	 * Sets the mission action.
	 * @param action the action string.
	 */
	private void setAction(String action) {
		if (action.equals(InfoPanel.ACTION_CONTINUE)) endCollectionPhase();
		else if (action.equals(InfoPanel.ACTION_HOME)) returnHome();
		else if (action.equals(InfoPanel.ACTION_NEAREST)) goToNearestSettlement();
	}
	
	/**
	 * End the mission collection phase at the current site.
	 */
	private void endCollectionPhase() {
		if (mission instanceof CollectResourcesMission) 
			((CollectResourcesMission) mission).endCollectingAtSite();
	}
	
	/**
	 * Have the mission return home and end collection phase if necessary.
	 */
	private void returnHome() {
		if (mission instanceof TravelMission) {
			TravelMission travelMission = (TravelMission) mission;
			try {
				int offset = 2;
				if (travelMission.getPhase().equals(VehicleMission.TRAVELLING)) offset = 1;
				travelMission.setNextNavpointIndex(travelMission.getNumberOfNavpoints() - offset);
				travelMission.updateTravelDestination();
				endCollectionPhase();
			}
			catch (MissionException e) {}
		}
	}
	
	/**
	 * Go to the nearest settlement and end collection phase if necessary.
	 */
	private void goToNearestSettlement() {
		if (mission instanceof VehicleMission) {
			VehicleMission vehicleMission = (VehicleMission) mission;
			try {
				Settlement nearestSettlement = vehicleMission.findClosestSettlement();
				if (nearestSettlement != null) {
					vehicleMission.clearRemainingNavpoints();
		    		vehicleMission.addNavpoint(new NavPoint(nearestSettlement.getCoordinates(), nearestSettlement, 
		    				nearestSettlement.getName()));
		    		vehicleMission.associateAllMembersWithSettlement(nearestSettlement);
		    		vehicleMission.updateTravelDestination();
		    		endCollectionPhase();
				}
			}
			catch (Exception e) {}
		}
	}
	
	/**
	 * Sets the mission members.
	 */
	private void setMissionMembers() {
		// Add new members.
		for (int x = 0; x < infoPane.memberListModel.size(); x++) {
			Person person = (Person) infoPane.memberListModel.elementAt(x);
			if (!mission.hasPerson(person)) person.getMind().setMission(mission);
		}
		
		// Remove old members.
		PersonIterator i = mission.getPeople().iterator();
		while (i.hasNext()) {
			Person person = i.next();
			if (!infoPane.memberListModel.contains(person)) person.getMind().setMission(null);
		}
	}
}