/**
 * Mars Simulation Project
 * AmountResourceStorage.java
 * @version 3.03 2012-07-07
 * @author Scott Davis 
 */

package org.mars_sim.msp.core.resource;

import java.io.Serializable;
import java.util.*;
import java.util.logging.Logger;

/**
 * Storage for amount resources.
 */
public class AmountResourceStorage implements Serializable {
	
	private static Logger logger = Logger.getLogger(AmountResourceStorage.class.getName());
	
	// Domain members
	private AmountResourceTypeStorage typeStorage = null;
	private AmountResourcePhaseStorage phaseStorage = null;
	
	// Cache values
	private transient AmountResource resourceCapacityKeyCache = null;
	private transient double resourceCapacityCache = 0D;
	private transient AmountResource resourceStoredKeyCache = null;
	private transient double resourceStoredCache = 0D;
	private transient Set<AmountResource> allStoredResourcesCache = null;
	private transient double totalResourcesStored = -1D;
	private transient boolean totalResourcesStoredSet = false;
	
    /**
     * Adds capacity for a resource type.
     * @param resource the resource.
     * @param capacity the capacity amount (kg).
     * @throws ResourceException if error setting capacity.
     */
	public void addAmountResourceTypeCapacity(AmountResource resource, double capacity) {
		if (typeStorage == null) typeStorage = new AmountResourceTypeStorage();
		typeStorage.addAmountResourceTypeCapacity(resource, capacity);
		resourceCapacityKeyCache = null;
	}
	
	/**
	 * Gets the amount resources and the type capacity for them.
	 * @return map of all amount resources that have type capacity.
	 */
    public Map<AmountResource, Double> getAmountResourceTypeCapacities() {
    	Map<AmountResource, Double> typeCapacities = new HashMap<AmountResource, Double>();
    	
    	if (typeStorage != null) {
    		Iterator<AmountResource> i = AmountResource.getAmountResources().iterator();
    		while (i.hasNext()) {
    			AmountResource resource = i.next();
    			double capacity = typeStorage.getAmountResourceTypeCapacity(resource);
    			if (capacity > 0D) typeCapacities.put(resource, capacity);
    		}
    	}
    	
    	return typeCapacities;
    }
	
    /**
     * Adds capacity for a resource phase.
     * @param phase the phase
     * @param capacity the capacity amount (kg).
     * @throws ResourceException if error adding capacity.
     */
    public void addAmountResourcePhaseCapacity(Phase phase, double capacity) {
    	if (phaseStorage == null)  phaseStorage = new AmountResourcePhaseStorage();
    	phaseStorage.addAmountResourcePhaseCapacity(phase, capacity);
    	resourceCapacityKeyCache = null;
    }
    
    /**
     * Gets the phase capacities in storage.
     * @return map of phases with capacities.
     */
    public Map<Phase, Double> getAmountResourcePhaseCapacities() {
    	Map<Phase, Double> phaseCapacities = new HashMap<Phase, Double>();
    	
    	if (phaseStorage != null) {
    		Iterator<Phase> i = Phase.getPhases().iterator();
    		while (i.hasNext()) {
    			Phase phase = i.next();
    			double capacity = phaseStorage.getAmountResourcePhaseCapacity(phase);
    			if (capacity > 0D) phaseCapacities.put(phase, capacity);
    		}
    	}
    	
    	return phaseCapacities;
    }
	
    /**
     * Checks if storage has capacity for a resource.
     * @param resource the resource.
     * @return true if storage capacity.
     */
    public boolean hasAmountResourceCapacity(AmountResource resource) {
    	boolean result = false;
    	if ((resourceCapacityKeyCache != null) && resourceCapacityKeyCache.equals(resource)) {
    	    result = (resourceCapacityCache > 0D);
    	}
    	else {
    		if ((typeStorage != null) && typeStorage.hasAmountResourceTypeCapacity(resource)) result = true;
    		else if ((phaseStorage != null) && phaseStorage.hasAmountResourcePhaseCapacity(resource.getPhase())) result = true;
    	}
    	return result;
    }
    
