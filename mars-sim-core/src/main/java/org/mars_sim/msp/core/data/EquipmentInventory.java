/*
 * Mars Simulation Project
 * EquipmentInventory.java
 * @date 2021-10-23
 * @author Barry Evans
 */

package org.mars_sim.msp.core.data;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.Container;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.equipment.EquipmentOwner;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ResourceUtil;

/**
 * The represents an Inventory that can hold Equipment as well as Resources. It provides
 * basic capacity management.
 * Resources can be retrieved from held Equipment. But resources can not be stored
 * in the underlying Equipment.
 */
public class EquipmentInventory
		implements EquipmentOwner, Serializable {

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
		microInventory = new MicroInventory(owner);

	}

	/**
	 * Gets the total mass of this inventory including the total mass of any held equipment
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
	 * Get the equipment set
	 *
	 * @return
	 */
	@Override
	public Set<Equipment> getEquipmentSet() {
//		if (equipmentSet == null)
//			equipmentSet = new UnitSet<>();
		return Collections.unmodifiableSet(equipmentSet);
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
			if (e != null && e instanceof Container) {
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
	 * Does this person possess an equipment of this equipment type
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
	 * Adds an equipment to this person
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
	 * Remove an equipment
	 *
	 * @param equipment
	 */
	@Override
	public boolean removeEquipment(Equipment equipment) {
		return equipmentSet.remove(equipment);
	}

	/**
	 * Stores the item resource
	 *
	 * @param resource the item resource
	 * @param quantity
	 * @return excess quantity that cannot be stored
	 */
	@Override
	public int storeItemResource(int resource, int quantity) {
		if (!microInventory.isResourceSupported(resource)) {
			microInventory.setCapacity(resource, getTotalCapacity());
		}

		return microInventory.storeItemResource(resource, quantity);
	}

	/**
	 * Retrieves the item resource
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
	 * Retrieves the resource
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

		if (shortfall > 0D) {
			for (Equipment e: equipmentSet) {
				if (e instanceof Container) {
					// Only take resources out of Containers;
					// other active Equipment, e.g. EVASuit need the resources to function
					shortfall = e.retrieveAmountResource(resource, shortfall);
					if (shortfall == 0D) {
						return 0D;
					}
				}
			}
		}

		// Return any missing quantity
		return shortfall;
	}

	/**
	 * Stores the resource
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
			microInventory.setCapacity(resource, cargoCapacity);
		}
		return microInventory.storeAmountResource(resource, quantity);
	}

	/**
	 * Gets the item resource stored
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public int getItemResourceStored(int resource) {
		return microInventory.getItemResourceStored(resource);
	}

	/**
	 * Gets the capacity of a particular amount resource
	 *
	 * @param resource
	 * @return capacity
	 */
	@Override
	public double getAmountResourceCapacity(int resource) {
		double result = 0;

		for (Equipment e: equipmentSet) {
			result += e.getAmountResourceCapacity(resource);
		}

		result += microInventory.getCapacity(resource);

		return result;
	}

	/**
	 * Obtains the remaining storage space of a particular amount resource
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public double getAmountResourceRemainingCapacity(int resource) {
		// Note: it does not include the general capacity (aka cargo capacity)
		return getAmountResourceCapacity(resource) - getAmountResourceStored(resource);
	}

	/**
	 * Obtains the remaining general storage space
	 *
	 * @return quantity
	 */
	@Override
	public double getRemainingCargoCapacity() {
		return cargoCapacity - getStoredMass();
	}

	/**
     * Gets the total capacity that this person can hold.
     *
     * @return total capacity (kg).
     */
	@Override
	public double getTotalCapacity() {
		// Question: Should the total capacity varies ?
		// based on one's instant carrying capacity ?
		return cargoCapacity;
	}

	/**
	 * Gets the amount resource stored
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public double getAmountResourceStored(int resource) {
		double result = microInventory.getAmountResourceStored(resource);

		for(Equipment e: equipmentSet) {
			if (e instanceof ResourceHolder) {
				result += e.getAmountResourceStored(resource);
			}
		}

		return result;
	}


	/**
	 * Finds the number of empty containers of a particular equipment
	 *
	 * @param containerType the equipment type.
	 * @param brandNew  does it include brand new bag only
	 * @return number of empty containers.
	 */
	@Override
	public int findNumEmptyContainersOfType(EquipmentType containerType, boolean brandNew) {
		int result = 0;
		Set<Equipment> set = new HashSet<>(equipmentSet);
		for (Equipment e : set) {
			// The contained unit has to be an Equipment that is empty and of the correct type
			if (e.isEmpty(brandNew) && (e.getEquipmentType() == containerType)) {
				result++;
			}
		}

		return result;
	}

	/**
	 * Finds the number of containers of a particular type
	 *
	 * @param containerType the equipment type.
	 * @return number of empty containers.
	 */
	@Override
	public int findNumContainersOfType(EquipmentType containerType) {
		int result = 0;
		for (Equipment e : equipmentSet) {
			// The contained unit has to be an Equipment that is empty and of the correct type
			if ((e.getEquipmentType() == containerType)) {
				result++;
			}
		}

		return result;
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
				if (resource == -1 || containerResource == resource || containerResource == -1)
				{
					if (!empty || (c.getStoredMass() == 0D)) {
						return c;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Gets all stored item resources
	 *
	 * @return all stored item resources.
	 */
	@Override
	public Set<Integer> getItemResourceIDs() {
		Set<Integer> set = new HashSet<>(microInventory.getItemResourceIDs());
		for (Equipment e: equipmentSet) {
			set.addAll(e.getItemResourceIDs());
		}

		return set;
	}

	/**
	 * Gets a set of resources in storage.
	 * @return  a set of resources
	 */
	@Override
	public Set<Integer> getAmountResourceIDs() {
		Set<Integer> set = new HashSet<Integer>();
		set.addAll(microInventory.getResourcesStored());
		for (Equipment e: equipmentSet) {
			if (e instanceof ResourceHolder) {
				set.addAll(((ResourceHolder) e).getAmountResourceIDs());
			}
		}

		return set;
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
	 * Add resource capacity
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
	 * Set the resource capacity
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
	 * Set the resource capacities.
	 *
	 * TODO should be keyed on resourceID not string.
	 * @param capacities
	 */
	public void setResourceCapacityMap(Map<String, Double> capacities) {
		for (Entry<String, Double> v : capacities.entrySet()) {
			AmountResource foundResource = ResourceUtil.findAmountResource(v.getKey());
			if (foundResource != null) {
				microInventory.setCapacity(foundResource.getID(), v.getValue());
			}
		}
	}

	/**
	 * Set the resource capacities.
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
	 * Adds to the cargo capacity (aka the general capacity)
	 *
	 * @param value
	 */
	public void addCargoCapacity(double value) {
		cargoCapacity += value;
	}

	/**
	 * Adds the capacity of a particular resource
	 *
	 * @param resource
	 * @param capacity
	 */
	public void addCapacity(int resource, double capacity) {
		microInventory.addCapacity(resource, capacity);
	}

	/**
	 * Removes the capacity of a particular resource
	 *
	 * @param resource
	 * @param capacity
	 */
	public void removeCapacity(int resource, double capacity) {
		microInventory.removeCapacity(resource, capacity);
	}

	/**
	 * Gets the holder's unit instance
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
}