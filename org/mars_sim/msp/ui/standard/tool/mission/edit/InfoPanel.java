package org.mars_sim.msp.ui.standard.tool.mission.edit;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.person.PersonCollection;
import org.mars_sim.msp.simulation.person.PersonIterator;
import org.mars_sim.msp.simulation.person.ai.mission.CollectResourcesMission;
import org.mars_sim.msp.simulation.person.ai.mission.Mission;
import org.mars_sim.msp.simulation.person.ai.mission.RoverMission;
import org.mars_sim.msp.simulation.person.ai.mission.TravelMission;
import org.mars_sim.msp.simulation.person.ai.mission.VehicleMission;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.vehicle.Rover;
import org.mars_sim.msp.ui.standard.MarsPanelBorder;

public class InfoPanel extends JPanel {

	// Action text
	final static String ACTION_NONE = "None";
	final static String ACTION_CONTINUE = "End EVA and Continue to Next Site";
	final static String ACTION_HOME = "Return to Home Settlement and End Mission";
	final static String ACTION_NEAREST = "Go to Nearest Settlement and End Mission";
	
	// Data members.
	Mission mission;
	Dialog parent;
	JTextField descriptionField;
	JComboBox actionDropDown;
	DefaultListModel memberListModel;
	JList memberList;
	JButton addMembersButton;
	JButton removeMembersButton;
	
	InfoPanel(Mission mission, Dialog parent) {
		// Use JPanel constructor.
		super();
		
		this.mission = mission;
		this.parent = parent;
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(new MarsPanelBorder());
		
		JPanel descriptionPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		descriptionPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(descriptionPane);
		
		JLabel descriptionLabel = new JLabel("Description: ");
		descriptionPane.add(descriptionLabel);
		
		descriptionField = new JTextField(mission.getDescription(), 20);
		descriptionPane.add(descriptionField);
		
		JPanel actionPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		actionPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(actionPane);
		
		JLabel actionLabel = new JLabel("Action: ");
		actionPane.add(actionLabel);
		
		actionDropDown = new JComboBox(getActions(mission));
		actionDropDown.setEnabled(actionDropDown.getItemCount() > 1);
		actionPane.add(actionDropDown);
		
		JPanel membersPane = new JPanel(new BorderLayout());
		membersPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		membersPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(membersPane);
		
		JLabel membersLabel = new JLabel("Members: ");
		membersLabel.setVerticalAlignment(JLabel.TOP);
		membersPane.add(membersLabel, BorderLayout.WEST);
		
		JPanel memberListPane = new JPanel(new BorderLayout(0, 0));
		membersPane.add(memberListPane, BorderLayout.CENTER);
		
        // Create scroll panel for member list.
        JScrollPane memberScrollPane = new JScrollPane();
        memberScrollPane.setPreferredSize(new Dimension(100, 100));
        memberListPane.add(memberScrollPane, BorderLayout.CENTER);
        
        // Create member list model
        memberListModel = new DefaultListModel();
        PersonIterator i = mission.getPeople().iterator();
        while (i.hasNext()) memberListModel.addElement(i.next());
        
        // Create member list
        memberList = new JList(memberListModel);
        memberList.addListSelectionListener(
        		new ListSelectionListener() {
        			public void valueChanged(ListSelectionEvent e) {
        				removeMembersButton.setEnabled(memberList.getSelectedValues().length > 0);
        			}
        		}
        	);
        memberScrollPane.setViewportView(memberList);
        
        JPanel memberButtonPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
        memberListPane.add(memberButtonPane, BorderLayout.SOUTH);
        
        addMembersButton = new JButton("Add Members");
        addMembersButton.setEnabled(canAddMembers());
        addMembersButton.addActionListener(
        		new ActionListener() {
        			public void actionPerformed(ActionEvent e) {
        				addMembers();
        			}
        		});
        memberButtonPane.add(addMembersButton);
        
        removeMembersButton = new JButton("Remove Members");
        removeMembersButton.setEnabled(false);
        removeMembersButton.addActionListener(
        		new ActionListener() {
        			public void actionPerformed(ActionEvent e) {
        				removeMembers();
        			}
        		});
        memberButtonPane.add(removeMembersButton);
	}
	
