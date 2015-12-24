/**
 * Mars Simulation Project
 * MissionWindow.java
 * @version 3.08 2015-03-23
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.mission;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.ui.javafx.MainScene;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MainWindow;
import org.mars_sim.msp.ui.swing.ModalInternalFrame;
import org.mars_sim.msp.ui.swing.tool.mission.create.CreateMissionWizard;
import org.mars_sim.msp.ui.swing.tool.mission.edit.EditMissionDialog;
import org.mars_sim.msp.ui.swing.toolWindow.ToolWindow;

import javafx.application.Platform;

/**
 * Window for the mission tool.
 */
public class MissionWindow
extends ToolWindow {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Tool name. */
	public static final String NAME = "Mission Tool";

	// Private members
	private JList<Mission> missionList;
	private NavpointPanel navpointPane;
	private MainScene ms;
	private CreateMissionWizard createMissionWizard;
	private EditMissionDialog editMissionDialog;

	/**
	 * Constructor.
	 * @param desktop {@link MainDesktopPane} the main desktop panel.
	 */
	public MissionWindow(MainDesktopPane desktop) {

		// Use ToolWindow constructor
		super(NAME, desktop);

		ms = desktop.getMainScene();

		// Set window resizable to false.
		//setResizable(false);

		// Create content panel.
		JPanel mainPane = new JPanel(new BorderLayout());
		mainPane.setBorder(MainDesktopPane.newEmptyBorder());
		setContentPane(mainPane);

		// Create the mission list panel.
		JPanel missionListPane = new JPanel(new BorderLayout());
		missionListPane.setPreferredSize(new Dimension(200, 200));
		mainPane.add(missionListPane, BorderLayout.WEST);

		// Create the mission list.
		missionList = new JList<Mission>(new MissionListModel());
		missionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		missionListPane.add(new JScrollPane(missionList), BorderLayout.CENTER);

		// Create the info tab panel.
		JTabbedPane infoPane = new JTabbedPane();
		mainPane.add(infoPane, BorderLayout.CENTER);

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
			}
		);
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

		if (ms != null )  {
			// 2015-12-16 Track the current pause state
			boolean previous = Simulation.instance().getMasterClock().isPaused();
			if (!previous) {
				ms.pauseSimulation();
		    	//System.out.println("previous is false. Paused sim");
			}
			desktop.getTimeWindow().enablePauseButton(false);

			createMissionWizard = new CreateMissionWizard(desktop);

			boolean now = Simulation.instance().getMasterClock().isPaused();
			if (!previous) {
				if (now) {
					ms.unpauseSimulation();
	   	    		//System.out.println("previous is false. now is true. Unpaused sim");
				}
			} else {
				if (!now) {
					ms.unpauseSimulation();
	   	    		//System.out.println("previous is true. now is false. Unpaused sim");
				}
			}
			desktop.getTimeWindow().enablePauseButton(true);

		} else

			createMissionWizard = new CreateMissionWizard(desktop);


	}

	/**
	 * Open wizard to edit a mission.
	 * @param mission the mission to edit.
	 */
	private void editMission(Mission mission) {

		if (ms != null )  {
			// 2015-12-16 Track the current pause state
			boolean previous = Simulation.instance().getMasterClock().isPaused();
			if (!previous) {
				ms.pauseSimulation();
		    	//System.out.println("previous is false. Paused sim");
			}
			desktop.getTimeWindow().enablePauseButton(false);

			editMissionDialog = new EditMissionDialog(desktop, mission);

			boolean now = Simulation.instance().getMasterClock().isPaused();
			if (!previous) {
				if (now) {
					ms.unpauseSimulation();
	   	    		//System.out.println("previous is false. now is true. Unpaused sim");
				}
			} else {
				if (!now) {
					ms.unpauseSimulation();
	   	    		//System.out.println("previous is true. now is false. Unpaused sim");
				}
			}
			desktop.getTimeWindow().enablePauseButton(true);

		} else

			editMissionDialog = new EditMissionDialog(desktop, mission);


	}

	/**
	 * Ends the mission.
	 * @param mission the mission to end.
	 */
	private void endMission(Mission mission) {
		// logger.info("End mission: " + mission.getName());

		mission.endMission("User ending mission.");
	}

	public CreateMissionWizard getCreateMissionWizard() {
		return createMissionWizard;
	}


	/**
	 * Prepares tool window for deletion.
	 */
	@Override
	public void destroy() {
		missionList.clearSelection();
		((MissionListModel) missionList.getModel()).destroy();
		navpointPane.destroy();
	}
}