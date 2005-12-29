/**
 * Mars Simulation Project
 * AmountResourceTypeStorage.java
 * @version 2.79 2005-12-09
 * @author Scott Davis 
 */

package org.mars_sim.msp.simulation.resource;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Storage for types of amount resource.
 */
class AmountResourceTypeStorage implements Serializable {

	// Data members
	private Map amountResourceTypeCapacities = null; // Capacity for each type of amount resource.
    private Map amountResourceTypeStored = null; // Stored resources by type.
    
    /**
     * Adds capacity for a resource type.
     * @param resource the resource.
     * @param capacity the capacity amount (kg).
     * @throws ResourceException if error setting capacity.
     */
    void addAmountResourceTypeCapacity(AmountResource resource, double capacity) throws ResourceException {
    	if (capacity < 0D) throw new ResourceException("Cannot add negative type capacity: " + capacity);
    	if (amountResourceTypeCapacities == null) amountResourceTypeCapacities = new HashMap(1);
    	double totalCapacity = capacity;
    	if (hasAmountResourceTypeCapacity(resource)) totalCapacity += getAmountResourceTypeCapacity(resource);
    	amountResourceTypeCapacities.put(resource, new Double(totalCapacity));
    }
    
    /**
     * Checks if storage has capacity for a resource type.
     * @param resource the resource.
     * @return true if storage capacity.
     */
    boolean hasAmountResourceTypeCapacity(AmountResource resource) {
    	if (amountResourceTypeCapacities != null) {
    		if (amountResourceTypeCapacities.containsKey(resource)) return true;
    	}
    	return false;
    }
    
    /**
     * Gets the storage capacity for a resource type.
     * @param resource the resource.
     * @return capacity amount (kg).
     */
    double getAmountResourceTypeCapacity(AmountResource resource) {
    	double result = 0D;
    	if (hasAmountResourceTypeCapacity(resource)) 
    		result = ((Double) amountResourceTypeCapacities.get(resource)).doubleValue();
    	return result;
    }
    
    /**
     * Gets the amount of a resource type stored.
     * @param resource the resource.
     * @return stored amount (kg).
     */
    double getAmountResourceTypeStored(AmountResource resource) {
    	double result = 0D;
    	if (hasAmountResourceTypeCapacity(resource) && (amountResourceTypeStored != null)) {
    		Double amount = (Double) amountResourceTypeStored.get(resource);
    		if (amount != null) result = amount.doubleValue();
    	}
    	return result;
    }
    
    /**
     * Gets the total amount of resources stored.
     * @return stored amount (kg).
     */
    double getTotalAmountResourceTypesStored() {
    	double result = 0D;
    	if (amountResourceTypeStored != null) {
    		Iterator i = amountResourceTypeStored.keySet().iterator();
    		while (i.hasNext()) result += getAmountResourceTypeStored((AmountResource) i.next());
    	}
    	return result;
    }
    
    /**
     * Gets a set of resources stored.
     * @return set of resources.
     */
    Set getAllAmountResourcesStored() {
    	Set result = new HashSet();
    	if (amountResourceTypeStored != null) {
    		Iterator i = amountResourceTypeStored.keySet().iterator();
    		while (i.hasNext()) {
    			AmountResource resource = (AmountResource) i.next();
    			if (getAmountResourceTypeStored(resource) > 0D) result.add(resource);
    		}
    	}
    	return result;
    }
    
    /**
     * Gets the remaining capacity available for a resource type.
     * @param resource the resource.
     * @return remaining capacity amount (kg).
     */
    double getAmountResourceTypeRemainingCapacity(AmountResource resource) {
    	double result = 0D;
    	if (hasAmountResourceTypeCapacity(resource)) 
    		result = getAmountResourceTypeCapacity(resource) - getAmountResourceTypeStored(resource);
    	return result;
    }
    
    /**
     * Store an amount of a resource type.
     * @param resource the resource.
     * @param amount the amount (kg).
     * @throws ResourceException if error storing resource.
     */
    void storeAmountResourceType(AmountResource resource, double amount) throws ResourceException {
    	if (amount < 0D) throw new ResourceException("Cannot store negative amount of type: " + amount);
    	if (getAmountResourceTypeRemainingCapacity(resource) >= amount) {
    		if (amountResourceTypeStored == null) amountResourceTypeStored = new HashMap(1);
    		amountResourceTypeStored.put(resource, new Double(getAmountResourceTypeStored(resource) + amount));
    	}
    	else throw new ResourceException("Amount resource could not be added in type storage.");
    }
    
    /**
     * Retrieves an amount of a resource type from storage.
     * @param resource the resource.
     * @param amount the amount (kg).
     * @throws ResourceException if error retrieving resource.
     */
    void retrieveAmountResourceType(AmountResource resource, double amount) throws ResourceException {
    	if (amount < 0D) throw new ResourceException("Cannot retrieve negative amount of type: " + amount); 
    	if (getAmountResourceTypeStored(resource) >= amount) 
    		amountResourceTypeStored.put(resource, new Double(getAmountResourceTypeStored(resource) - amount));
    	else throw new ResourceException("Amount resource (" + resource.getName() +  
    			":" + amount + ") could not be retrieved from type storage");
    }
}