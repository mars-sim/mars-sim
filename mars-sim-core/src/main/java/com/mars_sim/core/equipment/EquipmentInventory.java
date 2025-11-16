/*
 * Mars Simulation Project
 * EquipmentInventory.java
 * @date 2025-07-15
 * @author Barry Evans
 */

package com.mars_sim.core.equipment;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.mars_sim.core.Unit;
import com.mars_sim.core.EntityEventType;
import com.mars_sim.core.data.UnitSet;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.resource.AmountResource;
import com.mars_sim.core.resource.ResourceUtil;

/**
 * This class represents an Inventory that can hold equipment as well as resources. It provides
 * basic capacity management.
 */
public class EquipmentInventory
		implements EquipmentOwner, ItemHolder, BinHolder {

	private static final long serialVersionUID = 1L;

	private static final SimLogger logger = SimLogger.getLogger(EquipmentInventory.class.getName());

	private Unit owner;

	private double cargoCapacity;

	/** Locally held EVA suit set. */
	private Set<Equipment> suitSet;
	
	/** Locally held container set. */
	private Set<Equipment> containerSet;
	
	/** Locally held amount resource bin set. */
	private Set<AmountResourceBin> amountResourceBinSet;

	/** The MicroInventory instance. */
	private MicroInventory microInventory;

	/**
	 * Constructor.
	 * 
	 * @param owner
	 * @param cargoCapacity
	 */
	public EquipmentInventory(Unit owner, double cargoCapacity) {

		this.owner = owner;
		this.cargoCapacity = cargoCapacity;
	
		// Create equipment set
		suitSet = new UnitSet<>();
		containerSet = new UnitSet<>();
		
		// Create microInventory instance
		microInventory = new MicroInventory(owner, cargoCapacity);
		
		///////////// EXPERIMENTAL ONLY /////////////
		
		// Create the amount resource bin set
		amountResourceBinSet = new HashSet<>();
		
		var baskets = BinFactory.findBinMap(owner, amountResourceBinSet, BinType.BASKET);
		var crates = BinFactory.findBinMap(owner, amountResourceBinSet, BinType.CRATE);
		
		amountResourceBinSet.add(baskets);
		amountResourceBinSet.add(crates);
		//////////////////////////////////////////////
	}

	/**
	 * Gets the locally held amount resource bin set.
	 * 
	 * @return
	 */
	public Set<AmountResourceBin> getAmountResourceBinSet() {
		return amountResourceBinSet;
	}
	
	/**
	 * Gets the total mass (stored resource and base equipment) of this inventory.
	 * 
	 * @return
	 */
	@Override
	public double getStoredMass() {
		double result = 0;
		for (Equipment e: suitSet) {
			result += e.getMass();
		}
		for (Equipment e: containerSet) {
			result += e.getMass();
		}
		return result + microInventory.getStoredMass();
	}
	
	/**
	 * Prints the micro inventory stored mass.
	 *
	 * @return mass [kg]
	 */
	public void printMicroInventoryStoredMass() {
		microInventory.printStoredMass();
	}
	
	/**
	 * Gets the modified mass for a container. Useful when accounting for pushing a wheelbarrow, 
	 * instead of carrying a wheelbarrow.
	 * 
	 * @param type  the equipment type of interest
	 * @param percent the percent of mass of the equipment type to be treated as carrying mass
	 * @return
	 */
	public double getModifiedMass(EquipmentType type, double percent) {
		double result = 0;
		for (Equipment e: containerSet) {
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
		Set<Equipment> result = new HashSet<>(containerSet);
		result.addAll(suitSet);
		return Collections.unmodifiableSet(result);
	}

	/**
	 * Gets the container set.
	 *
	 * @return
	 */
	@Override
	public Set<Equipment> getContainerSet() {
		return Collections.unmodifiableSet(containerSet);
	}
	
	/**
	 * Gets the EVA suit set.
	 * 
	 * @return
	 */
	@Override
	public Set<Equipment> getSuitSet() {
		return suitSet;
	}

	/**
	 * Does this unit possess an equipment of this equipment type ?
	 *
	 * @param typeID
	 * @return
	 */
	@Override
	public boolean containsEquipment(EquipmentType type) {
		if (type == EquipmentType.EVA_SUIT) {
			if (suitSet.isEmpty())
				return false;
		}

		return containerSet.stream().anyMatch(e -> e.getEquipmentType() == type);
	}

	/**
	 * Adds an equipment to this unit.
	 *
	 * @param equipment
	 * @return true if this unit can carry it
	 */
	@Override
	public boolean addEquipment(Equipment equipment) {
		if (equipment.getEquipmentType() == EquipmentType.EVA_SUIT) {
			return addToSet(suitSet, equipment);
		}
		
		return addToSet(containerSet, equipment);
	}
	
	/**
	 * Adds the equipment (suit or container) to a particular equipment set.
	 * 
	 * @param set
	 * @param equipment
	 * @return true if this unit can carry it
	 */
	private boolean addToSet(Set<Equipment> set, Equipment equipment) {
		boolean contained = set.contains(equipment);
		if (!contained) {
			
			double suitMass = 0;
			for (Equipment e: suitSet) {
				suitMass += e.getMass();
			}
			
			double containerMass = 0;
			String containerName = "";
			
			for (Equipment e: containerSet) {
				Container c = (Container)e;
				Set<Integer> ids = c.getSpecificResourceStoredIDs();
				String arNames = "";
				for (int i: ids) {
					arNames += ResourceUtil.findAmountResourceName(i) 
							+ " (" + Math.round(c.getSpecificAmountResourceStored(i) * 100.0)/100.0 + ")";
				}
				containerName += e.getName() + " [" + arNames + "]";
				containerMass += e.getMass();
			}

			double microInvMass = microInventory.getStoredMass();
			
			double totalStored = suitMass + containerMass + microInvMass;
			
			double newCapacity = cargoCapacity - totalStored - equipment.getMass();
			if (newCapacity >= 0D) {
				owner.fireUnitUpdate(EntityEventType.INVENTORY_STORING_UNIT_EVENT, equipment);
				return set.add(equipment);
			}
			else {
				logger.warning(owner, 60_000L, "No capacity to hold " + equipment.getName()
								+ " - cargoCapacity: " + cargoCapacity 
								+ ", container name: " + containerName
								+ ", totalStored: " + totalStored 
								+ ", microInvMass: " + microInvMass
								+ ", containerMass: " + containerMass 
								+ ", suitMass: " + suitMass
								+ ", equipmentMass: " + equipment.getMass() 
								+ ".");
				return false;
			}
		}
		
		return !contained;
	}

	/**
	 * Removes an equipment.
	 *
	 * @param equipment
	 */
	@Override
	public boolean removeEquipment(Equipment equipment) {
		owner.fireUnitUpdate(EntityEventType.INVENTORY_RETRIEVING_UNIT_EVENT, equipment);
		if (equipment.getEquipmentType() == EquipmentType.EVA_SUIT) {
			return suitSet.remove(equipment);
		}
		return containerSet.remove(equipment);
	}


	@Override
	public boolean addBin(Bin bin) {
		var	binMap = BinFactory.findBinMap(owner, getAmountResourceBinSet(), bin.getBinType());
		
		binMap.addBin(bin);
		
		return true;
	}
	
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
	 * @return shortfall quantity that cannot be retrieved
	 */
	@Override
	public double retrieveAmountResource(int resource, double quantity) {
		double shortfall = quantity;
		double stored = microInventory.getSpecificAmountResourceStored(resource);
		if (stored > 0) {
			shortfall = microInventory.retrieveAmountResource(resource, shortfall);
		}

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
			microInventory.setSpecificCapacity(resource, cargoCapacity);
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
	 * Gets the specific capacity of a particular amount resource.
	 *
	 * @param resource
	 * @return capacity
	 */
	@Override
	public double getSpecificCapacity(int resource) {
		return microInventory.getSpecificCapacity(resource);
	}

	/**
	 * Obtains the specific capacity of remaining storage space for storing an amount resource.
	 * @apiNote This includes the stock capacity
	 *
	 * @param resource
	 * @return remaining capacity
	 */
	@Override
	public double getRemainingSpecificCapacity(int resource) {
		return microInventory.getRemainingSpecificCapacity(resource);

		// Warning : May also needs to account for the amount resources inside equipment
	}
	
	/**
	 * Obtains the combined capacity of remaining storage space for storing an amount resource.
	 * @apiNote This includes the stock capacity
	 *
	 * @param resource
	 * @return remaining capacity
	 */
	@Override
	public double getRemainingCombinedCapacity(int resource) {
		return microInventory.getRemainingCombinedCapacity(resource);

		// Warning : May also needs to account for the amount resources inside equipment
	}

	/**
	 * Does it have unused space or capacity for a particular resource ?
	 * 
	 * @param resource
	 * @return
	 */
	public boolean hasAmountResourceRemainingCapacity(int resource) {
		
		double cap = microInventory.getSpecificCapacity(resource);
		double stored = microInventory.getSpecificAmountResourceStored(resource);
		
		return (cap > stored);
		
		// Warning : May also needs to account for the amount resources inside equipment
	}
	
	
	/**
	 * Gets the stock capacity of its micro inventory.
	 *
	 * @return
	 */
	public double getStockCapacity() {
		return microInventory.getStockCapacity();
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
	 * Gets the specific amount resources stored, NOT including those inside equipment.
	 *
	 * @param resource
	 * @return amount
	 */
	@Override
	public double getSpecificAmountResourceStored(int resource) {
		return microInventory.getSpecificAmountResourceStored(resource);
	}

	/**
	 * Gets all the specific amount resources stored, including those inside equipment.
	 *
	 * @param resource
	 * @return amount
	 */
	@Override
	public double getAllSpecificAmountResourceStored(int resource) {
		double result = 0;
		// Do not consume resources from container Equipment. THis is an expensive
		// and can generate concurrency issues
		for (Equipment e: suitSet) {
		 	result += e.getSpecificAmountResourceStored(resource);
		 }
		for (Equipment e: containerSet) {
		 	result += e.getSpecificAmountResourceStored(resource);
		 }
		return result + getSpecificAmountResourceStored(resource);
	}

	/**
	 * Gets the quantity of all stock and specific amount resource stored.
	 *
	 * @param resource
	 * @return quantity
	 */
	public double getAllAmountResourceStored(int resource) {
		return microInventory.getAllAmountResourceStored(resource);
	}
	
	/**
	 * Finds the number of empty containers of a particular equipment type.
	 * 
	 * Note: NOT for EVA suits.
	 * 
	 * @param containerType the equipment type.
	 * @param brandNew  does it include brand new bag only
	 * @return number of empty containers.
	 */
	@Override
	public int findNumEmptyContainersOfType(EquipmentType containerType, boolean brandNew) {
		return (int) containerSet.stream().filter(e -> e.isEmpty(brandNew) && (e.getEquipmentType() == containerType))
								.count();
	}

	/**
	 * Finds the number of empty containers of a particular equipment type.
	 * 
	 * @param containerType
	 * @param brandNew
	 * @return
	 */
	public int findNumEmptyCopyContainersOfType(EquipmentType containerType, boolean brandNew) {
		return (int) containerSet.stream()
					.filter(e -> e.isEmpty(brandNew) && (e.getEquipmentType() == containerType))
					.count();
	}
	
	
	/**
	 * Finds the number of containers of a particular type.
	 *
	 * Note: will not count EVA suits.
	 * 
	 * @param containerType the equipment type.
	 * @return number of empty containers.
	 */
	@Override
	public int findNumContainersOfType(EquipmentType containerType) {
		return (int) containerSet.stream().filter(e -> e.getEquipmentType() == containerType).count();
	}
	
	/**
	 * Finds all of the containers of a particular type (excluding EVA suit).
	 *
	 * @return collection of containers or empty collection if none.
	 */
	public Collection<Container> findContainersOfType(EquipmentType type) {
		Collection<Container> result = new HashSet<>();
		for (Equipment e : containerSet) {
			if (type == e.getEquipmentType()) {
				result.add((Container)e);
			}
		}
		return result;
	}
	
	/**
	 * Finds a container in storage.
	 *
	 * Note: will not count EVA suits.
	 *
	 * @param containerType
	 * @param empty does it need to be empty ?
	 * @param resource If -1 then resource doesn't matter
	 * @return instance of container or null if none.
	 */
	@Override
	public Container findContainer(EquipmentType containerType, boolean empty, int resource) {
		for (Equipment e : containerSet) {
			if (e.getEquipmentType() == containerType) {
				 Container c = (Container)e;
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
	 * @return instance of container or null if none.
	 */
	public Container findOwnedContainer(EquipmentType containerType, int personId, int resource) {
		for (Equipment e : containerSet) {
			if (e.getEquipmentType() == containerType) {
				 Container c = (Container)e;
				// Check it matches the resource spec
				int containerResource = c.getResource();
				if (resource == -1 || containerResource == resource || containerResource == -1) {
					if (e.getRegisteredOwnerID() == personId) {
						return c;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Finds the number of empty bins of a particular bin type.
	 * 
	 * @param containerType the equipment type.
	 */
	@Override
	public int findNumBinsOfType(BinType binType) {
		for (AmountResourceBin arb : amountResourceBinSet) {
			if (arb.getBinType() == binType) {
				return arb.getBinMap().size();
			}
		}
		return 0;
	}
	
	/**
	 * Finds all of the bins of a particular type.
	 *
	 * @return collection of bins or empty collection if none.
	 */
	@Override
	public Collection<Bin> findBinsOfType(BinType binType){
		Collection<Bin> result = new HashSet<>();
		for (AmountResourceBin arb : amountResourceBinSet) {
			if (arb.getBinType() == binType) {
				result = arb.getBinMap().values();
			}
		}

		return result;
	}

	/**
	 * Gets a set of item resources in storage.
	 *
	 * @return a set of item resources.
	 */
	@Override
	public Set<Integer> getItemResourceIDs() {
		return microInventory.getItemStoredIDs();
	}

	/**
	 * Gets a set of amount resources in storage.
	 *
	 * @return a set of amount resources
	 */
	@Override
	public Set<Integer> getSpecificResourceStoredIDs() {
		return microInventory.getSpecificResourceStoredIDs();
	}

	/**
	 * Gets a set of IDs of the specific amount resources being stored, 
	 * including those in containers and EVA suit set.
	 * Ignore any IDs that are zero amount.
	 * 
	 * @return all stored amount resources.
	 */
	public Set<Integer> getAllAmountResourceStoredIDs() {
		Set<Integer> set = new HashSet<>(getSpecificResourceStoredIDs());
		for (Equipment e: containerSet) {
			if (e instanceof ResourceHolder rh) {
				set.addAll(rh.getSpecificResourceStoredIDs());
			}
		}
		for (Equipment e: suitSet) {
			if (e instanceof ResourceHolder rh) {
				set.addAll(rh.getSpecificResourceStoredIDs());
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
		if (!containerSet.isEmpty())
			return false;
		if (!suitSet.isEmpty())
			return false;
		return microInventory.isEmpty();
	}

	/**
	 * Sets a specific resource capacity.
	 *
	 * @param resource
	 * @param capacity
	 */
	public void setSpecificResourceCapacity(int resource, double capacity) {
		if (ResourceUtil.findAmountResource(resource) != null) {
			microInventory.setSpecificCapacity(resource, capacity);
		}
	}

	/**
	 * Sets the resource capacities.
	 *
	 * @param capacities
	 */
	public void setResourceCapacityMap(Map<Integer, Double> capacities) {
		setResourceCapacityMap(capacities, false);
	}

	/**
	 * Sets the resource capacities.
	 * 
	 * @param capacities
	 * @param add True if it should these be "added" on top of its existing capacity. False if it should be 'set' to a new capacity
	 */
	public void setResourceCapacityMap(Map<Integer, Double> capacities, boolean toAdd) {
		for (Entry<Integer, Double> v : capacities.entrySet()) {
			Integer foundResource = v.getKey();
			if (toAdd) {
				microInventory.addSpecificCapacity(foundResource, v.getValue());
			}
			else {
				microInventory.setSpecificCapacity(foundResource, v.getValue());
			}
		}
	}

	/**
	 * Sets the cargo/general/shared capacity.
	 *
	 * @param value
	 */
	public void setCargoCapacity(double value) {
		cargoCapacity = value;
 		microInventory.setStockCapacity(cargoCapacity);
	}
	
	/**
	 * Adds the cargo/general/shared capacity.
	 *
	 * @param value
	 */
	public void addCargoCapacity(double value) {
		cargoCapacity += value;
 		microInventory.addStockCapacity(cargoCapacity);
	}
	
	
	
	/**
	 * Adds the specific capacity of a particular resource.
	 *
	 * @param resource
	 * @param capacity
	 */
	public void addSpecificCapacity(int resource, double capacity) {
		microInventory.addSpecificCapacity(resource, capacity);
	}

	/**
	 * Removes the specific capacity of a particular resource.
	 *
	 * @param resource
	 * @param capacity
	 */
	public void removeSpecificCapacity(int resource, double capacity) {
		microInventory.removeSpecificCapacity(resource, capacity);
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
	 * Gets the remaining quantity (in integer) of an item resource that this inventory can store.
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public int getItemResourceRemainingQuantity(int resource) {
		return microInventory.getItemResourceRemainingQuantity(resource);
	}
	
	/**
	 * Checks if it has the container type.
	 * 
	 * @param type
	 * @return
	 */
	public boolean haveContainerType(BinType type) {
		for (AmountResourceBin c: amountResourceBinSet) {
			if (c.getBinType() == type) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks if it has the container type.
	 * 
	 * @param type
	 * @return
	 */
	public boolean haveContainerTypeResource(BinType type) {
		for (AmountResourceBin c: amountResourceBinSet) {
			if (c.getBinType() == type) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public double getAmountResourceStored(BinType type, int id, int resource) {
		for (AmountResourceBin c: amountResourceBinSet) {
			if (c.getBinType() == type) {
				AmountResource ar = ResourceUtil.findAmountResource(resource);
				if (ar != null && c.getBinMap().containsKey(id)) {
					return c.getBinMap().get(id).getAmount();
				}
			}
		}

		return -1;
	}

	@Override
	public double storeAmountResource(BinType type, int id, int resource, double quantity) {
		for (AmountResourceBin c: amountResourceBinSet) {
			if (c.getBinType() == type) {
				AmountResource ar = ResourceUtil.findAmountResource(resource);
				if (ar != null && c.getBinMap().containsKey(id)) {
					Bin mc = c.getBinMap().get(id);
					double existingAmount = mc.getAmount();
					double capacity = c.getCapacity();
					if (existingAmount + quantity > capacity) {
						mc.setAmount(capacity);
						return capacity - existingAmount - quantity;
					}
					else {
						mc.setAmount(existingAmount + quantity);
						return 0;
					}
				}
			}
		}
		
		return -1;
	}

	@Override
	public double retrieveAmountResource(BinType type, int id, int resource, double quantity) {
		for (AmountResourceBin c: amountResourceBinSet) {
			if (c.getBinType() == type) {
				AmountResource ar = ResourceUtil.findAmountResource(resource);
				if (ar != null && c.getBinMap().containsKey(id)) {
					Bin mc = c.getBinMap().get(id);
					double existingAmount = mc.getAmount();
					if (quantity > existingAmount) {
						mc.setAmount(0);
						return quantity - existingAmount;
					}
					else {
						mc.setAmount(existingAmount - quantity);
						return 0;
					}
				}
			}
		}
		
		return -1;
	}

	@Override
	public double getAmountResourceCapacity(BinType type, int id, int resource) {
		for (AmountResourceBin c: amountResourceBinSet) {
			if (c.getBinType() == type) {
				AmountResource ar = ResourceUtil.findAmountResource(resource);
				if (ar != null && c.getBinMap().containsKey(id)) {
					return c.getCapacity();
				}
			}
		}

		return 0;
	}

	@Override
	public double getAmountResourceRemainingCapacity(BinType type, int id, int resource) {
		for (AmountResourceBin c: amountResourceBinSet) {
			if (c.getBinType() == type) {
				AmountResource ar = ResourceUtil.findAmountResource(resource);
				if (ar != null && c.getBinMap().containsKey(id)) {
					Bin mc = c.getBinMap().get(id);
					double existingAmount = mc.getAmount();
					double capacity = c.getCapacity();
					if (existingAmount <= capacity) {
						return capacity - existingAmount;
					}
					else {
						return 0;
					}
				}
			}
		}
		
		return -1;
	}

	@Override
	public boolean hasAmountResourceRemainingCapacity(BinType type, int id, int resource) {
		for (AmountResourceBin c: amountResourceBinSet) {
			if (c.getBinType() == type) {
				AmountResource ar = ResourceUtil.findAmountResource(resource);
				if (ar != null && c.getBinMap().containsKey(id)) {
					Bin mc = c.getBinMap().get(id);
					double existingAmount = mc.getAmount();
					double capacity = c.getCapacity();
					if (existingAmount <= capacity) {
						return true;
					}
				}
			}
		}

		return false;
	}

	@Override
	public double getCargoCapacity(BinType type, int id) {
		for (AmountResourceBin c: amountResourceBinSet) {
			if (c.getBinType() == type) {
				if (c.getBinMap().containsKey(id)) {
					return c.getCapacity();
				}
			}
		}
		
		return 0;
	}

	@Override
	public int getAmountResource(BinType type, int id) {
		for (AmountResourceBin c: amountResourceBinSet) {
			if (c.getBinType() == type && c.getBinMap().containsKey(id)) {
				return c.getBinMap().get(id).getAmountResource().getID();
			}
		}
		
		return 0;
	}
	
	@Override
	public String getName() {
		return owner.getName();
	}

	@Override
	public String getContext() {
		return owner.getDescription();
	}	

	public void destroy() {
		containerSet.clear();
		containerSet = null;
		suitSet.clear();
		suitSet = null;
		microInventory = null;
	}
}