/**
 * Mars Simulation Project
 * Cooking.java
 * @version 2.78 2004-11-10
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.structure.building.function;

import java.io.Serializable;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.SimulationConfig;
import org.mars_sim.msp.simulation.structure.building.*;


public class Cooking extends Function implements Serializable {

	public static final String NAME = "Cooking";

	// Data members
	private int numCooks;
	private int cookCapacity;
	
	/**
	 * Constructor
	 * @param building the building this function is for.
	 * @throws BuildingException if error in constructing function.
	 */
	public Cooking(Building building) throws BuildingException {
		// Use Function constructor.
		super(NAME, building);
		
		numCooks = 0;
		
		SimulationConfig simConfig = Simulation.instance().getSimConfig();
		BuildingConfig config = simConfig.getBuildingConfiguration();
		
		try {
			this.cookCapacity = config.getCookCapacity(building.getName());
		}
		catch (Exception e) {
			throw new BuildingException("Cooking.constructor: " + e.getMessage());
		}		
	}	
	
	/**
	 * Get the maximum number of cooks supported by this facility.
	 * @return max number of cooks
	 */
	public int getCookCapacity() {
		return cookCapacity;
	}
	
	/**
	 * Get the current number of cooks using this facility.
	 * @return number of cooks
	 */
	public int getNumCooks() {
		return numCooks;
	}
	
	/**
	 * Time passing for the building.
	 * @param time amount of time passing (in millisols)
	 * @throws BuildingException if error occurs.
	 */
	public void timePassing(double time) throws BuildingException {

	}

	/**
	 * Gets the amount of power required when function is at full power.
	 * @return power (kW)
	 */
	public double getFullPowerRequired() {
		return numCooks * 10D;
	}

	/**
	 * Gets the amount of power required when function is at power down level.
	 * @return power (kW)
	 */
	public double getPowerDownPowerRequired() {
		return 0;
	}
}