/**
 * Mars Simulation Project
 * Inventory.java
 * @version 2.74 2002-01-30
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

    public void setTotalCapacity(double mass) {
        if (mass >= 0D) totalCapacity = mass;
    }
    
    public double getResourceMass(String resource) {
        if (containedResources.containsKey(resource)) {
            return ((Double) containedResources.get(resource)).doubleValue();
        }
	else return 0D;
    }
    
    public double removeResource(String resource, double mass) {
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

    public void setResourceCapacity(String resource, double mass) {
        resourceCapacities.put(resource, new Double(mass));
    }

    public double getResourceRemainingCapacity(String resource) {
        return getResourceCapacity(resource) - getResourceMass(resource);
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

    public UnitCollection getContainedUnits() {
        return new UnitCollection(containedUnits);
    }
    
    public boolean containsUnit(Unit unit) {
        return containedUnits.contains(unit);
    }

    public boolean containsUnit(Class unitClass) {
        UnitIterator i = containedUnits.iterator();
	while (i.hasNext()) {
            if (unitClass.isInstance(i.next())) return true;
	}
	return false;
    }

    public boolean containsUnitAll(Unit unit) {
        boolean result = false;
	
	// See if this unit contains the unit in question.
        if (containedUnits.contains(unit)) result = true;

	// Go though each contained unit and see it contains the unit in question.
	UnitIterator i = containedUnits.iterator();
	while (i.hasNext()) {
	    if (i.next().getInventory().containsUnitAll(unit)) result = true;
	}

	return result;
    }

    public boolean canAddUnit(Unit unit) {
        if (!containsUnitAll(unit)) {
	    if ((unit.getMass() + getTotalMass()) <= getTotalCapacity()) {
	        return true;
	    }
	}
        return false;
    }
    
    public boolean addUnit(Unit unit) {
        if (canAddUnit(unit)) {
	    containedUnits.add(unit);
	    unit.setContainerUnit(owningUnit);
	    unit.setCoordinates(owningUnit.getCoordinates());
	    return true;
	}
	return false;
    }
   
    public boolean takeUnit(Unit unit, Unit newOwner) {
        if (newOwner.getInventory().canAddUnit(unit)) {
	    if (containedUnits.contains(unit)) {
	        containedUnits.remove(unit);
	        newOwner.getInventory().addUnit(unit);
	        return true;
	    }
	}
	return false;
    }

    public Unit findUnit(Class unitClass) {
        if (containsUnit(unitClass)) {
	    UnitIterator i = containedUnits.iterator();
	    while (i.hasNext()) {
		Unit unit = i.next();
	        if (unitClass.isInstance(unit)) return unit;
		else {
	            if (unit.getInventory().containsUnit(unitClass)) {
	                return unit.getInventory().findUnit(unitClass);
		    }
		}
	    }
        }
	return null;
    }

    public UnitCollection getUnitsOfClass(Class unitClass) {
	UnitCollection result = new UnitCollection();
        if (containsUnit(unitClass)) {
            UnitIterator i = containedUnits.iterator();
	    while (i.hasNext()) {
	        Unit unit = i.next();
		if (unitClass.isInstance(unit)) result.add(unit);
	    }
        }
	return result;
    }
    
    public boolean dropUnit(Unit unit) {
        if (containedUnits.contains(unit)) {
	    containedUnits.remove(unit);
	   
	    unit.setContainerUnit(null);
	    if (owningUnit != null) owningUnit.getInventory().addUnit(unit); 
	    
	    return true;
	}
	else return false;
    }

    public boolean dropUnitOutside(Unit unit) {
        if (containedUnits.contains(unit)) {
	    containedUnits.remove(unit);
	    unit.setContainerUnit(null);
	    return true;
	}
	else return false;
    }

    public void setCoordinates(Coordinates newLocation) {
        UnitIterator i = containedUnits.iterator();
	while (i.hasNext()) {
	    i.next().setCoordinates(newLocation);
	}
    }
}