	private boolean canAddMembers() {
		boolean roomInMission = (memberListModel.size() < mission.getMissionCapacity());
		boolean availablePeople = (getAvailablePeople().size() > 0);
		return (roomInMission && availablePeople);
	}
	
	private void addMembers() {
		// Open add member dialog.
		new AddMembersDialog(parent, mission, memberListModel, getAvailablePeople());
		addMembersButton.setEnabled(canAddMembers());
	}
	
	private void removeMembers() {
		int[] selectedIndexes = memberList.getSelectedIndices();
		Object[] selectedPeople = new Object[selectedIndexes.length];
		for (int x = 0; x < selectedIndexes.length; x++) 
			selectedPeople[x] = memberListModel.elementAt(selectedIndexes[x]);
		for (int x = 0; x < selectedPeople.length; x++) 
			memberListModel.removeElement(selectedPeople[x]);
		addMembersButton.setEnabled(canAddMembers());
	}
	
	private Vector getActions(Mission mission) {
		Vector actions = new Vector();
		actions.add(ACTION_NONE);
		
		String phase = mission.getPhase();
		
		if (phase.equals(CollectResourcesMission.COLLECT_RESOURCES)) {
			CollectResourcesMission collectResourcesMission = (CollectResourcesMission) mission;
			if (collectResourcesMission.getNumCollectionSites() > collectResourcesMission.getNumCollectionSitesVisited())
				actions.add(ACTION_CONTINUE);
		}
		
		if (mission instanceof TravelMission) {
			TravelMission travelMission = (TravelMission) mission;
			int nextNavpointIndex = travelMission.getNextNavpointIndex();
			if ((nextNavpointIndex > -1) && (nextNavpointIndex < (travelMission.getNumberOfNavpoints() - 1))) {
				if (!mission.getPhase().equals(VehicleMission.EMBARKING))
					actions.add(ACTION_HOME);
			}
		}
		
		if (mission instanceof VehicleMission) {
			VehicleMission vehicleMission = (VehicleMission) mission;
			try {
				Settlement closestSettlement = vehicleMission.findClosestSettlement();
				if ((closestSettlement != null) && !closestSettlement.equals(vehicleMission.getAssociatedSettlement())) {
					if (!mission.getPhase().equals(VehicleMission.EMBARKING))
						actions.add(ACTION_NEAREST);
				}
			}
			catch (Exception e) {}
		}
		
		return actions;
	}
	
	private PersonCollection getAvailablePeople() {
		PersonCollection result = new PersonCollection();
	
		if (mission instanceof RoverMission) {
			Rover rover = ((RoverMission) mission).getRover();
			String phase = mission.getPhase();
			PersonCollection peopleAtLocation = null;
			if (phase.equals(RoverMission.EMBARKING) || phase.equals(RoverMission.DISEMBARKING)) {
				// Add available people at the local settlement.
				peopleAtLocation = rover.getSettlement().getInhabitants();
			}
			else {
				// Add available people in the rover.
				peopleAtLocation = rover.getCrew();
			}
			
			// Add people.
			PersonIterator i = peopleAtLocation.iterator();
			while (i.hasNext()) {
				Person person = i.next();
				if (!memberListModel.contains(person)) result.add(person);
			}
		}
		
		// Add people who are outside at this location as well.
		try {
			Coordinates missionLocation = mission.getCurrentMissionLocation();
			PersonIterator i = Simulation.instance().getUnitManager().getPeople().iterator();
			while (i.hasNext()) {
				Person person = i.next();
				if (person.getLocationSituation().equals(Person.OUTSIDE)) {
					if (person.getCoordinates().equals(missionLocation)) {
						if (!memberListModel.contains(person)) result.add(person);
					}
				}
			}
		}
		catch (Exception e) {}
		
		return result;
	}
}