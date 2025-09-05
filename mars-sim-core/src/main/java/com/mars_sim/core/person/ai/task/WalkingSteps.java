/*
 * Mars Simulation Project
 * WalkingSteps.java
 * @date 2024-07-10
 * @author Scott Davis
 */

package com.mars_sim.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.mars_sim.core.LocalAreaUtil;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.LocalBoundedObject;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.Airlock;
import com.mars_sim.core.structure.AirlockType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.Vehicle;

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
	 * 
	 * @param person
	 * @param pos
	 * @param interiorObject
	 */
	public WalkingSteps(Person person, LocalPosition pos, LocalBoundedObject interiorObject) {
        this.person = person;

        // Initialize data members.
        canWalkAllSteps = true;

        walkingStepList = new ArrayList<>();

        // Determine initial walk state.
        WalkState initialWalkState = determineInitialWalkState(person);

        // Determine destination walk state.
        // NOTE: will incorporate zLoc
        WalkState destinationWalkState = determineDestinationWalkState(pos, interiorObject);

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
	 * 
	 * @param robot
	 * @param pos
	 * @param zLoc
	 * @param interiorObject
	 */
	public WalkingSteps(Robot robot, LocalPosition pos, LocalBoundedObject interiorObject) {
        this.robot = robot;

        // Initialize data members.
        canWalkAllSteps = true;

        walkingStepList = new ArrayList<>();

        // Determine initial walk state.
        RobotWalkState initialWalkState = determineInitialRobotWalkState(robot);

        // Determine destination walk state.
        // NOTE: will incorporate zLoc
        RobotWalkState destinationWalkState = determineDestinationRobotWalkState(pos, interiorObject);

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
            	logger.warning(person,  4000,
                        "Inside the settlement but not in a building.");
            	return null;
            }

            result = new WalkState(WalkState.BUILDING_LOC);
            result.building = building;

            logger.log(person, Level.FINER, 4000, "Inside " + building + ".");
        }

        else if (person.isInVehicle()) {

            Vehicle vehicle = person.getVehicle();

            if (vehicle instanceof Rover r) {
                result = new WalkState(WalkState.ROVER_LOC);
                result.rover = r;

                logger.log(person, Level.FINER, 4000,
                		"Inside " + r + ".");
            }

            else {
                result = new WalkState(WalkState.OUTSIDE_LOC);

                logger.log(person, Level.FINER, 4000,
                		 "Outside.");
            }
        }

        else {

        	logger.severe(person, 4000, "Can not identify parent container.");
        }

        // Set person X and Y location.
        if (result != null) {
            result.loc = person.getPosition();
        }

        return result;
    }

    /**
     * Determines the robot initial walk state.
     *
     * @param robot the robot walking.
     * @return the initial location state.
     */
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

            logger.log(robot, Level.FINER, 4000, "Inside " + building + ".");
            
