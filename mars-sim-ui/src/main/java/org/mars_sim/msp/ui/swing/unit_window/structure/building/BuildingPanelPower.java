/*
 * Mars Simulation Project
 * BuildingPanelPower.java
 * @date 2022-07-10
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.util.Iterator;
import java.util.logging.Level;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FissionPowerSource;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.PowerGeneration;
import org.mars_sim.msp.core.structure.building.function.PowerMode;
import org.mars_sim.msp.core.structure.building.function.PowerSource;
import org.mars_sim.msp.core.structure.building.function.PowerSourceType;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.StyleManager;
import org.mars_sim.msp.ui.swing.utils.AttributePanel;

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
	
	private JLabel statusTF;
	private JLabel producedTF;
	private JLabel usedTF;

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
		
		AttributePanel springPanel = new AttributePanel(isProducer ? 4 : 2);
		center.add(springPanel, BorderLayout.NORTH);
		
		// Prepare power status label.
		powerStatusCache = building.getPowerMode();
		statusTF = springPanel.addTextField(Msg.getString("BuildingPanelPower.powerStatus"),
				                powerStatusCache.getName(), null);

		// If power producer, prepare power producer label.
		if (isProducer) {
			powerCache = generator.getGeneratedPower();
			producedTF = springPanel.addTextField(Msg.getString("BuildingPanelPower.powerProduced"),
									  StyleManager.DECIMAL_KW.format(powerCache), null);
			Iterator<PowerSource> iP = generator.getPowerSources().iterator();
			while (iP.hasNext()) {
				PowerSource powerSource = iP.next();
				double loadCapacity = ((FissionPowerSource)powerSource).getCurrentLoadCapacity();
				if (powerSource.getType() == PowerSourceType.FISSION_POWER
						|| powerSource.getType() == PowerSourceType.THERMIONIC_NUCLEAR_POWER) {
					loadCapacityLabel = springPanel.addTextField(Msg.getString("BuildingPanelPower.loadCapacity"),
							Math.round(loadCapacity *10.0)/10.0 + " %", null);
				}
			}
		}

		// Prepare power used label.
		if (powerStatusCache == PowerMode.FULL_POWER) 
			usedCache = building.getFullPowerRequired();
		else if (powerStatusCache == PowerMode.POWER_DOWN) 
			usedCache = building.getPoweredDownPowerRequired();
		else usedCache = 0D;
		usedTF = springPanel.addTextField(Msg.getString("BuildingPanelPower.powerUsed"),
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
			statusTF.setText(powerStatusCache.getName()); //$NON-NLS-1$
		}

		// Update power production if necessary.
		if (isProducer) {
			double power = generator.getGeneratedPower();
			if (powerCache != power) {
				powerCache = power;
				producedTF.setText(StyleManager.DECIMAL_KW.format(powerCache)); //$NON-NLS-1$
			}
			
			Iterator<PowerSource> iP = generator.getPowerSources().iterator();
			while (iP.hasNext()) {
				PowerSource powerSource = iP.next();
				double loadCapacity = ((FissionPowerSource)powerSource).getCurrentLoadCapacity();
				if (powerSource.getType() == PowerSourceType.FISSION_POWER
						|| powerSource.getType() == PowerSourceType.THERMIONIC_NUCLEAR_POWER) {
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
			usedTF.setText(StyleManager.DECIMAL_KW.format(usedCache)); //$NON-NLS-1$
		}
	}
	
	/**
	 * Prepares object for garbage collection.
	 */
	@Override
	public void destroy() {
		super.destroy();
		
		// take care to avoid null exceptions
		statusTF = null;
		producedTF = null;
		usedTF = null;
		powerStatusCache = null;
		generator = null;
	}
}