    /**
     * Gets the storage capacity for a resource.
     * @param resource the resource.
     * @return capacity amount (kg).
     */
    public double getAmountResourceCapacity(AmountResource resource) {
    	double result = 0D;
    	if ((resourceCapacityKeyCache != null) && resourceCapacityKeyCache.equals(resource)) {
    	    result = resourceCapacityCache;
    	}
    	else {
    		if ((typeStorage != null) && typeStorage.hasAmountResourceTypeCapacity(resource)) 
    			result = typeStorage.getAmountResourceTypeCapacity(resource);
    		if ((phaseStorage != null) && phaseStorage.hasAmountResourcePhaseCapacity(resource.getPhase())) {
    			if ((phaseStorage.getAmountResourcePhaseType(resource.getPhase()) == null) || 
    					phaseStorage.getAmountResourcePhaseType(resource.getPhase()).equals(resource)) 
    				result += phaseStorage.getAmountResourcePhaseCapacity(resource.getPhase());
    		}
    		resourceCapacityKeyCache = resource;
    		resourceCapacityCache = result;
    	}
    	return result;
    }
    
    /**
     * Gets the amount of a resource stored.
     * @param resource the resource.
     * @return stored amount (kg).
     */
    public double getAmountResourceStored(AmountResource resource) {
    	double result = 0D;
    	if ((resourceStoredKeyCache != null) && resourceStoredKeyCache.equals(resource)) {
    	    result = resourceStoredCache;
    	}
    	else {
    		if ((typeStorage != null) && typeStorage.hasAmountResourceTypeCapacity(resource)) 
    			result = typeStorage.getAmountResourceTypeStored(resource);
    		if ((phaseStorage != null) && resource.equals(phaseStorage.getAmountResourcePhaseType(resource.getPhase()))) 
    			result += phaseStorage.getAmountResourcePhaseStored(resource.getPhase());
    		resourceStoredKeyCache = resource;
    		resourceStoredCache = result;
    	}
    	
    	return result;
    }
    
    /**
     * Gets all of the amount resources stored.
     * @return set of amount resources.
     */
    public Set<AmountResource> getAllAmountResourcesStored() {
    	if (allStoredResourcesCache != null) return Collections.unmodifiableSet(allStoredResourcesCache);
    	else {
    		allStoredResourcesCache = new HashSet<AmountResource>();
            synchronized(allStoredResourcesCache) {
                if (typeStorage != null) allStoredResourcesCache.addAll(typeStorage.getAllAmountResourcesStored());
    		    if (phaseStorage != null) {
    			    Iterator<Phase> i = Phase.getPhases().iterator();
    			    while (i.hasNext()) {
    				    Phase phase = i.next();
    				    if (phaseStorage.getAmountResourcePhaseStored(phase) > 0D) 
    				        allStoredResourcesCache.add(phaseStorage.getAmountResourcePhaseType(phase));
    			    }
    		    }
    		    return Collections.unmodifiableSet(allStoredResourcesCache);
            }
    	}
    }
    
    /**
     * Gets the total amount of resources stored.
     * @return stored amount (kg).
     */
    public double getTotalAmountResourcesStored() {
    	if (totalResourcesStoredSet) return totalResourcesStored;
    	else {
    		totalResourcesStored = 0D;
    		if (typeStorage != null) totalResourcesStored += typeStorage.getTotalAmountResourceTypesStored();
    		if (phaseStorage != null) totalResourcesStored += phaseStorage.getTotalAmountResourcePhasesStored();
    		totalResourcesStoredSet = true;
    		return totalResourcesStored;
    	}
    }
    
    /**
     * Gets the remaining capacity available for a resource.
     * @param resource the resource.
     * @return remaining capacity amount (kg).
     */
    public double getAmountResourceRemainingCapacity(AmountResource resource) {
    	double result = 0D;
    	if (hasAmountResourceCapacity(resource)) {
    		result = getAmountResourceCapacity(resource) - getAmountResourceStored(resource);
    	}
    	return result;
    }
    
