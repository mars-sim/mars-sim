/**
 * Mars Simulation Project
 * WalkingSteps.java
 * @version 3.1.0 2017-01-21
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LocalBoundedObject;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Airlock;
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

	private static Logger logger = Logger.getLogger(WalkingSteps.class.getName());

	private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1, 
			logger.getName().length());

	// Data members.
	private List<WalkStep> walkingSteps;
	private List<RobotWalkStep> robotWalkingSteps;
	private boolean canWalkAllSteps;

	private Person person;
	private Robot robot;

	/**
	 * constructor 1.
	 */
	public WalkingSteps(Person person, double xLoc, double yLoc, double zLoc, LocalBoundedObject interiorObject) {
        this.person = person;

        // Initialize data members.
        canWalkAllSteps = true;
        walkingSteps = new ArrayList<WalkStep>();
        
        // Determine initial walk state.
        WalkState initialWalkState = determineInitialWalkState(person);
        if (initialWalkState == null)  {
        	logger.severe("initialWalkState : " + initialWalkState);
        	return;
        }
        // Determine destination walk state.
        WalkState destinationWalkState = determineDestinationWalkState(xLoc, yLoc, interiorObject);
        if (destinationWalkState == null) {
        	logger.severe("destinationWalkState : " + destinationWalkState);
        	return;
        }
        // Determine walking steps to destination.
        determineWalkingSteps(initialWalkState, destinationWalkState);
    }

	/**
	 * constructor 2.
	 */
	public WalkingSteps(Robot robot, double xLoc, double yLoc, double zLoc, LocalBoundedObject interiorObject) {
        this.robot = robot;

        // Initialize data members.
        canWalkAllSteps = true;
        robotWalkingSteps = new ArrayList<RobotWalkStep>();

        // Determine initial walk state.
        RobotWalkState initialWalkState = determineInitialRobotWalkState(robot);
//        if (initialWalkState == null)
//        	System.out.println("initialWalkState == null");

        // Determine destination walk state.
        RobotWalkState destinationWalkState = determineDestinationRobotWalkState(xLoc, yLoc, interiorObject);
//        if (destinationWalkState == null)
//        	System.out.println("destinationWalkState == null");
        
        // Determine walking steps to destination.
        determineWalkingSteps(initialWalkState, destinationWalkState);
    }

    /**
     * Gets a list of walking steps to the destination.
     * @return list of walk steps.  Returns empty list if a valid path isn't found.
     */
    public List<WalkStep> getWalkingStepsList() {
        return walkingSteps;
    }

    /**
     * Gets a list of robot walking steps to the destination.
     * @return list of robot walk steps.  Returns empty list if a valid path isn't found.
     */
    public List<RobotWalkStep> getRobotWalkingStepsList() {
        return robotWalkingSteps;
    }

    
    /**
     * Gets the number of walking steps to the destination.
     * @return number of walking steps.  Returns 0 if a valid path isn't found.
     */
    public int getWalkingStepsNumber() {
        int result = 0;

        if (walkingSteps != null) {
            result = walkingSteps.size();
        }

        return result;
    }

    /**
     * Gets the number of robot walking steps to the destination.
     * @return number of robot walking steps.  Returns 0 if a valid path isn't found.
     */
    public int getRobotWalkingStepsNumber() {
        int result = 0;

        if (robotWalkingSteps != null) {
            result = robotWalkingSteps.size();
        }

        return result;
    }
    
    /**
     * Checks if a valid path has been found to the destination.
     * @return true if valid path to destination found.
     */
    public boolean canWalkAllSteps() {
        return canWalkAllSteps;
    }

    /**
     * Determines the person's initial walk state.
     * @param person the person walking.
     * @return the initial location state.
     */
    private WalkState determineInitialWalkState(Person person) {

        WalkState result = null;

        // Determine initial walk state based on person's location situation.
        if (person.isOutside()) {

            result = new WalkState(WalkState.OUTSIDE_LOC);
            
			LogConsolidated.log(Level.FINER, 0, sourceName,
					"[" 
					+ person.getLocationTag().getLocale()
//					+ person.getLocationStateType().getName() 
					+ "] "  + person.getName() +
                    " is having WalkState.OUTSIDE_LOC");
        }
        else if (person.isInSettlement()) {

            Building building = person.getBuildingLocation();//BuildingManager.getBuilding(person);
            
            if (building == null) {
    			LogConsolidated.log(Level.WARNING, 0, sourceName,
    					"[" 
    					+ person.getLocationTag().getLocale()
//    					+ person.getLocationStateType().getName() 
    					+ "] " + person.getName() +
                        " is inside the settlement but isn't in a building");
            	return null;
            }

            result = new WalkState(WalkState.BUILDING_LOC);
            result.building = building;
            
			LogConsolidated.log(Level.FINER, 0, sourceName,
					"[" 
					+ person.getLocationTag().getLocale()
					+ "] " 
					+ person.getName()
                    + " (" + person.getLocationStateType().getName() + ")"
                    + " in " + building
					+ " (WalkState : BUILDING_LOC)."
					);

//			// TODO: why is checkLocationWithinLocalBoundedObject() troublesome ?
//            if (!LocalAreaUtil.checkLocationWithinLocalBoundedObject(person.getXLocation(),
//                    person.getYLocation(), building)) {
//
//    			LogConsolidated.log(Level.WARNING, 0, sourceName, 		
//// 				throw new IllegalStateException(
//    					"[" 
//    					+ person.getLocationTag().getLocale() 
//    					+ "] " 
//    					+ person.getName() 
//    					+ " (" + person.getLocationStateType().getName() + ")"
//    					+ " has invalid walk start loc @ (" +
//                        Math.round(person.getXLocation()*10.0)/10.0 + ", " 
// 						+ Math.round(person.getYLocation()*10.0)/10.0 
// 						+ "). Should have been within " + building + " in " + person.getSettlement());
// 				
//            	return null;
//            }
        }
        else if (person.isInVehicle()) {

            Vehicle vehicle = person.getVehicle();

            if (vehicle instanceof Rover) {
                result = new WalkState(WalkState.ROVER_LOC);
                result.rover = (Rover) vehicle;
                
    			LogConsolidated.log(Level.FINER, 0, sourceName,
    					"[" 
    					+ person.getLocationTag().getLocale()
    					+ "] " 
    					+ person.getName()
                        + " (" + person.getLocationStateType().getName() + ")"
                        + " in " + vehicle
    					+ " (WalkState : ROVER_LOC)."
    					);
    			
//                if (!LocalAreaUtil.checkLocationWithinLocalBoundedObject(person.getXLocation(),
//                        person.getYLocation(), vehicle)) {
//                	
//        			LogConsolidated.log(Level.SEVERE, 5_000, sourceName,
////        			throw new IllegalStateException(		
//        					"[" 
//        					+ person.getLocationTag().getLocale()
////        					+ person.getLocationStateType().getName() 
//        					+ "] " 
//        				+ person.getName() + " was supposed to be inside " + vehicle 
//        				+ " but had an invalid location at (" 
//        				+ Math.round(person.getXLocation()*10.0)/10.0 + ", " 
//        				+ Math.round(person.getYLocation()*10.0)/10.0 + ").");
//        			return null;
//                }
            }
            else {
                result = new WalkState(WalkState.OUTSIDE_LOC);
  
    			LogConsolidated.log(Level.FINER, 0, sourceName,
    					"[" 
    					+ person.getLocationTag().getLocale()
    					+ "] " 
    					+ person.getName()
                        + " (" + person.getLocationStateType().getName() + ")"
    					+ " (WalkState : OUTSIDE_LOC)."
    					);
            }
        }
        
        else {
        	
			LogConsolidated.log(Level.WARNING, 0, sourceName,
					"[" 
					+ person.getLocationTag().getLocale()
					+ "] " 
					+ person.getName()
                    + " (" + person.getLocationStateType().getName() + ")"
					+ " (invalid WalkState)."
					);
			
            //throw new IllegalStateException(person.getName() +
            //        " is in an invalid location situation for walking task: " + locationSituation);
        }

        // Set person X and Y location.
        if (result != null) {
            result.xLoc = person.getXLocation();
            result.yLoc = person.getYLocation();
        }

        return result;
    }

   private RobotWalkState determineInitialRobotWalkState(Robot robot) {

	   RobotWalkState result = null;

        // Determine initial walk state based on robot's location situation.
        if (robot.isInSettlement()) {

            Building building = robot.getBuildingLocation();//BuildingManager.getBuilding(robot);
            if (building == null) {
                return null;
            }

            result = new RobotWalkState(RobotWalkState.BUILDING_LOC);
            result.building = building;

            if (!LocalAreaUtil.checkLocationWithinLocalBoundedObject(robot.getXLocation(),
                    robot.getYLocation(), building)) {
            	LogConsolidated.log(Level.SEVERE, 5000, sourceName,
            			"[" + robot.getSettlement() + "] " + robot.getName() + " has invalid walk start location. (" +
                        robot.getXLocation() + ", " + robot.getYLocation() + ") is not within building " + building);
                //throw new IllegalStateException(robot.getName() + " has invalid walk start location. (" +
                //    robot.getXLocation() + ", " + robot.getYLocation() + ") is not within building " + building);
            }
        }

//        else if (robot.isInVehicle()) {
//
//            Vehicle vehicle = robot.getVehicle();
//
//            if (vehicle instanceof Rover) {
//                result = new RobotWalkState(RobotWalkState.ROVER_LOC);
//                result.rover = (Rover) vehicle;
//
//                if (!LocalAreaUtil.checkLocationWithinLocalBoundedObject(robot.getXLocation(),
//                        robot.getYLocation(), vehicle)) {
//                    throw new IllegalStateException(robot.getName() + " has invalid walk start location. (" +
//                        robot.getXLocation() + ", " + robot.getYLocation() + ") is not within vehicle " + vehicle);
//                }
//            }
//            else {
//                result = new RobotWalkState(RobotWalkState.OUTSIDE_LOC);
//            }
//        }
//        else if (robot.isOutside()) {
//
//            result = new RobotWalkState(RobotWalkState.OUTSIDE_LOC);
//        }

        else {
        	LogConsolidated.log(Level.SEVERE, 5000, sourceName,
        			"[" + robot.getLocationStateType().getName() + "] " + robot.getName() +
                    " is in an invalid location situation for walking task.");
            //throw new IllegalStateException(robot.getName() +
            //        " is in an invalid location situation for walking task: " + locationSituation);
        }

        // Set robot X and Y location.
        if (result != null) {
            result.xLoc = robot.getXLocation();
            result.yLoc = robot.getYLocation();
        }

        return result;
    }
   
    /**
     * Determines the destination walk state.
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

            if (!LocalAreaUtil.checkLocationWithinLocalBoundedObject(xLoc, yLoc, building)) {
            	if (person != null)
            		LogConsolidated.log(Level.SEVERE, 0, sourceName,
            			"[" + person.getSettlement() + "] " + person +		
    					" has an invalid walk destination location. (" +
                        xLoc + ", " + yLoc + ") is not within building " + building);
            	else if (robot != null)
        			LogConsolidated.log(Level.SEVERE, 0, sourceName,
                			"[" + robot.getSettlement() + "] " + robot +		
        					" has an invalid walk destination location. (" +
                            xLoc + ", " + yLoc + ") is not within building " + building);
                //throw new IllegalStateException("Invalid walk destination location. (" +
                //    xLoc + ", " + yLoc + ") is not within building " + building);
            }
        }
        else if (interiorObject instanceof Rover) {
        	
        	if (person != null) {
	            Rover rover = (Rover) interiorObject;
	            result = new WalkState(WalkState.ROVER_LOC);
	            result.rover = rover;
	
	            if (!LocalAreaUtil.checkLocationWithinLocalBoundedObject(xLoc, yLoc, rover)) {
	            	if (person != null)
	            		LogConsolidated.log(Level.SEVERE, 5000, sourceName,
	            			"[" + person.getSettlement() + "] " + person +		
	    					" has an invalid walk destination location. (" +
	                        xLoc + ", " + yLoc + ") is not within rover " + rover);
	            	else if (robot != null)
	        			LogConsolidated.log(Level.SEVERE, 5000, sourceName,
	                			"[" + robot.getSettlement() + "] " + robot +		
	        					" has an invalid walk destination location. (" +
	                            xLoc + ", " + yLoc + ") is not within rover " + rover);
	                //throw new IllegalStateException("Invalid walk destination location. (" +
	                //    xLoc + ", " + yLoc + ") is not within rover " + rover);
	            }
        	}
        }
        else {
        	if (person != null) { 
        		result = new WalkState(WalkState.OUTSIDE_LOC);
        	}
        }

        result.xLoc = xLoc;
        result.yLoc = yLoc;

        return result;
    }

    /**
     * Determines the destination walk state.
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

            if (!LocalAreaUtil.checkLocationWithinLocalBoundedObject(xLoc, yLoc, building)) {
        			LogConsolidated.log(Level.SEVERE, 5000, sourceName,
                			"[" + robot.getSettlement() + "] " + robot +		
        					" has an invalid walk destination location. (" +
                            xLoc + ", " + yLoc + ") is not within building " + building);
//                throw new IllegalStateException("Invalid walk destination location. (" +
//                    xLoc + ", " + yLoc + ") is not within building " + building);
            }
        }
//        else if (interiorObject instanceof Rover) {
//        	
//        	if (person != null) {
//	            Rover rover = (Rover) interiorObject;
//	            result = new WalkState(WalkState.ROVER_LOC);
//	            result.rover = rover;
//	
//	            if (!LocalAreaUtil.checkLocationWithinLocalBoundedObject(xLoc, yLoc, rover)) {
//
//	        			LogConsolidated.log(Level.SEVERE, 5000, sourceName,
//	                			"[" + robot.getSettlement() + "] " + robot +		
//	        					" has an invalid walk destination location. (" +
//	                            xLoc + ", " + yLoc + ") is not within rover " + rover, null);
//	                //throw new IllegalStateException("Invalid walk destination location. (" +
//	                //    xLoc + ", " + yLoc + ") is not within rover " + rover);
//	            }
//        	}
//        }
//        else {
//        		result = new WalkState(WalkState.OUTSIDE_LOC);
//        }

        result.xLoc = xLoc;
        result.yLoc = yLoc;

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
            createWalkSettlementInteriorStep(destinationWalkState.xLoc, destinationWalkState.yLoc,
                    destinationBuilding);
        }
        else {

            // Find closest walkable airlock to destination.
            Airlock airlock = settlement.getClosestWalkableAvailableAirlock(initialBuilding,
                    destinationWalkState.xLoc, destinationWalkState.yLoc);
            if (airlock == null) {
                canWalkAllSteps = false;
                if (person != null)
        			LogConsolidated.log(Level.WARNING, 10000, sourceName,
        					"[" + person.getSettlement() + "] " + person.getName()
                		+ " in " + person.getBuildingLocation().getNickName()
                		+ " cannot find walkable airlock from building interior to building interior.");
                else if (robot != null)
                	LogConsolidated.log(Level.WARNING, 10000, sourceName,
        					"[" + robot.getSettlement() + "] " + robot.getName()
                    		+ " in " + robot.getBuildingLocation().getNickName()
                    		+ " cannot find walkable airlock from building interior to building interior.");

                return;
            }

            Building airlockBuilding = (Building) airlock.getEntity();
            Point2D interiorAirlockPosition = airlock.getAvailableInteriorPosition();

            // Add settlement interior walk step to starting airlock.
            createWalkSettlementInteriorStep(interiorAirlockPosition.getX(),
                    interiorAirlockPosition.getY(), airlockBuilding);

            // Create interior airlock walk state.
            WalkState interiorAirlockState = new WalkState(WalkState.INTERIOR_AIRLOCK);
            interiorAirlockState.airlock = airlock;
            interiorAirlockState.building = airlockBuilding;
            interiorAirlockState.xLoc = interiorAirlockPosition.getX();
            interiorAirlockState.yLoc = interiorAirlockPosition.getY();

            determineWalkingSteps(interiorAirlockState, destinationWalkState);
        }
 
    }

    /**
     * Determine the walking steps from a building interior to another building interior.
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
            createWalkSettlementInteriorStep(destinationWalkState.xLoc, destinationWalkState.yLoc,
                    destinationBuilding);
        }
    }
    
    /**
     * Determine the walking steps from a building interior to a rover interior.
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
            garageWalkState.xLoc = destinationWalkState.xLoc;
            garageWalkState.yLoc = destinationWalkState.yLoc;
            determineBuildingInteriorToBuildingInteriorWalkingSteps(initialWalkState, garageWalkState);

            // Add enter rover walk step.
            WalkStep enterRoverInGarageStep = new WalkStep(WalkStep.ENTER_GARAGE_ROVER);
            enterRoverInGarageStep.rover = destinationRover;
            enterRoverInGarageStep.building = garageBuilding;
            enterRoverInGarageStep.xLoc = destinationWalkState.xLoc;
            enterRoverInGarageStep.yLoc = destinationWalkState.yLoc;
            walkingSteps.add(enterRoverInGarageStep);
        }
        else {

            // Find closest walkable airlock to destination.
            Airlock airlock = settlement.getClosestWalkableAvailableAirlock(initialBuilding,
                    destinationWalkState.xLoc, destinationWalkState.yLoc);
            if (airlock == null) {
                canWalkAllSteps = false;
                if (person != null)
                	LogConsolidated.log(Level.WARNING, 10000, sourceName,
        					"[" + person.getSettlement() + "] " + person.getName()
                		+ " in " + person.getBuildingLocation().getNickName()
                		+ " cannot find walkable airlock from building interior to building interior.");
                else if (robot != null)
                	LogConsolidated.log(Level.WARNING, 10000, sourceName,
        					"[" + robot.getSettlement() + "] " + robot.getName()
                    		+ " in " + robot.getBuildingLocation().getNickName()
                    		+ " cannot find walkable airlock from building interior to building interior.");
               return;
            }

            if (person != null) {
	            Building airlockBuilding = (Building) airlock.getEntity();
	            Point2D interiorAirlockPosition = airlock.getAvailableInteriorPosition();
	
	            // Add settlement interior walk step to starting airlock.
	            createWalkSettlementInteriorStep(interiorAirlockPosition.getX(),
	                    interiorAirlockPosition.getY(), airlockBuilding);
	
	            // Create interior airlock walk state.
	            WalkState interiorAirlockState = new WalkState(WalkState.INTERIOR_AIRLOCK);
	            interiorAirlockState.airlock = airlock;
	            interiorAirlockState.building = airlockBuilding;
	            interiorAirlockState.xLoc = interiorAirlockPosition.getX();
	            interiorAirlockState.yLoc = interiorAirlockPosition.getY();
	
	            determineWalkingSteps(interiorAirlockState, destinationWalkState);
            }
        }
    }

    /**
     * Determine the walking steps between a building interior and outside.
     * @param initialWalkState the initial walk state.
     * @param destinationWalkState the destination walk state.
     */
    private void determineBuildingInteriorToOutsideWalkingSteps(WalkState initialWalkState,
            WalkState destinationWalkState) {

        Building initialBuilding = initialWalkState.building;
        Settlement settlement = initialBuilding.getSettlement();

        // Find closest walkable airlock to destination.
        Airlock airlock = settlement.getClosestWalkableAvailableAirlock(initialBuilding,
                destinationWalkState.xLoc, destinationWalkState.yLoc);
        if (airlock == null) {
            canWalkAllSteps = false;
            if (person != null) {
            	LogConsolidated.log(Level.WARNING, 10000, sourceName,
    					"[" + person.getSettlement() + "] " + person.getName()
            		+ " in " + person.getBuildingLocation().getNickName()
            		+ " cannot find walkable airlock from building interior to building interior.");
            }
            else if (robot != null) {
            	LogConsolidated.log(Level.WARNING, 10000, sourceName,
    					"[" + robot.getSettlement() + "] " + robot.getName()
                		+ " in " + robot.getBuildingLocation().getNickName()
                		+ " cannot find walkable airlock from building interior to building interior.");
            }
           return;
        }

        Building airlockBuilding = (Building) airlock.getEntity();
        Point2D interiorAirlockPosition = airlock.getAvailableInteriorPosition();

        // Add settlement interior walk step to starting airlock.
        createWalkSettlementInteriorStep(interiorAirlockPosition.getX(),
                interiorAirlockPosition.getY(), airlockBuilding);

        // Create interior airlock walk state.
        WalkState interiorAirlockState = new WalkState(WalkState.INTERIOR_AIRLOCK);
        interiorAirlockState.airlock = airlock;
        interiorAirlockState.building = airlockBuilding;
        interiorAirlockState.xLoc = interiorAirlockPosition.getX();
        interiorAirlockState.yLoc = interiorAirlockPosition.getY();

        determineWalkingSteps(interiorAirlockState, destinationWalkState);
    }

    /**
     * Determine the walking steps between two rover interior locations.
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
            exitRoverInGarageStep.xLoc = initialWalkState.xLoc;
            exitRoverInGarageStep.yLoc = initialWalkState.yLoc;
            walkingSteps.add(exitRoverInGarageStep);

            // Create walking steps to destination building.
            WalkState buildingWalkState = new WalkState(WalkState.BUILDING_LOC);
            buildingWalkState.building = garageBuilding;
            buildingWalkState.xLoc = initialWalkState.xLoc;
            buildingWalkState.yLoc = initialWalkState.yLoc;
            determineBuildingInteriorToBuildingInteriorWalkingSteps(buildingWalkState,
                    destinationWalkState);
        }
        else {

        	if (person != null) {
	            // Walk to rover airlock.
	            Airlock airlock = initialRover.getAirlock();
	            Point2D interiorAirlockPosition = airlock.getAvailableInteriorPosition();
	
	            // Add rover interior walk step to starting airlock.
	            createWalkRoverInteriorStep(interiorAirlockPosition.getX(),
	                    interiorAirlockPosition.getY(), initialRover);
	
	            // Create interior airlock walk state.
	            WalkState interiorAirlockState = new WalkState(WalkState.INTERIOR_AIRLOCK);
	            interiorAirlockState.airlock = airlock;
	            interiorAirlockState.rover = initialRover;
	            interiorAirlockState.xLoc = interiorAirlockPosition.getX();
	            interiorAirlockState.yLoc = interiorAirlockPosition.getY();
	
	            determineWalkingSteps(interiorAirlockState, destinationWalkState);
        	}
        }
    }

    /**
     * Determine the walking steps between a rover interior and a rover interior.
     * @param initialWalkState the initial walk state.
     * @param destinationWalkState the destination walk state.
     */
    private void determineRoverToRoverWalkingSteps(WalkState initialWalkState,
            WalkState destinationWalkState) {

        Rover initialRover = initialWalkState.rover;
        Rover destinationRover = destinationWalkState.rover;

        if (initialRover.equals(destinationRover)) {

            // Walk to rover interior location.
            createWalkRoverInteriorStep(destinationWalkState.xLoc,
                    destinationWalkState.yLoc, destinationRover);
        }
        else {

            // Check if initial rover is in a garage.
            Building garageBuilding = BuildingManager.getBuilding(initialRover);
            if (garageBuilding != null) {

                // Add exit rover walk step.
                WalkStep exitRoverInGarageStep = new WalkStep(WalkStep.EXIT_GARAGE_ROVER);
                exitRoverInGarageStep.rover = initialRover;
                exitRoverInGarageStep.building = garageBuilding;
                exitRoverInGarageStep.xLoc = initialWalkState.xLoc;
                exitRoverInGarageStep.yLoc = initialWalkState.yLoc;
                walkingSteps.add(exitRoverInGarageStep);

                // Create walking steps to destination rover.
                WalkState buildingWalkState = new WalkState(WalkState.BUILDING_LOC);
                buildingWalkState.building = garageBuilding;
                buildingWalkState.xLoc = initialWalkState.xLoc;
                buildingWalkState.yLoc = initialWalkState.yLoc;

                determineBuildingInteriorToRoverWalkingSteps(buildingWalkState,
                        destinationWalkState);
            }
            else {
            	if (person != null) {
	                // Walk to rover airlock.
	                Airlock airlock = initialRover.getAirlock();
	                Point2D interiorAirlockPosition = airlock.getAvailableInteriorPosition();
	
	                // Add rover interior walk step to starting airlock.
	                createWalkRoverInteriorStep(interiorAirlockPosition.getX(),
	                        interiorAirlockPosition.getY(), initialRover);
	
	                // Create interior airlock walk state.
	                WalkState interiorAirlockState = new WalkState(WalkState.INTERIOR_AIRLOCK);
	                interiorAirlockState.airlock = airlock;
	                interiorAirlockState.rover = initialRover;
	                interiorAirlockState.xLoc = interiorAirlockPosition.getX();
	                interiorAirlockState.yLoc = interiorAirlockPosition.getY();
	
	                determineWalkingSteps(interiorAirlockState, destinationWalkState);
            	}
            }
        }
    }

    /**
     * Determine the walking steps between a rover interior and outside location.
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
            exitRoverInGarageStep.xLoc = initialWalkState.xLoc;
            exitRoverInGarageStep.yLoc = initialWalkState.yLoc;
            walkingSteps.add(exitRoverInGarageStep);

            // Create walking steps to destination building.
            WalkState buildingWalkState = new WalkState(WalkState.BUILDING_LOC);
            buildingWalkState.building = garageBuilding;
            buildingWalkState.xLoc = initialWalkState.xLoc;
            buildingWalkState.yLoc = initialWalkState.yLoc;

            determineBuildingInteriorToOutsideWalkingSteps(buildingWalkState,
                    destinationWalkState);
        }
        else {

            // Walk to rover airlock.
            Airlock airlock = initialRover.getAirlock();
            Point2D interiorAirlockPosition = airlock.getAvailableInteriorPosition();

            // Add rover interior walk step to starting airlock.
            createWalkRoverInteriorStep(interiorAirlockPosition.getX(),
                    interiorAirlockPosition.getY(), initialRover);

            // Create interior airlock walk state.
            WalkState interiorAirlockState = new WalkState(WalkState.INTERIOR_AIRLOCK);
            interiorAirlockState.airlock = airlock;
            interiorAirlockState.rover = initialRover;
            interiorAirlockState.xLoc = interiorAirlockPosition.getX();
            interiorAirlockState.yLoc = interiorAirlockPosition.getY();

            determineWalkingSteps(interiorAirlockState, destinationWalkState);
        }
    }

    /**
     * Determine the walking steps from an airlock interior location.
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
     * @param initialWalkState the initial walk state.
     * @param destinationWalkState the destination walk state.
     */
    private void determineAirlockInteriorToBuildingInteriorWalkingSteps(WalkState initialWalkState,
            WalkState destinationWalkState) {

        Airlock airlock = initialWalkState.airlock;
        Building destinationBuilding = destinationWalkState.building;
        Settlement settlement = destinationBuilding.getSettlement();

        // Check if airlock is for a building or a rover.
        if (airlock.getEntity() instanceof Building) {

            Building airlockBuilding = (Building) airlock.getEntity();

            // Check if walkable interior path between airlock building and destination building.
            if (settlement.getBuildingConnectorManager().hasValidPath(airlockBuilding, destinationBuilding)) {

                // Add settlement interior walk step.
                createWalkSettlementInteriorStep(destinationWalkState.xLoc, destinationWalkState.yLoc,
                        destinationBuilding);
            }
            else {

                // Add exit airlock walk step.
                createExitAirlockStep(airlock);

                // Create exterior airlock walk state.
                WalkState exteriorAirlockState = new WalkState(WalkState.EXTERIOR_AIRLOCK);
                exteriorAirlockState.airlock = airlock;
                Point2D exteriorAirlockPosition = airlock.getAvailableExteriorPosition();
                exteriorAirlockState.xLoc = exteriorAirlockPosition.getX();
                exteriorAirlockState.yLoc = exteriorAirlockPosition.getY();

                determineWalkingSteps(exteriorAirlockState, destinationWalkState);
            }
        }
        else if (airlock.getEntity() instanceof Rover) {

            // Add exit airlock walk step.
            createExitAirlockStep(airlock);

            // Create exterior airlock walk state.
            WalkState exteriorAirlockState = new WalkState(WalkState.EXTERIOR_AIRLOCK);
            exteriorAirlockState.airlock = airlock;
            Point2D exteriorAirlockPosition = airlock.getAvailableExteriorPosition();
            exteriorAirlockState.xLoc = exteriorAirlockPosition.getX();
            exteriorAirlockState.yLoc = exteriorAirlockPosition.getY();

            determineWalkingSteps(exteriorAirlockState, destinationWalkState);
        }
        else {
            throw new IllegalArgumentException("Invalid airlock entity for walking: " + airlock.getEntity());
        }
    }

    /**
     * Determine the walking steps between an airlock interior and rover interior location.
     * @param initialWalkState the initial walk state.
     * @param destinationWalkState the destination walk state.
     */
    private void determineAirlockInteriorToRoverWalkingSteps(WalkState initialWalkState,
            WalkState destinationWalkState) {

        Airlock airlock = initialWalkState.airlock;
        Rover destinationRover = destinationWalkState.rover;

        // Check if airlock is for a building or a rover.
        if (airlock.getEntity() instanceof Building) {

            Building airlockBuilding = (Building) airlock.getEntity();

            // Check if rover is in a garage or outside.
            Building garageBuilding = BuildingManager.getBuilding(destinationRover);
            if (garageBuilding != null) {

                // Check if garage building has a walkable interior path from airlock building.
                Settlement settlement = airlockBuilding.getSettlement();
                if (settlement.getBuildingConnectorManager().hasValidPath(airlockBuilding, garageBuilding)) {

                    // Add settlement interior walk step.
                    createWalkSettlementInteriorStep(destinationWalkState.xLoc, destinationWalkState.yLoc,
                            garageBuilding);

                    // Add enter rover walk step.
                    WalkStep enterRoverInGarageStep = new WalkStep(WalkStep.ENTER_GARAGE_ROVER);
                    enterRoverInGarageStep.rover = destinationRover;
                    enterRoverInGarageStep.building = garageBuilding;
                    enterRoverInGarageStep.xLoc = destinationWalkState.xLoc;
                    enterRoverInGarageStep.yLoc = destinationWalkState.yLoc;
                    walkingSteps.add(enterRoverInGarageStep);
                }
                else {

                    // Add exit airlock walk step.
                    createExitAirlockStep(airlock);

                    // Create exterior airlock walk state.
                    WalkState exteriorAirlockState = new WalkState(WalkState.EXTERIOR_AIRLOCK);
                    exteriorAirlockState.airlock = airlock;
                    Point2D exteriorAirlockPosition = airlock.getAvailableExteriorPosition();
                    exteriorAirlockState.xLoc = exteriorAirlockPosition.getX();
                    exteriorAirlockState.yLoc = exteriorAirlockPosition.getY();

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
                exteriorAirlockState.xLoc = exteriorAirlockPosition.getX();
                exteriorAirlockState.yLoc = exteriorAirlockPosition.getY();

                determineWalkingSteps(exteriorAirlockState, destinationWalkState);
            }

        }
        else if (airlock.getEntity() instanceof Rover) {

            Rover airlockRover = (Rover) airlock.getEntity();

            // Check if airlockRover is the same as destinationRover.
            if (airlockRover.equals(destinationRover)) {

                // Create walking step internal to rover.
                createWalkRoverInteriorStep(destinationWalkState.xLoc, destinationWalkState.yLoc,
                        destinationRover);
            }
            else {

                // Add exit airlock walk step.
                createExitAirlockStep(airlock);

                // Create exterior airlock walk state.
                WalkState exteriorAirlockState = new WalkState(WalkState.EXTERIOR_AIRLOCK);
                exteriorAirlockState.airlock = airlock;
                Point2D exteriorAirlockPosition = airlock.getAvailableExteriorPosition();
                exteriorAirlockState.xLoc = exteriorAirlockPosition.getX();
                exteriorAirlockState.yLoc = exteriorAirlockPosition.getY();

                determineWalkingSteps(exteriorAirlockState, destinationWalkState);
            }
        }
        else {
            throw new IllegalArgumentException("Invalid airlock entity for walking: " + airlock.getEntity());
        }
    }

    /**
     * Determine the walking steps between an airlock interior and an outside location.
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
        exteriorAirlockState.xLoc = exteriorAirlockPosition.getX();
        exteriorAirlockState.yLoc = exteriorAirlockPosition.getY();

        determineWalkingSteps(exteriorAirlockState, destinationWalkState);
    }

    /**
     * Determine the walking steps from an airlock exterior location.
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
     * @param initialWalkState the initial walk state.
     * @param destinationWalkState the destination walk state.
     */
    private void determineAirlockExteriorToBuildingInteriorWalkingSteps(WalkState initialWalkState,
            WalkState destinationWalkState) {

        Airlock airlock = initialWalkState.airlock;
        Building destinationBuilding = destinationWalkState.building;

        if (airlock.getEntity() instanceof Building) {

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
                interiorAirlockState.xLoc = interiorAirlockPosition.getX();
                interiorAirlockState.yLoc = interiorAirlockPosition.getY();

                determineWalkingSteps(interiorAirlockState, destinationWalkState);
            }
            else {

                // Determine closest airlock to destination building.
                Airlock destinationAirlock = settlement.getClosestWalkableAvailableAirlock(destinationBuilding,
                        initialWalkState.xLoc, initialWalkState.yLoc);
                if (destinationAirlock != null) {

                    // Create walk step to exterior airlock position.
                    Point2D destinationAirlockExteriorPosition = destinationAirlock.getAvailableExteriorPosition();
                    createWalkExteriorStep(destinationAirlockExteriorPosition.getX(),
                            destinationAirlockExteriorPosition.getY());

                    // Create exterior airlock walk state.
                    WalkState exteriorAirlockState = new WalkState(WalkState.EXTERIOR_AIRLOCK);
                    exteriorAirlockState.airlock = destinationAirlock;
                    exteriorAirlockState.xLoc = destinationAirlockExteriorPosition.getX();
                    exteriorAirlockState.yLoc = destinationAirlockExteriorPosition.getY();

                    determineWalkingSteps(exteriorAirlockState, destinationWalkState);
                }
                else {

                    // Cannot walk to destination building.
                    canWalkAllSteps = false;

                    if (person != null) {
                    	LogConsolidated.log(Level.WARNING, 10000, sourceName,
            					"[" + person.getLocationTag().getLocale()  + "] " + person.getName()
            					+ " in " + person.getBuildingLocation().getNickName()
                        		+ " cannot find walkable airlock from building airlock exterior to building interior.");
                    }
                    else if (robot != null) {
                    	LogConsolidated.log(Level.WARNING, 10000, sourceName,
            					"[" + robot.getLocationTag().getLocale()  + "] " + robot.getName()
                        		+ " in " + robot.getBuildingLocation().getNickName()
                        		+ " cannot find walkable airlock from building airlock exterior to building interior.");
                    }
                    
                    return;
                }
            }
        }
        else if (airlock.getEntity() instanceof Rover) {

            Settlement settlement = destinationBuilding.getSettlement();

            // Determine closest airlock to destination building.
            Airlock destinationAirlock = settlement.getClosestWalkableAvailableAirlock(destinationBuilding,
                    initialWalkState.xLoc, initialWalkState.yLoc);
            if (destinationAirlock != null) {

                // Create walk step to exterior airlock position.
                Point2D destinationAirlockExteriorPosition = destinationAirlock.getAvailableExteriorPosition();
                createWalkExteriorStep(destinationAirlockExteriorPosition.getX(),
                        destinationAirlockExteriorPosition.getY());

                // Create exterior airlock walk state.
                WalkState exteriorAirlockState = new WalkState(WalkState.EXTERIOR_AIRLOCK);
                exteriorAirlockState.airlock = destinationAirlock;
                exteriorAirlockState.xLoc = destinationAirlockExteriorPosition.getX();
                exteriorAirlockState.yLoc = destinationAirlockExteriorPosition.getY();

                determineWalkingSteps(exteriorAirlockState, destinationWalkState);
            }
            else {

                // Cannot walk to destination building.
                canWalkAllSteps = false;
                
                if (person != null)
                	LogConsolidated.log(Level.WARNING, 10000, sourceName,
        					"[" + person.getLocationTag().getLocale() + "] " + person.getName()
        					+ " in " + person.getBuildingLocation().getNickName()
                    		+ " cannot find walkable airlock from rover airlock exterior to building interior.");
                else if (robot != null)
                	LogConsolidated.log(Level.WARNING, 10000, sourceName,
        					"[" + robot.getLocationTag().getLocale()  + "] " + robot.getName()
                    		+ " in " + robot.getBuildingLocation().getNickName()
                    		+ " cannot find walkable airlock from rover airlock exterior to building interior.");
                
            }
        }
        else {
            throw new IllegalArgumentException("Invalid airlock entity for walking: " + airlock.getEntity());
        }
    }

    /**
     * Determine the walking steps between an airlock exterior and a rover interior location.
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
                    initialWalkState.xLoc, initialWalkState.yLoc);
            if (destinationAirlock != null) {

                if (initialAirlock.equals(destinationAirlock)) {

                    // Create enter airlock walk step.
                    createEnterAirlockStep(initialAirlock);

                    // Create airlock interior state.
                    WalkState interiorAirlockState = new WalkState(WalkState.INTERIOR_AIRLOCK);
                    interiorAirlockState.airlock = initialAirlock;
                    interiorAirlockState.building = (Building) initialAirlock.getEntity();
                    Point2D interiorAirlockPosition = initialAirlock.getAvailableInteriorPosition();
                    interiorAirlockState.xLoc = interiorAirlockPosition.getX();
                    interiorAirlockState.yLoc = interiorAirlockPosition.getY();

                    determineWalkingSteps(interiorAirlockState, destinationWalkState);
                }
                else {

                    // Create walk step to exterior airlock position.
                    Point2D destinationAirlockExteriorPosition = destinationAirlock.getAvailableExteriorPosition();
                    createWalkExteriorStep(destinationAirlockExteriorPosition.getX(),
                            destinationAirlockExteriorPosition.getY());

                    // Create exterior airlock walk state.
                    WalkState exteriorAirlockState = new WalkState(WalkState.EXTERIOR_AIRLOCK);
                    exteriorAirlockState.airlock = destinationAirlock;
                    exteriorAirlockState.xLoc = destinationAirlockExteriorPosition.getX();
                    exteriorAirlockState.yLoc = destinationAirlockExteriorPosition.getY();

                    determineWalkingSteps(exteriorAirlockState, destinationWalkState);
                }
            }
            else {

                // Cannot walk to destination building.
                canWalkAllSteps = false;
           
                if (person != null)
                	LogConsolidated.log(Level.WARNING, 10000, sourceName,
        					"[" + person.getLocationTag().getLocale() + "] " + person.getName()
        					+ " in " + person.getBuildingLocation().getNickName()
                    		+ " cannot find walkable airlock from airlock exterior to rover in garage.");
                else if (robot != null)
                	LogConsolidated.log(Level.WARNING, 10000, sourceName,
        					"[" + robot.getLocationTag().getLocale() + "] " + robot.getName()
                    		+ " in " + robot.getBuildingLocation().getNickName()
                    		+ " cannot find walkable airlock from airlock exterior to rover in garage.");
                
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
                interiorAirlockState.xLoc = interiorAirlockPosition.getX();
                interiorAirlockState.yLoc = interiorAirlockPosition.getY();

                determineWalkingSteps(interiorAirlockState, destinationWalkState);
            }
            else {

                Airlock destinationAirlock = destinationRover.getAirlock();

                // Create walk step to exterior airlock position.
                Point2D destinationAirlockExteriorPosition = destinationAirlock.getAvailableExteriorPosition();
                createWalkExteriorStep(destinationAirlockExteriorPosition.getX(),
                        destinationAirlockExteriorPosition.getY());

                // Create exterior airlock walk state.
                WalkState exteriorAirlockState = new WalkState(WalkState.EXTERIOR_AIRLOCK);
                exteriorAirlockState.airlock = destinationAirlock;
                exteriorAirlockState.xLoc = destinationAirlockExteriorPosition.getX();
                exteriorAirlockState.yLoc = destinationAirlockExteriorPosition.getY();

                determineWalkingSteps(exteriorAirlockState, destinationWalkState);
            }
        }
    }

    /**
     * Determine the walking steps between an airlock exterior and an outside location.
     * @param initialWalkState the initial walk state.
     * @param destinationWalkState the destination walk state.
     */
    private void determineAirlockExteriorToOutsideWalkingSteps(WalkState initialWalkState,
            WalkState destinationWalkState) {

        // Create walk step to exterior location.
        createWalkExteriorStep(destinationWalkState.xLoc, destinationWalkState.yLoc);
    }

    /**
     * Determine the walking steps from an outside location.
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
     * @param initialWalkState the initial walk state.
     * @param destinationWalkState the destination walk state.
     */
    private void determineOutsideToBuildingInteriorWalkingSteps(WalkState initialWalkState,
            WalkState destinationWalkState) {

        Building destinationBuilding = destinationWalkState.building;
        Settlement settlement = destinationBuilding.getSettlement();

        // Determine closest airlock to destination building.
        Airlock destinationAirlock = settlement.getClosestWalkableAvailableAirlock(destinationBuilding,
                initialWalkState.xLoc, initialWalkState.yLoc);
        if (destinationAirlock != null) {

            // Create walk step to exterior airlock position.
            Point2D destinationAirlockExteriorPosition = destinationAirlock.getAvailableExteriorPosition();
            createWalkExteriorStep(destinationAirlockExteriorPosition.getX(),
                    destinationAirlockExteriorPosition.getY());

            // Create exterior airlock walk state.
            WalkState exteriorAirlockState = new WalkState(WalkState.EXTERIOR_AIRLOCK);
            exteriorAirlockState.airlock = destinationAirlock;
            exteriorAirlockState.xLoc = destinationAirlockExteriorPosition.getX();
            exteriorAirlockState.yLoc = destinationAirlockExteriorPosition.getY();

            determineWalkingSteps(exteriorAirlockState, destinationWalkState);
        }
        else {

            // Cannot walk to destination building.
            canWalkAllSteps = false;
//            logger.severe("Cannot find walkable airlock from outside to building interior.");
            
            if (person != null)
            	LogConsolidated.log(Level.WARNING, 10000, sourceName,
    					"[" + person.getLocationTag().getLocale() + "] " + person.getName()
    					+ " in " + person.getBuildingLocation().getNickName()
                		+ " cannot find walkable airlock from outside to building interior.");
            else if (robot != null)
            	LogConsolidated.log(Level.WARNING, 10000, sourceName,
    					"[" + robot.getLocationTag().getLocale() + "] " + robot.getName()
                		+ " in " + robot.getBuildingLocation().getNickName()
                		+ " cannot find walkable airlock from outside to building interior.");    
        }
    }

    /**
     * Determine the walking steps between an outside and rover interior location.
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
                    initialWalkState.xLoc, initialWalkState.yLoc);
            if (destinationAirlock != null) {

                // Create walk step to exterior airlock position.
                Point2D destinationAirlockExteriorPosition = destinationAirlock.getAvailableExteriorPosition();
                createWalkExteriorStep(destinationAirlockExteriorPosition.getX(),
                        destinationAirlockExteriorPosition.getY());

                // Create exterior airlock walk state.
                WalkState exteriorAirlockState = new WalkState(WalkState.EXTERIOR_AIRLOCK);
                exteriorAirlockState.airlock = destinationAirlock;
                exteriorAirlockState.xLoc = destinationAirlockExteriorPosition.getX();
                exteriorAirlockState.yLoc = destinationAirlockExteriorPosition.getY();

                determineWalkingSteps(exteriorAirlockState, destinationWalkState);
            }
            else {

                // Cannot walk to destination building.
                canWalkAllSteps = false;
                
                if (person != null)
                	LogConsolidated.log(Level.WARNING, 10000, sourceName,
        					"[" + person.getLocationTag().getLocale() + "] " + person.getName()
        					+ " in " + person.getBuildingLocation().getNickName()
                    		+ " cannot find walkable airlock from outside to rover in garage.");
                else if (robot != null)
                	LogConsolidated.log(Level.WARNING, 10000, sourceName,
        					"[" + robot.getLocationTag().getLocale()  + "] " + robot.getName()
                    		+ " in " + robot.getBuildingLocation().getNickName()
                    		+ " cannot find walkable airlock from outside to rover in garage.");
                
            }
        }
        else {

            Airlock destinationAirlock = destinationRover.getAirlock();

            // Create walk step to exterior airlock position.
            Point2D destinationAirlockExteriorPosition = destinationAirlock.getAvailableExteriorPosition();
            createWalkExteriorStep(destinationAirlockExteriorPosition.getX(),
                    destinationAirlockExteriorPosition.getY());

            // Create exterior airlock walk state.
            WalkState exteriorAirlockState = new WalkState(WalkState.EXTERIOR_AIRLOCK);
            exteriorAirlockState.airlock = destinationAirlock;
            exteriorAirlockState.xLoc = destinationAirlockExteriorPosition.getX();
            exteriorAirlockState.yLoc = destinationAirlockExteriorPosition.getY();

            determineWalkingSteps(exteriorAirlockState, destinationWalkState);
        }
    }

    /**
     * Determine the walking steps between an outside and outside location.
     * @param initialWalkState the initial walk state.
     * @param destinationWalkState the destinatino walk state.
     */
    private void determineOutsideToOutsideWalkingSteps(WalkState initialWalkState,
            WalkState destinationWalkState) {

        // Create walk step to exterior location.
        createWalkExteriorStep(destinationWalkState.xLoc, destinationWalkState.yLoc);
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
     * @param destXLoc the destination X location.
     * @param destYLoc the destination Y location.
     * @param destinationRover the destination rover.
     */
    private void createWalkRoverInteriorStep(double destXLoc, double destYLoc,
            Rover destinationRover) {

        WalkStep walkStep = new WalkStep(WalkStep.ROVER_INTERIOR_WALK);
        walkStep.xLoc = destXLoc;
        walkStep.yLoc = destYLoc;
        walkStep.rover = destinationRover;
        walkingSteps.add(walkStep);
    }

    /**
     * Create a settlement interior walking step.
     * @param destXLoc the destination X location.
     * @param destYLoc the destination Y location.
     * @param destinationBuilding the destination building.
     */
    private void createWalkSettlementInteriorStep(double destXLoc, double destYLoc,
            Building destinationBuilding) {
       if (person != null) {
           WalkStep walkStep = new WalkStep(WalkStep.SETTLEMENT_INTERIOR_WALK);
           walkStep.xLoc = destXLoc;
           walkStep.yLoc = destYLoc;
           walkStep.building = destinationBuilding;
           walkingSteps.add(walkStep);        	
        }
        else if (robot != null ){
            RobotWalkStep walkStep = new RobotWalkStep(RobotWalkStep.SETTLEMENT_INTERIOR_WALK);
            walkStep.xLoc = destXLoc;
            walkStep.yLoc = destYLoc;
            walkStep.building = destinationBuilding;
            robotWalkingSteps.add(walkStep);       	
        }

    }

    /**
     * Create a climb up step.
     * 
     * @param destXLoc the destination X location.
     * @param destYLoc the destination Y location.
     * @param destZLoc the destination Z location.
     * @param destinationBuilding the destination building.
     */
    private void createClimbUpStep(double destXLoc, double destYLoc, double destZLoc,
            Building destinationBuilding) {
       if (person != null) {
           WalkStep walkStep = new WalkStep(WalkStep.UP_LADDER);
           walkStep.xLoc = destXLoc;
           walkStep.yLoc = destYLoc;
           walkStep.zLoc = destZLoc;
           walkStep.building = destinationBuilding;
           walkingSteps.add(walkStep);        	
        }
        else if (robot != null ){
//            RobotWalkStep walkStep = new RobotWalkStep(RobotWalkStep.UP_LADDER);
//            walkStep.xLoc = destXLoc;
//            walkStep.yLoc = destYLoc;
//            walkStep.building = destinationBuilding;
//            robotWalkingSteps.add(walkStep);       	
        }
    }
    
    /**
     * Create a climb up step.
     * 
     * @param destXLoc the destination X location.
     * @param destYLoc the destination Y location.
     * @param destZLoc the destination Z location.
     * @param destinationBuilding the destination building.
     */
    private void createClimbDownStep(double destXLoc, double destYLoc, double destZLoc,
            Building destinationBuilding) {
       if (person != null) {
           WalkStep walkStep = new WalkStep(WalkStep.DOWN_LADDER);
           walkStep.xLoc = destXLoc;
           walkStep.yLoc = destYLoc;
           walkStep.zLoc = destZLoc;
           walkStep.building = destinationBuilding;
           walkingSteps.add(walkStep);        	
        }
        else if (robot != null ){
//            RobotWalkStep walkStep = new RobotWalkStep(RobotWalkStep.UP_LADDER);
//            walkStep.xLoc = destXLoc;
//            walkStep.yLoc = destYLoc;
//            walkStep.building = destinationBuilding;
//            robotWalkingSteps.add(walkStep);       	
        }
    }
    
    /**
     * Create an exterior walking step.
     * @param destXLoc the destination X location.
     * @param destYLoc the destination Y location.
     */
    private void createWalkExteriorStep(double destXLoc, double destYLoc) {

        WalkStep walkExterior = new WalkStep(WalkStep.EXTERIOR_WALK);
        walkExterior.xLoc = destXLoc;
        walkExterior.yLoc = destYLoc;
        walkingSteps.add(walkExterior);
    }

    /**
     * Create an exit airlock walking step.
     * @param airlock the airlock.
     */
    private void createExitAirlockStep(Airlock airlock) {

        WalkStep exitAirlockStep = new WalkStep(WalkStep.EXIT_AIRLOCK);
        exitAirlockStep.airlock = airlock;
        walkingSteps.add(exitAirlockStep);
    }

    /**
     * Create an enter airlock walking step.
     * @param airlock the airlock.
     */
    private void createEnterAirlockStep(Airlock airlock) {

        WalkStep enterAirlockStep = new WalkStep(WalkStep.ENTER_AIRLOCK);
        enterAirlockStep.airlock = airlock;
        walkingSteps.add(enterAirlockStep);
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
        private double xLoc;
        private double yLoc;
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
        double xLoc;
        double yLoc;
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
        private double xLoc;
        private double yLoc;
        private Building building;

        private RobotWalkState(int stateType) {
            this.stateType = stateType;
        }
    }

    /**
     * Inner class for representing a walking step.
     */
    class RobotWalkStep implements Serializable {

        /** default serial id. */
        private static final long serialVersionUID = 1L;

        // Step types.
        static final int SETTLEMENT_INTERIOR_WALK = 0;

        // Data members
        int stepType;
        double xLoc;
        double yLoc;
        Building building;

        private RobotWalkStep(int stepType) {
            this.stepType = stepType;
        }
        
    	public void destroy() {
            building = null;
    	}
    }

    
	public void destroy() {
		walkingSteps = null;
		person = null;
		robot = null;
	}
}
