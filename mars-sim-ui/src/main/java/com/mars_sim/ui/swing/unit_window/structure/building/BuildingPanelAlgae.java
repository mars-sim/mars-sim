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

import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.building.function.farming.AlgaeFarming;
import com.mars_sim.tools.Msg;
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
	
	private static final DecimalFormat DECIMAL_KG_SOL = new DecimalFormat("#,##0.0 kg/Sol");

	// Caches
	private double algaeMass;
	private double idealAlgaeMass; 
	private double maxAlgaeMass;
	private double foodMass;
	private double foodDemand;
	private double powerReq;
	
	/** The cache value for the average water usage per sol per square meters. */
	private double waterUsageCache;
	/** The cache value for the average grey water usage per sol per square meters. */
	private double greyWaterUsageCache;
	/** The cache value for the average O2 generated per sol per square meters. */
	private double o2Cache;
	/** The cache value for the average CO2 consumed per sol per square meters. */
	private double co2Cache;
	/** The cache value for the work time done in this greenhouse. */
	private double workTimeCache;
	
	private JLabel algaeMassLabel;
	private JLabel idealAlgaeMassLabel;
	private JLabel maxAlgaeMassLabel;
	
	private JLabel foodMassLabel;
	private JLabel foodDemandLabel;

	private JLabel powerReqLabel;

	private JLabel waterLabel;
	private JLabel greyWaterLabel;
	private JLabel o2Label;
	private JLabel co2Label;
	private JLabel workTimeLabel;
	
	private AlgaeFarming pond;

	
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
	}
	
	/**
	 * Build the UI
	 */
	@Override
	protected void buildUI(JPanel center) {
		AttributePanel labelPanel = new AttributePanel(12);
		center.add(labelPanel, BorderLayout.NORTH);
		
		labelPanel.addTextField(Msg.getString("BuildingPanelAlgae.tankSize"), Integer.toString(pond.getTankSize()), null);
		
		algaeMass = pond.getCurrentAlgae();
		algaeMassLabel = labelPanel.addTextField(Msg.getString("BuildingPanelAlgae.algaeMass"),
				StyleManager.DECIMAL_KG2.format(algaeMass), null);
				
		idealAlgaeMass = pond.getIdealAlgae();
		idealAlgaeMassLabel = labelPanel.addTextField(Msg.getString("BuildingPanelAlgae.idealAlgaeMass"),
				StyleManager.DECIMAL_KG2.format(idealAlgaeMass), null);
		
		maxAlgaeMass = pond.getMaxAlgae();
		maxAlgaeMassLabel = labelPanel.addTextField(Msg.getString("BuildingPanelAlgae.maxAlgaeMass"),
				StyleManager.DECIMAL_KG2.format(maxAlgaeMass), null);

		foodMass = pond.getFoodMass();	
		foodMassLabel = labelPanel.addTextField(Msg.getString("BuildingPanelAlgae.foodMass"),
								 StyleManager.DECIMAL_KG2.format(foodMass), null);
		
		foodDemand = pond.getFoodDemand();	
		foodDemandLabel = labelPanel.addTextField(Msg.getString("BuildingPanelAlgae.foodDemand"),
								 StyleManager.DECIMAL_PLACES2.format(foodDemand), null);
		
		powerReq = pond.getFullPowerRequired();	
		powerReqLabel = labelPanel.addTextField(Msg.getString("BuildingPanelAlgae.powerReq"),
								 StyleManager.DECIMAL_KW.format(powerReq), null);
		
		waterUsageCache = pond.computeUsage(ResourceUtil.waterID);
		waterLabel = labelPanel.addTextField(Msg.getString("BuildingPanelFarming.waterUsage.title"),
									DECIMAL_KG_SOL.format(waterUsageCache),
									Msg.getString("BuildingPanelFarming.waterUsage.tooltip"));

		greyWaterUsageCache = pond.computeUsage(ResourceUtil.greyWaterID);
		greyWaterLabel = labelPanel.addTextField(Msg.getString("BuildingPanelFarming.greyWaterUsage.title"),
									DECIMAL_KG_SOL.format(greyWaterUsageCache),
									Msg.getString("BuildingPanelFarming.greyWaterUsage.tooltip"));
		
		o2Cache = pond.computeUsage(ResourceUtil.oxygenID);
		o2Label = labelPanel.addTextField(Msg.getString("BuildingPanelFarming.o2.title"),
									DECIMAL_KG_SOL.format(o2Cache),
									Msg.getString("BuildingPanelFarming.o2.tooltip"));

		co2Cache = pond.computeUsage(ResourceUtil.co2ID);
		co2Label = labelPanel.addTextField(Msg.getString("BuildingPanelFarming.co2.title"),
									DECIMAL_KG_SOL.format(co2Cache),
								 	Msg.getString("BuildingPanelFarming.co2.tooltip"));

		// Update the cumulative work time
		workTimeCache = pond.getCumulativeWorkTime()/1000.0;
		workTimeLabel = labelPanel.addTextField(Msg.getString("BuildingPanelFarming.workTime.title"),
									StyleManager.DECIMAL_SOLS.format(workTimeCache),
									Msg.getString("BuildingPanelFarming.workTime.tooltip"));
	}

	/**
	 * Updates this panel with latest values.
	 */
	@Override
	public void update() {	

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

		double newFoodMass = pond.getFoodMass();
		if (foodMass != newFoodMass) {
			foodMass = newFoodMass;
			foodMassLabel.setText(StyleManager.DECIMAL_KG2.format(newFoodMass));
		}
		
		double newFoodDemand = pond.getFoodDemand();
		if (foodDemand != newFoodDemand) {
			foodDemand = newFoodDemand;
			foodDemandLabel.setText(StyleManager.DECIMAL_PLACES1.format(newFoodDemand));
		}
		
		double newPowerReq = pond.getFullPowerRequired();	
		if (powerReq != newPowerReq) {
			powerReq = newPowerReq;
			powerReqLabel.setText(StyleManager.DECIMAL_KW.format(newPowerReq));
		}
		
		// Update the average water usage
		double newWater = pond.computeUsage(ResourceUtil.waterID);
		if (waterUsageCache != newWater) {
			waterUsageCache = newWater;
			waterLabel.setText(DECIMAL_KG_SOL.format(newWater));
		}

		// Update the average O2 generated
		double newO2 = pond.computeUsage(ResourceUtil.oxygenID);
		if (o2Cache != newO2) {
			o2Cache = newO2;
			o2Label.setText(DECIMAL_KG_SOL.format(newO2));
		}

		// Update the average CO2 consumed
		double newCo2 = pond.computeUsage(ResourceUtil.co2ID);
		if (co2Cache != newCo2) {
			co2Cache = newCo2;
			co2Label.setText(DECIMAL_KG_SOL.format(newCo2));
		}

		// Update the average grey water usage
		double newGreyWater = pond.computeUsage(ResourceUtil.greyWaterID);
		if (greyWaterUsageCache != newGreyWater) {
			greyWaterUsageCache = newGreyWater;
			greyWaterLabel.setText(DECIMAL_KG_SOL.format(newGreyWater));
		}
		
		// Update the cumulative work time
		double workTime = pond.getCumulativeWorkTime()/1000.0;
		if (workTimeCache != workTime) {
			workTimeCache = workTime;
			workTimeLabel.setText(StyleManager.DECIMAL_SOLS.format(workTime));
		}
	}
}