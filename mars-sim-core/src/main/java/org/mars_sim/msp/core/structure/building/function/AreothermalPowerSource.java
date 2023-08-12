/**
 * Mars Simulation Project
 * WindPowerSource.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * An areothermal power source.
 */
public class AreothermalPowerSource
extends PowerSource {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * @param maxPower the max power generated (kW).
	 */
	public AreothermalPowerSource(double maxPower) {
		// Call PowerSource constructor.
		super(PowerSourceType.AREOTHERMAL_POWER, maxPower);
	}

	@Override
	public double getCurrentPower(Building building) {
		double areothermalHeat = surface.getAreothermalPotential(building.getSettlement().getCoordinates());

		return getMaxPower() * (areothermalHeat / 100D);
	}

	@Override
	public double getAveragePower(Settlement settlement) {
		double areothermalHeat =surface.getAreothermalPotential(settlement.getCoordinates());

		return getMaxPower() * (areothermalHeat / 100D);
	}

	@Override
	public double getMaintenanceTime() {
	    return getMaxPower();
	}
}
