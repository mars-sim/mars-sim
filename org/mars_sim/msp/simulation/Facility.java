/**
 * Mars Simulation Project
 * Facility.java
 * @version 2.72 2001-06-24
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

/** The Facility class is an abstract class that is the parent to all
 *  settlement facilities and has data members and methods common to
 *  all facilities.
 */
public abstract class Facility {

    // Data members
    String name; // Name of the facility.
    FacilityManager manager; // The Settlement's FacilityManager.

    /** Constructs a Facility object 
     *  @param manager manager of the facility
     *  @name name of the facility
     */
    public Facility(FacilityManager manager, String name) {
        // Initialize data members
        this.manager = manager;
        this.name = name;
    }

    /** Returns the name of the facility. 
     *  @return name of the facility
     */
    public String getName() {
        return name;
    }

    /** Called every clock pulse for time events in facilities.
      *  Override in children to use this. 
      *  @param time the amount of time passing (in millisols) 
      */
    void timePassing(double time) {}
}
