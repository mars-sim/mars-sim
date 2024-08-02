/*
 * Mars Simulation Project
 * MissionVehicleProject.java
 * @date 2023-06-03
 * @author Barry Evans
 */
package com.mars_sim.core.mission;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.mission.MissionStatus;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.mission.NavPoint;
import com.mars_sim.core.person.ai.mission.VehicleMission;
import com.mars_sim.core.project.ProjectStep;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.core.vehicle.task.LoadingController;
import com.mars_sim.mapdata.location.Coordinates;
import com.mars_sim.tools.util.RandomUtil;

/**
 * This represents a MissionProject that specialises in Vehicle based Mission.
 */
public class MissionVehicleProject extends MissionProject
    implements VehicleMission {
    
	private static final long serialVersionUID = 1L;

	private static final MissionStatus NO_AVAILABLE_VEHICLES = new MissionStatus("Mission.status.noVehicle");

    private Vehicle vehicle;
    private double proposedDistance;
    private List<NavPoint> route;
    private double distanceCompleted = 0;

    public MissionVehicleProject(String name, MissionType type, int priority, int maxMembers, Person leader) {
        super(name, type, priority, 2, maxMembers, leader);
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
    
    /**
	 * Finds the best suitable vehicle for the mission if possible.
	 *
	 * @return The selected bets vehicle; null if none found
	 */
	private final Vehicle findBestVehicle(Settlement base) {
		Collection<Vehicle> vList = base.getParkedGaragedVehicles();
		List<Vehicle> bestVehicles = new ArrayList<>();
        int bestScore = 0;

		for (Vehicle v : vList) {
			int vehicleScore = scoreVehicle(v);
            if (bestScore == vehicleScore) {
                // Just as good
                bestVehicles.add(v);
            }
            else if (bestScore < vehicleScore) {
                // New bets so reset
                bestVehicles = new ArrayList<>();
                bestVehicles.add(v);
                bestScore = vehicleScore;
            }
		}

		// Randomly select from the best vehicles.
		if (!bestVehicles.isEmpty()) {
			int bestVehicleIndex = RandomUtil.getRandomInt(bestVehicles.size() - 1);
			return bestVehicles.get(bestVehicleIndex);
		}
        return null;
	}

    /**
     * Scores the vehicle suitability for this mission.
     * 
     * @param v
     * @return Return -1 if not suitable at all
     */
    private int scoreVehicle(Vehicle v) {
        // needs better choosing based on capabilities and range
        return switch(v.getVehicleType()) {
            case CARGO_ROVER, EXPLORER_ROVER, TRANSPORT_ROVER -> 1;
            case LUV, DELIVERY_DRONE -> -1;
        };
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
        return super.getCurrentMissionLocation();
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
    public LoadingController getLoadingPlan() {
        if (getCurrentStep() instanceof MissionLoadVehicleStep ls) {
            return ls.getLoadingPlan();
        }
        return null;
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
        throw new UnsupportedOperationException("Unimplemented method 'getHelp'");
    }

    @Override
    public boolean isVehicleUnloadableHere(Settlement settlement) {
        return false;
    }
    
    /**
     * Extracst the Navpoints and calculates the proposed. This will extract the NavPoints for the Route.
     * 
     * It will also select a Vehicle based on the steps.
     * @param plan Steps for the mission
     */
    @Override
    protected void setSteps(List<MissionStep> plan) {
        route = plan.stream()
                        .filter(MissionTravelStep.class::isInstance)
                        .map (sc -> ((MissionTravelStep) sc).getDestination())
                        .toList();
        proposedDistance = route.stream()
                            .mapToDouble(NavPoint::getPointToPointDistance)
                            .sum();

        if (vehicle == null) {
            Vehicle best = findBestVehicle(getStartingPerson().getAssociatedSettlement());
            if (best == null) {
                abortMission(NO_AVAILABLE_VEHICLES);
                return;
            }
            setVehicle(best);
        }

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
		// TODO Auto-generated method stub
		return 0;
	}
}
