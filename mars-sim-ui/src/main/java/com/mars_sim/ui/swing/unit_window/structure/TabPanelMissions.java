/*
 * Mars Simulation Project
 * TabPanelMissions.java
 * @date 2024-07-22
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.structure.ExplorationManager;
import com.mars_sim.core.structure.OverrideType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.tool.mission.MissionWindow;
import com.mars_sim.ui.swing.tool.monitor.MonitorWindow;
import com.mars_sim.ui.swing.tool.monitor.PersonTableModel;
import com.mars_sim.ui.swing.unit_window.TabPanel;
import com.mars_sim.ui.swing.unit_window.vehicle.TabPanelMission;
import com.mars_sim.ui.swing.utils.AttributePanel;

/**
 * Tab panel displaying a list of settlement missions.<br>
 * Renamed to plural form to be distinguishable from
 * {@link TabPanelMission}, which displays a vehicle's
 * single mission's details.
 */
@SuppressWarnings("serial")
public class TabPanelMissions extends TabPanel {
	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(TabPanelMissions.class.getName());

	private static final String FLAG_ICON = "mission";

	// Data members	
	private JLabel siteLabel;
	private JLabel numROIsLabel;
	private JLabel siteMeanLabel;
	private JLabel siteSDevLabel;
	
	private JLabel claimedSiteLabel;
	private JLabel claimedMeanLabel;
	private JLabel claimedSDevLabel;
	
	private JLabel unclaimedSiteLabel;
	private JLabel unclaimedMeanLabel;
	private JLabel unclaimedSDevLabel;
	
	private JList<Mission> missionList;
	
	private DefaultListModel<Mission> missionListModel;
	
	private JButton missionButton;
	private JButton monitorButton;
	
	private JCheckBox overrideCheckbox;
	
	/** The Settlement instance. */
	private Settlement settlement;

	private List<Mission> missionsCache;


	/**
	 * Constructor.
	 * 
	 * @param settlement {@link Settlement} the settlement this tab panel is for.
	 * @param desktop {@link MainDesktopPane} the main desktop panel.
	 */
	public TabPanelMissions(Settlement settlement, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelMissions.title"),
			ImageLoader.getIconByName(FLAG_ICON),
			Msg.getString("TabPanelMissions.title"), //$NON-NLS-1$
			settlement, desktop
		);

