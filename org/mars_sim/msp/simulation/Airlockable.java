/**
 * Mars Simulation Project
 * Airlockable.java
 * @version 2.74 2002-05-05
 * @author Scott Davis 
 */

package org.mars_sim.msp.simulation;

/**
 * This interface represents a unit with an airlock. 
 */
public interface Airlockable {

    /**
     * Gets the unit's airlock.
     * @return airlock
     */
    public Airlock getAirlock();

    /**
     * Gets the airlockable entity's name.
     * @return name
     */
    public String getName();
}
