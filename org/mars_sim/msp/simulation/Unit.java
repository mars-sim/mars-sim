/**
 * Mars Simulation Project
 * Unit.java
 * @version 2.71 2000-09-17
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation; 

/** The Unit class is the abstract parent class to all units on the
 *  virtual Mars.  Units include people, vehicles and settlements.
 *  This class provides data members and methods common to all units.
 */
public abstract class Unit {

    protected Coordinates location; // Unit location coordinates
    protected String name; // Unit name
    protected VirtualMars mars; // The virtual Mars
    protected UnitManager manager; // Primary unit manager

    public Unit(String name, Coordinates location, VirtualMars mars,
            UnitManager manager) {

        // Initialize data members from parameters
        this.name = name;
        this.location = location;
        this.mars = mars;
        this.manager = manager;
    }

    /** Returns unit's UnitManager */
    public UnitManager getUnitManager() {
        return manager;
    }

    /** Returns unit's name */
    public String getName() {
        return name;
    }

    /** Returns unit's location */
    public Coordinates getCoordinates() {
        return location;
    }

    /** Sets unit's location coordinates */
    public void setCoordinates(Coordinates newLocation) {
        location.setCoords(newLocation);
    }

    // perhaps this should be moved into a seperate Time interface
    /** the opportunity for a unit to handle time passing */
    public void timePasses(int seconds) {
    }
}

