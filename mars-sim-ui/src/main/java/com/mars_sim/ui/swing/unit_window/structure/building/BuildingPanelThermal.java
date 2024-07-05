/*
 * Mars Simulation Project
 * BuildingPanelThermal.java
 * @date 2022-07-10
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.mars_sim.core.structure.building.utility.heating.HeatMode;
import com.mars_sim.core.structure.building.utility.heating.ThermalGeneration;
import com.mars_sim.tools.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.utils.AttributePanel;

/**
 * The BuildingPanelThermal class is a building function panel representing 
 * the heat production of a settlement building.
 */
@SuppressWarnings("serial")
public class BuildingPanelThermal
extends BuildingFunctionPanel {

	private static final String HEAT_ICON = "heat";

	/** The heat status textfield. */
	private JLabel statusTF;
	/** The heat production textfield. */
	private JLabel producedTF;
	/** The air heat sink label. */
	private JLabel airHeatSinkLabel;
	/** The water heat sink label. */
	private JLabel waterHeatSinkLabel;
	
	// Caches
	/** The heat production cache. */
	private double productionCache;
	/** The air heat sink cache. */
	private double airHeatSinkCache;
	/** The water heat sink cache. */
	private double waterHeatSinkCache;
	
	
	/** The heat status cache. */
	private HeatMode heatStatusCache;
	/** The ThermalGeneration instance. */
	private ThermalGeneration furnace;
	
	/**
	 * Constructor.
	 * @param The panel for the Heating System
	 * @param The main desktop
	 */
	public BuildingPanelThermal(ThermalGeneration furnace, MainDesktopPane desktop) {
		super(
			Msg.getString("BuildingPanelThermal.title"),
			ImageLoader.getIconByName(HEAT_ICON), 
			furnace.getBuilding(), 
			desktop
		);

		this.furnace = furnace;
		this.building = furnace.getBuilding();
	}
	
	/**
	 * Builds the UI.
	 */
	@Override
	protected void buildUI(JPanel center) {
	
		// Prepare spring layout info panel.
		AttributePanel infoPanel = new AttributePanel(4);
		center.add(infoPanel, BorderLayout.NORTH);
		
		// Prepare heat status label.
//		heatStatusCache = building.getHeatMode();
//		statusTF = infoPanel.addTextField(Msg.getString("BuildingPanelThermal.heatStatus"),
//								heatStatusCache.getName(), "The status of the heating system");
		
		productionCache = furnace.getGeneratedHeat();		
		producedTF = infoPanel.addTextField(Msg.getString("BuildingPanelThermal.heatProduced"),
								  StyleManager.DECIMAL_KW.format(productionCache), 
								  "The heat production of this building");
	
		airHeatSinkCache = building.getThermalGeneration().getHeating().getAirHeatSink();		
		airHeatSinkLabel = infoPanel.addTextField(Msg.getString("BuildingPanelThermal.airHeatSink"),
				  StyleManager.DECIMAL_KW.format(airHeatSinkCache), "The air heat sink of this building");

		waterHeatSinkCache = building.getThermalGeneration().getHeating().getWaterHeatSink();		
		waterHeatSinkLabel = infoPanel.addTextField(Msg.getString("BuildingPanelThermal.waterHeatSink"),
				  StyleManager.DECIMAL_KW.format(waterHeatSinkCache), "The water heat sink of this building");
	}

	/**
	 * Updates this panel with latest Heat Mode status and amount of heat produced.
	 */
	@Override
	public void update() {	
		// Update heat status if necessary.
//		if (!heatStatusCache.equals(building.getHeatMode())) {
//			heatStatusCache = building.getHeatMode();			
//			statusTF.setText(heatStatusCache.getName());
//		}

		double newProductionCache = furnace.getGeneratedHeat();
		if (productionCache != newProductionCache) {
			productionCache = newProductionCache;
			producedTF.setText(StyleManager.DECIMAL_KW.format(productionCache));
		}
		
		
		double newAirHeatSink = building.getThermalGeneration().getHeating().getAirHeatSink();	
		if (airHeatSinkCache != newAirHeatSink) {
			airHeatSinkCache = newAirHeatSink;
			airHeatSinkLabel.setText(StyleManager.DECIMAL_KW.format(newAirHeatSink));
		}
		
		double newWaterHeatSink = building.getThermalGeneration().getHeating().getWaterHeatSink();	
		if (waterHeatSinkCache != newWaterHeatSink) {
			waterHeatSinkCache = newWaterHeatSink;
			waterHeatSinkLabel.setText(StyleManager.DECIMAL_KW.format(newWaterHeatSink));
		}
	}
}
