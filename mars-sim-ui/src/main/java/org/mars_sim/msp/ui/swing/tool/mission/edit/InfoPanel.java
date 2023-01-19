/**
 * Mars Simulation Project
 * InfoPanel.java
 * @date 2021-10-21
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.mission.edit;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.CollectResourcesMission;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionPhase;
import org.mars_sim.msp.core.person.ai.mission.RoverMission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.ui.swing.JComboBoxMW;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;


/**
 * The mission info panel for the edit mission dialog.
 */
@SuppressWarnings("serial")
public class InfoPanel extends JPanel {

	/** action text. */
	final static String ACTION_NONE = "None";
	/** action text. */
	final static String ACTION_CONTINUE = "End EVA and Continue to Next Site";
	/** action text. */
	final static String ACTION_HOME = "Return to Home Settlement and End Mission";
	
	// Data members.
	protected Mission mission;
	protected JInternalFrame frame;
	protected MainDesktopPane desktop;	
	
	protected JTextField descriptionField;
	protected JComboBoxMW<?> actionDropDown;
	protected DefaultListModel<Worker> memberListModel;
	protected JList<Worker> memberList;
	protected JButton addMembersButton;
	protected JButton removeMembersButton;
	
	private static UnitManager unitManager = Simulation.instance().getUnitManager();
	
	/**
	 * Constructor.
	 * @param mission {@link Mission} the mission to edit.
	 * @param parent {@link Dialog} the parent dialog.
	 */
	public InfoPanel(Mission mission, MainDesktopPane desktop, JInternalFrame parent) {
		
		// Data members
		this.mission = mission;
		this.frame = parent;
		this.desktop = desktop;
		
		// Sets the layout.
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		// Sets the border.
		setBorder(new MarsPanelBorder());
		
		// Create the description panel.
		JPanel descriptionPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		descriptionPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(descriptionPane);
		
		// Create the description label.
		JLabel descriptionLabel = new JLabel("Name: ");
		descriptionPane.add(descriptionLabel);
		
		// Create the description text field.
		descriptionField = new JTextField(mission.getName(), 20);
		descriptionPane.add(descriptionField);
		
		// Create the action panel.
		JPanel actionPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		actionPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(actionPane);
		
		// Create the action label.
		JLabel actionLabel = new JLabel("Action: ");
		actionPane.add(actionLabel);
		
		// Create the action drop down box.
		actionDropDown = new JComboBoxMW<>(getActions(mission));
		actionDropDown.setEnabled(actionDropDown.getItemCount() > 1);
		actionPane.add(actionDropDown);
		
		// Create the members panel.
		JPanel membersPane = new JPanel(new BorderLayout());
		membersPane.setBorder(MainDesktopPane.newEmptyBorder());
		membersPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(membersPane);
		
		// Create the members label.
		JLabel membersLabel = new JLabel("Members: ");
		membersLabel.setVerticalAlignment(JLabel.TOP);
		membersPane.add(membersLabel, BorderLayout.WEST);
		
		// Create the member list panel.
		JPanel memberListPane = new JPanel(new BorderLayout(0, 0));
		membersPane.add(memberListPane, BorderLayout.CENTER);
		
        // Create scroll panel for member list.
        JScrollPane memberScrollPane = new JScrollPane();
        memberScrollPane.setPreferredSize(new Dimension(100, 100));
        memberListPane.add(memberScrollPane, BorderLayout.CENTER);
        
        // Create member list model
        memberListModel = new DefaultListModel<Worker>();
        Iterator<Worker> i = mission.getMembers().iterator();
        while (i.hasNext()) memberListModel.addElement(i.next());
        
        // Create member list
        memberList = new JList<Worker>(memberListModel);
        memberList.addListSelectionListener(
        		new ListSelectionListener() {
        			@Override
        			public void valueChanged(ListSelectionEvent e) {
        				// Enable remove members button if there are members in the list.
        				removeMembersButton.setEnabled(memberList.getSelectedValuesList().size() > 0);
        			}
        		}
        	);
        memberScrollPane.setViewportView(memberList);
        
        // Create the member button panel.
        JPanel memberButtonPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
        memberListPane.add(memberButtonPane, BorderLayout.SOUTH);
        
        // Create the add members button.
        addMembersButton = new JButton("Add Members");
        addMembersButton.setEnabled(canAddMembers());
        addMembersButton.addActionListener(
        		new ActionListener() {
        			public void actionPerformed(ActionEvent e) {
        				// Open the add member dialog.
        				addMembers();
        			}
        		});
        memberButtonPane.add(addMembersButton);
        
        // Create the remove members button.
        removeMembersButton = new JButton("Remove Members");
        removeMembersButton.setEnabled(false);
        removeMembersButton.addActionListener(
        		new ActionListener() {
        			public void actionPerformed(ActionEvent e) {
        				// Remove selected members from the list.
        				removeMembers();
        			}
        		});
        memberButtonPane.add(removeMembersButton);
        
	}
	
