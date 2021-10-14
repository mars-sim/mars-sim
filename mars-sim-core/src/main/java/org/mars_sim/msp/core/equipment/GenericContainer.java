/*
 * Mars Simulation Project
 * GenericContainer.java
 * @date 2021-10-13
 * @author Barry Evans
 */
package org.mars_sim.msp.core.equipment;

import java.io.Serializable;

import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * A container class for holding resources.
 */
class GenericContainer extends Equipment implements Container, Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	private double totalCapacity;
	
	/**
	 * Constructor.
	 * @param newName 
	 * 
	 * @param base the location of the bag.
	 * @throws Exception if error creating bag.
	 */
	GenericContainer(String name, EquipmentType type, Settlement base) {
		// Use Equipment constructor
		super(name, type, type.name(), base);

		// Sets the base mass of the bag.
		setBaseMass(EquipmentFactory.getEquipmentMass(type));
		
		this.totalCapacity = ContainerUtil.getContainerCapacity(type);
	}

	/**
	 * Gets the total capacity of resource that this container can hold.
	 * 
	 * @return total capacity (kg).
	 */
	public double getTotalCapacity() {
		return totalCapacity;
	}

	/**
	 * THis need optimising. The Get ResourceID method should
	 * only be in Container
	 * @return
	 */
	private boolean isUnallocated() {
		return getResource() == -1;
	}
	/**
	 * Gets the capacity of a particular amount resource. Check if container
	 * is unallocated
	 * 
	 * @param resource
	 * @return capacity
	 */
	public double getAmountResourceCapacity(int resource) {
		if (isUnallocated() || microInventory.isResourceSupported(resource)) {
			return totalCapacity;
		}
		return 0;
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
