/*
 * Mars Simulation Project
 * BuildingPanelAlgae.java
 * @date 2023-09-19
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.entitywindow.building;

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
import com.mars_sim.ui.swing.components.JDoubleLabel;
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

	// Caches
	
	/** The amount of water in the tank */
	private double algaeWaterRatio;

	private JDoubleLabel tempLabel;
	private JDoubleLabel algaeMassLabel;
	private JDoubleLabel idealAlgaeMassLabel;
	private JDoubleLabel maxAlgaeMassLabel;
	private JDoubleLabel algaeHarvestLabel;
	private JDoubleLabel algaeProducedLabel;
	
	private JDoubleLabel foodMassLabel;
	private JDoubleLabel foodDemandLabel;
	
	private JDoubleLabel waterMassLabel;
	
	private JLabel algaeWaterRatioLabel;
	
	private JDoubleLabel powerReqLabel;
	private JDoubleLabel radLabel;
	
	private JDoubleLabel waterUsageLabel;
	private JDoubleLabel greyWaterLabel;
	private JDoubleLabel o2GenLabel;
	private JDoubleLabel co2ConsumedLabel;
	private JDoubleLabel workTimeLabel;
	
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
		
		algaeMassLabel = new JDoubleLabel(StyleManager.DECIMAL_KG2, pond.getCurrentAlgae());
		labelPanel.addLabelledItem(Msg.getString("BuildingPanelAlgae.algaeMass"), algaeMassLabel);
				
		idealAlgaeMassLabel = new JDoubleLabel(StyleManager.DECIMAL_KG2, pond.getIdealAlgae());
		labelPanel.addLabelledItem(Msg.getString("BuildingPanelAlgae.idealAlgaeMass"), idealAlgaeMassLabel);
		
		maxAlgaeMassLabel = new JDoubleLabel(StyleManager.DECIMAL_KG2, pond.getMaxAlgae());
		labelPanel.addLabelledItem(Msg.getString("BuildingPanelAlgae.maxAlgaeMass"), maxAlgaeMassLabel);

		var algaeProducedCache = pond.computeDailyAverage(ResourceUtil.SPIRULINA_ID);
		algaeProducedLabel = new JDoubleLabel(StyleManager.DECIMAL1_KG_SOL, algaeProducedCache);
		labelPanel.addLabelledItem(Msg.getString("BuildingPanelAlgae.algae.produced"), algaeProducedLabel,
									Msg.getString("BuildingPanelAlgae.algae.produced.tooltip"));
		
		var algaeHarvestCache = pond.computeDailyAverage(ResourceUtil.SPIRULINA_ID);
		algaeHarvestLabel = new JDoubleLabel(StyleManager.DECIMAL1_KG_SOL, algaeHarvestCache);
		labelPanel.addLabelledItem(Msg.getString("BuildingPanelAlgae.algae.harvested"),
									algaeHarvestLabel,
									Msg.getString("BuildingPanelAlgae.algae.harvested.tooltip"));
		
		waterMassLabel = new JDoubleLabel(StyleManager.DECIMAL_LITER2, pond.getWaterMass());
		labelPanel.addLabelledItem(Msg.getString("BuildingPanelAlgae.waterMass"), waterMassLabel);
		
		algaeWaterRatio = pond.getAlgaeWaterRatio();
		algaeWaterRatioLabel = labelPanel.addTextField(Msg.getString("BuildingPanelAlgae.algaeWaterRatio"),
			StyleManager.DECIMAL2_G_LITER.format(algaeWaterRatio)
				+ " (" + Math.round(AlgaeFarming.ALGAE_TO_WATER_RATIO*100000.0)/100.0 + ")", null);
		
		foodMassLabel = new JDoubleLabel(StyleManager.DECIMAL_KG2, pond.getFoodMass());
		labelPanel.addLabelledItem(Msg.getString("BuildingPanelAlgae.foodMass"), foodMassLabel);
		
		var foodDemand = pond.getNutrientDemand();	
		foodDemandLabel = new JDoubleLabel(StyleManager.DECIMAL_PLACES2, foodDemand);
		labelPanel.addLabelledItem(Msg.getString("BuildingPanelAlgae.foodDemand"), foodDemandLabel);
		powerReqLabel = new JDoubleLabel(StyleManager.DECIMAL_KW, pond.getCombinedPowerLoad());
		labelPanel.addLabelledItem(Msg.getString("BuildingPanelAlgae.powerReq"), powerReqLabel);
		
		var tempCache = pond.getBuilding().getCurrentTemperature();	
		tempLabel = new JDoubleLabel(StyleManager.DECIMAL_CELCIUS, tempCache);
		labelPanel.addLabelledItem(Msg.getString("BuildingPanelAlgae.temp"), tempLabel);
			
		// Prepare solar irradiance label
		var radCache = surfaceFeatures.getSolarIrradiance(location);
		radLabel = new JDoubleLabel(StyleManager.DECIMAL_W_M2, radCache);
		labelPanel.addLabelledItem(Msg.getString("BuildingPanelAlgae.solarIrradiance.title"), radLabel, "Estimated sunlight on top of the greenhouse roof");
		
		var waterUsageCache = pond.computeDailyAverage(ResourceUtil.WATER_ID);
		waterUsageLabel = new JDoubleLabel(StyleManager.DECIMAL1_KG_SOL, waterUsageCache);
		labelPanel.addLabelledItem(Msg.getString("BuildingPanelAlgae.waterUsage.title"), waterUsageLabel,
							Msg.getString("BuildingPanelAlgae.waterUsage.tooltip"));

		var greyWaterProducedCache = pond.computeDailyAverage(ResourceUtil.GREY_WATER_ID);
		greyWaterLabel = new JDoubleLabel(StyleManager.DECIMAL1_KG_SOL, greyWaterProducedCache);
		labelPanel.addLabelledItem(Msg.getString("BuildingPanelAlgae.greyWaterProduced.title"), greyWaterLabel,
							Msg.getString("BuildingPanelAlgae.greyWaterProduced.tooltip"));
		
		var o2Cache = pond.computeDailyAverage(ResourceUtil.OXYGEN_ID);
		o2GenLabel = new JDoubleLabel(StyleManager.DECIMAL1_KG_SOL, o2Cache);
		labelPanel.addLabelledItem(Msg.getString("BuildingPanelAlgae.o2.title"), o2GenLabel,
							Msg.getString("BuildingPanelAlgae.o2.tooltip"));

		var co2Cache = pond.computeDailyAverage(ResourceUtil.CO2_ID);
		co2ConsumedLabel = new JDoubleLabel(StyleManager.DECIMAL1_KG_SOL, co2Cache);
		labelPanel.addLabelledItem(Msg.getString("BuildingPanelAlgae.co2.title"), co2ConsumedLabel,
							Msg.getString("BuildingPanelAlgae.co2.tooltip"));
		// Update the cumulative work time
		var workTimeCache = pond.getCumulativeWorkTime()/1000.0;
		workTimeLabel = new JDoubleLabel(StyleManager.DECIMAL3_SOLS, workTimeCache);
		labelPanel.addLabelledItem(Msg.getString("BuildingPanelAlgae.workTime.title"), workTimeLabel,
									Msg.getString("BuildingPanelAlgae.workTime.tooltip"));
	}


	@Override
	public void clockUpdate(ClockPulse pulse) {

		waterMassLabel.setValue(pond.getWaterMass());
		algaeMassLabel.setValue(pond.getCurrentAlgae());
		idealAlgaeMassLabel.setValue(pond.getIdealAlgae());		
		maxAlgaeMassLabel.setValue(pond.getMaxAlgae());

		double newAlgaeHarvested = pond.computeDailyAverage(ResourceUtil.SPIRULINA_ID);
		algaeHarvestLabel.setValue(newAlgaeHarvested);	
		
		double newAlgaeProduced = pond.computeDailyAverage(AlgaeFarming.PRODUCED_ALGAE_ID);
		algaeProducedLabel.setValue(newAlgaeProduced);
		
		double newAlgaeWaterRatio = pond.getAlgaeWaterRatio();
		if (DoubleMath.fuzzyEquals(algaeWaterRatio, newAlgaeWaterRatio, 0.1)) {
			algaeWaterRatio = newAlgaeWaterRatio;
			algaeWaterRatioLabel.setText(StyleManager.DECIMAL2_G_LITER.format(newAlgaeWaterRatio)
					+ " (" + Math.round(AlgaeFarming.ALGAE_TO_WATER_RATIO *100000.0)/100.0 + ")");
		}
		
		// Update foodMassLabel
		foodMassLabel.setValue(pond.getFoodMass());
		
		double newFoodDemand = pond.getNutrientDemand();
		foodDemandLabel.setValue(newFoodDemand);
		
		// Update powerReqLabel
		powerReqLabel.setValue(pond.getCombinedPowerLoad());
		tempLabel.setValue(getEntity().getCurrentTemperature());
		
		// Update solar irradiance label
		double rad = surfaceFeatures.getSolarIrradiance(location);
		radLabel.setValue(rad);
		
		// Update the average water usage
		double newWater = pond.computeDailyAverage(ResourceUtil.WATER_ID);
		waterUsageLabel.setValue(newWater);

		// Update the average O2 generated
		double newO2 = pond.computeDailyAverage(ResourceUtil.OXYGEN_ID);
		o2GenLabel.setValue(newO2);

		// Update the average CO2 consumed
		double newCo2 = pond.computeDailyAverage(ResourceUtil.CO2_ID);
		co2ConsumedLabel.setValue(newCo2);

		// Update the average grey water usage
		double newGreyWater = pond.computeDailyAverage(ResourceUtil.GREY_WATER_ID);
		greyWaterLabel.setValue(newGreyWater);
		
		// Update the cumulative work time
		double workTime = pond.getCumulativeWorkTime()/1000.0;
		workTimeLabel.setValue(workTime);
	}
}