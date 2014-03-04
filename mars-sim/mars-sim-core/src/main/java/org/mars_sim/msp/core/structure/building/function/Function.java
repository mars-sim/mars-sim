/**
 * Mars Simulation Project
 * Function.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;

import org.mars_sim.msp.core.structure.building.Building;

/**
 * A settlement building function.
 */
public abstract class Function
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private String name;
	private Building building;

	/**
	 * Constructor.
	 * @param name the function name.
	 */
	public Function(String name, Building building) {
		this.name = name;
		this.building = building;
	}

	/**
	 * Gets the function name.
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the function's building.
	 * @return building
	 */
	public Building getBuilding() {
		return building;
	}

	/**
	 * Gets the function's malfunction scope strings.
	 * @return array of scope strings.
	 */
	public String[] getMalfunctionScopeStrings() {
		String[] result = {name};
		return result;
	}

	/**
	 * Time passing for the building.
	 * @param time amount of time passing (in millisols)
	 */
	public abstract void timePassing(double time) ;

	/**
	 * Gets the amount of power required when function is at full power.
	 * @return power (kW)
	 */
	public abstract double getFullPowerRequired();

	/**
	 * Gets the amount of power required when function is at power down level.
	 * @return power (kW)
	 */
	public abstract double getPowerDownPowerRequired();

	/**
	 * Perform any actions needed when removing this building function from
	 * the settlement.
	 */
	public void removeFromSettlement() {
		// Override as needed.
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		name = null;
		building = null;
	}
}