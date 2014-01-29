/**
 * Mars Simulation Project
 * Medical.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */

package org.mars_sim.msp.core.vehicle;

/**
 * The Medical interface is for vehicles that have a sick bay.
 */
public interface Medical {

    /**
     * Gets the vehicle's sick bay.
     * 
     * @return Sickbay
     */
    public SickBay getSickBay();
}