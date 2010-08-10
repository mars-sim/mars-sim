/**
 * Mars Simulation Project
 * Medical.java
 * @version 3.00 2010-08-10
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