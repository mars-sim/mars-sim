/**
 * Mars Simulation Project
 * Crewable.java
 * @version 2.74 2002-02-28
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.vehicle;

import org.mars_sim.msp.simulation.person.*;

/**
 * The Crewable interface represents a vehicle that is capable
 * of having a crew of people.
 */
public interface Crewable {

    /**
     * Gets the number of crewmembers the vehicle can carry.
     * @return capacity
     */
    public int getCrewCapacity();

    /**
     * Gets the current number of crewmembers.
     * @return number of crewmembers
     */
    public int getCrewNum();

    /**
     * Gets a collection of the crewmembers.
     * @return crewmembers as PersonCollection
     */
    public PersonCollection getCrew();

    /**
     * Checks if person is a crewmember.
     * @param person the person to check
     * @return true if person is a crewmember
     */
    public boolean isCrewmember(Person person);
}
