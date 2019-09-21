/**
 * Mars Simulation Project
 * WindPowerSource.java
 * @version 3.1.0 2019-09-20
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
//		Coordinates location = building.getSettlement().getCoordinates();
		double areothermalHeat = surface.getAreothermalPotential(building.getSettlement().getCoordinates());

		return getMaxPower() * (areothermalHeat / 100D);
	}

	@Override
	public double getAveragePower(Settlement settlement) {
//		Coordinates location = settlement.getCoordinates();
		double areothermalHeat =surface.getAreothermalPotential(settlement.getCoordinates());

		return getMaxPower() * (areothermalHeat / 100D);
	}

	@Override
	public double getMaintenanceTime() {
	    return getMaxPower() * 1D;
	}

	@Override
	public void removeFromSettlement() {
		// TODO Auto-generated method stub
	}

	@Override
	public void setTime(double time) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void destroy() {
		super.destroy();
	}
}