/**
 * Mars Simulation Project
 * PowerSource.java
 * @version 3.1.0 2017-08-14
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;

import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * The PowerSource class represents a power generator for a building.
 */
public abstract class PowerSource
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	//private static Logger logger = Logger.getLogger(HeatSource.class.getName());

	// Data members
	private PowerSourceType type;
	private double maxPower;

	/**
	 * Constructor.
	 * @param type the type of power source.
	 * @param maxPower the max power generated.
	 */
	public PowerSource(PowerSourceType type, double maxPower) {
		this.type = type;
		this.maxPower = maxPower;

	}

	/**
	 * Gets the type of power source.
	 * @return type
	 */
	public PowerSourceType getType() {
		return type;
	}

	/**
	 * Gets the max power generated.
	 * @return power (kW)
	 */
	public double getMaxPower() {
		return maxPower;
	}

	/**
	 * Gets the current power produced by the power source.
	 * @param building the building this power source is for.
	 * @return power (kW)
	 */
	public abstract double getCurrentPower(Building building);

	/**
	 * Gets the average power produced by the power source.
	 * @param settlement the settlement this power source is at.
	 * @return power(kW)
	 */
	public abstract double getAveragePower(Settlement settlement);

	/**
     * Gets the maintenance time for this power source.
     * @return maintenance work time (millisols).
     */
	public abstract double getMaintenanceTime();

	// 2015-09-28 Added removeFromSettlement() to return the fuel cell stacks to the inventory
	public abstract void removeFromSettlement();
	
	public abstract void setTime(double time);
	
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		type = null;
	}


}