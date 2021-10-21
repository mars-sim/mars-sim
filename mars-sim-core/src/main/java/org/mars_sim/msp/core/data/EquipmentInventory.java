/*
 * Mars Simulation Project
 * EquipmentInventory.java
 * @date 2021-10-19
 * @author Barry Evans
 */

package org.mars_sim.msp.core.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.mars_sim.msp.core.equipment.Container;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.equipment.EquipmentOwner;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.logging.Loggable;
import org.mars_sim.msp.core.logging.SimLogger;
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
	private static final SimLogger logger = SimLogger.getLogger(EquipmentInventory.class.getName());
	
	private Loggable owner;
	
	private double cargoCapacity;
	
	/** Locally held Equipment **/
	private List<Equipment> equipmentList;

	/** The MicroInventory instance. */
	private MicroInventory microInventory;
	
	/**
	 * 
	 */
	public EquipmentInventory(Loggable owner, double cargoCapacity) {

		this.owner = owner;
		this.cargoCapacity = cargoCapacity;
		
		// Create equipment list instance		
		equipmentList = new ArrayList<>();
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
		for (Equipment e: equipmentList) {
			result += e.getMass();
		}
		return result +  microInventory.getStoredMass();
	}
	
	/**
	 * Get the equipment list
	 * 
	 * @return
	 */
	@Override
	public List<Equipment> getEquipmentList() {
		return Collections.unmodifiableList(equipmentList);
	}

	/**
	 * Does this person possess an equipment of this equipment type
	 * 
	 * @param typeID
	 * @return
	 */
	@Override
	public boolean containsEquipment(EquipmentType type) {
		for (Equipment e: equipmentList) {
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
		if (cargoCapacity >= getStoredMass() + equipment.getMass()) {
			equipmentList.add(equipment);
			return true;
		}
		return false;		
	}
	
	/**
	 * Remove an equipment 
	 * 
	 * @param equipment
	 */
	@Override
	public boolean removeEquipment(Equipment equipment) {
		if (equipmentList.contains(equipment)) {
			equipmentList.remove(equipment);
			return true;
		}
		return false;
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
			for (Equipment e: equipmentList) {
				if (e instanceof ResourceHolder)
				shortfall = ((ResourceHolder) e).retrieveAmountResource(resource, shortfall);
				if (shortfall == 0D) {
					return 0D;
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

		for (Equipment e: equipmentList) {
			result += e.getCapacity(resource);
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

		for (Equipment e: equipmentList) {
			if (e instanceof ResourceHolder) {
				result += ((ResourceHolder) e).getAmountResourceStored(resource);
			}
		}

		return result;
	}


	/**
	 * Finds the number of empty containers of a class that are contained in storage and have
	 * an empty inventory.
	 * 
	 * @param containerClass  the unit class.
	 * @param brandNew  does it include brand new bag only
	 * @return number of empty containers.
	 */
	@Override
	public int findNumEmptyContainersOfType(EquipmentType containerType, boolean brandNew) {
		int result = 0;
		for (Equipment e : equipmentList) {
			// The contained unit has to be an Equipment that is empty and of the correct type
			if ((e != null) && e.isEmpty(brandNew) && (e.getEquipmentType() == containerType)) {
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
		for (Equipment e : equipmentList) {
			if (e != null && e.getEquipmentType() == containerType) {
				Container c = (Container) e;
				if (empty) {
					// It must be empty inside
					if (c.getStoredMass() == 0D) {
						return c;
					}
				}
				else if (resource == -1 || c.getResource() == resource || c.getResource() == -1)
					return c;
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
		for (Equipment e: equipmentList) {
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
		for (Equipment e: equipmentList) {
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
		if (!equipmentList.isEmpty())
			return false;
		return microInventory.isEmpty();
	}

	/**
	 * Set the resource capacities.
	 * TODO should be keyed on resourceID not string.
	 * @param capacities
	 */
	public void setResourceCapacities(Map<String, Double> capacities) {
		for (Entry<String, Double> v : capacities.entrySet()) {
			AmountResource foundResource = ResourceUtil.findAmountResource(v.getKey());
			if (foundResource != null) {
				microInventory.setCapacity(foundResource.getID(), v.getValue());
			}
		}
	}	
	
//	/**
//	 * Sets the cargo capacity
//	 * 
//	 * @param value
//	 */
//	public void setCargoCapacity(double value) {
//		cargoCapacity = value;
//	}
}