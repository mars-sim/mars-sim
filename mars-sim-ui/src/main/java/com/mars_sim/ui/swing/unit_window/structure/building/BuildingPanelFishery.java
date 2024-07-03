/*
 * Mars Simulation Project
 * BuildingPanelFishery.java
 * @date 2023-12-07
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.mars_sim.core.structure.building.function.farming.Fishery;
import com.mars_sim.tools.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.utils.AttributePanel;

/**
 * The BuildingPanelFishery class is a building function panel for
 * the fish farm of a settlement building.
 */
@SuppressWarnings("serial")
public class BuildingPanelFishery extends BuildingFunctionPanel {

	private static final String FISH_ICON = "fish";

	// Caches
	private int numFish;
	private int numIdealFish; 
	private int maxFish;
	private int numWeed;
	
	private double fishMass;
	private double weedMass;
	private double weedDemand;
	private double powerReq;
	/** The amount of water in the tank */
	private double waterMass;
	/** The cache value for the work time done in this greenhouse. */
	private double workTimeCache;
	
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
	 * @param The panel for the Fishery
	 * @param The main desktop
	 */
	public BuildingPanelFishery(Fishery tank, MainDesktopPane desktop) {
		super(
			Msg.getString("BuildingPanelFishery.title"), 
			ImageLoader.getIconByName(FISH_ICON), 
			tank.getBuilding(), 
			desktop
		);
		
		this.tank = tank;
	}
	
	/**
	 * Builds the UI.
	 */
	@Override
	protected void buildUI(JPanel center) {
		AttributePanel labelPanel = new AttributePanel(11);
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
								 StyleManager.DECIMAL_KG2.format(fishMass), null);
				
		numIdealFish = tank.getIdealFish();
		numIdealFishLabel = labelPanel.addTextField(Msg.getString("BuildingPanelFishery.numIdealFish"),
									Integer.toString(numIdealFish), null);
		
		maxFish = tank.getMaxFish();
		maxFishLabel = labelPanel.addTextField(Msg.getString("BuildingPanelFishery.maxFish"),
									Integer.toString(maxFish), null);

		numWeed = tank.getNumWeed();
		numWeedLabel = labelPanel.addTextField(Msg.getString("BuildingPanelFishery.numWeed"),
									Integer.toString(numWeed), null);
		
		weedMass = tank.getTotalWeedMass();	
		weedMassLabel = labelPanel.addTextField(Msg.getString("BuildingPanelFishery.weedMass"),
								 StyleManager.DECIMAL_KG2.format(weedMass), null);
		
		weedDemand = tank.getWeedDemand();	
		weedDemandLabel = labelPanel.addTextField(Msg.getString("BuildingPanelFishery.weedDemand"),
								 StyleManager.DECIMAL_PLACES2.format(weedDemand), null);
		
		powerReq = tank.getPowerRequired();	
		powerReqLabel = labelPanel.addTextField(Msg.getString("BuildingPanelFishery.powerReq"),
								 StyleManager.DECIMAL_KW.format(powerReq), null);
		
		// Update the cumulative work time
		workTimeCache = tank.getCumulativeWorkTime()/1000.0;
		workTimeLabel = labelPanel.addTextField(Msg.getString("BuildingPanelAlgae.workTime.title"),
									StyleManager.DECIMAL_SOLS3.format(workTimeCache),
									Msg.getString("BuildingPanelAlgae.workTime.tooltip"));
	}

	/**
	 * Updates this panel with latest values.
	 */
	@Override
	public void update() {	

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
		if (fishMass != newFishMass) {
			fishMass = newFishMass;
			fishMassLabel.setText(StyleManager.DECIMAL_KG.format(newFishMass));
		}
		
		double newWeedMass = tank.getTotalWeedMass();
		if (weedMass != newWeedMass) {
			weedMass = newWeedMass;
			weedMassLabel.setText(StyleManager.DECIMAL_KG.format(newWeedMass));
		}
		
		double newWeedDemand = tank.getWeedDemand();
		if (weedDemand != newWeedDemand) {
			weedDemand = newWeedDemand;
			weedDemandLabel.setText(StyleManager.DECIMAL_PLACES1.format(newWeedDemand));
		}
		
		double newWaterMass = tank.getWaterMass();
		if (waterMass != newWaterMass) {
			waterMass = newWaterMass;
			waterMassLabel.setText(StyleManager.DECIMAL_KG2.format(newWaterMass));
		}
		
		double newPowerReq = tank.getPowerRequired();	
		if (powerReq != newPowerReq) {
			powerReq = newPowerReq;
			powerReqLabel.setText(StyleManager.DECIMAL_KW.format(newPowerReq));
		}
		
		// Update the cumulative work time
		double workTime = tank.getCumulativeWorkTime()/1000.0;
		if (workTimeCache != workTime) {
			workTimeCache = workTime;
			workTimeLabel.setText(StyleManager.DECIMAL_SOLS3.format(workTime));
		}
	}
	
	/**
	 * Prepares for deletion.
	 */
	public void destroy() {
		super.destroy();
		
		waterMassLabel = null;
		powerReqLabel = null;
		workTimeLabel = null;
		
		numFishLabel = null;
		numIdealFishLabel = null;
		maxFishLabel = null;
		fishMassLabel = null;
		
		numWeedLabel = null;
		weedMassLabel = null;
		weedDemandLabel = null;
		tank = null;
	}
}