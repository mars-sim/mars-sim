/**
 * Mars Simulation Project
 * Unit.java
 * @version 2.73 2001-11-14
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

    /** Constructs a Unit object
     *  @param name the name of the unit
     *  @param location the unit's location
     *  @param mars the virtual Mars
     */
    Unit(String name, Coordinates location, VirtualMars mars) {
        // Initialize data members from parameters
        this.name = name;
        this.location = new Coordinates(location);
        this.mars = mars;
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
    }

    /** Time passing for unit.
     *  Unit should take action or be modified by time as appropriate.
     *  @param time the amount of time passing (in millisols)
     */
    void timePassing(double time) {
    }
}
