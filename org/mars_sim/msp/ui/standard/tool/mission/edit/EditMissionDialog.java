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

public class EditMissionDialog extends JDialog {

	private Mission mission;
	private InfoPanel infoPane;
	
	public EditMissionDialog(Frame owner, Mission mission) {
		// Use JDialog constructor
		super(owner, "Edit Mission", true);
	
		this.mission = mission;
		
		setLayout(new BorderLayout(0, 0));
		((JComponent) getContentPane()).setBorder(new MarsPanelBorder());
        
        infoPane = new InfoPanel(mission, this);
        add(infoPane, BorderLayout.CENTER);
        
        JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        add(buttonPane, BorderLayout.SOUTH);
        
        JButton modifyButton = new JButton("Modify");
        modifyButton.addActionListener(
        		new ActionListener() {
        			public void actionPerformed(ActionEvent e) {
        				modifyMission();
        				setVisible(false);
        			}
				});
        buttonPane.add(modifyButton);
        
        JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(
				new ActionListener() {
        			public void actionPerformed(ActionEvent e) {
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
	
	private void modifyMission() {
		// Set the mission description.
		mission.setDescription(infoPane.descriptionField.getText());
		
		// Change the mission's action.
		setAction((String) infoPane.actionDropDown.getSelectedItem());
		
		// Set mission members.
		setMissionMembers();
	}
	
	private void setAction(String action) {
		if (action.equals(InfoPanel.ACTION_CONTINUE)) endCollectionPhase();
		else if (action.equals(InfoPanel.ACTION_HOME)) returnHome();
		else if (action.equals(InfoPanel.ACTION_NEAREST)) goToNearestSettlement();
	}
	
	private void endCollectionPhase() {
		if (mission instanceof CollectResourcesMission) 
			((CollectResourcesMission) mission).endCollectingAtSite();
	}
	
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