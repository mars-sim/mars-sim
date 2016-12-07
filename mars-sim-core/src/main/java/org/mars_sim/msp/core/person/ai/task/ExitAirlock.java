/**
 * Mars Simulation Project
 * ExitAirlock.java
 * @version 3.08 2015-05-22
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Airlock;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LifeSupportType;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.NaturalAttribute;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RoboticAttribute;
import org.mars_sim.msp.core.robot.RoboticAttributeManager;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.BuildingAirlock;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * The ExitAirlock class is a task for exiting an airlock for an EVA operation.
 */
public class ExitAirlock
extends Task
implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** default logger. */
    private static Logger logger = Logger.getLogger(ExitAirlock.class.getName());

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.exitAirlock"); //$NON-NLS-1$

    /** Task phases. */
    private static final TaskPhase PROCURING_EVA_SUIT = new TaskPhase(Msg.getString(
            "Task.phase.procuringEVASuit")); //$NON-NLS-1$
    private static final TaskPhase WAITING_TO_ENTER_AIRLOCK = new TaskPhase(Msg.getString(
            "Task.phase.waitingToEnterAirlock")); //$NON-NLS-1$
    private static final TaskPhase ENTERING_AIRLOCK = new TaskPhase(Msg.getString(
            "Task.phase.enteringAirlock")); //$NON-NLS-1$
    private static final TaskPhase WAITING_INSIDE_AIRLOCK = new TaskPhase(Msg.getString(
            "Task.phase.waitingInsideAirlock")); //$NON-NLS-1$
    private static final TaskPhase EXITING_AIRLOCK = new TaskPhase(Msg.getString(
            "Task.phase.exitingAirlock")); //$NON-NLS-1$

    // Static members
    /** The stress modified per millisol. */
    private static final double STRESS_MODIFIER = .5D;

    // Data members
    /** The airlock to be used. */
    private Airlock airlock;
    /** True if person has an EVA suit. */
    private boolean hasSuit = false;

    private Point2D insideAirlockPos = null;
    private Point2D exteriorAirlockPos = null;

	protected static AmountResource oxygenAR = BuildingAirlock.oxygenAR;
	protected static AmountResource waterAR = BuildingAirlock.waterAR;

    //private Person person = null;
    //private Robot robot = null;

    /**
     * Constructor.
     * @param person the person to perform the task
     * @param airlock the airlock to use.
     */
    public ExitAirlock(Person person, Airlock airlock) {
        super(NAME, person, false, false, STRESS_MODIFIER, false, 0D);

        this.airlock = airlock;

        init();

        logger.fine(person.getName() + " is starting to exit airlock of " + airlock.getEntityName());
    }

    public ExitAirlock(Robot robot, Airlock airlock) {
        super(NAME, robot, false, false, STRESS_MODIFIER, false, 0D);

        this.airlock = airlock;

        init();

        logger.fine(robot.getName() + " is starting to exit airlock of " + airlock.getEntityName());
    }

    public void init() {
        // Initialize data members
        setDescription(Msg.getString("Task.description.exitAirlock.detail",
                airlock.getEntityName())); //$NON-NLS-1$
        // Initialize task phase
        addPhase(PROCURING_EVA_SUIT);
        addPhase(WAITING_TO_ENTER_AIRLOCK);
        addPhase(ENTERING_AIRLOCK);
        addPhase(WAITING_INSIDE_AIRLOCK);
        addPhase(EXITING_AIRLOCK);

        setPhase(PROCURING_EVA_SUIT);
    }

    /**
     * Performs the method mapped to the task's current phase.
     * @param time the amount of time (millisols) the phase is to be performed.
     * @return the remaining time (millisols) after the phase has been performed.
     */
    protected double performMappedPhase(double time) {
        if (getPhase() == null) {
            throw new IllegalArgumentException("Task phase is null");
        }
        else if (PROCURING_EVA_SUIT.equals(getPhase())) {
            return procuringEVASuit(time);
        }
        else if (WAITING_TO_ENTER_AIRLOCK.equals(getPhase())) {
            return waitingToEnterAirlockPhase(time);
        }
        else if (ENTERING_AIRLOCK.equals(getPhase())) {
            return enteringAirlockPhase(time);
        }
        else if (WAITING_INSIDE_AIRLOCK.equals(getPhase())) {
            return waitingInsideAirlockPhase(time);
        }
        else if (EXITING_AIRLOCK.equals(getPhase())) {
            return exitingAirlockPhase(time);
        }
        else {
            return time;
        }
    }

    /**
     * Performs the procuring EVA suit phase of the task.
     * @param time the amount of time to perform the task phase.
     * @return the remaining time after performing the task phase.
     */
    private double procuringEVASuit(double time) {

        double remainingTime = time;

        if (person != null) {
            // Check if person already has EVA suit.
            if (!hasSuit && alreadyHasEVASuit()) {
                hasSuit = true;
            }

            logger.finer(person + " procuring EVA suit.");

            // Get an EVA suit from entity inventory.
            if (!hasSuit) {
                Inventory inv = airlock.getEntityInventory();
                EVASuit suit = getGoodEVASuit(inv);
                if (suit != null) {
                    try {
                        inv.retrieveUnit(suit);
                        person.getInventory().storeUnit(suit);
                        loadEVASuit(suit);
                        hasSuit = true;
                    }
                    catch (Exception e) {
                        logger.severe(e.getMessage());
                    }
                }
            }

            // If person still doesn't have an EVA suit, end task.
            if (!hasSuit) {
                logger.finer(person.getName() + " does not have an EVA suit, ExitAirlock ended");
                endTask();
                return 0D;
            }
            else {
                setPhase(WAITING_TO_ENTER_AIRLOCK);
            }


            // Add experience
            addExperience(time - remainingTime);

        }
        else if (robot != null) {

        	// No need of procuring an EVA suit
        	setPhase(WAITING_TO_ENTER_AIRLOCK);

        }

        return remainingTime;
    }

    /**
     * Performs the waiting to enter airlock phase of the task.
     * @param time the amount of time to perform the task phase.
     * @return the remaining time after performing the task phase.
     */
    private double waitingToEnterAirlockPhase(double time) {

        double remainingTime = time;



        if (person != null) {
            logger.finer(person + " waiting to enter airlock.");

            // If person is already outside, change to exit airlock phase.
            if (LocationSituation.OUTSIDE == person.getLocationSituation()) {
                setPhase(EXITING_AIRLOCK);
                return remainingTime;
            }

            // If airlock is pressurized and inner door unlocked, enter airlock.
            if ((Airlock.PRESSURIZED.equals(airlock.getState()) && !airlock.isInnerDoorLocked()) ||
                    airlock.inAirlock(person)) {
                setPhase(ENTERING_AIRLOCK);
            }
            else {
                // Add person to queue awaiting airlock at inner door if not already.
                airlock.addAwaitingAirlockInnerDoor(person);

                // If airlock has not been activated, activate it.
                if (!airlock.isActivated()) {
                    airlock.activateAirlock(person);
                }

                // Check if person is the airlock operator.
                if (person.equals(airlock.getOperator())) {
                    // If person is airlock operator, add cycle time to airlock.
                    double activationTime = remainingTime;
                    if (airlock.getRemainingCycleTime() < remainingTime) {
                        remainingTime -= airlock.getRemainingCycleTime();
                    }
                    else {
                        remainingTime = 0D;
                    }
                    boolean activationSuccessful = airlock.addCycleTime(activationTime);
                    if (!activationSuccessful) {
                        logger.severe("Problem with airlock activation: " + person.getName());
                    }
                }
                else {
                    // If person is not airlock operator, just wait.
                    remainingTime = 0D;
                }
            }
        }
        else if (robot != null) {
            logger.finer(robot + " waiting to enter airlock.");

            // If person is already outside, change to exit airlock phase.
            if (LocationSituation.OUTSIDE == robot.getLocationSituation()) {
                setPhase(EXITING_AIRLOCK);
                return remainingTime;
            }

            // If inner door is unlocked, enter airlock.
            // TODO: Doesn't a robot need to wait for airlock to be pressurized ?
            if ((Airlock.PRESSURIZED.equals(airlock.getState()) && !airlock.isInnerDoorLocked()) ||
                    airlock.inAirlock(robot)) {
                setPhase(ENTERING_AIRLOCK);
            }
            else {
                // Add robot to queue awaiting airlock at inner door if not already.
                airlock.addAwaitingAirlockInnerDoor(robot);

                // If airlock has not been activated, activate it.
                if (!airlock.isActivated()) {
                    airlock.activateAirlock(robot);
                }

                // Check if robot is the airlock operator.
                if (robot.equals(airlock.getOperator())) {
                    // If robot is airlock operator, add cycle time to airlock.
                    double activationTime = remainingTime;
                    if (airlock.getRemainingCycleTime() < remainingTime) {
                        remainingTime -= airlock.getRemainingCycleTime();
                    }
                    else {
                        remainingTime = 0D;
                    }
                    boolean activationSuccessful = airlock.addCycleTime(activationTime);
                    if (!activationSuccessful) {
                        logger.severe("Problem with airlock activation: " + robot.getName());
                    }
                }
                else {
                    // If robot is not airlock operator, just wait.
                    remainingTime = 0D;
                }
            }
        }



        // Add experience
        addExperience(time - remainingTime);

        return remainingTime;
    }

    /**
     * Performs the entering airlock phase of the task.
     * @param time the amount of time to perform the task phase.
     * @return the remaining time after performing the task phase.
     */
    private double enteringAirlockPhase(double time) {

        double remainingTime = time;

        if (insideAirlockPos == null) {
            insideAirlockPos = airlock.getAvailableAirlockPosition();
        }

		if (person != null) {
		       logger.finer(person + " entering airlock.");

		        Point2D personLocation = new Point2D.Double(person.getXLocation(), person.getYLocation());

		        if (airlock.inAirlock(person)) {
		            logger.finer(person + " is entering airlock, but is already in airlock.");
		            setPhase(WAITING_INSIDE_AIRLOCK);
		        }
		        else if (person.getLocationSituation() == LocationSituation.OUTSIDE) {
		            logger.finer(person + " is entering airlock, but is already outside.");
		            endTask();
		        }
		        else if (LocalAreaUtil.areLocationsClose(personLocation,  insideAirlockPos)) {

		            //logger.finer(person + " is at inside airlock location.");
		            logger.finer(person + " has arrived at an airlock and ready to enter.");

		            // Enter airlock.
		            if (airlock.enterAirlock(person, true)) {

		                // If airlock has not been activated, activate it.
		                if (!airlock.isActivated()) {
		                    airlock.activateAirlock(person);
		                }

		                logger.finer(person + " has just entered airlock");

		                setPhase(WAITING_INSIDE_AIRLOCK);
		            }
		            else {
		                // If airlock has not been activated, activate it.
		                if (!airlock.isActivated()) {
		                    airlock.activateAirlock(person);
		                }

		                // Check if person is the airlock operator.
		                if (person.equals(airlock.getOperator())) {
		                    // If person is airlock operator, add cycle time to airlock.
		                    double activationTime = remainingTime;
		                    if (airlock.getRemainingCycleTime() < remainingTime) {
		                        remainingTime -= airlock.getRemainingCycleTime();
		                    }
		                    else {
		                        remainingTime = 0D;
		                    }
		                    boolean activationSuccessful = airlock.addCycleTime(activationTime);
		                    if (!activationSuccessful) {
		                        logger.severe("Problem with airlock activation: " + person.getName());
		                    }
		                }
		                else {
		                    // If person is not airlock operator, just wait.
		                    remainingTime = 0D;
		                }
		            }
		        }
		        else {
		            if (LocationSituation.OUTSIDE != person.getLocationSituation()) {

		                // Walk to inside airlock position.
		                if (airlock.getEntity() instanceof Building) {
		                    double distance = Point2D.distance(person.getXLocation(), person.getYLocation(),
		                            insideAirlockPos.getX(), insideAirlockPos.getY());
		                    //logger.finer(person + " walking to inside airlock position, distance: " + distance);
		                    logger.finer(person + " is walking toward an airlock within a distance of " + distance);
		                    Building airlockBuilding = (Building) airlock.getEntity();
		                    addSubTask(new WalkSettlementInterior(person, airlockBuilding,
		                            insideAirlockPos.getX(), insideAirlockPos.getY()));
		                }
		                else if (airlock.getEntity() instanceof Rover) {

		                    Rover airlockRover = (Rover) airlock.getEntity();

		                    addSubTask(new WalkRoverInterior(person, airlockRover,
		                            insideAirlockPos.getX(), insideAirlockPos.getY()));
		                }
		            }
		        }
		}
		else if (robot != null) {
		       logger.finer(robot + " entering airlock.");

		        Point2D robotLocation = new Point2D.Double(robot.getXLocation(), robot.getYLocation());

		        if (airlock.inAirlock(robot)) {
		            logger.finer(robot + " is entering airlock, but is already in airlock.");
		            setPhase(WAITING_INSIDE_AIRLOCK);
		        }
		        else if (robot.getLocationSituation() == LocationSituation.OUTSIDE) {
		            logger.finer(robot + " is entering airlock, but is already outside.");
		            endTask();
		        }
		        else if (LocalAreaUtil.areLocationsClose(robotLocation,  insideAirlockPos)) {

		            //logger.finer(robot + " is at inside airlock location.");
		            logger.finer(robot + " has arrived at an airlock and ready to enter.");

		            // Enter airlock.
		            if (airlock.enterAirlock(robot, true)) {

		                // If airlock has not been activated, activate it.
		                if (!airlock.isActivated()) {
		                    airlock.activateAirlock(robot);
		                }

		                logger.finer(robot + " has just entered airlock");

		                setPhase(WAITING_INSIDE_AIRLOCK);
		            }
		            else {
		                // If airlock has not been activated, activate it.
		                if (!airlock.isActivated()) {
		                    airlock.activateAirlock(robot);
		                }

		                // Check if robot is the airlock operator.
		                if (robot.equals(airlock.getOperator())) {
		                    // If robot is airlock operator, add cycle time to airlock.
		                    double activationTime = remainingTime;
		                    if (airlock.getRemainingCycleTime() < remainingTime) {
		                        remainingTime -= airlock.getRemainingCycleTime();
		                    }
		                    else {
		                        remainingTime = 0D;
		                    }
		                    boolean activationSuccessful = airlock.addCycleTime(activationTime);
		                    if (!activationSuccessful) {
		                        logger.severe("Problem with airlock activation: " + robot.getName());
		                    }
		                }
		                else {
		                    // If robot is not airlock operator, just wait.
		                    remainingTime = 0D;
		                }
		            }
		        }
		        else {
		            if (LocationSituation.OUTSIDE != robot.getLocationSituation()) {

		                // Walk to inside airlock position.
		                if (airlock.getEntity() instanceof Building) {
		                    double distance = Point2D.distance(robot.getXLocation(), robot.getYLocation(),
		                            insideAirlockPos.getX(), insideAirlockPos.getY());
		                    //logger.finer(robot + " walking to inside airlock position, distance: " + distance);
		                    logger.finer(robot + " is walking toward an airlock within a distance of " + distance);
		                    Building airlockBuilding = (Building) airlock.getEntity();
		                    addSubTask(new WalkSettlementInterior(robot, airlockBuilding,
		                            insideAirlockPos.getX(), insideAirlockPos.getY()));
		                }
		                else if (airlock.getEntity() instanceof Rover) {

		                    Rover airlockRover = (Rover) airlock.getEntity();

		                    addSubTask(new WalkRoverInterior(robot, airlockRover,
		                            insideAirlockPos.getX(), insideAirlockPos.getY()));
		                }
		            }
		        }
		}



        // Add experience
        addExperience(time - remainingTime);

        return remainingTime;
    }

    /**
     * Performs the waiting inside airlock phase of the task.
     * @param time the amount of time to perform the task phase.
     * @return the remaining time after performing the task phase.
     */
    private double waitingInsideAirlockPhase(double time) {

        double remainingTime = time;

        if (person != null) {
 
            if (airlock.inAirlock(person)) {

                // Check if person is the airlock operator.
                if (person.equals(airlock.getOperator())) {

                    // If airlock has not been activated, activate it.
                    if (!airlock.isActivated()) {
                        logger.finer(person + " is the operator activating the airlock.");                    
                        airlock.activateAirlock(person);                     
                    }

                    // If person is airlock operator, add cycle time to airlock.
                    double activationTime = remainingTime;
                    if (airlock.getRemainingCycleTime() < remainingTime) {
                        remainingTime -= airlock.getRemainingCycleTime();
                    }
                    else {
                        remainingTime = 0D;
                    }
                    boolean activationSuccessful = airlock.addCycleTime(activationTime);
                    if (!activationSuccessful) {
                        logger.severe("Problem with airlock activation: " + person.getName());
                    }
                }
                else {
                    // If person is not airlock operator, just wait.
                    logger.finer(person+ " is not the operator and is waiting inside an airlock for the completion of the air cycle."); 
                    remainingTime = 0D;
                }
            }
            else {
                //logger.finer(robot + " is already outside during waiting inside airlock phase.");
                logger.finer(robot + " is no longer inside the airlock.");
                setPhase(EXITING_AIRLOCK);
            }

        }
        else if (robot != null) {
  
            if (airlock.inAirlock(robot)) {

                 // Check if robot is the airlock operator.
                if (robot.equals(airlock.getOperator())) {

                    // If airlock has not been activated, activate it.
                    if (!airlock.isActivated()) {
                        logger.finer(robot + " is the operator activating the airlock.");
                        airlock.activateAirlock(robot);
                    }

                    // If robot is airlock operator, add cycle time to airlock.
                    double activationTime = remainingTime;
                    if (airlock.getRemainingCycleTime() < remainingTime) {
                        remainingTime -= airlock.getRemainingCycleTime();
                    }
                    else {
                        remainingTime = 0D;
                    }
                    boolean activationSuccessful = airlock.addCycleTime(activationTime);
                    if (!activationSuccessful) {
                        logger.severe("Problem with airlock activation: " + robot.getName());
                    }
                }
                else {
                    // If robot is not airlock operator, just wait.
                    logger.finer(robot + " is not the operator and is waiting inside an airlock for the completion of the air cycle."); 
                	remainingTime = 0D;
                }
            }
            else {
                //logger.finer(robot + " is already outside during waiting inside airlock phase.");
                logger.finer(robot + " is no longer inside the airlock.");
                setPhase(EXITING_AIRLOCK);
            }

        }

        // Add experience
        addExperience(time - remainingTime);

        return remainingTime;
    }

    /**
     * Performs the exit airlock phase of the task.
     * @param time the amount of time to perform the task phase.
     * @return the remaining time after performing the task phase.
     */
    private double exitingAirlockPhase(double time) {

        double remainingTime = time;

        if (person != null) {
            logger.finer(person + " is exiting airlock going outside.");

            if (LocationSituation.OUTSIDE != person.getLocationSituation()) {
                throw new IllegalStateException(person + " has exited airlock of " + airlock.getEntityName() +
                        " but is not outside.");
            }

            if (exteriorAirlockPos == null) {
                exteriorAirlockPos = airlock.getAvailableExteriorPosition();
            }

            Point2D personLocation = new Point2D.Double(person.getXLocation(), person.getYLocation());
            if (LocalAreaUtil.areLocationsClose(personLocation, exteriorAirlockPos)) {

                logger.finer(person + " has exited airlock going outside.");
                endTask();
            }
            else {
                // TODO: why addSubTask below? should we throw new IllegalStateException
            	// Walk to exterior airlock position.
                addSubTask(new WalkOutside(person, person.getXLocation(), person.getYLocation(),
                        exteriorAirlockPos.getX(), exteriorAirlockPos.getY(), true));
            }

        }
        else if (robot != null) {
            logger.finer(robot + " is exiting airlock going outside.");

            if (LocationSituation.OUTSIDE != robot.getLocationSituation()) {
                throw new IllegalStateException(robot + " has exited airlock of " + airlock.getEntityName() +
                        " but is not outside.");
            }

            if (exteriorAirlockPos == null) {
                exteriorAirlockPos = airlock.getAvailableExteriorPosition();
            }

            Point2D robotLocation = new Point2D.Double(robot.getXLocation(), robot.getYLocation());
            if (LocalAreaUtil.areLocationsClose(robotLocation, exteriorAirlockPos)) {

                logger.finer(robot + " has exited airlock going outside.");
                endTask();
            }
            else {
                // TODO: why addSubTask below? should we throw new IllegalStateException
                // Walk to exterior airlock position.
                addSubTask(new WalkOutside(robot, robot.getXLocation(), robot.getYLocation(),
                        exteriorAirlockPos.getX(), exteriorAirlockPos.getY(), true));
            }

        }


        // Add experience
        addExperience(time - remainingTime);

        return remainingTime;
    }

    /**
     * Adds experience to the person's skills used in this task.
     * @param time the amount of time (ms) the person performed this task.
     */
    protected void addExperience(double time) {

        // Add experience to "EVA Operations" skill.
        // (1 base experience point per 100 millisols of time spent)
        double evaExperience = time / 100D;

        if (person != null) {
            // Experience points adjusted by person's "Experience Aptitude" attribute.
            NaturalAttributeManager nManager = person.getNaturalAttributeManager();
            int experienceAptitude = nManager.getAttribute(NaturalAttribute.EXPERIENCE_APTITUDE);
            double experienceAptitudeModifier = (((double) experienceAptitude) - 50D) / 100D;
            evaExperience += evaExperience * experienceAptitudeModifier;
            evaExperience *= getTeachingExperienceModifier();
            person.getMind().getSkillManager().addExperience(SkillType.EVA_OPERATIONS, evaExperience);

        }
        else if (robot != null) {
            // Experience points adjusted by robot's "Experience Aptitude" attribute.
            RoboticAttributeManager nManager = robot.getRoboticAttributeManager();
            int experienceAptitude = nManager.getAttribute(RoboticAttribute.EXPERIENCE_APTITUDE);
            double experienceAptitudeModifier = (((double) experienceAptitude) - 50D) / 100D;
            evaExperience += evaExperience * experienceAptitudeModifier;
            evaExperience *= getTeachingExperienceModifier();
            robot.getBotMind().getSkillManager().addExperience(SkillType.EVA_OPERATIONS, evaExperience);

        }

    }

    /**
     * Checks if a person can exit an airlock on an EVA.
     * @param person the person exiting
     * @param airlock the airlock to be used
     * @return true if person can exit the entity
     */
    public static boolean canExitAirlock(Person person, Airlock airlock) {

        boolean result = true;

		// Check if person is outside.
        if (person.getLocationSituation().equals(LocationSituation.OUTSIDE)) {
            result = false;
            logger.severe(person.getName() + " cannot exit airlock from " + airlock.getEntityName() +
                    " since he/she is already outside.");
        }

        // Check if EVA suit is available.
        else if (!goodEVASuitAvailable(airlock.getEntityInventory())) {
            result = false;
            logger.severe(person.getName() + " cannot exit airlock from " + airlock.getEntityName() +
                    " since no EVA suit is available.");
        }

        //double performance = person.getPerformanceRating();
        // Check if person is incapacitated.
        // TODO: if incapacitated, should someone else help this person to get out?
        else if (person.getPerformanceRating() == 0) {
        	
        	result = false;
        	// TODO: how to prevent the logger statement below from being repeated multiple times?
        	logger.severe(person.getName() + " cannot exit airlock from " + airlock.getEntityName() +
                " due to crippling performance rating");
        
            // 2016-02-28 Calling getNewAction(true, false) so as not to get "stuck" inside the airlock.
            try {
            	logger.info(person.getName() + " is nearly abandoning the action of exiting the airlock and switching to a new task");
            	// 2016-10-07 Note: calling getNewAction() below is still considered "experimental" 
            	// It may have caused StackOverflowError if a very high fatigue person is stranded in the airlock and cannot go outside.
            	// Intentionally add a 5% performance boost
            	person.getPhysicalCondition().setPerformanceFactor(.05);
            	person.getMind().getNewAction(true, false);
            	
            } catch (Exception e) {
                logger.log(Level.WARNING, person + " could not get new action", e);
                e.printStackTrace(System.err);

            }
        }

        return result;
    }

    public static boolean canExitAirlock(Robot robot, Airlock airlock) {

        boolean result = true;

		// Check if robot is outside.
        if (robot.getLocationSituation().equals(LocationSituation.OUTSIDE)) {
            result = false;
            logger.severe(robot.getName() + " cannot exit airlock from " + airlock.getEntityName() +
                    " due to already being outside.");
        }

        // Check if EVA suit is available.
        //if (!goodEVASuitAvailable(airlock.getEntityInventory())) {
        //    result = false;
        //    logger.severe(robot.getName() + " cannot exit airlock from " + airlock.getEntityName() +
        //            " due to not able to find good EVA suit.");
        //}

        // Check if robot is incapacitated.
        else if (robot.getPerformanceRating() == 0D) {
            result = false;
            logger.severe(robot.getName() + " cannot exit airlock from " + airlock.getEntityName() +
                    " due to performance rating is 0 (low battery, malfunctioned, etc.).");
        }

        return result;
    }

    /**
     * Checks if the person already has an EVA suit in their inventory.
     * @return true if person already has an EVA suit.
     */
    private boolean alreadyHasEVASuit() {
        boolean result = false;
        if (person != null) {
            EVASuit suit = (EVASuit) person.getInventory().findUnitOfClass(EVASuit.class);
            if (suit != null) {
                result = true;
                logger.severe(person.getName() + " already has an EVA suit in inventory!");
            }
        }
        else if (robot != null) {

        }

        return result;
    }

    /**
     * Checks if a good EVA suit is in entity inventory.
     * @param inv the inventory to check.
     * @return true if good EVA suit is in inventory
     */
    public static boolean goodEVASuitAvailable(Inventory inv) {
        return (getGoodEVASuit(inv) != null);
    }

    /**
     * Gets a good EVA suit from an inventory.
     *
     * @param inv the inventory to check.
     * @return EVA suit or null if none available.
     */
    public static EVASuit getGoodEVASuit(Inventory inv) {

        EVASuit result = null;

        Iterator<Unit> i = inv.findAllUnitsOfClass(EVASuit.class).iterator();
        while (i.hasNext() && (result == null)) {
            EVASuit suit = (EVASuit) i.next();
            boolean malfunction = suit.getMalfunctionManager().hasMalfunction();
            try {
                boolean hasEnoughResources = hasEnoughResourcesForSuit(inv, suit);
                if (!malfunction && hasEnoughResources) {
                    result = suit;
                }
            }
            catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }

        return result;
    }

    /**
     * Checks if entity unit has enough resource supplies to fill the EVA suit.
     * @param entityInv the entity unit.
     * @param suit the EVA suit.
     * @return true if enough supplies.
     * @throws Exception if error checking suit resources.
     */
    private static boolean hasEnoughResourcesForSuit(Inventory entityInv, EVASuit suit) {

        Inventory suitInv = suit.getInventory();
        int otherPeopleNum = entityInv.findNumUnitsOfClass(Person.class) - 1;

        // Check if enough oxygen.
        //AmountResource oxygenAR = AmountResource.findAmountResource(LifeSupportType.OXYGEN);
        double neededOxygen = suitInv.getAmountResourceRemainingCapacity(oxygenAR, true, false);
        double availableOxygen = entityInv.getAmountResourceStored(oxygenAR, false);
        // Make sure there is enough extra oxygen for everyone else.
        availableOxygen -= (neededOxygen * otherPeopleNum);
        boolean hasEnoughOxygen = (availableOxygen >= neededOxygen);

        // Check if enough water.
        //AmountResource waterAR = AmountResource.findAmountResource(LifeSupportType.WATER);
        double neededWater = suitInv.getAmountResourceRemainingCapacity(waterAR, true, false);
        double availableWater = entityInv.getAmountResourceStored(waterAR, false);
        // Make sure there is enough extra water for everyone else.
        availableWater -= (neededWater * otherPeopleNum);
        boolean hasEnoughWater = (availableWater >= neededWater);

        return hasEnoughOxygen && hasEnoughWater;
    }

    /**
     * Loads an EVA suit with resources from the container unit.
     * @param suit the EVA suit.
     */
    private void loadEVASuit(EVASuit suit) {

    	if (person != null) {

            Inventory suitInv = suit.getInventory();
            Inventory entityInv = person.getContainerUnit().getInventory();

            // Fill oxygen in suit from entity's inventory.
            //AmountResource oxygen = AmountResource.findAmountResource(LifeSupportType.OXYGEN);
            double neededOxygen = suitInv.getAmountResourceRemainingCapacity(oxygenAR, true, false);
            double availableOxygen = entityInv.getAmountResourceStored(oxygenAR, false);

        	// 2015-01-09 Added addDemandTotalRequest()
            entityInv.addAmountDemandTotalRequest(oxygenAR);

            double takenOxygen = neededOxygen;
            if (takenOxygen > availableOxygen) takenOxygen = availableOxygen;
            try {
                entityInv.retrieveAmountResource(oxygenAR, takenOxygen);
            	// 2015-01-09 addDemandRealUsage()
                entityInv.addAmountDemand(oxygenAR, takenOxygen);
                suitInv.storeAmountResource(oxygenAR, takenOxygen, true);
                // not calling addSupplyAmount()
            }
            catch (Exception e) {}

            // Fill water in suit from entity's inventory.
            //AmountResource waterAR = AmountResource.findAmountResource(LifeSupportType.WATER);
            double neededWater = suitInv.getAmountResourceRemainingCapacity(waterAR, true, false);
            double availableWater = entityInv.getAmountResourceStored(waterAR, false);
        	// 2015-01-09 Added addDemandTotalRequest()
            entityInv.addAmountDemandTotalRequest(waterAR);

            double takenWater = neededWater;
            if (takenWater > availableWater) takenWater = availableWater;
            try {
                entityInv.retrieveAmountResource(waterAR, takenWater);
            	// 2015-01-09 addDemandRealUsage()
                entityInv.addAmountDemand(waterAR, takenWater);
                suitInv.storeAmountResource(waterAR, takenWater, true);
                // not calling addSupplyAmount()
            }
            catch (Exception e) {}
    	}
    	else if (robot != null) {

    	}

    }

    @Override
    public void endTask() {
        super.endTask();

        if (person != null) {
        	  // Clear the person as the airlock operator if task ended prematurely.
            if ((airlock != null) && person.equals(airlock.getOperator())) {
                logger.severe(person + " ending exiting airlock task prematurely, " +
                        "clearing as airlock operator for " + airlock.getEntityName());
                airlock.clearOperator();
            }
        }
        else if (robot != null) {
        	  // Clear the robot as the airlock operator if task ended prematurely.
            if ((airlock != null) && robot.equals(airlock.getOperator())) {
                logger.severe(robot + " ending exiting airlock task prematurely, " +
                        "clearing as airlock operator for " + airlock.getEntityName());
                airlock.clearOperator();
            }
        }

    }

    @Override
    public int getEffectiveSkillLevel() {
	    SkillManager manager = null;
    	if (person != null) {
    		manager = person.getMind().getSkillManager();
    	}
    	else if (robot != null) {
    	     manager = robot.getBotMind().getSkillManager();
    	}

        return manager.getEffectiveSkillLevel(SkillType.EVA_OPERATIONS);
    }

    @Override
    public List<SkillType> getAssociatedSkills() {
        List<SkillType> results = new ArrayList<SkillType>(1);
        results.add(SkillType.EVA_OPERATIONS);
        return results;
    }

    @Override
    public void destroy() {
        super.destroy();

        airlock = null;
    }
}