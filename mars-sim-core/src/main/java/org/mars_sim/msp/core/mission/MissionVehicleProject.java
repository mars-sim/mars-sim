/**
 * Mars Simulation Project
 * MissionVehicleProject.java
 * @date 2023-06-03
 * @author Barry Evans
 */
package org.mars_sim.msp.core.mission;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.MissionStatus;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.person.ai.mission.NavPoint;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.task.LoadingController;
import org.mars_sim.msp.core.project.ProjectStep;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * This represents a MissionProject that specialises in Vehcile based Mission.
 */
public class MissionVehicleProject extends MissionProject
    implements VehicleMission {
    
    private static final MissionStatus NO_AVAILABLE_VEHICLES = new MissionStatus("Mission.status.noVehicle");

    private Vehicle vehicle;
    private double proposedDistance;
    private List<NavPoint> route;
    private double distanceCompleted = 0;

    public MissionVehicleProject(String name, MissionType type, int priority, int maxMembers, Person leader) {
        super(name, type, priority, 2, maxMembers, leader);
    }

    /**
     * If the completed step is a Travel step then update the distance
     * @param ms The Mission step just completed
     */
    @Override
    protected void stepCompleted(MissionStep ms) {
        if (ms instanceof MissionTravelStep mts) {
            distanceCompleted += mts.getDestination().getDistance();
        }
    }

    /**
     * Set the Vehicle being used
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
     * Clear down the mission as it has been completed.
     */
    @Override
    protected void clearDown() {
        releaseVehicle(vehicle);
        super.clearDown();
    }
    /**
	 * Find the best suitable vehicle for the mission if possible.
	 *
	 * @return The selected bets vehicle; null if none found
	 */
	private final Vehicle findBestVehicle(Settlement base) {
		Collection<Vehicle> vList = base.getParkedVehicles();
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
     * Score the vehicle suitability for this Mission
     * @param v
     * @return Return -1 if not suitable at all
     */
    private int scoreVehicle(Vehicle v) {
        // TODO needs better choosing based on capabilities and range
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
     * Return the location based on the vehicle if the mission is active
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
    public double getDistanceProposed() {
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
        return getDistanceProposed() - getTotalDistanceTravelled();
    }

    @Override
    public double getDistanceCurrentLegRemaining() {
        if (getCurrentStep() instanceof MissionTravelStep mts) {
            return mts.getDestination().getDistance() - mts.getDistanceCovered();
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
    public MarsClock getLegETA() {
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getHelp'");
    }

    @Override
    public boolean isVehicleUnloadableHere(Settlement settlement) {
        return false;
    }
    
    /**
     * Extract the Navpoints and calculate the proposed. This will extract the NavPoints for the Route.
     * It will also select a Vehicle based on the steps.
     * @param plan Steps for the mission
     */
    @Override
    protected void setSteps(List<MissionStep> plan) {
        route = plan.stream()
                        .filter(sc -> sc instanceof MissionTravelStep)
                        .map (sc -> ((MissionTravelStep) sc).getDestination())
                        .toList();
        proposedDistance = route.stream()
                            .mapToDouble(NavPoint::getDistance)
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
     * How long will it take for this Misison to cover a distance
     * @param distance Distance to cover
     * @return Durstino in mSols
     */
    public double getEstimateTravelTime(double distance) {
        double result = 0D;
        double averageSpeed = vehicle.getBaseSpeed() * 0.8D;
		if (averageSpeed > 0) {
			result = distance / averageSpeed * MarsClock.MILLISOLS_PER_HOUR;
		}
        return result;
    }  
}
