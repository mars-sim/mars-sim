/*
 * Mars Simulation Project
 * TabPanelMissions.java
 * @date 2022-07-09
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.structure.OverrideType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.mission.MissionWindow;
import org.mars_sim.msp.ui.swing.tool.monitor.MonitorWindow;
import org.mars_sim.msp.ui.swing.tool.monitor.PersonTableModel;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;
import org.mars_sim.msp.ui.swing.unit_window.vehicle.TabPanelMission;

/**
 * Tab panel displaying a list of settlement missions.<br>
 * Renamed to plural form to be distinguishable from
 * {@link TabPanelMission}, which displays a vehicle's
 * single mission's details.
 */
@SuppressWarnings("serial")
public class TabPanelMissions
extends TabPanel {
	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(TabPanelMissions.class.getName());

	private static final String FLAG_ICON = "mission";
	
	// Data members
	/** The Settlement instance. */
	private Settlement settlement;

	private List<Mission> missionsCache;
	private DefaultListModel<Mission> missionListModel;
	private JList<Mission> missionList;
	private JButton missionButton;
	private JButton monitorButton;
	private JCheckBox overrideCheckbox;


	/**
	 * Constructor.
	 * @param settlement {@link Settlement} the settlement this tab panel is for.
	 * @param desktop {@link MainDesktopPane} the main desktop panel.
	 */
	public TabPanelMissions(Settlement settlement, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			null,
			ImageLoader.getIconByName(FLAG_ICON),
			Msg.getString("TabPanelMissions.title"), //$NON-NLS-1$
			settlement, desktop
		);

		// Initialize data members.
		this.settlement = settlement;
	}

	@Override
	protected void buildUI(JPanel content) {

		// Create center panel.
		JPanel centerPanel = new JPanel(new BorderLayout());
		content.add(centerPanel, BorderLayout.CENTER);

		// Create mission list panel.
		JPanel missionListPanel = new JPanel();
		centerPanel.add(missionListPanel, BorderLayout.CENTER);

		buildScrollPanel(centerPanel, missionListPanel);
		buildButtonPanel(centerPanel);
		buildBottomPanel(content);
	}
		
	public void buildScrollPanel(JPanel centerPanel, JPanel missionListPanel) {
		MissionManager missionManager = getSimulation().getMissionManager();

		// Create mission scroll panel.
		JScrollPane missionScrollPanel = new JScrollPane();
		missionScrollPanel.setPreferredSize(new Dimension(190, 220));
		missionListPanel.add(missionScrollPanel);

		// Create mission list model.
		missionListModel = new DefaultListModel<Mission>();
		//MissionManager manager = Simulation.instance().getMissionManager();
		missionsCache = missionManager.getMissionsForSettlement(settlement);
		Iterator<Mission> i = missionsCache.iterator();
		while (i.hasNext()) missionListModel.addElement(i.next());

		// Create mission list.
		missionList = new JList<Mission>(missionListModel);
		missionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		missionList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				boolean missionSelected = !missionList.isSelectionEmpty();
				missionButton.setEnabled(missionSelected);
				monitorButton.setEnabled(missionSelected);
			}
		});
		missionScrollPanel.setViewportView(missionList);
	}
	
	private void buildButtonPanel(JPanel centerPanel) {
		// Create button panel.
		JPanel buttonPanel = new JPanel(new BorderLayout());
		buttonPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
		centerPanel.add(buttonPanel, BorderLayout.EAST);

		// Create inner button panel.
		JPanel innerButtonPanel = new JPanel(new GridLayout(2, 1, 0, 2));
		buttonPanel.add(innerButtonPanel, BorderLayout.NORTH);

		// Create mission button.
		missionButton = new JButton(ImageLoader.getIconByName(MissionWindow.ICON)); //$NON-NLS-1$
		missionButton.setMargin(new Insets(1, 1, 1, 1));
		missionButton.setToolTipText(Msg.getString("TabPanelMissions.tooltip.mission")); //$NON-NLS-1$
		missionButton.setEnabled(false);
		missionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				openMissionTool();
			}
		});
		innerButtonPanel.add(missionButton);

		// Create monitor button.
		monitorButton = new JButton(ImageLoader.getIconByName(MonitorWindow.ICON)); //$NON-NLS-1$
		monitorButton.setMargin(new Insets(1, 1, 1, 1));
		monitorButton.setToolTipText(Msg.getString("TabPanelMissions.tooltip.monitor")); //$NON-NLS-1$
		monitorButton.setEnabled(false);
		monitorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				openMonitorTool();
			}
		});
		innerButtonPanel.add(monitorButton);
	}
	
	private void buildBottomPanel(JPanel content) {
		// Create bottom panel.
		JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		content.add(bottomPanel, BorderLayout.SOUTH);

		// Create override check box.
		overrideCheckbox = new JCheckBox(Msg.getString("TabPanelMissions.checkbox.overrideMissionCreation")); //$NON-NLS-1$
		overrideCheckbox.setToolTipText(Msg.getString("TabPanelMissions.tooltip.overrideMissionCreation")); //$NON-NLS-1$
		overrideCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setMissionCreationOverride(overrideCheckbox.isSelected());
			}
		});
		overrideCheckbox.setSelected(settlement.getProcessOverride(OverrideType.MISSION));
		bottomPanel.add(overrideCheckbox);
	}

	@Override
	public void update() {
		// Get all missions for the settlement.
		List<Mission> missions = getSimulation().getMissionManager().getMissionsForSettlement(settlement);

		// Update mission list if necessary.
		if (!missions.equals(missionsCache)) {
			Mission selectedMission = (Mission) missionList.getSelectedValue();

			missionsCache = missions;
			missionListModel.clear();
			Iterator<Mission> i = missionsCache.iterator();
			while (i.hasNext()) missionListModel.addElement(i.next());

			if ((selectedMission != null) && missionListModel.contains(selectedMission))
				missionList.setSelectedValue(selectedMission, true);
		}

		// Update mission override check box if necessary.
		if (settlement.getProcessOverride(OverrideType.MISSION) != overrideCheckbox.isSelected())
			overrideCheckbox.setSelected(settlement.getProcessOverride(OverrideType.MISSION));
	}

	/**
	 * Opens the mission tool to the selected mission in the mission list.
	 */
	private void openMissionTool() {
		Mission mission = (Mission) missionList.getSelectedValue();
		if (mission != null) {
			((MissionWindow)getDesktop().openToolWindow(MissionWindow.NAME)).openMission(mission);
		}
	}

	/**
	 * Opens the monitor tool with a mission tab for the selected mission
	 * in the mission list.
	 */
	private void openMonitorTool() {
		Mission mission = (Mission) missionList.getSelectedValue();
		if (mission != null) {
			try {
				getDesktop().addModel(new PersonTableModel(mission));
			} catch (Exception e) {
				logger.severe("PersonTableModel cannot be added.");
			}
		}
	}

	/**
	 * Sets the settlement mission creation override flag.
	 * @param override the mission creation override flag.
	 */
	private void setMissionCreationOverride(boolean override) {
		settlement.setProcessOverride(OverrideType.MISSION, override);
	}

	/**
	 * Prepare object for garbage collection.
	 */
	@Override
	public void destroy() {
		super.destroy();
		
		settlement = null;
		missionsCache = null;
		missionListModel = null;
		missionList = null;
		missionButton = null;
		monitorButton = null;
		overrideCheckbox = null;
	}
}
