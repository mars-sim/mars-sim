/**
 * Mars Simulation Project
 * Exercise.java
 * @version 2.75 2004-04-01
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.structure.building.function;
 
import java.io.Serializable;
import org.mars_sim.msp.simulation.structure.building.*;

/**
 * The Exercise class is a building function for exercise.
 */
public class Exercise extends Function implements Serializable {
        
	private static final String NAME = "Exercise";
    
	/**
	 * Constructor
	 * @param building the building this function is for.
	 */
	public Exercise(Building building) {
		// Use Function constructor.
		super(NAME, building);
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