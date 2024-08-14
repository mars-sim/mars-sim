/*
 * Mars Simulation Project
 * BuildingPanelAlgae.java
 * @date 2023-09-19
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.text.DecimalFormat;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.mars_sim.core.environment.SurfaceFeatures;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.building.function.farming.AlgaeFarming;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.utils.AttributePanel;

/**
 * The BuildingPanelAlgae class is a building function panel for
 * the algae pond building.
 */
@SuppressWarnings("serial")
public class BuildingPanelAlgae extends BuildingFunctionPanel {

	private static final String FISH_ICON = "fish";
	private static final String DEGREE_CELSIUS = " " + Msg.getString("temperature.sign.degreeCelsius");
	
	private static final DecimalFormat DECIMAL_KG_SOL = StyleManager.DECIMAL1_KG_SOL;
	private static final DecimalFormat DECIMAL_G_LITER = StyleManager.DECIMAL2_G_LITER;

	/** Is UI constructed. */
	private boolean uiDone = false;
	
	// Caches
	private double algaeMass;
	private double idealAlgaeMass; 
	private double maxAlgaeMass;
	private double foodMass;
	private double foodDemand;
	private double powerReq;
	private double tempCache;
	
	/** The amount of water in the tank */
	private double waterMass;	
	/** The cache value for the average water usage per sol per square meters. */
	private double waterUsageCache;
	/** The cache value for the average grey water produced per sol per square meters. */
	private double greyWaterProducedCache;
	/** The cache value for the average O2 generated per sol. */
	private double o2Cache;
	/** The cache value for the average CO2 consumed per sol. */
	private double co2Cache;
	/** The cache value for the average kg of algae harvested per sol . */
	private double algaeHarvestCache;
	/** The cache value for the average kg of algae produced per sol . */
	private double algaeProducedCache;
	/** The cache value for the work time done in this greenhouse. */
	private double workTimeCache;
	/** The cache for the amount of solar irradiance. */
	private double radCache;
	/** The cache for the ratio of algae to water. */
	private double algaeWaterRatio;

	private JLabel tempLabel;
	private JLabel algaeMassLabel;
	private JLabel idealAlgaeMassLabel;
	private JLabel maxAlgaeMassLabel;
	private JLabel algaeHarvestLabel;
	private JLabel algaeProducedLabel;
	
	private JLabel foodMassLabel;
	private JLabel foodDemandLabel;
	
	private JLabel waterMassLabel;
	
	private JLabel algaeWaterRatioLabel;
	
	private JLabel powerReqLabel;
	private JLabel radLabel;
	
	private JLabel waterUsageLabel;
	private JLabel greyWaterLabel;
	private JLabel o2GenLabel;
	private JLabel co2ConsumedLabel;
	private JLabel workTimeLabel;
	
	private AlgaeFarming pond;

	private Coordinates location;

	private SurfaceFeatures surfaceFeatures;
	
	/**
	 * Constructor.
	 * 
	 * @param The panel for AlgaeFarming
	 * @param The main desktop
	 */
	public BuildingPanelAlgae(AlgaeFarming pond, MainDesktopPane desktop) {
		super(
			Msg.getString("BuildingPanelAlgae.title"), 
			ImageLoader.getIconByName(FISH_ICON), 
			pond.getBuilding(), 
			desktop
		);
		
		this.pond = pond;
		
		location = pond.getBuilding().getCoordinates();
	
		surfaceFeatures = getSimulation().getSurfaceFeatures();
	}
	
