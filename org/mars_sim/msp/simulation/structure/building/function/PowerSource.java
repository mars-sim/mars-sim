/**
 * Mars Simulation Project
 * PowerSource.java
 * @version 2.75 2004-03-29
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.structure.building.function;

import java.io.Serializable;
import org.mars_sim.msp.simulation.structure.building.Building;

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
}