/**
 * Mars Simulation Project
 * InsidePathLocation.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.connection;

import org.mars.sim.mapdata.location.LocalPosition;

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
