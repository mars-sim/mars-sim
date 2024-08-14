/*
 * Mars Simulation Project
 * InsidePathLocation.java
 * @date 2023-11-24
 * @author Scott Davis
 */
package com.mars_sim.core.structure.building.connection;

import com.mars_sim.core.map.location.LocalPosition;

/**
 * A location point object on an inside building path.
 */
public interface InsidePathLocation {

    /**
     * Gets the position in the settlement locale.
     * @return Position in (meters).
     */
    public LocalPosition getPosition();
}
