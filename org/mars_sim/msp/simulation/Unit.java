/**
 * Mars Simulation Project
 * Unit.java
 * @version 2.72 2001-05-31
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

/** The Unit class is the abstract parent class to all units on the
 *  virtual Mars.  Units include people, vehicles and settlements.
 *  This class provides data members and methods common to all units.
 */
public abstract class Unit {

    // Data members
    Coordinates location; // Unit location coordinates
    String name; // Unit name
    VirtualMars mars; // The virtual Mars
    UnitManager manager; // Primary unit manager

    /** Constructs a Unit object
     *  @param name the name of the unit
     *  @param location the unit's location
     *  @param mars the virtual Mars
     *  @param manager the unit's unit manager
     */
    Unit(String name, Coordinates location, VirtualMars mars, UnitManager manager) {
        // Initialize data members from parameters
        this.name = name;
        this.location = location;
        this.mars = mars;
        this.manager = manager;
    }

    /** Returns unit's UnitManager 
     *  @return the unit's unit manager
     */
    public UnitManager getUnitManager() {
        return manager;
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
    }

    // perhaps this should be moved into a seperate Time interface
    /** the opportunity for a unit to handle time passing 
     *  @param seconds for action
     */
    void timePasses(double seconds) {
    }
}
