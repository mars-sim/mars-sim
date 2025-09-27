/*
 * Mars Simulation Project
 * AreothermalPowerSource.java
 * @date 2023-12-25
 * @author Scott Davis
 */
package com.mars_sim.core.building.utility.power;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.structure.Settlement;

/**
 * An areothermal power source.
 */
public class AreothermalPowerSource
extends PowerSource {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final double MAINTENANCE_FACTOR = .5D;
	
	private double powerGenerated;
	
	/**
	 * Constructor.
	 * 
	 * @param maxPower the max power generated (kW).
	 */
	public AreothermalPowerSource(double maxPower) {
		// Call PowerSource constructor.
		super(PowerSourceType.AREOTHERMAL_POWER, maxPower);
	}

	@Override
	public double getCurrentPower(Building building) {
		return getAveragePower(building.getSettlement());
	}

	@Override
	public double getAveragePower(Settlement settlement) {
		if (powerGenerated == 0D) {
			return surface.getAreothermalPotential(settlement.getCoordinates());
		}
		return powerGenerated;
	}

	@Override
	public double getMaintenanceTime() {
	    return getMaxPower() * MAINTENANCE_FACTOR;
	}
	
	/**
	 * Measures or estimates the power produced by this power source.
	 * 
	 * @param percent The percentage of capacity of this power source
	 * @return power (kWe)
	 */
	@Override
	public double measurePower(double percent) {
		return getMaxPower() * percent / 100;
	}
}
