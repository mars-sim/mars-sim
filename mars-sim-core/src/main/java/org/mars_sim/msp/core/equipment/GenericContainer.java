/*
 * Mars Simulation Project
 * GenericContainer.java
 * @date 2023-06-12
 * @author Barry Evans
 */
package org.mars_sim.msp.core.equipment;

import java.util.Collections;
import java.util.Set;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * A container class for holding resources.
 */
class GenericContainer extends Equipment implements Container {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static SimLogger logger = SimLogger.getLogger(GenericContainer.class.getName());

	private double totalCapacity;
	private double amountStored;
	private int resourceHeld = -1;
	private boolean reusable;

	private EquipmentType type;
	
	/**
	 * Constructor.
	 * 
	 * @param name
	 * @param type Type of container
	 * @param base the location of the bag.
	 * @param reusable Is the container reusable by a different resource when empty
	 * @throws Exception if error creating bag.
	 */
	GenericContainer(String name, EquipmentType type, boolean reusable, Settlement base) {
		// Use Equipment constructor
		super(name, type, type.name(), base);
		
		this.type = type;
		this.reusable = reusable;
		this.totalCapacity = ContainerUtil.getContainerCapacity(type);
		
		setBaseMass(type);
	}

//	/**
//	 * Gets the base mass.
//	 */
//	@Override
//	public double getBaseMass() {
//		double mass = super.getBaseMass();
//		if (mass == 0) {
//			setBaseMass(type);
//		}
//		return super.getBaseMass();
//	}
	
	/**
	 * Sets the base mass of this container type.
	 * 
	 * @param type
	 */
	public void setBaseMass(EquipmentType type) {
		// Sets the base mass of the bag.
		setBaseMass(EquipmentFactory.getEquipmentMass(type));
	}
	
	/**
	 * Gets the total capacity of resource that this container can hold.
	 *
	 * @return total capacity (kg).
	 */
	@Override
	public double getCargoCapacity() {
		return totalCapacity;
	}


	/**
	 * Get the resource that is held.
	 */
	@Override
	public int getResource() {
		return resourceHeld;
	}

	/**
	 * Gets a list of supported resources.
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
	 * Gets all stored amount resources in eqmInventory, including inside equipment
	 *
	 * @return all stored amount resources.
	 */
	@Override
	public Set<Integer> getAllAmountResourceIDs() {
		return getAmountResourceIDs();
	}
	
	/**
	 * Total mass held.
	 */
	@Override
	public double getStoredMass() {
		return amountStored;
	}

	
	
	/**
	 * Retrieves the resource.
	 *
	 * @param resource
	 * @param quantity
	 * @return quantity that cannot be retrieved
	 */
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
	 * Gets the amount resource stored.
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
	 * Gets all the amount resource resource stored, including inside equipment.
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public double getAllAmountResourceStored(int resource) {
		return getAmountResourceStored(resource);
	}
	
	/**
	 * Stores the resource but only if it matches current resource or empty.
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
			if (canStore(resource)) {
				// Allocate the capacity to this new resource
				resourceHeld = resource;
			}
			else {
				throw new IllegalArgumentException("Can not resource "
							+ ResourceUtil.findAmountResourceName(resource)
							+ " in a " + getEquipmentType().getName());
			}
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
	 * Can this container hold a specific Amount Resources ? 
	 * It will look at the Phase Type.
	 */
	private boolean canStore(int resourceId) {
		AmountResource required = ResourceUtil.findAmountResource(resourceId);
		return ContainerUtil.isPhaseSupported(getEquipmentType(), required.getPhase());
	}

	/**
	 * Obtains the remaining storage space of a particular amount resource.
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public double getAmountResourceRemainingCapacity(int resource) {
		if ((resourceHeld == -1) && canStore(resource)) {
			return totalCapacity;
		}
		else if (resourceHeld == resource) {
			return totalCapacity - amountStored;
		}

		return 0;
	}

	/**
	 * Does it have unused space or capacity for a particular resource ?
	 * 
	 * @param resource
	 * @return
	 */
	@Override
	public boolean hasAmountResourceRemainingCapacity(int resource) {
		if (resourceHeld == resource) {
			return totalCapacity > amountStored;
		}
		return false;
	}
	
	/**
	 * Gets the capacity of a particular amount resource. Check if container
	 * is unallocated.
	 *
	 * @param resource
	 * @return capacity
	 */
	@Override
	public double getAmountResourceCapacity(int resource) {
		if (((resourceHeld == -1) && canStore(resource)) || (resource == resourceHeld)) {
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
	public boolean hasResource(int resource) {
		return resourceHeld == resource;
	}

	/**
	 * Is this equipment empty ?
	 *
	 * @param brandNew true if it needs to be brand new
	 * @return
	 */
	public boolean isEmpty(boolean brandNew) {
		if (brandNew) {
			return (getRegisteredOwnerID() == -1);
		}

		return resourceHeld == -1;
	}

	/**
	 * Gets the holder's unit instance.
	 *
	 * @return the holder's unit instance
	 */
	@Override
	public Unit getHolder() {
		return this;
	}

	@Override
	public UnitType getUnitType() {
		return UnitType.CONTAINER;
	}
	
	/**
	 * Cleans the container by resetting the assigned resource.
	 */
	@Override
	public void clean() {
		if (amountStored > 0) {
			logger.warning(this, "Not empty during cleaning");
		}
		else {
			resourceHeld = -1;
		}
	}
}
