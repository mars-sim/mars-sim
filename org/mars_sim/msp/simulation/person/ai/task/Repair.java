/**
 * Mars Simulation Project
 * Repair.java
 * @version 2.74 2002-05-05
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.task;

import org.mars_sim.msp.simulation.malfunction.Malfunctionable;

/**
 * The Repair interface is a task for repairing malfunction. 
 */
public interface Repair {

    /**
     * Gets the malfunctionable entity the person is currently repairing.
     * @returns null if none.
     * @return entity
     */
    public Malfunctionable getEntity(); 
}
