/**
 * Mars Simulation Project
 * GasCanister.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */

package org.mars_sim.msp.core.equipment;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.resource.Phase;

import java.io.Serializable;

/**
 * A canister container for holding gas amount resources.
 */
public class GasCanister extends Equipment implements Container, Serializable {

	// Static data members
	public static final String TYPE = "Gas Canister";
    public static final double CAPACITY = 50D;
    public static final double EMPTY_MASS = 20D;
	
	/**
	 * Constructor
	 * @param location the location of the gas canister.
	 * @throws Exception if error creating gas canister.
	 */
	public GasCanister(Coordinates location) {
		// Use Equipment constructor.
		super(TYPE, location);
		
		// Sets the base mass of the gas canister.
		setBaseMass(EMPTY_MASS);
		
		// Set the gas capacity.
		getInventory().addAmountResourcePhaseCapacity(Phase.GAS, CAPACITY);
	}
	
	/**
	 * Gets the phase of resources this container can hold.
	 * @return resource phase.
	 */
	public Phase getContainingResourcePhase() {
		return Phase.GAS;
	}
}