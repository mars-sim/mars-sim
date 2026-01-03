/*
 * Mars Simulation Project
 * BuildingPanelAlgae.java
 * @date 2023-09-19
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.google.common.math.DoubleMath;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.farming.AlgaeFarming;
import com.mars_sim.core.environment.SurfaceFeatures;
import com.mars_sim.core.map.location.Coordinates;
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
 * The BuildingPanelAlgae class is a building function panel for
 * the algae pond building.
 */
@SuppressWarnings("serial")
class BuildingPanelAlgae extends EntityTabPanel<Building> 
	implements TemporalComponent {

	private static final String FISH_ICON = "fish";
	private static final String DEGREE_CELSIUS = " " + Msg.getString("temperature.sign.degreeCelsius");

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
	 * @param pond the algae pond
	 * @param context the UI context
	 */
	public BuildingPanelAlgae(AlgaeFarming pond, UIContext context) {
		super(
			Msg.getString("BuildingPanelAlgae.title"), 
			ImageLoader.getIconByName(FISH_ICON), null,
			context, pond.getBuilding()
		);
		
		this.pond = pond;
		
		location = pond.getBuilding().getCoordinates();
	
		surfaceFeatures = context.getSimulation().getSurfaceFeatures();
	}
	
	/**
	 * Builds the UI.
	 */
	@Override
	protected void buildUI(JPanel center) {
		AttributePanel labelPanel = new AttributePanel(18);
		center.add(labelPanel, BorderLayout.NORTH);

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

		algaeProducedCache = pond.computeDailyAverage(ResourceUtil.SPIRULINA_ID);
		algaeProducedLabel = labelPanel.addTextField(Msg.getString("BuildingPanelAlgae.algae.produced"),
									StyleManager.DECIMAL1_KG_SOL.format(algaeProducedCache),
									Msg.getString("BuildingPanelAlgae.algae.produced.tooltip"));
		
		algaeHarvestCache = pond.computeDailyAverage(ResourceUtil.SPIRULINA_ID);
		algaeHarvestLabel = labelPanel.addTextField(Msg.getString("BuildingPanelAlgae.algae.harvested"),
									StyleManager.DECIMAL1_KG_SOL.format(algaeHarvestCache),
									Msg.getString("BuildingPanelAlgae.algae.harvested.tooltip"));
		
		waterMass = pond.getWaterMass();
		waterMassLabel = labelPanel.addTextField(Msg.getString("BuildingPanelAlgae.waterMass"),
				StyleManager.DECIMAL_LITER2.format(waterMass), null);
		
		algaeWaterRatio = pond.getAlgaeWaterRatio();
		algaeWaterRatioLabel = labelPanel.addTextField(Msg.getString("BuildingPanelAlgae.algaeWaterRatio"),
			StyleManager.DECIMAL2_G_LITER.format(algaeWaterRatio)
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
				StyleManager.DECIMAL_W_M2.format(radCache), "Estimated sunlight on top of the greenhouse roof");
		
		waterUsageCache = pond.computeDailyAverage(ResourceUtil.WATER_ID);
		waterUsageLabel = labelPanel.addTextField(Msg.getString("BuildingPanelAlgae.waterUsage.title"),
									StyleManager.DECIMAL1_KG_SOL.format(waterUsageCache),
									Msg.getString("BuildingPanelAlgae.waterUsage.tooltip"));

		greyWaterProducedCache = pond.computeDailyAverage(ResourceUtil.GREY_WATER_ID);
		greyWaterLabel = labelPanel.addTextField(Msg.getString("BuildingPanelAlgae.greyWaterProduced.title"),
									StyleManager.DECIMAL1_KG_SOL.format(greyWaterProducedCache),
									Msg.getString("BuildingPanelAlgae.greyWaterProduced.tooltip"));
		
		o2Cache = pond.computeDailyAverage(ResourceUtil.OXYGEN_ID);
		o2GenLabel = labelPanel.addTextField(Msg.getString("BuildingPanelAlgae.o2.title"),
									StyleManager.DECIMAL1_KG_SOL.format(o2Cache),
									Msg.getString("BuildingPanelAlgae.o2.tooltip"));

		co2Cache = pond.computeDailyAverage(ResourceUtil.CO2_ID);
		co2ConsumedLabel = labelPanel.addTextField(Msg.getString("BuildingPanelAlgae.co2.title"),
									StyleManager.DECIMAL1_KG_SOL.format(co2Cache),
								 	Msg.getString("BuildingPanelAlgae.co2.tooltip"));
	
		// Update the cumulative work time
		workTimeCache = pond.getCumulativeWorkTime()/1000.0;
		workTimeLabel = labelPanel.addTextField(Msg.getString("BuildingPanelAlgae.workTime.title"),
									StyleManager.DECIMAL3_SOLS.format(workTimeCache),
									Msg.getString("BuildingPanelAlgae.workTime.tooltip"));
	}


	@Override
	public void clockUpdate(ClockPulse pulse) {

		double newWaterMass = pond.getWaterMass();
		if (DoubleMath.fuzzyEquals(waterMass, newWaterMass, 0.1)) {
			waterMass = newWaterMass;
			waterMassLabel.setText(StyleManager.DECIMAL_KG2.format(newWaterMass));
		}
		
		double newAlgae = pond.getCurrentAlgae();
		if (DoubleMath.fuzzyEquals(algaeMass, newAlgae, 0.1)) {
			algaeMass = newAlgae;
			algaeMassLabel.setText(StyleManager.DECIMAL_KG2.format(newAlgae));
		}
		
		double newIdealAlgae = pond.getIdealAlgae();
		if (DoubleMath.fuzzyEquals(idealAlgaeMass, newIdealAlgae, 0.1)) {
			idealAlgaeMass = newIdealAlgae;
			idealAlgaeMassLabel.setText(StyleManager.DECIMAL_KG2.format(newIdealAlgae));
		}
		
		double newMaxAlgae = pond.getMaxAlgae();
		if (DoubleMath.fuzzyEquals(maxAlgaeMass, newMaxAlgae, 0.1)) {
			maxAlgaeMass = newMaxAlgae;
			maxAlgaeMassLabel.setText(StyleManager.DECIMAL_KG2.format(newMaxAlgae));
		}

		double newAlgaeHarvested = pond.computeDailyAverage(ResourceUtil.SPIRULINA_ID);
		if (DoubleMath.fuzzyEquals(algaeHarvestCache, newAlgaeHarvested, 0.1)) {
			algaeHarvestCache = newAlgaeHarvested;
			algaeHarvestLabel.setText(StyleManager.DECIMAL1_KG_SOL.format(newAlgaeHarvested));
		}	
		
		double newAlgaeProduced = pond.computeDailyAverage(AlgaeFarming.PRODUCED_ALGAE_ID);
		if (DoubleMath.fuzzyEquals(algaeProducedCache, newAlgaeProduced, 0.1)) {
			algaeProducedCache = newAlgaeProduced;
			algaeProducedLabel.setText(StyleManager.DECIMAL1_KG_SOL.format(newAlgaeProduced));
		}
		
		double newAlgaeWaterRatio = pond.getAlgaeWaterRatio();
		if (DoubleMath.fuzzyEquals(algaeWaterRatio, newAlgaeWaterRatio, 0.1)) {
			algaeWaterRatio = newAlgaeWaterRatio;
			algaeWaterRatioLabel.setText(StyleManager.DECIMAL2_G_LITER.format(newAlgaeWaterRatio)
					+ " (" + Math.round(AlgaeFarming.ALGAE_TO_WATER_RATIO *100000.0)/100.0 + ")");
		}
		
		double newFoodMass = pond.getFoodMass();
		if (DoubleMath.fuzzyEquals(foodMass, newFoodMass, 0.1)) {
			foodMass = newFoodMass;
			foodMassLabel.setText(StyleManager.DECIMAL_KG2.format(newFoodMass));
		}
		
		double newFoodDemand = pond.getNutrientDemand();
		if (DoubleMath.fuzzyEquals(foodDemand, newFoodDemand, 0.01)) {
			foodDemand = newFoodDemand;
			foodDemandLabel.setText(StyleManager.DECIMAL_PLACES1.format(newFoodDemand));
		}
		
		double newPowerReq = pond.getCombinedPowerLoad();	
		if (DoubleMath.fuzzyEquals(powerReq, newPowerReq, 0.1)) {
			powerReq = newPowerReq;
			powerReqLabel.setText(StyleManager.DECIMAL_KW.format(newPowerReq));
		}
		
		double newTemp = getEntity().getCurrentTemperature();
		if (DoubleMath.fuzzyEquals(tempCache, newTemp, 0.1)) {
			tempCache = newTemp;
			tempLabel.setText(StyleManager.DECIMAL_PLACES1.format(newTemp) + DEGREE_CELSIUS);
		}
		
		// Update solar irradiance label
		double rad = surfaceFeatures.getSolarIrradiance(location);
		if (DoubleMath.fuzzyEquals(radCache, rad, 0.1)) {
			radCache = rad;
			radLabel.setText(StyleManager.DECIMAL_W_M2.format(rad));
		}
		
		// Update the average water usage
		double newWater = pond.computeDailyAverage(ResourceUtil.WATER_ID);
		if (DoubleMath.fuzzyEquals(waterUsageCache, newWater, 0.1)) {
			waterUsageCache = newWater;
			waterUsageLabel.setText(StyleManager.DECIMAL1_KG_SOL.format(newWater));
		}

		// Update the average O2 generated
		double newO2 = pond.computeDailyAverage(ResourceUtil.OXYGEN_ID);
		if (DoubleMath.fuzzyEquals(o2Cache, newO2, 0.1)) {
			o2Cache = newO2;
			o2GenLabel.setText(StyleManager.DECIMAL1_KG_SOL.format(newO2));
		}

		// Update the average CO2 consumed
		double newCo2 = pond.computeDailyAverage(ResourceUtil.CO2_ID);
		if (DoubleMath.fuzzyEquals(co2Cache, newCo2, 0.1)) {
			co2Cache = newCo2;
			co2ConsumedLabel.setText(StyleManager.DECIMAL1_KG_SOL.format(newCo2));
		}

		// Update the average grey water usage
		double newGreyWater = pond.computeDailyAverage(ResourceUtil.GREY_WATER_ID);
		if (DoubleMath.fuzzyEquals(greyWaterProducedCache, newGreyWater, 0.1)) {
			greyWaterProducedCache = newGreyWater;
			greyWaterLabel.setText(StyleManager.DECIMAL1_KG_SOL.format(newGreyWater));
		}
		
		// Update the cumulative work time
		double workTime = pond.getCumulativeWorkTime()/1000.0;
		if (DoubleMath.fuzzyEquals(workTimeCache, workTime, 0.1)) {
			workTimeCache = workTime;
			workTimeLabel.setText(StyleManager.DECIMAL3_SOLS.format(workTime));
		}
	}
}