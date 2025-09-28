/*
 * Mars Simulation Project
 * BuildingPanelThermal.java
 * @date 2025-09-28
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.mars_sim.core.building.utility.heating.FuelHeatSource;
import com.mars_sim.core.building.utility.heating.HeatMode;
import com.mars_sim.core.building.utility.heating.HeatSource;
import com.mars_sim.core.building.utility.heating.ThermalGeneration;
import com.mars_sim.core.tool.Msg;
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
	
	private static final String HEAT_TYPE = Msg.getString("BuildingPanelThermal.heatsource.heatType"); //$NON-NLS-1$
	private static final String MAX_HEAT = Msg.getString("BuildingPanelThermal.heatsource.maxHeat"); //$NON-NLS-1$
	private static final String HEAT_GEN = Msg.getString("BuildingPanelThermal.heatsource.heatGen"); //$NON-NLS-1$
	private static final String HEAT_STATUS = Msg.getString("BuildingPanelThermal.heatsource.heatStatus"); //$NON-NLS-1$
	
	private static final String TOTAL_HEAT_PRODUCED = Msg.getString("BuildingPanelThermal.totalHeatProduced"); //$NON-NLS-1$
	private static final String TOTAL_HEAT_CAP = Msg.getString("BuildingPanelThermal.totalHeatCap"); //$NON-NLS-1$

	/** Is UI constructed. */
	private boolean uiDone = false;


	// Caches
	/** The heat production cache. */
	private double totalHeatproducedCache;
	/** The air heat sink cache. */
	private double airHeatSinkCache;
	/** The water heat sink cache. */
	private double waterHeatSinkCache;
	

	
	/** The total heat production label. */
	private JLabel totalHeatProducedLabel;
	
	/** The air heat sink label. */
	private JLabel airHeatSinkLabel;
	/** The water heat sink label. */
	private JLabel waterHeatSinkLabel;
	
	private List<HeatSource> sources = null;

	/** The heat status label. */
	private JLabel[] heatStatusLabels;
	
	private JLabel[] maxHeatLabels;
	
	private JLabel[] heatGenLabels;
	
	/** The ThermalGeneration instance. */
	private ThermalGeneration furnace;
	
	/**
	 * Constructor.
	 * 
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
		
		this.sources = furnace.getHeatSources();

	}
	
	/**
	 * Builds the UI.
	 */
	@Override
	protected void buildUI(JPanel center) {
	
		// Prepare spring layout info panel.
		AttributePanel infoPanel = new AttributePanel();
		center.add(infoPanel, BorderLayout.NORTH);
		
		// If heat producer, prepare labels.
		if (furnace != null) {
			
			airHeatSinkCache = furnace.getHeating().getAirHeatSink();		
			airHeatSinkLabel = infoPanel.addTextField(Msg.getString("BuildingPanelThermal.airHeatSink"),
					  StyleManager.DECIMAL_KW.format(airHeatSinkCache), "The air heat sink of this building");
	
			waterHeatSinkCache = furnace.getHeating().getWaterHeatSink();		
			waterHeatSinkLabel = infoPanel.addTextField(Msg.getString("BuildingPanelThermal.waterHeatSink"),
					  StyleManager.DECIMAL_KW.format(waterHeatSinkCache), "The water heat sink of this building");


			totalHeatproducedCache = furnace.getGeneratedHeat();		
			totalHeatProducedLabel = infoPanel.addTextField(TOTAL_HEAT_PRODUCED,
									  StyleManager.DECIMAL_KW.format(totalHeatproducedCache), 
									  "The total heat production of this building");
			
			double totalHeatCapCache = furnace.getHeatGenerationCapacity();		
			infoPanel.addTextField(TOTAL_HEAT_CAP,
									  StyleManager.DECIMAL_KW.format(totalHeatCapCache), 
									  "The total heat capacity of this building");
			
			int num = sources.size();
			AttributePanel sPanel = new AttributePanel(num * 4);
			sPanel.setBorder(StyleManager.createLabelBorder("Heat Sources"));

			var centerPanel = new JPanel(new BorderLayout());
			center.add(centerPanel, BorderLayout.CENTER);
			centerPanel.add(sPanel, BorderLayout.NORTH);

			heatStatusLabels = new JLabel[num];
			maxHeatLabels = new JLabel[num];
			heatGenLabels = new JLabel[num];
			
			int count = 0;
			for (var source : sources) {

				sPanel.addRow(HEAT_TYPE + " " + (count + 1), source.getType().getName());
				
				// Prepare heat status label.
				HeatMode heatStatus = source.getHeatMode();
				heatStatusLabels[count] = sPanel.addRow(HEAT_STATUS, heatStatus.getName(), "The status of the heating system");
			
				maxHeatLabels[count] = sPanel.addRow(MAX_HEAT, "The max heating capacity this heat source");
				heatGenLabels[count] = sPanel.addRow(HEAT_GEN, "The heat produced by this heat source");
				
				count++;
			}
		}
	}

	/**
	 * Updates this panel with latest Heat Mode status and amount of heat produced.
	 */
	@Override
	public void update() {		
		if (!uiDone)
			initializeUI();

		// Update power production if necessary.
		if (furnace != null) {
			
			double newProductionCache = furnace.getGeneratedHeat();
			if (totalHeatproducedCache != newProductionCache) {
				totalHeatproducedCache = newProductionCache;
				totalHeatProducedLabel.setText(StyleManager.DECIMAL_KW.format(totalHeatproducedCache));
			}
	
			double newAirHeatSink = furnace.getHeating().getAirHeatSink();	
			if (airHeatSinkCache != newAirHeatSink) {
				airHeatSinkCache = newAirHeatSink;
				airHeatSinkLabel.setText(StyleManager.DECIMAL_KW.format(newAirHeatSink));
			}
			
			double newWaterHeatSink = furnace.getHeating().getWaterHeatSink();	
			if (waterHeatSinkCache != newWaterHeatSink) {
				waterHeatSinkCache = newWaterHeatSink;
				waterHeatSinkLabel.setText(StyleManager.DECIMAL_KW.format(newWaterHeatSink));
			}

			double totalProduced = furnace.getGeneratedHeat();
			if (totalHeatproducedCache != totalProduced) {
				totalHeatproducedCache = totalProduced;
				totalHeatProducedLabel.setText(StyleManager.DECIMAL_KW.format(totalProduced));
			}
			
			int count = 0;
			for (var source : sources) {

				double maxPower = source.getMaxHeat();
				maxHeatLabels[count].setText(StyleManager.DECIMAL_KW.format(maxPower));
				
				double heatGen = 0;
				if (source instanceof FuelHeatSource fuel) {
					if (fuel.isToggleON())
						heatGen = fuel.measureHeat(100);
				}
				else {
					heatGen = source.getCurrentHeat();
				}
				
				heatGenLabels[count].setText(StyleManager.DECIMAL_KW.format(heatGen));
				
				heatStatusLabels[count].setText(source.getHeatMode().getName());
				
				count++;
			}
		}
	}
}
