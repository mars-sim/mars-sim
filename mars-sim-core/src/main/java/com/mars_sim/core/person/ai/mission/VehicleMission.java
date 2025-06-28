/*
 * Mars Simulation Project
 * VehicleMission.java
 * @date 2024-08-01
 * @author Barry Evans
 */
package com.mars_sim.core.person.ai.mission;

import java.util.List;

import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.vehicle.Vehicle;

public interface VehicleMission extends Mission {

    /**
     * Gets the Vehicle assigned to the Mission.
     * 
     * @return 
     */
    Vehicle getVehicle();

    /**
	 * Gets the estimated total distance of the trip.
	 * 
	 * @return distance (km)
	 */
    double getTotalDistanceProposed();
    
    /**
	 * Gets the actual total distance traveled during the mission so far.
	 *
	 * @return distance (km)
	 */
    double getTotalDistanceTravelled();

    /**
     * Gets the total distance remaining for the mission.
     */
    double getTotalDistanceRemaining();

    /**
     * Gets the remaining distance for the current travel leg.
     */
    double getDistanceCurrentLegRemaining();

    /**
     * Gets the remaining distance for the current travel leg.
     */
    double getDistanceCurrentLegTravelled();

    /**
	 * Gets the estimated time of arrival (ETA) for the current leg of the mission.
	 *
	 * @return time (MarsClock) or null if not applicable.
	 */
	MarsTime getLegETA();

    /**
     * Is the Mission traveling to the current destination.
     * 
     * @see #getCurrentDestination()
     */
    boolean isTravelling();

    /**
	 * Gets the current destination of the Mission. The isTravelling flag
	 * identifies if the Mission is on the way.
	 * 
     * @see #isTravelling()
	 */
	NavPoint getCurrentDestination();
 
	/**
	 * Returns the current mission location. For a vehicle mission, return its vehicle's position.
	 */
	Coordinates getCurrentMissionLocation();

    /**
	 * Gets the navpoint at an index value.
	 * 
	 * @param index the index value
	 * @return navpoint
	 * @throws IllegaArgumentException if no navpoint at that index.
	 */
    List<NavPoint> getNavpoints();

    /**
     * Requests help as things have gone wrong.
     */
    void getHelp(MissionStatus status);
}
