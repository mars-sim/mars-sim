/**
 * Mars Simulation Project
 * Medical.java
 * @version 2.75 2003-11-27
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.vehicle;

/**
 * The Medical interface is for vehicles that have a sick bay.
 */
public interface Medical {

    /**
     * Gets the vehicle's sick bay.
     * 
     * @return SickBay
     */
    public SickBay getSickBay();
}
