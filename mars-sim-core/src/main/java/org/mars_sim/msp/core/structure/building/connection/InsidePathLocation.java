/**
 * Mars Simulation Project
 * InsidePathLocation.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.connection;

import org.mars_sim.msp.core.LocalPosition;

/**
 * A location point object on an inside building path.
 */
public interface InsidePathLocation {

    /**
     * Gets the X location in the settlement locale.
     * @return X location (meters).
     */
	@Deprecated
    default double getXLocation() {
    	return getPosition().getX();
    }
    
    /**
     * Gets the Y location in the settlement locale.
     * @return Y location (meters).
     */
	@Deprecated
    default double getYLocation() {
    	return getPosition().getY();
    }
    
    /**
     * Gets the position in the settlement locale.
     * @return Position in (meters).
     */
    public LocalPosition getPosition();
}
