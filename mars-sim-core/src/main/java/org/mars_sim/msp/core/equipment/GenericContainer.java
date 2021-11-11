/*
 * Mars Simulation Project
 * GenericContainer.java
 * @date 2021-10-13
 * @author Barry Evans
 */
package org.mars_sim.msp.core.equipment;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * A container class for holding resources.
 */
class GenericContainer extends Equipment implements Container, Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private double totalCapacity;
	private double amountStored;
	private int resourceHeld = -1;
	private boolean reusable;

	/**
	 * Constructor.
	 * @param name
	 * @param type Type of container
	 * @param base the location of the bag.
	 * @param reusable Is the container reusable by a different resource when empty
	 * @throws Exception if error creating bag.
	 */
	GenericContainer(String name, EquipmentType type, boolean reusable, Settlement base) {
		// Use Equipment constructor
		super(name, type, type.name(), base);

		this.reusable = reusable;

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
	 * Get the resource that is held
	 */
	@Override
	public int getResource() {
		return resourceHeld;
	}

	/**
	 * Gets a list of supported resources
	 *
	 * @return a list of resource ids
	 */
	@Override
	public Set<Integer> getAmountResourceIDs() {
		if (resourceHeld == -1) {
			return Collections.emptySet();
		}
		return Set.of(resourceHeld);
	}

	/**
	 * Total mass held
	 */
	@Override
	public double getStoredMass() {
		return amountStored;
	}

	/**
	 * Retrieves the resource
	 *
	 * @param resource
	 * @param quantity
	 * @return quantity that cannot be retrieved
	 */
	@Override
	public double retrieveAmountResource(int resource, double quantity) {
		if (resourceHeld == resource) {
			if (quantity < amountStored) {
				amountStored -= quantity;
				return 0;
			}
			else {
				// Now empty
				double shortfall = quantity - amountStored;
				amountStored = 0D;
				if (reusable) {
					resourceHeld = -1;
				}
				return shortfall;
			}
		}
		return quantity;
	}

	/**
	 * Gets the amount resource stored
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public double getAmountResourceStored(int resource) {
		if (resource == resourceHeld) {
			return amountStored;
		}
		return 0;
	}

	/**
	 * Stores the resource but only if it matches current resource or empty
	 *
	 * @param resource
	 * @param quantity
	 * @return excess quantity that cannot be stored
	 */
	@Override
	public double storeAmountResource(int resource, double quantity) {
		// Question: if a bag was filled with regolith and later was emptied out
		// should it be tagged for only regolith and NOT for another resource ?
		if (resourceHeld == -1) {
			// Allocate the capacity to this new resource
			resourceHeld = resource;
		}
		else if (resourceHeld != resource) {
			return quantity; // Allocated to a different resource
		}

		double remainingCap = totalCapacity - amountStored;
		if (remainingCap < quantity) {
			amountStored = totalCapacity;
			return quantity - remainingCap;
		}
		else {
			amountStored += quantity;
			return 0D;
		}
	}

	/**
	 * Obtains the remaining storage space of a particular amount resource
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public double getAmountResourceRemainingCapacity(int resource) {
		if (resourceHeld == -1) {
			return totalCapacity;
		}
		else if (resourceHeld == resource) {
			return totalCapacity - amountStored;
		}

		return 0;
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
		if ((resourceHeld == -1) || (resource == resourceHeld)) {
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

	/**
	 * Does this unit have this resource ?
	 *
	 * @param resource
	 * @return
	 */
	@Override
	public boolean hasResource(int resource) {
		return resourceHeld == resource;
	}

	/**
	 * Gets the holder's unit instance
	 *
	 * @return the holder's unit instance
	 */
	@Override
	public Unit getHolder() {
		return this;
	}

	@Override
	public int storeItemResource(int resource, int quantity) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int retrieveItemResource(int resource, int quantity) {
		// TODO Auto-generated method stub
		return 0;
	}

}
