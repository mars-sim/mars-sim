/**
 * Mars Simulation Project
 * VirtualMars.java
 * @version 2.71 2000-09-26
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

import java.util.*;

/** VirtualMars represents Mars in the simulation. It contains all the
 *  units, a master clock, and access to the topography data.
 */
public class VirtualMars {

    private TerrainElevation elevationMap; // Terrain elevation of Mars
    private UnitManager units; // Unit controller
    private MasterClock masterClock; // Master clock for virtual world

    public VirtualMars() {
        // Initialize terrain
        elevationMap =
                new TerrainElevation("map_data/TopoMarsMap.dat", "map_data/TopoMarsMap.index",
                "map_data/TopoMarsMap.sum");

        // Initialize all units
        units = new UnitManager(this);

        // Initialize and start master clock
        masterClock = new MasterClock(this);
        masterClock.start();
    }

    /** Clock pulse from master clock */
    void clockPulse(int seconds) {
        units.takeAction(seconds);
    }

    /** Returns terrain elevation object */
    public TerrainElevation getElevationMap() {
        return elevationMap;
    }

    /** Returns the unit manager */
    public UnitManager getUnitManager() {
        return units;
    }
}