	/**
	 * Checks if members can be added to the mission.
	 * @return true if members can be added.
	 */
	private boolean canAddMembers() {
		boolean roomInMission = (memberListModel.size() < mission.getMissionCapacity());
		boolean availableMembers = (getAvailableMembers().size() > 0);
		return (roomInMission && availableMembers);
	}
	
	/**
	 * Open the add members dialog.
	 */
	private void addMembers() {
		new AddMembersDialog(frame, desktop, mission, memberListModel, getAvailableMembers());
		addMembersButton.setEnabled(canAddMembers());
	}
	
	/**
	 * Remove selected members from the list.
	 */
	private void removeMembers() {
		int[] selectedIndexes = memberList.getSelectedIndices();
		Object[] selectedMembers = new Object[selectedIndexes.length];
		for (int x = 0; x < selectedIndexes.length; x++) { 
			selectedMembers[x] = memberListModel.elementAt(selectedIndexes[x]);
		}
        for (Object aSelectedMembers : selectedMembers) {
            memberListModel.removeElement(aSelectedMembers);
        }
		addMembersButton.setEnabled(canAddMembers());
	}
	
	/**
	 * Gets a vector of possible actions for the mission.
	 * @param mission {@link Mission} the mission 
	 * @return vector of actions.
	 */
	private Vector<String> getActions(Mission mission) {
		Vector<String> actions = new Vector<String>();
		actions.add(ACTION_NONE);
		
		MissionPhase phase = mission.getPhase();
		
		// Check if continue action can be added.
		if (phase.equals(CollectResourcesMission.COLLECT_RESOURCES)) {
			CollectResourcesMission collectResourcesMission = (CollectResourcesMission) mission;
			if (collectResourcesMission.getNumEVASites() > collectResourcesMission.getNumEVASitesVisited())
				actions.add(ACTION_CONTINUE);
		}
		
		if (mission instanceof VehicleMission) {
			VehicleMission vehicleMission = (VehicleMission) mission;

			// Check if go home action can be added.
			int nextNavpointIndex = vehicleMission.getNextNavpointIndex();
			if ((nextNavpointIndex > -1) && (nextNavpointIndex < (vehicleMission.getNumberOfNavpoints() - 1))) {
				if (mission.getPhase().getStage() == MissionPhase.Stage.ACTIVE)
					actions.add(ACTION_HOME);
			}
		}
		
		return actions;
	}
	
	/**
	 * Gets a collection of people and robots available to be added to the mission.
	 * @return collection of available members.
	 */
	private Collection<Worker> getAvailableMembers() {
		Collection<Worker> result = new ConcurrentLinkedQueue<>();
	
		// Add people and robots in the settlement or rover.
		if (mission instanceof RoverMission) {
			Rover rover = ((RoverMission) mission).getRover();
			MissionPhase phase = mission.getPhase();
			Collection<Worker> membersAtLocation = new ArrayList<>();
			if (rover != null) {
				if (phase.getStage() == MissionPhase.Stage.PREPARATION) {
					// Add available people and robots at the local settlement.
					Settlement settlement = rover.getSettlement();
					if (settlement != null) {
					    membersAtLocation.addAll(settlement.getIndoorPeople());
					    membersAtLocation.addAll(settlement.getRobots());
					}
				}
				else {
					// Add available people and robots in the rover.
					membersAtLocation.addAll(rover.getCrew());
					membersAtLocation.addAll(rover.getRobotCrew());
				}
			}
			
			// Add people.
			Iterator<Worker> i = membersAtLocation.iterator();
			while (i.hasNext()) {
				Worker member = i.next();
				if (!memberListModel.contains(member)) {
				    result.add(member);
				}
			}
		}
		else {
		    
		    // Add people and robots at settlement.
		    Settlement settlement = mission.getAssociatedSettlement();
		    if (settlement != null) {
		        Iterator<Person> i = settlement.getIndoorPeople().iterator();
		        while (i.hasNext()) {
		            Person person = i.next();
		            if (!memberListModel.contains(person)) {
		                result.add(person);
		            }
		        }
		        
		        Iterator<Robot> j = settlement.getRobots().iterator();
		        while (j.hasNext()) {
		            Robot robot = j.next();
		            if (!memberListModel.contains(robot)) {
		                result.add(robot);
		            }
		        }
		    }
		}
		
		// Add people and robots who are outside at this location as well.
		Coordinates missionLocation = mission.getCurrentMissionLocation();
		Iterator<Person> i = unitManager.getPeople().iterator();
		while (i.hasNext()) {
		    Person person = i.next();
		    if (person.isOutside()) {
		        if (person.getCoordinates().equals(missionLocation)) {
		            if (!memberListModel.contains(person)) {
		                result.add(person);
		            }
		        }
		    }
		}
		
		Iterator<Robot> j = unitManager.getRobots().iterator();
        while (j.hasNext()) {
            Robot robot = j.next();
            if (robot.isOutside()) {
                if (robot.getCoordinates().equals(missionLocation)) {
                    if (!memberListModel.contains(robot)) {
                        result.add(robot);
                    }
                }
            }
        }
		
		return result;
	}
	
	public JInternalFrame getParent() {
		return frame;
	}
	
	
}
