/*
 * Mars Simulation Project
 * GenericContainer.java
 * @date 2023-06-12
 * @author Barry Evans
 */
package com.mars_sim.core.equipment;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import com.mars_sim.core.UnitType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.resource.AmountResource;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;

/**
 * A container class for holding resources.
 */
class GenericContainer extends Equipment implements Container {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static SimLogger logger = SimLogger.getLogger(GenericContainer.class.getName());
	private static final Map<EquipmentType,String> DESCRIPTION_CACHE = new EnumMap<>(EquipmentType.class);

	private double totalCapacity;
	private double amountStored;
	private int resourceHeld = -1;
	private boolean reusable;
	
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
		setDescription(createDescription(type));

		this.reusable = reusable;
		this.totalCapacity = ContainerUtil.getContainerCapacity(type);
		
		setBaseMass(type);
	}
		
	/**
	 * Caches descriptions per Equipment Type to reduce String creation
	 * @param type
	 * @return
	 */
	private static String createDescription(EquipmentType type) {
		return DESCRIPTION_CACHE.computeIfAbsent(type, t -> "A standard " + t.getName() + ".");
	}

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
	public Set<Integer> getSpecificResourceStoredIDs() {
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
	public Set<Integer> getAllAmountResourceStoredIDs() {
		return getSpecificResourceStoredIDs();
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
	public double getSpecificAmountResourceStored(int resource) {
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
	public double getAllSpecificAmountResourceStored(int resource) {
		return getSpecificAmountResourceStored(resource);
	}
	
	/**
	 * Gets the quantity of all stock and specific amount resource stored.
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public double getAllAmountResourceStored(int resource) {
		return getSpecificAmountResourceStored(resource);
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
	 * Obtains the combined capacity of the remaining storage space of a particular amount resource.
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public double getRemainingCombinedCapacity(int resource) {
		if ((resourceHeld == -1) && canStore(resource)) {
			return totalCapacity;
		}
		else if (resourceHeld == resource) {
			return totalCapacity - amountStored;
		}

		return 0;
	}
	
	/**
	 * Obtains the specific capacity of the remaining storage space of a particular amount resource.
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public double getRemainingSpecificCapacity(int resource) {
		return getRemainingCombinedCapacity(resource);
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
	public double getSpecificCapacity(int resource) {
		if (((resourceHeld == -1) && canStore(resource)) || (resource == resourceHeld)) {
			return totalCapacity;
		}
		return 0;
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
