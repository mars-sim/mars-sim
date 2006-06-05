/**
 * Mars Simulation Project
 * SpecimenContainer.java
 * @version 2.79 2006-01-02
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.equipment;

import java.io.Serializable;
import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.simulation.resource.AmountResource;

/**
 * A specialized container for holding rock samples.
 */
public class SpecimenContainer extends Equipment implements Container, Serializable {

	public static final String TYPE = "Specimen Container";
	private static final double BASE_MASS = .5D; // Base mass of the container (kg).
	private static final double ROCK_CAPACITY = 50D; // Rock sample capacity (kg).
	
	public SpecimenContainer(Coordinates location) throws Exception {
		// Use Equipment constructor.
		super(TYPE, location);
		
		// Set the base mass of the container.
		baseMass = BASE_MASS;
		
		// Set the capacity of the container.
		inventory.addAmountResourceTypeCapacity(AmountResource.ROCK_SAMPLES, ROCK_CAPACITY);
	}
}