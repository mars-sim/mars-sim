/**
 * Mars Simulation Project
 * Inventory.java
 * @version 3.03 2012-07-07
 * @author Scott Davis 
 */
package org.mars_sim.msp.core;

import org.mars_sim.msp.core.equipment.Container;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.AmountResourceStorage;
import org.mars_sim.msp.core.resource.ItemResource;
import org.mars_sim.msp.core.resource.Phase;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/** 
 * The Inventory class represents what a unit 
 * contains in terms of resources and other units.
 * It has methods for adding, removing and querying
 * what the unit contains.
 */
public class Inventory implements Serializable {

    // Unit events
    public static final String INVENTORY_STORING_UNIT_EVENT = "inventory storing unit";
    public static final String INVENTORY_RETRIEVING_UNIT_EVENT = "inventory retrieving unit";
    public static final String INVENTORY_RESOURCE_EVENT = "inventory resource event";
    
    // Data members
    private Unit owner; // The unit that owns this inventory. 
    private Collection<Unit> containedUnits = null; // Collection of units in inventory.
    private ConcurrentHashMap<ItemResource, Integer> containedItemResources = null; // Map of item resources.
    private double generalCapacity = 0D; // General mass capacity of inventory.
    private AmountResourceStorage resourceStorage = null; // Resource storage.
    
    // Cache capacity variables.
    private transient ConcurrentHashMap<AmountResource, Double> amountResourceCapacityCache = new ConcurrentHashMap<AmountResource, Double>(10);
    private transient ConcurrentHashMap<AmountResource, Double> amountResourceStoredCache = new ConcurrentHashMap<AmountResource, Double>(10);
    private transient Set<AmountResource> allStoredAmountResourcesCache = null;
    private transient double totalAmountResourcesStored = -1D;
    private transient boolean totalAmountResourcesStoredSet = false;
    private transient ConcurrentHashMap<AmountResource, Double> amountResourceRemainingCache = new ConcurrentHashMap<AmountResource, Double>(10);

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
     * @throws InventoryException if error setting capacity.
     */
    public synchronized void addAmountResourceTypeCapacity(AmountResource resource, double capacity) {
        
        if (resourceStorage == null) {
            resourceStorage = new AmountResourceStorage();
        }

        resourceStorage.addAmountResourceTypeCapacity(resource, capacity);
        clearAmountResourceCapacityCache();
    }

    /**
     * Adds capacity for a resource phase.
     * @param phase the phase
     * @param capacity the capacity amount (kg).
     * @throws InventoryException if error adding capacity.
     */
    public synchronized void addAmountResourcePhaseCapacity(Phase phase, double capacity) {
        
        if (resourceStorage == null) {
            resourceStorage = new AmountResourceStorage();
        }

        resourceStorage.addAmountResourcePhaseCapacity(phase, capacity);
        clearAmountResourceCapacityCache();
    }

