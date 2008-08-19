/**
 * Mars Simulation Project
 * Dining.java
 * @version 2.85 2008-08-18
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.structure.building.function;

import java.io.Serializable;

import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.*;

/**
 * The Dining class is a building function for dining.
 */
public class Dining extends Function implements Serializable {
        
	public static final String NAME = "Dining";
	
	/**
	 * Constructor
	 * @param building the building this function is for.
	 */
	public Dining(Building building) {
		// Use Function constructor.
		super(NAME, building);
	}
    
    /**
     * Gets the value of the function for a named building.
     * @param buildingName the building name.
     * @param newBuilding true if adding a new building.
     * @param settlement the settlement.
     * @return value (VP) of building function.
     */
    public static final double getFunctionValue(String buildingName, boolean newBuilding, 
            Settlement settlement) {
        // TODO: Implement later as needed.
        return 0D;
    }
	
	/**
	 * Time passing for the building.
	 * @param time amount of time passing (in millisols)
	 * @throws BuildingException if error occurs.
	 */
	public void timePassing(double time) throws BuildingException {}
	
	/**
	 * Gets the amount of power required when function is at full power.
	 * @return power (kW)
	 */
	public double getFullPowerRequired() {
		return 0D;
	}
	
	/**
	 * Gets the amount of power required when function is at power down level.
	 * @return power (kW)
	 */
	public double getPowerDownPowerRequired() {
		return 0D;
	}
}