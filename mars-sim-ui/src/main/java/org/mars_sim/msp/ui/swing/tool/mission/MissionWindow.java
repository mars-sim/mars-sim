/**
 * Mars Simulation Project
 * MissionWindow.java
 * @version 2.80 2007-03-20
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.mission;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.RoverMission;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.ToolWindow;
import org.mars_sim.msp.ui.swing.tool.mission.create.CreateMissionWizard;
import org.mars_sim.msp.ui.swing.tool.mission.edit.EditMissionDialog;

/**
 * Window for the mission tool.
 */
public class MissionWindow extends ToolWindow {

	// Tool name
	public static final String NAME = "Mission Tool";	

	// Private members
	private JList missionList;
	private NavpointPanel navpointPane;
	
	/**
	 * Constructor
	 * @param desktop the main desktop panel.
	 */
	public MissionWindow(MainDesktopPane desktop) {
		
		// Use ToolWindow constructor
		super(NAME, desktop);
		
		// Set window resizable to false.
        //setResizable(false);
		
		// Create content panel.
        JPanel mainPane = new JPanel(new BorderLayout());
        mainPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(mainPane);
        
        // Create the mission list panel.
        JPanel missionListPane = new JPanel(new BorderLayout());
        missionListPane.setPreferredSize(new Dimension(200, 200));
        mainPane.add(missionListPane, BorderLayout.WEST);
        
        // Create the mission list.
        missionList = new JList(new MissionListModel());
        missionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        missionListPane.add(new JScrollPane(missionList), BorderLayout.CENTER);
        
        // Create the info tab panel.
        JTabbedPane infoPane = new JTabbedPane();
        mainPane.add(infoPane, BorderLayout.EAST);
        
        // Create the main detail panel.
        MainDetailPanel mainDetailPane = new MainDetailPanel(desktop);
        missionList.addListSelectionListener(mainDetailPane);
        infoPane.add("Info", mainDetailPane);
        
        // Create the navpoint panel.
        navpointPane = new NavpointPanel();
        missionList.addListSelectionListener(navpointPane);
        infoPane.add("Navpoints", navpointPane);
        
        // Create the button panel.
        JPanel buttonPane = new JPanel(new FlowLayout());
        mainPane.add(buttonPane, BorderLayout.SOUTH);
        
        // Create the create mission button.
        JButton createButton = new JButton("Create New Mission");
        createButton.addActionListener( 
        		new ActionListener() {
        			public void actionPerformed(ActionEvent e) {
        				// Create new mission.
        				createNewMission();
        			}
        		});
        buttonPane.add(createButton);
        
        // Create the edit mission button.
        final JButton editButton = new JButton("Modify Mission");
        editButton.setEnabled(false);
        editButton.addActionListener(
        		new ActionListener() {
        			public void actionPerformed(ActionEvent e) {
        				// Edit the mission.
        				Mission mission = (Mission) missionList.getSelectedValue();
        				if (mission != null) editMission(mission);
        			}
        		});
        missionList.addListSelectionListener(
            	new ListSelectionListener() {
            		public void valueChanged(ListSelectionEvent e) {
            			// Enable button if mission is selected in list.
            			editButton.setEnabled(missionList.getSelectedValue() != null);
            		}
            	});
        buttonPane.add(editButton);
        
        // Create the end mission button.
        final JButton endButton = new JButton("End Mission");
        endButton.setEnabled(false);
        endButton.addActionListener(
        		new ActionListener() {
        			public void actionPerformed(ActionEvent e) {
        				// End the mission.
        				Mission mission = (Mission) missionList.getSelectedValue();
        				if (mission != null) endMission(mission);
        			}
        		});
        missionList.addListSelectionListener(
            	new ListSelectionListener() {
            		public void valueChanged(ListSelectionEvent e) {
            			endButton.setEnabled(missionList.getSelectedValue() != null);
            		}
            	});
        buttonPane.add(endButton);
        
        // Pack window
        pack();
	}
	
	/**
	 * Selects a mission for display.
	 * @param mission the mission to select.
	 */
	public void selectMission(Mission mission) {
		MissionListModel model = (MissionListModel) missionList.getModel();
		if (model.containsMission(mission)) missionList.setSelectedValue(mission, true);
	}
	
	/**
	 * Open wizard to create a new mission.
	 */
	private void createNewMission() {
		// Pause simulation.
		desktop.getMainWindow().pauseSimulation();
		
		// Create new mission wizard.
		new CreateMissionWizard(desktop.getMainWindow());
        
		// Unpause simulation.
		desktop.getMainWindow().unpauseSimulation();
	}
	
	/**
	 * Open wizard to edit a mission.
	 * @param mission the mission to edit.
	 */
	private void editMission(Mission mission) {
		// Pause simulation.
		desktop.getMainWindow().pauseSimulation();
		
		// Create new mission wizard.
		new EditMissionDialog(desktop.getMainWindow(), mission);
        
		// Unpause simulation.
		desktop.getMainWindow().unpauseSimulation();
	}
	
	/**
	 * Ends the mission.
	 * @param mission the mission to end.
	 */
	private void endMission(Mission mission) {
		// logger.info("End mission: " + mission.getName());
		
		// If vehicle is parked at a settlement, have all people exit vehicle.
		exitVehicleAtSettlement(mission);
		
		mission.endMission("User ending mission.");
	}
	
	/**
	 * If vehicle is parked at a settlement, have all people exit vehicle.
	 * @param mission the mission to check.
	 */
	private void exitVehicleAtSettlement(Mission mission) {
		if (mission instanceof RoverMission) {
			RoverMission roverMission = (RoverMission) mission;
			if (roverMission.hasVehicle()) {
				Rover rover = roverMission.getRover();
				Settlement settlement = rover.getSettlement();
				if (settlement != null) {
					Iterator<Person> i = rover.getCrew().iterator();
					while (i.hasNext()) {
	        			Person crewmember = i.next();
	        			try {
	        				rover.getInventory().retrieveUnit(crewmember);
	        				settlement.getInventory().storeUnit(crewmember);
	        				BuildingManager.addToRandomBuilding(crewmember, settlement);
	        			}
	        			catch (Exception e) {
	        				e.printStackTrace(System.err);
	        			}
					}
				}
			}
		}
	}
	
	/**
	 * Prepares tool window for deletion.
	 */
	public void destroy() {
		missionList.clearSelection();
		((MissionListModel) missionList.getModel()).destroy();
		navpointPane.destroy();
	}
}