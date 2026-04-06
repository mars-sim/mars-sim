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
import com.mars_sim.ui.swing.components.JDoubleLabel;
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
	
	private JDoubleLabel totalHeatProducedLabel;
	private JDoubleLabel airHeatSinkLabel;
	private JDoubleLabel waterHeatSinkLabel;
	
	/**
	 * Collates all the details of a HeatSource into one record
	 */
	private record HeatSourceStatus(HeatSource source, JDoubleLabel maxHeatLabel, JDoubleLabel heatGenLabel, JLabel heatStatusLabel) {
		void refresh(){
			maxHeatLabel.setValue(source.getMaxHeat());
			
			double heatGen = 0;
			if (source instanceof FuelHeatSource fuel) {
				if (fuel.isToggleON())
					heatGen = fuel.measureHeat(100);
			}
			else {
				heatGen = source.getCurrentHeat();
			}
			
			heatGenLabel.setValue(heatGen);
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
			
			var airHeatSinkCache = furnace.getHeating().getAirHeatSink();
			airHeatSinkLabel = new JDoubleLabel(StyleManager.DECIMAL_KW, airHeatSinkCache);
			infoPanel.addLabelledItem(Msg.getString("BuildingPanelThermal.airHeatSink"), airHeatSinkLabel,
								"The air heat sink of this building");
	
			var waterHeatSinkCache = furnace.getHeating().getWaterHeatSink();		
			waterHeatSinkLabel = new JDoubleLabel(StyleManager.DECIMAL_KW, waterHeatSinkCache);
			infoPanel.addLabelledItem(Msg.getString("BuildingPanelThermal.waterHeatSink"), waterHeatSinkLabel,
								"The water heat sink of this building");

			var totalHeatproducedCache = furnace.getGeneratedHeat();		
			totalHeatProducedLabel = new JDoubleLabel(StyleManager.DECIMAL_KW, totalHeatproducedCache);
			infoPanel.addLabelledItem(TOTAL_HEAT_PRODUCED, totalHeatProducedLabel,
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
		var heatStatusLabel = sPanel.addTextField(HEAT_STATUS, heatStatus.getName(), "The status of the heating system");

		
		var maxHeatLabel = new JDoubleLabel(StyleManager.DECIMAL_KW);
		sPanel.addLabelledItem(MAX_HEAT, maxHeatLabel, "The max heating capacity this heat source");
		var heatGenLabel = new JDoubleLabel(StyleManager.DECIMAL_KW);
		sPanel.addLabelledItem(HEAT_GEN, heatGenLabel, "The heat produced by this heat source");
		sources.add(new HeatSourceStatus(source, maxHeatLabel, heatGenLabel, heatStatusLabel));
		return sPanel;
	}

	@Override
	public void clockUpdate(ClockPulse pulse) {
		// Update power production if necessary.
		if (furnace != null) {
			
			double newProductionCache = furnace.getGeneratedHeat();
			totalHeatProducedLabel.setValue(newProductionCache);
	
			airHeatSinkLabel.setValue(furnace.getHeating().getAirHeatSink());			
			waterHeatSinkLabel.setValue(furnace.getHeating().getWaterHeatSink());
			totalHeatProducedLabel.setValue(furnace.getGeneratedHeat());
			
			// Update each heat source status.
			sources.forEach(HeatSourceStatus::refresh);
		}
	}
}
