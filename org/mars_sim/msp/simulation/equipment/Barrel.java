/**
 * Mars Simulation Project
 * Barrel.java
 * @version 2.85 2008-09-13
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
    public static final double CAPACITY = 200D;
    public static final double EMPTY_MASS = 10D;
	
	/**
	 * Constructor
	 * @param location the location of the barrel.
	 * @throws Exception if error creating barrel.
	 */
	public Barrel(Coordinates location) throws Exception {
		// Use Equipment constructor
		super(TYPE, location);
		
		// Sets the base mass of the barrel.
		setBaseMass(EMPTY_MASS);
		
		// Set the liquid capacity.
		getInventory().addAmountResourcePhaseCapacity(Phase.LIQUID, CAPACITY);
	}
	
	/**
	 * Gets the phase of resources this container can hold.
	 * @return resource phase.
	 */
	public Phase getContainingResourcePhase() {
		return Phase.LIQUID;
	}
}