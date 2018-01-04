/**
 * Mars Simulation Project
 * MissionMemberable.java
 * @version 3.1.0 2017-05-05
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.mission;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.UnitListener;
import org.mars_sim.msp.core.location.LocationTag;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.ShiftType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * An interface representing a person or robot that can be a member of a mission.
 */
public interface MissionMember {

    /**
     * Gets the mission member's name.
     * @return name string.
     */
    public String getName();

    /**
     * Gets the mission member's current task description.
     * @return task description string.
     */
    public String getTaskDescription();

    /**
     * Sets the associated settlement for the member.
     * @param newSettlement the new associated settlement or null if none.
     */
    public void setAssociatedSettlement(Settlement settlement);

    /**
     * Gets the member's location.
     * @return the member's coordinate location on Mars.
     */
    public Coordinates getCoordinates();

    /**
     * Adds a unit listener
     * @param newListener the listener to add.
     */
    public void addUnitListener(UnitListener newListener);

    /**
     * Removes a unit listener
     * @param oldListener the listener to remove.
     */
    public void removeUnitListener(UnitListener oldListener);

    /**
     * Sets the person's current mission.
     * @param newMission the new mission
     */
    public void setMission(Mission newMission);

    /**
     * Sets a person's work shift .
     * @param newMission the new mission
     */
    public void setShiftType(ShiftType shiftType);

    /**
     * @return {@link LocationSituation} the mission member's location
     */
    public LocationSituation getLocationSituation();

    /**
     * Get settlement the member is at, null if member is not at a settlement
     * @return the member's settlement
     */
    public Settlement getSettlement();

    /**
     * Get vehicle member is in, null if member is not in vehicle
     * @return the member's vehicle
     */
    public Vehicle getVehicle();

    /**
     * Sets the person's vehicle.
     * @param vehicle
     */
    public void setVehicle(Vehicle vehicle);

	public LocationTag getLocationTag();
}