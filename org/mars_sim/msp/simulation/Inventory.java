/**
 * Mars Simulation Project
 * Inventory.java
 * @version 2.74 2002-01-21
 * @author Scott Davis 
 */

package org.mars_sim.msp.simulation;

import java.io.Serializable;
import java.util.*;

/** The Inventory class represents what a unit 
 *  contains in terms of resources and other units.
 *  It has methods for adding, removing and querying
 *  what the unit contains.
 */

public class Inventory implements Serializable {

    // Static data members
    public static final String WATER = "water";
    public static final String OXYGEN = "oxygen";
    public static final String FOOD = "food";
    public static final String FUEL = "fuel";
    public static final String ROCK_SAMPLES = "rock samples";
	
    // Data members
    private Unit containerUnit = null;
    private UnitCollection containedUnits = new UnitCollection();
    private HashMap containedResources = new HashMap();
    private HashMap resourceCapacities = new HashMap();
    private double totalCapacity = 0D;
    
    /** Construct an Inventory object */
    public Inventory() {
        containedResources.put(WATER, new Double(0D));
	containedResources.put(OXYGEN, new Double(0D));
	containedResources.put(FOOD, new Double(0D));
	containedResources.put(FUEL, new Double(0D));
	containedResources.put(ROCK_SAMPLES, new Double(0D));
    }

    public double getResourceAmount(String resource) {
        if (containedResources.containsKey(resource)) {
            return ((Double) containedResources.get(resource)).doubleValue();
        }
	else return 0D;
    }
    
    public double takeResource(String resource, double amount) {
        if (containedResources.containsKey(resource)) {
	    double containedAmount = ((Double) containedResources.get(resource)).doubleValue();
	    if (amount > containedAmount) {
                containedResources.put(resource, new Double(0D));
		return amount - containedAmount;
            }
	    else {
                containedResources.put(resource, new Double(containedAmount - amount));
		return amount;
            }
        }
	else return 0D;
    }

    public double addResource(String resource, double amount) {
        if (containedResources.containsKey(resource)) {
	    double remainingResourceCap = getResourceCapacity(resource) - getResourceAmount(resource); 
	    double remainingTotalCap = getTotalCapacity() - getTotalAmount();
	    
	    double amountLimit = Double.MAX_VALUE;
	    if (remainingResourceCap < amountLimit) amountLimit = remainingResourceCap;
	    if (remainingTotalCap < amountLimit) amountLimit = remainingTotalCap;
	    
	    if (amount > amountLimit) {
	        containedResources.put(resource, new Double(amountLimit));
		return amount - amountLimit;
            }
	    else {
                containedResources.put(resource, new Double(amount));
		return 0D;
            }
        }
	else return 0D;
    }

    public double getResourceCapacity(String resource) {
        if (resourceCapacities.containsKey(resource)) {
            return ((Double) resourceCapacities.get(resource)).doubleValue();
	}
	else return Double.MAX_VALUE;
    }

    public double getTotalCapacity() {
        return totalCapacity;
    }

    public double getTotalAmount() {
        double totalAmount = 0D;
	
	// Add resources in inventory.
	Iterator i = containedResources.values().iterator();
	while (i.hasNext()) {
	    totalAmount += ((Double) i.next()).doubleValue();
	}

	// Add mass of units in inventory.
	
	return totalAmount;
    }
}
