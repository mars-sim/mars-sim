/**
 * Mars Simulation Project
 * Unit.java
 * @version 2.74 2002-01-29
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

import java.io.Serializable;
/** The Unit class is the abstract parent class to all units on the
 *  virtual Mars.  Units include people, vehicles and settlements.
 *  This class provides data members and methods common to all units.
 */
public abstract class Unit implements Serializable {

    // Data members
    protected Coordinates location; // Unit location coordinates
    protected String name;          // Unit name
    protected VirtualMars mars;     // The virtual Mars
    protected double baseMass;      // The mass of the unit without inventory
    protected Inventory inventory;  // The unit's inventory
    protected Unit containerUnit;   // The unit containing this unit
    
    /** Constructs a Unit object
     *  @param name the name of the unit
     *  @param location the unit's location
     *  @param mars the virtual Mars
     */
    public Unit(String name, Coordinates location, VirtualMars mars) {
        // Initialize data members from parameters
        this.name = name;
        this.location = new Coordinates(location);
        this.mars = mars;
	
	// Default base mass is effectively infinite.  Child classes can override.
	baseMass = Double.MAX_VALUE;

	// Child units should set parameters on inventory.
	inventory = new Inventory(this); 

	// Defaults to no containing unit.
	containerUnit = null;
    }

    /** Returns unit's UnitManager 
     *  @return the unit's unit manager
     */
    public UnitManager getUnitManager() {
        return mars.getUnitManager();
    }

    /** Returns unit's name 
     *  @return the unit's name
     */
    public String getName() {
        return name;
    }

    /** Returns unit's location 
     *  @return the unit's location
     */
    public Coordinates getCoordinates() {
        return location;
    }

    /** Sets unit's location coordinates 
     *  @param newLocation the new location of the unit
     */
    public void setCoordinates(Coordinates newLocation) {
        location.setCoords(newLocation);
        getInventory().setCoordinates(newLocation);	
    }

    /** Time passing for unit.
     *  Unit should take action or be modified by time as appropriate.
     *  @param time the amount of time passing (in millisols)
     */
    public void timePassing(double time) {
    }

    /** Gets the unit's inventory
     *  @return the unit's inventory object
     */
    public Inventory getInventory() {
        return inventory;
    }

    /** Gets the unit's container unit.
     *  Returns null if unit has no container unit.
     *  @return the unit's container unit
     */
    public Unit getContainerUnit() {
        return containerUnit;
    }

    /** Gets the topmost container unit that owns this unit.
     *  Returns null if unit has no container unit.
     *  @return the unit's topmost container unit
     */
    public Unit getTopContainerUnit() {
    
        Unit topUnit = getContainerUnit();
	while (topUnit != null) {
	    topUnit = topUnit.getContainerUnit();
	}

	return topUnit;
    }

    /** Sets the unit's container unit.
     *  @param containerUnit the unit to contain this unit.
     */
    public void setContainerUnit(Unit containerUnit) {
        this.containerUnit = containerUnit;
    }

    /** Gets the unit's mass including inventory mass.
     *  @return mass of unit and inventory
     */
    public double getMass() {
        return baseMass + inventory.getTotalMass();
    }
}
