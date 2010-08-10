/**
 * Mars Simulation Project
 * Airlockable.java
 * @version 3.00 2010-08-10
 * @author Scott Davis 
 */

package org.mars_sim.msp.core.vehicle;

import org.mars_sim.msp.core.Airlock;

/**
 * This interface represents a vehicle with an airlock. 
 */
public interface Airlockable {

    /**
     * Gets the vehicle's airlock.
     * @return airlock
     */
    public Airlock getAirlock();
}
