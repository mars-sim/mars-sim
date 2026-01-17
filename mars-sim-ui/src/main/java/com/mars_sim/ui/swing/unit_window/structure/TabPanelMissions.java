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
import java.util.Collection;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.structure.ExplorationManager;
import com.mars_sim.core.structure.OverrideType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.JDoubleLabel;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.unit_window.UnitListPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.SwingHelper;

/**
 * Tab panel displaying a list of settlement missions.<br>
 * Renamed to plural form to be distinguishable from
 * {@link TabPanelMission}, which displays a vehicle's
 * single mission's details.
 */
@SuppressWarnings("serial")
class TabPanelMissions extends EntityTabPanel<Settlement> implements TemporalComponent{

	private static final String MISSION_ICON = "mission";

	// Data members	
	private JLabel siteLabel;
	private JLabel numROIsLabel;
	private JDoubleLabel siteMeanLabel;
	private JDoubleLabel siteSDevLabel;
	
	private JLabel claimedSiteLabel;
	private JDoubleLabel claimedMeanLabel;
	private JDoubleLabel claimedSDevLabel;
	
	private JLabel unclaimedSiteLabel;
	private JDoubleLabel unclaimedMeanLabel;
	private JDoubleLabel unclaimedSDevLabel;
	
	private UnitListPanel<Mission> missionList;
	
	private JCheckBox overrideCheckbox;


	/**
	 * Constructor.
	 * 
	 * @param settlement {@link Settlement} the settlement this tab panel is for.
	 * @param context The UI context.
	 */
	public TabPanelMissions(Settlement settlement, UIContext context) {
		super(
			Msg.getString("Mission.plural"),
			ImageLoader.getIconByName(MISSION_ICON), null,
			context, settlement
		);
	}

	@Override
	protected void buildUI(JPanel content) {

		JPanel topPanel = new JPanel(new BorderLayout(0, 10));
		content.add(topPanel, BorderLayout.NORTH);

		
		AttributePanel sitePanel = new AttributePanel();
		topPanel.add(sitePanel, BorderLayout.NORTH);
		sitePanel.setBorder(SwingHelper.createLabelBorder("Nearby Sites"));
		
		siteLabel = sitePanel.addTextField("Sites Found","", null);
		numROIsLabel = sitePanel.addTextField("Declared ROIs", "", null);
		siteMeanLabel = new JDoubleLabel(StyleManager.DECIMAL_KM);
		sitePanel.addLabelledItem("Mean Distance \u03BC", siteMeanLabel); 
		siteSDevLabel = new JDoubleLabel(StyleManager.DECIMAL_KM);
		sitePanel.addLabelledItem("Standard Deviation \u03C3", siteSDevLabel);
		
		
		AttributePanel twoPanel = new AttributePanel();
		topPanel.add(twoPanel, BorderLayout.CENTER);
		twoPanel.setBorder(SwingHelper.createLabelBorder("Claimed Sites"));
		
		claimedSiteLabel = twoPanel.addTextField("Sites", "", null);
		claimedMeanLabel = new JDoubleLabel(StyleManager.DECIMAL_KM);
		twoPanel.addLabelledItem("Mean Distance \u03BC", claimedMeanLabel);
		claimedSDevLabel = new JDoubleLabel(StyleManager.DECIMAL_KM);
		twoPanel.addLabelledItem("Standard Deviation \u03C3", claimedSDevLabel);
		
		
		AttributePanel unclaimPanel = new AttributePanel();
		topPanel.add(unclaimPanel, BorderLayout.SOUTH);
		unclaimPanel.setBorder(SwingHelper.createLabelBorder("Unclaimed Sites"));
		
		unclaimedSiteLabel = unclaimPanel.addTextField("Sites", "", null);		
		unclaimedMeanLabel = new JDoubleLabel(StyleManager.DECIMAL_KM);
		unclaimPanel.addLabelledItem("Mean Distance \u03BC", unclaimedMeanLabel);
		unclaimedSDevLabel = new JDoubleLabel(StyleManager.DECIMAL_KM);
		unclaimPanel.addLabelledItem("Standard Deviation \u03C3", unclaimedSDevLabel);
		
		
		// Create center panel.
		JPanel centerPanel = new JPanel(new BorderLayout());
		content.add(centerPanel, BorderLayout.CENTER);

		// Create mission list panel.
		JPanel missionListPanel = new JPanel();
		centerPanel.add(missionListPanel, BorderLayout.CENTER);
		missionListPanel.setBorder(SwingHelper.createLabelBorder("Active Missions"));

		// Create mission list.
		var missionMgr = getContext().getSimulation().getMissionManager();
		missionList = new UnitListPanel<Mission>(getContext(), new Dimension(300, 100)) {
			@Override
			protected Collection<Mission> getData() {
				return missionMgr.getMissionsForSettlement(getEntity());
			}
		};
		missionListPanel.add(missionList);

		buildBottomPanel(content, getEntity());

		// Add values to components
		updateMissionStats();
	}
		
	/**
	 * Builds the bottom panel.
	 * 
	 * @param content
	 */
	private void buildBottomPanel(JPanel content, Settlement settlement) {
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

	private void updateMissionStats() {
		var settlement = getEntity();

		var eMgr = settlement.getExplorations();

		var site = eMgr.getStatistics(ExplorationManager.SITE_STAT);
		var claimed = eMgr.getStatistics(ExplorationManager.CLAIMED_STAT);
		var unclaimed = eMgr.getStatistics(ExplorationManager.UNCLAIMED_STAT);
		
		siteLabel.setText(Integer.toString(eMgr.getNearbyMineralLocations().size()));
		numROIsLabel.setText(Integer.toString(eMgr.getDeclaredROIs().size()));
		siteMeanLabel.setValue(site.mean());
		siteSDevLabel.setValue(site.sd());
		
		claimedSiteLabel.setText(eMgr.numDeclaredROIs(true) + "");
		claimedMeanLabel.setValue(claimed.mean());
		claimedSDevLabel.setValue(claimed.sd());
		
		unclaimedSiteLabel.setText(eMgr.numDeclaredROIs(false) + "");		
		unclaimedMeanLabel.setValue(unclaimed.mean());
		unclaimedSDevLabel.setValue(unclaimed.sd());
		
		// Update mission list if necessary.
		missionList.update();

		// Update mission override check box if necessary.
		if (settlement.getProcessOverride(OverrideType.MISSION) != overrideCheckbox.isSelected())
			overrideCheckbox.setSelected(settlement.getProcessOverride(OverrideType.MISSION));
	}

	/**
	 * Sets the settlement mission creation override flag.
	 * 
	 * @param override the mission creation override flag.
	 */
	private void setMissionCreationOverride(boolean override) {
		getEntity().setProcessOverride(OverrideType.MISSION, override);
	}

	/**
	 * Update the mission and sites stats. Ideally should be event driven
	 * @param pulse
	 */
	@Override
	public void clockUpdate(ClockPulse pulse) {
		updateMissionStats();
	}
}
