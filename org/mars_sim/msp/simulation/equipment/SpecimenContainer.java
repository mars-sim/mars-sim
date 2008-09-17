/**
 * Mars Simulation Project
 * SpecimenContainer.java
 * @version 2.85 2008-09-13
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
	public static final double EMPTY_MASS = .5D; // Base mass of the container (kg).
	public static final double CAPACITY = 10D; // Rock sample capacity (kg).
	
	public SpecimenContainer(Coordinates location) throws Exception {
		// Use Equipment constructor.
		super(TYPE, location);
		
		// Set the base mass of the container.
		setBaseMass(EMPTY_MASS);
		
		// Set the capacity of the container.
		getInventory().addAmountResourceTypeCapacity(
				AmountResource.findAmountResource("rock samples"), CAPACITY);
	}
	
	/**
	 * Gets the phase of resources this container can hold.
	 * @return resource phase.
	 */
	public Phase getContainingResourcePhase() {
		return Phase.SOLID;
	}
}