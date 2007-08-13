/**
 * Mars Simulation Project
 * Resource.java
 * @version 2.79 2005-12-28
 * @author Scott Davis 
 */

package org.mars_sim.msp.simulation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.resource.AmountResourceStorage;
import org.mars_sim.msp.simulation.resource.ItemResource;
import org.mars_sim.msp.simulation.resource.Phase;
import org.mars_sim.msp.simulation.resource.ResourceException;

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
    private UnitCollection containedUnits = null; // Collection of units in inventory.
    private Map containedItemResources = null; // Map of item resources.
    private double generalCapacity = 0D; // General mass capacity of inventory.
    private AmountResourceStorage resourceStorage = null; // Resource storage.
    
    // Cache capacity variables.
    private transient Map amountResourceCapacityCache = new HashMap(10);
    private transient Map amountResourceStoredCache = new HashMap(10);
	private transient Set allStoredAmountResourcesCache = null;
	private transient double totalAmountResourcesStored = -1D;
	private transient Map amountResourceRemainingCache = new HashMap(10);
    
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
	public void addAmountResourceTypeCapacity(AmountResource resource, double capacity) throws InventoryException {
		if (resourceStorage == null) resourceStorage = new AmountResourceStorage();
		try {
			resourceStorage.addAmountResourceTypeCapacity(resource, capacity);
			clearAmountResourceCapacityCache();
		}
		catch (ResourceException e) {
			throw new InventoryException("Error adding resource type capacity: " + e.getMessage());
		}
	}
	
    /**
     * Adds capacity for a resource phase.
     * @param phase the phase
     * @param capacity the capacity amount (kg).
     * @throws InventoryException if error adding capacity.
     */
    public void addAmountResourcePhaseCapacity(Phase phase, double capacity) throws InventoryException {
    	if (resourceStorage == null) resourceStorage = new AmountResourceStorage();
    	try {
    		resourceStorage.addAmountResourcePhaseCapacity(phase, capacity);
    		clearAmountResourceCapacityCache();
    	}
    	catch (ResourceException e) {
    		throw new InventoryException("Error adding resource phase capacity: " + e.getMessage());
    	}
    }
	
    /**
     * Checks if storage has capacity for a resource.
     * @param resource the resource.
     * @return true if storage capacity.
     */
    public boolean hasAmountResourceCapacity(AmountResource resource) throws InventoryException {
    	try {
    		boolean result = false;
    		if ((amountResourceCapacityCache != null) && amountResourceCapacityCache.containsKey(resource)) {
    			double amountCapacity = ((Double) amountResourceCapacityCache.get(resource)).doubleValue();
    			result = (amountCapacity > 0D);
    		}
    		else {
    			if ((resourceStorage != null) && resourceStorage.hasAmountResourceCapacity(resource)) result = true;
    			else if ((containedUnits != null) && (getRemainingGeneralCapacity() > 0D)) {
    				UnitIterator i = containedUnits.iterator();
    				while (i.hasNext()) {
    					if (i.next().getInventory().hasAmountResourceCapacity(resource)) result = true;
    				}
    			}
    		}
    		return result;
    	}
    	catch (Exception e) {
    		throw new InventoryException(e);
    	}
    }
    
    /**
     * Checks if storage has capacity for an amount of a resource.
     * @param resource the resource.
     * @param amount the amount (kg).
     * @return true if storage capacity.
     * @throws InventoryException if error checking capacity.
     */
    public synchronized boolean hasAmountResourceCapacity(AmountResource resource, double amount) throws InventoryException {
    	try {
    		boolean result = false;
    		if ((amountResourceCapacityCache != null) && amountResourceCapacityCache.containsKey(resource)) {
    			double amountCapacity = ((Double) amountResourceCapacityCache.get(resource)).doubleValue();
    			result = (amountCapacity >= amount);
    		}
    		else {
    			double capacity = 0D;
    			if (resourceStorage != null) capacity += resourceStorage.getAmountResourceCapacity(resource);
    			if (amount < capacity) result = true;
    			else if ((containedUnits != null) && (getRemainingGeneralCapacity() > 0D)) {
    				double containedCapacity = 0D;
    				UnitIterator i = containedUnits.iterator();
    				while (i.hasNext()) containedCapacity += i.next().getInventory().getAmountResourceCapacity(resource);
    				if (containedCapacity > getGeneralCapacity()) containedCapacity = getGeneralCapacity();
    				capacity += containedCapacity;
    				if ((capacity + containedCapacity) > amount) result = true;
    			}
    			if (amountResourceCapacityCache == null) amountResourceCapacityCache = new HashMap(10);
    			amountResourceCapacityCache.put(resource, new Double(capacity));
    		}
    		return result;
    	}
    	catch (Exception e) {
    		throw new InventoryException(e);
    	}
    }
    
    /**
     * Gets the storage capacity for a resource.
     * @param resource the resource.
     * @return capacity amount (kg).
     * @throws InventoryException if error determining capacity.
     */
    public synchronized double getAmountResourceCapacity(AmountResource resource) throws InventoryException {
    	try {
    		double result = 0D;
    		if ((amountResourceCapacityCache != null) && amountResourceCapacityCache.containsKey(resource)) 
    			result = ((Double) amountResourceCapacityCache.get(resource)).doubleValue();
    		else {
    			if (hasAmountResourceCapacity(resource)) {
    				if (resourceStorage != null) result += resourceStorage.getAmountResourceCapacity(resource);
    				if ((containedUnits != null) && (generalCapacity > 0D)) {
    					double containedCapacity = 0D;
    					UnitIterator i = containedUnits.iterator();
    					while (i.hasNext()) containedCapacity += i.next().getInventory().getAmountResourceCapacity(resource);
    					if (containedCapacity > getGeneralCapacity()) containedCapacity = getGeneralCapacity();
    					result += containedCapacity;
    				}
    			}
    			if (amountResourceCapacityCache == null) amountResourceCapacityCache = new HashMap(10);
    			amountResourceCapacityCache.put(resource, new Double(result));
    		}
    		return result;
    	}
    	catch (Exception e) {
    		throw new InventoryException(e);
    	}
    }
    
    /**
     * Gets the amount of a resource stored.
     * @param resource the resource.
     * @return stored amount (kg).
     * @throws InventoryException if error getting amount stored.
     */
    public synchronized double getAmountResourceStored(AmountResource resource) throws InventoryException {
    	try {
    		double result = 0D;
    		if ((amountResourceStoredCache != null) && amountResourceStoredCache.containsKey(resource)) 
    			result = ((Double) amountResourceStoredCache.get(resource)).doubleValue();
    		else {
    			if (resourceStorage != null) result += resourceStorage.getAmountResourceStored(resource);
    			if (containedUnits != null) {
    				UnitIterator i = containedUnits.iterator();
    				while (i.hasNext()) result += i.next().getInventory().getAmountResourceStored(resource);
    			}
    			if (amountResourceStoredCache == null) amountResourceStoredCache = new HashMap(10);
    			amountResourceStoredCache.put(resource, new Double(result));
    		}
    		return result;
    	}
    	catch (Exception e) {
    		throw new InventoryException(e);
    	}
    }
    
    /**
     * Gets all of the amount resources stored.
     * @return set of amount resources.
     * @throws InventoryException if error getting all amount resources.
     */
    public synchronized Set getAllAmountResourcesStored() throws InventoryException {
    	try {
    		if (allStoredAmountResourcesCache != null) return new HashSet(allStoredAmountResourcesCache);
    		else {
    			allStoredAmountResourcesCache = new HashSet(1, 1);
    			if (resourceStorage != null) allStoredAmountResourcesCache.addAll(resourceStorage.getAllAmountResourcesStored());
    			if (containedUnits != null) {
    				UnitIterator i = containedUnits.iterator();
    				while (i.hasNext()) allStoredAmountResourcesCache.addAll(i.next().getInventory().getAllAmountResourcesStored());
    			}
    			return new HashSet(allStoredAmountResourcesCache);
    		}
    	}
    	catch(Exception e) {
    		throw new InventoryException(e);
    	}
    }
    
    /**
     * Gets the total mass of amount resources stored.
     * @return stored amount (kg).
     * throws InventoryException if error getting total amount resources stored.
     */
    private double getTotalAmountResourcesStored() throws InventoryException { 
    	try {
    		double result = 0D;
    		if (totalAmountResourcesStored >= 0D) result = totalAmountResourcesStored;
    		else {
    			if (resourceStorage != null) result += resourceStorage.getTotalAmountResourcesStored();
    			if (containedUnits != null) {
    				UnitIterator i = containedUnits.iterator();
    				while (i.hasNext()) result += i.next().getInventory().getTotalAmountResourcesStored();
    			}
    			totalAmountResourcesStored = result;
    		}
    		return result;
    	}
    	catch (Exception e) {
    		throw new InventoryException(e);
    	}
    }
    
    /**
     * Gets the remaining capacity available for a resource.
     * @param resource the resource.
     * @return remaining capacity amount (kg).
     * throws InventoryException if error getting remaining capacity.
     */
    public synchronized double getAmountResourceRemainingCapacity(AmountResource resource) throws InventoryException {
    	try {
    		double result = 0D;
    		if ((amountResourceRemainingCache != null) && amountResourceRemainingCache.containsKey(resource))
    			return ((Double) amountResourceRemainingCache.get(resource)).doubleValue();
    		else {
    			if (resourceStorage != null) result += resourceStorage.getAmountResourceRemainingCapacity(resource);
    			if (containedUnits != null) {
    				double containedRemainingCapacity = 0D;
    				UnitIterator i = containedUnits.iterator();
    				while (i.hasNext()) containedRemainingCapacity += i.next().getInventory().getAmountResourceRemainingCapacity(resource);
    				if (containedRemainingCapacity > getRemainingGeneralCapacity()) containedRemainingCapacity = getRemainingGeneralCapacity();
    				result += containedRemainingCapacity;
    			}
    			if (result > getContainerUnitGeneralCapacityLimit()) result = getContainerUnitGeneralCapacityLimit();
    		
    			if (amountResourceRemainingCache == null) amountResourceRemainingCache = new HashMap(10);
    			amountResourceRemainingCache.put(resource, new Double(result));
    		}
    		return result;
    	}
    	catch (Exception e) {
    		throw new InventoryException(e);
    	}
    }
    
    /**
     * Store an amount of a resource.
     * @param resource the resource.
     * @param amount the amount (kg).
     * @throws InventoryException if error storing resource.
     */
    public void storeAmountResource(AmountResource resource, double amount) throws InventoryException {
    	try {
    		if (amount < 0D) throw new InventoryException("Cannot store negative amount of resource: " + amount);
    		if (amount > 0D) {
    			if (amount <= getAmountResourceRemainingCapacity(resource)) {
    				double remainingAmount = amount;
    			
    				// Store resource in local resource storage.
    				if (resourceStorage != null) {
    					double remainingStorageCapacity = resourceStorage.getAmountResourceRemainingCapacity(resource);
    					double storageAmount = remainingAmount;
    					if (storageAmount > remainingStorageCapacity) storageAmount = remainingStorageCapacity;
    					resourceStorage.storeAmountResource(resource, storageAmount);
    					remainingAmount -= storageAmount;
    				}
    			
    				// Store remaining resource in contained units in general capacity.
    				if ((remainingAmount > 0D) && (containedUnits != null)) {
    					UnitIterator i = containedUnits.iterator();
    					while (i.hasNext()) {
    						Inventory unitInventory = i.next().getInventory();
    						double remainingUnitCapacity = unitInventory.getAmountResourceRemainingCapacity(resource);
    						double storageAmount = remainingAmount;
    						if (storageAmount > remainingUnitCapacity) storageAmount = remainingUnitCapacity;
    						unitInventory.storeAmountResource(resource, storageAmount);
    						remainingAmount -= storageAmount;
    					}
    				}
    				
    				if (remainingAmount <= 0D) clearAmountResourceStoredCache();
    				else throw new InventoryException(resource.getName() + 
    						" could not be totally stored. Remaining: " + remainingAmount);
    				
    				if (owner != null) owner.fireUnitUpdate(INVENTORY_RESOURCE_EVENT, resource);
    			}
    			else throw new InventoryException("Insufficiant capacity to store " + resource.getName() + ", capacity: " + 
    					getAmountResourceRemainingCapacity(resource) + ", attempted: " + amount);
    		}
    	}
    	catch (ResourceException e) {
    	 	throw new InventoryException("Error storing amount resource: " + e.getMessage());
    	}
    }
    
    /**
     * Retrieves an amount of a resource from storage.
     * @param resource the resource.
     * @param amount the amount (kg).
     * @throws InventoryException if error retrieving resource.
     */
    public void retrieveAmountResource(AmountResource resource, double amount) throws InventoryException {
    	try {
    		if (amount <= getAmountResourceStored(resource)) {
    			double remainingAmount = amount;
    			
    			// Retrieve from local resource storage.
    			if (resourceStorage != null) {
    				double resourceStored = resourceStorage.getAmountResourceStored(resource);
    				double retrieveAmount = remainingAmount;
    				if (retrieveAmount > resourceStored) retrieveAmount = resourceStored;
    				resourceStorage.retrieveAmountResource(resource, retrieveAmount);
    				remainingAmount -= retrieveAmount;
    			}
    			
    			// Retrieve remaining resource from contained units.
    			if ((remainingAmount > 0D) && (containedUnits != null)) {
       				UnitIterator i = containedUnits.iterator();
    	    		while (i.hasNext()) {
    	    			Inventory unitInventory = i.next().getInventory();
    	    			double resourceStored = unitInventory.getAmountResourceStored(resource);
    	    			double retrieveAmount = remainingAmount;
    	    			if (retrieveAmount > resourceStored) retrieveAmount = resourceStored;
    	    			unitInventory.retrieveAmountResource(resource, retrieveAmount);
    	    			remainingAmount -= retrieveAmount;
    	    		}
    			}
    			
    			if (remainingAmount <= 0D) clearAmountResourceStoredCache();
    			else throw new InventoryException(resource.getName() + 
            			" could not be totally retrieved. Remaining: " + remainingAmount);
    			
    			if (owner != null) owner.fireUnitUpdate(INVENTORY_RESOURCE_EVENT, resource);
        	}
        	else throw new InventoryException("Insufficiant stored amount to retrieve " + resource.getName() + 
        			", stored: " + getAmountResourceStored(resource) + ", attempted: " + amount);
    	}
    	catch (ResourceException e) {
    		throw new InventoryException("Error retrieving amount resource: " + e.getMessage());
    	}
    }

    /**
     * Adds a capacity to general capacity.
     * @param capacity amount capacity (kg).
     */
    public void addGeneralCapacity(double capacity) {
    	generalCapacity += capacity;
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
     * @return stored mass (kg).
     * @throws InventoryException if error getting stored mass.
     */
    public double getGeneralStoredMass() throws InventoryException {
    	try {
    		return getItemResourceTotalMass() + getUnitTotalMass();
    	}
    	catch (Exception e) {
    		throw new InventoryException(e);
    	}
    }
    
    /**
     * Gets the remaining general capacity available.
     * @return amount capacity (kg).
     * @throws InventoryException if error getting remaining capacity.
     */
    public double getRemainingGeneralCapacity() throws InventoryException {
    	try {
    		double result = getGeneralCapacity() - getGeneralStoredMass();
    		if (result > getContainerUnitGeneralCapacityLimit()) result = getContainerUnitGeneralCapacityLimit();
    		return result;
    	}
    	catch (Exception e) {
    		throw new InventoryException(e);
    	}
    }
    
    /**
     * Checks if a person has an item resource.
     * @param resource the resource.
     * @return true if has resource.
     * @throws InventoryException if error checking resource.
     */
    public boolean hasItemResource(ItemResource resource) throws InventoryException {
    	try {
    		boolean result = false;
    		if ((containedItemResources != null) && containedItemResources.containsKey(resource)) {
    			if (((Integer) containedItemResources.get(resource)).intValue() > 0) result = true;
    		}
    		else if (containedUnits != null) {
    			UnitIterator i = containedUnits.iterator();
    			while (i.hasNext()) {
    				if (i.next().getInventory().hasItemResource(resource)) result = true;
    			}
    		}
    		return result;
    	}
    	catch(Exception e) {
    		throw new InventoryException(e);
    	}
    }
    
    /**
     * Gets the number of an item resource in storage.
     * @param resource the resource.
     * @return number of resources.
     * @throws InventoryException if error getting item resource.
     */
    public int getItemResourceNum(ItemResource resource) throws InventoryException {
    	try {
    		int result = 0;
    		if ((containedItemResources != null) && containedItemResources.containsKey(resource)) 
    			result += ((Integer) containedItemResources.get(resource)).intValue();
    		if (containedUnits != null) {
    			UnitIterator i = containedUnits.iterator();
    			while (i.hasNext()) result += i.next().getInventory().getItemResourceNum(resource);
    		}
    		return result;
    	}
    	catch(Exception e) {
    		throw new InventoryException(e);
    	}
    }
    
    /**
     * Gets a set of all the item resources in storage.
     * @return set of item resources.
     * @throws InventoryException if error getting all item resources.
     */
    public Set getAllItemResourcesStored() throws InventoryException {
    	try {
    		if (containedItemResources != null) return new HashSet(containedItemResources.keySet());
    		else return new HashSet();
    	}
    	catch (Exception e) {
    		throw new InventoryException(e);
    	}
    }
    
    /**
     * Gets the total mass of item resources in storage.
     * @return the total mass (kg).
     * @throws InventoryException if error getting total mass.
     */
    private double getItemResourceTotalMass() throws InventoryException {
    	try {
    		double result = 0D;
    		if (containedItemResources != null) {
    			Iterator i = containedItemResources.keySet().iterator();
    			while (i.hasNext()) {
    				ItemResource resource = (ItemResource) i.next();
    				int resourceNum = getItemResourceNum(resource);
    				result += resourceNum * resource.getMassPerItem();
    			}
    		}
    		return result;
    	}
    	catch (Exception e) {
    		throw new InventoryException(e);
    	}
    }
    
    /**
     * Stores item resources.
     * @param resource the resource to store.
     * @param number the number of resources to store.
     * @throws InventoryException if error storing the resources.
     */
    public void storeItemResources(ItemResource resource, int number) throws InventoryException {
    	if (number < 0) throw new InventoryException("Cannot store negative number of resources.");
    	double totalMass = resource.getMassPerItem() * number;
    	if (totalMass <= getRemainingGeneralCapacity()) {
    		if (containedItemResources == null) containedItemResources = new HashMap();
    		int totalNum = number + getItemResourceNum(resource);
    		containedItemResources.put(resource, new Integer(totalNum));
    		
    		if (owner != null) owner.fireUnitUpdate(INVENTORY_RESOURCE_EVENT, resource);
    	}
    	else throw new InventoryException("Could not store item resources.");
    }
    
    /**
     * Retrieves item resources.
     * @param resource the resource to retrieve.
     * @param number the number of resources to retrieve.
     * @throws InventoryException if error retrieving the resources.
     */
    public void retrieveItemResources(ItemResource resource, int number) throws InventoryException {
    	if (number < 0) throw new InventoryException("Cannot retrieve negative number of resources.");
    	if (hasItemResource(resource) && (number <= getItemResourceNum(resource))) {
    		int remainingNum = number;
    		
    		// Retrieve resources from local storage.
    		if ((containedItemResources != null) &&  containedItemResources.containsKey(resource)) {
    			int storedLocal = ((Integer) containedItemResources.get(resource)).intValue();
    			int retrieveNum = remainingNum;
    			if (retrieveNum > storedLocal) retrieveNum = storedLocal;
    			containedItemResources.put(resource, new Integer(storedLocal - retrieveNum));
    			remainingNum -= retrieveNum;
    		}
    		
    		// Retrieve resources from contained units.
			if ((remainingNum > 0) && (containedUnits != null)) {
   				UnitIterator i = containedUnits.iterator();
	    		while (i.hasNext()) {
	    			Inventory unitInventory = i.next().getInventory();
	    			int storedUnit = unitInventory.getItemResourceNum(resource);
	    			int retrieveNum = remainingNum;
	    			if (retrieveNum > storedUnit) retrieveNum = storedUnit;
	    			unitInventory.retrieveItemResources(resource, retrieveNum);
	    			remainingNum -= retrieveNum;
	    		}
			}
			if (owner != null) owner.fireUnitUpdate(INVENTORY_RESOURCE_EVENT, resource);
			
        	if (remainingNum > 0) throw new InventoryException(resource.getName() + 
        			" could not be totally retrieved. Remaining: " + remainingNum);
    	}
    	else throw new InventoryException("Insufficiant stored number to retrieve " + resource.getName() + 
    			", stored: " + getItemResourceNum(resource) + ", attempted: " + number);
    }
    
    /** 
     * Gets the total unit mass in storage.
     * @return total mass (kg).
     * @throws InventoryException if error getting mass.
     */
    public double getUnitTotalMass() throws InventoryException {
    	try {
    		double totalMass = 0D;
    		if (containedUnits != null) {
    			UnitIterator unitIt = containedUnits.iterator();
    			while (unitIt.hasNext()) totalMass += unitIt.next().getMass();
    		}
    		return totalMass;
    	}
    	catch (Exception e) {
    		throw new InventoryException(e);
    	}
    }

    /** 
     * Gets a collection of all the stored units.
     * @return UnitCollection of all units
     */
    public UnitCollection getContainedUnits() {
    	if (containedUnits != null) return new UnitCollection(containedUnits);
    	return new UnitCollection();
    }
    
    /** 
     * Checks if a unit is in storage.
     * @param unit the unit.
     * @return true if unit is in storage.
     */
    public boolean containsUnit(Unit unit) {
        boolean result = false;
        if (containedUnits != null) {
        	// See if this unit contains the unit in question.
        	if (containedUnits.contains(unit)) result = true;
        }
        return result;
    }

    /**
     * Checks if any of a given class of unit is in storage.
     * @param unitClass the unit class.
     * @return true if class of unit is in storage.
     */
    private boolean containsUnitClassLocal(Class unitClass) {
    	boolean result = false;
    	if (containedUnits != null) {
    		UnitIterator i = containedUnits.iterator();
    		while (i.hasNext()) {
    			if (unitClass.isInstance(i.next())) result = true;
    		}
    	}
        return result;
    }
    
    /**
     * Checks if any of a given class of unit is in storage.
     * @param unitClass the unit class.
     * @return if class of unit is in storage.
     */
    public boolean containsUnitClass(Class unitClass) {
    	boolean result = false;
    	if (containedUnits != null) {
    		// Check if unit of class is in inventory.
    		if (containsUnitClassLocal(unitClass)) result = true;
    	}
    	return result;
    }
    
    /**
     * Finds a unit of a given class in storage.
     * @param unitClass the unit class.
     * @return the instance of the unit class or null if none.
     */
    public Unit findUnitOfClass(Class unitClass) {
    	Unit result = null;
        if (containsUnitClass(unitClass)) {
            UnitIterator i = containedUnits.iterator();
            while (i.hasNext()) {
                Unit unit = i.next();
                if (unitClass.isInstance(unit)) result = unit;
            }
        }
        return result;
    }
    
    /**
     * Finds all of the units of a class in storage.
     * @param unitClass the unit class.
     * @return collection of units or empty collection if none.
     */
    public UnitCollection findAllUnitsOfClass(Class unitClass) {
    	UnitCollection result = new UnitCollection();
        if (containsUnitClass(unitClass)) {
            UnitIterator i = containedUnits.iterator();
            while (i.hasNext()) {
                Unit unit = i.next();
                if (unitClass.isInstance(unit)) result.add(unit);
            }
        }
        return result;
    }
    
    /**
     * Finds the number of units of a class that are contained in storage.
     * @param unitClass the unit class.
     * @return number of units
     */
    public int findNumUnitsOfClass(Class unitClass) {
    	int result = 0;
    	if (containsUnitClass(unitClass)) {
    		UnitIterator i = containedUnits.iterator();
    		while (i.hasNext()) {
    			Unit unit = i.next();
    			if (unitClass.isInstance(unit)) result++;
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
    public boolean canStoreUnit(Unit unit) throws InventoryException {
    	try {
    		boolean result = false;
    		if (unit != null) {
    			if (unit.getMass() <= getRemainingGeneralCapacity()) result = true;
    			if (unit == owner) result = false;
    			if ((containedUnits != null) && containsUnit(unit)) result = false;
    			if (unit.getInventory().containsUnit(owner)) result = false;
    		}
    		return result;
    	}
    	catch (Exception e) {
    		throw new InventoryException(e);
    	}
    }
    
    /** 
     * Stores a unit.
     * @param unit the unit
     * @throws InventoryException if unit could not be stored.
     */
    public void storeUnit(Unit unit) throws InventoryException {
        if (canStoreUnit(unit)) {
        	if (containedUnits == null) containedUnits = new UnitCollection();
            containedUnits.add(unit);
            unit.setContainerUnit(owner);
            clearAmountResourceCapacityCache();
        	clearAmountResourceStoredCache();
            if (owner != null) {
            	unit.setCoordinates(owner.getCoordinates());
            	owner.fireUnitUpdate(INVENTORY_STORING_UNIT_EVENT, unit); 
            	Iterator i = unit.getInventory().getAllAmountResourcesStored().iterator();
            	while (i.hasNext()) owner.fireUnitUpdate(INVENTORY_RESOURCE_EVENT, (AmountResource) i.next());
            	Iterator j = unit.getInventory().getAllItemResourcesStored().iterator();
            	while (j.hasNext()) owner.fireUnitUpdate(INVENTORY_RESOURCE_EVENT, (ItemResource) j.next());
            }
        }
        else throw new InventoryException("Unit: " + unit + " could not be stored.");
    }
    
    /**
     * Retrieves a unit from storage.
     * @param unit the unit.
     * @throws InventoryException if unit could not be retrieved.
     */
    public void retrieveUnit(Unit unit) throws InventoryException {
    	boolean retrieved = false;
    	if (containsUnit(unit)) {
    		if (containedUnits.contains(unit)) {
    			containedUnits.remove(unit);
    			clearAmountResourceCapacityCache();
            	clearAmountResourceStoredCache();
    			if (owner != null) {
    				owner.fireUnitUpdate(INVENTORY_RETRIEVING_UNIT_EVENT, unit);
    				Iterator i = unit.getInventory().getAllAmountResourcesStored().iterator();
    				while (i.hasNext()) owner.fireUnitUpdate(INVENTORY_RESOURCE_EVENT, (AmountResource) i.next());
    				Iterator j = unit.getInventory().getAllItemResourcesStored().iterator();
    				while (j.hasNext()) owner.fireUnitUpdate(INVENTORY_RESOURCE_EVENT, (ItemResource) j.next());
    			}
    			retrieved = true;
    		}
    	}
    	if (retrieved) unit.setContainerUnit(null);
    	else throw new InventoryException("Unit: " + unit + " could not be retrived.");
    }

    /**
     * Sets the coordinates of all units in the inventory.
     * @param newLocation the new coordinate location
     */
    public void setCoordinates(Coordinates newLocation) {
    	if (containedUnits != null) {
    		UnitIterator i = containedUnits.iterator();
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
    public double getTotalInventoryMass() throws InventoryException {
    	double result = 0D;
    	
    	// Add total amount resource mass stored.
    	result+= getTotalAmountResourcesStored();
    	
    	// Add general storage mass.
    	result+= getGeneralStoredMass();
    	
    	return result;
    }
    
    /**
     * Gets any limits in the owner's general capacity.
     * @return owner general capacity limit (kg).
     * @throws InventoryException if error getting capacity.
     */
    private double getContainerUnitGeneralCapacityLimit() throws InventoryException {
    	double result = Double.MAX_VALUE;
    	if ((owner != null) && (owner.getContainerUnit() != null)) {
    		Inventory containerInv = owner.getContainerUnit().getInventory();
    		if (containerInv.getRemainingGeneralCapacity() < result) result = 
    			containerInv.getRemainingGeneralCapacity();
    		if (containerInv.getContainerUnitGeneralCapacityLimit() < result) result = 
    			containerInv.getContainerUnitGeneralCapacityLimit();
    	}
    	return result;
    }
    
    /**
     * Clears the amount resource capacity cache as well as the container's cache if any.
     */
    private void clearAmountResourceCapacityCache() {
    	if (amountResourceCapacityCache != null) amountResourceCapacityCache.clear();
    	if (owner != null) {
    		Unit container = owner.getContainerUnit();
    		if (container != null) container.getInventory().clearAmountResourceCapacityCache();
    	}
    	if (amountResourceRemainingCache != null) amountResourceRemainingCache.clear();
    }
    
    /**
     * Clears the amount resource stored cache as well as the container's cache if any.
     */
    private void clearAmountResourceStoredCache() {
    	if (amountResourceStoredCache != null) amountResourceStoredCache.clear();
    	
    	if (allStoredAmountResourcesCache != null) {
    		allStoredAmountResourcesCache.clear();
    		allStoredAmountResourcesCache = null;
    	}
    	totalAmountResourcesStored = -1D;
    	
    	if (owner != null) {
    		Unit container = owner.getContainerUnit();
    		if (container != null) container.getInventory().clearAmountResourceStoredCache();
    	}
    	
    	if (amountResourceRemainingCache != null) amountResourceRemainingCache.clear();
    }
    
    /**
     * Creates a clone of this inventory (not including the inventory contents).
     * @param owner the unit owner of the inventory (or null).
     * @return inventory clone.
     * @throws InventoryException if error creating inventory clone.
     */
    public Inventory clone(Unit owner) throws InventoryException {
    	Inventory result = new Inventory(owner);
    	result.addGeneralCapacity(getGeneralCapacity());
    	
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
}