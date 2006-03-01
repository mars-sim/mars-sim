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
    private transient double totalAmountCache = 0D; // Cache value for the total amount of resources stored.
    
    /**
     * Adds capacity for a resource type.
     * @param resource the resource.
     * @param capacity the capacity amount (kg).
     * @throws ResourceException if error setting capacity.
     */
    void addAmountResourceTypeCapacity(AmountResource resource, double capacity) throws ResourceException {
    	if (capacity < 0D) throw new ResourceException("Cannot add negative type capacity: " + capacity);
    	if (amountResourceTypeCapacities == null) amountResourceTypeCapacities = new HashMap();
    	if (hasAmountResourceTypeCapacity(resource)) {
    		ResourceAmount existingCapacity = (ResourceAmount) amountResourceTypeCapacities.get(resource);
    		existingCapacity.setAmount(existingCapacity.getAmount() + capacity);
    	}
    	else amountResourceTypeCapacities.put(resource, new ResourceAmount(capacity));
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
    		result = ((ResourceAmount) amountResourceTypeCapacities.get(resource)).getAmount();
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
    		ResourceAmount amount = (ResourceAmount) amountResourceTypeStored.get(resource);
    		if (amount != null) result = amount.getAmount();
    	}
    	return result;
    }
    
    /**
     * Gets the amount of a resource type stored.
     * @param resource the resource.
     * @return stored amount as Double (kg).
     */
    private ResourceAmount getAmountResourceTypeStoredObject(AmountResource resource) {
    	if (amountResourceTypeStored != null) return (ResourceAmount) amountResourceTypeStored.get(resource);
    	else return null;
    }
    
    /**
     * Gets the total amount of resources stored.
     * @return stored amount (kg).
     */
    double getTotalAmountResourceTypesStored() {
    	return totalAmountCache;
    }
    
    /**
     * Updates the total amount of resources stored.
     */
    private void updateTotalAmountResourceTypesStored() {
    	totalAmountCache = 0D;
    	if (amountResourceTypeStored != null) {
    		Iterator i = amountResourceTypeStored.keySet().iterator();
    		while (i.hasNext()) totalAmountCache += getAmountResourceTypeStored((AmountResource) i.next());
    	}
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
    	if (amount > 0D) {
    		if (getAmountResourceTypeRemainingCapacity(resource) >= amount) {
    			if (amountResourceTypeStored == null) amountResourceTypeStored = new HashMap();
    			ResourceAmount stored = getAmountResourceTypeStoredObject(resource);
    			if (stored != null) {
    				stored.setAmount(stored.getAmount() + amount);
    			}
    			else {
    				amountResourceTypeStored.put(resource, new ResourceAmount(amount));
    			}
    			
    			// Update total resources amount cache.
    			updateTotalAmountResourceTypesStored();
    		}
    		else throw new ResourceException("Amount resource could not be added in type storage.");
    	}
    }
    
    /**
     * Retrieves an amount of a resource type from storage.
     * @param resource the resource.
     * @param amount the amount (kg).
     * @throws ResourceException if error retrieving resource.
     */
    void retrieveAmountResourceType(AmountResource resource, double amount) throws ResourceException {
    	if (amount < 0D) throw new ResourceException("Cannot retrieve negative amount of type: " + amount); 
    	if (getAmountResourceTypeStored(resource) >= amount) {
    		ResourceAmount stored = getAmountResourceTypeStoredObject(resource);
    		stored.setAmount(stored.getAmount() - amount);
    		
    		// Update total resources amount cache.
			updateTotalAmountResourceTypesStored();
    	}
    	else throw new ResourceException("Amount resource (" + resource.getName() +  
    			":" + amount + ") could not be retrieved from type storage");
    }
    
    /**
     * Internal class for storing type resource amounts.
     */
    private class ResourceAmount implements Serializable {
    	double amount;
    	
    	private ResourceAmount(double amount) {
    		this.amount = amount;
    	}
    	
    	private void setAmount(double amount) {
    		this.amount = amount;
    	}
    	
    	private double getAmount() {
    		return amount;
    	}
    }
}