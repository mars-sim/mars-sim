/*
 * Mars Simulation Project
 * SpecimenContainer.java
 * @date 2021-09-04
 * @author Scott Davis
 */
package org.mars_sim.msp.core.equipment;

import java.io.Serializable;

import org.mars_sim.msp.core.resource.PhaseType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * A specialized container for holding rock samples.
 */
public class SpecimenBox
extends Equipment
implements Container, Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	public static final String TYPE = "Specimen Box";
	/** Base mass of the container (kg). */
	public static final double EMPTY_MASS = .5D;
	/** capacity (kg). */
	public static final double CAPACITY = 50D;
	/** The phase type that this container can hold */
	public static final PhaseType phaseType = PhaseType.SOLID;
	
	public SpecimenBox(String name, Settlement settlement) {
		// Use Equipment constructor.
		super(name, TYPE, settlement);

		// Set the base mass of the container.
		setBaseMass(EMPTY_MASS);

		// Set the capacity of the container.
//		getInventory().addAmountResourceTypeCapacity(ResourceUtil.rockSamplesID, CAPACITY);
	}

	/**
	 * Gets the phase of resources this container can hold.
	 * @return resource phase.
	 */
	public PhaseType getContainingResourcePhase() {
		return phaseType;
	}

	/**
	 * Gets the capacity of resource that this container can hold.
	 * @return capacity (kg).
	 */
	public double getTotalCapacity() {
		return CAPACITY;
	}

	@Override
	public Building getBuildingLocation() {
		return getContainerUnit().getBuildingLocation();
	}

	@Override
	public Settlement getAssociatedSettlement() {
		return getContainerUnit().getAssociatedSettlement();
	}
}
