/*
 * Mars Simulation Project
 * FissionPowerSource.java
 * @date 2023-05-25
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * A fission power source that gives a constant supply of power.
 */
public class FissionPowerSource extends PowerSource {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final double MAINTENANCE_FACTOR = 2D;
	
	public FissionPowerSource(double maxPower) {
		// Call PowerSource constructor.
		super(PowerSourceType.FISSION_POWER, maxPower);
	}

	/**
	 * Gets the current power produced by the power source.
	 * 
	 * @param building the building this power source is for.
	 * @return power (kW)
	 */
	public double getCurrentPower(Building building) {
		return getMaxPower();
	}


	public double getAveragePower(Settlement settlement) {
		return getMaxPower() * 0.707;
	}

	@Override
	public double getMaintenanceTime() {
	    return getMaxPower() * MAINTENANCE_FACTOR;
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
