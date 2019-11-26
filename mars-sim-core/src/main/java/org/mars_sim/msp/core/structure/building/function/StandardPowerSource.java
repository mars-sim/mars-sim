/**
 * Mars Simulation Project
 * StandardPowerSource.java
 * @version 3.1.0 2019-09-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;

import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * A power source that gives a constant supply of power.
 */
public class StandardPowerSource
extends PowerSource
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final double MAINTENANCE_FACTOR = 2D;
	
	public StandardPowerSource(double maxPower) {
		// Call PowerSource constructor.
		super(PowerSourceType.STANDARD_POWER, maxPower);
	}

	/**
	 * Gets the current power produced by the power source.
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