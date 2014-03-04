/**
 * Mars Simulation Project
 * WindPowerSource.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;

import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * A wind turbine power source.
 */
public class WindPowerSource
extends PowerSource
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * @param maxPower the maximum generated power.
	 */
	public WindPowerSource(double maxPower) {
		// Call PowerSource constructor.
		super(PowerSourceType.WIND, maxPower);
	}

	@Override
	public double getCurrentPower(Building building) {
		// TODO: Make power generated to be based on current wind speed at location.
		return getMaxPower();
	}

	@Override
	public double getAveragePower(Settlement settlement) {
		return getMaxPower() / 3D;
	}
}