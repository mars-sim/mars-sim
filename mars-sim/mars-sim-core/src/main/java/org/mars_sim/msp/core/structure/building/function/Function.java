/**
 * Mars Simulation Project
 * Function.java
 * @version 3.06 2014-03-08
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

	private BuildingFunction function;
	private Building building;

	/**
	 * Constructor.
	 * @param function the function name.
	 */
	public Function(BuildingFunction function, Building building) {
		this.function = function;
		this.building = building;
	}

	/**
	 * Gets the function.
	 * @return {@link BuildingFunction}
	 */
	public BuildingFunction getFunction() {
		return function;
	}

	/**
	 * Gets the function's building.
	 * @return building
	 */
	public Building getBuilding() {
		return building;
	}

	/**
	 * Gets the maintenance time for this building function.
	 * @return maintenance work time (millisols).
	 */
	public abstract double getMaintenanceTime();
	
	/**
	 * Gets the function's malfunction scope strings.
	 * @return array of scope strings.
	 * @deprecated
	 * TODO malfunction scope strings should be internationalized.
	 */
	public String[] getMalfunctionScopeStrings() {
		String[] result = {function.getName()};
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
		function = null;
		building = null;
	}
}