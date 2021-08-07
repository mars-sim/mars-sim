/**
 * Mars Simulation Project
 * Bag.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.equipment;

import java.io.Serializable;

import org.mars_sim.msp.core.resource.PhaseType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * A bag container for holding solid amount resources.
 */
public class Bag extends Equipment implements Container, Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Static data members
	public static final String TYPE = EquipmentType.BAG.getName();
	public static final double CAPACITY = 50D;
	public static final double EMPTY_MASS = .1D;
	/** The phase type that this container can hold */
	public static final PhaseType phaseType = PhaseType.SOLID;
	
	/**
	 * Constructor.
	 * @param newName 
	 * 
	 * @param base the location of the bag.
	 * @throws Exception if error creating bag.
	 */
	public Bag(String name, Settlement base) {
		// Use Equipment constructor
		super(name, TYPE, base);

		// Sets the base mass of the bag.
		setBaseMass(EMPTY_MASS);

		// Set the solid capacity.
		getInventory().addAmountResourcePhaseCapacity(phaseType, CAPACITY);
	}

	/**
	 * Gets the phase of resources this container can hold.
	 * 
	 * @return resource phase.
	 */
	public PhaseType getContainingResourcePhase() {
		return phaseType;
	}

	/**
	 * Gets the total capacity of resource that this container can hold.
	 * 
	 * @return total capacity (kg).
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
