/**
 * Mars Simulation Project
 * VirtualMars.java
 * @version 2.72 2001-04-02
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

import java.util.*;

/** VirtualMars represents Mars in the simulation. It contains all the
 *  units, a master clock, and access to the topography data.
 */
public class VirtualMars {

    // Data members
    private SurfaceFeatures surfaceFeatures; // Surface features
    private UnitManager units; // Unit controller
    private MasterClock masterClock; // Master clock for virtual world

    /** Constructs a VirtualMars object */
    public VirtualMars() {
        
        // Initialize surface features
        surfaceFeatures = new SurfaceFeatures();

        // Initialize all units
        units = new UnitManager(this);

        // Initialize and start master clock
        masterClock = new MasterClock(this);
        masterClock.start();
    }

    /** Clock pulse from master clock 
     *  @param seconds amount of time passing (in seconds)
     */
    void clockPulse(int seconds) {
        units.takeAction(seconds);
    }

    /** Returns surface features
     *  @return surfaces features
     */
    public SurfaceFeatures getSurfaceFeatures() {
        return surfaceFeatures;
    }

    /** Returns the unit manager 
     *  @return unit manager for virtual Mars
     */
    public UnitManager getUnitManager() {
        return units;
    }

    /** Returns the master clock
     *  @return master clock instance
     */
    public MasterClock getMasterClock() {
        return masterClock;
    }
}

