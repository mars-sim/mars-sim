/**
 * Mars Simulation Project
 * Malfunctionable.java
 * @version 2.74 2002-04-29
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.malfunction;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;

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

    /**
     * Gets a collection of people affected by this entity.
     * @return person collection
     */
    public PersonCollection getAffectedPeople();

    /**
     * Gets the inventory associated with this entity.
     * @return inventory
     */
    public Inventory getInventory();
}
