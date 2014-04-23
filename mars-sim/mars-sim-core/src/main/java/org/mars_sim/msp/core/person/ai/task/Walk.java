/**
 * Mars Simulation Project
 * Walk.java
 * @version 3.06 2014-04-22
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Airlock;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LocalBoundedObject;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.vehicle.Airlockable;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * A general walking task that includes interior/exterior walking
 * and entering/exiting airlocks.
 */
public class Walk
extends Task
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default serial id. */
	private static Logger logger = Logger.getLogger(Walk.class.getName());

	// TODO Task phase should be an enum
	private static final String WALKING_SETTLEMENT_INTERIOR = "Walking Inside Settlement";
	private static final String WALKING_ROVER_INTERIOR = "Walking Inside Rover";
	private static final String WALKING_EXTERIOR = "Walking Outside";
	private static final String EXITING_AIRLOCK = "Exiting Airlock";
	private static final String ENTERING_AIRLOCK = "Entering Airlock";
	private static final String EXITING_ROVER_GARAGE = "Exiting Rover in Garage";
	private static final String ENTERING_ROVER_GARAGE = "Entering Rover in Garage";

	// Data members
	private WalkingSteps walkingSteps;
	private Map<Integer, String> walkingStepPhaseMap;
	private int walkingStepIndex;

	/**
	 * Constructor.
	 * @param person the person performing the task.
	 */
    public Walk(Person person) {
        super("Walking", person, false, false, 0D, false, 0D);
        
        logger.finer(person + " starting new walk task.");
        
        // Initialize data members.
        walkingStepIndex = 0;
        
        // Determine if person is outside.
        if (LocationSituation.OUTSIDE == person.getLocationSituation()) {
            
            Airlock airlock = findEmergencyAirlock(person);
            if (airlock != null) {
                LocalBoundedObject entity = (LocalBoundedObject) airlock.getEntity();
                Point2D interiorPos = LocalAreaUtil.getRandomInteriorLocation(entity);
                Point2D adjustedInteriorPos = LocalAreaUtil.getLocalRelativeLocation(
                        interiorPos.getX(), interiorPos.getY(), entity);
                walkingSteps = new WalkingSteps(person, adjustedInteriorPos.getX(), 
                        adjustedInteriorPos.getY(), entity);
            }
        }
        else if (LocationSituation.IN_SETTLEMENT == person.getLocationSituation()){
         
            // Walk to random inhabitable building at settlement.
            Building currentBuilding = BuildingManager.getBuilding(person);
            List<Building> buildingList = currentBuilding.getBuildingManager().getBuildings(BuildingFunction.LIFE_SUPPORT);
            if (buildingList.size() > 0) {
                int buildingIndex = RandomUtil.getRandomInt(buildingList.size() - 1);
                Building destinationBuilding = buildingList.get(buildingIndex);
                Point2D interiorPos = LocalAreaUtil.getRandomInteriorLocation(destinationBuilding);
                Point2D adjustedInteriorPos = LocalAreaUtil.getLocalRelativeLocation(
                        interiorPos.getX(), interiorPos.getY(), destinationBuilding);
                walkingSteps = new WalkingSteps(person, adjustedInteriorPos.getX(), 
                        adjustedInteriorPos.getY(), destinationBuilding);
            }
            
        }
        else if (LocationSituation.IN_VEHICLE == person.getLocationSituation()) {
            
            Vehicle vehicle = person.getVehicle();
            
            // If no mission and vehicle is at a settlement location, enter settlement.
            if ((person.getMind().getMission() == null) && (vehicle.getSettlement() != null)) {
                
                Settlement settlement = vehicle.getSettlement();
                
                // If not a rover, retrieve person from vehicle.
                if (!(vehicle instanceof Rover)) {
                    vehicle.getInventory().retrieveUnit(person);
                }
                
                // Walk to nearest emergency airlock in settlement.
                Airlock airlock = settlement.getClosestAvailableAirlock(person);
                if (airlock != null) {
                    LocalBoundedObject entity = (LocalBoundedObject) airlock.getEntity();
                    Point2D interiorPos = LocalAreaUtil.getRandomInteriorLocation(entity);
                    Point2D adjustedInteriorPos = LocalAreaUtil.getLocalRelativeLocation(
                            interiorPos.getX(), interiorPos.getY(), entity);
                    walkingSteps = new WalkingSteps(person, adjustedInteriorPos.getX(), 
                            adjustedInteriorPos.getY(), entity);
                }
            }
            else {
            
                // Walk to random location within rover.
                if (person.getVehicle() instanceof Rover) {
                    Rover rover = (Rover) person.getVehicle();
                    Point2D interiorPos = LocalAreaUtil.getRandomInteriorLocation(rover);
                    Point2D adjustedInteriorPos = LocalAreaUtil.getLocalRelativeLocation(
                            interiorPos.getX(), interiorPos.getY(), rover);
                    walkingSteps = new WalkingSteps(person, adjustedInteriorPos.getX(), 
                            adjustedInteriorPos.getY(), rover);
                }
            }
        }
        else {
            throw new IllegalStateException("Could not determine walking steps for " + person.getName() +
                    " at location " + person.getLocationSituation());
        }
        
        if (walkingSteps == null) {
            logger.severe("Walking steps could not be determined for " + person.getName());
            endTask();
        }
        else if (!canWalkAllSteps(person, walkingSteps)) {
            logger.fine("Valid Walking steps could not be determined for " + person.getName());
            endTask();
        }
        
        // Initialize task phase.
        addPhase(WALKING_SETTLEMENT_INTERIOR);
        addPhase(WALKING_ROVER_INTERIOR);
        addPhase(WALKING_EXTERIOR);
        addPhase(EXITING_AIRLOCK);
        addPhase(ENTERING_AIRLOCK);
        addPhase(EXITING_ROVER_GARAGE);
        addPhase(ENTERING_ROVER_GARAGE);
        
        setPhase(getWalkingStepPhase());
    }
    
    /**
     * Constructor with destination parameters.
     * @param person the person performing the task.
     * @param xLoc the destination X location.
     * @param yLoc the destination Y location.
     * @param interiorObject the interior destination object (inhabitable building or rover).
     */
    public Walk(Person person, double xLoc, double yLoc, LocalBoundedObject interiorObject) {
        super("Walking", person, false, false, 0D, false, 0D);
        
        logger.finer(person + " starting new walk task to a location in " + interiorObject);
        
        // Initialize data members.
        walkingStepIndex = 0;
        
        walkingSteps = new WalkingSteps(person, xLoc, yLoc, interiorObject);
        
        // End task if all steps cannot be walked.
        if (!canWalkAllSteps(person, walkingSteps)) {
            logger.severe(person.getName() + " could not find valid walking steps to " + interiorObject);
            endTask();
        }
        
        // Initialize task phase.
        addPhase(WALKING_SETTLEMENT_INTERIOR);
        addPhase(WALKING_ROVER_INTERIOR);
        addPhase(WALKING_EXTERIOR);
        addPhase(EXITING_AIRLOCK);
        addPhase(ENTERING_AIRLOCK);
        addPhase(EXITING_ROVER_GARAGE);
        addPhase(ENTERING_ROVER_GARAGE);
        
        setPhase(getWalkingStepPhase());
    }
    
    /** 
     * Returns the weighted probability that a person might perform this task.
     * It should return a 0 if there is no chance to perform this task given the person and his/her situation.
     * @param person the person to perform the task
     * @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person) {
        double result = 0D;

        // If person is outside, give high probability to walk to emergency airlock location.
        if (LocationSituation.OUTSIDE == person.getLocationSituation()) {
            result = 1000D;
        }
        else if (LocationSituation.IN_SETTLEMENT == person.getLocationSituation()) {
            // If person is inside a settlement building, may walk to a random location within settlement.
            result = 10D;
        }
        else if (LocationSituation.IN_VEHICLE == person.getLocationSituation()) {
            // If person is inside a rover, may walk to random location within rover.
            result = 10D;
        }
        
        return result;
    }
    
    /**
     * Find an emergency airlock at a person's location.
     * @param person the person.
     * @return airlock or null if none found.
     */
    public static Airlock findEmergencyAirlock(Person person) {
        
        Airlock result = null;
        
        // Determine airlock from other people on mission.
        if (person.getMind().getMission() != null) {
            Iterator<Person> i = person.getMind().getMission().getPeople().iterator();
            while (i.hasNext() && (result == null)) {
                Person p = i.next();
                if (p != person) {
                    LocationSituation location = p.getLocationSituation();
                    if (location == LocationSituation.IN_SETTLEMENT) {
                        result = p.getSettlement().getClosestAvailableAirlock(person);
                    }
                    else if (location == LocationSituation.IN_VEHICLE) {
                        Vehicle vehicle = p.getVehicle();
                        if (vehicle instanceof Airlockable) 
                            result = ((Airlockable) vehicle).getAirlock();
                    }
                }
            }
        }

        // If not look for any settlements at person's location.
        if (result == null) {
            Iterator<Settlement> i = Simulation.instance().getUnitManager().getSettlements().iterator();
            while (i.hasNext() && (result == null)) {
                Settlement settlement = i.next();
                if (person.getCoordinates().equals(settlement.getCoordinates())) 
                    result = settlement.getClosestAvailableAirlock(person);
            }
        }

        // If not look for any vehicles with airlocks at person's location.
        if (result == null) {
            Iterator<Vehicle> i = Simulation.instance().getUnitManager().getVehicles().iterator();
            while (i.hasNext() && (result == null)) {
                Vehicle vehicle = i.next();
                if (person.getCoordinates().equals(vehicle.getCoordinates())) {
                    if (vehicle instanceof Airlockable) 
                        result = ((Airlockable) vehicle).getAirlock();
                }
            }
        }
        
        return result;
    }
    
    /**
     * Check if person can walk to a local destination.
     * @param person the person.
     * @param xLoc the X location.
     * @param yLoc the Y location.
     * @param interiorObject the destination interior object, or null if none.
     * @return true if a person can walk all the steps to the destination.
     */
    public static boolean canWalkAllSteps(Person person, double xLoc, double yLoc, LocalBoundedObject interiorObject) {
        
        WalkingSteps walkingSteps = new WalkingSteps(person, xLoc, yLoc, interiorObject);
        
        return canWalkAllSteps(person, walkingSteps);
    }
    
    /**
     * Check if person can walk to a local destination.
     * @param person the person.
     * @param walkingSteps the walking steps.
     * @return true if a person can walk all the steps to the destination.
     */
    private static boolean canWalkAllSteps(Person person, WalkingSteps walkingSteps) {
        
        boolean result = true;
        
        // Check if all steps can be walked.
        if (!walkingSteps.canWalkAllSteps()) {
            result = false;
        }
        
        // Check if all airlocks can be exited.
        if (!canExitAllAirlocks(person, walkingSteps)) {
            result = false;
        }
        
        return result;
    }
    
    /**
     * Populates the walking step phase map.
     */
    private void populateWalkingStepPhaseMap() {
        
        walkingStepPhaseMap = new HashMap<Integer, String>(7);
        walkingStepPhaseMap.put(WalkingSteps.WalkStep.ENTER_AIRLOCK, ENTERING_AIRLOCK);
        walkingStepPhaseMap.put(WalkingSteps.WalkStep.ENTER_GARAGE_ROVER, ENTERING_ROVER_GARAGE);
        walkingStepPhaseMap.put(WalkingSteps.WalkStep.EXIT_AIRLOCK, EXITING_AIRLOCK);
        walkingStepPhaseMap.put(WalkingSteps.WalkStep.EXIT_GARAGE_ROVER, EXITING_ROVER_GARAGE);
        walkingStepPhaseMap.put(WalkingSteps.WalkStep.EXTERIOR_WALK, WALKING_EXTERIOR);
        walkingStepPhaseMap.put(WalkingSteps.WalkStep.ROVER_INTERIOR_WALK, WALKING_ROVER_INTERIOR);
        walkingStepPhaseMap.put(WalkingSteps.WalkStep.SETTLEMENT_INTERIOR_WALK, WALKING_SETTLEMENT_INTERIOR);
    }
    
    /**
     * Gets the walking step phase.
     * @return walking step phase string.
     */
    private String getWalkingStepPhase() {
        
        String result = null;
        
        // Create and populate walkingStepPhaseMap if necessary.
        if (walkingStepPhaseMap == null) {
            populateWalkingStepPhaseMap();
        }
        
        if (walkingStepIndex < walkingSteps.getWalkingStepsNumber()) {
            
            WalkingSteps.WalkStep step = walkingSteps.getWalkingStepsList().get(walkingStepIndex);
            result = walkingStepPhaseMap.get(step.stepType);
        }
        
        return result;
    }
    
    /**
     * Checks if a person can exit all airlocks in walking steps.
     * @return true is all airlocks can be exited (or no airlocks in walk steps).
     */
    private static boolean canExitAllAirlocks(Person person, WalkingSteps walkingSteps) {
        
        boolean result = true;
        
        List<WalkingSteps.WalkStep> stepList = walkingSteps.getWalkingStepsList();
        if (stepList != null) {
            Iterator<WalkingSteps.WalkStep> i = stepList.iterator();
            while (i.hasNext()) {
                WalkingSteps.WalkStep step = i.next();
                if (step.stepType == WalkingSteps.WalkStep.EXIT_AIRLOCK) {
                    Airlock airlock = step.airlock;
                    if (!ExitAirlock.canExitAirlock(person, airlock)) {
                        result = false;
                    }
                }
            }
        }
        
        return result;
    }
    
    @Override
    protected double performMappedPhase(double time) {
        if (getPhase() == null) {
            throw new IllegalArgumentException("Task phase is null");
        }
        else if (WALKING_SETTLEMENT_INTERIOR.equals(getPhase())) {
            return walkingSettlementInteriorPhase(time);
        }
        else if (WALKING_ROVER_INTERIOR.equals(getPhase())) {
            return walkingRoverInteriorPhase(time);
        }
        else if (WALKING_EXTERIOR.equals(getPhase())) {
            return walkingExteriorPhase(time);
        }
        else if (EXITING_AIRLOCK.equals(getPhase())) {
            return exitingAirlockPhase(time);
        }
        else if (ENTERING_AIRLOCK.equals(getPhase())) {
            return enteringAirlockPhase(time);
        }
        else if (EXITING_ROVER_GARAGE.equals(getPhase())) {
            return exitingRoverGaragePhase(time);
        }
        else if (ENTERING_ROVER_GARAGE.equals(getPhase())) {
            return enteringRoverGaragePhase(time);
        }
        else {
            return time;
        }
    }
    
    /**
     * Performs the walking settlement interior phase of the task.
     * @param time the amount of time (millisol) to perform the walking phase.
     * @return the amount of time (millisol) left after performing the walking phase.
     */
    private double walkingSettlementInteriorPhase(double time) {
        
        logger.finer(person + " walking settlement interior phase.");
        double timeLeft = time;
        
        // Check if person has reached destination location.
        WalkingSteps.WalkStep step = walkingSteps.getWalkingStepsList().get(walkingStepIndex);
        Building building = BuildingManager.getBuilding(person);
        if (step.building.equals(building) && (person.getXLocation() == step.xLoc) && 
                (person.getYLocation() == step.yLoc)) {
            if (walkingStepIndex < (walkingSteps.getWalkingStepsNumber() - 1)) {
                walkingStepIndex++;
                setPhase(getWalkingStepPhase());
            }
            else {
                endTask();
            }
        }
        else {
            addSubTask(new WalkSettlementInterior(person, step.building, step.xLoc, step.yLoc));
        }
        
        return timeLeft;
    }
    
    /**
     * Performs the walking rover interior phase of the task.
     * @param time the amount of time (millisol) to perform the walking phase.
     * @return the amount of time (millisol) left after performing the walking phase.
     */
    private double walkingRoverInteriorPhase(double time) {
        
        logger.finer(person + " walking rover interior phase.");
        double timeLeft = time;
        
        // Check if person has reached destination location.
        WalkingSteps.WalkStep step = walkingSteps.getWalkingStepsList().get(walkingStepIndex);
        Rover rover = (Rover) person.getVehicle();
        
        // Update rover destination if rover has moved and existing destination is no longer within rover.
        if (!LocalAreaUtil.checkLocationWithinLocalBoundedObject(step.xLoc, step.yLoc, rover)) {
            // Determine new destination location within rover.
            // TODO: Determine location based on activity spot?
            Point2D newRoverLoc = LocalAreaUtil.getRandomInteriorLocation(rover);
            Point2D relativeRoverLoc = LocalAreaUtil.getLocalRelativeLocation(newRoverLoc.getX(), 
                    newRoverLoc.getY(), rover);
            step.xLoc = relativeRoverLoc.getX();
            step.yLoc = relativeRoverLoc.getY();
        }
        
        if (step.rover.equals(rover) && (person.getXLocation() == step.xLoc) && 
                (person.getYLocation() == step.yLoc)) {
            if (walkingStepIndex < (walkingSteps.getWalkingStepsNumber() - 1)) {
                walkingStepIndex++;
                setPhase(getWalkingStepPhase());
            }
            else {
                endTask();
            }
        }
        else {
            addSubTask(new WalkRoverInterior(person, step.rover, step.xLoc, step.yLoc));
        }
        
        return timeLeft;
    }
    
    /**
     * Performs the walking exterior phase of the task.
     * @param time the amount of time (millisol) to perform the walking phase.
     * @return the amount of time (millisol) left after performing the walking phase.
     */
    private double walkingExteriorPhase(double time) {
        
        logger.finer(person + " walking exterior phase.");
        
        double timeLeft = time;
        
        // Check if person has reached destination location.
        WalkingSteps.WalkStep step = walkingSteps.getWalkingStepsList().get(walkingStepIndex);
        if ((person.getXLocation() == step.xLoc) && (person.getYLocation() == step.yLoc)) {
            if (walkingStepIndex < (walkingSteps.getWalkingStepsNumber() - 1)) {
                walkingStepIndex++;
                setPhase(getWalkingStepPhase());
            }
            else {
                endTask();
            }
        }
        else {
            logger.finer(person + " starting walk outside task.");
            addSubTask(new WalkOutside(person, person.getXLocation(), person.getYLocation(), 
                    step.xLoc, step.yLoc, true));
        }
        
        return timeLeft;
    }
    
    /**
     * Performs the exiting airlock phase of the task.
     * @param time the amount of time (millisol) to perform the walking phase.
     * @return the amount of time (millisol) left after performing the walking phase.
     */
    private double exitingAirlockPhase(double time) {
        
        logger.finer(person + " walking exiting airlock phase.");
        double timeLeft = time;
        
        // Check if person has reached the outside of the airlock.
        WalkingSteps.WalkStep step = walkingSteps.getWalkingStepsList().get(walkingStepIndex);
        Airlock airlock = step.airlock;
        if (person.getLocationSituation() == LocationSituation.OUTSIDE) {
            if (walkingStepIndex < (walkingSteps.getWalkingStepsNumber() - 1)) {
                walkingStepIndex++;
                setPhase(getWalkingStepPhase());
            }
            else {
                endTask();
            }
        }
        else {
            if (ExitAirlock.canExitAirlock(person, airlock)) {
                addSubTask(new ExitAirlock(person, airlock));
            }
            else {
                endTask();
                logger.severe(person.getName() + " unable to exit airlock of " + 
                        airlock.getEntityName());
            }
        }
        
        return timeLeft;
    }
    
    /**
     * Performs the entering airlock phase of the task.
     * @param time the amount of time (millisol) to perform the walking phase.
     * @return the amount of time (millisol) left after performing the walking phase.
     */
    private double enteringAirlockPhase(double time) {
        
        logger.finer(person + " walking entering airlock phase.");
        double timeLeft = time;
        
        // Check if person has reached the inside of the airlock.
        WalkingSteps.WalkStep step = walkingSteps.getWalkingStepsList().get(walkingStepIndex);
        Airlock airlock = step.airlock;
        if (person.getLocationSituation() != LocationSituation.OUTSIDE) {
            if (walkingStepIndex < (walkingSteps.getWalkingStepsNumber() - 1)) {
                walkingStepIndex++;
                setPhase(getWalkingStepPhase());
            }
            else {
                endTask();
            }
        }
        else {
            if (EnterAirlock.canEnterAirlock(person, airlock)) {
                addSubTask(new EnterAirlock(person, airlock));
            }
            else {
                endTask();
                logger.severe(person.getName() + " unable to enter airlock of " + 
                        airlock.getEntityName());
            }
        }
        
        return timeLeft;
    }
    
    /**
     * Performs the exiting rover in garage phase of the task.
     * @param time the amount of time (millisol) to perform the walking phase.
     * @return the amount of time (millisol) left after performing the walking phase.
     */
    private double exitingRoverGaragePhase(double time) {
        
        logger.finer(person + " walking exiting rover garage phase.");
        double timeLeft = time;
        
        WalkingSteps.WalkStep step = walkingSteps.getWalkingStepsList().get(walkingStepIndex);
        Rover rover = step.rover;
        Building garageBuilding = step.building;
        
        rover.getInventory().retrieveUnit(person);
        garageBuilding.getInventory().storeUnit(person);
        BuildingManager.addPersonToBuildingSameLocation(person, garageBuilding);
        
        if (walkingStepIndex < (walkingSteps.getWalkingStepsNumber() - 1)) {
            walkingStepIndex++;
            setPhase(getWalkingStepPhase());
        }
        else {
            endTask();
        }
        
        return timeLeft;
    }
    
    /**
     * Performs the entering rover in garage phase of the task.
     * @param time the amount of time (millisol) to perform the walking phase.
     * @return the amount of time (millisol) left after performing the walking phase.
     */
    private double enteringRoverGaragePhase(double time) {
        
        logger.finer(person + " walking entering rover garage phase.");
        double timeLeft = time;
        
        WalkingSteps.WalkStep step = walkingSteps.getWalkingStepsList().get(walkingStepIndex);
        Rover rover = step.rover;
        Building garageBuilding = step.building;
        
        logger.finer(person + " location situation: " + person.getLocationSituation());
        garageBuilding.getInventory().retrieveUnit(person);
        BuildingManager.removePersonFromBuilding(person, garageBuilding);
        rover.getInventory().storeUnit(person);
        
        if (walkingStepIndex < (walkingSteps.getWalkingStepsNumber() - 1)) {
            walkingStepIndex++;
            setPhase(getWalkingStepPhase());
        }
        else {
            endTask();
        }
        
        return timeLeft;
    }

    @Override
    public int getEffectiveSkillLevel() {
        return 0;
    }

    @Override
    public List<SkillType> getAssociatedSkills() {
        return new ArrayList<SkillType>(0);
    }

    @Override
    protected void addExperience(double time) {
        // Do nothing
    }
}