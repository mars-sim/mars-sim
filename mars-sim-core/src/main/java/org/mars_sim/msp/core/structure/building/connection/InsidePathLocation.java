/**
 * Mars Simulation Project
 * InsidePathLocation.java
 * @version 3.1.0 2019-09-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.connection;

/**
 * A location point object on an inside building path.
 */
public interface InsidePathLocation {

    /**
     * Gets the X location in the settlement locale.
     * @return X location (meters).
     */
    public double getXLocation();
    
    /**
     * Gets the Y location in the settlement locale.
     * @return Y location (meters).
     */
    public double getYLocation();
}
