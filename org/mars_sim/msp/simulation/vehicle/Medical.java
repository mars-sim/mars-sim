/**
 * Mars Simulation Project
 * Medical.java
 * @version 2.76 2004-05-12
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
     * @return Sickbay
     */
    public SickBay getSickBay();
}