    /**
     * Store an amount of a resource.
     * @param resource the resource.
     * @param amount the amount (kg).
     * @throws ResourceException if error storing resource.
     */
    public void storeAmountResource(AmountResource resource, double amount) {
    	if (amount < 0D) throw new IllegalStateException("Cannot store negative amount of resource: " + amount);
    	if (amount > 0D) {
    		boolean storable = false;
    		if (hasAmountResourceCapacity(resource)) {
    			if (getAmountResourceRemainingCapacity(resource) >= amount) {
    				double remainingAmount = amount;
    			
    				// Store resource in type storage.
    				if (typeStorage != null) {
    					double remainingTypeCapacity = typeStorage.getAmountResourceTypeRemainingCapacity(resource);
    					if (remainingTypeCapacity > 0D) {
    						double typeStore = remainingAmount;
    						if (typeStore > remainingTypeCapacity) typeStore = remainingTypeCapacity;
    						typeStorage.storeAmountResourceType(resource, typeStore);
    						remainingAmount -= typeStore;
    					}
    				}
    			
    				// Store resource in phase storage.
    				if ((phaseStorage != null) && (remainingAmount > 0D)) {
    					double remainingPhaseCapacity = phaseStorage.getAmountResourcePhaseRemainingCapacity(resource.getPhase());
    					if (remainingPhaseCapacity >= remainingAmount) {
    						AmountResource resourceTypeStored = phaseStorage.getAmountResourcePhaseType(resource.getPhase());
    						if ((resourceTypeStored == null) || resource.equals(resourceTypeStored)) 
    							phaseStorage.storeAmountResourcePhase(resource, remainingAmount);
    						remainingAmount = 0D;
    					}
    				}
    			
    				if (remainingAmount == 0D) {
    					storable = true;
    					clearStoredCache();
    				}
    				else {
    					logger.severe("Amount resource " + resource + " of amount: " + amount + " to store.  Amount remaining capacity: " + 
    	    					getAmountResourceRemainingCapacity(resource) + " remaining: " + remainingAmount);
    				}
    			}
    		}
    		if (!storable) throw new IllegalStateException("Amount resource: " + resource + " of amount: " + amount + 
    			" could not be stored in inventory.");
    	}
    }
    
    /**
     * Retrieves an amount of a resource from storage.
     * @param resource the resource.
     * @param amount the amount (kg).
     * @throws ResourceException if error retrieving resource.
     */
    public void retrieveAmountResource(AmountResource resource, double amount) {
    	if (amount < 0D) throw new IllegalStateException("Cannot retrieve negative amount of resource: " + amount);
    	boolean retrievable = false;
    	double amountStored = getAmountResourceStored(resource);
    	if (amountStored >= amount) {
    		double remainingAmount = amount;
    		
    		// Retrieve resource from phase storage.
    		if (phaseStorage != null) {
    			double phaseStored = phaseStorage.getAmountResourcePhaseStored(resource.getPhase());
    			double retrieveAmount = remainingAmount;
    			if (retrieveAmount > phaseStored) retrieveAmount = phaseStored;
    			AmountResource resourceTypeStored = phaseStorage.getAmountResourcePhaseType(resource.getPhase());
    			if (resource.equals(resourceTypeStored)) phaseStorage.retrieveAmountResourcePhase(resource.getPhase(), retrieveAmount);
    			remainingAmount -= retrieveAmount;
    		}
    		
    		// Retrieve resource from type storage.
    		if ((typeStorage != null) && (remainingAmount > 0D)) {
    			double remainingTypeStored = typeStorage.getAmountResourceTypeStored(resource);
    			if (remainingTypeStored >= remainingAmount) {
    				typeStorage.retrieveAmountResourceType(resource, remainingAmount);
    				remainingAmount = 0D;
    			}
    		}
    		
    		if (Math.abs(remainingAmount) < .0000001D) {
    			retrievable = true;
    			clearStoredCache();
    		}
    		else {
    			logger.severe("Amount resource " + resource + " of amount: " + amount + " needed to retrieve.  Amount stored: " + 
    					amountStored + " remaining: " + remainingAmount);
    		}
    	}
    	else {
    		logger.severe("Amount resource " + resource + " of amount: " + amount + " needed to retrieve.  Amount stored: " +
    				amountStored);
    	}
    	if (!retrievable) throw new IllegalStateException("Amount resource: " + resource + " of amount: " + amount + 
    			" could not be retrieved from inventory.");
    }
    
    private void clearStoredCache() {
    	resourceStoredKeyCache = null;
    	if (allStoredResourcesCache != null) {
    		allStoredResourcesCache.clear();
			allStoredResourcesCache = null;
    	}
    	resourceCapacityKeyCache = null;
    	totalResourcesStored = -1D;
    	totalResourcesStoredSet = false;
    }

    /**
     * Prepare object for garbage collection.
     */
    public void destroy() {
        if (typeStorage != null) typeStorage.destroy();
        typeStorage = null;
        if (phaseStorage != null) phaseStorage.destroy();
        phaseStorage = null;
        resourceCapacityKeyCache = null;
        resourceStoredKeyCache = null;
        if (allStoredResourcesCache != null) allStoredResourcesCache.clear();
        allStoredResourcesCache = null;
    }
}