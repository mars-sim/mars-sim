/**
 * Mars Simulation Project
 * Facility.java
 * @version 2.71 2000-09-25
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

/** The Facility class is an abstract class that is the parent to all
 *  settlement facilities and has data members and methods common to
 *  all facilities.
 */
public abstract class Facility {

    String name; // Name of the facility.
    FacilityManager manager; // The Settlement's FacilityManager.

    public Facility(FacilityManager manager, String name) {
        // Initialize data members
        this.manager = manager;
        this.name = name;
    }

    /** Returns the name of the facility. */
    public String getName() {
        return name;
    }

    /** Called every clock pulse for time events in facilities.
      *  Override in children to use this. */
    void timePasses(int seconds) {}
}
