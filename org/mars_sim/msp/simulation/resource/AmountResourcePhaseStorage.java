/**
 * Mars Simulation Project
 * AmountResourcePhaseStorage.java
 * @version 2.85 2008-09-13
 * @author Scott Davis 
 */

package org.mars_sim.msp.simulation.resource;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Storage for phases of amount resource.
 */
class AmountResourcePhaseStorage implements Serializable {

	// Data members
	private Map<Phase, Double> amountResourcePhaseCapacities = null; // Capacity for each phase of amount resource.
    private Map<Phase, StoredPhase> amountResourcePhaseStored = null; // Stored resources by phase.
    private transient double totalStoredCache = -1D; // The total amount phase resources stored. (kg)
    private transient boolean totalStoredCacheSet = false;
    
    /**
     * Adds capacity for a resource phase.
     * @param phase the phase
     * @param capacity the capacity amount (kg).
     * @throws ResourceException if error adding capacity.
     */
    void addAmountResourcePhaseCapacity(Phase phase, double capacity) throws ResourceException {
    	if (capacity < 0D) throw new ResourceException("Cannot add negative phase capacity: " + capacity);
    	if (amountResourcePhaseCapacities == null) amountResourcePhaseCapacities = new HashMap<Phase, Double>();
    	if (hasAmountResourcePhaseCapacity(phase)) {
    		double current = amountResourcePhaseCapacities.get(phase);
    		amountResourcePhaseCapacities.put(phase, (current + capacity));
    	}
    	else amountResourcePhaseCapacities.put(phase, capacity);
    }
    
    /**
     * Checks if storage has capacity for a phase.
     * @param phase the phase.
     * @return true if capacity in phase.
     */
    boolean hasAmountResourcePhaseCapacity(Phase phase) {
    	if (amountResourcePhaseCapacities != null) {
    		if (amountResourcePhaseCapacities.containsKey(phase)) return true; 
    	}
    	return false;
    }
    
    /**
     * Gets the capacity for a phase.
     * @param phase the phase
     * @return the capacity (kg).
     */
    double getAmountResourcePhaseCapacity(Phase phase) {
    	double result = 0D;
    	if (hasAmountResourcePhaseCapacity(phase)) 
    		result = amountResourcePhaseCapacities.get(phase);
    	return result;
    }
    
    /**
     * Gets the stored amount of a phase.
     * @param phase the phase
     * @return amount stored (kg)
     */
    double getAmountResourcePhaseStored(Phase phase) {
    	double result = 0D;
    	if (hasAmountResourcePhaseCapacity(phase) && (amountResourcePhaseStored != null)) {
    		StoredPhase stored = amountResourcePhaseStored.get(phase);
    		if (stored != null) result = stored.amount;
    	}
    	return result;
    }
    
    /**
     * Gest the total amount of phase resources stored.
     * @return amount stored (kg).
     */
    double getTotalAmountResourcePhasesStored() {
    	if (!totalStoredCacheSet) updateTotalAmountResourcePhasesStored();
    	return totalStoredCache;
    }
    
    /**
     * Updates the total amount resource phases stored cache value.
     */
    private void updateTotalAmountResourcePhasesStored() {
    	totalStoredCache = 0D;
    	if (amountResourcePhaseStored != null) {
    		Iterator<Phase> i = amountResourcePhaseStored.keySet().iterator();
    		while (i.hasNext()) totalStoredCache += getAmountResourcePhaseStored(i.next());
    	}
    	totalStoredCacheSet = true;
    }
    
    /**
     * Gets the remaining capacity for a phase .
     * @param phase the phase
     * @return remaining capacity (kg)
     */
    double getAmountResourcePhaseRemainingCapacity(Phase phase) {
    	double result = 0D;
    	if (hasAmountResourcePhaseCapacity(phase))
    		result = getAmountResourcePhaseCapacity(phase) - getAmountResourcePhaseStored(phase);
    	return result;
    }
    
    /**
     * Gets the type of resource that is stored for a phase.
     * @param phase the phase
     * @return the resource stored.
     */
    AmountResource getAmountResourcePhaseType(Phase phase) {
    	AmountResource result = null;
    	if (hasAmountResourcePhaseCapacity(phase) && amountResourcePhaseStored != null) {
    		StoredPhase stored = amountResourcePhaseStored.get(phase);
    		if (stored != null) result = stored.resource;
    	}
    	return result;
    }
    
    /**
     * Stores an amount of a resource.
     * @param resource the resource.
     * @param amount the amount to store (kg)
     * @throws ResourceException if error storing resource.
     */
    void storeAmountResourcePhase(AmountResource resource, double amount) throws ResourceException {
    	if (amount < 0D) throw new ResourceException("Cannot store negative amount of phase: " + amount);
    	if (amount > 0D) {
    		Phase resourcePhase = resource.getPhase();
    		boolean storable = false;
    		if (getAmountResourcePhaseRemainingCapacity(resourcePhase) >= amount) {
    			if ((getAmountResourcePhaseStored(resourcePhase) == 0D) || 
    						(resource.equals(getAmountResourcePhaseType(resourcePhase))))
    				storable = true;
    		}
    		if (storable) {
    			double totalAmount = getAmountResourcePhaseStored(resourcePhase) + amount;
                // Check for trace amount storage.
                if (totalAmount > .00000001D) { 
                    if (amountResourcePhaseStored == null) amountResourcePhaseStored = new HashMap<Phase, StoredPhase>();
                    amountResourcePhaseStored.put(resourcePhase, new StoredPhase(resource, totalAmount));
                    updateTotalAmountResourcePhasesStored();
                }
    		}
    		else throw new ResourceException("Amount resource could not be added in phase storage.");
    	}
    }
    
    /**
     * Retrieves an amount of a resource.
     * @param phase the phase
     * @param amount the amount to retrieve.
     * @throws ResourceException if error retrieving amount from phase.
     */
    void retrieveAmountResourcePhase(Phase phase, double amount) throws ResourceException {
    	if (amount < 0D) throw new ResourceException("Cannot retrieve negative amount of phase: " + amount); 
    	boolean retrievable = false;
    	if (getAmountResourcePhaseStored(phase) >= amount) {
    		StoredPhase stored = amountResourcePhaseStored.get(phase);
    		if (stored != null) {
    			stored.amount -= amount;
                // check for trace amount remaining.
                if (stored.amount <= .00000001D) stored.amount = 0D;
    			retrievable = true;
    			if (stored.amount <= 0D) amountResourcePhaseStored.remove(phase);
    			updateTotalAmountResourcePhasesStored();
    		}
    	}
    	if (!retrievable) throw new ResourceException("Amount resource (" + phase.getName() +  ":" + 
    			amount + ") could not be retrieved from phase storage");
    }
    
    /**
     * Internal class for a stored phase.
     */
    private class StoredPhase implements Serializable {
    	private AmountResource resource;
    	private double amount;
    	
    	private StoredPhase(AmountResource resource, double amount) {
    		this.resource = resource;
    		this.amount = amount;
    	}
    }
}