//            if (!LocalAreaUtil.isPositionWithinLocalBoundedObject(robot.getPosition(), building)) {
//            	logger.severe(robot, 5000,
//            			"Invalid Robot start location at " + robot.getPosition()
//                        + ", not within " + building + " @ "
//                        + LocalAreaUtil.getDescription(building));
//            }
        }

        else {
        	logger.severe(robot, 5000, "Invalid location situation for walking task.");
        }

        // Set robot X and Y location.
        if (result != null) {
            result.loc = robot.getPosition();
        }

        return result;
    }

    /**
     * Determines the destination walk state.
     *
     * @param pos the destination location.
     * @param interiorObject the destination interior object (inhabitable building or rover).
     * @return destination walk state.
     */
    private WalkState determineDestinationWalkState(LocalPosition pos,
            LocalBoundedObject interiorObject) {

        WalkState result = null;

        if (interiorObject instanceof Building building) {
            result = new WalkState(WalkState.BUILDING_LOC);
            result.building = building;

            if (!LocalAreaUtil.isPositionWithinLocalBoundedObject(pos, building)) {
                Worker walker = person;
                if (walker == null) {
                    walker = robot;
                }
            	logger.severe(walker, 60_000,
            				"Invalid destination at " +
            				pos + ". Not within building " + building + " at "
                            + LocalAreaUtil.getDescription(building));
            }
        }
        
        else if (interiorObject instanceof Rover rover) {

        	if (person != null) {
	            result = new WalkState(WalkState.ROVER_LOC);
	            result.rover = rover;

	            if (!LocalAreaUtil.isPositionWithinLocalBoundedObject(pos, rover)) {
	            	logger.severe(person, 5000,
	            			"Invalid destination at " +
	                        pos + ". Not within rover " + rover + " at "
                            + LocalAreaUtil.getDescription(rover));
	            }
        	}
        }

        else {
        	if (person != null) {
        		result = new WalkState(WalkState.OUTSIDE_LOC);
        	}
        }
        
        if (result != null) {
        	result.loc = pos;
        }
        
        return result;
    }

    /**
     * Determines the destination walk state.
     *
     * @param pos the destination position
     * @param interiorObject the destination interior object (inhabitable building or rover).
     * @return destination walk state.
     */
    private RobotWalkState determineDestinationRobotWalkState(LocalPosition pos,
            LocalBoundedObject interiorObject) {

    	RobotWalkState result = null;

        if (interiorObject instanceof Building building) {
            result = new RobotWalkState(RobotWalkState.BUILDING_LOC);
            result.building = building;

            if (!LocalAreaUtil.isPositionWithinLocalBoundedObject(pos, building)) {
        			logger.log(robot, Level.SEVERE, 5000,
        					"Invalid robot destination at " +
                            pos + ". Not within " + building
                            + " at " + LocalAreaUtil.getDescription(building));
            }

            result.loc = pos;
        }
        return result;
    }

    /**
     * Determines the walk steps from an initial walk state to a destination walk state.
     * 
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
     * Determines the walk steps from an initial walk state to a destination walk state.
     * 
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

            case RobotWalkState.BUILDING_LOC:    
            	determineBuildingInteriorWalkingSteps(initialWalkState, destinationWalkState);
            	break;
            default:
            	throw new IllegalArgumentException("Invalid walk state type: " +
                    initialWalkState.stateType);
        }

        return;
    }


    /**
     * Determines the walking steps in a building interior.
     * 
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
     * Determines the walking steps in a building interior.
     * 
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
     * Determines the walking steps from a building interior to another building interior.
     * 
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

        	// Find an airlock that's least full and an airlock that still has space for egress
            Airlock airlock = settlement.getBestWalkableAvailableAirlock(initialBuilding,
                    destinationWalkState.loc, false);
            if (airlock == null) {
                canWalkAllSteps = false;
                if (person != null) {
        			logger.warning(person, 10_000,
        					"No walkable airlock from building interior to building interior in "
        					+ person.getBuildingLocation().getName() + ".");
                }
                else {
                	logger.warning(robot, 10_000,
                			"No walkable airlock from building interior to building interior in "
                			+ robot.getBuildingLocation().getName() + ".");
                }
                return;
            }

            Building airlockBuilding = (Building) airlock.getEntity();
            LocalPosition interiorAirlockPosition = airlock.getAvailableInteriorPosition();

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
     * Determines the walking steps from a building interior to another building interior.
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
     * Determines the walking steps from a building interior to a rover interior.
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
        Building garageBuilding = destinationRover.getGarage();
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
        	// Find an airlock that's least full for egress to destination rover.
            Airlock airlock = settlement.getBestWalkableAvailableAirlock(initialBuilding,
                    destinationWalkState.loc, false);
            if (airlock == null) {
                canWalkAllSteps = false;
                if (person != null) {
                	logger.warning(person, 10_000,
                		"No walkable airlock from building interior to rover interior in "
                		+ person.getBuildingLocation().getName());
                }
                else {
                	logger.warning(robot, 10_000,
                		"No walkable airlock from building interior to rover interior in "
                    	+ robot.getBuildingLocation().getName());
                }
               return;
            }

            Building airlockBuilding = (Building) airlock.getEntity();
            LocalPosition interiorAirlockPosition = airlock.getAvailableInteriorPosition();

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
     * Determines the walking steps between a building interior and outside.
     *
     * @param initialWalkState the initial walk state.
     * @param destinationWalkState the destination walk state.
     */
    private void determineBuildingInteriorToOutsideWalkingSteps(WalkState initialWalkState,
            WalkState destinationWalkState) {

        Building initialBuilding = initialWalkState.building;
        Settlement settlement = initialBuilding.getSettlement();

        // Find an airlock that's least full for egress to destination.
        Airlock airlock = settlement.getBestWalkableAvailableAirlock(initialBuilding,
                destinationWalkState.loc, false);
        if (airlock == null) {
            canWalkAllSteps = false;
            if (person != null) {
            	logger.warning(person, 10_000,
            		"No walkable airlock from "
            		+ person.getBuildingLocation().getName() 
            		+ " to outside.");
            }
            else {
            	logger.warning(robot, 10_000,
            		"No walkable airlock from "
                    + robot.getBuildingLocation().getName() 
                    + " to outside.") ;
            }
           return;
        }

        Building airlockBuilding = (Building) airlock.getEntity();
        LocalPosition interiorAirlockPosition = airlock.getAvailableInteriorPosition();

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
     * Determines the walking steps between two rover interior locations.
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
     * Determines the walking steps between a rover interior and a building interior.
     *
     * @param initialWalkState the initial walk state.
     * @param destinationWalkState the destination walk state.
     */
    private void determineRoverToBuildingInteriorWalkingSteps(WalkState initialWalkState,
            WalkState destinationWalkState) {

        Rover initialRover = initialWalkState.rover;

        // Check if rover is parked in garage or outside.
        Building garageBuilding = initialRover.getGarage();
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
            LocalPosition interiorAirlockPosition = airlock.getAvailableInteriorPosition();

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
     * Determines the walking steps between a rover interior and a rover interior.
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
            Building garageBuilding = initialRover.getGarage();
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
                LocalPosition interiorAirlockPosition = airlock.getAvailableInteriorPosition();

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
     * Determines the walking steps between a rover interior and outside location.
     *
     * @param initialWalkState the initial walk state.
     * @param destinationWalkState the destination walk state.
     */
    private void determineRoverToOutsideWalkingSteps(WalkState initialWalkState,
            WalkState destinationWalkState) {

        Rover initialRover = initialWalkState.rover;

        // Check if rover is parked in garage or outside.
        Building garageBuilding = initialRover.getGarage();
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
            LocalPosition interiorAirlockPosition = airlock.getAvailableInteriorPosition();

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
     * Determines the walking steps from an airlock interior location.
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
     * Determines the walking steps between an airlock interior and a building interior location.
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
                LocalPosition exteriorAirlockPosition = airlock.getAvailableExteriorPosition();
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
            LocalPosition exteriorAirlockPosition = airlock.getAvailableExteriorPosition();
            exteriorAirlockState.loc = exteriorAirlockPosition;

            determineWalkingSteps(exteriorAirlockState, destinationWalkState);
        }
    }

    /**
     * Determines the walking steps between an airlock interior and rover interior location.
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
            Building garageBuilding = destinationRover.getGarage();;
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
                    LocalPosition exteriorAirlockPosition = airlock.getAvailableExteriorPosition();
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
                LocalPosition exteriorAirlockPosition = airlock.getAvailableExteriorPosition();
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
                LocalPosition exteriorAirlockPosition = airlock.getAvailableExteriorPosition();
                exteriorAirlockState.loc = exteriorAirlockPosition;

                determineWalkingSteps(exteriorAirlockState, destinationWalkState);
            }
        }
    }

    /**
     * Determines the walking steps between an airlock interior and an outside location.
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
        LocalPosition exteriorAirlockPosition = airlock.getAvailableExteriorPosition();
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
     * Determines the walking steps between an airlock exterior and building interior location.
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
            
            if (settlement.getBuildingConnectorManager()
            		.hasValidPath(airlockBuilding, destinationBuilding)) {

                // Create enter airlock walk step.
                createEnterAirlockStep(airlock);

                // Create airlock interior state.
                WalkState interiorAirlockState = new WalkState(WalkState.INTERIOR_AIRLOCK);
                interiorAirlockState.airlock = airlock;
                interiorAirlockState.building = airlockBuilding;
                LocalPosition interiorAirlockPosition = airlock.getAvailableInteriorPosition();
                interiorAirlockState.loc = interiorAirlockPosition;

                determineWalkingSteps(interiorAirlockState, destinationWalkState);
            }
            else {

                // Find an airlock that's least full for egress to destination building.
                Airlock destinationAirlock = settlement.getBestWalkableAvailableAirlock(destinationBuilding,
                        initialWalkState.loc, true);

                if (destinationAirlock != null) {

                    // Create walk step to exterior airlock position.
                    LocalPosition destinationAirlockExteriorPosition = destinationAirlock.getAvailableExteriorPosition();
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
                    	logger.warning(person,  10_000,
                    			"No walkable airlock from airlock exterior to building interior in "
                        		+ person.getBuildingLocation().getName());
                    }
                    else if (robot != null) {
                    	logger.warning(robot, 10_000,
                    			"No walkable airlock from airlock exterior to building interior in "
                        		+ robot.getBuildingLocation().getName());
                    }

                    return;
                }
            }
        }
        else {

            Settlement settlement = destinationBuilding.getSettlement();

            // Find an airlock that's least full to destination building.
            Airlock destinationAirlock = settlement.getBestWalkableAvailableAirlock(destinationBuilding,
                    initialWalkState.loc, false);
            if (destinationAirlock != null) {

                // Create walk step to exterior airlock position.
                LocalPosition destinationAirlockExteriorPosition = destinationAirlock.getAvailableExteriorPosition();
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
                	logger.warning(person,  10_000,
                			"No walkable airlock from rover airlock exterior to building interior.");
                else if (robot != null)
                	logger.warning(robot, 10_000,
                			"No walkable airlock from rover airlock exterior to building interior.");

            }
        }
    }

    /**
     * Determines the walking steps between an airlock exterior and a rover interior location.
     *
     * @param initialWalkState the initial walk state.
     * @param destinationWalkState the destination walk state.
     */
    private void determineAirlockExteriorToRoverWalkingSteps(WalkState initialWalkState,
            WalkState destinationWalkState) {

        Airlock initialAirlock = initialWalkState.airlock;
        Rover destinationRover = destinationWalkState.rover;

        // Check if rover is in a garage or outside.
        Building garageBuilding = destinationRover.getGarage();
        if (garageBuilding != null) {

            Settlement settlement = garageBuilding.getSettlement();
            // Find an airlock that's least full 
            Airlock destinationAirlock = settlement.getBestWalkableAvailableAirlock(garageBuilding,
                    initialWalkState.loc, false);
            if (destinationAirlock != null) {

                if (initialAirlock.equals(destinationAirlock)) {

                    // Create enter airlock walk step.
                    createEnterAirlockStep(initialAirlock);

                    // Create airlock interior state.
                    WalkState interiorAirlockState = new WalkState(WalkState.INTERIOR_AIRLOCK);
                    interiorAirlockState.airlock = initialAirlock;
                    interiorAirlockState.building = (Building) initialAirlock.getEntity();
                    LocalPosition interiorAirlockPosition = initialAirlock.getAvailableInteriorPosition();
                    interiorAirlockState.loc = interiorAirlockPosition;

                    determineWalkingSteps(interiorAirlockState, destinationWalkState);
                }
                else {

                    // Create walk step to exterior airlock position.
                    LocalPosition destinationAirlockExteriorPosition = destinationAirlock.getAvailableExteriorPosition();
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
                	logger.warning(person,  10_000,
                			"No walkable airlock from airlock exterior to rover in garage in "
                			+ garageBuilding.getName());
                }
                else {
                	logger.warning(robot, 10_000,
                			"No walkable airlock from airlock exterior to rover in garage in "
                			+ garageBuilding.getName());
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
                LocalPosition interiorAirlockPosition = initialAirlock.getAvailableInteriorPosition();
                interiorAirlockState.loc = interiorAirlockPosition;

                determineWalkingSteps(interiorAirlockState, destinationWalkState);
            }
            else {

                Airlock destinationAirlock = destinationRover.getAirlock();

                // Create walk step to exterior airlock position.
                LocalPosition destinationAirlockExteriorPosition = destinationAirlock.getAvailableExteriorPosition();
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
     * Determines the walking steps from an outside location.
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
     * Determines the walking steps between an outside and a building interior location.
     *
     * @param initialWalkState the initial walk state.
     * @param destinationWalkState the destination walk state.
     */
    private void determineOutsideToBuildingInteriorWalkingSteps(WalkState initialWalkState,
            WalkState destinationWalkState) {

        Building destinationBuilding = destinationWalkState.building;
        Settlement settlement = destinationBuilding.getSettlement();

        // Find an airlock that's least full to destination building.
        Airlock destinationAirlock = settlement.getBestWalkableAvailableAirlock(destinationBuilding,
                initialWalkState.loc, true);
        if (destinationAirlock != null) {

            // Create walk step to exterior airlock position.
            LocalPosition destinationAirlockExteriorPosition = destinationAirlock.getAvailableExteriorPosition();
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
//        	logger.warning(person,  10_000,
//        			"No walkable airlock from outside to building interior in "
//            		 + destinationBuilding.getName());
        }
    }

    /**
     * Determines the walking steps between an outside and rover interior location.
     *
     * @param initialWalkState the initial walk state.
     * @param destinationWalkState the destination walk state.
     */
    private void determineOutsideToRoverWalkingSteps(WalkState initialWalkState,
            WalkState destinationWalkState) {

        Rover destinationRover = destinationWalkState.rover;
        // Check if rover is in a garage or outside.
        Building garageBuilding = destinationRover.getGarage();
        
        if (garageBuilding != null) {

            Settlement settlement = garageBuilding.getSettlement();
            // Find an airlock that's least full for ingress to destination rover. 		
            Airlock destinationAirlock = settlement.getBestWalkableAvailableAirlock(garageBuilding,
                    initialWalkState.loc, true);
            if (destinationAirlock != null) {

                // Create walk step to exterior airlock position.
                LocalPosition destinationAirlockExteriorPosition = destinationAirlock.getAvailableExteriorPosition();
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
                	logger.warning(person,  10_000,
                			"No find walkable airlock from outside to rover in garage in "
                    		+ garageBuilding.getName());
                }
                else {
                	logger.warning(robot, 10_000,
                			"No walkable airlock from outside to rover in garage in "
                    		+ garageBuilding.getName());
                }
            }
        }
        else {

            Airlock destinationAirlock = destinationRover.getAirlock();

            // Create walk step to exterior airlock position.
            LocalPosition destinationAirlockExteriorPosition = destinationAirlock.getAvailableExteriorPosition();
            createWalkExteriorStep(destinationAirlockExteriorPosition);

            // Create exterior airlock walk state.
            WalkState exteriorAirlockState = new WalkState(WalkState.EXTERIOR_AIRLOCK);
            exteriorAirlockState.airlock = destinationAirlock;
            exteriorAirlockState.loc = destinationAirlockExteriorPosition;

            determineWalkingSteps(exteriorAirlockState, destinationWalkState);
        }
    }

    /**
     * Determines the walking steps between an outside and outside location.
     *
     * @param initialWalkState the initial walk state.
     * @param destinationWalkState the destination walk state.
     */
    private void determineOutsideToOutsideWalkingSteps(WalkState initialWalkState,
            WalkState destinationWalkState) {

        // Create walk step to exterior location.
        createWalkExteriorStep(destinationWalkState.loc);
    }

    /**
     * Determines the walking steps in climbing up and down the ladder of a multi-level building.
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
     * Creates a rover interior walking step.
     *
     * @param destLoc the destination position.
     * @param destinationRover the destination rover.
     */
    private void createWalkRoverInteriorStep(LocalPosition destLoc, Rover destinationRover) {

        WalkStep walkStep = new WalkStep(WalkStep.ROVER_INTERIOR_WALK);
        walkStep.loc = destLoc;
        walkStep.rover = destinationRover;
        walkingStepList.add(walkStep);
    }

    /**
     * Creates a settlement interior walking step.
     *
     * @param destLoc the destination.
     * @param destinationBuilding the destination building.
     */
    private void createWalkSettlementInteriorStep(LocalPosition destLoc,
            Building destinationBuilding) {
       WalkStep walkStep = new WalkStep(WalkStep.SETTLEMENT_INTERIOR_WALK);
       walkStep.loc = destLoc;
       walkStep.building = destinationBuilding;
       walkingStepList.add(walkStep);
    }

    /**
     * Creates a climb up step.
     *
     * @param destLoc the destination position
     * @param destZLoc the destination Z location.
     * @param destinationBuilding the destination building.
     */
    private void createClimbUpStep(LocalPosition destLoc, double destZLoc,
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
     * Creates a climb up step.
     *
     * @param destLoc the destination position.
     * @param destZLoc the destination Z location.
     * @param destinationBuilding the destination building.
     */
    private void createClimbDownStep(LocalPosition destLoc, double destZLoc,
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
     * Creates an exterior walking step.
     * 
     * @param destLoc the destination.
     */
    private void createWalkExteriorStep(LocalPosition destLoc) {

        WalkStep walkExterior = new WalkStep(WalkStep.EXTERIOR_WALK);
        walkExterior.loc = destLoc;
        walkingStepList.add(walkExterior);
    }

    /**
     * Creates an exit airlock walking step.
     * 
     * @param airlock the airlock.
     */
    private void createExitAirlockStep(Airlock airlock) {

        WalkStep exitAirlockStep = new WalkStep(WalkStep.EXIT_AIRLOCK);
        exitAirlockStep.airlock = airlock;
        walkingStepList.add(exitAirlockStep);
    }

    /**
     * Creates an enter airlock walking step.
     * 
     * @param airlock the airlock.
     */
    private void createEnterAirlockStep(Airlock airlock) {

        WalkStep enterAirlockStep = new WalkStep(WalkStep.ENTER_AIRLOCK);
        enterAirlockStep.airlock = airlock;
        walkingStepList.add(enterAirlockStep);
    }

    /**
     * Generates a string representation of the calculated route.
     * 
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
				route.append(" building=").append(step.building.getName()).append(',');
			}
			if (step.rover != null) {
				route.append(" rover=").append(step.rover.getName()).append(',');
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
        private LocalPosition loc;
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
        LocalPosition loc;
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
        private LocalPosition loc;
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
