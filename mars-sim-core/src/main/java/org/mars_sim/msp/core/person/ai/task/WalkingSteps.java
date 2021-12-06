/*
 * Mars Simulation Project
 * WalkingSteps.java
 * @date 2021-12-06
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LocalBoundedObject;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Airlock;
import org.mars_sim.msp.core.structure.AirlockType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * A helper class for determining the walking steps from one location to another.
 */
public class WalkingSteps
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static SimLogger logger = SimLogger.getLogger(WalkingSteps.class.getName());

	// Data members.
	private boolean canWalkAllSteps;

	private Person person;
	private Robot robot;

	private List<WalkStep> walkingStepList;

	/**
	 * constructor 1.
	 */
	public WalkingSteps(Person person, double xLoc, double yLoc, double zLoc, LocalBoundedObject interiorObject) {
        this.person = person;

        // Initialize data members.
        canWalkAllSteps = true;

        walkingStepList = new CopyOnWriteArrayList<>();

        // Determine initial walk state.
        WalkState initialWalkState = determineInitialWalkState(person);

        // Determine destination walk state.
        // NOTE: will incorporate zLoc
        WalkState destinationWalkState = determineDestinationWalkState(xLoc, yLoc, interiorObject);

        if (initialWalkState != null) {
	        // Determine walking steps to destination.
	        determineWalkingSteps(initialWalkState, destinationWalkState);
        }

        else {
            logger.severe(person, "initialWalkState : " + initialWalkState);
        }
    }

	/**
	 * constructor 2.
	 */
	public WalkingSteps(Robot robot, double xLoc, double yLoc, double zLoc, LocalBoundedObject interiorObject) {
        this.robot = robot;

        // Initialize data members.
        canWalkAllSteps = true;

        walkingStepList = new CopyOnWriteArrayList<>();

        // Determine initial walk state.
        RobotWalkState initialWalkState = determineInitialRobotWalkState(robot);

        // Determine destination walk state.
        // NOTE: will incorporate zLoc
        RobotWalkState destinationWalkState = determineDestinationRobotWalkState(xLoc, yLoc, interiorObject);

        if (initialWalkState != null) {
	        // Determine walking steps to destination.
	        determineWalkingSteps(initialWalkState, destinationWalkState);
        }

        else {
            logger.severe(robot, "initialWalkState : " + initialWalkState);
        }
    }

    /**
     * Gets a list of walking steps to the destination.
     *
     * @return list of walk steps.  Returns empty list if a valid path isn't found.
     */
    public List<WalkStep> getWalkingStepsList() {
        return walkingStepList;
    }

    /**
     * Gets the number of walking steps to the destination.
     *
     * @return number of walking steps.  Returns 0 if a valid path isn't found.
     */
    public int getWalkingStepsNumber() {
        int result = 0;

        if (walkingStepList != null) {
            result = walkingStepList.size();
        }

        return result;
    }

    /**
     * Checks if a valid path has been found to the destination.
     *
     * @return true if valid path to destination found.
     */
    public boolean canWalkAllSteps() {
        return canWalkAllSteps;
    }

    /**
     * Determines the person's initial walk state.
     *
     * @param person the person walking.
     * @return the initial location state.
     */
    private WalkState determineInitialWalkState(Person person) {

        WalkState result = null;

        // Determine initial walk state based on person's location situation.
        if (person.isOutside()) {

            result = new WalkState(WalkState.OUTSIDE_LOC);

			logger.log(person, Level.FINER, 4000,
                    "Outside.");
        }

        else if (person.isInSettlement()) {

            Building building = person.getBuildingLocation();

            if (building == null) {
            	logger.log(person, Level.WARNING, 4000,
                        "Inside the settlement but not in a building.");
            	return null;
            }

            result = new WalkState(WalkState.BUILDING_LOC);
            result.building = building;

            logger.log(person, Level.FINER, 4000,
            		"Inside " + building
					+ ".");
        }

        else if (person.isInVehicle()) {

            Vehicle vehicle = person.getVehicle();

            if (vehicle instanceof Rover) {
                result = new WalkState(WalkState.ROVER_LOC);
                result.rover = (Rover) vehicle;

                logger.log(person, Level.FINER, 4000,
                		"Inside " + vehicle + ".");
            }

            else {
                result = new WalkState(WalkState.OUTSIDE_LOC);

                logger.log(person, Level.FINER, 4000,
                		 "Outside.");
            }
        }

        else {

        	logger.log(person, Level.WARNING, 4000,
        			"Can not identify parent container.");
        }

        // Set person X and Y location.
        if (result != null) {
            result.loc = person.getPosition().toPoint();
        }

        return result;
    }

   private RobotWalkState determineInitialRobotWalkState(Robot robot) {

	   RobotWalkState result = null;

        // Determine initial walk state based on robot's location situation.
        if (robot.isInSettlement()) {

            Building building = robot.getBuildingLocation();
            if (building == null) {
                return null;
            }

            result = new RobotWalkState(RobotWalkState.BUILDING_LOC);
            result.building = building;

            if (!LocalAreaUtil.isLocationWithinLocalBoundedObject(robot.getXLocation(),
                    robot.getYLocation(), building)) {
            	logger.log(robot, Level.SEVERE, 5000,
            			"Invalid walk start location at (" +
                        robot.getXLocation() + ", " + robot.getYLocation()
                        + ") and not within " + building + ".");
            }
        }

        else {
        	logger.log(robot, Level.SEVERE, 5000,
        			"Invalid location situation for walking task.");
        }

        // Set robot X and Y location.
        if (result != null) {
            result.loc = robot.getPosition().toPoint();
        }

        return result;
    }

    /**
     * Determines the destination walk state.
     *
     * @param xLoc the destination X location.
     * @param yLoc the destination Y location.
     * @param interiorObject the destination interior object (inhabitable building or rover).
     * @return destination walk state.
     */
    private WalkState determineDestinationWalkState(double xLoc, double yLoc,
            LocalBoundedObject interiorObject) {

        WalkState result = null;

        if (interiorObject instanceof Building) {
            Building building = (Building) interiorObject;
            result = new WalkState(WalkState.BUILDING_LOC);
            result.building = building;

            if (!LocalAreaUtil.isLocationWithinLocalBoundedObject(xLoc, yLoc, building)) {
            	if (person != null)
            		logger.log(person, Level.SEVERE, 4000,
            			"Invalid walk destination location. (" +
                        xLoc + ", " + yLoc + ") and not within " + building + ".");
            	else if (robot != null)
        			logger.log(robot, Level.SEVERE, 4000,
        					"Invalid walk destination location at (" +
                            xLoc + ", " + yLoc + ") and not within " + building + ".");
            }
        }
        else if (interiorObject instanceof Rover) {

        	if (person != null) {
	            Rover rover = (Rover) interiorObject;
	            result = new WalkState(WalkState.ROVER_LOC);
	            result.rover = rover;

	            if (!LocalAreaUtil.isLocationWithinLocalBoundedObject(xLoc, yLoc, rover)) {
	            	logger.log(person, Level.SEVERE, 5000,
	            				"Invalid walk destination location at (" +
	                        xLoc + ", " + yLoc + ") and not within rover " + rover + ".");
	            }
        	}
        }

        else {
        	if (person != null) {
        		result = new WalkState(WalkState.OUTSIDE_LOC);
        	}
        }

        result.loc = new Point2D.Double(xLoc, yLoc);
        return result;
    }

    /**
     * Determines the destination walk state.
     *
     * @param xLoc the destination X location.
     * @param yLoc the destination Y location.
     * @param interiorObject the destination interior object (inhabitable building or rover).
     * @return destination walk state.
     */
    private RobotWalkState determineDestinationRobotWalkState(double xLoc, double yLoc,
            LocalBoundedObject interiorObject) {

    	RobotWalkState result = null;

        if (interiorObject instanceof Building) {
            Building building = (Building) interiorObject;
            result = new RobotWalkState(RobotWalkState.BUILDING_LOC);
            result.building = building;

            if (!LocalAreaUtil.isLocationWithinLocalBoundedObject(xLoc, yLoc, building)) {
        			logger.log(robot, Level.SEVERE, 5000,
        					"Invalid walk destination location at (" +
                            xLoc + ", " + yLoc + ") and not within building " + building);
            }

            result.loc = new Point2D.Double(xLoc, yLoc);
        }
        return result;
    }

    /**
     * Determine the walk steps from an initial walk state to a destination walk state.
     * @param initialWalkState the initial walk state.
     * @param destinationWalkState the destination walk state.
     */
    private void determineWalkingSteps(WalkState initialWalkState, WalkState destinationWalkState) {

        // If cannot walk steps, return.
        if (!canWalkAllSteps) {
            return;
        }

        if (person != null) {
	        // Determine walking steps based on initial walk state.
	        switch(initialWalkState.stateType) {

	            case WalkState.BUILDING_LOC:    determineBuildingInteriorWalkingSteps(initialWalkState,
	                    destinationWalkState);
	                                            break;
	            case WalkState.ROVER_LOC:       determineRoverInteriorWalkingSteps(initialWalkState,
	                    destinationWalkState);
	                                            break;
	            case WalkState.INTERIOR_AIRLOCK:determineAirlockInteriorWalkingSteps(initialWalkState,
	                    destinationWalkState);
	                                            break;
	            case WalkState.EXTERIOR_AIRLOCK:determineAirlockExteriorWalkingSteps(initialWalkState,
	                    destinationWalkState);
	                                            break;
	            case WalkState.OUTSIDE_LOC:     determineOutsideWalkingSteps(initialWalkState,
	                    destinationWalkState);
	                                            break;
	            case WalkState.LADDER_LOC:		determineLadderWalkingSteps(initialWalkState,
	                    destinationWalkState);
	                                            break;
	            default:                        throw new IllegalArgumentException("Invalid walk state type: " +
	                    initialWalkState.stateType);
	        }
        }
        else if (robot != null ){
	        // Determine walking steps based on initial walk state.
	        switch(initialWalkState.stateType) {

	            case WalkState.BUILDING_LOC:    determineBuildingInteriorWalkingSteps(initialWalkState,
	                    destinationWalkState);
	                                            break;
//	            case WalkState.ROVER_LOC:       determineRoverInteriorWalkingSteps(initialWalkState,
//	                    destinationWalkState);
//	                                            break;
//	            case WalkState.INTERIOR_AIRLOCK:determineAirlockInteriorWalkingSteps(initialWalkState,
//	                    destinationWalkState);
//	                                            break;
//	            case WalkState.EXTERIOR_AIRLOCK:determineAirlockExteriorWalkingSteps(initialWalkState,
//	                    destinationWalkState);
//	                                            break;
//	            case WalkState.OUTSIDE_LOC:     determineOutsideWalkingSteps(initialWalkState,
//	                    destinationWalkState);
//	                                            break;
	            default:                        throw new IllegalArgumentException("Invalid walk state type: " +
	                    initialWalkState.stateType);
	        }
        }
        return;
    }

    /**
     * Determine the walk steps from an initial walk state to a destination walk state.
     * @param initialWalkState the initial walk state.
     * @param destinationWalkState the destination walk state.
     */
    private void determineWalkingSteps(RobotWalkState initialWalkState, RobotWalkState destinationWalkState) {

        // If cannot walk steps, return.
        if (!canWalkAllSteps) {
            return;
        }

        // Determine walking steps based on initial walk state.
        switch(initialWalkState.stateType) {

            case RobotWalkState.BUILDING_LOC:    determineBuildingInteriorWalkingSteps(initialWalkState,
                    destinationWalkState);
                                            break;
            default:                        throw new IllegalArgumentException("Invalid walk state type: " +
                    initialWalkState.stateType);
        }

        return;
    }


    /**
     * Determine the walking steps in a building interior.
     * @param initialWalkState the initial walk state.
     * @param destinationWalkState the destination walk state.
     */
    private void determineBuildingInteriorWalkingSteps(WalkState initialWalkState,
            WalkState destinationWalkState) {

        // Determine walking steps based on the destination walk state.

    	if (person != null) {
	        switch(destinationWalkState.stateType) {

	            case WalkState.BUILDING_LOC:    determineBuildingInteriorToBuildingInteriorWalkingSteps(
	                    initialWalkState, destinationWalkState);
	                                            break;
	            case WalkState.ROVER_LOC:       determineBuildingInteriorToRoverWalkingSteps(
	                    initialWalkState, destinationWalkState);
	                                            break;
	            case WalkState.OUTSIDE_LOC:     determineBuildingInteriorToOutsideWalkingSteps(
	                    initialWalkState, destinationWalkState);
	                                            break;
	            default:                        throw new IllegalArgumentException("Invalid walk state type: " +
	                initialWalkState.stateType);
	        }
    	}
    	else {
            switch(destinationWalkState.stateType) {

	            case WalkState.BUILDING_LOC:    determineBuildingInteriorToBuildingInteriorWalkingSteps(
	                    initialWalkState, destinationWalkState);
                                            break;
//	            case WalkState.ROVER_LOC:       determineBuildingInteriorToRoverWalkingSteps(
//	            		initialWalkState, destinationWalkState);
//                                            break;
//            case WalkState.OUTSIDE_LOC:     determineBuildingInteriorToOutsideWalkingSteps(
//                    initialWalkState, destinationWalkState);
//                                            break;
	            default:                        throw new IllegalArgumentException("Invalid walk state type: " +
	                initialWalkState.stateType);
            }
    	}
    }

    /**
     * Determine the walking steps in a building interior.
     * @param initialWalkState the initial walk state.
     * @param destinationWalkState the destination walk state.
     */
    private void determineBuildingInteriorWalkingSteps(RobotWalkState initialWalkState,
    		RobotWalkState destinationWalkState) {

        // Determine walking steps based on the destination walk state.
        switch(destinationWalkState.stateType) {

            case RobotWalkState.BUILDING_LOC:    determineBuildingInteriorToBuildingInteriorRobotWalkingSteps(
                    initialWalkState, destinationWalkState);
                                        break;
            default:                        throw new IllegalArgumentException("Invalid walk state type: " +
                initialWalkState.stateType);
        }

    }

    /**
     * Determine the walking steps from a building interior to another building interior.
     * @param initialWalkState the initial walk state.
     * @param destinationWalkState the destination walk state.
     */
    private void determineBuildingInteriorToBuildingInteriorWalkingSteps(WalkState initialWalkState,
            WalkState destinationWalkState) {

        Building initialBuilding = initialWalkState.building;
        Building destinationBuilding = destinationWalkState.building;
        Settlement settlement = initialBuilding.getSettlement();

        // Check if two buildings have walkable path.
        if (settlement.getBuildingConnectorManager().hasValidPath(initialBuilding, destinationBuilding)) {

            // Add settlement interior walk step.
            createWalkSettlementInteriorStep(destinationWalkState.loc, destinationBuilding);
        }
        else {

            // Find closest walkable airlock to destination.
            Airlock airlock = settlement.getClosestWalkableAvailableAirlock(initialBuilding,
                    destinationWalkState.loc);
            if (airlock == null) {
                canWalkAllSteps = false;
                if (person != null) {
        			logger.log(person, Level.WARNING, 10_000,
        					"No walkable airlock from building interior to building interior in "
        					+ person.getBuildingLocation().getNickName() + ".");
                }
                else {
                	logger.log(robot, Level.WARNING, 10_000,
                			"No walkable airlock from building interior to building interior in "
                			+ robot.getBuildingLocation().getNickName() + ".");
                }
                return;
            }

            Building airlockBuilding = (Building) airlock.getEntity();
            Point2D interiorAirlockPosition = airlock.getAvailableInteriorPosition();

            // Add settlement interior walk step to starting airlock.
            createWalkSettlementInteriorStep(interiorAirlockPosition, airlockBuilding);

            // Create interior airlock walk state.
            WalkState interiorAirlockState = new WalkState(WalkState.INTERIOR_AIRLOCK);
            interiorAirlockState.airlock = airlock;
            interiorAirlockState.building = airlockBuilding;
            interiorAirlockState.loc = interiorAirlockPosition;

            determineWalkingSteps(interiorAirlockState, destinationWalkState);
        }

    }

    /**
     * Determine the walking steps from a building interior to another building interior.
     *
     * @param initialWalkState the initial walk state.
     * @param destinationWalkState the destination walk state.
     */
    private void determineBuildingInteriorToBuildingInteriorRobotWalkingSteps(RobotWalkState initialWalkState,
    		RobotWalkState destinationWalkState) {

        Building initialBuilding = initialWalkState.building;
        Building destinationBuilding = destinationWalkState.building;
        Settlement settlement = initialBuilding.getSettlement();

        // Check if two buildings have walkable path.
        if (settlement.getBuildingConnectorManager().hasValidPath(initialBuilding, destinationBuilding)) {

            // Add settlement interior walk step.
            createWalkSettlementInteriorStep(destinationWalkState.loc, destinationBuilding);
        }
    }

    /**
     * Determine the walking steps from a building interior to a rover interior.
     *
     * @param initialWalkState the initial walk state.
     * @param destinationWalkState the destination walk state.
     */
    private void determineBuildingInteriorToRoverWalkingSteps(WalkState initialWalkState,
            WalkState destinationWalkState) {

        Building initialBuilding = initialWalkState.building;
        Rover destinationRover = destinationWalkState.rover;
        Settlement settlement = initialBuilding.getSettlement();

        // Check if rover is parked in garage or outside.
        Building garageBuilding = BuildingManager.getBuilding(destinationRover);
        if (garageBuilding != null) {

            // Create walking steps to garage building.
            WalkState garageWalkState = new WalkState(WalkState.BUILDING_LOC);
            garageWalkState.building = garageBuilding;
            garageWalkState.loc = destinationWalkState.loc;
            determineBuildingInteriorToBuildingInteriorWalkingSteps(initialWalkState, garageWalkState);

            // Add enter rover walk step.
            WalkStep enterRoverInGarageStep = new WalkStep(WalkStep.ENTER_GARAGE_ROVER);
            enterRoverInGarageStep.rover = destinationRover;
            enterRoverInGarageStep.building = garageBuilding;
            enterRoverInGarageStep.loc = destinationWalkState.loc;
            walkingStepList.add(enterRoverInGarageStep);
        }
        else {
            // Find closest walkable airlock to destination.
            Airlock airlock = settlement.getClosestWalkableAvailableAirlock(initialBuilding,
                    destinationWalkState.loc);
            if (airlock == null) {
                canWalkAllSteps = false;
                if (person != null) {
                	logger.log(person, Level.WARNING, 10_000,
                		"No walkable airlock from building interior to building interior in "
                		+ person.getBuildingLocation().getNickName());
                }
                else {
                	logger.log(robot, Level.WARNING, 10_000,
                		"No walkable airlock from building interior to building interior in "
                    	+ robot.getBuildingLocation().getNickName());
                }
               return;
            }

            Building airlockBuilding = (Building) airlock.getEntity();
            Point2D interiorAirlockPosition = airlock.getAvailableInteriorPosition();

            // Add settlement interior walk step to starting airlock.
            createWalkSettlementInteriorStep(interiorAirlockPosition, airlockBuilding);

            // Create interior airlock walk state.
            WalkState interiorAirlockState = new WalkState(WalkState.INTERIOR_AIRLOCK);
            interiorAirlockState.airlock = airlock;
            interiorAirlockState.building = airlockBuilding;
            interiorAirlockState.loc = interiorAirlockPosition;

            determineWalkingSteps(interiorAirlockState, destinationWalkState);
        }
    }

    /**
     * Determine the walking steps between a building interior and outside.
     *
     * @param initialWalkState the initial walk state.
     * @param destinationWalkState the destination walk state.
     */
    private void determineBuildingInteriorToOutsideWalkingSteps(WalkState initialWalkState,
            WalkState destinationWalkState) {

        Building initialBuilding = initialWalkState.building;
        Settlement settlement = initialBuilding.getSettlement();

        // Find closest walkable airlock to destination.
        Airlock airlock = settlement.getClosestWalkableAvailableAirlock(initialBuilding,
                destinationWalkState.loc);
        if (airlock == null) {
            canWalkAllSteps = false;
            if (person != null) {
            	logger.log(person, Level.WARNING, 10_000,
            			"No walkable airlock from building interior to building interior in "
            		+ person.getBuildingLocation().getNickName());
            }
            else if (robot != null) {
            	logger.log(robot, Level.WARNING, 10_000,
            			"No walkable airlock from building interior to building interior in "
                		+ robot.getBuildingLocation().getNickName());
            }
           return;
        }

        Building airlockBuilding = (Building) airlock.getEntity();
        Point2D interiorAirlockPosition = airlock.getAvailableInteriorPosition();

        // Add settlement interior walk step to starting airlock.
        createWalkSettlementInteriorStep(interiorAirlockPosition, airlockBuilding);

        // Create interior airlock walk state.
        WalkState interiorAirlockState = new WalkState(WalkState.INTERIOR_AIRLOCK);
        interiorAirlockState.airlock = airlock;
        interiorAirlockState.building = airlockBuilding;
        interiorAirlockState.loc = interiorAirlockPosition;

        determineWalkingSteps(interiorAirlockState, destinationWalkState);
    }

    /**
     * Determine the walking steps between two rover interior locations.
     *
     * @param initialWalkState the initial walk state.
     * @param destinationWalkState the destination walk state.
     */
    private void determineRoverInteriorWalkingSteps(WalkState initialWalkState,
            WalkState destinationWalkState) {

        switch(destinationWalkState.stateType) {

            case WalkState.BUILDING_LOC:    determineRoverToBuildingInteriorWalkingSteps(
                    initialWalkState, destinationWalkState);
                                            break;
            case WalkState.ROVER_LOC:       determineRoverToRoverWalkingSteps(
                    initialWalkState, destinationWalkState);
                                            break;
            case WalkState.OUTSIDE_LOC:     determineRoverToOutsideWalkingSteps(
                    initialWalkState, destinationWalkState);
                                            break;
            default:                        throw new IllegalArgumentException("Invalid walk state type: " +
                    initialWalkState.stateType);
        }
    }

    /**
     * Determine the walking steps between a rover interior and a building interior.
     *
     * @param initialWalkState the initial walk state.
     * @param destinationWalkState the destination walk state.
     */
    private void determineRoverToBuildingInteriorWalkingSteps(WalkState initialWalkState,
            WalkState destinationWalkState) {

        Rover initialRover = initialWalkState.rover;

        // Check if rover is parked in garage or outside.
        Building garageBuilding = BuildingManager.getBuilding(initialRover);
        if (garageBuilding != null) {

            // Add exit rover walk step.
            WalkStep exitRoverInGarageStep = new WalkStep(WalkStep.EXIT_GARAGE_ROVER);
            exitRoverInGarageStep.rover = initialRover;
            exitRoverInGarageStep.building = garageBuilding;
            exitRoverInGarageStep.loc = initialWalkState.loc;
            walkingStepList.add(exitRoverInGarageStep);

            // Create walking steps to destination building.
            WalkState buildingWalkState = new WalkState(WalkState.BUILDING_LOC);
            buildingWalkState.building = garageBuilding;
            buildingWalkState.loc = initialWalkState.loc;
            determineBuildingInteriorToBuildingInteriorWalkingSteps(buildingWalkState,
                    destinationWalkState);
        }
        else {
            // Walk to rover airlock.
            Airlock airlock = initialRover.getAirlock();
            Point2D interiorAirlockPosition = airlock.getAvailableInteriorPosition();

            // Add rover interior walk step to starting airlock.
            createWalkRoverInteriorStep(interiorAirlockPosition, initialRover);

            // Create interior airlock walk state.
            WalkState interiorAirlockState = new WalkState(WalkState.INTERIOR_AIRLOCK);
            interiorAirlockState.airlock = airlock;
            interiorAirlockState.rover = initialRover;
            interiorAirlockState.loc = interiorAirlockPosition;

            determineWalkingSteps(interiorAirlockState, destinationWalkState);
        }
    }

    /**
     * Determine the walking steps between a rover interior and a rover interior.
     *
     * @param initialWalkState the initial walk state.
     * @param destinationWalkState the destination walk state.
     */
    private void determineRoverToRoverWalkingSteps(WalkState initialWalkState,
            WalkState destinationWalkState) {

        Rover initialRover = initialWalkState.rover;
        Rover destinationRover = destinationWalkState.rover;

        if (initialRover.equals(destinationRover)) {

            // Walk to rover interior location.
            createWalkRoverInteriorStep(destinationWalkState.loc, destinationRover);
        }
        else {
            // Check if initial rover is in a garage.
            Building garageBuilding = BuildingManager.getBuilding(initialRover);
            if (garageBuilding != null) {

                // Add exit rover walk step.
                WalkStep exitRoverInGarageStep = new WalkStep(WalkStep.EXIT_GARAGE_ROVER);
                exitRoverInGarageStep.rover = initialRover;
                exitRoverInGarageStep.building = garageBuilding;
                exitRoverInGarageStep.loc = initialWalkState.loc;
                walkingStepList.add(exitRoverInGarageStep);

                // Create walking steps to destination rover.
                WalkState buildingWalkState = new WalkState(WalkState.BUILDING_LOC);
                buildingWalkState.building = garageBuilding;
                buildingWalkState.loc = initialWalkState.loc;

                determineBuildingInteriorToRoverWalkingSteps(buildingWalkState,
                        destinationWalkState);
            }
            else {
                // Walk to rover airlock.
                Airlock airlock = initialRover.getAirlock();
                Point2D interiorAirlockPosition = airlock.getAvailableInteriorPosition();

                // Add rover interior walk step to starting airlock.
                createWalkRoverInteriorStep(interiorAirlockPosition, initialRover);

                // Create interior airlock walk state.
                WalkState interiorAirlockState = new WalkState(WalkState.INTERIOR_AIRLOCK);
                interiorAirlockState.airlock = airlock;
                interiorAirlockState.rover = initialRover;
                interiorAirlockState.loc = interiorAirlockPosition;

                determineWalkingSteps(interiorAirlockState, destinationWalkState);
            }
        }
    }

    /**
     * Determine the walking steps between a rover interior and outside location.
     *
     * @param initialWalkState the initial walk state.
     * @param destinationWalkState the destination walk state.
     */
    private void determineRoverToOutsideWalkingSteps(WalkState initialWalkState,
            WalkState destinationWalkState) {

        Rover initialRover = initialWalkState.rover;

        // Check if rover is parked in garage or outside.
        Building garageBuilding = BuildingManager.getBuilding(initialRover);
        if (garageBuilding != null) {

            // Add exit rover walk step.
            WalkStep exitRoverInGarageStep = new WalkStep(WalkStep.EXIT_GARAGE_ROVER);
            exitRoverInGarageStep.rover = initialRover;
            exitRoverInGarageStep.building = garageBuilding;
            exitRoverInGarageStep.loc = initialWalkState.loc;
            walkingStepList.add(exitRoverInGarageStep);

            // Create walking steps to destination building.
            WalkState buildingWalkState = new WalkState(WalkState.BUILDING_LOC);
            buildingWalkState.building = garageBuilding;
            buildingWalkState.loc = initialWalkState.loc;

            determineBuildingInteriorToOutsideWalkingSteps(buildingWalkState,
                    destinationWalkState);
        }
        else {

            // Walk to rover airlock.
            Airlock airlock = initialRover.getAirlock();
            Point2D interiorAirlockPosition = airlock.getAvailableInteriorPosition();

            // Add rover interior walk step to starting airlock.
            createWalkRoverInteriorStep(interiorAirlockPosition, initialRover);

            // Create interior airlock walk state.
            WalkState interiorAirlockState = new WalkState(WalkState.INTERIOR_AIRLOCK);
            interiorAirlockState.airlock = airlock;
            interiorAirlockState.rover = initialRover;
            interiorAirlockState.loc = interiorAirlockPosition;

            determineWalkingSteps(interiorAirlockState, destinationWalkState);
        }
    }

    /**
     * Determine the walking steps from an airlock interior location.
     *
     * @param initialWalkState the initial walk state.
     * @param destinationWalkState the destination walk state.
     */
    private void determineAirlockInteriorWalkingSteps(WalkState initialWalkState,
            WalkState destinationWalkState) {

        switch(destinationWalkState.stateType) {

            case WalkState.BUILDING_LOC:    determineAirlockInteriorToBuildingInteriorWalkingSteps(
                    initialWalkState, destinationWalkState);
                                            break;
            case WalkState.ROVER_LOC:       determineAirlockInteriorToRoverWalkingSteps(
                    initialWalkState, destinationWalkState);
                                            break;
            case WalkState.OUTSIDE_LOC:     determineAirlockInteriorToOutsideWalkingSteps(
                    initialWalkState, destinationWalkState);
                                            break;
            default:                        throw new IllegalArgumentException("Invalid walk state type: " +
                    initialWalkState.stateType);
        }
    }

    /**
     * Determine the walking steps between an airlock interior and a building interior location.
     *
     * @param initialWalkState the initial walk state.
     * @param destinationWalkState the destination walk state.
     */
    private void determineAirlockInteriorToBuildingInteriorWalkingSteps(WalkState initialWalkState,
            WalkState destinationWalkState) {

        Airlock airlock = initialWalkState.airlock;
        Building destinationBuilding = destinationWalkState.building;
        Settlement settlement = destinationBuilding.getSettlement();

        // Check if airlock is for a building or a rover.
        if (airlock.getAirlockType() == AirlockType.BUILDING_AIRLOCK) {

            Building airlockBuilding = (Building) airlock.getEntity();

            // Check if walkable interior path between airlock building and destination building.
            if (settlement.getBuildingConnectorManager().hasValidPath(airlockBuilding, destinationBuilding)) {

                // Add settlement interior walk step.
                createWalkSettlementInteriorStep(destinationWalkState.loc, destinationBuilding);
            }
            else {

                // Add exit airlock walk step.
                createExitAirlockStep(airlock);

                // Create exterior airlock walk state.
                WalkState exteriorAirlockState = new WalkState(WalkState.EXTERIOR_AIRLOCK);
                exteriorAirlockState.airlock = airlock;
                Point2D exteriorAirlockPosition = airlock.getAvailableExteriorPosition();
                exteriorAirlockState.loc = exteriorAirlockPosition;

                determineWalkingSteps(exteriorAirlockState, destinationWalkState);
            }
        }
        else {

            // Add exit airlock walk step.
            createExitAirlockStep(airlock);

            // Create exterior airlock walk state.
            WalkState exteriorAirlockState = new WalkState(WalkState.EXTERIOR_AIRLOCK);
            exteriorAirlockState.airlock = airlock;
            Point2D exteriorAirlockPosition = airlock.getAvailableExteriorPosition();
            exteriorAirlockState.loc = exteriorAirlockPosition;

            determineWalkingSteps(exteriorAirlockState, destinationWalkState);
        }
    }

    /**
     * Determine the walking steps between an airlock interior and rover interior location.
     *
     * @param initialWalkState the initial walk state.
     * @param destinationWalkState the destination walk state.
     */
    private void determineAirlockInteriorToRoverWalkingSteps(WalkState initialWalkState,
            WalkState destinationWalkState) {

        Airlock airlock = initialWalkState.airlock;
        Rover destinationRover = destinationWalkState.rover;

        // Check if airlock is for a building or a rover.
        if (airlock.getAirlockType() == AirlockType.BUILDING_AIRLOCK) {

            Building airlockBuilding = (Building) airlock.getEntity();

            // Check if rover is in a garage or outside.
            Building garageBuilding = BuildingManager.getBuilding(destinationRover);
            if (garageBuilding != null) {

                // Check if garage building has a walkable interior path from airlock building.
                Settlement settlement = airlockBuilding.getSettlement();
                if (settlement.getBuildingConnectorManager().hasValidPath(airlockBuilding, garageBuilding)) {

                    // Add settlement interior walk step.
                    createWalkSettlementInteriorStep(destinationWalkState.loc, garageBuilding);

                    // Add enter rover walk step.
                    WalkStep enterRoverInGarageStep = new WalkStep(WalkStep.ENTER_GARAGE_ROVER);
                    enterRoverInGarageStep.rover = destinationRover;
                    enterRoverInGarageStep.building = garageBuilding;
                    enterRoverInGarageStep.loc = destinationWalkState.loc;
                    walkingStepList.add(enterRoverInGarageStep);
                }
                else {

                    // Add exit airlock walk step.
                    createExitAirlockStep(airlock);

                    // Create exterior airlock walk state.
                    WalkState exteriorAirlockState = new WalkState(WalkState.EXTERIOR_AIRLOCK);
                    exteriorAirlockState.airlock = airlock;
                    Point2D exteriorAirlockPosition = airlock.getAvailableExteriorPosition();
                    exteriorAirlockState.loc = exteriorAirlockPosition;

                    determineWalkingSteps(exteriorAirlockState, destinationWalkState);
                }
            }
            else {

                // Add exit airlock walk step.
                createExitAirlockStep(airlock);

                // Create exterior airlock walk state.
                WalkState exteriorAirlockState = new WalkState(WalkState.EXTERIOR_AIRLOCK);
                exteriorAirlockState.airlock = airlock;
                Point2D exteriorAirlockPosition = airlock.getAvailableExteriorPosition();
                exteriorAirlockState.loc = exteriorAirlockPosition;

                determineWalkingSteps(exteriorAirlockState, destinationWalkState);
            }
        }
        else {

            Rover airlockRover = (Rover) airlock.getEntity();

            // Check if airlockRover is the same as destinationRover.
            if (airlockRover.equals(destinationRover)) {

                // Create walking step internal to rover.
                createWalkRoverInteriorStep(destinationWalkState.loc, destinationRover);
            }
            else {

                // Add exit airlock walk step.
                createExitAirlockStep(airlock);

                // Create exterior airlock walk state.
                WalkState exteriorAirlockState = new WalkState(WalkState.EXTERIOR_AIRLOCK);
                exteriorAirlockState.airlock = airlock;
                Point2D exteriorAirlockPosition = airlock.getAvailableExteriorPosition();
                exteriorAirlockState.loc = exteriorAirlockPosition;

                determineWalkingSteps(exteriorAirlockState, destinationWalkState);
            }
        }
    }

    /**
     * Determine the walking steps between an airlock interior and an outside location.
     *
     * @param initialWalkState the initial walk state.
     * @param destinationWalkState the destination walk state.
     */
    private void determineAirlockInteriorToOutsideWalkingSteps(WalkState initialWalkState,
            WalkState destinationWalkState) {

        Airlock airlock = initialWalkState.airlock;

        // Add exit airlock walk step.
        createExitAirlockStep(airlock);

        // Create exterior airlock walk state.
        WalkState exteriorAirlockState = new WalkState(WalkState.EXTERIOR_AIRLOCK);
        exteriorAirlockState.airlock = airlock;
        Point2D exteriorAirlockPosition = airlock.getAvailableExteriorPosition();
        exteriorAirlockState.loc = exteriorAirlockPosition;

        determineWalkingSteps(exteriorAirlockState, destinationWalkState);
    }

    /**
     * Determine the walking steps from an airlock exterior location.
     *
     * @param initialWalkState the initial walk state.
     * @param destinationWalkState the destination walk state.
     */
    private void determineAirlockExteriorWalkingSteps(WalkState initialWalkState,
            WalkState destinationWalkState) {

        switch(destinationWalkState.stateType) {

            case WalkState.BUILDING_LOC:    determineAirlockExteriorToBuildingInteriorWalkingSteps(
                    initialWalkState, destinationWalkState);
                                            break;
            case WalkState.ROVER_LOC:       determineAirlockExteriorToRoverWalkingSteps(
                    initialWalkState, destinationWalkState);
                                            break;
            case WalkState.OUTSIDE_LOC:     determineAirlockExteriorToOutsideWalkingSteps(
                    initialWalkState, destinationWalkState);
                                            break;
            default:                        throw new IllegalArgumentException("Invalid walk state type: " +
                    initialWalkState.stateType);
        }
    }

    /**
     * Determine the walking steps between an airlock exterior and building interior location.
     *
     * @param initialWalkState the initial walk state.
     * @param destinationWalkState the destination walk state.
     */
    private void determineAirlockExteriorToBuildingInteriorWalkingSteps(WalkState initialWalkState,
            WalkState destinationWalkState) {

        Airlock airlock = initialWalkState.airlock;
        Building destinationBuilding = destinationWalkState.building;

        if (airlock.getAirlockType() == AirlockType.BUILDING_AIRLOCK) {

            Building airlockBuilding = (Building) airlock.getEntity();

            // Check if valid interior walking path between airlock building and destination building.
            Settlement settlement = airlockBuilding.getSettlement();
            if (settlement.getBuildingConnectorManager().hasValidPath(airlockBuilding, destinationBuilding)) {

                // Create enter airlock walk step.
                createEnterAirlockStep(airlock);

                // Create airlock interior state.
                WalkState interiorAirlockState = new WalkState(WalkState.INTERIOR_AIRLOCK);
                interiorAirlockState.airlock = airlock;
                interiorAirlockState.building = airlockBuilding;
                Point2D interiorAirlockPosition = airlock.getAvailableInteriorPosition();
                interiorAirlockState.loc = interiorAirlockPosition;

                determineWalkingSteps(interiorAirlockState, destinationWalkState);
            }
            else {

                // Determine closest airlock to destination building.
                Airlock destinationAirlock = settlement.getClosestWalkableAvailableAirlock(destinationBuilding,
                        initialWalkState.loc);
                if (destinationAirlock != null) {

                    // Create walk step to exterior airlock position.
                    Point2D destinationAirlockExteriorPosition = destinationAirlock.getAvailableExteriorPosition();
                    createWalkExteriorStep(destinationAirlockExteriorPosition);

                    // Create exterior airlock walk state.
                    WalkState exteriorAirlockState = new WalkState(WalkState.EXTERIOR_AIRLOCK);
                    exteriorAirlockState.airlock = destinationAirlock;
                    exteriorAirlockState.loc = destinationAirlockExteriorPosition;

                    determineWalkingSteps(exteriorAirlockState, destinationWalkState);
                }
                else {

                    // Cannot walk to destination building.
                    canWalkAllSteps = false;

                    if (person != null) {
                    	logger.log(person, Level.WARNING, 10_000,
                    			"No walkable airlock from building airlock exterior to building interior in "
                        		+ person.getBuildingLocation().getNickName());
                    }
                    else if (robot != null) {
                    	logger.log(robot, Level.WARNING, 10_000,
                    			"No walkable airlock from building airlock exterior to building interior in "
                        		+ robot.getBuildingLocation().getNickName());
                    }

                    return;
                }
            }
        }
        else {

            Settlement settlement = destinationBuilding.getSettlement();

            // Determine closest airlock to destination building.
            Airlock destinationAirlock = settlement.getClosestWalkableAvailableAirlock(destinationBuilding,
                    initialWalkState.loc);
            if (destinationAirlock != null) {

                // Create walk step to exterior airlock position.
                Point2D destinationAirlockExteriorPosition = destinationAirlock.getAvailableExteriorPosition();
                createWalkExteriorStep(destinationAirlockExteriorPosition);

                // Create exterior airlock walk state.
                WalkState exteriorAirlockState = new WalkState(WalkState.EXTERIOR_AIRLOCK);
                exteriorAirlockState.airlock = destinationAirlock;
                exteriorAirlockState.loc = destinationAirlockExteriorPosition;

                determineWalkingSteps(exteriorAirlockState, destinationWalkState);
            }
            else {

                // Cannot walk to destination building.
                canWalkAllSteps = false;

                if (person != null)
                	logger.log(person, Level.WARNING, 10_000,
                			"No walkable airlock from rover airlock exterior to building interior.");
                else if (robot != null)
                	logger.log(robot, Level.WARNING, 10_000,
                			"No walkable airlock from rover airlock exterior to building interior.");

            }
        }
    }

    /**
     * Determine the walking steps between an airlock exterior and a rover interior location.
     *
     * @param initialWalkState the initial walk state.
     * @param destinationWalkState the destination walk state.
     */
    private void determineAirlockExteriorToRoverWalkingSteps(WalkState initialWalkState,
            WalkState destinationWalkState) {

        Airlock initialAirlock = initialWalkState.airlock;
        Rover destinationRover = destinationWalkState.rover;

        // Check if rover is in a garage or outside.
        Building garageBuilding = BuildingManager.getBuilding(destinationRover);
        if (garageBuilding != null) {

            Settlement settlement = garageBuilding.getSettlement();
            Airlock destinationAirlock = settlement.getClosestWalkableAvailableAirlock(garageBuilding,
                    initialWalkState.loc);
            if (destinationAirlock != null) {

                if (initialAirlock.equals(destinationAirlock)) {

                    // Create enter airlock walk step.
                    createEnterAirlockStep(initialAirlock);

                    // Create airlock interior state.
                    WalkState interiorAirlockState = new WalkState(WalkState.INTERIOR_AIRLOCK);
                    interiorAirlockState.airlock = initialAirlock;
                    interiorAirlockState.building = (Building) initialAirlock.getEntity();
                    Point2D interiorAirlockPosition = initialAirlock.getAvailableInteriorPosition();
                    interiorAirlockState.loc = interiorAirlockPosition;

                    determineWalkingSteps(interiorAirlockState, destinationWalkState);
                }
                else {

                    // Create walk step to exterior airlock position.
                    Point2D destinationAirlockExteriorPosition = destinationAirlock.getAvailableExteriorPosition();
                    createWalkExteriorStep(destinationAirlockExteriorPosition);

                    // Create exterior airlock walk state.
                    WalkState exteriorAirlockState = new WalkState(WalkState.EXTERIOR_AIRLOCK);
                    exteriorAirlockState.airlock = destinationAirlock;
                    exteriorAirlockState.loc = destinationAirlockExteriorPosition;

                    determineWalkingSteps(exteriorAirlockState, destinationWalkState);
                }
            }
            else {

                // Cannot walk to destination building.
                canWalkAllSteps = false;

                if (person != null) {
                	logger.log(person, Level.WARNING, 10_000,
                			"No walkable airlock from airlock exterior to rover in garage in "
                			+ person.getBuildingLocation().getNickName());
                }
                else {
                	logger.log(robot, Level.WARNING, 10_000,
                			"No walkable airlock from airlock exterior to rover in garage in "
                			+ robot.getBuildingLocation().getNickName());
                }

            }
        }
        else {

            Object airlockEntity = initialAirlock.getEntity();

            if (airlockEntity.equals(destinationRover)) {

                // Create enter airlock walk step.
                createEnterAirlockStep(initialAirlock);

                // Create airlock interior state.
                WalkState interiorAirlockState = new WalkState(WalkState.INTERIOR_AIRLOCK);
                interiorAirlockState.airlock = initialAirlock;
                interiorAirlockState.rover = destinationRover;
                Point2D interiorAirlockPosition = initialAirlock.getAvailableInteriorPosition();
                interiorAirlockState.loc = interiorAirlockPosition;

                determineWalkingSteps(interiorAirlockState, destinationWalkState);
            }
            else {

                Airlock destinationAirlock = destinationRover.getAirlock();

                // Create walk step to exterior airlock position.
                Point2D destinationAirlockExteriorPosition = destinationAirlock.getAvailableExteriorPosition();
                createWalkExteriorStep(destinationAirlockExteriorPosition);

                // Create exterior airlock walk state.
                WalkState exteriorAirlockState = new WalkState(WalkState.EXTERIOR_AIRLOCK);
                exteriorAirlockState.airlock = destinationAirlock;
                exteriorAirlockState.loc = destinationAirlockExteriorPosition;

                determineWalkingSteps(exteriorAirlockState, destinationWalkState);
            }
        }
    }

    /**
     * Determine the walking steps between an airlock exterior and an outside location.
     *
     * @param initialWalkState the initial walk state.
     * @param destinationWalkState the destination walk state.
     */
    private void determineAirlockExteriorToOutsideWalkingSteps(WalkState initialWalkState,
            WalkState destinationWalkState) {

        // Create walk step to exterior location.
        createWalkExteriorStep(destinationWalkState.loc);
    }

    /**
     * Determine the walking steps from an outside location.
     *
     * @param initialWalkState the initial walk state.
     * @param destinationWalkState the destination walk state.
     */
    private void determineOutsideWalkingSteps(WalkState initialWalkState, WalkState destinationWalkState) {

        switch(destinationWalkState.stateType) {

            case WalkState.BUILDING_LOC:    determineOutsideToBuildingInteriorWalkingSteps(
                    initialWalkState, destinationWalkState);
                                            break;
            case WalkState.ROVER_LOC:       determineOutsideToRoverWalkingSteps(
                    initialWalkState, destinationWalkState);
                                            break;
            case WalkState.OUTSIDE_LOC:     determineOutsideToOutsideWalkingSteps(
                    initialWalkState, destinationWalkState);
                                            break;
            default:                        throw new IllegalArgumentException("Invalid walk state type: " +
                    initialWalkState.stateType);
        }
    }

    /**
     * Determine the walking steps between an outside and a building interior location.
     *
     * @param initialWalkState the initial walk state.
     * @param destinationWalkState the destination walk state.
     */
    private void determineOutsideToBuildingInteriorWalkingSteps(WalkState initialWalkState,
            WalkState destinationWalkState) {

        Building destinationBuilding = destinationWalkState.building;
        Settlement settlement = destinationBuilding.getSettlement();

        // Determine closest airlock to destination building.
        Airlock destinationAirlock = settlement.getClosestWalkableAvailableAirlock(destinationBuilding,
                initialWalkState.loc);
        if (destinationAirlock != null) {

            // Create walk step to exterior airlock position.
            Point2D destinationAirlockExteriorPosition = destinationAirlock.getAvailableExteriorPosition();
            createWalkExteriorStep(destinationAirlockExteriorPosition);

            // Create exterior airlock walk state.
            WalkState exteriorAirlockState = new WalkState(WalkState.EXTERIOR_AIRLOCK);
            exteriorAirlockState.airlock = destinationAirlock;
            exteriorAirlockState.loc = destinationAirlockExteriorPosition;

            determineWalkingSteps(exteriorAirlockState, destinationWalkState);
        }
        else {

            // Cannot walk to destination building.
            canWalkAllSteps = false;
        	logger.log(person, Level.WARNING, 10_000,
        			"No walkable airlock from outside to building interior in "
            		 + destinationBuilding.getNickName());
        }
    }

    /**
     * Determine the walking steps between an outside and rover interior location.
     *
     * @param initialWalkState the initial walk state.
     * @param destinationWalkState the destination walk state.
     */
    private void determineOutsideToRoverWalkingSteps(WalkState initialWalkState,
            WalkState destinationWalkState) {

        Rover destinationRover = destinationWalkState.rover;

        // Check if rover is in a garage or outside.
        Building garageBuilding = BuildingManager.getBuilding(destinationRover);
        if (garageBuilding != null) {

            Settlement settlement = garageBuilding.getSettlement();
            Airlock destinationAirlock = settlement.getClosestWalkableAvailableAirlock(garageBuilding,
                    initialWalkState.loc);
            if (destinationAirlock != null) {

                // Create walk step to exterior airlock position.
                Point2D destinationAirlockExteriorPosition = destinationAirlock.getAvailableExteriorPosition();
                createWalkExteriorStep(destinationAirlockExteriorPosition);

                // Create exterior airlock walk state.
                WalkState exteriorAirlockState = new WalkState(WalkState.EXTERIOR_AIRLOCK);
                exteriorAirlockState.airlock = destinationAirlock;
                exteriorAirlockState.loc = destinationAirlockExteriorPosition;

                determineWalkingSteps(exteriorAirlockState, destinationWalkState);
            }
            else {

                // Cannot walk to destination building.
                canWalkAllSteps = false;

                if (person != null) {
                	logger.log(person, Level.WARNING, 10_000,
                			"No find walkable airlock from outside to rover in garage in "
                    		+ person.getBuildingLocation().getNickName());
                }
                else {
                	logger.log(robot, Level.WARNING, 10_000,
                			"No walkable airlock from outside to rover in garage in "
                    		+ robot.getBuildingLocation().getNickName());
                }
            }
        }
        else {

            Airlock destinationAirlock = destinationRover.getAirlock();

            // Create walk step to exterior airlock position.
            Point2D destinationAirlockExteriorPosition = destinationAirlock.getAvailableExteriorPosition();
            createWalkExteriorStep(destinationAirlockExteriorPosition);

            // Create exterior airlock walk state.
            WalkState exteriorAirlockState = new WalkState(WalkState.EXTERIOR_AIRLOCK);
            exteriorAirlockState.airlock = destinationAirlock;
            exteriorAirlockState.loc = destinationAirlockExteriorPosition;

            determineWalkingSteps(exteriorAirlockState, destinationWalkState);
        }
    }

    /**
     * Determine the walking steps between an outside and outside location.
     *
     * @param initialWalkState the initial walk state.
     * @param destinationWalkState the destinatino walk state.
     */
    private void determineOutsideToOutsideWalkingSteps(WalkState initialWalkState,
            WalkState destinationWalkState) {

        // Create walk step to exterior location.
        createWalkExteriorStep(destinationWalkState.loc);
    }

    /**
     * Determine the walking steps in climbing up and down the ladder of a multi-level building
     *
     * @param initialWalkState the initial walk state.
     * @param destinationWalkState the destination walk state.
     */
    private void determineLadderWalkingSteps(WalkState initialWalkState,
            WalkState destinationWalkState) {

        Building destinationBuilding = destinationWalkState.building;
//        Settlement settlement = destinationBuilding.getSettlement();

        if (destinationBuilding.isAHabOrHub()) {

//        	createClimbUpStep(destinationWalkState.xLoc, destinationWalkState.yLoc, destinationWalkState.zLoc,
//                    destinationBuilding);
//
//        	createClimbDownStep(destinationWalkState.xLoc, destinationWalkState.yLoc, destinationWalkState.zLoc,
//                    destinationBuilding);
        }

    }

    /**
     * Create a rover interior walking step.
     *
     * @param destLoc the destination position.
     * @param destinationRover the destination rover.
     */
    private void createWalkRoverInteriorStep(Point2D destLoc, Rover destinationRover) {

        WalkStep walkStep = new WalkStep(WalkStep.ROVER_INTERIOR_WALK);
        walkStep.loc = destLoc;
        walkStep.rover = destinationRover;
        walkingStepList.add(walkStep);
    }

    /**
     * Create a settlement interior walking step.
     *
     * @param destLoc the destination.
     * @param destinationBuilding the destination building.
     */
    private void createWalkSettlementInteriorStep(Point2D destLoc,
            Building destinationBuilding) {
       WalkStep walkStep = new WalkStep(WalkStep.SETTLEMENT_INTERIOR_WALK);
       walkStep.loc = destLoc;
       walkStep.building = destinationBuilding;
       walkingStepList.add(walkStep);
    }

    /**
     * Create a climb up step.
     *
     * @param destLoc the destination position
     * @param destZLoc the destination Z location.
     * @param destinationBuilding the destination building.
     */
    private void createClimbUpStep(Point2D destLoc, double destZLoc,
            Building destinationBuilding) {
       if (person != null) {
           WalkStep walkStep = new WalkStep(WalkStep.UP_LADDER);
           walkStep.loc = destLoc;
           walkStep.zLoc = destZLoc;
           walkStep.building = destinationBuilding;
           walkingStepList.add(walkStep);
        }
        else if (robot != null) {
        	throw new IllegalStateException("Robots can not climb up ladders");
        }
    }

    /**
     * Create a climb up step.
     *
     * @param destLoc the destination position.
     * @param destZLoc the destination Z location.
     * @param destinationBuilding the destination building.
     */
    private void createClimbDownStep(Point2D destLoc, double destZLoc,
            Building destinationBuilding) {
       if (person != null) {
           WalkStep walkStep = new WalkStep(WalkStep.DOWN_LADDER);
           walkStep.loc = destLoc;
           walkStep.zLoc = destZLoc;
           walkStep.building = destinationBuilding;
           walkingStepList.add(walkStep);
        }
        else if (robot != null) {
        	throw new IllegalStateException("Robots can not climb down ladders");
        }
    }

    /**
     * Create an exterior walking step.
     * @param destLoc the destination.
     */
    private void createWalkExteriorStep(Point2D destLoc) {

        WalkStep walkExterior = new WalkStep(WalkStep.EXTERIOR_WALK);
        walkExterior.loc = destLoc;
        walkingStepList.add(walkExterior);
    }

    /**
     * Create an exit airlock walking step.
     * @param airlock the airlock.
     */
    private void createExitAirlockStep(Airlock airlock) {

        WalkStep exitAirlockStep = new WalkStep(WalkStep.EXIT_AIRLOCK);
        exitAirlockStep.airlock = airlock;
        walkingStepList.add(exitAirlockStep);
    }

    /**
     * Create an enter airlock walking step.
     * @param airlock the airlock.
     */
    private void createEnterAirlockStep(Airlock airlock) {

        WalkStep enterAirlockStep = new WalkStep(WalkStep.ENTER_AIRLOCK);
        enterAirlockStep.airlock = airlock;
        walkingStepList.add(enterAirlockStep);
    }

    /**
     * Generate a string representation of the calculated route.
     * @return This will be a multi-line output.
     */
    public String generateRoute() {
    	var route = new StringBuilder();
		for(WalkStep step : walkingStepList) {
			route.append("Type=").append(step.stepType).append(",");
			if (step.loc != null) {
				route.append(" loc=").append(step.loc);
			}
			if (step.building != null) {
				route.append(" building=").append(step.building.getNickName()).append(',');
			}
			if (step.rover != null) {
				route.append(" rover=").append(step.rover.getNickName()).append(',');
			}
			if (step.airlock != null) {
				route.append(" airlock=").append(step.airlock.getEntityName()).append(',');
			}
			route.append("\n");
		}

    	return route.toString();
    }

    /**
     * Inner class for representing a walking state.
     */
    private class WalkState {

        // State types.
        private static final int BUILDING_LOC = 0;
        private static final int INTERIOR_AIRLOCK = 1;
        private static final int EXTERIOR_AIRLOCK = 2;
        private static final int ROVER_LOC = 3;
        private static final int OUTSIDE_LOC = 4;
        private static final int LADDER_LOC = 5;

        // Data members
        private int stateType;
        private Point2D loc;
        private double zLoc;

        private Building building;
        private Rover rover;
        private Airlock airlock;

        private WalkState(int stateType) {
            this.stateType = stateType;
        }
    }

    /**
     * Inner class for representing a walking step.
     */
    class WalkStep implements Serializable {

        /** default serial id. */
        private static final long serialVersionUID = 1L;

        // Step types.
        static final int SETTLEMENT_INTERIOR_WALK = 0;
        static final int ROVER_INTERIOR_WALK = 1;
        static final int EXTERIOR_WALK = 2;
        static final int EXIT_AIRLOCK = 3;
        static final int ENTER_AIRLOCK = 4;
        static final int ENTER_GARAGE_ROVER = 5;
        static final int EXIT_GARAGE_ROVER = 6;
        static final int UP_LADDER = 7;
        static final int DOWN_LADDER = 8;

        // Data members
        int stepType;
        Point2D loc;
        double zLoc;

        Building building;
        Rover rover;
        Airlock airlock;

        private WalkStep(int stepType) {
            this.stepType = stepType;
        }

    	public void destroy() {
            building = null;
            rover = null;
            airlock = null;
    	}
    }

    /**
     * Inner class for representing a walking state.
     */
    private class RobotWalkState {

        // State types.
        private static final int BUILDING_LOC = 0;

        // Data members
        private int stateType;
        private Point2D loc;
        private Building building;

        private RobotWalkState(int stateType) {
            this.stateType = stateType;
        }
    }

	public void destroy() {
		walkingStepList = null;
		person = null;
		robot = null;
	}
}
