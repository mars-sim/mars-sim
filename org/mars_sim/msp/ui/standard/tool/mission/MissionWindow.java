/**
 * Mars Simulation Project
 * MissionWindow.java
 * @version 2.80 2006-08-11
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.tool.mission;

import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.mars_sim.msp.simulation.person.ai.mission.Mission;
import org.mars_sim.msp.ui.standard.MainDesktopPane;
import org.mars_sim.msp.ui.standard.tool.ToolWindow;

/**
 * Window for the mission tool.
 */
public class MissionWindow extends ToolWindow {

	// Tool name
	public static final String NAME = "Mission Tool";	
	
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
        setResizable(false);
		
		// Create content pane
        JPanel mainPane = new JPanel(new BorderLayout());
        mainPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(mainPane);
        
        JPanel missionListPane = new JPanel(new BorderLayout());
        missionListPane.setPreferredSize(new Dimension(200, 200));
        mainPane.add(missionListPane, BorderLayout.WEST);
        
        missionList = new JList(new MissionListModel());
        missionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        missionListPane.add(new JScrollPane(missionList), BorderLayout.CENTER);
        
        JTabbedPane infoPane = new JTabbedPane();
        mainPane.add(infoPane, BorderLayout.EAST);
        
        MainDetailPanel mainDetailPane = new MainDetailPanel(desktop);
        missionList.addListSelectionListener(mainDetailPane);
        infoPane.add("Info", mainDetailPane);
        
        navpointPane = new NavpointPanel();
        missionList.addListSelectionListener(navpointPane);
        infoPane.add("Navpoints", navpointPane);
        
        JPanel buttonPane = new JPanel(new FlowLayout());
        mainPane.add(buttonPane, BorderLayout.SOUTH);
        
        JButton createButton = new JButton("Create New Mission");
        createButton.addActionListener( 
        		new ActionListener() {
        			public void actionPerformed(ActionEvent e) {
        				createNewMission();
        			}
        		});
        buttonPane.add(createButton);
        
        final JButton editButton = new JButton("Edit Mission");
        editButton.setEnabled(false);
        editButton.addActionListener(
        		new ActionListener() {
        			public void actionPerformed(ActionEvent e) {
        				Mission mission = (Mission) missionList.getSelectedValue();
        				if (mission != null) editMission(mission);
        			}
        		});
        missionList.addListSelectionListener(
            	new ListSelectionListener() {
            		public void valueChanged(ListSelectionEvent e) {
            			editButton.setEnabled(missionList.getSelectedValue() != null);
            		}
            	});
        buttonPane.add(editButton);
        
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
		System.out.println("Editing mission: " + mission.getName());
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