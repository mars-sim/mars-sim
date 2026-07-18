/*
 * Mars Simulation Project
 * MissionVehicleProject.java
 * @date 2023-06-03
 * @author Barry Evans
 */
package com.mars_sim.core.mission;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.mission.MetaMission.Roster;
import com.mars_sim.core.mission.steps.MissionBoardVehicleStep;
import com.mars_sim.core.mission.steps.MissionDisembarkStep;
import com.mars_sim.core.mission.steps.MissionLoadVehicleStep;
import com.mars_sim.core.mission.steps.MissionTravelStep;
import com.mars_sim.core.person.ai.mission.MissionStatus;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.mission.NavPoint;
import com.mars_sim.core.person.ai.mission.VehicleMission;
import com.mars_sim.core.project.ProjectStep;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.vehicle.Vehicle;

/**
 * This represents a MissionProject that specialises in Vehicle based Mission.
 */
public class MissionVehicleProject extends MissionProject
    implements VehicleMission {
    
	private static final SimLogger logger = SimLogger.getLogger(MissionVehicleProject.class.getName());
	private static final long serialVersionUID = 1L;

    private Vehicle vehicle;
    private double proposedDistance;
    private List<NavPoint> route;
    private double distanceCompleted = 0;

    public MissionVehicleProject(String name, MissionType type, int priority, Roster crew) {
        super(name, type, priority, crew.leader(), crew.members());

        setVehicle(crew.vehicle());
    }

    /**
     * If the completed step is a Travel step then update the distance.
     * 
     * @param ms The Mission step just completed
     */
    @Override
    protected void stepCompleted(MissionStep ms) {
        if (ms instanceof MissionTravelStep mts) {
            distanceCompleted += mts.getDestination().getPointToPointDistance();
        }
    }

    /**
     * Sets the Vehicle being used.
     * 
     * @param best
     */
    private void setVehicle(Vehicle best) {
        vehicle = best;
        vehicle.setMission(this);
        vehicle.setReservedForMission(true);
    }

    private void releaseVehicle(Vehicle v) {
        if (this.equals(v.getMission())) {
            // If still assigned then release
            v.setMission(null);
            v.setReservedForMission(false);
        }
    }

    /**
     * Clears down the mission as it has been completed.
     */
    @Override
    protected void clearDown() {
        releaseVehicle(vehicle);
        super.clearDown();
    }

    @Override
    public Vehicle getVehicle() {
        return vehicle;
    }

    /**
     * Returns the location based on the vehicle if the mission is active.
     * 
     * @return Coordinates of home
     */
    @Override
    public Coordinates getCurrentMissionLocation() {
        if (!isDone()) {
            return getVehicle().getCoordinates();
        }

        return getAssociatedSettlement().getCoordinates();
    }

    @Override
    public double getTotalDistanceProposed() {
        return proposedDistance;
    }

    @Override
    public double getTotalDistanceTravelled() {
        double result = distanceCompleted;
        if (getCurrentStep() instanceof MissionTravelStep mts) {
            result += mts.getDistanceCovered();
        }   
        return result;
    }

    @Override
    public double getTotalDistanceRemaining() {
        // Not perfectly accurate but quick
        return getTotalDistanceProposed() - getTotalDistanceTravelled();
    }

    @Override
    public double getDistanceCurrentLegRemaining() {
        if (getCurrentStep() instanceof MissionTravelStep mts) {
            return mts.getDestination().getPointToPointDistance() - mts.getDistanceCovered();
        }   
        return 0D;
    }

    @Override
    public MarsTime getLegETA() {
        return null;
    }

    @Override
    public boolean isTravelling() {
        return (getCurrentStep() instanceof MissionTravelStep);
    }

    @Override
    public NavPoint getCurrentDestination() {
        ProjectStep step = getCurrentStep();
        if (step instanceof MissionTravelStep mts) {
            return mts.getDestination();
        }

        return null;
    }

    @Override
    public List<NavPoint> getNavpoints() {
        return route;
    }

    @Override
    public void getHelp(MissionStatus status) {

		// Set emergency beacon if vehicle is not at settlement.
		// Note: need to find out if there are other matching reasons for setting
		// emergency beacon.
		if (vehicle.getSettlement() == null && !vehicle.isBeaconOn()) {
            addStatus(status);
            
            var message = new StringBuilder();

            // Question: could the emergency beacon itself be broken ?
            message.append("Turned on emergency beacon to request for towing. Status flag(s): ");
            message.append(getMissionStatus().stream().map(MissionStatus::getName).collect(Collectors.joining(", ")));
            
            vehicle.setEmergencyBeacon(true);
            logger.warning(this, message.toString());
		}
    }


    /**
     * Extracts the Navpoints and calculates the proposed. This will extract the NavPoints for the Route.
     * 
     * It will also select a Vehicle based on the steps.
     * @param plan Steps for the mission
     */
    @Override
    public void setSteps(List<MissionStep> plan) {
        route = plan.stream()
                        .filter(MissionTravelStep.class::isInstance)
                        .map (sc -> ((MissionTravelStep) sc).getDestination())
                        .toList();
        proposedDistance = route.stream()
                            .mapToDouble(NavPoint::getPointToPointDistance)
                            .sum();

        // Add in the basic steps needs in a Vehicle Mission
        List<MissionStep> fullPlan = new ArrayList<>();
        fullPlan.add(new MissionLoadVehicleStep(this));
        fullPlan.add(new MissionBoardVehicleStep(this));
        fullPlan.addAll(plan);
        fullPlan.add(new MissionDisembarkStep(this));

        super.setSteps(fullPlan);
    }

    /**
     * Gets the estimated travel time take for this Mission to cover a distance.
     * 
     * @param distance Distance to cover
     * @return Duration in mSols
     */
    public double getEstimateTravelTime(double distance) {
        double result = 0D;
        double averageSpeed = vehicle.getBaseSpeed() * 0.8D;
		if (averageSpeed > 0) {
			result = distance / averageSpeed * MarsTime.MILLISOLS_PER_HOUR;
		}
        return result;
    }

	@Override
	public double getDistanceCurrentLegTravelled() {
		// Auto-generated method stub
		return 0;
	}
}
