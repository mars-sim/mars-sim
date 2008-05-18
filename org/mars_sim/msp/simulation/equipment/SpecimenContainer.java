/**
 * Mars Simulation Project
 * SpecimenContainer.java
 * @version 2.84 2008-05-17
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.equipment;

import java.io.Serializable;
import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.resource.Phase;

/**
 * A specialized container for holding rock samples.
 */
public class SpecimenContainer extends Equipment implements Container, Serializable {

	public static final String TYPE = "Specimen Box";
	private static final double BASE_MASS = .5D; // Base mass of the container (kg).
	private static final double ROCK_CAPACITY = 10D; // Rock sample capacity (kg).
	
	public SpecimenContainer(Coordinates location) throws Exception {
		// Use Equipment constructor.
		super(TYPE, location);
		
		// Set the base mass of the container.
		setBaseMass(BASE_MASS);
		
		// Set the capacity of the container.
		getInventory().addAmountResourceTypeCapacity(
				AmountResource.findAmountResource("rock samples"), ROCK_CAPACITY);
	}
	
	/**
	 * Gets the phase of resources this container can hold.
	 * @return resource phase.
	 */
	public Phase getContainingResourcePhase() {
		return Phase.SOLID;
	}
}