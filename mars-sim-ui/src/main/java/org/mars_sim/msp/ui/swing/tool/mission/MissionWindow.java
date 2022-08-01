/*
 * Mars Simulation Project
 * MissionWindow.java
 * @date 2022-07-31
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.mission;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.mission.create.CreateMissionWizard;
import org.mars_sim.msp.ui.swing.tool.mission.edit.EditMissionDialog;
import org.mars_sim.msp.ui.swing.toolwindow.ToolWindow;

import com.alee.laf.button.WebButton;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.tabbedpane.WebTabbedPane;

/**
 * Window for the mission tool.
 */
@SuppressWarnings("serial")
public class MissionWindow extends ToolWindow {

	/** Tool name. */
	public static final String NAME = "Mission Tool";
	public static final int WIDTH = 640;
	public static final int HEIGHT = 640;

	// Private members
	private WebTabbedPane tabPane;
	private MainDetailPanel mainPanel;

	private JList<Settlement> settlementList;
	private JList<Mission> missionList;

	private SettlementListModel settlementListModel;
	private MissionListModel missionListModel;

	private Settlement settlementCache;
	private Mission missionCache;

	private NavpointPanel navpointPane;

	private CreateMissionWizard createMissionWizard;

	private EditMissionDialog editMissionDialog;

	/**
	 * Constructor.
	 *
	 * @param desktop {@link MainDesktopPane} the main desktop panel.
	 */
	public MissionWindow(MainDesktopPane desktop) {

		// Use ToolWindow constructor
		super(NAME, desktop);

		// Create content panel.
		WebPanel mPane = new WebPanel(new BorderLayout());
		mPane.setBorder(MainDesktopPane.newEmptyBorder());
		setContentPane(mPane);

		// Create the left panel.
		WebPanel leftPane = new WebPanel(new BorderLayout());
		mPane.add(leftPane, BorderLayout.WEST);

		// Create the settlement list panel.
		WebPanel settlementListPane = new WebPanel(new BorderLayout());
		settlementListPane.setPreferredSize(new Dimension(200, 200));
		leftPane.add(settlementListPane, BorderLayout.NORTH);

		// Create the mission list panel.
		WebPanel missionListPane = new WebPanel(new BorderLayout());
		missionListPane.setPreferredSize(new Dimension(200, 400));
		leftPane.add(missionListPane, BorderLayout.CENTER);

		// Create the settlement list.
		settlementListModel = new SettlementListModel();
		settlementList = new JList<>(settlementListModel);
		settlementList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		settlementListPane.add(new WebScrollPane(settlementList), BorderLayout.CENTER);
		settlementList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent me) {
				JList<Settlement> target = (JList<Settlement>)me.getSource();
				int index = target.locationToIndex(me.getPoint());
				if (index >= 0) {
					Settlement settlement = (Settlement)target.getModel().getElementAt(index);
					selectSettlement(settlement);
				}
	         }
		});

		// Create the mission list.
		missionListModel = new MissionListModel(this);
		missionList = new JList<>(missionListModel);
		missionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);//SINGLE_INTERVAL_SELECTION);
		missionListPane.add(new WebScrollPane(missionList), BorderLayout.CENTER);
		missionList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent me) {
				JList<Mission> target = (JList<Mission>)me.getSource();
				int index = target.locationToIndex(me.getPoint());
				if (index >= 0) {
					Mission mission = (Mission)target.getModel().getElementAt(index);
					selectMission(mission);
               }
	         }
		});

		// Create the info tab panel.
		tabPane = new WebTabbedPane();
		mPane.add(tabPane, BorderLayout.CENTER);

		// Create the main detail panel.
		mainPanel = new MainDetailPanel(desktop, this);
		tabPane.add("Main", mainPanel);

		// Create the navpoint panel.
		navpointPane = new NavpointPanel(desktop, this);
		missionList.addListSelectionListener(navpointPane);
		tabPane.add("Navigation", navpointPane);

		// Create the button panel.
		WebPanel buttonPane = new WebPanel(new FlowLayout());
		mPane.add(buttonPane, BorderLayout.SOUTH);

		// Create the create mission button.
		WebButton createButton = new WebButton("Create New Mission");
		createButton.addActionListener(e -> 
				// Create new mission.
				createNewMission()
		);
		buttonPane.add(createButton);

		// Create the edit mission button.
		final WebButton editButton = new WebButton("Modify Mission");
		editButton.setEnabled(false);

		editButton.addActionListener(e -> {
			missionCache = (Mission) missionList.getSelectedValue();
			if (missionCache != null) editMission(missionCache);
		});

		buttonPane.add(editButton);

		// Create the abort mission button.
		final WebButton abortButton = new WebButton("Abort Mission");
		abortButton.setEnabled(false);

		abortButton.addActionListener(e -> {
			// End the mission.
			missionCache = missionList.getSelectedValue();
			if (missionCache != null) missionCache.abortMission();
		});
		
		missionList.addListSelectionListener(
				new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						boolean value = missionList.getSelectedValue() != null;
						abortButton.setEnabled(value);
						// Enable button if mission is selected in list.
						editButton.setEnabled(value);
					}
				});

		buttonPane.add(abortButton);

		setSize(new Dimension(WIDTH, HEIGHT));