    /**
     * Checks if storage has capacity for a resource.
     * @param resource the resource.
     * @return true if storage capacity.
     */
    public synchronized boolean hasAmountResourceCapacity(AmountResource resource) {
        
        if (resource == null) {
            throw new IllegalArgumentException("resource cannot be null.");
        }
        try {
            boolean result = false;
            if ((amountResourceCapacityCache != null) && amountResourceCapacityCache.containsKey(resource)) {
                double amountCapacity = amountResourceCapacityCache.get(resource);
                result = (amountCapacity > 0D);
            } else {
                if ((resourceStorage != null) && resourceStorage.hasAmountResourceCapacity(resource)) {
                    result = true;
                } else if ((containedUnits != null) && (getRemainingGeneralCapacity() > 0D)) {
                    Iterator<Unit> i = containedUnits.iterator();
                    while (i.hasNext()) {
                        if (i.next().getInventory().hasAmountResourceCapacity(resource)) {
                            result = true;
                        }
                    }
                }
            }
            return result;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Checks if storage has capacity for an amount of a resource.
     * @param resource the resource.
     * @param amount the amount (kg).
     * @return true if storage capacity.
     * @throws InventoryException if error checking capacity.
     */
    public synchronized boolean hasAmountResourceCapacity(AmountResource resource, double amount) {
        
        try {
            boolean result = false;
            if ((amountResourceCapacityCache != null) && amountResourceCapacityCache.containsKey(resource)) {
                double amountCapacity = amountResourceCapacityCache.get(resource);
                result = (amountCapacity >= amount);
            } else {
                double capacity = 0D;
                if (resourceStorage != null) {
                    capacity += resourceStorage.getAmountResourceCapacity(resource);
                }
                if (amount < capacity) {
                    result = true;
                } else if ((containedUnits != null) && (getRemainingGeneralCapacity() > 0D)) {
                    double containedCapacity = 0D;
                    Iterator<Unit> i = containedUnits.iterator();
                    while (i.hasNext()) {
                        containedCapacity += i.next().getInventory().getAmountResourceCapacity(resource);
                    }
                    if (containedCapacity > generalCapacity) {
                        containedCapacity = generalCapacity;
                    }
                    capacity += containedCapacity;
                    if ((capacity + containedCapacity) > amount) {
                        result = true;
                    }
                }
                if (amountResourceCapacityCache == null) {
                    amountResourceCapacityCache = new ConcurrentHashMap<AmountResource, Double>(10);
                }
                amountResourceCapacityCache.put(resource, capacity);
            }
            return result;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Gets the storage capacity for a resource.
     * @param resource the resource.
     * @return capacity amount (kg).
     * @throws InventoryException if error determining capacity.
     */
    public synchronized double getAmountResourceCapacity(AmountResource resource) {
        
        if (resource == null) {
            throw new IllegalArgumentException("resource cannot be null.");
        }
        try {
            double result = 0D;
            if ((amountResourceCapacityCache != null) && amountResourceCapacityCache.containsKey(resource)) {
                result = amountResourceCapacityCache.get(resource);
            } else {
                if (hasAmountResourceCapacity(resource)) {
                    if (resourceStorage != null) {
                        result += resourceStorage.getAmountResourceCapacity(resource);
                    }
                    if ((containedUnits != null) && (generalCapacity > 0D)) {
                        double containedCapacity = 0D;
                        Iterator<Unit> i = containedUnits.iterator();
                        while (i.hasNext()) {
                            Unit containedUnit = i.next();
                            if (containedUnit instanceof Container) {
                                containedCapacity += containedUnit.getInventory().getAmountResourceCapacity(resource);
                            }
                        }
                        if (containedCapacity > generalCapacity) {
                            containedCapacity = generalCapacity;
                        }
                        result += containedCapacity;
                    }
                }
                if (amountResourceCapacityCache == null) {
                    amountResourceCapacityCache = new ConcurrentHashMap<AmountResource, Double>(10);
                }
                amountResourceCapacityCache.put(resource, result);
            }
            return result;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Gets the amount of a resource stored.
     * @param resource the resource.
     * @return stored amount (kg).
     * @throws InventoryException if error getting amount stored.
     */
    public synchronized double getAmountResourceStored(AmountResource resource) {
        
        if (resource == null) {
            throw new IllegalArgumentException("resource is null");
        }

        double result = 0D;
        if ((amountResourceStoredCache != null) && amountResourceStoredCache.containsKey(resource)) {
            result = amountResourceStoredCache.get(resource);
        } else {
            if (resourceStorage != null) {
                result += resourceStorage.getAmountResourceStored(resource);
            }
            if (containedUnits != null) {
                Iterator<Unit> i = containedUnits.iterator();
                while (i.hasNext()) {
                    Unit unit = i.next();
                    if (unit instanceof Container) {
                        result += unit.getInventory().getAmountResourceStored(resource);
                    }
                }
            }
            if (amountResourceStoredCache == null) {
                amountResourceStoredCache = new ConcurrentHashMap<AmountResource, Double>(10);
            }
            amountResourceStoredCache.put(resource, result);
        }
        return result;
    }

    /**
     * Gets all of the amount resources stored.
     * @return set of amount resources.
     * @throws InventoryException if error getting all amount resources.
     */
    public synchronized Set<AmountResource> getAllAmountResourcesStored() {
        
        if (allStoredAmountResourcesCache != null) {
            return new HashSet<AmountResource>(allStoredAmountResourcesCache);
        } else {
            allStoredAmountResourcesCache = new HashSet<AmountResource>(1, 1);
            if (resourceStorage != null) {
                synchronized (resourceStorage) {
                    allStoredAmountResourcesCache.addAll(resourceStorage.getAllAmountResourcesStored());
                }
            }
            if (containedUnits != null) {
                Iterator<Unit> i = containedUnits.iterator();
                while (i.hasNext()) {
                    Set<AmountResource> containedResources =
                            i.next().getInventory().getAllAmountResourcesStored();
                    allStoredAmountResourcesCache.addAll(containedResources);
                }
            }

            return new HashSet<AmountResource>(allStoredAmountResourcesCache);
        }
    }

    /**
     * Gets the total mass of amount resources stored.
     * @return stored amount (kg).
     * throws InventoryException if error getting total amount resources stored.
     */
    private double getTotalAmountResourcesStored() {

        double result = 0D;
        if (totalAmountResourcesStoredSet) {
            result = totalAmountResourcesStored;
        } else {
            if (resourceStorage != null) {
                result += resourceStorage.getTotalAmountResourcesStored();
            }
            if (containedUnits != null) {
                Iterator<Unit> i = containedUnits.iterator();
                while (i.hasNext()) {
                    result += i.next().getInventory().getTotalAmountResourcesStored();
                }
            }
            totalAmountResourcesStored = result;
            totalAmountResourcesStoredSet = true;
        }
        return result;
    }

    /**
     * Gets the remaining capacity available for a resource.
     * @param resource the resource.
     * @param useContainedUnits should the capacity of contained units be added?
     * @return remaining capacity amount (kg).
     * throws InventoryException if error getting remaining capacity.
     */
    public synchronized double getAmountResourceRemainingCapacity(AmountResource resource, 
            boolean useContainedUnits) {
        
        try {
            double result = 0D;
            if (useContainedUnits && (amountResourceRemainingCache != null) && 
                    amountResourceRemainingCache.containsKey(resource)) {
                return amountResourceRemainingCache.get(resource);
            } else {
                if (resourceStorage != null) {
                    result += resourceStorage.getAmountResourceRemainingCapacity(resource);
                }
                if (useContainedUnits && (containedUnits != null)) {
                    double containedRemainingCapacity = 0D;
                    Iterator<Unit> i = containedUnits.iterator();
                    while (i.hasNext()) {
                        Unit unit = i.next();
                        if (unit instanceof Container) {
                            containedRemainingCapacity += unit.getInventory().getAmountResourceRemainingCapacity(
                                    resource, true);
                        }
                    }
                    if (containedRemainingCapacity > getRemainingGeneralCapacity()) {
                        containedRemainingCapacity = getRemainingGeneralCapacity();
                    }
                    result += containedRemainingCapacity;
                }
                if (result > getContainerUnitGeneralCapacityLimit()) {
                    result = getContainerUnitGeneralCapacityLimit();
                }

                if (useContainedUnits) {
                    if (amountResourceRemainingCache == null) {
                        amountResourceRemainingCache = new ConcurrentHashMap<AmountResource, Double>(10);
                    }
                    amountResourceRemainingCache.put(resource, result);
                }
            }
            return result;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Store an amount of a resource.
     * @param resource the resource.
     * @param amount the amount (kg).
     * @throws InventoryException if error storing resource.
     */
    public synchronized void storeAmountResource(AmountResource resource, double amount,
            boolean useContainedUnits) {

        if (amount < 0D) {
            throw new IllegalStateException("Cannot store negative amount of resource: " + amount);
        }
        if (amount > 0D) {
            if (amount <= getAmountResourceRemainingCapacity(resource, useContainedUnits)) {
                double remainingAmount = amount;

                // Store resource in local resource storage.
                if (resourceStorage != null) {
                    double remainingStorageCapacity = resourceStorage.getAmountResourceRemainingCapacity(resource);
                    double storageAmount = remainingAmount;
                    if (storageAmount > remainingStorageCapacity) {
                        storageAmount = remainingStorageCapacity;
                    }
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
                                    resource, true);
                            double storageAmount = remainingAmount;
                            if (storageAmount > remainingUnitCapacity) {
                                storageAmount = remainingUnitCapacity;
                            }
                            if (storageAmount > 0D) {
                                unitInventory.storeAmountResource(resource, storageAmount, true);
                                remainingAmount -= storageAmount;
                            }
                        }
                    }
                }

                if (remainingAmount <= .000001D) {
                    clearAmountResourceStoredCache();
                } else {
                    throw new IllegalStateException(resource.getName()
                            + " could not be totally stored. Remaining: " + remainingAmount);
                }

                if (owner != null) {
                    owner.fireUnitUpdate(INVENTORY_RESOURCE_EVENT, resource);
                }
            } else {
                throw new IllegalStateException("Insufficiant capacity to store " + resource.getName() + ", capacity: "
                        + getAmountResourceRemainingCapacity(resource, useContainedUnits) + ", attempted: " + amount);
            }
        }
    }

    /**
     * Retrieves an amount of a resource from storage.
     * @param resource the resource.
     * @param amount the amount (kg).
     */
    public synchronized void retrieveAmountResource(AmountResource resource, double amount) {
        
        if (amount <= getAmountResourceStored(resource)) {
            double remainingAmount = amount;

            // Retrieve from local resource storage.
            if (resourceStorage != null) {
                double resourceStored = resourceStorage.getAmountResourceStored(resource);
                double retrieveAmount = remainingAmount;
                if (retrieveAmount > resourceStored) {
                    retrieveAmount = resourceStored;
                }
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
                        double resourceStored = unitInventory.getAmountResourceStored(resource);
                        double retrieveAmount = remainingAmount;
                        if (retrieveAmount > resourceStored) {
                            retrieveAmount = resourceStored;
                        }
                        unitInventory.retrieveAmountResource(resource, retrieveAmount);
                        remainingAmount -= retrieveAmount;
                    }
                }
            }

            if (remainingAmount <= .0000001D) {
                clearAmountResourceStoredCache();
            } else {
                throw new IllegalStateException(resource.getName()
                        + " could not be totally retrieved. Remaining: " + remainingAmount);
            }

            if (owner != null) {
                owner.fireUnitUpdate(INVENTORY_RESOURCE_EVENT, resource);
            }
        } else {
            throw new IllegalStateException("Insufficiant stored amount to retrieve " + resource.getName()
                    + ", stored: " + getAmountResourceStored(resource) + ", attempted: " + amount);
        }
    }

    /**
     * Adds a capacity to general capacity.
     * @param capacity amount capacity (kg).
     */
    public synchronized void addGeneralCapacity(double capacity) {
        generalCapacity += capacity;
    }

    /**
     * Gets the general capacity.
     * @return amount capacity (kg).
     */
    public synchronized double getGeneralCapacity() {
        return generalCapacity;
    }

    /**
     * Gets the mass stored in general capacity.
     * @return stored mass (kg).
     * @throws InventoryException if error getting stored mass.
     */
    public synchronized double getGeneralStoredMass() {

        return getItemResourceTotalMass() + getUnitTotalMass();
    }

    /**
     * Gets the remaining general capacity available.
     * @return amount capacity (kg).
     * @throws InventoryException if error getting remaining capacity.
     */
    public synchronized double getRemainingGeneralCapacity() {

        double result = generalCapacity - getGeneralStoredMass();
        if (result > getContainerUnitGeneralCapacityLimit()) {
            result = getContainerUnitGeneralCapacityLimit();
        }
        return result;
    }

    /**
     * Checks if storage has an item resource.
     * @param resource the resource.
     * @return true if has resource.
     * @throws InventoryException if error checking resource.
     */
    public synchronized boolean hasItemResource(ItemResource resource) {
        
        try {
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
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Gets the number of an item resource in storage.
     * @param resource the resource.
     * @return number of resources.
     * @throws InventoryException if error getting item resource.
     */
    public synchronized int getItemResourceNum(ItemResource resource) {
        
        int result = 0;
        if (containedItemResources != null){
            final Integer res = containedItemResources.get(resource);
            if(res != null){
                result += res;
            }
        }
        if (containedUnits != null) {
            for(Unit u : containedUnits){
                if (u instanceof Container) {
                    result+=u.getInventory().getItemResourceNum(resource);
                }
            }
        }
        return result;
    }

    /**
     * Gets a set of all the item resources in storage.
     * @return set of item resources.
     * @throws InventoryException if error getting all item resources.
     */
    public synchronized Set<ItemResource> getAllItemResourcesStored() {
        
        if (containedItemResources != null) {
            //return Collections.synchronizedSet(new HashSet<ItemResource>(containedItemResources.keySet()));
            return new HashSet<ItemResource>(containedItemResources.keySet());
        } else {
            //return Collections.synchronizedSet(new HashSet<ItemResource>(0));
            return new HashSet<ItemResource>(0);
        }
    }

    /**
     * Gets the total mass of item resources in storage.
     * @return the total mass (kg).
     * @throws InventoryException if error getting total mass.
     */
    private double getItemResourceTotalMass() {
        
        double result = 0D;
        if (containedItemResources != null) {
            final Set<Entry<ItemResource, Integer>> es = containedItemResources.entrySet();
            for(Entry<ItemResource, Integer> e : es){
                result += e.getValue() * e.getKey().getMassPerItem();
            }
        }
        return result;
    }

    /**
     * Stores item resources.
     * @param resource the resource to store.
     * @param number the number of resources to store.
     * @throws InventoryException if error storing the resources.
     */
    public synchronized void storeItemResources(ItemResource resource, int number) {
        
        if (number < 0) {
            throw new IllegalStateException("Cannot store negative number of resources.");
        }
        double totalMass = resource.getMassPerItem() * number;
        if (totalMass <= getRemainingGeneralCapacity()) {
            if (containedItemResources == null) {
                containedItemResources = new ConcurrentHashMap<ItemResource, Integer>();
            }
            int totalNum = number + getItemResourceNum(resource);
            if (totalNum > 0) {
                containedItemResources.put(resource, totalNum);
            }

            if (owner != null) {
                owner.fireUnitUpdate(INVENTORY_RESOURCE_EVENT, resource);
            }
        } else {
            throw new IllegalStateException("Could not store item resources.");
        }
    }

    /**
     * Retrieves item resources.
     * @param resource the resource to retrieve.
     * @param number the number of resources to retrieve.
     * @throws InventoryException if error retrieving the resources.
     */
    public synchronized void retrieveItemResources(ItemResource resource, int number) {
        
        if (number < 0) {
            throw new IllegalStateException("Cannot retrieve negative number of resources.");
        }
        if (hasItemResource(resource) && (number <= getItemResourceNum(resource))) {
            int remainingNum = number;

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

            // Retrieve resources from contained units.
            if ((remainingNum > 0) && (containedUnits != null)) {
                Iterator<Unit> i = containedUnits.iterator();
                while (i.hasNext()) {
                    Inventory unitInventory = i.next().getInventory();
                    if (unitInventory.hasItemResource(resource)) {
                        int storedUnit = unitInventory.getItemResourceNum(resource);
                        int retrieveNum = remainingNum;
                        if (retrieveNum > storedUnit) {
                            retrieveNum = storedUnit;
                        }
                        unitInventory.retrieveItemResources(resource, retrieveNum);
                        remainingNum -= retrieveNum;
                    }
                }
            }
            if (owner != null) {
                owner.fireUnitUpdate(INVENTORY_RESOURCE_EVENT, resource);
            }

            if (remainingNum > 0) {
                throw new IllegalStateException(resource.getName()
                        + " could not be totally retrieved. Remaining: " + remainingNum);
            }
        } else {
            throw new IllegalStateException("Insufficiant stored number to retrieve " + resource.getName()
                    + ", stored: " + getItemResourceNum(resource) + ", attempted: " + number);
        }
    }

    /** 
     * Gets the total unit mass in storage.
     * @return total mass (kg).
     * @throws InventoryException if error getting mass.
     */
    public synchronized double getUnitTotalMass() {

        double totalMass = 0D;
        if (containedUnits != null) {
            Iterator<Unit> unitIt = containedUnits.iterator();
            while (unitIt.hasNext()) {
                totalMass += unitIt.next().getMass();
            }
        }
        return totalMass;
    }

    /** 
     * Gets a collection of all the stored units.
     * @return Collection of all units
     */
    public synchronized Collection<Unit> getContainedUnits() {
        
        if (containedUnits != null) {
            return containedUnits;
        } else {
            return Collections.emptySet();
        }
    }

    /** 
     * Checks if a unit is in storage.
     * @param unit the unit.
     * @return true if unit is in storage.
     */
    public synchronized boolean containsUnit(Unit unit) {
        
        boolean result = false;
        if (containedUnits != null) {
            // See if this unit contains the unit in question.
            if (containedUnits.contains(unit)) {
                result = true;
            }
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
    public synchronized boolean containsUnitClass(Class<? extends Unit> unitClass) {
        
        boolean result = false;
        if (containedUnits != null) {
            // Check if unit of class is in inventory.
            if (containsUnitClassLocal(unitClass)) {
                result = true;
            }
        }
        return result;
    }

    /**
     * Finds a unit of a given class in storage.
     * @param unitClass the unit class.
     * @return the instance of the unit class or null if none.
     */
    public synchronized Unit findUnitOfClass(Class<? extends Unit> unitClass) {
        
        Unit result = null;
        if (containsUnitClass(unitClass)) {
            Iterator<Unit> i = containedUnits.iterator();
            while (i.hasNext()) {
                Unit unit = i.next();
                if (unitClass.isInstance(unit)) {
                    result = unit;
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
    public synchronized Collection<Unit> findAllUnitsOfClass(Class<? extends Unit> unitClass) {
        
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
    public synchronized int findNumUnitsOfClass(Class<? extends Unit> unitClass) {
        
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
     * @return number of empty units.
     * @throws InventoryException if error determining number of units.
     */
    public synchronized int findNumEmptyUnitsOfClass(Class<? extends Unit> unitClass) {
        
        int result = 0;
        if (containsUnitClass(unitClass)) {
            Iterator<Unit> i = containedUnits.iterator();
            while (i.hasNext()) {
                Unit unit = i.next();
                if (unitClass.isInstance(unit)) {
                    Inventory inv = unit.getInventory();
                    if ((inv != null) && inv.isEmpty()) {
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
     * @return true if unit can be added to inventory
     * @throws InventoryException if error checking unit.
     */
    public synchronized boolean canStoreUnit(Unit unit) {
        
        boolean result = false;
        if (unit != null) {
            if (unit.getMass() <= getRemainingGeneralCapacity()) {
                result = true;
            }
            if (unit == owner) {
                result = false;
            }
            if ((containedUnits != null) && containsUnit(unit)) {
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
     * @throws InventoryException if unit could not be stored.
     */
    public synchronized void storeUnit(Unit unit) {
        
        if (canStoreUnit(unit)) {
            if (containedUnits == null) {
                containedUnits = new ConcurrentLinkedQueue<Unit>();
            }

            containedUnits.add(unit);
            unit.setContainerUnit(owner);

            // Try to empty amount resources into parent if container.
            if (unit instanceof Container) {
                Inventory containerInv = unit.getInventory();
                Iterator<AmountResource> i = containerInv.getAllAmountResourcesStored().iterator();
                while (i.hasNext()) {
                    AmountResource resource = i.next();
                    double containerAmount = containerInv.getAmountResourceStored(resource);
                    if (getAmountResourceRemainingCapacity(resource, false) >= containerAmount) {
                        containerInv.retrieveAmountResource(resource, containerAmount);
                        storeAmountResource(resource, containerAmount, false);
                    }
                }
            }

            clearAmountResourceCapacityCache();
            clearAmountResourceStoredCache();
            if (owner != null) {
                unit.setCoordinates(owner.getCoordinates());
                owner.fireUnitUpdate(INVENTORY_STORING_UNIT_EVENT, unit);
                Iterator<AmountResource> i = unit.getInventory().getAllAmountResourcesStored().iterator();
                while (i.hasNext()) {
                    owner.fireUnitUpdate(INVENTORY_RESOURCE_EVENT, i.next());
                }
                Iterator<ItemResource> j = unit.getInventory().getAllItemResourcesStored().iterator();
                while (j.hasNext()) {
                    owner.fireUnitUpdate(INVENTORY_RESOURCE_EVENT, j.next());
                }
            }
        } else {
            throw new IllegalStateException("Unit: " + unit + " could not be stored.");
        }
    }

    /**
     * Retrieves a unit from storage.
     * @param unit the unit.
     * @throws InventoryException if unit could not be retrieved.
     */
    public synchronized void retrieveUnit(Unit unit) {
        
        boolean retrieved = false;
        if (containsUnit(unit)) {
            if (containedUnits.contains(unit)) {
                containedUnits.remove(unit);
                clearAmountResourceCapacityCache();
                clearAmountResourceStoredCache();
                if (owner != null) {
                    owner.fireUnitUpdate(INVENTORY_RETRIEVING_UNIT_EVENT, unit);

                    Iterator<AmountResource> i = unit.getInventory().getAllAmountResourcesStored().iterator();
                    while (i.hasNext()) {
                        owner.fireUnitUpdate(INVENTORY_RESOURCE_EVENT, i.next());
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
    public synchronized void setCoordinates(Coordinates newLocation) {
        
        if (containedUnits != null) {
            Iterator<Unit> i = containedUnits.iterator();
            while (i.hasNext()) {
                i.next().setCoordinates(newLocation);
            }
        }
    }

    /**
     * Gets the total mass stored in inventory.
     * @return stored mass (kg).
     * @throws InventoryException if error getting mass.
     */
    public synchronized double getTotalInventoryMass() {
        
        double result = 0D;

        // Add total amount resource mass stored.
        result += getTotalAmountResourcesStored();

        // Add general storage mass.
        result += getGeneralStoredMass();

        return result;
    }

    /**
     * Checks if inventory is empty.
     * @return true if empty.
     * @throws InventoryException if error checking inventory.
     */
    public synchronized boolean isEmpty() {
        
        return (getTotalInventoryMass() == 0D);
    }

    /**
     * Gets any limits in the owner's general capacity.
     * @return owner general capacity limit (kg).
     * @throws InventoryException if error getting capacity.
     */
    private double getContainerUnitGeneralCapacityLimit() {
        
        double result = Double.MAX_VALUE;
        if ((owner != null) && (owner.getContainerUnit() != null)) {
            Inventory containerInv = owner.getContainerUnit().getInventory();
            if (containerInv.getRemainingGeneralCapacity() < result) {
                result =
                        containerInv.getRemainingGeneralCapacity();
            }
            if (containerInv.getContainerUnitGeneralCapacityLimit() < result) {
                result =
                        containerInv.getContainerUnitGeneralCapacityLimit();
            }
        }
        return result;
    }

    /**
     * Clears the amount resource capacity cache as well as the container's cache if any.
     */
    private void clearAmountResourceCapacityCache() {
        
        if (amountResourceCapacityCache != null) {
            amountResourceCapacityCache.clear();
        }
        if (owner != null) {
            Unit container = owner.getContainerUnit();
            if (container != null) {
                container.getInventory().clearAmountResourceCapacityCache();
            }
        }
        if (amountResourceRemainingCache != null) {
            amountResourceRemainingCache.clear();
        }
    }

    /**
     * Clears the amount resource stored cache as well as the container's cache if any.
     */
    private void clearAmountResourceStoredCache() {
        
        if (amountResourceStoredCache != null) {
            amountResourceStoredCache.clear();
        }

        if (allStoredAmountResourcesCache != null) {
            synchronized(allStoredAmountResourcesCache) {
                allStoredAmountResourcesCache.clear();
                allStoredAmountResourcesCache = null;
            }
        }
        totalAmountResourcesStored = -1D;
        totalAmountResourcesStoredSet = false;

        if (owner != null) {
            Unit container = owner.getContainerUnit();
            if (container != null) {
                container.getInventory().clearAmountResourceStoredCache();
            }
        }

        if (amountResourceRemainingCache != null) {
            amountResourceRemainingCache.clear();
        }
    }

    /**
     * Creates a clone of this inventory (not including the inventory contents).
     * @param owner the unit owner of the inventory (or null).
     * @return inventory clone.
     * @throws InventoryException if error creating inventory clone.
     */
    public synchronized Inventory clone(Unit owner) {
        
        Inventory result = new Inventory(owner);
        result.addGeneralCapacity(generalCapacity);

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

        return result;
    }

    /**
     * Prepare object for garbage collection.
     */
    public synchronized void destroy() {
        
        owner = null;
        if (containedUnits != null) containedUnits.clear();
        containedUnits = null;
        if (containedItemResources != null) containedItemResources.clear();
        containedItemResources = null;
        if (resourceStorage != null) resourceStorage.destroy();
        resourceStorage = null;
        if (amountResourceCapacityCache != null) amountResourceCapacityCache.clear();
        amountResourceCapacityCache = null;
        if (amountResourceStoredCache != null) amountResourceStoredCache.clear();
        amountResourceStoredCache = null;
        if (allStoredAmountResourcesCache != null) allStoredAmountResourcesCache.clear();
        allStoredAmountResourcesCache = null;
        if (amountResourceRemainingCache != null) amountResourceRemainingCache.clear();
        amountResourceRemainingCache = null;
    }
}