	/**
	 * Builds the UI.
	 */
	@Override
	protected void buildUI(JPanel center) {
		AttributePanel labelPanel = new AttributePanel(18);
		center.add(labelPanel, BorderLayout.NORTH);
		
//		labelPanel.addTextField(Msg.getString("BuildingPanelAlgae.tankSize"), 
//				Double.toString(pond.getTankSize()), null);
		
		labelPanel.addTextField(Msg.getString("BuildingPanelAlgae.algae.type"), 
				"Spirulina", null);
		
		algaeMass = pond.getCurrentAlgae();
		algaeMassLabel = labelPanel.addTextField(Msg.getString("BuildingPanelAlgae.algaeMass"),
				StyleManager.DECIMAL_KG2.format(algaeMass), null);
				
		idealAlgaeMass = pond.getIdealAlgae();
		idealAlgaeMassLabel = labelPanel.addTextField(Msg.getString("BuildingPanelAlgae.idealAlgaeMass"),
				StyleManager.DECIMAL_KG2.format(idealAlgaeMass), null);
		
		maxAlgaeMass = pond.getMaxAlgae();
		maxAlgaeMassLabel = labelPanel.addTextField(Msg.getString("BuildingPanelAlgae.maxAlgaeMass"),
				StyleManager.DECIMAL_KG2.format(maxAlgaeMass), null);

		algaeProducedCache = pond.computeDailyAverage(AlgaeFarming.PRODUCED_ALGAE_ID);
		algaeProducedLabel = labelPanel.addTextField(Msg.getString("BuildingPanelAlgae.algae.produced"),
									DECIMAL_KG_SOL.format(algaeProducedCache),
									Msg.getString("BuildingPanelAlgae.algae.produced.tooltip"));
		
		algaeHarvestCache = pond.computeDailyAverage(AlgaeFarming.HARVESTED_ALGAE_ID);
		algaeHarvestLabel = labelPanel.addTextField(Msg.getString("BuildingPanelAlgae.algae.harvested"),
									DECIMAL_KG_SOL.format(algaeHarvestCache),
									Msg.getString("BuildingPanelAlgae.algae.harvested.tooltip"));
		
		waterMass = pond.getWaterMass();
		waterMassLabel = labelPanel.addTextField(Msg.getString("BuildingPanelAlgae.waterMass"),
				StyleManager.DECIMAL_LITER2.format(waterMass), null);
		
		algaeWaterRatio = pond.getAlgaeWaterRatio();
		algaeWaterRatioLabel = labelPanel.addTextField(Msg.getString("BuildingPanelAlgae.algaeWaterRatio"),
			DECIMAL_G_LITER.format(algaeWaterRatio)
				+ " (" + Math.round(AlgaeFarming.ALGAE_TO_WATER_RATIO*100000.0)/100.0 + ")", null);
		
		foodMass = pond.getFoodMass();	
		foodMassLabel = labelPanel.addTextField(Msg.getString("BuildingPanelAlgae.foodMass"),
								 StyleManager.DECIMAL_KG2.format(foodMass), null);
		
		foodDemand = pond.getNutrientDemand();	
		foodDemandLabel = labelPanel.addTextField(Msg.getString("BuildingPanelAlgae.foodDemand"),
								 StyleManager.DECIMAL_PLACES2.format(foodDemand), null);
		
		powerReq = pond.getCombinedPowerLoad();	
		powerReqLabel = labelPanel.addTextField(Msg.getString("BuildingPanelAlgae.powerReq"),
								 StyleManager.DECIMAL_KW.format(powerReq), null);
		
		tempCache = pond.getBuilding().getCurrentTemperature();	
		tempLabel = labelPanel.addTextField(Msg.getString("BuildingPanelAlgae.temp"),
								 StyleManager.DECIMAL_PLACES1.format(tempCache) + DEGREE_CELSIUS, null);
			
		// Prepare solar irradiance label
		radCache = surfaceFeatures.getSolarIrradiance(location);
		radLabel = labelPanel.addTextField(Msg.getString("BuildingPanelAlgae.solarIrradiance.title"),
							 radCache + " W/m", "Estimated sunlight on top of the greenhouse roof");
		
		waterUsageCache = pond.computeDailyAverage(ResourceUtil.waterID);
		waterUsageLabel = labelPanel.addTextField(Msg.getString("BuildingPanelAlgae.waterUsage.title"),
									DECIMAL_KG_SOL.format(waterUsageCache),
									Msg.getString("BuildingPanelAlgae.waterUsage.tooltip"));

		greyWaterProducedCache = pond.computeDailyAverage(ResourceUtil.greyWaterID);
		greyWaterLabel = labelPanel.addTextField(Msg.getString("BuildingPanelAlgae.greyWaterProduced.title"),
									DECIMAL_KG_SOL.format(greyWaterProducedCache),
									Msg.getString("BuildingPanelAlgae.greyWaterProduced.tooltip"));
		
		o2Cache = pond.computeDailyAverage(ResourceUtil.oxygenID);
		o2GenLabel = labelPanel.addTextField(Msg.getString("BuildingPanelAlgae.o2.title"),
									DECIMAL_KG_SOL.format(o2Cache),
									Msg.getString("BuildingPanelAlgae.o2.tooltip"));

		co2Cache = pond.computeDailyAverage(ResourceUtil.co2ID);
		co2ConsumedLabel = labelPanel.addTextField(Msg.getString("BuildingPanelAlgae.co2.title"),
									DECIMAL_KG_SOL.format(co2Cache),
								 	Msg.getString("BuildingPanelAlgae.co2.tooltip"));
	
		// Update the cumulative work time
		workTimeCache = pond.getCumulativeWorkTime()/1000.0;
		workTimeLabel = labelPanel.addTextField(Msg.getString("BuildingPanelAlgae.workTime.title"),
									StyleManager.DECIMAL3_SOLS.format(workTimeCache),
									Msg.getString("BuildingPanelAlgae.workTime.tooltip"));
	}

