/*
 * Mars Simulation Project
 * EditPanel.java
 * @date 2024-07-21
 * @author Scott Davis
 */

package com.mars_sim.ui.swing.tool.mission.edit;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

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

import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.NavPoint;
import com.mars_sim.core.person.ai.mission.RoverMission;
import com.mars_sim.core.person.ai.mission.VehicleMission;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.project.Stage;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.ui.swing.JComboBoxMW;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.MarsPanelBorder;
import com.mars_sim.ui.swing.StyleManager;

/**
 * The mission edit panel of a dialog.
 */
@SuppressWarnings("serial")
public class EditPanel extends JPanel {

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

	
	/**
	 * Constructor.
	 * 
	 * @param mission {@link Mission} the mission to edit.
	 * @param parent {@link Dialog} the parent dialog.
	 */
	public EditPanel(Mission mission, MainDesktopPane desktop, JInternalFrame parent) {
		
		// Data members
		this.mission = mission;
		this.frame = parent;
		this.desktop = desktop;
		
		// Sets the layout.
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		// Sets the border.
		setBorder(new MarsPanelBorder());
		
		// Create the description panel.
		JPanel descriptionPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
		descriptionPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(descriptionPane);
		
		// Create the description label.
		JLabel descriptionLabel = new JLabel("Name: ");
		descriptionPane.add(descriptionLabel);
		
		// Create the description text field.
		descriptionField = new JTextField(mission.getName(), 20);
		descriptionPane.add(descriptionField);
		
		// Create the action panel.
		JPanel actionPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
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
		membersPane.setBorder(StyleManager.newEmptyBorder());
		membersPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(membersPane);
		
		// Create the members label.
		JLabel membersLabel = new JLabel("Members: ");
		membersLabel.setVerticalAlignment(JLabel.TOP);
		membersPane.add(membersLabel, BorderLayout.WEST);
		
		// Create the member list panel.
		JPanel memberListPane = new JPanel(new BorderLayout(5, 5));
		membersPane.add(memberListPane, BorderLayout.CENTER);
		
        // Create scroll panel for member list.
        JScrollPane memberScrollPane = new JScrollPane();
        memberScrollPane.setPreferredSize(new Dimension(80, 100));
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
        				removeMembersButton.setEnabled(!memberList.getSelectedValuesList().isEmpty());
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
	 * 
	 * @return true if members can be added.
	 */
	private boolean canAddMembers() {
		boolean roomInMission = (memberListModel.size() < mission.getMissionCapacity());
		boolean availableMembers = (getAvailableMembers().size() > 0);
		return (roomInMission && availableMembers);
	}
	
	/**
	 * Opens the add members dialog.
	 */
	private void addMembers() {
		new AddMembersDialog(frame, desktop, mission, memberListModel, getAvailableMembers());
		addMembersButton.setEnabled(canAddMembers());
	}
	
	/**
	 * Removes selected members from the list.
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
	 * 
	 * @param mission {@link Mission} the mission 
	 * @return vector of actions.
	 */
	private Vector<String> getActions(Mission mission) {
		Vector<String> actions = new Vector<String>();
		actions.add(ACTION_NONE);
		
		// MissionPhase phase = mission.getPhase();
		
		// // Check if continue action can be added.
		// if (phase.equals(CollectResourcesMission.COLLECT_RESOURCES)) {
		// 	CollectResourcesMission collectResourcesMission = (CollectResourcesMission) mission;
		// 	if (collectResourcesMission.getNumEVASites() > collectResourcesMission.getNumEVASitesVisited())
		// 		actions.add(ACTION_CONTINUE);
		// }
		
		if (mission instanceof VehicleMission vm) {
			// Check if go home action can be added.
			if (mission.getStage() == Stage.ACTIVE) {
				NavPoint currentNavPoint = vm.getCurrentDestination();
				List<NavPoint> route = vm.getNavpoints();

				// If the current destination is the last one then already on the way home
				if (!route.isEmpty() && !route.get(route.size()-1).equals(currentNavPoint)) {
					actions.add(ACTION_HOME);
				}
			}
		}
		
		return actions;
	}
	
	/**
	 * Gets a collection of people and robots available to be added to the mission.
	 * 
	 * @return collection of available members.
	 */
	private Collection<Worker> getAvailableMembers() {
		Collection<Worker> result = new HashSet<>();
	
		// Add people in the settlement or rover.
		if (mission instanceof RoverMission rm) {
			Rover rover = rm.getRover();
			Stage phase = mission.getStage();
			if (rover != null) {
				if (phase == Stage.PREPARATION) {
					// Add available people and robots at the local settlement.
					Settlement settlement = rover.getSettlement();
					if (settlement != null) {
					    result.addAll(settlement.getIndoorPeople());
					}
				}
				else {
					// Add available people and robots in the rover.
					result.addAll(rover.getCrew());
				}
			}
		}
		else {
		    
		    // Add people and robots at settlement.
		    Settlement settlement = mission.getAssociatedSettlement();
		    if (settlement != null) {
				result.addAll(settlement.getIndoorPeople());
				result.addAll(settlement.getAllAssociatedRobots());
		    }
		}

		return result;
	}
	
	public JInternalFrame getParent() {
		return frame;
	}
	
}