//		setMaximizable(true);
		setResizable(false);

		setVisible(true);
		// pack();

		Dimension desktopSize = desktop.getSize();
		Dimension jInternalFrameSize = this.getSize();
		int width = (desktopSize.width - jInternalFrameSize.width) / 2;
		int height = (desktopSize.height - jInternalFrameSize.height) / 2;
		setLocation(width, height);

	}

	
	/**
	 * Selects a mission for display.
	 *
	 * @param missionCache the mission to select.
	 */
	public void selectSettlement(Settlement settlement) {
		if (settlement == null) {
			settlementList.clearSelection();
			return;
		}
	
		if (settlementCache == null || settlementCache != settlement) {
			// Update the settlement cache
			this.settlementCache = settlement;
			// Highlight the selected mission
			settlementList.setSelectedValue(settlement, true);
		}
		
		// Populate the missions in this settlement
		missionListModel.populateMissions();
		
		missionCache = null;
		// Clear the info on main tab and navigation tab to avoid confusion
		mainPanel.clearInfo();
			
		navpointPane.clearInfo();
	}

	/**
	 * Selects a mission for display.
	 *
	 * @param missionCache the mission to select.
	 */
	public void selectSettlement(Settlement settlement, Mission mission) {
		if (settlement == null) {
			settlementList.clearSelection();
			return;
		}
	
		if (settlementCache == null || settlementCache != settlement) {
			// Update the settlement cache
			this.settlementCache = settlement;
			// Highlight the selected mission
			settlementList.setSelectedValue(settlement, true);
		}
		
		// Populate the missions in this settlement
		missionListModel.populateMissions();
		
		// Automatically select the first mission in the mission list
		selectMission(mission);
	}
	
	
	/**
	 * Selects a mission for display.
	 *
	 * @param newMission the mission to select.
	 */
	public void selectMission(Mission newMission) {	
		if (newMission == null) {	
			missionList.clearSelection();
			return;
		}
		
		if (missionCache == null || missionCache != newMission) {
			missionCache = newMission;
			
			missionList.setSelectedValue(newMission, true);
			// Highlight the selected mission in Main tab
			mainPanel.setMission(newMission);
			// Highlight the selected mission in Nav tab
			navpointPane.setMission(newMission);
		}
	}

	/**
	 * Open wizard to create a new mission.
	 */
	private void createNewMission() {
		createMissionWizard = new CreateMissionWizard(desktop, this);
	}

	/**
	 * Open wizard to edit a mission.
	 * @param mission the mission to edit.
	 */
	private void editMission(Mission mission) {
		editMissionDialog = new EditMissionDialog(desktop, mission, this);
	}

	public CreateMissionWizard getCreateMissionWizard() {
		return createMissionWizard;
	}

	public boolean isNavPointsMapTabOpen() {
        return tabPane.getSelectedIndex() == 1;
	}

	public Settlement getSettlement() {
		return settlementCache;
	}

	public Mission getMission() {
		return missionCache;
	}

	public void selectFirstIndex() {
		missionList.setSelectedIndex(0);
	}

	public JList<Mission> getMissionList() {
		return missionList;
	}

	/**
	 * Prepares tool window for deletion.
	 */
	@Override
	public void destroy() {
		missionListModel.destroy();
		settlementListModel.destroy();
		navpointPane.destroy();
	}
}
