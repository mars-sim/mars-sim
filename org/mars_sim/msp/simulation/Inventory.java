/**
 * Mars Simulation Project
 * Inventory.java
 * @version 2.74 2002-02-24
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
    // The resource names.
    public static final String WATER = "water";
    public static final String OXYGEN = "oxygen";
    public static final String FOOD = "food";
    public static final String FUEL = "fuel";
    public static final String ROCK_SAMPLES = "rock samples";
	
    // Data members
    private Unit owner;  // The unit that owns this inventory. 
    private UnitCollection containedUnits = new UnitCollection(); // Collection of units in inventory.
    private HashMap containedResources = new HashMap();  // Resources in inventory.
    private HashMap resourceCapacities = new HashMap();  // Maximum capacity for each resource.
    private double totalCapacity = 0D;  // Total mass capacity of inventory.
    
    /** 
     * Construct an Inventory object 
     * @param owner the unit that owns this inventory
     */
    public Inventory(Unit owner) {
	
	// Set owning unit.
        this.owner = owner;

	// Initialize contained resources to zero.
        containedResources.put(WATER, new Double(0D));
	containedResources.put(OXYGEN, new Double(0D));
	containedResources.put(FOOD, new Double(0D));
	containedResources.put(FUEL, new Double(0D));
	containedResources.put(ROCK_SAMPLES, new Double(0D));
    }

    /**
     * Sets the total mass capacity of the inventory.
     * @param mass the total mass capacity in kg.
     */
    public void setTotalCapacity(double mass) {
        if (mass >= 0D) totalCapacity = mass;
    }
    
    /**
     * Gets the mass of a given resource in the inventory.
     * @param resource the name of the resource
     * @return the mass of the resource in kg.
     */
    public double getResourceMass(String resource) {
        if (containedResources.containsKey(resource)) {
            return ((Double) containedResources.get(resource)).doubleValue();
        }
	else return 0D;
    }
    
    /**
     * Removes an amount of a given resource from the inventory.
     * @param resource the name of the resource
     * @param mass the mass to be removed in kg.
     * @return the mass actually removed in kg.
     */
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

    /**
     * Adds an amount of a given resource to the inventory.
     * @param resource the name of the resource
     * @param mass the mass to be added in kg.
     * @param the mass actually added in kg.
     */
    public double addResource(String resource, double mass) {
        if (containedResources.containsKey(resource)) {
	    double remainingResourceCap = getResourceRemainingCapacity(resource);
	    double remainingTotalCap = getTotalCapacity() - getTotalMass();
	    
	    double massLimit = Double.MAX_VALUE;
	    if (remainingResourceCap < massLimit) massLimit = remainingResourceCap;
	    if (remainingTotalCap < massLimit) massLimit = remainingTotalCap;
	   
	    double finalResourceMass = getResourceMass(resource);
	    if (mass < massLimit) {
		finalResourceMass += mass;
	        containedResources.put(resource, new Double(finalResourceMass));
		return mass;
            }
	    else {
		finalResourceMass += massLimit;
                containedResources.put(resource, new Double(finalResourceMass));
		return massLimit;
            }
        }
	else return 0D;
    }

    /**
     * Gets the mass capacity of a given resource for the inventory.
     * @param resource the name of the resource
     * @return the mass capacity for the resource in kg.
     */
    public double getResourceCapacity(String resource) {
        if (resourceCapacities.containsKey(resource)) {
            return ((Double) resourceCapacities.get(resource)).doubleValue();
	}
	else return Double.MAX_VALUE;
    }

    /**
     * Sets the mass capacity of a given resource for the inventory.
     * @param resource the name of the resource
     * @param mass the mass capacity of the resource in kg.
     */
    public void setResourceCapacity(String resource, double mass) {
        resourceCapacities.put(resource, new Double(mass));
    }

    /**
     * Gets the remaining mass for a given resource the inventory can hold.
     * @param resource the name of the resource
     * @return the remaining mass capacity in kg.
     */
    public double getResourceRemainingCapacity(String resource) {
        return getResourceCapacity(resource) - getResourceMass(resource);
    }

    /** 
     * Gets the total mass capacity for the inventory.
     * @return the total mass capacity in kg.
     */
    public double getTotalCapacity() {
        return totalCapacity;
    }

    /** 
     * Gets the total mass currently in the inventory.
     * @return total mass in kg.
     */
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

    /** 
     * Gets a collection of all the units in the inventory.
     * @return UnitCollection of all units
     */
    public UnitCollection getContainedUnits() {
        return new UnitCollection(containedUnits);
    }
    
    /**
     * Returns true if inventory contains a given unit.
     * @param unit the given unit
     * @return if unit is in inventory
     */
    public boolean containsUnit(Unit unit) {
        return containedUnits.contains(unit);
    }

    /**
     * Returns true if inventory contains any of a given class of unit.
     * @param unitClass the given unit class
     * @return if class of unit is in inventory
     */
    public boolean containsUnit(Class unitClass) {
        UnitIterator i = containedUnits.iterator();
	while (i.hasNext()) {
            if (unitClass.isInstance(i.next())) return true;
	}
	return false;
    }

    /** 
     * Returns true if given unit is in inventory or any contained unit's inventory.
     * @param unit the given unit
     * @return if unit is in all of inventory
     */
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

    /**
     * Returns true if given unit can be added to the inventory.
     * @param unit the given unit
     * @return if unit can be added to inventory
     */
    public boolean canAddUnit(Unit unit) {
        if (!containsUnitAll(unit)) {
	    if ((unit.getMass() + getTotalMass()) <= getTotalCapacity()) {
	        return true;
	    }
	}
        return false;
    }
    
    /** 
     * Adds a unit to the inventory.
     * @param unit the given unit
     * @return true if unit is added successfully
     */
    public boolean addUnit(Unit unit) {
        if (canAddUnit(unit)) {
	    containedUnits.add(unit);
	    unit.setContainerUnit(owner);
	    unit.setCoordinates(owner.getCoordinates());
	    return true;
	}
	return false;
    }
  
    /**
     * Takes a unit from inventory and adds it to another unit's inventory.
     * @param unit the given unit to be taken
     * @param newOwner the taking unit
     * @return true if operation is successful
     */
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

    /**
     * Finds a unit of a given class in the inventory.
     * @param unitClass the given unit class
     * @return the unit instance of the given unit class if found.
     * Returns null otherwise.
     */
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

    /**
     * Returns a collection of units of a given class in the inventory.
     * @param unitClass the given unit class
     * @return collection of units of the given class from the inventory.
     * Returns empty collection if no units found.
     */
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
    
    /**
     * Drops the unit in the parent container or outside if none.
     * @param unit the given unit
     * @return true if operation is successful
     */
    public boolean dropUnit(Unit unit) {
        if (containedUnits.contains(unit)) {
	    containedUnits.remove(unit);
	   
	    unit.setContainerUnit(null);
	    Unit containerUnit = owner.getContainerUnit();
	    if (containerUnit != null) containerUnit.getInventory().addUnit(unit); 
	    
	    return true;
	}
	else return false;
    }

    /** 
     * Drops the unit outside.
     * @param unit the given unit
     * @return true if operation is successful
     */
    public boolean dropUnitOutside(Unit unit) {
        if (containedUnits.contains(unit)) {
	    containedUnits.remove(unit);
	    unit.setContainerUnit(null);
	    return true;
	}
	else return false;
    }

    /**
     * Sets the coordinates of all units in the inventory.
     * @param newLocation the new coordinate location
     */
    public void setCoordinates(Coordinates newLocation) {
        UnitIterator i = containedUnits.iterator();
	while (i.hasNext()) {
	    i.next().setCoordinates(newLocation);
	}
    }
}
