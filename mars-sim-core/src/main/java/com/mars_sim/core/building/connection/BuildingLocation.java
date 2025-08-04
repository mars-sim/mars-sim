/*
 * Mars Simulation Project
 * BuildingLocation.java
 * @date 2025-07-18
 * @author Scott Davis
 */
package com.mars_sim.core.building.connection;

import java.io.Serializable;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.map.location.LocalPosition;

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
     * Constructor.
     * 
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((building == null) ? 0 : building.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BuildingLocation other = (BuildingLocation) obj;
       if (!building.equals(other.building))
            return false;
        return pos.equals(other.pos);
    }
}
