/**
 * Mars Simulation Project
 * WindPowerSource.java
 * @version 3.07 2014-12-06
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * An areothermal power source.
 */
public class AreothermalPowerSource
extends PowerSource {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
	
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
		Coordinates location = building.getBuildingManager().getSettlement().getCoordinates();
		double areothermalHeat = surface.getAreothermalPotential(location);

		return getMaxPower() * (areothermalHeat / 100D);
	}

	@Override
	public double getAveragePower(Settlement settlement) {
		Coordinates location = settlement.getCoordinates();
		double areothermalHeat =surface.getAreothermalPotential(location);

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