		// Initialize data members.
		this.settlement = settlement;
	}

	@Override
	protected void buildUI(JPanel content) {

		JPanel topPanel = new JPanel(new BorderLayout(0, 10));
		content.add(topPanel, BorderLayout.NORTH);

		
		AttributePanel sitePanel = new AttributePanel(4, 1);
		topPanel.add(sitePanel, BorderLayout.NORTH);
		sitePanel.setBorder(BorderFactory.createTitledBorder("Nearby Sites"));
		
		siteLabel = sitePanel.addRow("Sites Found","");
		numROIsLabel = sitePanel.addRow("Declared ROIs", "");
		siteMeanLabel = sitePanel.addRow("Mean Distance \u03BC", ""); 
		siteSDevLabel = sitePanel.addRow("Standard Deviation \u03C3", "");
		
		
		AttributePanel twoPanel = new AttributePanel(3, 1);
		topPanel.add(twoPanel, BorderLayout.CENTER);
		twoPanel.setBorder(BorderFactory.createTitledBorder("Claimed Sites"));
		
		claimedSiteLabel = twoPanel.addRow("Sites", "");
		claimedMeanLabel = twoPanel.addRow("Mean Distance \u03BC", "");
		claimedSDevLabel = twoPanel.addRow("Standard Deviation \u03C3", "");
		
		
		AttributePanel unclaimPanel = new AttributePanel(3, 1);
		topPanel.add(unclaimPanel, BorderLayout.SOUTH);
		unclaimPanel.setBorder(BorderFactory.createTitledBorder("Unclaimed Sites"));
		
		unclaimedSiteLabel = unclaimPanel.addRow("Sites", "");		
		unclaimedMeanLabel = unclaimPanel.addRow("Mean Distance \u03BC", "");
		unclaimedSDevLabel = unclaimPanel.addRow("Standard Deviation \u03C3", "");
		
		
		// Create center panel.
		JPanel centerPanel = new JPanel(new BorderLayout());
		content.add(centerPanel, BorderLayout.CENTER);

		// Create mission list panel.
		JPanel missionListPanel = new JPanel();
		centerPanel.add(missionListPanel, BorderLayout.CENTER);

		buildScrollPanel(missionListPanel);
		buildButtonPanel(centerPanel);
		buildBottomPanel(content);

		// Add values to components
		update();
	}
		
	/**
	 * Builds the scroll panel.
	 * 
	 * @param centerPanel
	 * @param missionListPanel
	 */
	private void buildScrollPanel(JPanel missionListPanel) {

		// Create mission scroll panel.
		JScrollPane missionScrollPanel = new JScrollPane();
		missionScrollPanel.setPreferredSize(new Dimension(190, 220));
		missionListPanel.add(missionScrollPanel);

		// Create mission list model.
		missionListModel = new DefaultListModel<>();
		missionsCache = Collections.emptyList();

		// Create mission list.
		missionList = new JList<>(missionListModel);
		missionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		missionList.addListSelectionListener(e -> {
			boolean missionSelected = !missionList.isSelectionEmpty();
			missionButton.setEnabled(missionSelected);
			monitorButton.setEnabled(missionSelected);
		});
		missionScrollPanel.setViewportView(missionList);
	}
	
	/**
	 * Builds the button panel.
	 * 
	 * @param centerPanel
	 */
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
		missionButton.addActionListener(e -> openMissionTool());
		innerButtonPanel.add(missionButton);

		// Create monitor button.
		monitorButton = new JButton(ImageLoader.getIconByName(MonitorWindow.ICON)); //$NON-NLS-1$
		monitorButton.setMargin(new Insets(1, 1, 1, 1));
		monitorButton.setToolTipText(Msg.getString("TabPanelMissions.tooltip.monitor")); //$NON-NLS-1$
		monitorButton.setEnabled(false);
		monitorButton.addActionListener(e -> openMonitorTool());
		innerButtonPanel.add(monitorButton);
	}
	
	/**
	 * Builds the bottom panel.
	 * 
	 * @param content
	 */
	private void buildBottomPanel(JPanel content) {
		// Create bottom panel.
		JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		content.add(bottomPanel, BorderLayout.SOUTH);

		// Create override check box.
		overrideCheckbox = new JCheckBox(Msg.getString("TabPanelMissions.checkbox.overrideMissionCreation")); //$NON-NLS-1$
		overrideCheckbox.setToolTipText(Msg.getString("TabPanelMissions.tooltip.overrideMissionCreation")); //$NON-NLS-1$
		overrideCheckbox.addActionListener(e -> setMissionCreationOverride(overrideCheckbox.isSelected()));
		overrideCheckbox.setSelected(settlement.getProcessOverride(OverrideType.MISSION));
		bottomPanel.add(overrideCheckbox);
	}

	@Override
	public void update() {
		
		var eMgr = settlement.getExplorations();

		var site = eMgr.getStatistics(ExplorationManager.SITE_STAT);
		var claimed = eMgr.getStatistics(ExplorationManager.CLAIMED_STAT);
		var unclaimed = eMgr.getStatistics(ExplorationManager.UNCLAIMED_STAT);
		
		siteLabel.setText(Integer.toString(eMgr.getNearbyMineralLocations().size()));
		numROIsLabel.setText(Integer.toString(eMgr.getDeclaredROIs().size()));
		siteMeanLabel.setText(StyleManager.DECIMAL_KM.format(site.mean()));
		siteSDevLabel.setText(StyleManager.DECIMAL_KM.format(site.sd()));
		
		claimedSiteLabel.setText(eMgr.numDeclaredROIs(true) + "");
		claimedMeanLabel.setText(StyleManager.DECIMAL_KM.format(claimed.mean()));
		claimedSDevLabel.setText(StyleManager.DECIMAL_KM.format(claimed.sd()));
		
		
		unclaimedSiteLabel.setText(eMgr.numDeclaredROIs(false) + "");		
		unclaimedMeanLabel.setText(StyleManager.DECIMAL_KM.format(unclaimed.mean()));
		unclaimedSDevLabel.setText(StyleManager.DECIMAL_KM.format(unclaimed.sd()));
		
		
		// Get all missions for the settlement.
		List<Mission> missions = getSimulation().getMissionManager().getMissionsForSettlement(settlement);

		// Update mission list if necessary.
		if (!missions.equals(missionsCache)) {
			Mission selectedMission = missionList.getSelectedValue();

			missionsCache = missions;
			missionListModel.clear();
			missionsCache.forEach(m -> missionListModel.addElement(m));

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
		Mission mission = missionList.getSelectedValue();
		if (mission != null) {
			getDesktop().showDetails(mission);
		}
	}

	/**
	 * Opens the monitor tool with a mission tab for the selected mission
	 * in the mission list.
	 */
	private void openMonitorTool() {
		Mission mission = missionList.getSelectedValue();
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
	 * 
	 * @param override the mission creation override flag.
	 */
	private void setMissionCreationOverride(boolean override) {
		settlement.setProcessOverride(OverrideType.MISSION, override);
	}

	/**
	 * Prepares object for garbage collection.
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
