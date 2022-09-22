/*
 * Mars Simulation Project
 * VehicleMission.java
 * @date 2022-09-10
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person.ai.mission;

import org.mars_sim.msp.core.person.ai.task.LoadingController;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.Vehicle;

public interface VehicleMission extends Mission {

    /**
     * Get the Vehicle assigned to the Mission
     * @return 
     */
    Vehicle getVehicle();

    /**
	 * Gets the estimated total distance of the trip.
	 * 
	 * @return distance (km)
	 */
    double getDistanceProposed();

    void computeTotalDistanceProposed();
    
    /**
	 * Gets the actual total distance travelled during the mission so far.
	 *
	 * @return distance (km)
	 */
    double getTotalDistanceTravelled();

    /**
     * Get the total distacne remaining for the mission.
     */
    double getTotalDistanceRemaining();

    /**
	 * Gets the current loading plan for this Mission phase.
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
     * Get the remaining distacne for the current travel leg
     */
    double getDistanceCurrentLegRemaining();

    // TODO Revoe this
    int getNumberOfNavpoints();

    // TODO remove this Consolidate these methods
    NavPoint getNavpoint(int i);
	NavPoint getNextNavpoint();
    NavPoint getCurrentNavpoint();
    int getNextNavpointIndex();

    //TODO Remove thi as it exposes internal working of travelling
    String getTravelStatus();

    // TODO Remove these. OperateVehcile should just notify mission there is no Fuel; then VehcileMission detaisl with it
    void setEmergencyBeacon(Worker worker, Vehicle vehicle, boolean b, String name);
    void getHelp(MissionStatus status);

	/**
	 * Can the mission vehicle be unloaded at this Settlement ?
	 *
	 * @param settlement
	 * @return
	 */
    boolean isVehicleUnloadableHere(Settlement settlement);

    // TODO Only used in a single case ??
    void goToNearestSettlement();
}
