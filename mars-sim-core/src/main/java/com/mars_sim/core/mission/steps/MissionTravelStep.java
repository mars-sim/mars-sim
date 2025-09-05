/**
 * Mars Simulation Project
 * MissionTravelStep.java
 * @date 2023-06-03
 * @author Barry Evans
 */
package com.mars_sim.core.mission.steps;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.mission.MissionStep;
import com.mars_sim.core.mission.MissionVehicleProject;
import com.mars_sim.core.person.ai.mission.NavPoint;
import com.mars_sim.core.person.ai.mission.VehicleMission;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.project.Stage;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.resource.SuppliesManifest;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.GroundVehicle;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.core.vehicle.VehicleController;
import com.mars_sim.core.vehicle.VehicleType;
import com.mars_sim.core.vehicle.task.DriveGroundVehicle;

/**
 * This is a step in a Mission that travels from one location to another.
 */
public class MissionTravelStep extends MissionStep {

    private static final long serialVersionUID = 1L;

	private static final SimLogger logger = SimLogger.getLogger(MissionTravelStep.class.getName());

    private static final double SMALL_DISTANCE = 0.1D;
    private NavPoint destination;

    private Coordinates startingCoordinate;

    public MissionTravelStep(MissionVehicleProject project, NavPoint destination) {
        super(project, Stage.ACTIVE, "Travel to " + destination.getDescription());

        this.destination = destination;
    }

    @Override
    protected boolean execute(Worker worker) {

        Vehicle vehicle = ((VehicleMission) getMission()).getVehicle();
        Coordinates current = vehicle.getCoordinates();
        Coordinates target =  destination.getLocation();

		if (current.equals(target)
					|| current.getDistance(target) < SMALL_DISTANCE) {
            Settlement base = destination.getSettlement();
            if (vehicle.getAssociatedSettlement().equals(base)) {
                logger.info(vehicle, "Arrived back home " + base.getName() + ".");
                vehicle.transfer(base);

                // Note: There is a problem with the Vehicle not being on the
                // surface vehicle list. The problem is a lack of transfer at the start of TRAVEL phase
                // This is temporary fix pending #474 which will revisit transfers
                if (!base.equals(vehicle.getContainerUnit())) {
					// Avoid calling vehicle.setContainerUnit(base)
					vehicle.transfer(base);
                    logger.severe(vehicle, "Forced its container unit to become its home base.");
                }
            }

            // Arrived
            complete();
            return true;
        }

		// Choose a driver
        boolean workedOn = false;
        var driver = vehicle.getOperator();
        if (isOperatable(vehicle) && ((driver == null) || driver.equals(worker))) {
            Task operateVehicleTask = createOperateVehicleTask(vehicle, worker);
            workedOn = assignTask(worker, operateVehicleTask);
        }
        return workedOn;
    }

    /**
     * Feel thsi should be in the Vehicle class
     * @param vehicle
     * @return
     */
    private boolean isOperatable(Vehicle vehicle) {
        return !vehicle.isBeaconOn() && !vehicle.isBeingTowed();
    }

    /**
     * Starts the step so record the Vehicles current location.
     */
    @Override
    protected void start() {
        super.start();
        startingCoordinate = getVehicle().getCoordinates();
    }

    /**
     * Gets the assigned vehicle.
     * 
     * @return
     */
    private Vehicle getVehicle() {
        return ((VehicleMission) getMission()).getVehicle();
    }

    /**
     * Creates the most appropriate Task to operate the vehicle.
     * 
     * @param vehicle
     * @param worker
     * @return
     */
    private Task createOperateVehicleTask(Vehicle vehicle, Worker worker) {
        if (vehicle instanceof GroundVehicle gv) {
            double coveredSoFar = getDistanceCovered();
            return new DriveGroundVehicle(worker, gv, destination.getLocation(),
                                            getMission().getPhaseStartTime(), coveredSoFar);
        }
        else {
            throw new IllegalArgumentException("Can only operatate Ground Vehicle " + vehicle.getName());
        }
    }

    /**
     * Gets the resources are needed for this travel. Should be vehicle fuel plus food and oxygen.
     */
    @Override
    protected void getRequiredResources(SuppliesManifest manifest, boolean addOptionals) {

        Vehicle vehicle = getVehicle();
        double distance = destination.getPointToPointDistance() - getDistanceCovered();
        MissionVehicleProject mvp = (MissionVehicleProject) getMission();

        // Must use the same logic in all cases otherwise too few fuel will be loaded
        double amount = vehicle.getFuelNeededForTrip(distance, addOptionals);
        manifest.addAmount(vehicle.getFuelTypeID(), amount, true);
         
        if (vehicle.getFuelTypeID() == ResourceUtil.METHANOL_ID) {
            // if useMargin is true, include more oxygen
            manifest.addAmount(ResourceUtil.OXYGEN_ID, 
            		VehicleController.RATIO_OXIDIZER_METHANOL * amount, true);
        }
        else if (vehicle.getFuelTypeID() == ResourceUtil.METHANE_ID) {
            // if useMargin is true, include more oxygen
            manifest.addAmount(ResourceUtil.OXYGEN_ID, 
            		VehicleController.RATIO_OXIDIZER_METHANE * amount, true);
        }

        if (VehicleType.isRover(vehicle.getVehicleType())) {
            double travelDuration = mvp.getEstimateTravelTime(distance);

            addLifeSupportResource(mvp.getMembers().size(), travelDuration, addOptionals, manifest);
        }
    }

    /**
     * Gets the distance covered so far.
     */
    public double getDistanceCovered() {
        if (startingCoordinate == null) {
            return 0D;
        }          
        return getVehicle().getCoordinates().getDistance(startingCoordinate);
    }

    /**
     * Gets the navpoint that the travel step taking the Mission.
     * 
     * @return
     */
    public NavPoint getDestination() {
        return destination;
    }

    /**
     * Completes the travel step transfer the Vehicle to the destination settlement.
    */ 
    @Override
    protected void complete() {
        Settlement target = destination.getSettlement();
        Vehicle v = getVehicle();

        // If target is a settlement then park it
		if (target != null) {
            // Should already have been transferred to the Settlment but safety check
            if (!target.equals(v.getSettlement())) {
                if (v.transfer(target)) {
                    logger.warning(v, "Had to transfer as part of Travel step to " + target.getName() + ".");
                }
                else {
                    logger.warning(v, "Unable to transfer to " + target.getName() + ".");
                }
            }

            // garage it
            target.getBuildingManager().addToGarage(v);
		}
        super.complete();
    }
    
    @Override
    public String toString() {
        return "Mission " + getMission().getName() + " travel to " + destination.getDescription();
    }
}
