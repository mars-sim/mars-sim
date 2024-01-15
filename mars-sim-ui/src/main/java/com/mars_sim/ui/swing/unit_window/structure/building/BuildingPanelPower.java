/*
 * Mars Simulation Project
 * BuildingPanelPower.java
 * @date 2023-06-18
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.util.Iterator;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.function.FissionPowerSource;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.structure.building.function.PowerGeneration;
import com.mars_sim.core.structure.building.function.PowerMode;
import com.mars_sim.core.structure.building.function.PowerSource;
import com.mars_sim.core.structure.building.function.PowerSourceType;
import com.mars_sim.tools.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.utils.AttributePanel;

/**
 * The BuildingPanelPower class is a building function panel representing 
 * the power production and use of a settlement building.
 */
@SuppressWarnings("serial")
public class BuildingPanelPower
extends BuildingFunctionPanel {

	private static final String POWER_ICON = "power";
		
	/** Is the building a power producer? */
	private boolean isProducer;
	
	/** The power production cache. */
	private double powerCache;
	/** The power used cache. */
	private double usedCache;
	/** The max power cache. */
	private double maxPowerCache;
	
	private JLabel modeLabel;
	private JLabel maxPowerLabel;
	private JLabel producedLabel;
	private JLabel usedLabel;

	private JLabel loadCapacityLabel;
	
	/** The power status cache. */
	private PowerMode powerStatusCache;

	private PowerGeneration generator;

	/**
	 * Constructor.
	 * 
	 * @param building the building the panel is for.
	 * @param desktop The main desktop.
	 */
	public BuildingPanelPower(Building building, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor
		super(
			Msg.getString("BuildingPanelPower.title"), 
			ImageLoader.getIconByName(POWER_ICON),
			building, 
			desktop
		);

		// Check if the building is a power producer.
		isProducer = building.hasFunction(FunctionType.POWER_GENERATION);
		generator = building.getPowerGeneration();
	}

	/**
	 * Builds the UI elements.
	 */
	@Override
	protected void buildUI(JPanel center) {
		
		AttributePanel springPanel = new AttributePanel(isProducer ? 5 : 3);
		center.add(springPanel, BorderLayout.NORTH);
		
		// Prepare power status label.
		powerStatusCache = building.getPowerMode();
		modeLabel = springPanel.addTextField(Msg.getString("BuildingPanelPower.powerStatus"),
				                powerStatusCache.getName(), null);
		
		// If power producer, prepare power producer label.
		if (isProducer) {
			powerCache = generator.getGeneratedPower();
			producedLabel = springPanel.addTextField(Msg.getString("BuildingPanelPower.powerProduced"),
									  StyleManager.DECIMAL_KW.format(powerCache), null);
			Iterator<PowerSource> iP = generator.getPowerSources().iterator();
			while (iP.hasNext()) {
				PowerSource powerSource = iP.next();

				maxPowerCache = powerSource.getMaxPower();
				maxPowerLabel = springPanel.addTextField(Msg.getString("BuildingPanelPower.maxPower"),
						StyleManager.DECIMAL_KW.format(maxPowerCache), null);	
				
				if (powerSource.getType() == PowerSourceType.FISSION_POWER
						|| powerSource.getType() == PowerSourceType.THERMIONIC_NUCLEAR_POWER) {
							
					double loadCapacity = ((FissionPowerSource)powerSource).getCurrentLoadCapacity();
					loadCapacityLabel = springPanel.addTextField(Msg.getString("BuildingPanelPower.loadCapacity"),
							Math.round(loadCapacity *10.0)/10.0 + " %", null);
					break;
				}
			}
		}

		modeLabel = springPanel.addTextField(Msg.getString("BuildingPanelPower.powerStatus"),
                powerStatusCache.getName(), null);
		
		// Prepare power used label.
		if (powerStatusCache == PowerMode.FULL_POWER) 
			usedCache = building.getFullPowerRequired();
		else if (powerStatusCache == PowerMode.POWER_DOWN) 
			usedCache = building.getPoweredDownPowerRequired();
		else usedCache = 0D;
		usedLabel = springPanel.addTextField(Msg.getString("BuildingPanelPower.powerUsed"),
										StyleManager.DECIMAL_KW.format(usedCache), null);
	}

	/**
	 * Updates this panel.
	 */
	@Override
	public void update() {

		// Update power status if necessary.
		PowerMode mode = building.getPowerMode();
		if (powerStatusCache != mode) {
			powerStatusCache = mode;
			modeLabel.setText(mode.getName()); //$NON-NLS-1$
		}

		// Update power production if necessary.
		if (isProducer) {
			double power = generator.getGeneratedPower();
			if (powerCache != power) {
				powerCache = power;
				producedLabel.setText(StyleManager.DECIMAL_KW.format(power)); //$NON-NLS-1$
			}
			
			Iterator<PowerSource> iP = generator.getPowerSources().iterator();
			while (iP.hasNext()) {
				PowerSource powerSource = iP.next();

				if (powerSource.getType() == PowerSourceType.FISSION_POWER
						|| powerSource.getType() == PowerSourceType.THERMIONIC_NUCLEAR_POWER) {
					
					double loadCapacity = ((FissionPowerSource)powerSource).getCurrentLoadCapacity();
					loadCapacityLabel.setText(Math.round(loadCapacity *10.0)/10.0 + " %");
				}
			}
		}

		// Update power used if necessary.
		double usedPower = 0D;
		if (powerStatusCache == PowerMode.FULL_POWER) 
			usedPower = building.getFullPowerRequired();
		else if (powerStatusCache == PowerMode.POWER_DOWN) 
			usedPower = building.getPoweredDownPowerRequired();
		
		if (usedCache != usedPower) {
			usedCache = usedPower;
			usedLabel.setText(StyleManager.DECIMAL_KW.format(usedPower)); //$NON-NLS-1$
		}
	}
	
	/**
	 * Prepares object for garbage collection.
	 */
	@Override
	public void destroy() {
		super.destroy();
		modeLabel = null;
		producedLabel = null;
		usedLabel = null;
		powerStatusCache = null;
		generator = null;
		loadCapacityLabel = null;
	}
}