	/**
	 * Updates this panel with latest values.
	 */
	@Override
	public void update() {	
		if (!uiDone)
			initializeUI();
		
		double newWaterMass = pond.getWaterMass();
		if (waterMass != newWaterMass) {
			waterMass = newWaterMass;
			waterMassLabel.setText(StyleManager.DECIMAL_KG2.format(newWaterMass));
		}
		
		double newAlgae = pond.getCurrentAlgae();
		if (algaeMass != newAlgae) {
			algaeMass = newAlgae;
			algaeMassLabel.setText(StyleManager.DECIMAL_KG2.format(newAlgae));
		}
		
		double newIdealAlgae = pond.getIdealAlgae();
		if (idealAlgaeMass != newIdealAlgae) {
			idealAlgaeMass = newIdealAlgae;
			idealAlgaeMassLabel.setText(StyleManager.DECIMAL_KG2.format(newIdealAlgae));
		}
		
		double newMaxAlgae = pond.getMaxAlgae();
		if (maxAlgaeMass != newMaxAlgae) {
			maxAlgaeMass = newMaxAlgae;
			maxAlgaeMassLabel.setText(StyleManager.DECIMAL_KG2.format(newMaxAlgae));
		}

		double newAlgaeHarvested = pond.computeDailyAverage(AlgaeFarming.HARVESTED_ALGAE_ID);
		if (algaeHarvestCache != newAlgaeHarvested) {
			algaeHarvestCache = newAlgaeHarvested;
			algaeHarvestLabel.setText(DECIMAL_KG_SOL.format(newAlgaeHarvested));
		}	
		
		double newAlgaeProduced = pond.computeDailyAverage(AlgaeFarming.PRODUCED_ALGAE_ID);
		if (algaeProducedCache != newAlgaeProduced) {
			algaeProducedCache = newAlgaeProduced;
			algaeProducedLabel.setText(DECIMAL_KG_SOL.format(newAlgaeProduced));
		}
		
		double newAlgaeWaterRatio = pond.getAlgaeWaterRatio();
		if (algaeWaterRatio != newAlgaeWaterRatio) {
			algaeWaterRatio = newAlgaeWaterRatio;
			algaeWaterRatioLabel.setText(DECIMAL_G_LITER.format(newAlgaeWaterRatio)
					+ " (" + Math.round(AlgaeFarming.ALGAE_TO_WATER_RATIO *100000.0)/100.0 + ")");
		}
		
		double newFoodMass = pond.getFoodMass();
		if (foodMass != newFoodMass) {
			foodMass = newFoodMass;
			foodMassLabel.setText(StyleManager.DECIMAL_KG2.format(newFoodMass));
		}
		
		double newFoodDemand = pond.getNutrientDemand();
		if (foodDemand != newFoodDemand) {
			foodDemand = newFoodDemand;
			foodDemandLabel.setText(StyleManager.DECIMAL_PLACES1.format(newFoodDemand));
		}
		
		double newPowerReq = pond.getCombinedPowerLoad();	
		if (powerReq != newPowerReq) {
			powerReq = newPowerReq;
			powerReqLabel.setText(StyleManager.DECIMAL_KW.format(newPowerReq));
		}
		
		double newTemp = building.getCurrentTemperature();
		if (tempCache != newTemp) {
			tempCache = newTemp;
			tempLabel.setText(StyleManager.DECIMAL_PLACES1.format(newTemp) + DEGREE_CELSIUS);
		}
		
		// Update solar irradiance label
		double rad = Math.round(surfaceFeatures.getSolarIrradiance(location)*10.0)/10.0;
		if (radCache != rad) {
			radCache = rad;
			radLabel.setText(String.valueOf(rad) + " W/m");
		}
		
		// Update the average water usage
		double newWater = pond.computeDailyAverage(ResourceUtil.waterID);
		if (waterUsageCache != newWater) {
			waterUsageCache = newWater;
			waterUsageLabel.setText(DECIMAL_KG_SOL.format(newWater));
		}

		// Update the average O2 generated
		double newO2 = pond.computeDailyAverage(ResourceUtil.oxygenID);
		if (o2Cache != newO2) {
			o2Cache = newO2;
			o2GenLabel.setText(DECIMAL_KG_SOL.format(newO2));
		}

		// Update the average CO2 consumed
		double newCo2 = pond.computeDailyAverage(ResourceUtil.co2ID);
		if (co2Cache != newCo2) {
			co2Cache = newCo2;
			co2ConsumedLabel.setText(DECIMAL_KG_SOL.format(newCo2));
		}

		// Update the average grey water usage
		double newGreyWater = pond.computeDailyAverage(ResourceUtil.greyWaterID);
		if (greyWaterProducedCache != newGreyWater) {
			greyWaterProducedCache = newGreyWater;
			greyWaterLabel.setText(DECIMAL_KG_SOL.format(newGreyWater));
		}
		
		// Update the cumulative work time
		double workTime = pond.getCumulativeWorkTime()/1000.0;
		if (workTimeCache != workTime) {
			workTimeCache = workTime;
			workTimeLabel.setText(StyleManager.DECIMAL3_SOLS.format(workTime));
		}
	}
	
	/**
	 * Prepares for deletion.
	 */
	public void destroy() {
		super.destroy();
		
		tempLabel = null;
		algaeMassLabel = null;
		idealAlgaeMassLabel = null;
		maxAlgaeMassLabel = null;
		algaeHarvestLabel = null;
		algaeProducedLabel = null;
		
		foodMassLabel = null;
		foodDemandLabel = null;
		
		waterMassLabel = null;
		
		algaeWaterRatioLabel = null;
		
		powerReqLabel = null;
		radLabel = null;
		
		waterUsageLabel = null;
		greyWaterLabel = null;
		o2GenLabel = null;
		co2ConsumedLabel = null;
		workTimeLabel = null;
		pond = null;
		location = null;
		surfaceFeatures = null;
	}
}