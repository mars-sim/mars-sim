/**
 * Mars Simulation Project
 * AmountResourceTypeStorage.java
 * @version 3.07 2014-12-06

 * @author Scott Davis 
 */

package org.mars_sim.msp.core.resource;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Storage for types of amount resource.
 */
class AmountResourceTypeStorage implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members

	/** Capacity for each type of amount resource. */
	private Map<AmountResource, ResourceAmount> amountResourceTypeCapacities = null;

	/** Stored resources by type. */
	private Map<AmountResource, ResourceAmount> amountResourceTypeStored = null;

	/** Cache value for the total amount of resources stored. */
	private transient double totalAmountCache = 0D;
	private transient boolean totalAmountCacheDirty = true;

    /**
     * Adds capacity for a resource type.
     * @param resource the resource.
     * @param capacity the extra capacity amount (kg).
     */
    void addAmountResourceTypeCapacity(AmountResource resource, double capacity)  {

        if (capacity < 0D) {
            throw new IllegalStateException("Cannot add negative type capacity: " + capacity);
        }

        if (amountResourceTypeCapacities == null) {
            amountResourceTypeCapacities = new HashMap<AmountResource, ResourceAmount>();
        }

        if (hasAmountResourceTypeCapacity(resource)) {
            ResourceAmount existingCapacity = amountResourceTypeCapacities.get(resource);
            existingCapacity.setAmount(existingCapacity.getAmount() + capacity);
        }
        else {
            amountResourceTypeCapacities.put(resource, new ResourceAmount(capacity));
        }
    }
    
    /**
     * Removes capacity for a resource type.
     * @param resource the resource.
     * @param capacity the capacity amount (kg).
     */
    void removeAmountResourceTypeCapacity(AmountResource resource, double capacity) {
        
        if (capacity < 0D) {
            throw new IllegalStateException("Cannot remove negative type capacity: " + capacity);
        }
        
        if (amountResourceTypeCapacities == null) {
            amountResourceTypeCapacities = new HashMap<AmountResource, ResourceAmount>();
        }
        
        double existingCapacity = getAmountResourceTypeCapacity(resource);
        double newCapacity = existingCapacity - capacity;
        if (newCapacity > 0D) {
            if (hasAmountResourceTypeCapacity(resource)) {
                ResourceAmount existingCapacityAmount = amountResourceTypeCapacities.get(resource);
                existingCapacityAmount.setAmount(newCapacity);
            }
            else {
                amountResourceTypeCapacities.put(resource, new ResourceAmount(newCapacity));
            }
        }
        else if (newCapacity == 0D) {
            amountResourceTypeCapacities.remove(resource);
        }
        else {
            throw new IllegalStateException("Insufficient existing resource type capacity to remove - existing: " + 
                    existingCapacity + ", removed: " + capacity);
        }
    }

    /**
     * Checks if storage has capacity for a resource type.
     * @param resource the resource.
     * @return true if storage capacity.
     */
    boolean hasAmountResourceTypeCapacity(AmountResource resource) {

        boolean result = false;

        if (amountResourceTypeCapacities != null) {
            result = amountResourceTypeCapacities.containsKey(resource);
        }

        return result;
    }

    /**
     * Gets the storage capacity for a resource type.
     * @param resource the resource.
     * @return capacity amount (kg).
     */
    double getAmountResourceTypeCapacity(AmountResource resource) {

        double result = 0D;

        if (hasAmountResourceTypeCapacity(resource)) {
            result = (amountResourceTypeCapacities.get(resource)).getAmount();
        }

        return result;
    }

    /**
     * Gets the amount of a resource type stored.
     * @param resource the resource.
     * @return stored amount (kg).
     */
    double getAmountResourceTypeStored(AmountResource resource) {

        double result = 0D;

        ResourceAmount storedAmount = getAmountResourceTypeStoredObject(resource);
        if (storedAmount != null) {
            result = storedAmount.getAmount();
        }
        
        return result;
    }

    /**
     * Gets the amount of a resource type stored.
     * @param resource the resource.
     * @return stored amount as ResourceAmount object.
     */
    private ResourceAmount getAmountResourceTypeStoredObject(AmountResource resource) {

        ResourceAmount result = null;

        if (amountResourceTypeStored != null) {
            result = amountResourceTypeStored.get(resource);
        }

        return result;
    }

    /**
     * Gets the total amount of resources stored.
     * @param allowDirty will allow dirty (possibly out of date) results.
     * @return stored amount (kg).
     */
    double getTotalAmountResourceTypesStored(boolean allowDirty) {

        if (totalAmountCacheDirty && !allowDirty) {
            // Update total amount cache.
            updateTotalAmountResourceTypesStored();
        }

        return totalAmountCache;
    }

    /**
     * Updates the total amount of resources stored.
     */
    private void updateTotalAmountResourceTypesStored() {

        double totalAmount = 0D;

        if (amountResourceTypeStored != null) {
            Map<AmountResource, ResourceAmount> tempMap = Collections.unmodifiableMap(amountResourceTypeStored);
            Iterator<AmountResource> i = tempMap.keySet().iterator();
            while (i.hasNext()) {
                totalAmount += tempMap.get(i.next()).getAmount();
            }
        }

        totalAmountCache = totalAmount;
        totalAmountCacheDirty = false;
    }

    /**
     * Gets a set of resources stored.
     * @return set of resources.
     */
    Set<AmountResource> getAllAmountResourcesStored() {

        Set<AmountResource> result = null;

        if (amountResourceTypeStored != null) {
            result = new HashSet<AmountResource>(amountResourceTypeStored.size());
            Iterator<AmountResource> i = amountResourceTypeStored.keySet().iterator();
            while (i.hasNext()) {
                AmountResource resource = i.next();
                if (getAmountResourceTypeStored(resource) > 0D) {
                    result.add(resource);
                }
            }
        }
        else {
            result = new HashSet<AmountResource>(0);
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

        if (hasAmountResourceTypeCapacity(resource)) {
            result = getAmountResourceTypeCapacity(resource) - getAmountResourceTypeStored(resource);
        }

        return result;
    }

    /**
     * Store an amount of a resource type.
     * @param resource the resource.
     * @param amount the amount (kg).
     */
    void storeAmountResourceType(AmountResource resource, double amount) {

        if (amount < 0D) {
            throw new IllegalStateException("Cannot store negative amount of type: " + amount);
        }

        if (amount > 0D) {
            if (getAmountResourceTypeRemainingCapacity(resource) >= amount) {

                // Set total amount cache to dirty since value is changing.
                totalAmountCacheDirty = true;

                if (amountResourceTypeStored == null) {
                    amountResourceTypeStored = new HashMap<AmountResource, ResourceAmount>();
                }

                ResourceAmount stored = getAmountResourceTypeStoredObject(resource);
                if (stored != null) {
                    stored.setAmount(stored.getAmount() + amount);
                }
                else {
                    amountResourceTypeStored.put(resource, new ResourceAmount(amount));
                }
            }
            else throw new IllegalStateException("Amount resource could not be added in type storage.");
        }
    }

    /**
     * Retrieves an amount of a resource type from storage.
     * @param resource the resource.
     * @param amount the amount (kg).
     */
    void retrieveAmountResourceType(AmountResource resource, double amount) {

        if (amount < 0D) {
            throw new IllegalStateException("Cannot retrieve negative amount of type: " + amount); 
        }

        if (amount > 0D) {
            if (getAmountResourceTypeStored(resource) >= amount) {

                // Set total amount cache to dirty since value is changing.
                totalAmountCacheDirty = true;

                ResourceAmount stored = getAmountResourceTypeStoredObject(resource);
                stored.setAmount(stored.getAmount() - amount);
            }
            else {
                throw new IllegalStateException("Amount resource (" + resource.getName() +  
                        ":" + amount + ") could not be retrieved from type storage");
            }
        }
    }

    /**
     * Internal class for storing type resource amounts.
     */
    private static class ResourceAmount implements Serializable {

        private double amount;

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

    public void restoreARs(AmountResource[] ars) {
    	
    	if (amountResourceTypeCapacities != null && !amountResourceTypeCapacities.isEmpty()) {
	    	for (AmountResource r : amountResourceTypeCapacities.keySet()) {
	    		for (AmountResource ar : ars) {
	    			if (r.getName().equals(ar.getName())) {
	    				ResourceAmount ra = amountResourceTypeCapacities.get(r);
	    				// Replace the old AmountResource reference with the new
	    				amountResourceTypeCapacities.put(ar, ra);
	    				System.out.println("amountResourceTypeCapacities: " + ar.getName());
	    			}
	    		}
	    	}
    	}
    	
    	if (amountResourceTypeStored != null && !amountResourceTypeStored.isEmpty()) {
	    	for (AmountResource r : amountResourceTypeStored.keySet()) {
	    		for (AmountResource ar : ars) {
	    			if (r.getName().equals(ar.getName())) {
	    				ResourceAmount ra = amountResourceTypeStored.get(r);
	    				// Replace the old AmountResource reference with the new
	    				amountResourceTypeStored.put(ar, ra);
	       				System.out.println("amountResourceTypeStored: " + ar.getName());
	    			}
	    		}
	    	}
    	}

    }
    
    /**
     * Prepare object for garbage collection.
     */
    public void destroy() {
        if (amountResourceTypeCapacities != null) amountResourceTypeCapacities.clear();
        amountResourceTypeCapacities = null;
        if (amountResourceTypeStored != null) amountResourceTypeStored.clear();
        amountResourceTypeStored = null;
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
        totalAmountCacheDirty = true;
    }
}