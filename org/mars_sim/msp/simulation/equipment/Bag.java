/**
 * Mars Simulation Project
 * Bag.java
 * @version 2.79 2006-01-02
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.equipment;

import java.io.Serializable;
import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.simulation.resource.Phase;

/**
 * A bag container for holding solid amount resources.
 */
public class Bag extends Equipment implements Container, Serializable {
	
	// Static data members
	public static final String TYPE = "Bag";
	private static final double BASE_MASS = .1D; // Empty mass of bag (kg).

	public Bag(Coordinates location, double capacity) throws Exception {
		// Use Equipment constructor
		super(TYPE, location);
		
		// Sets the base mass of the bag.
		baseMass = (capacity / 100D) * BASE_MASS;
		
		// Set the solid capacity.
		if (capacity < 0D) throw new Exception("Capacity cannot be less than zero.");
		inventory.addAmountResourcePhaseCapacity(Phase.SOLID, capacity);
	}
}