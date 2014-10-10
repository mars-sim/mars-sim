/**
 * Mars Simulation Project
 * Walk.java
 * @version 3.07 2014-10-10
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
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.equipment.EVASuit;
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

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.walk"); //$NON-NLS-1$

    /** Task phases. */
    private static final TaskPhase WALKING_SETTLEMENT_INTERIOR = new TaskPhase(Msg.getString(
            "Task.phase.walkingSettlementInterior")); //$NON-NLS-1$
    private static final TaskPhase WALKING_ROVER_INTERIOR = new TaskPhase(Msg.getString(
            "Task.phase.walkingRoverInterior")); //$NON-NLS-1$
    private static final TaskPhase WALKING_EXTERIOR = new TaskPhase(Msg.getString(
            "Task.phase.walkingExterior")); //$NON-NLS-1$
    private static final TaskPhase EXITING_AIRLOCK = new TaskPhase(Msg.getString(
            "Task.phase.exitingAirlock")); //$NON-NLS-1$
    private static final TaskPhase ENTERING_AIRLOCK = new TaskPhase(Msg.getString(
            "Task.phase.enteringAirlock")); //$NON-NLS-1$
    private static final TaskPhase EXITING_ROVER_GARAGE = new TaskPhase(Msg.getString(
            "Task.phase.exitingRoverGarage")); //$NON-NLS-1$
    private static final TaskPhase ENTERING_ROVER_GARAGE = new TaskPhase(Msg.getString(
            "Task.phase.enteringRoverGarage")); //$NON-NLS-1$

    // Data members
    private WalkingSteps walkingSteps;
    private Map<Integer, TaskPhase> walkingStepPhaseMap;
    private int walkingStepIndex;

    /**
     * Constructor.
     * @param person the person performing the task.
     */
    public Walk(Person person) {
        super(NAME, person, false, false, 0D, false, 0D);

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
            boolean walkToSettlement = false;
            if ((person.getMind().getMission() == null) && (vehicle.getSettlement() != null)) {

                Settlement settlement = vehicle.getSettlement();
                
                // Check if vehicle is in garage.
                Building garageBuilding = BuildingManager.getBuilding(vehicle);
                if (garageBuilding != null) {
                    
                    Point2D interiorPos = LocalAreaUtil.getRandomInteriorLocation(garageBuilding);
                    Point2D adjustedInteriorPos = LocalAreaUtil.getLocalRelativeLocation(
                            interiorPos.getX(), interiorPos.getY(), garageBuilding);
                    walkingSteps = new WalkingSteps(person, adjustedInteriorPos.getX(), 
                            adjustedInteriorPos.getY(), garageBuilding);
                    walkToSettlement = true;
                }
                else if (vehicle instanceof Rover) {
                    
                    // Check if person has a good EVA suit available if in a rover.
                    boolean goodEVASuit = true;
                    boolean roverSuit = ExitAirlock.goodEVASuitAvailable(vehicle.getInventory());
                    boolean wearingSuit = person.getInventory().containsUnitClass(EVASuit.class);
                    goodEVASuit = roverSuit || wearingSuit;
                    if (goodEVASuit) {
                        
                        // Walk to nearest emergency airlock in settlement.
                        Airlock airlock = settlement.getClosestAvailableAirlock(person);
                        if (airlock != null) {
                            LocalBoundedObject entity = (LocalBoundedObject) airlock.getEntity();
                            Point2D interiorPos = LocalAreaUtil.getRandomInteriorLocation(entity);
                            Point2D adjustedInteriorPos = LocalAreaUtil.getLocalRelativeLocation(
                                    interiorPos.getX(), interiorPos.getY(), entity);
                            walkingSteps = new WalkingSteps(person, adjustedInteriorPos.getX(), 
                                    adjustedInteriorPos.getY(), entity);
                            walkToSettlement = true;
                        }
                    }
                    
                }
                else {
                    // If not a rover, retrieve person from vehicle.
                    vehicle.getInventory().retrieveUnit(person);
                }
            }
            
            if (!walkToSettlement) {

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

        walkingStepPhaseMap = new HashMap<Integer, TaskPhase>(7);
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
     * @return walking step task phase.
     */
    private TaskPhase getWalkingStepPhase() {

        TaskPhase result = null;

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
        Point2D personLocation = new Point2D.Double(person.getXLocation(), person.getYLocation());
        Point2D stepLocation = new Point2D.Double(step.xLoc, step.yLoc);
        if (step.building.equals(building) && LocalAreaUtil.areLocationsClose(personLocation, stepLocation)) {
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

        Point2D personLocation = new Point2D.Double(person.getXLocation(), person.getYLocation());
        Point2D stepLocation = new Point2D.Double(step.xLoc, step.yLoc);
        if (step.rover.equals(rover) && LocalAreaUtil.areLocationsClose(personLocation, stepLocation)) {
            if (walkingStepIndex < (walkingSteps.getWalkingStepsNumber() - 1)) {
                walkingStepIndex++;
                setPhase(getWalkingStepPhase());
            }
            else {
                endTask();
            }
        }
        else {
            logger.finer("Starting walk rover interior from Walk.walkingRoverInteriorPhase.");
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
        Point2D personLocation = new Point2D.Double(person.getXLocation(), person.getYLocation());
        Point2D stepLocation = new Point2D.Double(step.xLoc, step.yLoc);
        if (LocalAreaUtil.areLocationsClose(personLocation, stepLocation)) {
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