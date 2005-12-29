/**
 * Mars Simulation Project
 * AmountResourceStorage.java
 * @version 2.79 2005-12-09
 * @author Scott Davis 
 */

package org.mars_sim.msp.simulation.resource;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Storage for amount resources.
 */
public class AmountResourceStorage implements Serializable {
	
	// Domain members
	AmountResourceTypeStorage typeStorage = null;
	AmountResourcePhaseStorage phaseStorage = null;
	
    /**
     * Adds capacity for a resource type.
     * @param resource the resource.
     * @param capacity the capacity amount (kg).
     * @throws ResourceException if error setting capacity.
     */
	public void addAmountResourceTypeCapacity(AmountResource resource, double capacity) throws ResourceException {
		if (typeStorage == null) typeStorage = new AmountResourceTypeStorage();
		typeStorage.addAmountResourceTypeCapacity(resource, capacity);
	}
	
    /**
     * Adds capacity for a resource phase.
     * @param phase the phase
     * @param capacity the capacity amount (kg).
     * @throws ResourceException if error adding capacity.
     */
    public void addAmountResourcePhaseCapacity(Phase phase, double capacity) throws ResourceException {
    	if (phaseStorage == null)  phaseStorage = new AmountResourcePhaseStorage();
    	phaseStorage.addAmountResourcePhaseCapacity(phase, capacity);
    }
	
    /**
     * Checks if storage has capacity for a resource.
     * @param resource the resource.
     * @return true if storage capacity.
     */
    public boolean hasAmountResourceCapacity(AmountResource resource) {
    	boolean result = false;
    	if ((typeStorage != null) && typeStorage.hasAmountResourceTypeCapacity(resource)) result = true;
    	else if ((phaseStorage != null) && phaseStorage.hasAmountResourcePhaseCapacity(resource.getPhase())) result = true;
    	return result;
    }
    
    /**
     * Gets the storage capacity for a resource.
     * @param resource the resource.
     * @return capacity amount (kg).
     */
    public double getAmountResourceCapacity(AmountResource resource) {
    	double result = 0D;
    	if ((typeStorage != null) && typeStorage.hasAmountResourceTypeCapacity(resource)) 
    		result = typeStorage.getAmountResourceTypeCapacity(resource);
    	if ((phaseStorage != null) && phaseStorage.hasAmountResourcePhaseCapacity(resource.getPhase())) {
    		if ((phaseStorage.getAmountResourcePhaseType(resource.getPhase()) == null) || 
    				phaseStorage.getAmountResourcePhaseType(resource.getPhase()).equals(resource)) 
    			result += phaseStorage.getAmountResourcePhaseCapacity(resource.getPhase());
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
    	if ((typeStorage != null) && typeStorage.hasAmountResourceTypeCapacity(resource)) 
    		result = typeStorage.getAmountResourceTypeStored(resource);
    	if ((phaseStorage != null) && resource.equals(phaseStorage.getAmountResourcePhaseType(resource.getPhase()))) 
    		result += phaseStorage.getAmountResourcePhaseStored(resource.getPhase());
    	return result;
    }
    
    /**
     * Gets all of the amount resources stored.
     * @return set of amount resources.
     */
    public Set getAllAmountResourcesStored() {
    	Set result = new HashSet();
    	if (typeStorage != null) result.addAll(typeStorage.getAllAmountResourcesStored());
    	if (phaseStorage != null) {
    		Iterator i = Phase.getPhases().iterator();
    		while (i.hasNext()) {
    			Phase phase = (Phase) i.next();
    			if (phaseStorage.getAmountResourcePhaseStored(phase) > 0D) 
    				result.add(phaseStorage.getAmountResourcePhaseType(phase));
    		}
    	}
    	return result;
    }
    
    /**
     * Gets the total amount of resources stored.
     * @return stored amount (kg).
     */
    public double getTotalAmountResourcesStored() {
    	double result = 0D;
    	if (typeStorage != null) result += typeStorage.getTotalAmountResourceTypesStored();
    	if (phaseStorage != null) result += phaseStorage.getTotalAmountResourcePhasesStored();
    	return result;
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
    public void storeAmountResource(AmountResource resource, double amount) throws ResourceException {
    	if (amount < 0D) throw new ResourceException("Cannot store negative amount of resource: " + amount);
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
    			
    			if (remainingAmount == 0D) storable = true;
    		}
    	}
    	if (!storable) throw new ResourceException("Amount resource: " + resource + " of amount: " + amount + 
    			" could not be stored in inventory.");
    }
    
    /**
     * Retrieves an amount of a resource from storage.
     * @param resource the resource.
     * @param amount the amount (kg).
     * @throws ResourceException if error retrieving resource.
     */
    public void retrieveAmountResource(AmountResource resource, double amount) throws ResourceException {
    	if (amount < 0D) throw new ResourceException("Cannot retrieve negative amount of resource: " + amount);
    	boolean retrievable = false;
    	if (getAmountResourceStored(resource) >= amount) {
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
    		
    		if (remainingAmount == 0D) retrievable = true;
    	}
    	if (!retrievable) throw new ResourceException("Amount resource: " + resource + " of amount: " + amount + 
    			" could not be retrieved from inventory.");
    }
}