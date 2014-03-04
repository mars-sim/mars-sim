/**
 * Mars Simulation Project
 * WindPowerSource.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Simulation;
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
		super(PowerSourceType.AREAOTHERMAL, maxPower);
	}

	@Override
	public double getCurrentPower(Building building) {
		Coordinates location = building.getBuildingManager().getSettlement().getCoordinates();
		double areothermalHeat = Simulation.instance().getMars().getSurfaceFeatures()
				.getAreothermalPotential(location);

		return getMaxPower() * (areothermalHeat / 100D);
	}

	@Override
	public double getAveragePower(Settlement settlement) {
		Coordinates location = settlement.getCoordinates();
		double areothermalHeat = Simulation.instance().getMars().getSurfaceFeatures()
				.getAreothermalPotential(location);

		return getMaxPower() * (areothermalHeat / 100D);
	}
}