/**
 * Mars Simulation Project
 * DeathInfo.java
 * @version 2.74 2002-05-10
 * @author Barry Evans
 */

package org.mars_sim.msp.simulation.person.medical;

import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.simulation.MarsClock;
import org.mars_sim.msp.simulation.Unit;
/**
 * This class represents the status of a Person when death occurs. It records
 * the Complaint that caused the death to occur, the time of death and
 * the Location.
 * The Location is recorded as a dead body may be moved from the place of death.
 * This class is immutable since once Death occurs it is final.
 */
public class DeathInfo implements java.io.Serializable {

    private MarsClock   timeOfDeath;
    private Complaint   illness;
    private Unit        placeOfDeath;
    private Coordinates positionOfDeath;

    /**
     * The construct creates an instance of a DeathInfo class.
     *
     * @param complaint The Complaint that produced the death.
     * @param time Time the death.
     * @param location Place of death.
     * @param coordinate The actual coordinate where death happened
     */
    public DeathInfo(Complaint complaint, MarsClock time,
                     Unit location, Coordinates coordinate) {
        timeOfDeath = time;
        illness = complaint;
        placeOfDeath = location;
        positionOfDeath = coordinate;
    }

    /**
     * Get the time death happened.
     * @return Mars time.
     */
    public MarsClock getTime() {
        return timeOfDeath;
    }

    /**
     * Get the Unit where the death happened. This result could be null if
     * the Person was on an EVA.
     * @return Unit where Person was.
     */
    public Unit getLocation() {
        return placeOfDeath;
    }

    /**
     * Get the Illness that caused the death.
     * @return Complaint resulting in the death.
     */
    public Complaint getIllness() {
        return illness;
    }

    /**
     * Get the physical position of the Person when Death occurs. This could
     * be different from the Place of Death as the Unit may be a Vehicle.
     * @return Coordinates of place.
     */
    public Coordinates getPosition() {
        return positionOfDeath;
    }
}