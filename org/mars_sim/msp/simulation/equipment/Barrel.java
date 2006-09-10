/**
 * Mars Simulation Project
 * Barrel.java
 * @version 2.79 2006-01-02
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.equipment;

import java.io.Serializable;
import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.simulation.resource.Phase;

/**
 * A barrel container for holding liquid amount resources.
 */
public class Barrel extends Equipment implements Container, Serializable {

	// Static data members
	public static final String TYPE = "Barrel";
	private static final double BASE_MASS = 10D; // Empty mass of barrel (kg).
	
	/**
	 * Constructor
	 * @param location the location of the barrel.
	 * @param capacity the liquid capacity of the barrel (kg).
	 * @throws Exception if error creating barrel.
	 */
	public Barrel(Coordinates location, double capacity) throws Exception {
		// Use Equipment constructor
		super(TYPE, location);
		
		// Sets the base mass of the barrel.
		setBaseMass((capacity / 100D) * BASE_MASS);
		
		// Set the liquid capacity.
		if (capacity < 0D) throw new Exception("Capacity cannot be less than zero.");
		getInventory().addAmountResourcePhaseCapacity(Phase.LIQUID, capacity);
	}
}