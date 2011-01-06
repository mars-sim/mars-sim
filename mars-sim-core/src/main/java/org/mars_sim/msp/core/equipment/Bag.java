/**
 * Mars Simulation Project
 * Bag.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */

package org.mars_sim.msp.core.equipment;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.resource.Phase;

import java.io.Serializable;

/**
 * A bag container for holding solid amount resources.
 */
public class Bag extends Equipment implements Container, Serializable {
	
	// Static data members
	public static final String TYPE = "Bag";
    public static final double CAPACITY = 50D;
    public static final double EMPTY_MASS = .1D;

    /**
     * Constructor
     * @param location the location of the bag.
     * @throws Exception if error creating bag.
     */
	public Bag(Coordinates location) {
		// Use Equipment constructor
		super(TYPE, location);
		
		// Sets the base mass of the bag.
		setBaseMass(EMPTY_MASS);
		
		// Set the solid capacity.
		getInventory().addAmountResourcePhaseCapacity(Phase.SOLID, CAPACITY);
	}
	
	/**
	 * Gets the phase of resources this container can hold.
	 * @return resource phase.
	 */
	public Phase getContainingResourcePhase() {
		return Phase.SOLID;
	}
}