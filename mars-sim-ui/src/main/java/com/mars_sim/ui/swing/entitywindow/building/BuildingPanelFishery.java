/*
 * Mars Simulation Project
 * BuildingPanelFishery.java
 * @date 2023-12-07
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.entitywindow.building;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.mars_sim.ui.swing.components.JDoubleLabel;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.farming.Fishery;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;

/**
 * The BuildingPanelFishery class is a building function panel for
 * the fish farm of a settlement building.
 */
@SuppressWarnings("serial")
class BuildingPanelFishery extends EntityTabPanel<Building>
  				implements TemporalComponent {

	private static final String FISH_ICON = "fish";
	
	// Caches
	private int numFish;
	private int numIdealFish; 
	private int maxFish;
	private int numWeed;
	
	private JDoubleLabel ageLabel;
	private JDoubleLabel fishHarvestedLabel;
	private JLabel numFishLabel;
	private JLabel numIdealFishLabel;
	private JLabel maxFishLabel;
	private JDoubleLabel fishMassLabel;
	
	private JLabel numWeedLabel;
	private JDoubleLabel weedMassLabel;
	private JDoubleLabel weedDemandLabel;

	private JDoubleLabel waterMassLabel;
	private JDoubleLabel powerReqLabel;
	private JDoubleLabel workTimeLabel;
	
	private Fishery tank;

	
	/**
	 * Constructor.
	 * 
	 * @param tank the fishery
	 * @param context the UI context
	 */
	public BuildingPanelFishery(Fishery tank, UIContext context) {
		super(
			Msg.getString("BuildingPanelFishery.title"), 
			ImageLoader.getIconByName(FISH_ICON), null,
			context, tank.getBuilding()
		);
		
		this.tank = tank;
	}
	
	/**
	 * Builds the UI.
	 */
	@Override
	protected void buildUI(JPanel center) {
		AttributePanel labelPanel = new AttributePanel(13);
		center.add(labelPanel, BorderLayout.NORTH);
		
		labelPanel.addTextField(Msg.getString("BuildingPanelFishery.tankSize"), 
				StyleManager.DECIMAL_LITER2.format(tank.getTankSize()), null);
		
		waterMassLabel = new JDoubleLabel(StyleManager.DECIMAL_KG2, tank.getWaterMass());
		labelPanel.addLabelledItem(Msg.getString("BuildingPanelAlgae.waterMass"), waterMassLabel);
		
		numFish = tank.getNumFish();
		numFishLabel = labelPanel.addTextField(Msg.getString("BuildingPanelFishery.numFish"),
									Integer.toString(numFish), null);
				
		fishMassLabel = new JDoubleLabel(StyleManager.DECIMAL_KG, tank.getTotalFishMass());
		labelPanel.addLabelledItem(Msg.getString("BuildingPanelFishery.fishMass"), fishMassLabel);
				
		numIdealFish = tank.getIdealFish();
		numIdealFishLabel = labelPanel.addTextField(Msg.getString("BuildingPanelFishery.numIdealFish"),
									Integer.toString(numIdealFish), null);
		
		maxFish = tank.getMaxFish();
		maxFishLabel = labelPanel.addTextField(Msg.getString("BuildingPanelFishery.maxFish"),
									Integer.toString(maxFish), null);
		
		ageLabel = new JDoubleLabel(StyleManager.DECIMAL2_SOLS, tank.getAverageAge()/1000);
		labelPanel.addLabelledItem(Msg.getString("BuildingPanelFishery.age"), ageLabel);
		
		fishHarvestedLabel = new JDoubleLabel(StyleManager.DECIMAL1_KG_SOL, tank.computeDailyAverage(ResourceUtil.FISH_MEAT_ID));
		labelPanel.addLabelledItem(Msg.getString("BuildingPanelFishery.harvestedFish"), fishHarvestedLabel,
									Msg.getString("BuildingPanelFishery.harvestedFish.tooltip"));
		numWeed = tank.getNumWeed();
		numWeedLabel = labelPanel.addTextField(Msg.getString("BuildingPanelFishery.numWeed"),
									Integer.toString(numWeed), null);
		
		weedMassLabel = new JDoubleLabel(StyleManager.DECIMAL_KG, tank.getTotalWeedMass());
		labelPanel.addLabelledItem(Msg.getString("BuildingPanelFishery.weedMass"), weedMassLabel);
		
		weedDemandLabel = new JDoubleLabel(StyleManager.DECIMAL_PLACES2, tank.getWeedDemand());
		labelPanel.addLabelledItem(Msg.getString("BuildingPanelFishery.weedDemand"), weedDemandLabel);
		
		powerReqLabel = new JDoubleLabel(StyleManager.DECIMAL_KW, tank.getCombinedPowerLoad());
		labelPanel.addLabelledItem(Msg.getString("BuildingPanelFishery.powerReq"), powerReqLabel);
		
		// Update the cumulative work time
		workTimeLabel = new JDoubleLabel(StyleManager.DECIMAL3_SOLS, tank.getCumulativeWorkTime()/1000.0);
		labelPanel.addLabelledItem(Msg.getString("BuildingPanelAlgae.workTime.title"), workTimeLabel,
				Msg.getString("BuildingPanelAlgae.workTime.tooltip"));
	}

	/**
	 * Updates this panel on clock pulse.
	 * Ideally could be converted to event driven update later.
	 */
	@Override
	public void clockUpdate(ClockPulse pulse) {
		int newNumFish = tank.getNumFish();
		if (numFish != newNumFish) {
			numFish = newNumFish;
			numFishLabel.setText(Integer.toString(newNumFish));
		}
		
		int newIdealFish = tank.getIdealFish();
		if (numIdealFish != newIdealFish) {
			numIdealFish = newIdealFish;
			numIdealFishLabel.setText(Integer.toString(newIdealFish));
		}
		
		int newMaxFish = tank.getMaxFish();
		if (maxFish != newMaxFish) {
			maxFish = newMaxFish;
			maxFishLabel.setText(Integer.toString(newMaxFish));
		}

		int newNumWeed = tank.getNumWeed();
		if (numWeed != newNumWeed) {
			numWeed = newNumWeed;
			numWeedLabel.setText(Integer.toString(newNumWeed));
		}
		
		fishMassLabel.setValue(tank.getTotalFishMass());
		
		fishHarvestedLabel.setValue(tank.computeDailyAverage(ResourceUtil.FISH_MEAT_ID));
		
		ageLabel.setValue(tank.getAverageAge()/1000);

		weedMassLabel.setValue(tank.getTotalWeedMass());
		
		weedDemandLabel.setValue(tank.getWeedDemand());
		
		waterMassLabel.setValue(tank.getWaterMass());
		
		powerReqLabel.setValue(tank.getCombinedPowerLoad());
		
		// Update the cumulative work time
		workTimeLabel.setValue(tank.getCumulativeWorkTime()/1000.0);
	}
}