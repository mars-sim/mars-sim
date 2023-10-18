/**
 * Mars Simulation Project
 * BuildingLocation.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package com.mars_sim.core.structure.building.connection;

import java.io.Serializable;

import com.mars_sim.core.structure.building.Building;
import com.mars_sim.mapdata.location.LocalPosition;

/**
 * An internal building location.
 */
public class BuildingLocation implements Serializable, InsidePathLocation {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    // Data members
    private Building building;
    private LocalPosition pos;

    /**
     * Constructor
     * @param building the building.
     * @param xLoc the X location
     * @param yLoc the Y location
     */
    public BuildingLocation(Building building, LocalPosition pos) {
        this.building = building;
        this.pos = pos;
    }

    public Building getBuilding() {
        return building;
    }

    @Override
    public LocalPosition getPosition() {
    	return pos;
    }
}
