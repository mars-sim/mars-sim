/**
 * Mars Simulation Project
 * PowerSource.java
 * @version 3.02 2011-11-26
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;

import java.io.Serializable;

/**
 * The PowerSource class represents a power generator for a building.
 */
public abstract class PowerSource implements Serializable {

	// Data members
	private String type;
	private double maxPower;

	/**
	 * Constructor
	 * @param type the type of power source.
	 * @param maxPower the max power generated.
	 */
	public PowerSource(String type, double maxPower) {
		this.type = type;
		this.maxPower = maxPower;
	}

	/**
	 * Gets the type of power source.
	 * @return type
	 */
	public String getType() {
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
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
	    type = null;
	}
}