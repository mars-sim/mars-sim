/**
 * Mars Simulation Project
 * GasCanister.java
 * @version 2.79 2006-01-18
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.equipment;

import java.io.Serializable;
import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.simulation.resource.Phase;

/**
 * A canister container for holding gas amount resources.
 */
public class GasCanister extends Equipment implements Container, Serializable {

	// Static data members
	public static final String TYPE = "Gas Canister";
	private static final double BASE_MASS = 200D; // Empty mass of gas canister (kg).
	
	/**
	 * Constructor
	 * @param location the location of the gas canister.
	 * @param capacity the gas capacity of the canister (kg).
	 * @throws Exception if error creating gas canister.
	 */
	public GasCanister(Coordinates location, double capacity) throws Exception {
		// Use Equipment constructor.
		super(TYPE, location);
		
		// Sets the base mass of the gas canister.
		setBaseMass((capacity / 100D) * BASE_MASS);
		
		// Set the gas capacity.
		if (capacity < 0D) throw new Exception("Capacity cannot be less than zero.");
		getInventory().addAmountResourcePhaseCapacity(Phase.GAS, capacity);
	}
	
	/**
	 * Gets the phase of resources this container can hold.
	 * @return resource phase.
	 */
	public Phase getContainingResourcePhase() {
		return Phase.GAS;
	}
}