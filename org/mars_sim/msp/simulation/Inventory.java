/**
 * Mars Simulation Project
 * Inventory.java
 * @version 2.74 2002-01-24
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
    private Unit owningUnit;
    private UnitCollection containedUnits = new UnitCollection();
    private HashMap containedResources = new HashMap();
    private HashMap resourceCapacities = new HashMap();
    private double totalCapacity = 0D;
    
    /** Construct an Inventory object */
    public Inventory(Unit owningUnit) {
        this.owningUnit = owningUnit;

	// Initialize contained resources.
        containedResources.put(WATER, new Double(0D));
	containedResources.put(OXYGEN, new Double(0D));
	containedResources.put(FOOD, new Double(0D));
	containedResources.put(FUEL, new Double(0D));
	containedResources.put(ROCK_SAMPLES, new Double(0D));
    }

    public double getResourceMass(String resource) {
        if (containedResources.containsKey(resource)) {
            return ((Double) containedResources.get(resource)).doubleValue();
        }
	else return 0D;
    }
    
    public double takeResource(String resource, double mass) {
        if (containedResources.containsKey(resource)) {
	    double containedMass = ((Double) containedResources.get(resource)).doubleValue();
	    if (mass > containedMass) {
                containedResources.put(resource, new Double(0D));
		return mass - containedMass;
            }
	    else {
                containedResources.put(resource, new Double(containedMass - mass));
		return mass;
            }
        }
	else return 0D;
    }

    public double addResource(String resource, double mass) {
        if (containedResources.containsKey(resource)) {
	    double remainingResourceCap = getResourceCapacity(resource) - getResourceMass(resource); 
	    double remainingTotalCap = getTotalCapacity() - getTotalMass();
	    
	    double massLimit = Double.MAX_VALUE;
	    if (remainingResourceCap < massLimit) massLimit = remainingResourceCap;
	    if (remainingTotalCap < massLimit) massLimit = remainingTotalCap;
	    
	    if (mass > massLimit) {
	        containedResources.put(resource, new Double(massLimit));
		return mass - massLimit;
            }
	    else {
                containedResources.put(resource, new Double(mass));
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

    public double getTotalMass() {
        double totalMass = 0D;
	
	// Add resources in inventory.
	Iterator i = containedResources.values().iterator();
	while (i.hasNext()) {
	    totalMass += ((Double) i.next()).doubleValue();
	}

	// Add mass of units in inventory.
        UnitIterator unitIt = containedUnits.iterator();
	while (unitIt.hasNext()) {
	    totalMass += unitIt.next().getMass();
	}

	return totalMass;
    }

    public boolean containsUnit(Unit unit) {
        return containedUnits.contains(unit);
    }

    /*
    public boolean takeUnit(Unit unit) {
        if (containedUnits.contains(unit)) {
	    containedUnits.remove(unit);
	    
	    if (owningUnit != null) {
                if (!owningUnit.getInventory().addUnit(unit)) 
		    unit.setContainerUnit(null);
	    }
	    else owningUnit = null;
	    
	    return true;
	}
	else return false;
    }
    */

}
