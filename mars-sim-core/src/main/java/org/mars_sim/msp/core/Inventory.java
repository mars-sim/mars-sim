/**
 * Mars Simulation Project
 * Inventory.java
 * @version 3.06 2014-01-29
 * @author Scott Davis 
 */
package org.mars_sim.msp.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.mars_sim.msp.core.equipment.Container;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.AmountResourceStorage;
import org.mars_sim.msp.core.resource.ItemResource;
import org.mars_sim.msp.core.resource.Phase;

/** 
 * The Inventory class represents what a unit 
 * contains in terms of resources and other units.
 * It has methods for adding, removing and querying
 * what the unit contains.
 */
public class Inventory implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 12L;
	
	// Unit events
	public static final String INVENTORY_STORING_UNIT_EVENT = "inventory storing unit";
	public static final String INVENTORY_RETRIEVING_UNIT_EVENT = "inventory retrieving unit";
	public static final String INVENTORY_RESOURCE_EVENT = "inventory resource event";
	
	/** Comparison to indicate a small but non-zero amount. */
	private static final double SMALL_AMOUNT_COMPARISON = .0000001D;
	
	// Data members
	/** The unit that owns this inventory. */
	private Unit owner;
	/** Collection of units in inventory. */
	private Collection<Unit> containedUnits = null;
	/** Map of item resources. */
	private Map<ItemResource, Integer> containedItemResources = null;
	/** General mass capacity of inventory. */
	private double generalCapacity = 0D;
	/** Resource storage. */
	private AmountResourceStorage resourceStorage = new AmountResourceStorage();
	
	// Cache capacity variables.
	private transient Map<AmountResource, Double> amountResourceCapacityCache;
	private transient Map<AmountResource, Boolean> amountResourceCapacityCacheDirty;
	private transient Map<AmountResource, Double> amountResourceStoredCache;
	private transient Map<AmountResource, Boolean> amountResourceStoredCacheDirty;
	private transient Set<AmountResource> allStoredAmountResourcesCache;
	private transient boolean allStoredAmountResourcesCacheDirty = true;
	private transient double totalAmountResourcesStoredCache;
	private transient boolean totalAmountResourcesStoredCacheDirty = true;
	private transient double itemResourceTotalMassCache;
	private transient boolean itemResourceTotalMassCacheDirty = true;
	private transient double unitTotalMassCache;
	private transient boolean unitTotalMassCacheDirty = true;
	private transient double totalInventoryMassCache;
	private transient boolean totalInventoryMassCacheDirty = true;

    /** 
     * Constructor
     * @param owner the unit that owns this inventory
     */
    public Inventory(Unit owner) {
        // Set owning unit.
        this.owner = owner;
    }

    /**
     * Adds capacity for a resource type.
     * @param resource the resource.
     * @param capacity the capacity amount (kg).
     */
    public void addAmountResourceTypeCapacity(AmountResource resource, 
            double capacity) {

        // Set capacity cache to dirty because capacity values are changing.
        setAmountResourceCapacityCacheDirty(resource);
        // Initialize resource storage if necessary.
        if (resourceStorage == null) {
            resourceStorage = new AmountResourceStorage();
        }
        resourceStorage.addAmountResourceTypeCapacity(resource, capacity);
    }
    
    /**
     * Removes capacity for a resource type.
     * @param resource the resource
     * @param capacity the capacity amount (kg).
     */
    public void removeAmountResourceTypeCapacity(AmountResource resource,
            double capacity) {
        
        // Set capacity cache to dirty because capacity values are changing.
        setAmountResourceCapacityCacheDirty(resource);
        // Initialize resource storage if necessary.
        if (resourceStorage == null) {
            resourceStorage = new AmountResourceStorage();
        }
        resourceStorage.removeAmountResourceTypeCapacity(resource, capacity);
    }

    /**
     * Adds capacity for a resource phase.
     * @param phase the phase
     * @param capacity the capacity amount (kg).
     */
    public void addAmountResourcePhaseCapacity(Phase phase, double capacity) {
        // Set capacity cache to all dirty because capacity values are changing.
        setAmountResourceCapacityCacheAllDirty();
        // Initialize resource storage if necessary.
        if (resourceStorage == null) {
            resourceStorage = new AmountResourceStorage();
        }
        resourceStorage.addAmountResourcePhaseCapacity(phase, capacity);
    }

    /**
     * Checks if storage has capacity for a resource.
     * @param resource the resource.
     * @param allowDirty will allow dirty (possibly out of date) results.
     * @return true if storage capacity.
     */
    public boolean hasAmountResourceCapacity(AmountResource resource, boolean allowDirty) {
        if (resource == null) {
            throw new IllegalArgumentException("resource cannot be null.");
        }
        return (getAmountResourceCapacityCacheValue(resource, allowDirty) > 0D);
    }

    /**
     * Checks if storage has capacity for an amount of a resource.
     * @param resource the resource.
     * @param amount the amount (kg).
     * @param allowDirty will allow dirty (possibly out of date) results.
     * @return true if storage capacity.
     */
    public boolean hasAmountResourceCapacity(AmountResource resource, double amount, 
            boolean allowDirty) {

        if (resource == null) {
            throw new IllegalArgumentException("resource cannot be null.");
        }
        if (amount < 0D) {
            throw new IllegalArgumentException("amount cannot be a negative value.");
        }
        return (getAmountResourceCapacityCacheValue(resource, allowDirty) >= amount);
    }

    /**
     * Gets the storage capacity for a resource.
     * @param resource the resource.
     * @param allowDirty will allow dirty (possibly out of date) results.
     * @return capacity amount (kg).
     */
    public double getAmountResourceCapacity(AmountResource resource, boolean allowDirty) {
/*
        if (resource == null) {
            throw new IllegalArgumentException("resource cannot be null.");
        }
*/
        return getAmountResourceCapacityCacheValue(resource, allowDirty);
    }

    /**
     * Gets the amount of a resource stored.
     * @param resource the resource.
     * @param allowDirty will allow dirty (possibly out of date) results.
     * @return stored amount (kg).
     */
    public double getAmountResourceStored(AmountResource resource,boolean allowDirty) {
/*
        if (resource == null) {
            throw new IllegalArgumentException("resource is null");
        }
*/
        return getAmountResourceStoredCacheValue(resource, allowDirty);
    }

    /**
     * Gets all of the amount resources stored.
     * @param allowDirty will allow dirty (possibly out of date) results.
     * @return set of amount resources.
     */
    public Set<AmountResource> getAllAmountResourcesStored(boolean allowDirty) {
        return new HashSet<AmountResource>(getAllStoredAmountResourcesCache(allowDirty));
    }

    /**
     * Gets the total mass of amount resources stored.
     * @param allowDirty will allow dirty (possibly out of date) results.
     * @return stored amount (kg).
     */
    private double getTotalAmountResourcesStored(boolean allowDirty) {
        return getTotalAmountResourcesStoredCache(allowDirty);
    }

    /**
     * Gets the remaining capacity available for a resource.
     * @param resource the resource.
     * @param useContainedUnits should the capacity of contained units be added?
     * @param allowDirty will allow dirty (possibly out of date) results.
     * @return remaining capacity amount (kg).
     */
    public double getAmountResourceRemainingCapacity(AmountResource resource, 
            boolean useContainedUnits, boolean allowDirty) {

        double result = 0D;

        if (useContainedUnits) {
            double capacity = getAmountResourceCapacity(resource, allowDirty);
            double stored = getAmountResourceStored(resource, allowDirty);
            result += capacity - stored;
        } else if (resourceStorage != null) {
            result += resourceStorage.getAmountResourceRemainingCapacity(resource);
        }

        // Check if remaining capacity exceeds container unit's remaining general capacity.
        double containerUnitLimit = getContainerUnitGeneralCapacityLimit(allowDirty);
        if (result > containerUnitLimit) {
            result = containerUnitLimit;
        }
        
        return result;
    }

    /**
     * Store an amount of a resource.
     * @param resource the resource.
     * @param amount the amount (kg).
     */
    public void storeAmountResource(AmountResource resource, double amount,
            boolean useContainedUnits) {

        if (amount < 0D) {
            throw new IllegalStateException("Cannot store negative amount of resource: " + amount);
        }

        if (amount > 0D) {

            if (amount <= getAmountResourceRemainingCapacity(resource, useContainedUnits, false)) {

                // Set modified cache values as dirty.
                setAmountResourceCapacityCacheAllDirty();
                setAmountResourceStoredCacheAllDirty();
                setAllStoredAmountResourcesCacheDirty();
                setTotalAmountResourcesStoredCacheDirty();

                double remainingAmount = amount;
                double remainingStorageCapacity = 0D;
                if (resourceStorage != null) {
                    remainingStorageCapacity += resourceStorage.getAmountResourceRemainingCapacity(resource);
                }

                // Check if local resource storage can hold resources if not using contained units.
                if (!useContainedUnits && (remainingAmount > remainingStorageCapacity)) {
                    throw new IllegalStateException(resource.getName()
                            + " could not be totally stored. Remaining: " + (remainingAmount - 
                                    remainingStorageCapacity));
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

                // Store remaining resource in contained units in general capacity.
                if (useContainedUnits && (remainingAmount > 0D) && (containedUnits != null)) {
                    Iterator<Unit> i = containedUnits.iterator();
                    while (i.hasNext()) {
                        // Use only contained units that implement container interface.
                        Unit unit = i.next();
                        if (unit instanceof Container) {
                            Inventory unitInventory = unit.getInventory();
                            double remainingUnitCapacity = unitInventory.getAmountResourceRemainingCapacity(
                                    resource, false, false);
                            double unitStorageAmount = remainingAmount;
                            if (unitStorageAmount > remainingUnitCapacity) {
                                unitStorageAmount = remainingUnitCapacity;
                            }
                            if (unitStorageAmount > 0D) {
                                unitInventory.storeAmountResource(resource, unitStorageAmount, false);
                                remainingAmount -= unitStorageAmount;
                            }
                        }
                    }
                }

                if (remainingAmount > SMALL_AMOUNT_COMPARISON) {
                    throw new IllegalStateException(resource.getName()
                            + " could not be totally stored. Remaining: " + remainingAmount);
                }

                // Fire inventory event.
                if (owner != null) {
                    owner.fireUnitUpdate(INVENTORY_RESOURCE_EVENT, resource);
                }
            } else {
                throw new IllegalStateException("Insufficiant capacity to store " + resource.getName() + 
                        ", capacity: " + getAmountResourceRemainingCapacity(resource, useContainedUnits, 
                                false) + ", attempted: " + amount);
            }
        }
    }

    /**
     * Retrieves an amount of a resource from storage.
     * @param resource the resource.
     * @param amount the amount (kg).
     */
    public void retrieveAmountResource(AmountResource resource, double amount) {

        if (amount < 0D) {
            throw new IllegalStateException("Cannot retrieve negative amount of resource: " + amount);
        }

        if (amount > 0D) {

            if (amount <= getAmountResourceStored(resource, false)) {

                // Set modified cache values as dirty.
                setAmountResourceCapacityCacheAllDirty();
                setAmountResourceStoredCacheAllDirty();
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
                if ((remainingAmount > 0D) && (containedUnits != null)) {
                    Iterator<Unit> i = containedUnits.iterator();
                    while (i.hasNext()) {
                        Unit unit = i.next();
                        if (unit instanceof Container) {
                            Inventory unitInventory = unit.getInventory();
                            double unitResourceStored = unitInventory.getAmountResourceStored(resource, 
                                    false);
                            double unitRetrieveAmount = remainingAmount;
                            if (unitRetrieveAmount > unitResourceStored) {
                                unitRetrieveAmount = unitResourceStored;
                            }
                            if (unitRetrieveAmount > 0D) {
                                unitInventory.retrieveAmountResource(resource, unitRetrieveAmount);
                                remainingAmount -= unitRetrieveAmount;
                            }
                        }
                    }
                }

                if (remainingAmount > SMALL_AMOUNT_COMPARISON) {
                    throw new IllegalStateException(resource.getName()
                            + " could not be totally retrieved. Remaining: " + remainingAmount);
                }

                // Update caches.
                updateAmountResourceCapacityCache(resource);
                updateAmountResourceStoredCache(resource);

                // Fire inventory event.
                if (owner != null) {
                    owner.fireUnitUpdate(INVENTORY_RESOURCE_EVENT, resource);
                }
            } else {
                throw new IllegalStateException("Insufficiant stored amount to retrieve " + 
                        resource.getName() + ", stored: " + getAmountResourceStored(resource, false) + 
                        ", attempted: " + amount);
            }
        }
    }

    /**
     * Adds a capacity to general capacity.
     * @param capacity amount capacity (kg).
     */
    public void addGeneralCapacity(double capacity) {
        generalCapacity += capacity;
        // Mark amount resource capacity cache as dirty.
        setAmountResourceCapacityCacheAllDirty();
    }

    /**
     * Gets the general capacity.
     * @return amount capacity (kg).
     */
    public double getGeneralCapacity() {
        return generalCapacity;
    }

    /**
     * Gets the mass stored in general capacity.
     * @param allowDirty will allow dirty (possibly out of date) results.
     * @return stored mass (kg).
     */
    public double getGeneralStoredMass(boolean allowDirty) {
        return getItemResourceTotalMass(allowDirty) + getUnitTotalMass(allowDirty);
    }

    /**
     * Gets the remaining general capacity available.
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
     * Checks if storage has an item resource.
     * @param resource the resource.
     * @return true if has resource.
     */
    public boolean hasItemResource(ItemResource resource) {
        boolean result = false;
        if ((containedItemResources != null) && containedItemResources.containsKey(resource)) {
            if (containedItemResources.get(resource) > 0) {
                result = true;
            }
        } else if (containedUnits != null) {
            Iterator<Unit> i = containedUnits.iterator();
            while (i.hasNext()) {
                if (i.next().getInventory().hasItemResource(resource)) {
                    result = true;
                }
            }
        }
        return result;
    }

    /**
     * Gets the number of an item resource in storage.
     * @param resource the resource.
     * @return number of resources.
     */
    public int getItemResourceNum(ItemResource resource) {
        int result = 0;
        if ((containedItemResources != null) && containedItemResources.containsKey(resource)) {
            result += containedItemResources.get(resource);
        }
        return result;
    }

    /**
     * Gets a set of all the item resources in storage.
     * @return set of item resources.
     */
    public Set<ItemResource> getAllItemResourcesStored() {
        Set<ItemResource> result = null;
        if (containedItemResources != null) {
            result = containedItemResources.keySet();
        } else {
            result = new HashSet<ItemResource>();
        }
        return result;
    }

    /**
     * Gets the total mass of item resources in storage.
     * @param allowDirty will allow dirty (possibly out of date) results.
     * @return the total mass (kg).
     */
    private double getItemResourceTotalMass(boolean allowDirty) {
        return getItemResourceTotalMassCache(allowDirty);
    }

    /**
     * Stores item resources.
     * @param resource the resource to store.
     * @param number the number of resources to store.
     */
    public void storeItemResources(ItemResource resource, int number) {

        if (number < 0) {
            throw new IllegalStateException("Cannot store negative number of resources.");
        }

        double totalMass = resource.getMassPerItem() * number;

        if (number > 0) {
            if (totalMass <= getRemainingGeneralCapacity(false)) {

                // Mark caches as dirty.
                setAmountResourceCapacityCacheAllDirty();
                setItemResourceTotalMassCacheDirty();
                
                // Initialize contained item resources if necessary.
                if (containedItemResources == null) {
                    containedItemResources = new ConcurrentHashMap<ItemResource, Integer>();
                }
                
                int totalNum = number + getItemResourceNum(resource);
                if (totalNum > 0) {
                    containedItemResources.put(resource, totalNum);
                }
                
                // Fire inventory event.
                if (owner != null) {
                    owner.fireUnitUpdate(INVENTORY_RESOURCE_EVENT, resource);
                }
            } 
            else {
                throw new IllegalStateException("Could not store item resources.");
            }
        }
    }

    /**
     * Retrieves item resources.
     * @param resource the resource to retrieve.
     * @param number the number of resources to retrieve.
     */
    public void retrieveItemResources(ItemResource resource, int number) {

        if (number < 0) {
            throw new IllegalStateException("Cannot retrieve negative number of resources.");
        }

        if (number > 0) {
            if (number <= getItemResourceNum(resource)) {
                int remainingNum = number;

                // Mark caches as dirty.
                setAmountResourceCapacityCacheAllDirty();
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
                if (owner != null) {
                    owner.fireUnitUpdate(INVENTORY_RESOURCE_EVENT, resource);
                }

                if (remainingNum > 0) {
                    throw new IllegalStateException(resource.getName()
                            + " could not be totally retrieved. Remaining: " + remainingNum);
                }
            } 
            else {
                throw new IllegalStateException("Insufficiant stored number to retrieve " + 
                        resource.getName() + ", stored: " + getItemResourceNum(resource) + 
                        ", attempted: " + number);
            }
        }
    }

    /** 
     * Gets the total unit mass in storage.
     * @param allowDirty will allow dirty (possibly out of date) results.
     * @return total mass (kg).
     */
    public double getUnitTotalMass(boolean allowDirty) {
        return getUnitTotalMassCache(allowDirty);
    }

    /** 
     * Gets a collection of all the stored units.
     * @return Collection of all units
     */
    public Collection<Unit> getContainedUnits() {
        Collection<Unit> result = null;
        if (containedUnits != null) {
            result = new ArrayList<Unit>(containedUnits);
        } else {
            result = new ArrayList<Unit>(0);
        }
        return result; 
    }

    /** 
     * Checks if a unit is in storage.
     * @param unit the unit.
     * @return true if unit is in storage.
     */
    public boolean containsUnit(Unit unit) {
        boolean result = false;
        if (containedUnits != null) {
            result = containedUnits.contains(unit);
        }
        return result;
    }

    /**
     * Checks if any of a given class of unit is in storage.
     * @param unitClass the unit class.
     * @return true if class of unit is in storage.
     */
    private boolean containsUnitClassLocal(Class<? extends Unit> unitClass) {
        boolean result = false;
        if (containedUnits != null) {
            Iterator<Unit> i = containedUnits.iterator();
            while (i.hasNext()) {
                if (unitClass.isInstance(i.next())) {
                    result = true;
                }
            }
        }
        return result;
    }

    /**
     * Checks if any of a given class of unit is in storage.
     * @param unitClass the unit class.
     * @return if class of unit is in storage.
     */
    public boolean containsUnitClass(Class<? extends Unit> unitClass) {
        boolean result = false;
        // Check if unit of class is in inventory.
        if (containsUnitClassLocal(unitClass)) {
            result = true;
        }
        return result;
    }

    /**
     * Finds a unit of a given class in storage.
     * @param unitClass the unit class.
     * @return the instance of the unit class or null if none.
     */
    public Unit findUnitOfClass(Class<? extends Unit> unitClass) {
        Unit result = null;
        if (containsUnitClass(unitClass)) {
            Iterator<Unit> i = containedUnits.iterator();
            while (i.hasNext()) {
                Unit unit = i.next();
                if (unitClass.isInstance(unit)) {
                    result = unit;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Finds all of the units of a class in storage.
     * @param unitClass the unit class.
     * @return collection of units or empty collection if none.
     */
    public Collection<Unit> findAllUnitsOfClass(Class<? extends Unit> unitClass) {
        Collection<Unit> result = new ConcurrentLinkedQueue<Unit>();
        if (containsUnitClass(unitClass)) {
            Iterator<Unit> i = containedUnits.iterator();
            while (i.hasNext()) {
                Unit unit = i.next();
                if (unitClass.isInstance(unit)) {
                    result.add(unit);
                }
            }
        }
        return result;
    }

    /**
     * Finds the number of units of a class that are contained in storage.
     * @param unitClass the unit class.
     * @return number of units
     */
    public int findNumUnitsOfClass(Class<? extends Unit> unitClass) {
        int result = 0;
        if (containsUnitClass(unitClass)) {
            Iterator<Unit> i = containedUnits.iterator();
            while (i.hasNext()) {
                Unit unit = i.next();
                if (unitClass.isInstance(unit)) {
                    result++;
                }
            }
        }
        return result;
    }

    /**
     * Finds the number of units of a class that are contained in 
     * storage and have an empty inventory.
     * @param unitClass the unit class.
     * @param allowDirty will allow dirty (possibly out of date) results.
     * @return number of empty units.
     */
    public int findNumEmptyUnitsOfClass(Class<? extends Unit> unitClass, boolean allowDirty) {
        int result = 0;
        if (containsUnitClass(unitClass)) {
            Iterator<Unit> i = containedUnits.iterator();
            while (i.hasNext()) {
                Unit unit = i.next();
                if (unitClass.isInstance(unit)) {
                    Inventory inv = unit.getInventory();
                    if ((inv != null) && inv.isEmpty(allowDirty)) {
                        result++;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Checks if a unit can be stored.
     * @param unit the unit.
     * @param allowDirty will allow dirty (possibly out of date) results.
     * @return true if unit can be added to inventory
     */
    public boolean canStoreUnit(Unit unit, boolean allowDirty) {
        boolean result = false;
        if (unit != null) {
            if (unit.getMass() <= getRemainingGeneralCapacity(allowDirty)) {
                result = true;
            }
            if (unit == owner) {
                result = false;
            }
            if (containsUnit(unit)) {
                result = false;
            }
            if (unit.getInventory().containsUnit(owner)) {
                result = false;
            }
        }
        return result;
    }

    /** 
     * Stores a unit.
     * @param unit the unit
     */
    public void storeUnit(Unit unit) {

        if (canStoreUnit(unit, false)) {

            // Set modified cache values as dirty.
            setAmountResourceCapacityCacheAllDirty();
            setAmountResourceStoredCacheAllDirty();
            setAllStoredAmountResourcesCacheDirty();
            setTotalAmountResourcesStoredCacheDirty();
            setUnitTotalMassCacheDirty();

            // Initialize containedUnits if necessary.
            if (containedUnits == null) {
                containedUnits = new ConcurrentLinkedQueue<Unit>();
            }
            
            containedUnits.add(unit);
            unit.setContainerUnit(owner);

            // Try to empty amount resources into parent if container.
            if (unit instanceof Container) {
                Inventory containerInv = unit.getInventory();
                Iterator<AmountResource> i = containerInv.getAllAmountResourcesStored(false).iterator();
                while (i.hasNext()) {
                    AmountResource resource = i.next();
                    double containerAmount = containerInv.getAmountResourceStored(resource, false);
                    if (getAmountResourceRemainingCapacity(resource, false, false) >= containerAmount) {
                        containerInv.retrieveAmountResource(resource, containerAmount);
                        storeAmountResource(resource, containerAmount, false);
                    }
                }
            }

            // Update owner
            if (owner != null) {
                unit.setCoordinates(owner.getCoordinates());
                owner.fireUnitUpdate(INVENTORY_STORING_UNIT_EVENT, unit);
                Iterator<AmountResource> i = unit.getInventory().getAllAmountResourcesStored(false).
                        iterator();
                while (i.hasNext()) {
                    AmountResource resource = i.next();
                    updateAmountResourceCapacityCache(resource);
                    updateAmountResourceStoredCache(resource);
                    owner.fireUnitUpdate(INVENTORY_RESOURCE_EVENT, resource);
                }
                Iterator<ItemResource> j = unit.getInventory().getAllItemResourcesStored().iterator();
                while (j.hasNext()) {
                    owner.fireUnitUpdate(INVENTORY_RESOURCE_EVENT, j.next());
                }
            }
        } 
        else {
            throw new IllegalStateException("Unit: " + unit + " could not be stored.");
        }
    }

    /**
     * Retrieves a unit from storage.
     * @param unit the unit.
     */
    public void retrieveUnit(Unit unit) {

        boolean retrieved = false;

        if (containsUnit(unit)) {

            // Set modified cache values as dirty.
            setAmountResourceCapacityCacheAllDirty();
            setAmountResourceStoredCacheAllDirty();
            setAllStoredAmountResourcesCacheDirty();
            setTotalAmountResourcesStoredCacheDirty();
            setUnitTotalMassCacheDirty();

            if (containedUnits.contains(unit)) {

                containedUnits.remove(unit);

                // Update owner
                if (owner != null) {
                    owner.fireUnitUpdate(INVENTORY_RETRIEVING_UNIT_EVENT, unit);

                    Iterator<AmountResource> i = unit.getInventory().getAllAmountResourcesStored(false).
                            iterator();
                    while (i.hasNext()) {
                        AmountResource resource = i.next();
                        updateAmountResourceCapacityCache(resource);
                        updateAmountResourceStoredCache(resource);
                        owner.fireUnitUpdate(INVENTORY_RESOURCE_EVENT, resource);
                    }

                    Iterator<ItemResource> j = unit.getInventory().getAllItemResourcesStored().iterator();
                    while (j.hasNext()) {
                        owner.fireUnitUpdate(INVENTORY_RESOURCE_EVENT, j.next());
                    }
                }

                retrieved = true;
            }
        }

        if (retrieved) {
            unit.setContainerUnit(null);
        } else {
            throw new IllegalStateException("Unit: " + unit + " could not be retrived.");
        }
    }

    /**
     * Sets the coordinates of all units in the inventory.
     * @param newLocation the new coordinate location
     */
    public void setCoordinates(Coordinates newLocation) {

        if (containedUnits != null) {
            Iterator<Unit> i = containedUnits.iterator();
            while (i.hasNext()) {
                i.next().setCoordinates(newLocation);
            }
        }
    }

    /**
     * Gets the total mass stored in inventory.
     * @param allowDirty will allow dirty (possibly out of date) results.
     * @return stored mass (kg).
     */
    public double getTotalInventoryMass(boolean allowDirty) {

        return getTotalInventoryMassCache(allowDirty);
    }

    /**
     * Checks if inventory is empty.
     * @param allowDirty will allow dirty (possibly out of date) results.
     * @return true if empty.
     */
    public boolean isEmpty(boolean allowDirty) {

        return (getTotalInventoryMass(allowDirty) == 0D);
    }

    /**
     * Gets any limits in the owner's general capacity.
     * @param allowDirty will allow dirty (possibly out of date) results.
     * @return owner general capacity limit (kg).
     */
    private double getContainerUnitGeneralCapacityLimit(boolean allowDirty) {

        double result = Double.MAX_VALUE;

        if ((owner != null) && (owner.getContainerUnit() != null)) {
            Inventory containerInv = owner.getContainerUnit().getInventory();
            if (containerInv.getRemainingGeneralCapacity(allowDirty) < result) {
                result = containerInv.getRemainingGeneralCapacity(allowDirty);
            }

            if (containerInv.getContainerUnitGeneralCapacityLimit(allowDirty) < result) {
                result = containerInv.getContainerUnitGeneralCapacityLimit(allowDirty);
            }
        }

        return result;
    }
    
    /**
     * Initializes the amount resource capacity cache.
     */
    private synchronized void initializeAmountResourceCapacityCache() {
        
        Collection<AmountResource> resources = AmountResource.getAmountResources();
        amountResourceCapacityCache = new HashMap<AmountResource, Double>();
        amountResourceCapacityCacheDirty = new HashMap<AmountResource, Boolean>();

        Iterator<AmountResource> i = resources.iterator();
        while (i.hasNext()) {
            AmountResource resource = i.next();
            amountResourceCapacityCache.put(resource, 0D);
            amountResourceCapacityCacheDirty.put(resource, true);
        }
    }
    
    /**
     * Checks if the amount resource capacity cache is dirty for a resource.
     * @param resource the resource to check.
     * @return true if resource is dirty in cache.
     */
    private boolean isAmountResourceCapacityCacheDirty(AmountResource resource) {

        // Initialize amount resource capacity cache if necessary.
        if (amountResourceCapacityCache == null) {
            initializeAmountResourceCapacityCache();
        }

        return amountResourceCapacityCacheDirty.get(resource);
    }
    
    /**
     * Sets a resource in the amount resource capacity cache to dirty.
     * @param resource the dirty resource.
     */
    private void setAmountResourceCapacityCacheDirty(AmountResource resource) {

        // Initialize amount resource capacity cache if necessary.
        if (amountResourceCapacityCache == null) {
            initializeAmountResourceCapacityCache();
        }

        amountResourceCapacityCacheDirty.put(resource, true);
    }
    
    /**
     * Sets all of the resources in the amount resource capacity cache to dirty.
     */
    private void setAmountResourceCapacityCacheAllDirty() {

        // Initialize amount resource capacity cache if necessary.
        if (amountResourceCapacityCache == null) {
            initializeAmountResourceCapacityCache();
        }

        Iterator<AmountResource> i = AmountResource.getAmountResources().iterator();
        while (i.hasNext()) {
            setAmountResourceCapacityCacheDirty(i.next());
        }
        
        // Set owner unit's amount resource capacity cache as dirty (if any).
        if (owner != null) {
            Unit container = owner.getContainerUnit();
            if (container != null) {
                container.getInventory().setAmountResourceCapacityCacheAllDirty();
            }
        }
    }
    
    /**
     * Gets the cached capacity value for an amount resource.
     * @param resource the amount resource.
     * @param allowDirty true if cache value can be dirty.
     * @return capacity (kg) for the amount resource.
     */
    private double getAmountResourceCapacityCacheValue(AmountResource resource, boolean allowDirty) {

        // Initialize amount resource capacity cache if necessary.
        if (amountResourceCapacityCache == null) {
            initializeAmountResourceCapacityCache();
        }

        // Update amount resource capacity cache if it is dirty.
        if (isAmountResourceCapacityCacheDirty(resource) && !allowDirty) {
            updateAmountResourceCapacityCache(resource);
        }
        
        return amountResourceCapacityCache.get(resource);
    }

    /**
     * Update the amount resource capacity cache for an amount resource.
     * @param resource the resource to update.
     */
    private void updateAmountResourceCapacityCache(AmountResource resource) {

        // Initialize amount resource capacity cache if necessary.
        if (amountResourceCapacityCache == null) {
            initializeAmountResourceCapacityCache();
        }

        // Determine local resource capacity.
        double capacity = 0D;
        if (resourceStorage != null) {
            capacity += resourceStorage.getAmountResourceCapacity(resource);
        }

        // Determine capacity for all contained units.
        double containedCapacity = 0D;
        double containedStored = 0D;
        if (containedUnits != null) {
            Iterator<Unit> j = containedUnits.iterator();
            while (j.hasNext()) {
                // Only add unit capacity if unit is a container.
                Unit unit = j.next();
                if (unit instanceof Container) {
                    containedCapacity += unit.getInventory().getAmountResourceCapacity(
                            resource, false);
                    containedStored += unit.getInventory().getAmountResourceStored(resource, 
                            false);
                }
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

        amountResourceCapacityCache.put(resource, capacity);
        amountResourceCapacityCacheDirty.put(resource, false);
    }
    
    /**
     * Initializes the amount resource stored cache.
     */
    private synchronized void initializeAmountResourceStoredCache() {
        
        Collection<AmountResource> resources = AmountResource.getAmountResources();
        amountResourceStoredCache = new HashMap<AmountResource, Double>();
        amountResourceStoredCacheDirty = new HashMap<AmountResource, Boolean>();
        
        Iterator<AmountResource> i = resources.iterator();
        while (i.hasNext()) {
            AmountResource resource = i.next();
            amountResourceStoredCache.put(resource, 0D);
            amountResourceStoredCacheDirty.put(resource, true);
        }
    }
    
    /**
     * Checks if the amount resource stored cache is dirty for a resource.
     * @param resource the resource to check.
     * @return true if resource is dirty in cache.
     */
    private boolean isAmountResourceStoredCacheDirty(AmountResource resource) {

        // Initialize amount resource stored cache if necessary.
        if (amountResourceStoredCache == null) {
            initializeAmountResourceStoredCache();
        }

        return amountResourceStoredCacheDirty.get(resource);
    }
    
    /**
     * Sets a resource in the amount resource stored cache to dirty.
     * @param resource the dirty resource.
     */
    private void setAmountResourceStoredCacheDirty(AmountResource resource) {

        // Initialize amount resource stored cache if necessary.
        if (amountResourceStoredCache == null) {
            initializeAmountResourceStoredCache();
        }

        amountResourceStoredCacheDirty.put(resource, true);
    }
    
    /**
     * Sets all of the resources in the amount resource stored cache to dirty.
     */
    private void setAmountResourceStoredCacheAllDirty() {

        // Initialize amount resource stored cache if necessary.
        if (amountResourceStoredCache == null) {
            initializeAmountResourceStoredCache();
        }

        Iterator<AmountResource> i = AmountResource.getAmountResources().iterator();
        while (i.hasNext()) {
            setAmountResourceStoredCacheDirty(i.next());
        }
        
        // Set owner unit's amount resource stored cache as dirty (if any).
        if (owner != null) {
            Unit container = owner.getContainerUnit();
            if (container != null) {
                container.getInventory().setAmountResourceStoredCacheAllDirty();
            }
        }
    }
    
    /**
     * Gets the cached stored value for an amount resource.
     * @param resource the amount resource.
     * @param allowDirty true if cache value can be dirty.
     * @return stored amount (kg) for the amount resource.
     */
    private double getAmountResourceStoredCacheValue(final AmountResource resource, final boolean allowDirty) {

        // Initialize amount resource stored cache if necessary.
        if (amountResourceStoredCache == null) {
            initializeAmountResourceStoredCache();
        }

        // Update amount resource stored cache if it is dirty.
        if (!allowDirty && isAmountResourceStoredCacheDirty(resource)) {
            updateAmountResourceStoredCache(resource);
        }
        
        return amountResourceStoredCache.get(resource);
    }

    /**
     * Update the amount resource stored cache for an amount resource.
     * @param resource the resource to update.
     */
    private void updateAmountResourceStoredCache(AmountResource resource) {

        double stored = 0D;

        if (resourceStorage != null) {
            stored += resourceStorage.getAmountResourceStored(resource);
        }

        if (containedUnits != null) {
            Iterator<Unit> j = containedUnits.iterator();
            while (j.hasNext()) {
                Unit unit = j.next();
                if (unit instanceof Container) {
                    stored += unit.getInventory().getAmountResourceStored(resource, false);
                }
            }
        }

        amountResourceStoredCache.put(resource, stored);
        amountResourceStoredCacheDirty.put(resource, false);
    }

    /**
     * Initializes the all stored amount resources cache.
     */
    private synchronized void initializeAllStoredAmountResourcesCache() {
        
        allStoredAmountResourcesCache = new HashSet<AmountResource>();
        allStoredAmountResourcesCacheDirty = true;
    }
    
    /**
     * Sets the all stored amount resources cache as dirty.
     */
    private void setAllStoredAmountResourcesCacheDirty() {

		// Update all stored amount resources cache if it hasn't been initialized.
		if (allStoredAmountResourcesCache == null) {
			initializeAllStoredAmountResourcesCache();
		}

        allStoredAmountResourcesCacheDirty = true;
        
        // Mark owner unit's all stored amount resources stored as dirty, if any.
        if (owner != null) {
            Unit container = owner.getContainerUnit();
            if (container != null) {
                container.getInventory().setAllStoredAmountResourcesCacheDirty();
            }
        }
    }
    
    /**
     * Gets the all stored amount resources cache.
     * @param allowDirty true if cache value can be dirty.
     * @return all stored amount resources cache value.
     */
    private Set<AmountResource> getAllStoredAmountResourcesCache(boolean allowDirty) {

        // Update all stored amount resources cache if it hasn't been initialized.
        if (allStoredAmountResourcesCache == null) {
            initializeAllStoredAmountResourcesCache();
        }

        if (allStoredAmountResourcesCacheDirty && !allowDirty) {
            updateAllStoredAmountResourcesCache();
        }

        return allStoredAmountResourcesCache;
    }
    
    /**
     * Update the all stored amount resources cache as well as the container's cache if any.
     */
    private void updateAllStoredAmountResourcesCache() {

        Set<AmountResource> tempAllStored = new HashSet<AmountResource>();

        if (resourceStorage != null) {
            tempAllStored.addAll(resourceStorage.getAllAmountResourcesStored(false));
        }

        if (containedUnits != null) {
            Iterator<Unit> j = containedUnits.iterator();
            while (j.hasNext()) {
                Unit unit = j.next();
                if (unit instanceof Container) {
                    tempAllStored.addAll(unit.getInventory().getAllAmountResourcesStored(false));
                }
            }
        }

        allStoredAmountResourcesCache = tempAllStored;
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
        if (owner != null) {
            Unit container = owner.getContainerUnit();
            if (container != null) {
                container.getInventory().setTotalAmountResourcesStoredCacheDirty();
            }
        }
    }
    
    /**
     * Gets the total amount resource stored cache value.
     * @param allowDirty true if cache value can be dirty.
     * @return total amount resources stored cache value.
     */
    private double getTotalAmountResourcesStoredCache(boolean allowDirty) {
        
        // Update total amount resources stored cache if it is dirty.
        if (!allowDirty && totalAmountResourcesStoredCacheDirty) {
            updateTotalAmountResourcesStoredCache();
        }

        return totalAmountResourcesStoredCache;
    }
    
    /**
     * Update the total amount resources stored cache as well as the container's cache if any.
     */
    private void updateTotalAmountResourcesStoredCache() {

        double tempStored = 0D;
        if (resourceStorage != null) {
            tempStored += resourceStorage.getTotalAmountResourcesStored(false);
        }

        if (containedUnits != null) {
            Iterator<Unit> i = containedUnits.iterator();
            while (i.hasNext()) {
                Unit unit = i.next();
                tempStored = unit.getInventory().getTotalAmountResourcesStored(false);
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

        if (containedItemResources != null) {
            Set<Entry<ItemResource, Integer>> es = containedItemResources.entrySet();
            for(Entry<ItemResource, Integer> e : es){
                tempMass += e.getValue() * e.getKey().getMassPerItem();
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

        if (containedUnits != null) {
            Iterator<Unit> unitIt = containedUnits.iterator();
            while (unitIt.hasNext()) {
                tempMass += unitIt.next().getMass();
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
        if (owner != null) {
            Unit container = owner.getContainerUnit();
            if (container != null) {
                container.getInventory().setUnitTotalMassCacheDirty();
            }
        }
    }
    
    /**
     * Gets the total inventory mass cache value.
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
     * Creates a clone of this inventory (not including the inventory contents).
     * @param owner the unit owner of the inventory (or null).
     * @return inventory clone.
     */
    public Inventory clone(Unit owner) {

        Inventory result = new Inventory(owner);
        result.addGeneralCapacity(generalCapacity);

        if (resourceStorage != null) {
            Map<AmountResource, Double> typeCapacities = resourceStorage.getAmountResourceTypeCapacities();
            Iterator<AmountResource> i = typeCapacities.keySet().iterator();
            while (i.hasNext()) {
                AmountResource type = i.next();
                result.addAmountResourceTypeCapacity(type, typeCapacities.get(type));
            }

            Map<Phase, Double> phaseCapacities = resourceStorage.getAmountResourcePhaseCapacities();
            Iterator<Phase> j = phaseCapacities.keySet().iterator();
            while (j.hasNext()) {
                Phase phase = j.next();
                result.addAmountResourcePhaseCapacity(phase, phaseCapacities.get(phase));
            }
        }

        return result;
    }

    /**
     * Prepare object for garbage collection.
     */
    public void destroy() {

        owner = null;
        if (containedUnits != null) containedUnits.clear();
        containedUnits = null;
        if (containedItemResources != null) containedItemResources.clear();
        containedItemResources = null;
        if (resourceStorage != null) resourceStorage.destroy();
        resourceStorage = null;
        if (amountResourceCapacityCache != null) amountResourceCapacityCache.clear();
        amountResourceCapacityCache = null;
        if (amountResourceCapacityCacheDirty != null) amountResourceCapacityCacheDirty.clear();
        amountResourceCapacityCacheDirty = null;
        if (amountResourceStoredCache != null) amountResourceStoredCache.clear();
        if (amountResourceStoredCacheDirty != null) amountResourceStoredCacheDirty.clear();
        amountResourceStoredCacheDirty = null;
        amountResourceStoredCache = null;
        if (allStoredAmountResourcesCache != null) allStoredAmountResourcesCache.clear();
        allStoredAmountResourcesCache = null;
    }
    
    /**
     * Implementing readObject method for serialization.
     * @param in the input stream.
     * @throws IOException if error reading from input stream.
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