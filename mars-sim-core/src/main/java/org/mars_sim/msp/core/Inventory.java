/*
 * Mars Simulation Project
 * Inventory.java
 * @date 2021-10-03
 * @author Scott Davis
 */
package org.mars_sim.msp.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

import org.mars_sim.msp.core.data.ResourceHolder;
import org.mars_sim.msp.core.equipment.Container;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.AmountResourceStorage;
import org.mars_sim.msp.core.resource.ItemResource;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.PhaseType;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Drone;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.core.vehicle.VehicleType;

/**
 * The Inventory class represents what a unit contains in terms of resources and
 * other units. It has methods for adding, removing and querying what the unit
 * contains.
 */
public class Inventory implements Serializable {

	/** default serial id. 	*/
	private static final long serialVersionUID = 123L;
	
	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(Inventory.class.getName());

	/** Comparison to indicate a small but non-zero amount. */
	public static final double SMALL_AMOUNT_COMPARISON = .000_000_1D;

	// Data members
	/** True if this inventory instance is for maven test tracking only. */
	private transient boolean testingTag = false;
	/** General mass capacity of inventory. */
	private double generalCapacity = 0D;	

	/** Collection of units in inventory. */
	private Collection<Integer> containedUnitIDs;
	
	/** Map of item resources. */
	private Map<Integer, Integer> containedItemResources = null;
	
	// Cache capacity variables.
	private transient Map<Integer, Double> capacityCache = null;
	private transient Map<Integer, Boolean> capacityCacheDirty = null;
	private transient Map<Integer, Double> containersCapacityCache = null;
	private transient Map<Integer, Boolean> containersCapacityCacheDirty = null;
	private transient Map<Integer, Double> storedCache = null;
	private transient Map<Integer, Boolean> storedCacheDirty = null;
	private transient Map<Integer, Double> containersStoredCache = null;
	private transient Map<Integer, Boolean> containersStoredCacheDirty = null;
	private transient Set<Integer> allStoredARCache = null;

	private transient boolean allStoredAmountResourcesCacheDirty = true;
	private transient double totalAmountResourcesStoredCache;
	private transient boolean totalAmountResourcesStoredCacheDirty = true;
//	
	private transient double itemResourceTotalMassCache;
	private transient boolean itemResourceTotalMassCacheDirty = true;
//	
	private transient double unitTotalMassCache;
	private transient boolean unitTotalMassCacheDirty = true;
	private transient double totalInventoryMassCache;
	private transient boolean totalInventoryMassCacheDirty = true;

	// Add 4 amount resource demand maps
	private Map<Integer, Integer> amountDemandTotalRequestMap = new ConcurrentHashMap<>();
	private Map<Integer, Integer> amountDemandMetRequestMap = new ConcurrentHashMap<>();
	private Map<Integer, Double> amountDemandMap = new ConcurrentHashMap<>();
	private Map<Integer, Double> amountDemandEstimatedMap = new ConcurrentHashMap<>();
	
	// Add 2 amount resource supply maps
	private Map<Integer, Double> amountSupplyMap = new ConcurrentHashMap<>();
	private Map<Integer, Integer> amountSupplyRequestMap = new ConcurrentHashMap<>();
	
	// Add 4 item resource demand maps
	private Map<Integer, Integer> itemDemandTotalRequestMap = new ConcurrentHashMap<>();
	private Map<Integer, Integer> itemDemandMetRequestMap = new ConcurrentHashMap<>();
	private Map<Integer, Integer> itemDemandMap = new ConcurrentHashMap<>();
	private Map<Integer, Integer> itemDemandEstimatedMap = new ConcurrentHashMap<>();
	
	// Add 2 item resource supply maps
	private Map<Integer, Integer> itemSupplyMap = new ConcurrentHashMap<>();
	private Map<Integer, Integer> itemSupplyRequestMap = new ConcurrentHashMap<>();
	
	/** The unit that owns this inventory. */
	private transient Unit owner;
	/** The unit onwer's ID. */
	private Integer ownerID;

	/** Resource storage. */
	private AmountResourceStorage resourceStorage;
	
	private static UnitManager unitManager;

	/**
	 * Constructor
	 * 
	 * @param owner the unit that owns this inventory
	 */
	public Inventory(Unit owner) {
		// Set owning unit.
		if (owner != null) {
			this.owner = owner;
			this.ownerID = (Integer) owner.getIdentifier();
		}
	}

	public int getAmountSupplyRequest(Integer r) {
		int result;

		if (amountSupplyRequestMap.containsKey(r)) {
			result = amountSupplyRequestMap.get(r);
		} else {
			amountSupplyRequestMap.put(r, 0);
			result = 0;
		}
		return result;
	}

	
	public double getAmountSupply(Integer r) {
		double result;
		
		if (amountSupplyMap.containsKey(r)) {
			result = amountSupplyMap.get(r);
		} else {
			amountSupplyMap.put(r, 0.0);
			result = 0.0;
		}
		return result;
	}

	public double getItemSupply(Integer r) {
		double result;
		
		if (itemSupplyMap.containsKey(r)) {
			result = itemSupplyMap.get(r);
		} else {
			itemSupplyMap.put(r, 0);
			result = 0.0;
		}
		return result;
	}
	
	public void addAmountSupply(Integer r, double amount) {
		if (amountSupplyMap.containsKey(r)) {
			double oldAmount = amountSupplyMap.get(r);
			amountSupplyMap.put(r, amount + oldAmount);
		} else {
			amountSupplyMap.put(r, amount);
		}

		addAmountSupplyRequest(r);
	}

	public void addItemSupply(Integer r, int amount) {
		if (itemSupplyMap.containsKey(r)) {
			int oldAmount = itemSupplyMap.get(r);
			itemSupplyMap.put(r, amount + oldAmount);
		} else {
			itemSupplyMap.put(r, amount);
		}

		addItemSupplyRequest(r);
	}
	
	private void addAmountSupplyRequest(Integer r) {
		if (amountSupplyRequestMap.containsKey(r)) {
			int oldNum = amountSupplyRequestMap.get(r);
			amountSupplyRequestMap.put(r, oldNum + 1);
		}
		else {
			amountSupplyRequestMap.put(r, 1);
		}
	}

	private void addItemSupplyRequest(Integer r) {
		if (itemSupplyRequestMap.containsKey(r)) {
			int oldNum = itemSupplyRequestMap.get(r);
			itemSupplyRequestMap.put(r, oldNum + 1);
		}
		else {
			itemSupplyRequestMap.put(r, 1);
		}
	}
	
	public double getAmountDemand(Integer r) {
		double result;

		if (amountDemandMap.containsKey(r)) {
			result = amountDemandMap.get(r);
		} else {
			amountDemandMap.put(r, 0.0);
			result = 0.0;
		}
		return result;
	}

	public double getItemDemand(Integer r) {
		double result;

		if (itemDemandMap.containsKey(r)) {
			result = itemDemandMap.get(r);
		} else {
			itemDemandMap.put(r, 0);
			result = 0.0;
		}
		return result;
	}
	
	public double getAmountDemandEstimated(Integer r) {
		double result;

		if (amountDemandEstimatedMap.containsKey(r)) {
			result = amountDemandEstimatedMap.get(r);
		} else {
			amountDemandEstimatedMap.put(r, 0.0);
			result = 0.0;
		}
		return result;
	}

	public double getItemDemandEstimated(Integer r) {
		double result;

		if (itemDemandEstimatedMap.containsKey(r)) {
			result = itemDemandEstimatedMap.get(r);
		} else {
			itemDemandEstimatedMap.put(r, 0);
			result = 0.0;
		}
		return result;
	}
	
	public int getAmountDemandTotalRequest(Integer r) {
		int result;
		if (amountDemandTotalRequestMap.containsKey(r)) {
			result = amountDemandTotalRequestMap.get(r);
		} else {
			amountDemandTotalRequestMap.put(r, 0);
			result = 0;
		}
		return result;
	}

	public int getItemDemandTotalRequest(Integer r) {
		int result;
		if (itemDemandTotalRequestMap.containsKey(r)) {
			result = itemDemandTotalRequestMap.get(r);
		} else {
			itemDemandTotalRequestMap.put(r, 0);
			result = 0;
		}
		return result;
	}

	public int getAmountDemandMetRequest(Integer r) {
		int result;
		if (amountDemandMetRequestMap.containsKey(r)) {
			result = amountDemandMetRequestMap.get(r);
		} else {
			amountDemandMetRequestMap.put(r, 0);
			result = 0;
		}
		return result;
	}

	public int getItemDemandMetRequest(Integer r) {
		int result;
		if (itemDemandMetRequestMap.containsKey(r)) {
			result = itemDemandMetRequestMap.get(r);
		} else {
			itemDemandMetRequestMap.put(r, 0);
			result = 0;
		}
		return result;
	}


	public void compactAmountSupplyMap(int sol) {
		compactAMap(amountSupplyMap, sol);
	}

	public void compactItemSupplyMap(int sol) {
		compactIMap(itemSupplyMap, sol);
	}

	public void clearAmountSupplyRequestMap() {
		amountSupplyRequestMap.clear();
	}

	public void clearItemSupplyRequestMap() {
		itemSupplyRequestMap.clear();
	}

	public void compactAmountDemandMap(int sol) {
		compactAMap(amountDemandMap, sol);
	}

	public void compactItemDemandMap(int sol) {
		compactIMap(itemDemandMap, sol);
	}
	
	private static void compactAMap(Map<Integer, Double> amountMap, int sol) {

		Map<Integer, Double> map = amountMap;

		for (Map.Entry<Integer, Double> entry : map.entrySet()) {
			Integer key = entry.getKey();
			double value = entry.getValue();
			value = value / sol;
			map.put(key, value);
		}
	}

