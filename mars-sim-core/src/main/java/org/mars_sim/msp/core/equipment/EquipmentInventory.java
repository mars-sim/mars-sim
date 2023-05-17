/*
 * Mars Simulation Project
 * EquipmentInventory.java
 * @date 2021-10-23
 * @author Barry Evans
 */

package org.mars_sim.msp.core.equipment;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.data.UnitSet;
import org.mars_sim.msp.core.resource.ResourceUtil;

/**
 * The represents an Inventory that can hold Equipment as well as Resources. It provides
 * basic capacity management.
 * Resources can be retrieved from held Equipment. But resources can not be stored
 * in the underlying Equipment.
 */
public class EquipmentInventory
		implements EquipmentOwner, ItemHolder, Serializable {

	private static final long serialVersionUID = 1L;

	// default logger.
//	private static final SimLogger logger = SimLogger.getLogger(EquipmentInventory.class.getName());

	private Unit owner;

	private double cargoCapacity;

	/** Locally held equipment set**/
	private Set<Equipment> equipmentSet;

	/** The MicroInventory instance. */
	private MicroInventory microInventory;

	public EquipmentInventory(Unit owner, double cargoCapacity) {

		this.owner = owner;
		this.cargoCapacity = cargoCapacity;

		// Create equipment set
		equipmentSet = new UnitSet<>();

		// Create microInventory instance
		microInventory = new MicroInventory(owner, cargoCapacity);
	}

	/**
	 * Gets the total mass of this inventory including the total mass of any held equipment.
	 * 
	 * @return
	 */
	@Override
	public double getStoredMass() {
		double result = 0;
		for (Equipment e: equipmentSet) {
			result += e.getMass();
		}
		return result +  microInventory.getStoredMass();
	}

	/**
	 * Gets the modified mass. Useful when accounting for pushing a wheelbarrow, 
	 * instead of carrying a wheelbarrow.
	 * 
	 * @param type  the equipment type of interest
	 * @param percent the percent of mass of the equipment type to be treated as carrying mass
	 * @return
	 */
	public double getModifiedMass(EquipmentType type, double percent) {
		double result = 0;
		for (Equipment e: equipmentSet) {
			if (type == EquipmentType.WHEELBARROW) {
				result += e.getMass() * percent / 100;
			}
			else
				result += e.getMass();
		}
		return result +  microInventory.getStoredMass();
	}
	
	/**
	 * Gets the equipment set.
	 *
	 * @return
	 */
	@Override
	public Set<Equipment> getEquipmentSet() {
		if (equipmentSet == null)
			equipmentSet = new UnitSet<>();
		return equipmentSet;
//		return Collections.unmodifiableSet(equipmentSet);
	}

	/**
	 * Finds all of the containers (excluding EVA suit).
	 *
	 * @return collection of containers or empty collection if none.
	 */
	@Override
	public Collection<Container> findAllContainers() {
		Collection<Container> result = new HashSet<>();
		for (Equipment e : equipmentSet) {
			if (e != null && e.getUnitType() == UnitType.CONTAINER) {
				result.add((Container)e);
			}
		}
		return result;
	}

	/**
	 * Finds all of the containers of a particular type (excluding EVA suit).
	 *
	 * @return collection of containers or empty collection if none.
	 */
	public Collection<Container> findContainersOfType(EquipmentType type) {
		Collection<Container> result = new HashSet<>();
		for (Equipment e : equipmentSet) {
			if (e != null && e instanceof Container && type == e.getEquipmentType()) {
				result.add((Container)e);
			}
		}
		return result;
	}

	/**
	 * Does this person possess an equipment of this equipment type ?
	 *
	 * @param typeID
	 * @return
	 */
	@Override
	public boolean containsEquipment(EquipmentType type) {
		for (Equipment e: equipmentSet) {
			if (type == e.getEquipmentType()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Adds an equipment to this person.
	 *
	 * @param equipment
	 * @return true if this person can carry it
	 */
	@Override
	public boolean addEquipment(Equipment equipment) {
		boolean contained = equipmentSet.contains(equipment);
		if (!contained && (cargoCapacity >= getStoredMass() + equipment.getMass())) {
			equipmentSet.add(equipment);
			return true;
		}
		return contained;
	}

	/**
	 * Removes an equipment.
	 *
	 * @param equipment
	 */
	@Override
	public boolean removeEquipment(Equipment equipment) {
		return equipmentSet.remove(equipment);
	}

// Future: Will add the following
//	/**
//	 * Remove an equipment
//	 *
//	 * @param name
//	 */
//	@Override
//	public boolean removeEquipment(String name) {
//		for (Equipment e: equipmentSet) {
//			if (e.getName().equalsIgnoreCase(name)) {
//				equipmentSet.remove(e);
//				return true;
//			}
//		}
//		return false;
//	}
	
	/**
	 * Stores the item resource.
	 *
	 * @param resource the item resource
	 * @param quantity
	 * @return excess quantity that cannot be stored
	 */
	@Override
	public int storeItemResource(int resource, int quantity) {
		return microInventory.storeItemResource(resource, quantity);
	}

	/**
	 * Retrieves the item resource.
	 *
	 * @param resource
	 * @param quantity
	 * @return quantity that cannot be retrieved
	 */
	@Override
	public int retrieveItemResource(int resource, int quantity) {
		return microInventory.retrieveItemResource(resource, quantity);
	}

	/**
	 * Retrieves the resource.
	 *
	 * @param resource
	 * @param quantity
	 * @return quantity that cannot be retrieved
	 */
	@Override
	public double retrieveAmountResource(int resource, double quantity) {
		double shortfall = quantity;
		double stored = microInventory.getAmountResourceStored(resource);
		if (stored > 0) {
			shortfall = microInventory.retrieveAmountResource(resource, shortfall);
		}

		// if (shortfall > 0D) {
		// 	for (Equipment e: equipmentSet) {
		// 		if (e instanceof Container) {
		// 			// Only take resources out of Containers;
		// 			// other active Equipment, e.g. EVASuit need the resources to function
		// 			shortfall = e.retrieveAmountResource(resource, shortfall);
		// 			if (shortfall == 0D) {
		// 				return 0D;
		// 			}
		// 		}
		// 	}
		// }

		// Return any missing quantity
		return shortfall;
	}

	/**
	 * Stores the resource.
	 *
	 * @param resource
	 * @param quantity
	 * @return excess quantity that cannot be stored
	 */
	@Override
	public double storeAmountResource(int resource, double quantity) {
		// Note: this method is different from
		// Equipment's storeAmountResource
		if (!microInventory.isResourceSupported(resource)) {
			// Since cargoCapacity is changing dynamically,
			// does it mean one must constantly update the capacity of this amount resource ?
			microInventory.setCapacity(resource, cargoCapacity);
		}
		return microInventory.storeAmountResource(resource, quantity);
	}

	/**
	 * Gets the item resource stored.
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public int getItemResourceStored(int resource) {
		return microInventory.getItemResourceStored(resource);
	}

	/**
	 * Gets the capacity of a particular amount resource.
	 *
	 * @param resource
	 * @return capacity
	 */
	@Override
	public double getAmountResourceCapacity(int resource) {
		return microInventory.getCapacity(resource);

		// double result = 0;

		// for (Equipment e: equipmentSet) {
		// 	if (e instanceof ResourceHolder) {
		// 		result += e.getAmountResourceCapacity(resource);
		// 	}
		// }

		// return result + microInventory.getCapacity(resource);
	}

	/**
	 * Obtains the remaining storage space of a particular amount resource.
	 *
	 * @param resource
	 * @return remaining capacity
	 */
	@Override
	public double getAmountResourceRemainingCapacity(int resource) {

		double cap = microInventory.getCapacity(resource);
		double stored = microInventory.getAmountResourceStored(resource);

		// for (Equipment e: equipmentSet) {

		// 	if (e instanceof ResourceHolder) {
		// 		cap += e.getAmountResourceCapacity(resource);
		// 		stored += e.getAmountResourceStored(resource);
		// 	}
		// }

		// Note: it should not include the general capacity,
		// aka cargo capacity, getRemainingCargoCapacity(), 
		// since cargo capacity can be shared and can be
		// misleading.
		
		return cap - stored;
	}

	/**
	 * Does it have unused space or capacity for a particular resource ?
	 * 
	 * @param resource
	 * @return
	 */
	public boolean hasAmountResourceRemainingCapacity(int resource) {
		
		double cap = microInventory.getCapacity(resource);
		double stored = microInventory.getAmountResourceStored(resource);
		
		if (cap > stored)
			return true;
		
		// for (Equipment e: equipmentSet) {

		// 	if (e instanceof ResourceHolder) {
		// 		cap = e.getAmountResourceCapacity(resource);
		// 		stored = e.getAmountResourceStored(resource);
		// 	}
			
		// 	if (cap > stored)
		// 		return true;
		// }
		
		return false;
	}
	
	/**
	 * Obtains the remaining cargo/general/shared capacity.
	 *
	 * @return remaining capacity
	 */
	@Override
	public double getRemainingCargoCapacity() {
		return cargoCapacity - getStoredMass();
	}

	/**
     * Gets the cargo/general/shared capacity.
     *
     * @return capacity (kg).
     */
	@Override
	public double getCargoCapacity() {
		// Question: Should the total capacity varies ?
		// based on one's instant carrying capacity ?
		return cargoCapacity;
	}

	/**
	 * Gets the amount resource stored.
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public double getAmountResourceStored(int resource) {
		double result = 0;

		// Do not consume resources from container Equipment. THis is an expensive
		// and can generate concurrency issues
		// for(Equipment e: equipmentSet) {
		// 	if (e instanceof ResourceHolder) {
		// 		result += e.getAmountResourceStored(resource);
		// 	}
		// }

		return result + microInventory.getAmountResourceStored(resource);
	}


	/**
	 * Finds the number of empty containers of a particular equipment.
	 *
	 * @param containerType the equipment type.
	 * @param brandNew  does it include brand new bag only
	 * @return number of empty containers.
	 */
	@Override
	public int findNumEmptyContainersOfType(EquipmentType containerType, boolean brandNew) {
		return (int) equipmentSet.stream().filter(e -> e.isEmpty(brandNew) && (e.getEquipmentType() == containerType))
								.count();
	}

	/**
	 * Finds the number of containers of a particular type.
	 *
	 * @param containerType the equipment type.
	 * @return number of empty containers.
	 */
	@Override
	public int findNumContainersOfType(EquipmentType containerType) {
		return (int) equipmentSet.stream().filter(e -> e.getEquipmentType() == containerType).count();
	}

	/**
	 * Finds a container in storage.
	 *
	 * @param containerType
	 * @param empty does it need to be empty ?
	 * @param resource If -1 then resource doesn't matter
	 * @return instance of container or null if none.
	 */
	@Override
	public Container findContainer(EquipmentType containerType, boolean empty, int resource) {
		for (Equipment e : equipmentSet) {
			if (e.getEquipmentType() == containerType) {
				Container c = (Container) e;
				// Check it matches the resource spec
				int containerResource = c.getResource();
				if (resource == -1 || containerResource == resource || containerResource == -1) {
					if (!empty || (c.getStoredMass() == 0D)) {
						return c;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Finds a container in storage.
	 *
	 * @param containerType
	 * @param empty does it need to be empty ?
	 * @param resource If -1 then resource doesn't matter
	 * @retu,rn instance of container or null if none.
	 */
	public Container findOwnedContainer(EquipmentType containerType, int personId, int resource) {
		for (Equipment e : equipmentSet) {
			if (e.getEquipmentType() == containerType) {
				Container c = (Container) e;
				// Check it matches the resource spec
				int containerResource = c.getResource();
				if (resource == -1 || containerResource == resource || containerResource == -1) {
					if (e.getLastOwnerID() == personId) {
						return c;
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Gets a set of item resources in storage.
	 *
	 * @return a set of item resources.
	 */
	@Override
	public Set<Integer> getItemResourceIDs() {
		return microInventory.getItemsStored();
		// Set<Integer> set = new HashSet<>(microInventory.getItemsStored());
		// for (Equipment e: equipmentSet) {
		// 	if (e instanceof ItemHolder) {
		// 		set.addAll(((ItemHolder) e).getItemResourceIDs());
		// 	}
		// }

		// return set;
	}

	/**
	 * Gets a set of amount resources in storage.
	 *
	 * @return a set of amount resources
	 */
	@Override
	public Set<Integer> getAmountResourceIDs() {
		return microInventory.getResourcesStored();
		// Set<Integer> set = new HashSet<>(microInventory.getResourcesStored());
		// for (Equipment e: equipmentSet) {
		// 	if (e instanceof ResourceHolder) {
		// 		set.addAll(((ResourceHolder) e).getAmountResourceIDs());
		// 	}
		// }

		// return set;
	}


	/**
	 * Is this unit empty ?
	 *
	 * @return true if this unit doesn't carry any resources or equipment
	 */
	public boolean isEmpty() {
		if (!equipmentSet.isEmpty())
			return false;
		return microInventory.isEmpty();
	}

	/**
	 * Adds resource capacity.
	 *
	 * @param resource
	 * @param capacity
	 */
	public void addResourceCapacity(int resource, double capacity) {
		if (ResourceUtil.findAmountResource(resource) != null) {
			microInventory.addCapacity(resource, capacity);
		}
	}

	/**
	 * Sets the resource capacity.
	 *
	 * @param resource
	 * @param capacity
	 */
	public void setResourceCapacity(int resource, double capacity) {
		if (ResourceUtil.findAmountResource(resource) != null) {
			microInventory.setCapacity(resource, capacity);
		}
	}

	/**
	 * Sets the resource capacities.
	 *
	 * TODO should be keyed on resourceID not string.
	 * @param capacities
	 */
	public void setResourceCapacityMap(Map<Integer, Double> capacities) {
		setResourceCapacityMap(capacities, false);
	}

	/**
	 * Sets the resource capacities.
	 * 
	 * @param capacities
	 * @param add Should these be added to the current values (or else it should be set to the current value)
	 */
	public void setResourceCapacityMap(Map<Integer, Double> capacities, boolean add) {
		for (Entry<Integer, Double> v : capacities.entrySet()) {
			Integer foundResource = v.getKey();
			if (add) {
				microInventory.addCapacity(foundResource, v.getValue());
			}
			else {
				microInventory.setCapacity(foundResource, v.getValue());
			}
		}
	}

	/**
	 * Adds to the cargo/general/shared capacity.
	 *
	 * @param value
	 */
	public void addCargoCapacity(double value) {
		cargoCapacity += value;
		microInventory.setSharedCapacity(cargoCapacity);
	}

	/**
	 * Adds the capacity of a particular resource.
	 *
	 * @param resource
	 * @param capacity
	 */
	public void addCapacity(int resource, double capacity) {
		microInventory.addCapacity(resource, capacity);
	}

	/**
	 * Removes the capacity of a particular resource.
	 *
	 * @param resource
	 * @param capacity
	 */
	public void removeCapacity(int resource, double capacity) {
		microInventory.removeCapacity(resource, capacity);
	}

	/**
	 * Gets the holder's unit instance.
	 *
	 * @return the holder's unit instance
	 */
	@Override
	public Unit getHolder() {
		return owner.getContainerUnit();
	}

	/**
	 * Does it have this item resource ?
	 *
	 * @param resource
	 * @return
	 */
	@Override
	public boolean hasItemResource(int resource) {
		return getItemResourceIDs().contains(resource);
	}

	/**
	 * Gets the remaining quantity of an item resource.
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public int getItemResourceRemainingQuantity(int resource) {
		return microInventory.getItemResourceRemainingQuantity(resource);
	}
}