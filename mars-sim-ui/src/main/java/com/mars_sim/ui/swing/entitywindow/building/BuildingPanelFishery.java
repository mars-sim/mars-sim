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

import com.google.common.math.DoubleMath;
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
	
	private double fishHarvestedCache;
	private double fishMass;
	private double weedMass;
	private double weedDemand;
	private double powerReq;
	/** The amount of water in the tank */
	private double waterMass;
	/** The cache value for the work time done in this greenhouse. */
	private double workTimeCache;
	
	private double ageCache;
	
	private JLabel ageLabel;
	private JLabel fishHarvestedLabel;
	private JLabel numFishLabel;
	private JLabel numIdealFishLabel;
	private JLabel maxFishLabel;
	private JLabel fishMassLabel;
	
	private JLabel numWeedLabel;
	private JLabel weedMassLabel;
	private JLabel weedDemandLabel;

	private JLabel waterMassLabel;
	private JLabel powerReqLabel;
	private JLabel workTimeLabel;
	
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
		
		waterMass = tank.getWaterMass();
		waterMassLabel = labelPanel.addTextField(Msg.getString("BuildingPanelAlgae.waterMass"),
				StyleManager.DECIMAL_LITER2.format(waterMass), null);
		
		numFish = tank.getNumFish();
		numFishLabel = labelPanel.addTextField(Msg.getString("BuildingPanelFishery.numFish"),
									Integer.toString(numFish), null);
				
		fishMass = tank.getTotalFishMass();	
		fishMassLabel = labelPanel.addTextField(Msg.getString("BuildingPanelFishery.fishMass"),
								 StyleManager.DECIMAL_KG.format(fishMass), null);
				
		numIdealFish = tank.getIdealFish();
		numIdealFishLabel = labelPanel.addTextField(Msg.getString("BuildingPanelFishery.numIdealFish"),
									Integer.toString(numIdealFish), null);
		
		maxFish = tank.getMaxFish();
		maxFishLabel = labelPanel.addTextField(Msg.getString("BuildingPanelFishery.maxFish"),
									Integer.toString(maxFish), null);
		
		ageCache = tank.getAverageAge()/1000;
		ageLabel = labelPanel.addRow(Msg.getString("BuildingPanelFishery.age"),
				StyleManager.DECIMAL2_SOLS.format(ageCache));
		
		fishHarvestedCache = tank.computeDailyAverage(ResourceUtil.FISH_MEAT_ID);
		fishHarvestedLabel = labelPanel.addTextField(Msg.getString("BuildingPanelFishery.harvestedFish"),
				StyleManager.DECIMAL1_KG_SOL.format(fishHarvestedCache),
									Msg.getString("BuildingPanelFishery.harvestedFish.tooltip"));
		numWeed = tank.getNumWeed();
		numWeedLabel = labelPanel.addTextField(Msg.getString("BuildingPanelFishery.numWeed"),
									Integer.toString(numWeed), null);
		
		weedMass = tank.getTotalWeedMass();	
		weedMassLabel = labelPanel.addTextField(Msg.getString("BuildingPanelFishery.weedMass"),
								 StyleManager.DECIMAL_KG.format(weedMass), null);
		
		weedDemand = tank.getWeedDemand();	
		weedDemandLabel = labelPanel.addTextField(Msg.getString("BuildingPanelFishery.weedDemand"),
								 StyleManager.DECIMAL_PLACES2.format(weedDemand), null);
		
		powerReq = tank.getCombinedPowerLoad();	
		powerReqLabel = labelPanel.addTextField(Msg.getString("BuildingPanelFishery.powerReq"),
								 StyleManager.DECIMAL_KW.format(powerReq), null);
		
		// Update the cumulative work time
		workTimeCache = tank.getCumulativeWorkTime()/1000.0;
		workTimeLabel = labelPanel.addTextField(Msg.getString("BuildingPanelAlgae.workTime.title"),
									StyleManager.DECIMAL3_SOLS.format(workTimeCache),
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
		
		double newFishMass = tank.getTotalFishMass();
		if (DoubleMath.fuzzyEquals(fishMass, newFishMass, 0.1)) {
			fishMass = newFishMass;
			fishMassLabel.setText(StyleManager.DECIMAL_KG.format(newFishMass));
		}
		
		double newFishHarvest = tank.computeDailyAverage(ResourceUtil.FISH_MEAT_ID);
		if (DoubleMath.fuzzyEquals(fishHarvestedCache, newFishHarvest, 0.1)) {
			fishHarvestedCache = newFishHarvest;
			fishHarvestedLabel.setText(StyleManager.DECIMAL1_KG_SOL.format(fishHarvestedCache));
		}
		
		double newAge = tank.getAverageAge()/1000;
		if (DoubleMath.fuzzyEquals(ageCache, newAge, 0.1)) {
			ageCache = newAge;
			ageLabel.setText(StyleManager.DECIMAL2_SOLS.format(newAge));
		}

		double newWeedMass = tank.getTotalWeedMass();
		if (DoubleMath.fuzzyEquals(weedMass, newWeedMass, 0.1)) {
			weedMass = newWeedMass;
			weedMassLabel.setText(StyleManager.DECIMAL_KG.format(newWeedMass));
		}
		
		double newWeedDemand = tank.getWeedDemand();
		if (DoubleMath.fuzzyEquals(weedDemand, newWeedDemand, 0.1)) {
			weedDemand = newWeedDemand;
			weedDemandLabel.setText(StyleManager.DECIMAL_PLACES1.format(newWeedDemand));
		}
		
		double newWaterMass = tank.getWaterMass();
		if (DoubleMath.fuzzyEquals(waterMass, newWaterMass, 0.1)) {
			waterMass = newWaterMass;
			waterMassLabel.setText(StyleManager.DECIMAL_KG2.format(newWaterMass));
		}
		
		double newPowerReq = tank.getCombinedPowerLoad();	
		if (DoubleMath.fuzzyEquals(powerReq, newPowerReq, 0.1)) {
			powerReq = newPowerReq;
			powerReqLabel.setText(StyleManager.DECIMAL_KW.format(newPowerReq));
		}
		
		// Update the cumulative work time
		double workTime = tank.getCumulativeWorkTime()/1000.0;
		if (DoubleMath.fuzzyEquals(workTimeCache, workTime, 0.05)) {
			workTimeCache = workTime;
			workTimeLabel.setText(StyleManager.DECIMAL3_SOLS.format(workTime));
		}
	}
}