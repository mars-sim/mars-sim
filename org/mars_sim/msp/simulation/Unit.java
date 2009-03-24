/**
 * Mars Simulation Project
 * Unit.java
 * @version 2.86 2009-03-23
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/** 
 * The Unit class is the abstract parent class to all units in the
 * Simulation.  Units include people, vehicles and settlements.
 * This class provides data members and methods common to all units.
 */
public abstract class Unit implements Serializable, Comparable<Unit> {

	// Unit event types
	public static final String NAME_EVENT = "name";
	public static final String DESCRIPTION_EVENT = "description";
	public static final String MASS_EVENT = "mass";
	public static final String LOCATION_EVENT = "location";
	public static final String CONTAINER_UNIT_EVENT = "container unit";
	
    // Data members
    private Coordinates location;     // Unit location coordinates
    private String name;              // Unit name
    private String description;       // Unit description
    private double baseMass;          // The mass of the unit without inventory
    private Inventory inventory;      // The unit's inventory
    private Unit containerUnit;       // The unit containing this unit
    private transient List<UnitListener> listeners; // Unit listeners.
    
    /** 
     * Constructor
     * @param name the name of the unit
     * @param location the unit's location
     */
    public Unit(String name, Coordinates location) {
    	
        // Initialize data members from parameters
        setName(name);
        setDescription(name);
        setBaseMass(Double.MAX_VALUE);
        setInventory(new Inventory(this));
        setCoordinates(new Coordinates(location));
        setContainerUnit(null);
	    
	    // Initialize unit listeners.
	    listeners = Collections.synchronizedList(new ArrayList<UnitListener>());
    }

    /** 
     * Gets the unit's UnitManager 
     * @return the unit's unit manager
     */
    public UnitManager getUnitManager() {
        return Simulation.instance().getUnitManager();
    }

    /** 
     * Gets the unit's name 
     * @return the unit's name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the unit's name
     * @param name new name
     */
    public final void setName(String name) {
    	this.name = name;
    	fireUnitUpdate(NAME_EVENT, name);
    }
    
    /**
     * Gets the unit's description
     * @return description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Sets the unit's description.
     * @param description new description.
     */
    protected final void setDescription(String description) {
    	this.description = description;
    	fireUnitUpdate(DESCRIPTION_EVENT, description);
    }

    /** 
     * Gets the unit's location 
     * @return the unit's location
     */
    public Coordinates getCoordinates() {
        return location;
    }
    
    /** 
     * Sets unit's location coordinates 
     * @param newLocation the new location of the unit
     */
    public void setCoordinates(Coordinates newLocation) {
    	if (location == null) location = new Coordinates(0D, 0D);
        location.setCoords(newLocation);
        getInventory().setCoordinates(newLocation);
        fireUnitUpdate(LOCATION_EVENT, newLocation);
    }

    /** 
     * Time passing for unit.
     * Unit should take action or be modified by time as appropriate.
     * @param time the amount of time passing (in millisols)
     * @throws Exception if error during time passing.
     */
    public void timePassing(double time) throws Exception {
    }

    /** 
     * Gets the unit's inventory
     * @return the unit's inventory object
     */
    public Inventory getInventory() {
        return inventory;
    }
    
    /**
     * Sets the unit's inventory.
     * @param inventory new inventory
     */
    private void setInventory(Inventory inventory) {
    	this.inventory = inventory;
    }

    /** 
     * Gets the unit's container unit.
     * Returns null if unit has no container unit.
     * @return the unit's container unit
     */
    public Unit getContainerUnit() {
        return containerUnit;
    }

    /** 
     * Gets the topmost container unit that owns this unit.
     * Returns null if unit has no container unit.
     * @return the unit's topmost container unit
     */
    public Unit getTopContainerUnit() {
    
        Unit topUnit = getContainerUnit();
	    if (topUnit != null) {
	        while (topUnit.getContainerUnit() != null) {
	            topUnit = topUnit.getContainerUnit();
	        }
	    }

	    return topUnit;
    }

    /** 
     * Sets the unit's container unit.
     * @param containerUnit the unit to contain this unit.
     */
    public void setContainerUnit(Unit containerUnit) {
        this.containerUnit = containerUnit;
        fireUnitUpdate(CONTAINER_UNIT_EVENT, containerUnit);
    }

    /** 
     * Gets the unit's mass including inventory mass.
     * @return mass of unit and inventory
     * @throws Exception if error getting the mass.
     */
    public double getMass() throws Exception {
        return baseMass + inventory.getTotalInventoryMass();
    }
    
    /**
     * Sets the unit's base mass.
     * @param baseMass mass (kg)
     */
    protected final void setBaseMass(double baseMass) {
    	this.baseMass = baseMass;
    	fireUnitUpdate(MASS_EVENT);
    }
    
    /**
     * Gets the base mass of the unit.
     * @return base mass (kg).
     */
    public double getBaseMass() {
    	return baseMass;
    }

    /**
     * String representation of this Unit.
     * @return The units name.
     */
    public String toString() {
        return name;
    }
    
    /**
     * Adds a unit listener
     * @param newListener the listener to add.
     */
    public final void addUnitListener(UnitListener newListener) {
    	if (listeners == null) listeners = Collections.synchronizedList(new ArrayList<UnitListener>());
        if (!listeners.contains(newListener)) listeners.add(newListener);
    }
    
    /**
     * Removes a unit listener
     * @param oldListener the listener to remove.
     */
    public final void removeUnitListener(UnitListener oldListener) {
    	if (listeners == null) listeners = Collections.synchronizedList(new ArrayList<UnitListener>());
    	if (listeners.contains(oldListener)) listeners.remove(oldListener);
    }
    
    /**
     * Fire a unit update event.
     * @param updateType the update type.
     */
    public final void fireUnitUpdate(String updateType) {
    	fireUnitUpdate(updateType, null);
    }
    
    /**
     * Fire a unit update event.
     * @param updateType the update type.
     * @param target the event target object or null if none.
     */
    public final void fireUnitUpdate(String updateType, Object target) {
    	if (listeners == null) listeners = Collections.synchronizedList(new ArrayList<UnitListener>());
    	synchronized(listeners) {
    		Iterator<UnitListener> i = listeners.iterator();
    		while (i.hasNext()) i.next().unitUpdate(new UnitEvent(this, updateType, target));
    	}
    }
    
    /**
     * Compares this object with the specified object for order.
     * @param o the Object to be compared.
     * @return a negative integer, zero, or a positive integer as this object is less than, 
     * equal to, or greater than the specified object.
     */
    public int compareTo(Unit o) {
        return name.compareToIgnoreCase(o.getName());
    }
}