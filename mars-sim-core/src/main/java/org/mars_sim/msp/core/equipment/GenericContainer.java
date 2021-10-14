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
	@Override
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
	 * Stores the resource but only if it matches current resource or empty
	 * 
	 * @param resource
	 * @param quantity
	 * @return excess quantity that cannot be stored
	 */
	public double storeAmountResource(int resource, double quantity) {
		// Question: if a bag was filled with regolith and later was emptied out
		// should it be tagged for only regolith and NOT for another resource ?
		int allocated = getResource();
		if (allocated == -1) {
			// Allocate the capacity to this new resource
			microInventory.setCapacity(resource, getTotalCapacity());
		}
		else if (allocated != resource) {
			return quantity; // Allocated to a different resource
		}
		return microInventory.storeAmountResource(resource, quantity);
	}
	
	/**
	 * Obtains the remaining storage space of a particular amount resource
	 * 
	 * @param resource
	 * @return quantity
	 */
	@Override
	public double getAmountResourceRemainingCapacity(int resource) {
		if (isUnallocated()) {
			return totalCapacity;
		}
		else {
			return microInventory.getAmountResourceRemainingCapacity(resource);
		}
	}
		
	/**
	 * Gets the capacity of a particular amount resource. Check if container
	 * is unallocated
	 * 
	 * @param resource
	 * @return capacity
	 */
	@Override
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

	@Override
	public int getResource() {
		return microInventory.getResource();
	}
}
