/*
 * Mars Simulation Project
 * VehicleMission.java
 * @date 2022-09-10
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person.ai.mission;

import org.mars_sim.msp.core.person.ai.task.LoadingController;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.Vehicle;

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
    double getDistanceProposed();
    
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
	 * Gets the current loading plan for this Mission phase.
	 * 
	 * @return
	 */
    LoadingController getLoadingPlan();

    /**
	 * Gets the estimated time of arrival (ETA) for the current leg of the mission.
	 *
	 * @return time (MarsClock) or null if not applicable.
	 */
	MarsClock getLegETA();

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
	 * Gets the number of navpoints on the trip.
	 * 
	 * @return number of navpoints
	 */
    int getNumberOfNavpoints();

    /**
	 * Gets the navpoint at an index value.
	 * 
	 * @param index the index value
	 * @return navpoint
	 * @throws IllegaArgumentException if no navpoint at that index.
	 */
    NavPoint getNavpoint(int i);

    /**
     * Gets the mission's next navpoint index.
     * 
     * @return navpoint index or -1 if none.
     */
    int getNextNavpointIndex();

    /**
     * Something has gone wrong so request help
     */
    void getHelp(MissionStatus status);

	/**
	 * Can the mission vehicle be unloaded at this Settlement ?
	 *
	 * @param settlement
	 * @return
	 */
    boolean isVehicleUnloadableHere(Settlement settlement);
}