	private static void compactIMap(Map<Integer, Integer> amountMap, int sol) {

		Map<Integer, Integer> map = amountMap;

		for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
			Integer key = entry.getKey();
			Integer value = entry.getValue();
			value = value / sol;
			map.put(key, value);
		}
	}
	
	public void clearAmountDemandTotalRequestMap() {
		amountDemandTotalRequestMap.clear();
	}

	public void clearItemDemandTotalRequestMap() {
		itemDemandTotalRequestMap.clear();
	}

	public void clearAmountDemandMetRequestMap() {
		amountDemandMetRequestMap.clear();
	}

	public void clearItemDemandMetRequestMap() {
		itemDemandMetRequestMap.clear();
	}

	public void addAmountDemandTotalRequest(int r, double amount) {
		// Record this demand request 
		if (amountDemandTotalRequestMap.containsKey(r)) {
			int oldNum = amountDemandTotalRequestMap.get(r);
			amountDemandTotalRequestMap.put(r, oldNum + 1);
		} else
			amountDemandTotalRequestMap.put(r, 1);
		// Record estimated demand
		if (amountDemandEstimatedMap.containsKey(r)) {
			double oldAmount = amountDemandEstimatedMap.get(r);
			amountDemandEstimatedMap.put(r, oldAmount + amount);
		} else
			amountDemandEstimatedMap.put(r, amount);
	}

	public void addItemDemandTotalRequest(int r, int num) {
		// Record this demand request 
		if (itemDemandTotalRequestMap.containsKey(r)) {
			int oldNum = itemDemandTotalRequestMap.get(r);
			itemDemandTotalRequestMap.put(r, oldNum + 1);
		} else
			itemDemandTotalRequestMap.put(r, 1);
		// Record estimated demand		
		if (itemDemandEstimatedMap.containsKey(r)) {
			int oldNum = itemDemandEstimatedMap.get(r);
			itemDemandEstimatedMap.put(r, oldNum + num);
		} else
			itemDemandEstimatedMap.put(r, num);
	}

	/**
	 * Adds the demand of this resource. It prompts for raising its value point
	 * (VP).
	 * 
	 * @param resource
	 * @param amount
	 */
	public void addAmountDemand(int r, double amount) {
		if (amountDemandMap.containsKey(r)) {
			double oldAmount = amountDemandMap.get(r);
			amountDemandMap.put(r, amount + oldAmount);

		} else {
			amountDemandMap.put(r, amount);
		}

		addAmountDemandMetRequest(r);
	}

	public void addItemDemand(int r, int number) {
		if (itemDemandMap.containsKey(r)) {
			int oldNumber = itemDemandMap.get(r);
			itemDemandMap.put(r, number + oldNumber);

		} else {
			itemDemandMap.put(r, number);
		}

		addItemDemandMetRequest(r);
	}
	
	private void addAmountDemandMetRequest(int r) {
		if (amountDemandMetRequestMap.containsKey(r)) {
			int oldNum = amountDemandMetRequestMap.get(r);
			amountDemandMetRequestMap.put(r, oldNum + 1);
		}

		else {
			amountDemandMetRequestMap.put(r, 1);
		}
	}

	private void addItemDemandMetRequest(int r) {
		if (itemDemandMetRequestMap.containsKey(r)) {
			int oldNum = itemDemandMetRequestMap.get(r);
			itemDemandMetRequestMap.put(r, oldNum + 1);
		}

		else {
			itemDemandMetRequestMap.put(r, 1);
		}
	}
	
	/**
	 * Adds capacity for a resource type.
	 * 
	 * @param resource the resource.
	 * @param capacity the extra capacity amount (kg).
	 */
	public void addAmountResourceTypeCapacity(AmountResource resource, double capacity) {
		addAmountResourceTypeCapacity(resource.getID(), capacity);  
	}

	/**
	 * Adds capacity for a resource type.
	 * 
	 * @param resource the resource.
	 * @param capacity the extra capacity amount (kg).
	 */
	public void addAmountResourceTypeCapacity(int resource, double capacity) {
		// Initialize amount resource capacity cache if necessary.
		if (capacityCache == null) {
			initializeAmountResourceCapacityCache();
		}
		// Set capacity cache to dirty because capacity values are changing.
		setAmountResourceCapacityCacheDirty(resource);
		// Initialize resource storage if necessary.
		if (resourceStorage == null) {
			resourceStorage = new AmountResourceStorage(owner);
		}
		resourceStorage.addAmountResourceTypeCapacity(resource, capacity);
	}

	/**
	 * Removes capacity for a resource type.
	 * 
	 * @param resource the resource.
	 * @param capacity the extra capacity amount (kg).
	 */
	public void removeAmountResourceTypeCapacity(AmountResource resource, double capacity) {
		removeAmountResourceTypeCapacity(resource.getID(), capacity);  
	}

	
	/**
	 * Removes capacity for a resource type.
	 * 
	 * @param resource the resource
	 * @param capacity the capacity amount (kg).
	 */
	public void removeAmountResourceTypeCapacity(int resource, double capacity) {
		// Initialize amount resource capacity cache if necessary.
//		if (capacityCache == null) {
//			initializeAmountResourceCapacityCache();
//		}
		// Set capacity cache to dirty because capacity values are changing.
		setAmountResourceCapacityCacheDirty(resource);

		if (resourceStorage != null) 
			resourceStorage.removeAmountResourceTypeCapacity(resource, capacity);
	}

	/**
	 * Adds capacity for a resource phase.
	 * 
	 * @param phase    the phase
	 * @param capacity the capacity amount (kg).
	 */
	public void addAmountResourcePhaseCapacity(PhaseType phase, double capacity) {
		// Set capacity cache to all dirty because capacity values are changing.
		setAmountResourceCapacityCacheAllDirty(false);
		// Initialize resource storage if necessary.
		if (resourceStorage == null) {
			resourceStorage = new AmountResourceStorage(owner);
		}
		resourceStorage.addAmountResourcePhaseCapacity(phase, capacity);
	}

	/**
	 * Checks if storage has capacity for an amount of a resource.
	 * 
	 * @param resource   the resource.
	 * @param amount     the amount (kg).
	 * @param allowDirty will allow dirty (possibly out of date) results.
	 * @return true if storage capacity.
	 */
	public boolean hasAmountResourceCapacity(int resource, double amount, boolean allowDirty) {

		if (amount < 0D) {
			throw new IllegalArgumentException("amount cannot be a negative value.");
		}
		return (getAmountResourceCapacityCacheValue(resource, allowDirty) >= amount);
	}

	/**
	 * Gets the storage capacity for a resource.
	 * 
	 * @param resource   the resource.
	 * @param allowDirty will allow dirty (possibly out of date) results.
	 * @return capacity amount (kg).
	 */
	public double getAmountResourceCapacity(AmountResource resource, boolean allowDirty) {
		return getAmountResourceCapacityCacheValue(resource.getID(), allowDirty);
	}

	/**
	 * Gets the storage capacity for a resource.
	 * 
	 * @param resource   the resource.
	 * @param allowDirty will allow dirty (possibly out of date) results.
	 * @return capacity amount (kg).
	 */
	public double getAmountResourceCapacity(int resource, boolean allowDirty) {
		return getAmountResourceCapacityCacheValue(resource, allowDirty);
	}

	/**
	 * Gets the storage capacity for a resource not counting containers.
	 * 
	 * @param resource the resource.
	 * @return capacity amount (kg).
	 */
	public double getAmountResourceCapacityNoContainers(AmountResource resource) {
		double result = 0D;

		if (resourceStorage != null) {// && !resourceStorage.isEmpty()) {
			result = resourceStorage.getAmountResourceCapacity(resource);
		}

		return result;
	}

	/**
	 * Gets the amount of a resource stored.
	 * 
	 * @param resource   the resource.
	 * @param allowDirty will allow dirty (possibly out of date) results.
	 * @return stored amount (kg).
	 */
	public double getAmountResourceStored(AmountResource resource, boolean allowDirty) {
		return getAmountResourceStoredCacheValue(resource.getID(), allowDirty);
	}

	/**
	 * Gets the amount of a resource stored.
	 * 
	 * @param resource   the resource.
	 * @param allowDirty will allow dirty (possibly out of date) results.
	 * @return stored amount (kg).
	 */
	public double getAmountResourceStored(int resource, boolean allowDirty) {
		return getAmountResourceStoredCacheValue(resource, allowDirty);
	}

	/**
	 * Gets a clone of all of the amount resources stored.
	 * 
	 * @param allowDirty will allow dirty (possibly out of date) results.
	 * @return set of amount resources.
	 */
	public Set<AmountResource> getAllAmountResourcesStored(boolean allowDirty) {
//		Set<AmountResource> s = ConcurrentHashMap.newKeySet();
//		s.addAll(getAllStoredAmountResourcesCache(allowDirty));
//		return s;
		return new HashSet<>(getAllStoredAmountResourcesCache(allowDirty));
	}

	/**
	 * Gets a clone of all of the amount resource id's stored.
	 * 
	 * @param allowDirty will allow dirty (possibly out of date) results.
	 * @return set of amount resource id's.
	 */
	public Set<Integer> getAllARStored(boolean allowDirty) {
//		logger.info(owner, "getAllStoredARCache(allowDirty)");
		return new HashSet<>(getAllStoredARCache(allowDirty));
//		Set<Integer> s = ConcurrentHashMap.newKeySet();
////		logger.info(owner, getAllStoredARCache(allowDirty).toString());
//		s.addAll(getAllStoredARCache(allowDirty));
//		return s;
	}

	/**
	 * Gets the total mass of amount resources stored.
	 * 
	 * @param allowDirty will allow dirty (possibly out of date) results.
	 * @return stored amount (kg).
	 */
	private double getTotalAmountResourcesStored(boolean allowDirty) {
		return getTotalAmountResourcesStoredCache(allowDirty);
	}

	/**
	 * Gets the remaining capacity available for a resource.
	 * 
	 * @param resource          the resource.
	 * @param useContainedUnits should the capacity of contained units be added?
	 * @param allowDirty        will allow dirty (possibly out of date) results.
	 * @return remaining capacity amount (kg).
	 */
	public double getAmountResourceRemainingCapacity(int resource, boolean useContainedUnits, boolean allowDirty) {

		double result = 0D;

		if (useContainedUnits) {
			double capacity = getAmountResourceCapacity(resource, allowDirty);
			double stored = getAmountResourceStored(resource, allowDirty);
			result += capacity - stored;
		} else if (resourceStorage != null) {// && !resourceStorage.isEmpty()) {
			result += resourceStorage.getARRemainingCapacity(resource);
		}

		// Check if remaining capacity exceeds container unit's remaining general
		// capacity.
		double containerUnitLimit = getContainerUnitGeneralCapacityLimit(allowDirty);
		if (result > containerUnitLimit) {
			result = containerUnitLimit;
		}

		return result;
	}

	/**
	 * Gets the remaining capacity available for a resource.
	 * 
	 * @param resource          the resource.
	 * @param useContainedUnits should the capacity of contained units be added?
	 * @param allowDirty        will allow dirty (possibly out of date) results.
	 * @return remaining capacity amount (kg).
	 */
	public double getAmountResourceRemainingCapacity(AmountResource resource, boolean useContainedUnits,
			boolean allowDirty) {
		return getAmountResourceRemainingCapacity(resource.getID(), useContainedUnits, allowDirty);
	}

	/**
	 * Store an amount of a resource.
	 * 
	 * @param resource          the resource.
	 * @param amount            the amount (kg).
	 * @param useContainedUnits
	 */
	public void storeAmountResource(AmountResource resource, double amount, boolean useContainedUnits) {
		storeAmountResource(resource.getID(), amount, useContainedUnits);
	}

	/**
	 * Store an amount of a resource.
	 * 
	 * @param resource          the resource.
	 * @param amount            the amount (kg).
	 * @param useContainedUnits
	 */
	public void storeAmountResource(int resource, double amount, boolean useContainedUnits) {

		if (amount < 0D) {
			logger.log(getOwner(), Level.SEVERE, 30_000,
					"Cannot store negative amount of resource: " 
					+ Math.round(amount*100.0)/100.0);
		}

		if (amount > 0D) {
			double remainingCapacity = getAmountResourceRemainingCapacity(resource, useContainedUnits, false);
			if (amount <= remainingCapacity) {

				// Set modified cache values as dirty.
				setAmountResourceCapacityCacheAllDirty(false);
				setAmountResourceStoredCacheAllDirty(false);
				setAllStoredAmountResourcesCacheDirty();
				setTotalAmountResourcesStoredCacheDirty();

				double remainingAmount = amount;
				double remainingStorageCapacity = 0D;
				if (resourceStorage != null) {//  && !resourceStorage.isEmpty()) {
					remainingStorageCapacity += resourceStorage.getARRemainingCapacity(resource);
				}
				
				// Not using containers
				if (!useContainedUnits && (remainingAmount > remainingStorageCapacity)) {
					// Check if local resource storage can hold resources
					logger.log(getOwner(), Level.WARNING, 30_000,
							ResourceUtil.findAmountResourceName(resource) 
							+ " could not be totally stored. Remaining: "
							+ Math.round((remainingAmount - remainingStorageCapacity)*100.0)/100.0);
				}

				// Store resource in local resource storage.
				double storageAmount = remainingAmount;
				if (storageAmount > remainingStorageCapacity) {
					storageAmount = remainingStorageCapacity;
				}
				if ((storageAmount > 0D) && (resourceStorage != null)) {
					resourceStorage.storeAmountResource(resource, storageAmount);
					remainingAmount -= storageAmount;
				}

				// Store remaining resource in containers using general capacity.
				if (useContainedUnits && (remainingAmount > 0D) && (containedUnitIDs != null)) {
					for (Integer uid : containedUnitIDs) {
						Unit unit = unitManager.getUnitByID(uid);
						// Use only contained units that implement container interface.
						if (unit instanceof ResourceHolder) {
							ResourceHolder rh = (ResourceHolder)unit;
							double remainingCap = rh.getAmountResourceRemainingCapacity(resource);
							double amountStored = remainingAmount;
							if (amountStored > remainingCap) {
								amountStored = remainingCap;
							}
							
							if (amountStored > 0D) {
								rh.storeAmountResource(resource, amountStored);
								remainingAmount -= amountStored;
							}
						}
					}
				}

				if (remainingAmount > SMALL_AMOUNT_COMPARISON) {
					logger.log(getOwner(), Level.WARNING, 30_000,
							ResourceUtil.findAmountResourceName(resource)
							+ " cannot be fully stored. Excess: " 
							+ Math.round(remainingAmount*100.0)/100.0 + " kg.");
				}
						
				// Fire inventory event.
				Unit o = getOwner();
				if (o != null) {
					o.fireUnitUpdate(UnitEventType.INVENTORY_RESOURCE_EVENT, resource);
				}
			} else {
				logger.log(getOwner(), Level.SEVERE, 30_000, 
						"Insufficient capacity to store "
						+ ResourceUtil.findAmountResourceName(resource) + ", capacity: "
						+ remainingCapacity
						+ ", attempted: " + amount + " kg");
			}
		}
	}

	/**
	 * Retrieves an amount of a resource from storage.
	 * 
	 * @param resource the resource.
	 * @param amount   the amount (kg).
	 */
	public void retrieveAllAmountResource(int resource) {
		retrieveAmountResource(resource, getAmountResourceStored(resource, false));
	}
	
	/**
	 * Retrieves an amount of a resource from storage.
	 * 
	 * @param resource the resource.
	 * @param amount   the amount (kg).
	 */
	public void retrieveAmountResource(int resource, double amount) {
		if (amount < 0D) {
			logger.log(getOwner(), Level.SEVERE, 30_000,
					"Cannot retrieve negative amount "
					+ "(" + amount + " kg) of "
					+ ResourceUtil.findAmountResourceName(resource));
		}

		if (amount > 0D) {

			if (amount <= getAmountResourceStored(resource, false)) {

				// Set modified cache values as dirty.
				setAmountResourceCapacityCacheAllDirty(false);
				setAmountResourceStoredCacheAllDirty(false);
				setAllStoredAmountResourcesCacheDirty();
				setTotalAmountResourcesStoredCacheDirty();

				double remainingAmount = amount;

				// Retrieve from local resource storage.
				double resourceStored = 0D;
				if (resourceStorage != null) {
					resourceStored += resourceStorage.getAmountResourceStored(resource);
				}
				double retrieveAmount = remainingAmount;
				if (retrieveAmount > resourceStored) {
					retrieveAmount = resourceStored;
				}
				if ((retrieveAmount > 0D) && (resourceStorage != null)) {
					resourceStorage.retrieveAmountResource(resource, retrieveAmount);
					remainingAmount -= retrieveAmount;
				}

				// Retrieve remaining resource from contained units.
				if (remainingAmount > 0D && containedUnitIDs != null && !containedUnitIDs.isEmpty()) {
					for (Integer uid : containedUnitIDs) {
						Unit unit = unitManager.getUnitByID(uid);					
						if ((unit.getUnitType() == UnitType.EQUIPMENT)
							 && (unit instanceof ResourceHolder)) {
							ResourceHolder rh = (ResourceHolder)unit;

							double retrievedAmount = remainingAmount;
							double containerAmount = rh.getAmountResourceStored(resource);
									
							if (retrievedAmount >= containerAmount) {
								retrievedAmount = containerAmount;
							}
							if (retrievedAmount > 0) {
								rh.retrieveAmountResource(resource, retrievedAmount);
								remainingAmount -= retrievedAmount;
							}
						}
					}
				}

				if (remainingAmount > SMALL_AMOUNT_COMPARISON) {
					logger.log(getOwner(), Level.SEVERE, 30_000,
							ResourceUtil.findAmountResourceName(resource)
							+ " could not be totally retrieved. Remaining: " + remainingAmount);
				}

				// Update caches.
				updateAmountResourceCapacityCache(resource);
				updateAmountResourceStoredCache(resource);

				// Fire inventory event.
				Unit o = getOwner();
				if (o != null) {
					o.fireUnitUpdate(UnitEventType.INVENTORY_RESOURCE_EVENT, resource);
				}
			} else {
				logger.log(getOwner(), Level.SEVERE, 30_000, 
						"Insufficient stored amount to retrieve "
						+ ResourceUtil.findAmountResourceName(resource) + ". Storage Amount : "
						+ getAmountResourceStored(resource, false) + " kg. Attempted Amount : " + amount + " kg");
			}
		}
	}

	/**
	 * Retrieves an amount of a resource from storage.
	 * 
	 * @param resource the resource.
	 * @param amount   the amount (kg).
	 */
	public void retrieveAmountResource(AmountResource resource, double amount) {
		retrieveAmountResource(resource.getID(), amount);
	}

	/**
	 * Adds a capacity to general capacity.
	 * 
	 * @param capacity amount capacity (kg).
	 */
	public void addGeneralCapacity(double capacity) {
		generalCapacity += capacity;
		// Mark amount resource capacity cache as dirty.
		setAmountResourceCapacityCacheAllDirty(false);
	}

	/**
	 * Gets the general capacity.
	 * 
	 * @return amount capacity (kg).
	 */
	public double getGeneralCapacity() {
		return generalCapacity;
	}

	/**
	 * Gets the mass stored in general capacity.
	 * 
	 * @param allowDirty will allow dirty (possibly out of date) results.
	 * @return stored mass (kg).
	 */
	public double getGeneralStoredMass(boolean allowDirty) {
		return getItemResourceTotalMass(allowDirty) + getUnitTotalMass(allowDirty);
	}

	/**
	 * Gets the remaining general capacity available.
	 * 
	 * @param allowDirty will allow dirty (possibly out of date) results.
	 * @return amount capacity (kg).
	 */
	public double getRemainingGeneralCapacity(boolean allowDirty) {
		double result = generalCapacity - getGeneralStoredMass(allowDirty);
		double containerUnitGeneralCapacityLimit = getContainerUnitGeneralCapacityLimit(allowDirty);
		if (result > containerUnitGeneralCapacityLimit) {
			result = containerUnitGeneralCapacityLimit;
		}
		return result;
	}

	/**
	 * Does the inventory have this item resource ?
	 * 
	 * @param id
	 * @return
	 */
	public boolean hasItemResource(int id) {
		boolean result = false;
		if ((containedItemResources != null) && containedItemResources.containsKey(id)) {
			if (containedItemResources.get(id) > 0) {
				result = true;
			}
		} else if (containedUnitIDs != null && !containedUnitIDs.isEmpty()) {
			Iterator<Integer> i = containedUnitIDs.iterator();
			while (!result && i.hasNext()) {
				Unit u = unitManager.getUnitByID(i.next());
				if (u.getUnitType() != UnitType.PERSON) {
					if (u.getInventory().hasItemResource(id)) {
						result = true;
					}
				}
			}
		}
		return result;
	}

	/**
	 * Gets the number of an item resource in storage.
	 * 
	 * @param resource the resource.
	 * @return number of resources.
	 */
	public int getItemResourceNum(int id) {
		int result = 0;
		if ((containedItemResources != null) && containedItemResources.containsKey(id)) {
			result += containedItemResources.get(id);
		}
		return result;
	}

	/**
	 * Gets the number of an item resource in storage.
	 * 
	 * @param resource the resource.
	 * @return number of resources.
	 */
	public int getItemResourceNum(ItemResource resource) {
		int result = 0;
		if ((containedItemResources != null) && containedItemResources.containsKey(resource.getID())) {
			result += containedItemResources.get(resource.getID());
		}
		return result;
	}

    /**
     * Gets a set of all the item resources in storage.
     * 
     * @return set of item resources.
     */
    public Set<ItemResource> getAllIRStored() {
		
		if (containedItemResources != null
				&& !containedItemResources.isEmpty()) {
			return containedItemResources.keySet().stream()
		        .map(ir -> ItemResourceUtil.findItemResource(ir))
		        .collect(java.util.stream.Collectors.toSet());
		}
		else {
			return new HashSet<>();
		}
    }

	/**
	 * Gets a set of all the item resources in storage.
	 * 
	 * @return set of item resources.
	 */
	public Set<Integer> getAllItemResourcesStored() {

		if (containedItemResources != null  && !containedItemResources.isEmpty()) {
			return new HashSet<>(containedItemResources.keySet());
		}
		else {
			return new HashSet<>();
		}				
	}

	/**
	 * Gets the total mass of item resources in storage.
	 * 
	 * @param allowDirty will allow dirty (possibly out of date) results.
	 * @return the total mass (kg).
	 */
	private double getItemResourceTotalMass(boolean allowDirty) {
		return getItemResourceTotalMassCache(allowDirty);
	}

	/**
	 * Stores item resources.
	 * 
	 * @param resource the resource to store.
	 * @param number   the number of resources to store.
	 */
	public void storeItemResources(int resource, int number) {

		if (number < 0) {
			throw new IllegalStateException("Cannot store negative number of resources.");
		}

		double totalMass = ItemResourceUtil.findItemResource(resource).getMassPerItem() * number;

		if (number > 0) {
			if (totalMass <= getRemainingGeneralCapacity(false)) {

				// Mark caches as dirty.
				setAmountResourceCapacityCacheAllDirty(false);
				setItemResourceTotalMassCacheDirty();

				// Initialize contained item resources if necessary.
				if (containedItemResources == null) {
					containedItemResources = new HashMap<Integer, Integer>();
				}

				int totalNum = number + getItemResourceNum(resource);
				if (totalNum > 0) {
					containedItemResources.put(resource, totalNum);
				}

				// Fire inventory event.
				Unit o = getOwner();
				if (o != null) {
					o.fireUnitUpdate(UnitEventType.INVENTORY_RESOURCE_EVENT, resource);
				}
			} else {
				throw new IllegalStateException("Could not store item resources.");
			}
		}
	}


	/**
	 * Retrieves item resources.
	 * 
	 * @param resource the resource to retrieve.
	 * @param number   the number of resources to retrieve.
	 */
	public void retrieveItemResources(int resource, int number) {

		if (number < 0) {
			throw new IllegalStateException("Cannot retrieve negative number of resources.");
		}
				
		if (number > 0) {
			if (number <= getItemResourceNum(resource)) {

				int remainingNum = number;

				// Mark caches as dirty.
				setAmountResourceCapacityCacheAllDirty(false);
				setItemResourceTotalMassCacheDirty();

				// Retrieve resources from local storage.
				if ((containedItemResources != null) && containedItemResources.containsKey(resource)) {
					int storedLocal = containedItemResources.get(resource);
					int retrieveNum = remainingNum;
					if (retrieveNum > storedLocal) {
						retrieveNum = storedLocal;
					}
					int remainingLocal = storedLocal - retrieveNum;
					if (remainingLocal > 0) {
						containedItemResources.put(resource, remainingLocal);
					} else {
						containedItemResources.remove(resource);
					}
					remainingNum -= retrieveNum;
				}

				// Fire inventory event.
				Unit o = getOwner();
				if (o != null) {
					o.fireUnitUpdate(UnitEventType.INVENTORY_RESOURCE_EVENT, resource);
				}

				if (remainingNum > 0) {
					throw new IllegalStateException(
							ItemResourceUtil.findItemResource(resource).getName() 
							+ " could not be totally retrieved. Remaining: " + remainingNum);
				}
			} else {
				throw new IllegalStateException("Insufficient stored number to retrieve " 
						+ ItemResourceUtil.findItemResource(resource).getName() 
						+ ", stored: " + getItemResourceNum(resource) + ", attempted: " + number);
			}
		}
	}

	/**
	 * Gets the total unit mass in storage.
	 * 
	 * @param allowDirty will allow dirty (possibly out of date) results.
	 * @return total mass (kg).
	 */
	private double getUnitTotalMass(boolean allowDirty) {
		return getUnitTotalMassCache(allowDirty);
	}

	/**
	 * Gets a collection of all the unused EVA suits.
	 * 
	 * @return Collection
	 */
	public Collection<EVASuit> getUnusedEVASuits() {
		Collection<EVASuit> result = new HashSet<>();
		if (containedUnitIDs != null && !containedUnitIDs.isEmpty()) {
			for (Integer uid : containedUnitIDs) {
				Equipment e = unitManager.getEquipmentByID(uid);
				if (e.getEquipmentType() == EquipmentType.EVA_SUIT) {
					if (e.getLastOwnerID() != -1)
						result.add((EVASuit)e);
				}
			}
		}
		return result;
	}
	
	/**
	 * Gets a collection of all the stored units.
	 * 
	 * @return Collection of all units
	 */ 
	public Collection<Unit> getContainedUnits() {
		Collection<Unit> result = new HashSet<>();
		if (containedUnitIDs != null && !containedUnitIDs.isEmpty()) {
			for (Integer uid : containedUnitIDs) {
				result.add(unitManager.getUnitByID(uid));
			}
		}
		return result;
	}

	/**
	 * Gets a collection of all the stored people.
	 * 
	 * @return Collection of all people
	 */
	public Collection<Person> getContainedPeople() {
		Collection<Person> result = new HashSet<>();
		if (containedUnitIDs != null && !containedUnitIDs.isEmpty()) {
			for (Integer uid : containedUnitIDs) {
				Person p = null;
				if (getOwner().getUnitType() == UnitType.SETTLEMENT) {
					p = ((Settlement)getOwner()).getAssociatedPerson(uid);
				}
				else if (getOwner().getUnitType() == UnitType.VEHICLE) {
					p = ((Vehicle)getOwner()).getAssociatedSettlement().getAssociatedPerson(uid);
				}
				else 
					p = unitManager.getPersonByID(uid);
				if (p != null)
					result.add(p);
			}
		}
		return result;
	}

	/**
	 * Counts the number of people.
	 * 
	 * @return the number of people contained
	 */
	public int getNumContainedPeople() {
		int result = 0;
		if (containedUnitIDs != null && !containedUnitIDs.isEmpty()) {
			for (Integer uid : containedUnitIDs) {
				Person p = null;
				if (getOwner().getUnitType() == UnitType.SETTLEMENT) {
					p = ((Settlement)getOwner()).getAssociatedPerson(uid);
				}
				else if (getOwner().getUnitType() == UnitType.VEHICLE) {
					p = ((Vehicle)getOwner()).getAssociatedSettlement().getAssociatedPerson(uid);
				}
				else 
					p = unitManager.getPersonByID(uid);
				if (p != null)
					result++;
			}
		}
		return result;
	}
	
	/**
	 * Gets a collection of all the stored robots.
	 * 
	 * @return Collection of all robots
	 */
	public Collection<Robot> getContainedRobots() {
		Collection<Robot> result = new HashSet<>();
		if (containedUnitIDs != null && !containedUnitIDs.isEmpty()) {
			for (Integer uid : containedUnitIDs) {
				Robot r = unitManager.getRobotByID(uid);
				if (r != null)
					result.add(r);
			}
		}
		return result;
	}
	
	/**
	 * Gets a number of robots.
	 * 
	 * @return a number of robots contained
	 */
	public int getNumContainedRobots() {
		int result = 0;
		if (containedUnitIDs != null && !containedUnitIDs.isEmpty()) {
			for (Integer uid : containedUnitIDs) {
				Robot r = unitManager.getRobotByID(uid);
				if (r != null)
					result++;
			}
		}
		return result;
	}
	
	/**
	 * Gets a collection of all the stored vehicles.
	 * 
	 * @return Collection of all vehicles
	 */
	public Collection<Vehicle> getContainedVehicles() {
		Collection<Vehicle> result = new HashSet<>();
		if (containedUnitIDs != null && !containedUnitIDs.isEmpty()) {
			for (Integer uid : containedUnitIDs) {
				Vehicle v = unitManager.getVehicleByID(uid);
				if (v != null)
					result.add(v);
			}
		}
		return result;
	}
	
	
	/**
	 * Gets a collection of all the stored drones.
	 * 
	 * @return Collection of all drones
	 */
	public Collection<Drone> getContainedDrones() {
		Collection<Drone> result = new HashSet<>();
		if (containedUnitIDs != null && !containedUnitIDs.isEmpty()) {
			for (Integer uid : containedUnitIDs) {
				Vehicle v = unitManager.getVehicleByID(uid);
				if (v != null && v.getVehicleType() == VehicleType.DELIVERY_DRONE)
					result.add((Drone)v);
			}
		}
		return result;
	}
	
	/**
	 * Gets the number of the stored vehicles.
	 * 
	 * @return number of vehicles
	 */
	public int getNumContainedVehicles() {
		int result = 0;
		if (containedUnitIDs != null && !containedUnitIDs.isEmpty()) {
			for (Integer uid : containedUnitIDs) {
				Vehicle v = unitManager.getVehicleByID(uid);
				if (v != null)
					result++;
			}
		}
		return result;
	}
	
	/**
	 * Gets a collection of all the stored units.
	 * 
	 * @return Collection of all units
	 */
	public Collection<Integer> getContainedUnitIDs() {
		Collection<Integer> result = null;
		if (containedUnitIDs != null && !containedUnitIDs.isEmpty()) {
			result = Collections.unmodifiableCollection(containedUnitIDs);
		} else {
			result = Collections.emptyList();
		}
		return result;
	}

	public Collection<Integer> getAllContainedUnitIDs() {
		return containedUnitIDs;
	}

	/**
	 * Checks if a unit is in storage.
	 * 
	 * @param unit the unit.
	 * @return true if unit is in storage.
	 */
	public boolean containsUnit(Unit unit) {
		if (containedUnitIDs != null 
				&& !containedUnitIDs.isEmpty()) {
			Integer uid = unit.getIdentifier();
			if (uid != null && containedUnitIDs.contains(uid)) {
				return true;
			}
		}
				
		return false;
	}

	/**
	 * Checks if any of a given class of unit is in storage.
	 * 
	 * @param unitClass the unit class.
	 * @return true if class of unit is in storage.
	 */
	private boolean containsUnitClassLocal(Class<? extends Unit> unitClass) {
		boolean result = false;
		if (containedUnitIDs != null) {
			Iterator<Unit> i = getContainedUnits().iterator();
			while (!result && i.hasNext()) {
				if (unitClass.isInstance(i.next())) {
					return true;
				}
			}
		}
		return result;
	}

	/**
	 * Checks if it contains a particular type of equipment.
	 * 
	 * @param type
	 * @return true if it contains the equipment type
	 */
	public boolean containsEquipment(EquipmentType type) {
		if (containedUnitIDs != null && !containedUnitIDs.isEmpty()) {
			for (Integer uid : containedUnitIDs) {
				Equipment e = unitManager.getEquipmentByID(uid);
				if (e != null && e.getEquipmentType() == type) {
					return true;
				}
			}
		}
		return false;
	}
		
	/**
	 * Checks if it contains an EVA suit.
	 * 
	 * @return true if it contains an EVA suit.
	 */
	public boolean containsEVASuit() {
		if (containedUnitIDs != null && !containedUnitIDs.isEmpty()) {
			for (Integer uid : containedUnitIDs) {
				Equipment e = unitManager.getEquipmentByID(uid);
				if (e != null && e.getEquipmentType() == EquipmentType.EVA_SUIT) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Checks if it contains a particular type of vehicle.
	 * 
	 * @param type
	 * @return true if it contains the vehicle type
	 */
	public boolean containsVehicleType(VehicleType type) {
		if (containedUnitIDs != null && !containedUnitIDs.isEmpty()) {
			for (Integer uid : containedUnitIDs) {
				Vehicle v = unitManager.getVehicleByID(uid);
				if (v != null && v.getVehicleType() == type) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Checks if any of a given class of unit is in storage.
	 * 
	 * @param unitClass the unit class.
	 * @return if class of unit is in storage.
	 */
	public boolean containsUnitClass(Class<? extends Unit> unitClass) {
		boolean result = false;
		// Check if unit of class is in inventory.
		if (containsUnitClassLocal(unitClass)) {
			return true;
		}
		return result;
	}

	
	/**
	 * Finds an unit of a given class in storage.
	 * 
	 * @param unitClass the unit class.
	 * @return the instance of the unit class or null if none.
	 */
	public Unit findUnitOfClass(Class<? extends Unit> unitClass) {
		if (containedUnitIDs != null && !containedUnitIDs.isEmpty()) {
			for (Integer uid : containedUnitIDs) {
				Unit unit = unitManager.getUnitByID(uid);
				if (unitClass.isInstance(unit)) {
					return unit;
				}
			}
		}
		return null;
	}

	/**
	 * Finds an  suit in storage, preferably the suit worn by the given person
	 * 
	 * @param Person person the owner of this EVA suit, if any
	 * @return the instance of EVAsuit or null if none.
	 */
	public EVASuit findAnEVAsuit(Person person) {
		EVASuit suit = null;
		if (containedUnitIDs != null && !containedUnitIDs.isEmpty()) {
			for (Integer uid : containedUnitIDs) {
				Equipment e = unitManager.getEquipmentByID(uid);
				if (e != null && e.getEquipmentType() == EquipmentType.EVA_SUIT) {
					suit = (EVASuit)e;
					if (suit.getLastOwnerID() != -1
						&& suit.getLastOwner().getName().equalsIgnoreCase(person.getName())) {
//						logger.log(person, Level.INFO, 4_000,
//								"1. findAnEVAsuit: " + person + " found " + suit);
						return suit;
					}
				}
			}
		}

		// Will return a brand new one if can't find a person's own suit
//		logger.log(person, Level.INFO, 4_000,
//				"2. findAnEVAsuit: " + person + " found " + suit);
		return suit;
	}
	
	/**
	 * Finds a container in storage.
	 * 
	 * @param empty does it need to be empty ?
	 * @return the instance of SpecimenBox or null if none.
	 */
	public Container findContainer(EquipmentType containerType, boolean empty, int resource) {
		if (containedUnitIDs != null && !containedUnitIDs.isEmpty()) {
			for (Integer uid : containedUnitIDs) {
				Equipment e = unitManager.getEquipmentByID(uid);
				if (e != null && e.getEquipmentType() == containerType) {
					Container c = (Container) e;
					int storedResource = c.getResource();
					if (empty) {
						// It must be empty inside
						if (c.getStoredMass() == 0D) {
							return c;
						}
					}
					else if (storedResource == resource || storedResource == -1)
						return c;
				}
			}
		}
		return null;
	}
	

	/**
	 * Does this inventory have a container for holding a particular resource in storage.
	 * 
	 * @param empty does it need to be empty ?
	 * @return the instance of container or null if none.
	 */
	public boolean hasAContainer(EquipmentType containerType, boolean empty, int resource) {
		if (containedUnitIDs != null && !containedUnitIDs.isEmpty()) {
			for (Integer uid : containedUnitIDs) {
				Equipment e = unitManager.getEquipmentByID(uid);
				if (e != null && e.getEquipmentType() == containerType) {
					Container c = (Container)e;
					int storedResource = c.getResource();
					if (empty) {
						// It must be empty inside
						if (c.getStoredMass() == 0D) {
							return true;
						}
					}
					else if (storedResource == resource || storedResource == -1)
						// resourceID = -1 means the container has not been initialized
						return true;
				}
			}
		}
		return false;
	}	
	
	/**
	 * Finds all the equipment with a particular type ID
	 * 
	 * @param typeID
	 * @return
	 */
	public Collection<Equipment> findAllEquipmentTypeID(int typeID) {	
		Collection<Equipment> result = new HashSet<Equipment>();
		if (containedUnitIDs != null && !containedUnitIDs.isEmpty()) {
			for (Integer uid : containedUnitIDs) {
				Equipment e = unitManager.getEquipmentByID(uid);
				if (e != null && 
					e.getEquipmentType() == EquipmentType.convertID2Type(typeID)) {
						result.add(e);
				}
			}
		}
		
		return result;
	}
			
	/**
	 * Finds all of the units of a class in storage.
	 * 
	 * @param unitClass the unit class.
	 * @return collection of units or empty collection if none.
	 */
	public <T extends Unit> Collection<Unit> findAllUnitsOfClass(Class<T> unitClass) {
		Collection<Unit> result = new HashSet<Unit>();
		if (containsUnitClass(unitClass)) {
			for (Unit unit : getContainedUnits()) {
				if (unitClass.isInstance(unit)) {
					result.add(unit);
				}
			}
		}
		return result;
	}

	/**
	 * Finds all equipment with a particular equipment type
	 * 
	 * @param type EquipmentType
	 * @return collection of equipment or empty collection if none.
	 */
	public Set<Equipment> findAllEquipmentType(EquipmentType type) {
		Set<Equipment> result = new HashSet<>();
		if (containedUnitIDs != null && !containedUnitIDs.isEmpty()) {
			for (Integer uid : containedUnitIDs) {
				Equipment e = unitManager.getEquipmentByID(uid);
				if (e != null && e.getEquipmentType() == type) 
					result.add(e);
			}
		}
		return result;
	}
	
	/**
	 * Finds all of the equipment.
	 * 
	 * @return collection of equipment or empty collection if none.
	 */
	public Collection<Equipment> findAllEquipment() {
		Collection<Equipment> result = new HashSet<>();
		if (containedUnitIDs != null && !containedUnitIDs.isEmpty()) {
			for (Integer uid : containedUnitIDs) {
				Equipment e = unitManager.getEquipmentByID(uid);
				if (e != null) 
					result.add(e);
			}
		}
		return result;
	}
	
	/**
	 * Finds all of the containers (excluding EVA suit).
	 * 
	 * @return collection of containers or empty collection if none.
	 */
	public Collection<Container> findAllContainers() {
		Collection<Container> result = new HashSet<>();
		if (containedUnitIDs != null && !containedUnitIDs.isEmpty()) {
			for (Integer uid : containedUnitIDs) {
				Equipment e = unitManager.getEquipmentByID(uid);
				if (e != null && (e instanceof Container)) 
					result.add((Container) e);
			}
		}
		return result;
	}
	

	/**
	 * Finds all of the EVA suits in storage.
	 * 
	 * @return collection of EVA suits or empty collection if none.
	 */
	public Collection<EVASuit> findAllEVASuits() {
		Collection<EVASuit> result = new HashSet<>();
		if (containedUnitIDs != null && !containedUnitIDs.isEmpty()) {
			for (Integer uid : containedUnitIDs) {
				Equipment e = unitManager.getEquipmentByID(uid);
				if (e != null && e.getEquipmentType() == EquipmentType.EVA_SUIT) {
					result.add((EVASuit)e);
				}	
			}
		}
		return result;
	}
	
	/**
	 * Finds all of the bags in storage.
	 * 
	 * @return collection of bags or empty collection if none.
	 */
	public Collection<Container> findAllContainers(EquipmentType containerType) {
		Collection<Container> result = new HashSet<>();
		if (containedUnitIDs != null && !containedUnitIDs.isEmpty()) {
			for (Integer uid : containedUnitIDs) {
				Equipment e = unitManager.getEquipmentByID(uid);
				if (e != null && e.getEquipmentType() == containerType) {
					result.add((Container)e);
				}	
			}
		}
		return result;
	}


	/**
	 * Find the number of equipment having the same type id
	 * 
	 * @param type
	 * @return
	 */
	public int findNumEquipment(int type) {
		int result = 0;

		if (containedUnitIDs != null && !containedUnitIDs.isEmpty()) {
			for (Integer uid : containedUnitIDs) {
				Equipment e = unitManager.getEquipmentByID(uid);
				if (e != null && e.getEquipmentType() == EquipmentType.convertID2Type(type)) {
					result++;
				}
			}
		}

		return result;
	}

	/**
	 * Finds the number of units of a class that are contained in storage.
	 * 
	 * @param unitClass the unit class.
	 * @return number of units
	 */
	public <T extends Unit> int findNumUnitsOfClass(Class<T> unitClass) {
		int result = 0;
		if (containsUnitClass(unitClass)) {
			for (Unit unit : getContainedUnits()) {
				if (unitClass.isInstance(unit)) {
					result++;
				}
			}
		}
		return result;
	}

	
	/**
	 * Finds the number of containers of a type that are contained in storage.
	 * 
	 * @param isEmpty    does it need to be empty ?
	 * @param brandNew  does it include brand new bag only
	 * @return number of specimen box
	 */
	public int findNumContainers(EquipmentType containerType, boolean isEmpty, boolean brandNew) {
		int result = 0;
		if (containedUnitIDs != null && !containedUnitIDs.isEmpty()) {
			for (Integer uid : containedUnitIDs) {
				Equipment e = unitManager.getEquipmentByID(uid);
				if (e != null && e.getEquipmentType() == containerType) {
					if (isEmpty) {
						// It must be empty inside
						if (e.isEmpty(brandNew)) {
							result++;
						}
					}
					else
						result++;
				}	
			}
		}
		return result;
	}

	/**
	 * Finds the number of EVA suits (may or may not have resources inside) that are contained in storage.
	 *  
	 * @param isEmpty    does it need to be empty ?
	 * @param allowDirty will allow dirty (possibly out of date) results.
	 * @return number of EVA suits
	 */
	public int findNumEVASuits(boolean isEmpty, boolean allowDirty) {
		int result = 0;
		if (containedUnitIDs != null && !containedUnitIDs.isEmpty()) {
			for (Integer uid : containedUnitIDs) {
				Equipment e = unitManager.getEquipmentByID(uid);
				if (e != null && e.getEquipmentType() == EquipmentType.EVA_SUIT) {
					if (isEmpty) {
						// It must be empty inside
						if (e.isEmpty(allowDirty)) {
							result++;
						}
					}
					else
						result++;
				}	
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
	public int findNumEmptyContainersOfType(EquipmentType containerType, boolean brandNew) {
		return findNumContainers(containerType, true, brandNew);
	}
	
	/**
	 * Finds the number of empty containers
	 * 
	 * @param typeID
	 * @param brandNew is it brand new (never been used) ? 
	 * @return number of empty containers
	 */
	public int findNumEmptyContainers(int typeID, boolean brandNew) {
		return findNumEmptyContainersOfType(EquipmentType.convertID2Type(typeID), brandNew);
	}

	/**
	 * Checks if a unit can be stored.
	 * 
	 * @param unit       the unit.
	 * @param allowDirty will allow dirty (possibly out of date) results.
	 * @return true if unit can be added to inventory
	 */
	public boolean canStoreUnit(Unit unit, boolean allowDirty) {
		boolean result = false;
		if (unit != null) {
			Unit owner = getOwner();
			double remain = getRemainingGeneralCapacity(allowDirty);
			double mass = unit.getMass();
			if (mass <= remain) {
				return true;
			} 
			
			else {
				logger.log(owner, unit, Level.SEVERE, 30_000, 
						"Had a mass of " + Math.round(mass*10.0)/10.0 
						  + " kg - too much to put on '"
						  + owner.getName() 
						  + "' to carry. Remaining Gen Cap : " 
						  +  Math.round(remain*10.0)/10.0
						  + " kg. (Gen Cap : " 
						  +  Math.round(getGeneralCapacity()*10.0)/10.0
						  + " kg)"
						);
				
				result = false;
			}
			Integer unitID = unit.getIdentifier(); 

			if (unitID.equals(ownerID)) {
				logger.log(unit, Level.SEVERE, 30_000,
						  unit.getName() + " was the same as its owner.");
				
				result = false;
			}
			
			if (containsUnit(unit)) {
				String ownerName = owner.getName();
				
				if (ownerName.equalsIgnoreCase("Mars Surface"))
					logger.log(unit, Level.SEVERE, 30_000,
						  "Already on " + ownerName);
				else
					logger.log(unit, Level.SEVERE, 30_000,
							  unit.getName() + " was already inside " + ownerName);

				result = true;
			}
					
			if (owner != null && unit.getUnitType() != UnitType.EQUIPMENT  
					&& unit.getInventory().containsUnit(owner)) {
				logger.log(unit, Level.SEVERE, 30_000,
						owner.getName() + " was owned by " + unit);
				
				result = false;
			}
		}
		return result;
	}

	/**
	 * Stores a unit.
	 * 
	 * @param unit the unit
	 */
	public boolean storeUnit(Unit unit) {
		boolean stored = true;
		
		if (canStoreUnit(unit, false)) {

			setUnitTotalMassCacheDirty();

			// Initialize containedUnitIDs if necessary.
			if (containedUnitIDs == null) {
				containedUnitIDs = new ConcurrentLinkedQueue<>();
			}

			containedUnitIDs.add(unit.getIdentifier());

			Unit owner = getOwner();

			if (owner != null) {
				if (ownerID != Unit.MARS_SURFACE_UNIT_ID) {
					// Set modified cache values as dirty.
					setAmountResourceCapacityCacheAllDirty(true);
					setAmountResourceStoredCacheAllDirty(true);
					setAllStoredAmountResourcesCacheDirty();
					setTotalAmountResourcesStoredCacheDirty();
					
					// Note: MarsSurface represents the whole surface of Mars does not have coordinates
					// If MarsSurface is a container of an unit, that unit may keep its own coordinates
					unit.setCoordinates(owner.getCoordinates());
					
					owner.fireUnitUpdate(UnitEventType.INVENTORY_STORING_UNIT_EVENT, unit);

					if (unit.getUnitType() == UnitType.EQUIPMENT) {
						if (unit instanceof ResourceHolder) {
							ResourceHolder rh = (ResourceHolder)unit;
							for (int resourceID : rh.getResourceIDs()) {
								owner.fireUnitUpdate(UnitEventType.INVENTORY_RESOURCE_EVENT, resourceID);
							}
						}
					}
					else if (unit.getUnitType() != UnitType.PERSON) {
						for (Integer resource : unit.getInventory().getAllARStored(false)) {
							updateAmountResourceCapacityCache(resource);
							updateAmountResourceStoredCache(resource);
							owner.fireUnitUpdate(UnitEventType.INVENTORY_RESOURCE_EVENT, resource);
						}
						for (Integer itemResource : unit.getInventory().getAllItemResourcesStored()) {
							owner.fireUnitUpdate(UnitEventType.INVENTORY_RESOURCE_EVENT, itemResource);
						}
					}
				}
				
				if (owner.getUnitType() == UnitType.SETTLEMENT) {
					// Try to empty amount resources into parent if container.
					if (unit.getUnitType() == UnitType.EQUIPMENT) {
						if (unit instanceof ResourceHolder) {
							ResourceHolder rh = (ResourceHolder)unit;
							for (int resourceID: rh.getResourceIDs()) {
								double containerAmount = rh.getAmountResourceStored(resourceID);
								if (containerAmount > 0 &&
									// From Owner
									getAmountResourceRemainingCapacity(resourceID, false, false) >= containerAmount) {
									// From holder
									rh.retrieveAmountResource(resourceID, containerAmount);
									// To Owner
									storeAmountResource(resourceID, containerAmount, false);
								}
							}
						}
					}
	
					else if (unit.getUnitType() == UnitType.PERSON) {
						((Settlement) owner).addPeopleWithin((Person)unit);
					}
				}

				unit.setContainerUnit(owner);
			}
			
		} else {
			logger.severe(unit, 30_000, "Could not be stored.");
			stored = false;
		}
		
		return stored;
	}

	/**
	 * Transfer the ownership of an unit from one owner to another.
	 * 
	 * @param unit the unit.
	 * @return true if successful
	 */
	public boolean transferUnit(Unit unit, Unit newOwner) {
		boolean transferred = false;
		if (retrieveUnit(unit, false)) {
			// Check if newOwner is a person 
			if (newOwner.getUnitType() == UnitType.PERSON) {
				transferred = ((Person)newOwner).addEquipment((Equipment)unit);
			}
			else {
				transferred = newOwner.getInventory().storeUnit(unit);
			}
			
			if (!transferred) {
				// The transfer has failed. 
				// Must return the Unit back to the origin.
				storeUnit(unit);
			}
		}
		
		return transferred;
	}

	/**
	 * Retrieves a unit from storage.
	 * 
	 * @param unit the unit.
	 */
	public void retrieveUnit(Unit unit) {
		retrieveUnit(unit, true);	
	}
	
	/**
	 * Retrieves an unit from storage.
	 * 
	 * @param unit the unit.
	 * @param retrieveOnly is it just a retrieval
	 */
	public boolean retrieveUnit(Unit unit, boolean retrieveOnly) {
		
		boolean retrieved = true;
		
		Integer uid = unit.getIdentifier();

		if ((containedUnitIDs != null) && containedUnitIDs.contains(uid)) {
			
			setUnitTotalMassCacheDirty();

			containedUnitIDs.remove(uid);

			// Update owner
			Unit owner = getOwner();
			if (owner != null) {
				owner.fireUnitUpdate(UnitEventType.INVENTORY_RETRIEVING_UNIT_EVENT, unit);

				if (ownerID != Unit.MARS_SURFACE_UNIT_ID) {
					// Set modified cache values as dirty.
					setAmountResourceCapacityCacheAllDirty(true);
					setAmountResourceStoredCacheAllDirty(true);
					setAllStoredAmountResourcesCacheDirty();
					setTotalAmountResourcesStoredCacheDirty();
								
					if (unit.getUnitType() == UnitType.EQUIPMENT) {
						if (unit instanceof ResourceHolder) {
							ResourceHolder rh = (ResourceHolder)unit;
							for (int resourceID : rh.getResourceIDs()) {
								owner.fireUnitUpdate(UnitEventType.INVENTORY_RESOURCE_EVENT, resourceID);
							}
						}
					}
					else if (unit.getUnitType() != UnitType.PERSON) {		
						for (Integer resource : unit.getInventory().getAllARStored(false)) {
							updateAmountResourceCapacityCache(resource);
							updateAmountResourceStoredCache(resource);
							owner.fireUnitUpdate(UnitEventType.INVENTORY_RESOURCE_EVENT, resource);
						}
						
						for (Integer resource : unit.getInventory().getAllItemResourcesStored()) {
							owner.fireUnitUpdate(UnitEventType.INVENTORY_RESOURCE_EVENT, resource);
						}
					}
				}
				
				if (owner.getUnitType() == UnitType.SETTLEMENT) {
					if (unit.getUnitType() == UnitType.PERSON) {
						// Retrieve this person from the settlement
						((Settlement) owner).removePeopleWithin((Person)unit);
					}
				}
			}
			
			if (!retrieveOnly) {
	            unit.setContainerUnit(null);
			}
		}

		else {
			Unit owner = getOwner();
			logger.warning(unit + " (" + uid + ") not found. "  
				+ owner  
				+ " (" 
				+ owner.getIdentifier() + ") possessed only "  + containedUnitIDs);		
			
			logger.log(unit, Level.SEVERE, 30_000, 
					"Could not be retrieved from " + owner.getName() + ".");
			retrieved = false;
		}
		
		return retrieved;
	}

	/**
	 * Sets the coordinates of all units in the inventory.
	 * 
	 * @param newLocation the new coordinate location
	 */
	public void setCoordinates(Coordinates newLocation) {
		if (ownerID != Unit.MARS_SURFACE_UNIT_ID) {
			if (containedUnitIDs != null && newLocation != null && !newLocation.equals(new Coordinates(0D, 0D))) {
				for (Unit unit : getContainedUnits()) {
					unit.setCoordinates(newLocation);
				}
			}
		}
	}

	/**
	 * Gets the total mass stored in inventory (does not include the weight of container itself)
	 * 
	 * @param allowDirty will allow dirty (possibly out of date) results.
	 * @return stored mass (kg).
	 */
	public double getTotalInventoryMass(boolean allowDirty) {
		return getTotalInventoryMassCache(allowDirty);
	}

	/**
	 * Checks if inventory is empty.
	 * 
	 * @param allowDirty will allow dirty (possibly out of date) results.
	 * @return true if empty.
	 */
	public boolean isEmpty(boolean allowDirty) {
		return (getTotalInventoryMass(allowDirty) == 0D);
	}

	/**
	 * Gets any limits in the owner's general capacity.
	 * 
	 * @param allowDirty will allow dirty (possibly out of date) results.
	 * @return owner general capacity limit (kg).
	 */
	private double getContainerUnitGeneralCapacityLimit(boolean allowDirty) {

		double result = Double.MAX_VALUE;
		
		Unit owner = getOwner();
		
		if (owner != null) {
			if (owner.getIdentifier() == Unit.MARS_SURFACE_UNIT_ID)
				return result;
			if (owner.getContainerID() == Unit.OUTER_SPACE_UNIT_ID)
				return result;
			if (owner.getUnitType() == UnitType.PLANET)
				return result;
			
			Unit cu = owner.getContainerUnit();
			Inventory containerInv = null;
			if (cu != null) {
				containerInv = cu.getInventory();

				if (containerInv.getRemainingGeneralCapacity(allowDirty) < result) {
					result = containerInv.getRemainingGeneralCapacity(allowDirty);
				}
	
				if (containerInv.getContainerUnitGeneralCapacityLimit(allowDirty) < result) {
					result = containerInv.getContainerUnitGeneralCapacityLimit(allowDirty);
				}
			}
		}

		return result;
	}

	/**
	 * Initializes the amount resource capacity cache.
	 */
	private void initializeAmountResourceCapacityCache() {
		//Collection<Integer> resources = ResourceUtil.getIDs(); // allStoredARCache;
		capacityCache = new ConcurrentHashMap<Integer, Double>();
		capacityCacheDirty = new ConcurrentHashMap<Integer, Boolean>();
		containersCapacityCache = new ConcurrentHashMap<Integer, Double>();
		containersCapacityCacheDirty = new ConcurrentHashMap<Integer, Boolean>();

//		if (resources != null)
//		for (int resource : resources) {
//			capacityCache.put(resource, 0D);
//			capacityCacheDirty.put(resource, true);
//			containersCapacityCache.put(resource, 0D);
//			containersCapacityCacheDirty.put(resource, true);
//		}
	}

	/**
	 * Checks if the amount resource capacity cache is dirty for a resource.
	 * 
	 * @param resource the resource to check.
	 * @return true if resource is dirty in cache.
	 */
	private boolean isAmountResourceCapacityCacheDirty(int resource) {
		// Initialize amount resource capacity cache if necessary.
		if (capacityCache == null) {
			initializeAmountResourceCapacityCache();
		}
		// Check if amountResourceCapacityCacheDirty contains the resource
		if (capacityCacheDirty.containsKey(resource)) {
			boolean value = capacityCacheDirty.get(resource);
			return value;
		}
		else
			return true;

	}

	/**
	 * Sets a resource in the amount resource capacity cache to dirty.
	 * 
	 * @param resource the dirty resource.
	 */
	private void setAmountResourceCapacityCacheDirty(int resource) {
		// Initialize amount resource capacity cache if necessary.
		if (capacityCache == null) {
			initializeAmountResourceCapacityCache();
		}

		capacityCacheDirty.put(resource, true);
	}

	/**
	 * Sets all of the resources in the amount resource capacity cache to dirty.
	 * 
	 * @param containersDirty true if containers cache should be marked as all
	 *                        dirty.
	 */
	private void setAmountResourceCapacityCacheAllDirty(boolean containersDirty) {
		// Initialize amount resource capacity cache if necessary.
		if (capacityCache == null) {
			initializeAmountResourceCapacityCache();
		}
		
		for (int amountResource : ResourceUtil.getIDs()) { //allStoredARCache
			setAmountResourceCapacityCacheDirty(amountResource);

			if (containersDirty) {
				containersCapacityCacheDirty.put(amountResource, true);
			}
		}

//		Unit owner = getOwner();
//		// Set owner unit's amount resource capacity cache as dirty (if any).
//		if (owner != null) {
//			if (owner.getIdentifier() == Unit.MARS_SURFACE_UNIT_ID)
//				return;
//			if (owner.getContainerID() == Unit.OUTER_SPACE_UNIT_ID)
//				return;
//			if (owner.getUnitType() == UnitType.PLANET)
//				return;
//				
//			setCacheDirty(0);
//		}
	}

	/**
	 * Gets the cached capacity value for an amount resource.
	 * 
	 * @param resource   the amount resource.
	 * @param allowDirty true if cache value can be dirty.
	 * @return capacity (kg) for the amount resource.
	 */
	private double getAmountResourceCapacityCacheValue(int resource, boolean allowDirty) {

		// Initialize amount resource capacity cache if necessary.
		if (capacityCache == null) {
			initializeAmountResourceCapacityCache();
		}

		// Update amount resource capacity cache if it is dirty.
		if (isAmountResourceCapacityCacheDirty(resource) && !allowDirty) {
			updateAmountResourceCapacityCache(resource);
		}

		// Check if amountResourceCapacityCache contains the resource
		return capacityCache.getOrDefault(resource, 0D);
	}

	/**
	 * Update the amount resource capacity cache for an amount resource.
	 * 
	 * @param resource the resource to update.
	 */
	private void updateAmountResourceCapacityCache(int resource) {

		// Initialize amount resource capacity cache if necessary.
		if (capacityCache == null) {
			initializeAmountResourceCapacityCache();
		}

		// Determine local resource capacity.
		double capacity = 0D;
		if (resourceStorage != null) {// && !resourceStorage.isEmpty()) {
//			logger.warning(getOwner() , 20_000L, " " + ResourceUtil.findAmountResource(resource) 
//				+ " resourceStorage::isEmpty: " + resourceStorage.isEmpty());
			capacity += resourceStorage.getAmountResourceCapacity(resource);
		}

		// Determine capacity for all contained units.
		double containedCapacity = 0D;
		// Check for null
		if (containersCapacityCacheDirty.containsKey(resource)) {
			if (containersCapacityCacheDirty.get((Integer)resource)) {
				if (containedUnitIDs != null && !containedUnitIDs.isEmpty()) {
					for (Integer uid : containedUnitIDs) {
						Unit u = unitManager.getUnitByID(uid);
						if (u.getUnitType() == UnitType.EQUIPMENT) {
							containedCapacity += ((Equipment)u).getAmountResourceCapacity(resource);
						}
						else if (u.getUnitType() != UnitType.PERSON) {
							containedCapacity += u.getInventory().getAmountResourceCapacity(resource, false);
						}
					}
				}
				containersCapacityCache.put(resource, containedCapacity);
				containersCapacityCacheDirty.put(resource, false);
			}
			// Check for null
			if (containersCapacityCache.containsKey(resource)) {
				containedCapacity = containersCapacityCache.get(resource);
			}
		}
		// Check for null
		else if (containersCapacityCache.containsKey(resource)) {
			containedCapacity = containersCapacityCache.get(resource);
		}

		// Determine stored resources for all contained units.
		double containedStored = 0D;
		if (containersStoredCache == null) {
			initializeAmountResourceStoredCache();
		}

		// Add checking for amountResourceContainersStoredCacheDirty and add if else
		// clause
//		logger.config(ResourceUtil.findAmountResource(resource) + " (id : " + resource + ")"); 
		
		if (containersStoredCacheDirty == null)
			containersStoredCacheDirty = new ConcurrentHashMap<>();
		
		if (containersStoredCacheDirty.containsKey(resource)) {
			// Check if containersStoredCacheDirty is null from another thread
			if (containersStoredCacheDirty.get(resource)) {				
				if (containedUnitIDs != null && !containedUnitIDs.isEmpty()) {
					for (Integer uid : containedUnitIDs) {
						Unit u = unitManager.getUnitByID(uid);
						if (u.getUnitType() == UnitType.EQUIPMENT) {
							containedStored +=  ((Equipment)u).getAmountResourceStored(resource);
						}
						else if (u.getUnitType() != UnitType.PERSON) {
							containedStored += u.getInventory().getAmountResourceCapacity(resource, false);
						}
					}
					containersStoredCache.put(resource, containedStored);
					containersStoredCacheDirty.put(resource, false);
				}
			}
			else {
				containedStored = containersStoredCache.get(resource);
			}
		}

		// Limit container capacity to this inventory's remaining general capacity.
		// Add container's resource stored as this is already factored into inventory's
		// remaining general capacity.
		double generalResourceCapacity = getRemainingGeneralCapacity(false) + containedStored;
		if (containedCapacity > generalResourceCapacity) {
			containedCapacity = generalResourceCapacity;
		}

		capacity += containedCapacity;

		capacityCache.put(resource, capacity);
		capacityCacheDirty.put(resource, false);
	}

	/**
	 * Initializes the amount resource stored cache.
	 */
	private void initializeAmountResourceStoredCache() {
		// Collection<Integer> resources = ResourceUtil.getIDs(); // allStoredARCache
		storedCache = new ConcurrentHashMap<Integer, Double>();
		storedCacheDirty = new ConcurrentHashMap<Integer, Boolean>();
		containersStoredCache = new ConcurrentHashMap<Integer, Double>();
		containersStoredCacheDirty = new ConcurrentHashMap<Integer, Boolean>();
//
//		for (int resource : resources) {
//			storedCache.put(resource, 0D);
//			storedCacheDirty.put(resource, true);
//			containersStoredCache.put(resource, 0D);
//			containersStoredCacheDirty.put(resource, true);
//		}
	}

	/**
	 * Checks if the amount resource stored cache is dirty for a resource.
	 * 
	 * @param resource the resource to check.
	 * @return true if resource is dirty in cache.
	 */
	private boolean isAmountResourceStoredCacheDirty(int resource) {
		// Initialize amount resource stored cache if necessary.
		if (storedCacheDirty == null) {
			initializeAmountResourceStoredCache();
		}

		// Check if storedCacheDirty contains the resource
		return storedCacheDirty.getOrDefault(resource, true);
	}

	/**
	 * Sets a resource in the amount resource stored cache to dirty.
	 * 
	 * @param resource the dirty resource.
	 */
	private void setAmountResourceStoredCacheDirty(int resource) {

		// Initialize amount resource stored cache if necessary.
		if (storedCache == null) {
			initializeAmountResourceStoredCache();
		}

		storedCacheDirty.put(resource, true);
	}

	/**
	 * Sets all of the resources in the amount resource stored cache to dirty.
	 * 
	 * @param containersDirty true if containers cache should be marked as all
	 *                        dirty.
	 */
	private void setAmountResourceStoredCacheAllDirty(boolean containersDirty) {

		// Initialize amount resource stored cache if necessary.
		if (storedCache == null) {
			initializeAmountResourceStoredCache();
		}

		for (int id : ResourceUtil.getIDs()) { //allStoredARCache
			setAmountResourceStoredCacheDirty(id);

			if (containersDirty) {
				containersStoredCacheDirty.put(id, true);
			}
		}
		
		// Set owner unit's amount resource stored cache as dirty (if any).
//		setCacheDirty(1);
	}

	/**
	 * Gets the cached stored value for an amount resource.
	 * 
	 * @param resource   the amount resource.
	 * @param allowDirty true if cache value can be dirty.
	 * @return stored amount (kg) for the amount resource.
	 */
	private double getAmountResourceStoredCacheValue(final int resource, final boolean allowDirty) {

		// Initialize amount resource stored cache if necessary.
		if (storedCache == null) {
			initializeAmountResourceStoredCache();
		}

		if (capacityCache == null) {
			initializeAmountResourceCapacityCache();
		}
		
		// Update amount resource stored cache if it is dirty.
		if (!allowDirty && isAmountResourceStoredCacheDirty(resource)) {
			updateAmountResourceStoredCache(resource);
		}

		// Check if storedCache contains the resource
		if (storedCache.containsKey(resource))
			return storedCache.get(resource);
		else {

			capacityCache.put(resource, 0D);
			return 0;
		}
	}

	/**
	 * Update the amount resource stored cache for an amount resource.
	 * 
	 * @param resource the resource to update.
	 */
	private void updateAmountResourceStoredCache(int resource) {

		double stored = 0D;

		if (resourceStorage != null) {
			stored += resourceStorage.getAmountResourceStored(resource);	
		}
	
		double containerStored = 0D;
		if (containersStoredCacheDirty == null)
			containersStoredCacheDirty = new ConcurrentHashMap<>();
		else if (containersStoredCacheDirty.containsKey(resource)) {
			if (containersStoredCacheDirty.get(resource)) {
				if (containedUnitIDs != null && !containedUnitIDs.isEmpty()) {
					for (Integer uid : containedUnitIDs) {
						Unit u = unitManager.getUnitByID(uid);
						if (u.getUnitType() == UnitType.EQUIPMENT) {
							containerStored += ((Equipment)u).getAmountResourceStored(resource);
						}
						else if (u.getUnitType() != UnitType.PERSON) {
							containerStored += u.getInventory().getAmountResourceStored(resource, false);
						}
					}	
				}
				containersStoredCache.put(resource, containerStored);
				containersStoredCacheDirty.put(resource, false);
				
			} else {
				containerStored = containersStoredCache.get(resource);
			}
		}
		// Add checking amountResourceContainersStoredCache
		else if (containersStoredCache.containsKey(resource)) {
			containerStored = containersStoredCache.get(resource);
		}

		stored += containerStored;

		storedCache.put(resource, stored);
		storedCacheDirty.put(resource, false);
	}

	/**
	 * Initializes the all stored amount resources cache.
	 */
	private void initializeAllStoredAmountResourcesCache() {
		initializeAllStoredARCache();
	}

	/**
	 * Initializes the all stored amount resources cache.
	 */
	private void initializeAllStoredARCache() {
		allStoredARCache = new HashSet<>(); //ConcurrentHashMap.newKeySet();
		allStoredAmountResourcesCacheDirty = true;
	}

	/**
	 * Sets the all stored amount resources cache as dirty.
	 */
	private void setAllStoredAmountResourcesCacheDirty() {
		setAllStoredARCacheDirty();
	}

	/**
	 * Sets the all stored amount resources cache as dirty.
	 */
	private void setAllStoredARCacheDirty() {

		// Update all stored amount resources cache if it hasn't been initialized.
		if (allStoredARCache == null) {
			initializeAllStoredAmountResourcesCache();
		}

		allStoredAmountResourcesCacheDirty = true;

		// Mark owner unit's all stored amount resources stored as dirty, if any.
//		setCacheDirty(2);
	}

	/**
	 * Gets the all stored amount resources cache.
	 * 
	 * @param allowDirty true if cache value can be dirty.
	 * @return all stored amount resources cache value.
	 */
	private Set<AmountResource> getAllStoredAmountResourcesCache(boolean allowDirty) {
		return getAllStoredARCache(allowDirty).stream()
	        .map(ar -> ResourceUtil.findAmountResource(ar))
	        .collect(java.util.stream.Collectors.toSet());
	}

	/**
	 * Gets the all stored amount resources cache.
	 * 
	 * @param allowDirty true if cache value can be dirty.
	 * @return all stored amount resources cache value.
	 */
	private Set<Integer> getAllStoredARCache(boolean allowDirty) {

		// Update all stored amount resources cache if it hasn't been initialized.
		if (allStoredARCache == null) {
			initializeAllStoredARCache();
		}

		if (allStoredAmountResourcesCacheDirty && !allowDirty) {
			updateAllStoredARCache();
		}

		return allStoredARCache;
	}

	/**
	 * Update the all stored amount resources cache as well as the container's cache
	 * if any.
	 */
	private void updateAllStoredARCache() {

		Set<Integer> tempAllStored = ConcurrentHashMap.newKeySet();

		if (resourceStorage != null) {// && !resourceStorage.isEmpty()) {
			tempAllStored.addAll(resourceStorage.getAllARStored(false));
		}

		if (containedUnitIDs != null && !containedUnitIDs.isEmpty()) {
			for (Integer uid : containedUnitIDs) {
				Equipment e = unitManager.getEquipmentByID(uid);
				
				if (e != null) {
					if (e.getEquipmentType() == EquipmentType.EVA_SUIT) {
						tempAllStored.addAll(((ResourceHolder)e).getResourceIDs());
					}
				}
				else {
					Unit u = unitManager.getUnitByID(uid);
					if (u.getUnitType() != UnitType.PERSON) {
						Set<Integer> set = u.getInventory().getAllARStored(false);
						tempAllStored.addAll(set);
					}
				}
			}
		}

		allStoredARCache = tempAllStored;
		allStoredAmountResourcesCacheDirty = false;
	}

	
	/**
	 * Sets the total amount resources stored cache as dirty.
	 */
	private void setTotalAmountResourcesStoredCacheDirty() {

		totalAmountResourcesStoredCacheDirty = true;

		// Set total inventory mass cache dirty as well.
		setTotalInventoryMassCacheDirty();

		// Mark owner unit's total resources stored as dirty, if any.
//		setCacheDirty(3);
	}

	/**
	 * Gets the total amount resource stored cache value.
	 * 
	 * @param allowDirty true if cache value can be dirty.
	 * @return total amount resources stored cache value.
	 */
	public double getTotalAmountResourcesStoredCache(boolean allowDirty) {

		// Update total amount resources stored cache if it is dirty.
		if (!allowDirty && totalAmountResourcesStoredCacheDirty) {
			updateTotalAmountResourcesStoredCache();
		}

		return totalAmountResourcesStoredCache;
	}

	/**
	 * Update the total amount resources stored cache as well as the container's
	 * cache if any.
	 */
	private void updateTotalAmountResourcesStoredCache() {

		double tempStored = 0D;
		if (resourceStorage != null) {// && !resourceStorage.isEmpty()) {
			tempStored += resourceStorage.getTotalAmountResourcesStored(false);
		}

		if (containedUnitIDs != null && !containedUnitIDs.isEmpty()) {
			for (Integer uid : containedUnitIDs) {
				Unit u = unitManager.getUnitByID(uid);
				if (u.getUnitType() == UnitType.EQUIPMENT) {
					tempStored = ((Equipment)u).getStoredMass();
				}
				else if (u.getUnitType() != UnitType.PERSON) {
					tempStored = u.getInventory().getTotalAmountResourcesStored(false);
				}
			}
		}

		totalAmountResourcesStoredCache = tempStored;
		totalAmountResourcesStoredCacheDirty = false;
	}

	/**
	 * Sets the item resource total mass cache as dirty.
	 */
	private void setItemResourceTotalMassCacheDirty() {

		itemResourceTotalMassCacheDirty = true;

		// Set total inventory mass cache dirty as well.
		setTotalInventoryMassCacheDirty();
	}

	/**
	 * Gets the total amount resource stored cache value.
	 * 
	 * @param allowDirty true if cache value can be dirty.
	 * @return total amount resources stored cache value.
	 */
	private double getItemResourceTotalMassCache(boolean allowDirty) {

		// Update item resource total mass cache if it is dirty.
		if (itemResourceTotalMassCacheDirty && !allowDirty) {
			updateItemResourceTotalMassCache();
		}

		return itemResourceTotalMassCache;
	}

	/**
	 * Update the item resource total mass cache.
	 */
	private void updateItemResourceTotalMassCache() {

		double tempMass = 0D;

		if (containedItemResources != null && !containedItemResources.isEmpty()) {
			Set<Entry<Integer, Integer>> es = containedItemResources.entrySet();
			Iterator<Entry<Integer, Integer>> i = es.iterator();
			while (i.hasNext()) {
				Entry<Integer, Integer> e = i.next();
//			for (Entry<Integer, Integer> e : es) {
				tempMass += e.getValue() * ItemResourceUtil.findItemResource(e.getKey()).getMassPerItem();
			}
		}

		itemResourceTotalMassCache = tempMass;
		itemResourceTotalMassCacheDirty = false;
	}

	/**
	 * Sets the unit total mass cache as dirty.
	 */
	private void setUnitTotalMassCacheDirty() {

		unitTotalMassCacheDirty = true;

		// Set total inventory mass cache dirty as well.
		setTotalInventoryMassCacheDirty();
	}

	/**
	 * Gets the unit total mass cache value.
	 * 
	 * @param allowDirty true if cache value can be dirty.
	 * @return unit total mass cache value.
	 */
	private double getUnitTotalMassCache(boolean allowDirty) {

		// Update unit total mass cache if it is dirty.
		if (!allowDirty && unitTotalMassCacheDirty) {
			updateUnitTotalMassCache();
		}

		return unitTotalMassCache;
	}

	/**
	 * Update the unit total mass cache.
	 */
	private void updateUnitTotalMassCache() {
		double tempMass = 0D;
		if (containedUnitIDs != null && !containedUnitIDs.isEmpty()) {
			for (Unit unit : getContainedUnits()) {
				tempMass += unit.getMass();
			}
		}
		unitTotalMassCache = tempMass;
		unitTotalMassCacheDirty = false;
	}

	/**
	 * Sets the total inventory mass cache as dirty.
	 */
	private void setTotalInventoryMassCacheDirty() {

		totalInventoryMassCacheDirty = true;

		// Set owner's unit total mass to dirty, if any.
//		setCacheDirty(4);
	}

	/**
	 * Gets the total inventory mass cache value.
	 * 
	 * @param allowDirty true if cache value can be dirty.
	 * @return total inventory mass cache value.
	 */
	private double getTotalInventoryMassCache(boolean allowDirty) {

		// Update total inventory mass cache if it is dirty.
		if (!allowDirty && totalInventoryMassCacheDirty) {
			updateTotalInventoryMassCache();
		}

		return totalInventoryMassCache;
	}

	/**
	 * Update the total inventory mass cache.
	 */
	private void updateTotalInventoryMassCache() {

		double tempMass = 0D;

		// Add total amount resource mass stored.
		tempMass += getTotalAmountResourcesStored(false);

		// Add general storage mass.
		tempMass += getGeneralStoredMass(false);

		totalInventoryMassCache = tempMass;
		totalInventoryMassCacheDirty = false;
	}

	/**
	 * Creates a clone of this inventory for testing the available capacity (not including the inventory contents).
	 * Note: used by LoadVehicleEVA and LoaedVehicleGarage
	 * 
	 * @param owner the unit owner of the inventory (or null).
	 * @return inventory clone.
	 */
	public Inventory clone(Unit owner) {

		Inventory result = new Inventory(owner);
		result.addGeneralCapacity(generalCapacity);

		if (resourceStorage != null) {// && !resourceStorage.isEmpty()) {
			for (Entry<Integer, Double> entry : resourceStorage.getAmountResourceTypeCapacities().entrySet()) {
				result.addAmountResourceTypeCapacity(entry.getKey(), entry.getValue());
			}
			for (Entry<PhaseType, Double> entry : resourceStorage.getAmountResourcePhaseCapacities().entrySet()) {
				result.addAmountResourcePhaseCapacity(entry.getKey(), entry.getValue());
			}
		}

		return result;
	}

	public Unit getOwner() {
		if (owner != null) {
			return owner;
		}
		
		if (ownerID != null) {	

			if (unitManager == null)
				unitManager = Simulation.instance().getUnitManager();
			
			if (unitManager != null) {
				
				if (ownerID == Unit.MARS_SURFACE_UNIT_ID) {
					owner = unitManager.getMarsSurface();
					return owner;
				}

				owner = unitManager.getUnitByID(ownerID);
				return owner;
			}
		}

		return null;
	}
	
	/**
	 * Gets the testing tag (for maven test only)
	 * 
	 * @return true if this inventory instance is for maven test
	 */
	public boolean getTestingTag() {
		return testingTag;
	}

	/**
	 * Sets the testing tag (for maven test only)
	 * 
	 * @param value
	 */
	public void setTestingTag(boolean value) {
		testingTag = value;
	}

//	/**
//	 * Sets the cache as dirty 
//	 * 
//	 * @param type
//	 */
//	public void setCacheDirty(int type) {
//		// Set owner unit's amount resource stored cache as dirty (if any).	
//		Unit owner = getOwner();
//		if (owner != null) {
//			if (owner.getIdentifier() == Unit.MARS_SURFACE_UNIT_ID)
//				return;
//			if (owner.getContainerID() == Unit.OUTER_SPACE_UNIT_ID)
//				return;
//			if (owner.getUnitType() == UnitType.PLANET)
//				return;
//			
//			Unit cu = owner.getContainerUnit();
//			if (cu != null) {
//		
//				if (type == 0)
//					cu.getInventory().setAmountResourceCapacityCacheAllDirty(true);
//				else if (type == 1)
//					cu.getInventory().setAmountResourceStoredCacheAllDirty(true);
//				else if (type == 2)
//					cu.getInventory().setAllStoredARCacheDirty();
//				else if (type == 3)
//					cu.getInventory().setTotalAmountResourcesStoredCacheDirty();
//				else if (type == 4)
//					cu.getInventory().setUnitTotalMassCacheDirty();
//			}
//		}
//	}
	
	public static void initializeInstances(UnitManager um) {
		unitManager = um;
	}
	
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {

        if (containedUnitIDs != null) 
        	containedUnitIDs.clear();
		containedUnitIDs = null;
        if (containedItemResources != null) 
        	containedItemResources.clear();
		containedItemResources = null;

		if (resourceStorage != null)
			resourceStorage.destroy();
		resourceStorage = null;

		capacityCache = null;
		capacityCacheDirty = null;
		storedCacheDirty = null;
		allStoredARCache = null;

		containersCapacityCache = null;
		containersCapacityCacheDirty = null;
		storedCache = null;
		containersStoredCache = null;
		containersStoredCacheDirty = null;
	}

	/**
	 * Implementing readObject method for serialization.
	 * 
	 * @param in the input stream.
	 * @throws IOException            if error reading from input stream.
	 * @throws ClassNotFoundException if error creating class.
	 */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {

		in.defaultReadObject();
		
		// Initialize transient variables that need it.
		allStoredAmountResourcesCacheDirty = true;
		totalAmountResourcesStoredCacheDirty = true;
		itemResourceTotalMassCacheDirty = true;
		unitTotalMassCacheDirty = true;
		totalInventoryMassCacheDirty = true;
	}
}
