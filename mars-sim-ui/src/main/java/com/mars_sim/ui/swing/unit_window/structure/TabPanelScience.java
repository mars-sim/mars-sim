/*
 * Mars Simulation Project
 * TabPanelScience.java
 * @date 2022-07-09
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import com.mars_sim.core.Entity;
import com.mars_sim.core.EntityManagerListener;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.AttributePanel;
import com.mars_sim.ui.swing.components.JDoubleLabel;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.utils.AchievementTableModel;
import com.mars_sim.ui.swing.utils.SwingHelper;
import com.mars_sim.ui.swing.utils.model.BaseScienceStudyModel;

/**
 * A tab panel displaying a settlement's scientific studies and achievements.
 */
@SuppressWarnings("serial")
class TabPanelScience extends EntityTabPanel<Settlement>
				implements TemporalComponent, EntityManagerListener {

	private static final String SCIENCE_ICON = "science";

	private JDoubleLabel totalAchievementLabel;

	private BaseScienceStudyModel studyModel;
	private AchievementTableModel achievementModel;
	
	/**
	 * Constructor.
	 * 
	 * @param settlement the settlement.
	 * @param context the UI context.
	 */
	public TabPanelScience(Settlement settlement, UIContext context) {
		// Use the TabPanel constructor
		super(
			Msg.getString("scientificstudy.science"), //$NON-NLS-1$
			ImageLoader.getIconByName(SCIENCE_ICON), null,
			context, settlement
		);
	}
	
	@Override
	protected void buildUI(JPanel content){
		var studyManager = getContext().getSimulation().getScientificStudyManager();

		// Create the main panel.
		JPanel mainPane = new JPanel(new GridLayout(2, 1, 0, 0));
		content.add(mainPane);

		// Create the studies panel.
		JPanel studiesPane = new JPanel(new BorderLayout(5, 5));
		mainPane.add(studiesPane);
		
		var settlement = getEntity();
		// Create the study table.
		studyModel = new BaseScienceStudyModel(BaseScienceStudyModel.NAME, BaseScienceStudyModel.SCIENCE,
									BaseScienceStudyModel.LEVEL, BaseScienceStudyModel.PHASE, BaseScienceStudyModel.LEAD);
		studyModel.setEntities(studyManager.getAllStudies(settlement));

		var studyTable = SwingHelper.createScrolledTable(studyModel, getContext(),
							Msg.getString("scientificstudy.plural"), new Dimension(225, -1));
		studiesPane.add(studyTable, BorderLayout.CENTER);

		// Create the achievement panel.
		JPanel achievementPane = new JPanel(new BorderLayout());
		mainPane.add(achievementPane);

		var achievementSummary = new AttributePanel();
		achievementSummary.setBorder(BorderFactory.createEmptyBorder(2,0,2,0));
		totalAchievementLabel = new JDoubleLabel(StyleManager.DECIMAL_PLACES1, getEntity().getTotalScientificAchievement(), 0.1D);
		achievementSummary.addLabelledItem(Msg.getString(
								"TabPanelScience.totalAchievementCredit"), totalAchievementLabel);
		achievementPane.add(achievementSummary, BorderLayout.NORTH);

		// Create the achievement scroll panel and table.
		achievementModel = new AchievementTableModel(settlement::getScientificAchievement);
		achievementModel.update();
		var achievementTable = SwingHelper.createScrolledTable(achievementModel, getContext(),
							Msg.getString("TabPanelScience.scientificAchievement"), new Dimension(225, -1));
		achievementPane.add(achievementTable, BorderLayout.CENTER);

		// Get notified of new studies so we can update the table.
		studyManager.addListener(this);
	}

	/**
	 * Remove the listener for new ScientificStudies when the panel is destroyed.
	 */
	@Override
	public void destroy() {
		getContext().getSimulation().getScientificStudyManager().removeListener(this);

		if (studyModel != null) {
			studyModel.release();
		}
		super.destroy();
	}

	@Override
	public void clockUpdate(ClockPulse pulse) {

		// Update achievement table model.
		achievementModel.update();

		// Update total achievement label.
		totalAchievementLabel.setValue(getEntity().getTotalScientificAchievement());
	}

	@Override
	public void entityAdded(Entity newEntity) {
		if (newEntity instanceof ScientificStudy ss && ss.getPrimarySettlement().equals(getEntity())) {
			studyModel.addEntity(ss);
		}
	}

	@Override
	public void entityRemoved(Entity removedEntity) {
		if (removedEntity instanceof ScientificStudy ss && ss.getPrimarySettlement().equals(getEntity())) {
			studyModel.removeEntity(ss);
		}
	}
}
