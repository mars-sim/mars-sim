/**
 * Mars Simulation Project
 * Malfunctionable.java
 * @version 2.74 2002-04-21
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.malfunction;

/**
 * The Malfunctionable interface represents a Unit that can have malfunctions.
 */
public interface Malfunctionable {

    /**
     * Gets the entity's malfunction manager.
     * @return malfunction manager
     */
    public MalfunctionManager getMalfunctionManager();

    /**
     * Gets the name of the malfunctionable entity.
     * @return name the entity name
     */
    public String getName();
}
