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

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.mars_sim.core.person.ai.mission.MissionManager;
import com.mars_sim.core.structure.ExplorationManager;
import com.mars_sim.core.structure.OverrideType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.AttributePanel;
import com.mars_sim.ui.swing.components.JDoubleLabel;
import com.mars_sim.ui.swing.components.JIntegerLabel;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.utils.SwingHelper;
import com.mars_sim.ui.swing.utils.model.GenericMissionModel;

/**
 * Tab panel displaying a list of settlement missions.<br>
 * Renamed to plural form to be distinguishable from
 * {@link TabPanelMission}, which displays a vehicle's
 * single mission's details.
 */
@SuppressWarnings("serial")
class TabPanelMissions extends EntityTabPanel<Settlement> implements TemporalComponent{

	private static final String STD_DEVIATION = "Standard Deviation \u03C3";
	private static final String MEAN_DISTANCE = "Mean Distance \u03BC";

	private static final String MISSION_ICON = "mission";

	// Data members	
	private JIntegerLabel siteLabel;
	private JIntegerLabel numROIsLabel;
	private JDoubleLabel siteMeanLabel;
	private JDoubleLabel siteSDevLabel;
	
	private JIntegerLabel claimedSiteLabel;
	private JDoubleLabel claimedMeanLabel;
	private JDoubleLabel claimedSDevLabel;
	
	private JIntegerLabel unclaimedSiteLabel;
	private JDoubleLabel unclaimedMeanLabel;
	private JDoubleLabel unclaimedSDevLabel;
	
	private ActiveMissionModel model;
	
	private JCheckBox overrideCheckbox;


	/**
	 * Constructor.
	 * 
	 * @param settlement {@link Settlement} the settlement this tab panel is for.
	 * @param context The UI context.
	 */
	public TabPanelMissions(Settlement settlement, UIContext context) {
		super(
			Msg.getString("mission.plural"),
			ImageLoader.getIconByName(MISSION_ICON), null,
			context, settlement
		);
	}

	@Override
	protected void buildUI(JPanel content) {

		JPanel topPanel = new JPanel(new BorderLayout());
		content.add(topPanel, BorderLayout.NORTH);

		
		AttributePanel sitePanel = new AttributePanel();
		topPanel.add(sitePanel, BorderLayout.NORTH);
		sitePanel.setBorder(SwingHelper.createLabelBorder("Nearby Sites"));
		
		siteLabel = new JIntegerLabel();
		sitePanel.addLabelledItem("Sites Found", siteLabel);
		numROIsLabel = new JIntegerLabel();
		sitePanel.addLabelledItem("Declared ROIs", numROIsLabel);
		siteMeanLabel = new JDoubleLabel(StyleManager.DECIMAL_KM);
		sitePanel.addLabelledItem(MEAN_DISTANCE, siteMeanLabel); 
		siteSDevLabel = new JDoubleLabel(StyleManager.DECIMAL_KM);
		sitePanel.addLabelledItem(STD_DEVIATION, siteSDevLabel);
		
		
		AttributePanel twoPanel = new AttributePanel();
		topPanel.add(twoPanel, BorderLayout.CENTER);
		twoPanel.setBorder(SwingHelper.createLabelBorder("Claimed Sites"));
		
		claimedSiteLabel = new JIntegerLabel();
		twoPanel.addLabelledItem("Sites", claimedSiteLabel);
		claimedMeanLabel = new JDoubleLabel(StyleManager.DECIMAL_KM);
		twoPanel.addLabelledItem(MEAN_DISTANCE, claimedMeanLabel);
		claimedSDevLabel = new JDoubleLabel(StyleManager.DECIMAL_KM);
		twoPanel.addLabelledItem(STD_DEVIATION, claimedSDevLabel);
		
		AttributePanel unclaimPanel = new AttributePanel();
		topPanel.add(unclaimPanel, BorderLayout.SOUTH);
		unclaimPanel.setBorder(SwingHelper.createLabelBorder("Unclaimed Sites"));
		
		unclaimedSiteLabel = new JIntegerLabel();
		unclaimPanel.addLabelledItem("Sites", unclaimedSiteLabel);
		unclaimedMeanLabel = new JDoubleLabel(StyleManager.DECIMAL_KM);
		unclaimPanel.addLabelledItem(MEAN_DISTANCE, unclaimedMeanLabel);
		unclaimedSDevLabel = new JDoubleLabel(StyleManager.DECIMAL_KM);
		unclaimPanel.addLabelledItem(STD_DEVIATION, unclaimedSDevLabel);
		
		// Create mission list panel.
		var missionMgr = getContext().getSimulation().getMissionManager();
		model = new ActiveMissionModel(missionMgr, getEntity());
		var missionListPanel = SwingHelper.createScrolledTable(model, getContext(), "Active Missions", new Dimension(300, 100));
		content.add(missionListPanel, BorderLayout.CENTER);

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
		
		siteLabel.setValue(eMgr.getNearbyMineralLocations().size());
		numROIsLabel.setValue(eMgr.getDeclaredROIs().size());
		siteMeanLabel.setValue(site.mean());
		siteSDevLabel.setValue(site.sd());
		
		claimedSiteLabel.setValue(eMgr.numDeclaredROIs(true));
		claimedMeanLabel.setValue(claimed.mean());
		claimedSDevLabel.setValue(claimed.sd());
		
		unclaimedSiteLabel.setValue(eMgr.numDeclaredROIs(false));
		unclaimedMeanLabel.setValue(unclaimed.mean());
		unclaimedSDevLabel.setValue(unclaimed.sd());
		
		// Update mission list if necessary.
		model.update();

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

	@Override
	public void destroy() {
		if (model != null) {
			model.release();
		}
		super.destroy();
	}

	/**
	 * The ActiveMissionModel is a model for active missions.
	 */
	private static class ActiveMissionModel extends GenericMissionModel {
		private Settlement settlement;
		private MissionManager mMgr;

		public ActiveMissionModel(MissionManager mMgr, Settlement settlement) {
			super(NAME, PHASE);
			this.settlement = settlement;
			this.mMgr = mMgr;
		}

		public void update() {
			setEntities(mMgr.getMissionsForSettlement(settlement));
		}
	}
}
