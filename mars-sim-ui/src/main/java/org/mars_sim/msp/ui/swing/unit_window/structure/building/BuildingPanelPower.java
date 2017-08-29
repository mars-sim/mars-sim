/**
 * Mars Simulation Project
 * BuildingPanelPower.java
 * @version 3.07 2014-12-06

 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.text.DecimalFormat;

import javax.swing.JLabel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.PowerGeneration;
import org.mars_sim.msp.core.structure.building.function.PowerMode;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

/**
 * The BuildingPanelPower class is a building function panel representing 
 * the power production and use of a settlement building.
 */
public class BuildingPanelPower
extends BuildingFunctionPanel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Is the building a power producer? */
	private boolean isProducer;
	/** The power status label. */
	private JLabel powerStatusLabel;
	/** The power production label. */
	private JLabel powerLabel;
	/** The power used label. */
	private JLabel usedLabel;
	/** Decimal formatter. */
	private DecimalFormat formatter = new DecimalFormat(Msg.getString("BuildingPanelPower.decimalFormat")); //$NON-NLS-1$

	// Caches
	/** The power status cache. */
	private PowerMode powerStatusCache;
	/** The power production cache. */
	private double powerCache;
	/** The power used cache. */
	private double usedCache;

	/**
	 * Constructor.
	 * @param building the building the panel is for.
	 * @param desktop The main desktop.
	 */
	public BuildingPanelPower(Building building, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor
		super(building, desktop);

		// Check if the building is a power producer.
		isProducer = building.hasFunction(BuildingFunction.POWER_GENERATION);

		// Set the layout
		if (isProducer) setLayout(new GridLayout(4, 1, 0, 0));
		else setLayout(new GridLayout(3, 1, 0, 0));

		// 2014-11-21 Changed font type, size and color and label text
		JLabel titleLabel = new JLabel(
				Msg.getString("BuildingPanelPower.title"), //$NON-NLS-1$
				JLabel.CENTER);		
		titleLabel.setFont(new Font("Serif", Font.BOLD, 16));
		//titleLabel.setForeground(new Color(102, 51, 0)); // dark brown
		add(titleLabel);
		
		
		// Prepare power status label.
		powerStatusCache = building.getPowerMode();
		powerStatusLabel = new JLabel(
				Msg.getString("BuildingPanelPower.powerStatus", powerStatusCache.getName()), //$NON-NLS-1$
				JLabel.CENTER
			);
		add(powerStatusLabel);

		// If power producer, prepare power producer label.
		if (isProducer) {
			PowerGeneration generator = (PowerGeneration) building.getFunction(BuildingFunction.POWER_GENERATION);
			powerCache = generator.getGeneratedPower();
			powerLabel = new JLabel(
				Msg.getString("BuildingPanelPower.powerProduced", formatter.format(powerCache)), //$NON-NLS-1$
				JLabel.CENTER
			);
			add(powerLabel);
		}

		// Prepare power used label.
		if (powerStatusCache == PowerMode.FULL_POWER) 
			usedCache = building.getFullPowerRequired();
		else if (powerStatusCache == PowerMode.POWER_DOWN) 
			usedCache = building.getPoweredDownPowerRequired();
		else usedCache = 0D;
		usedLabel = new JLabel(
			Msg.getString("BuildingPanelPower.powerUsed", formatter.format(usedCache)), //$NON-NLS-1$
			JLabel.CENTER
		);
		add(usedLabel);
	}

	/**
	 * Update this panel
	 */
	public void update() {

		// Update power status if necessary.
		PowerMode mode = building.getPowerMode();
		if (powerStatusCache != mode) {
			powerStatusCache = mode;
			powerStatusLabel.setText(Msg.getString("BuildingPanelPower.powerStatus", powerStatusCache.getName())); //$NON-NLS-1$
		}

		// Update power production if necessary.
		if (isProducer) {
			PowerGeneration generator = building.getPowerGeneration();//(PowerGeneration) building.getFunction(BuildingFunction.POWER_GENERATION);
			double power = generator.getGeneratedPower();
			if (powerCache != power) {
				powerCache = power;
				powerLabel.setText(Msg.getString("BuildingPanelPower.powerProduced", formatter.format(powerCache))); //$NON-NLS-1$
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
			usedLabel.setText(Msg.getString("BuildingPanelPower.powerUsed", formatter.format(usedCache))); //$NON-NLS-1$
		}
	}
}