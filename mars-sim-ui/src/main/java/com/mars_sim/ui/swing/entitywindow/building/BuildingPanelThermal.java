/*
 * Mars Simulation Project
 * BuildingPanelThermal.java
 * @date 2025-10-07
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.entitywindow.building;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.utility.heating.FuelHeatSource;
import com.mars_sim.core.building.utility.heating.HeatMode;
import com.mars_sim.core.building.utility.heating.HeatSource;
import com.mars_sim.core.building.utility.heating.ThermalGeneration;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.SwingHelper;

/**
 * The BuildingPanelThermal class is a building function panel representing 
 * the heat production of a settlement building.
 */
@SuppressWarnings("serial")
class BuildingPanelThermal
extends EntityTabPanel<Building> implements TemporalComponent {

	private static final String HEAT_ICON = "heat";
	
	private static final String HEAT_TYPE = Msg.getString("BuildingPanelThermal.heatsource.heatType"); //-NLS-1$
	private static final String MAX_HEAT = Msg.getString("BuildingPanelThermal.heatsource.maxHeat"); //-NLS-1$
	private static final String HEAT_GEN = Msg.getString("BuildingPanelThermal.heatsource.heatGen"); //-NLS-1$
	private static final String HEAT_STATUS = Msg.getString("BuildingPanelThermal.heatsource.heatStatus"); //-NLS-1$
	
	private static final String TOTAL_HEAT_PRODUCED = Msg.getString("BuildingPanelThermal.totalHeatProduced"); //-NLS-1$
	private static final String TOTAL_HEAT_CAP = Msg.getString("BuildingPanelThermal.totalHeatCap"); //-NLS-1$

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
	
	/**
	 * Collates all the details of a HeatSource into one record
	 */
	private record HeatSourceStatus(HeatSource source, JLabel maxHeatLabel, JLabel heatGenLabel, JLabel heatStatusLabel) {
		void refresh(){
			double maxPower = source.getMaxHeat();
			maxHeatLabel.setText(StyleManager.DECIMAL_KW.format(maxPower));
			
			double heatGen = 0;
			if (source instanceof FuelHeatSource fuel) {
				if (fuel.isToggleON())
					heatGen = fuel.measureHeat(100);
			}
			else {
				heatGen = source.getCurrentHeat();
			}
			
			heatGenLabel.setText(StyleManager.DECIMAL_KW.format(heatGen));
			heatStatusLabel.setText(source.getHeatMode().getName());				
		}
	}
	private List<HeatSourceStatus> sources = new ArrayList<>();
	
	/** The ThermalGeneration instance. */
	private ThermalGeneration furnace;
	
	/**
	 * Constructor.
	 * 
	 * @param furnace The thermal generation building function.
	 * @param context The UI context.
	 */
	public BuildingPanelThermal(ThermalGeneration furnace, UIContext context) {
		super(
			Msg.getString("BuildingPanelThermal.title"),
			ImageLoader.getIconByName(HEAT_ICON), null,
			context, furnace.getBuilding()
		);

		this.furnace = furnace;
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
			
			// Create a vertical box for heat sources.
			var sourcesPanel = Box.createVerticalBox();
			for(var s : furnace.getHeatSources()) {
				sourcesPanel.add(createSourcePanel(s));
			}

			// Add some space.
			var spacePanel = new JPanel(new BorderLayout());
			spacePanel.add(sourcesPanel, BorderLayout.NORTH);
			center.add(spacePanel,  BorderLayout.CENTER);
		}
	}

	/**
	 * Creates a heat source panel.
	 * 
	 * @param source The heat source.
	 * @return The heat source panel.
	 */
	private JPanel createSourcePanel(HeatSource source) {
		int sourceId = sources.size() + 1;
		var sPanel = new AttributePanel();
		sPanel.setBorder(SwingHelper.createLabelBorder("Heat Source " + sourceId));

		sPanel.addRow(HEAT_TYPE, source.getType().getName());
				
		HeatMode heatStatus = source.getHeatMode();
		var heatStatusLabel = sPanel.addRow(HEAT_STATUS, heatStatus.getName(), "The status of the heating system");
		var maxHeatLabel = sPanel.addRow(MAX_HEAT, "", "The max heating capacity this heat source");
		var heatGenLabel = sPanel.addRow(HEAT_GEN, "", "The heat produced by this heat source");
		sources.add(new HeatSourceStatus(source, maxHeatLabel, heatGenLabel, heatStatusLabel));
		return sPanel;
	}

	@Override
	public void clockUpdate(ClockPulse pulse) {
		// Update power production if necessary.
		if (furnace != null) {
			
			double newProductionCache = furnace.getGeneratedHeat();
			if (totalHeatproducedCache != newProductionCache) {
				totalHeatproducedCache = newProductionCache;
				totalHeatProducedLabel.setText(StyleManager.DECIMAL_KW.format(newProductionCache));
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
			
			// Update each heat source status.
			sources.forEach(HeatSourceStatus::refresh);
		}
	}
}
