/**
 * Mars Simulation Project
 * SpecimenContainer.java
 * @version 3.1.0 2017-09-04
 * @author Scott Davis
 */
package org.mars_sim.msp.core.equipment;

import java.io.Serializable;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.resource.PhaseType;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * A specialized container for holding rock samples.
 */
public class SpecimenContainer
extends Equipment
implements Container, Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	public static final String TYPE = "Specimen Box";
	/** Base mass of the container (kg). */
	public static final double EMPTY_MASS = .5D;
	/** Rock sample capacity (kg). */
	public static final double CAPACITY = 10D;

	public SpecimenContainer(Coordinates location) {
		// Use Equipment constructor.
		super(TYPE, location);

		// Set the base mass of the container.
		setBaseMass(EMPTY_MASS);

		// Set the capacity of the container.
		getInventory().addAmountResourceTypeCapacity(ResourceUtil.rockSamplesAR, CAPACITY);
	}

	/**
	 * Gets the phase of resources this container can hold.
	 * @return resource phase.
	 */
	public PhaseType getContainingResourcePhase() {
		return PhaseType.SOLID;
	}

	/**
	 * Gets the total capacity of resource that this container can hold.
	 * @return total capacity (kg).
	 */
	public double getTotalCapacity() {
		return CAPACITY;
	}
}