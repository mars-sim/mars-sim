/**
 * Mars Simulation Project
 * BuildingPanelThermal.java
 * @version 3.07 2014-10-17
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.GridLayout;
import java.text.DecimalFormat;

import javax.swing.JLabel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.ThermalGeneration;
import org.mars_sim.msp.core.structure.building.function.HeatMode;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

/**
 * The BuildingPanelThermal class is a building function panel representing 
 * the heat production and use of a settlement building.
 */
public class BuildingPanelThermal
extends BuildingFunctionPanel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Is the building a heat producer? */
	private boolean isProducer;
	/** The heat status label. */
	private JLabel heatStatusLabel;
	/** The heat production label. */
	private JLabel productionLabel;
	/** The heat used label. */
	private JLabel usedLabel;
	/** Decimal formatter. */
	private DecimalFormat formatter = new DecimalFormat(Msg.getString("BuildingPanelThermal.decimalFormat")); //$NON-NLS-1$

	// Caches
	/** The heat status cache. */
	private HeatMode heatStatusCache;
	/** The heat production cache. */
	private double productionCache;
	/** The heat used cache. */
	private double usedCache;

	/**
	 * Constructor.
	 * @param The panel for the Heating System
	 * @param The main desktop
	 */
	public BuildingPanelThermal(Building building, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor
		super(building, desktop);

		// Check if the building is a heat producer.
		isProducer = building.hasFunction(BuildingFunction.THERMAL_GENERATION);

		// Set the layout
		if (isProducer) setLayout(new GridLayout(3, 1, 0, 0));
		else setLayout(new GridLayout(2, 1, 0, 0));

		// Prepare heat status label.
		heatStatusCache = building.getHeatMode();
		heatStatusLabel = new JLabel(
			Msg.getString("BuildingPanelThermal.heatStatus", heatStatusCache.getName()), //$NON-NLS-1$
			JLabel.CENTER
		);
		add(heatStatusLabel);

		// If heat producer, prepare heat producer label.
		if (isProducer) {
			ThermalGeneration generator = (ThermalGeneration) building.getFunction(BuildingFunction.THERMAL_GENERATION);
			productionCache = generator.getGeneratedHeat();
			productionLabel = new JLabel(
				Msg.getString("BuildingPanelThermal.heatProduced", formatter.format(productionCache)), //$NON-NLS-1$
				JLabel.CENTER
			);
			add(productionLabel);
		}

		// Prepare heat used label.
		if (heatStatusCache == HeatMode.FULL_POWER) 
			usedCache = building.getFullPowerRequired();
		else if (heatStatusCache == HeatMode.POWER_DOWN) 
			usedCache = building.getPoweredDownPowerRequired();
		else usedCache = 0D;
		usedLabel = new JLabel(
			Msg.getString("BuildingPanelThermal.heatUsed", formatter.format(usedCache)), //$NON-NLS-1$
			JLabel.CENTER
		);
		add(usedLabel);
	}

	/**
	 * Update this panel
	 */
	public void update() {

		// Update heat status if necessary.
		if (!heatStatusCache.equals(building.getPowerMode())) {
			heatStatusCache = building.getHeatMode();
			heatStatusLabel.setText(Msg.getString("BuildingPanelThermal.heatStatus", heatStatusCache.getName())); //$NON-NLS-1$
		}

		// Update heat production if necessary.
		if (isProducer) {
			ThermalGeneration generator = (ThermalGeneration) building.getFunction(BuildingFunction.THERMAL_GENERATION);
			if (productionCache != generator.getGeneratedHeat()) {
				productionCache = generator.getGeneratedHeat();
				productionLabel.setText(Msg.getString("BuildingPanelThermal.heatProduced", formatter.format(productionCache))); //$NON-NLS-1$
			}
		}

		// Update heat used if necessary.
		double usedPower = 0D;
		if (heatStatusCache == HeatMode.FULL_POWER) 
			usedPower = building.getFullPowerRequired();
		else if (heatStatusCache == HeatMode.POWER_DOWN) 
			usedPower = building.getPoweredDownPowerRequired();
		if (usedCache != usedPower) {
			usedCache = usedPower;
			usedLabel.setText(Msg.getString("BuildingPanelThermal.heatUsed", formatter.format(usedCache))); //$NON-NLS-1$
		}
	}
}