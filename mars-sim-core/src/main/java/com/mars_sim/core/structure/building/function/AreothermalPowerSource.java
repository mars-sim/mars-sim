/*
 * Mars Simulation Project
 * AreothermalPowerSource.java
 * @date 2023-12-25
 * @author Scott Davis
 */
package com.mars_sim.core.structure.building.function;

import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;

/**
 * An areothermal power source.
 */
public class AreothermalPowerSource
extends PowerSource {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final double MAINTENANCE_FACTOR = .5D;
	
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
		return surface.getAreothermalPotential(settlement.getCoordinates());
	}

	@Override
	public double getMaintenanceTime() {
	    return getMaxPower() * MAINTENANCE_FACTOR;
	}
}
