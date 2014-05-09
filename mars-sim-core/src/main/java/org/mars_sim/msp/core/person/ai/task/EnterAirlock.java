/**
 * Mars Simulation Project
 * EnterAirlock.java
 * @version 3.06 2014-05-09
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Airlock;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.NaturalAttribute;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.vehicle.Rover;

/** 
 * The EnterAirlock class is a task for entering a airlock from an EVA operation. 
 */
public class EnterAirlock
extends Task
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(EnterAirlock.class.getName());

	// TODO Task phase should be an enum
	private static final String WAITING_TO_ENTER_AIRLOCK = "Waiting to Enter Airlock";
	private static final String ENTERING_AIRLOCK = "Entering Airlock";
	private static final String WAITING_INSIDE_AIRLOCK = "Waiting inside Airlock";
	private static final String EXITING_AIRLOCK = "Exiting Airlock";
	private static final String STORING_EVA_SUIT = "Storing EVA Suit";

	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = .5D;

	// Data members
	/** The airlock to be used. */
	private Airlock airlock;
	private Point2D insideAirlockPos = null;
	private Point2D interiorAirlockPos = null;

	/** 
	 * Constructor.
	 * @param person the person to perform the task
	 * @param airlock to be used.
	 */
	public EnterAirlock(Person person, Airlock airlock) {
        super("Entering airlock from EVA", person, false, false, STRESS_MODIFIER, false, 0D);

        // Initialize data members
        setDescription("Entering " + airlock.getEntityName() + " from EVA");
        this.airlock = airlock;

        // Initialize task phase
        addPhase(WAITING_TO_ENTER_AIRLOCK);
        addPhase(ENTERING_AIRLOCK);
        addPhase(WAITING_INSIDE_AIRLOCK);
        addPhase(EXITING_AIRLOCK);
        addPhase(STORING_EVA_SUIT);
        
        setPhase(WAITING_TO_ENTER_AIRLOCK);
        
        logger.fine(person.getName() + " is starting to enter " + airlock.getEntityName());
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
        else if (STORING_EVA_SUIT.equals(getPhase())) {
            return storingEVASuitPhase(time);
        }
        else {
            return time;
        }
    }
    
    /**
     * Performs the waiting to enter airlock phase of the task.
     * @param time the amount of time to perform the task phase.
     * @return the remaining time after performing the task phase.
     */
    private double waitingToEnterAirlockPhase(double time) {
        
        double remainingTime = time;
        
        logger.finer(person + " waiting to enter airlock from outside.");
        
        // If person is already inside, change to exit airlock phase.
        if (LocationSituation.OUTSIDE != person.getLocationSituation()) {
            setPhase(EXITING_AIRLOCK);
            return remainingTime;
        }
        
        // If airlock is depressurized and outer door unlocked, enter airlock.
        if ((Airlock.DEPRESSURIZED.equals(airlock.getState()) && !airlock.isOuterDoorLocked()) || 
                airlock.inAirlock(person)) {
            setPhase(ENTERING_AIRLOCK);
        }
        else {
            // Add person to queue awaiting airlock at inner door if not already.
            airlock.addAwaitingAirlockOuterDoor(person); 
            
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
        
        // Add experience
        addExperience(time - remainingTime);
        
        return remainingTime;
    }

    /**
     * Performs the enter airlock phase of the task.
     * @param time the amount of time to perform the task phase.
     * @return remaining time after performing task phase.
     */
    private double enteringAirlockPhase(double time) {
        double remainingTime = time;
        
        logger.finer(person + " entering airlock from outside.");
        
        if (insideAirlockPos == null) {
            insideAirlockPos = airlock.getAvailableAirlockPosition();
        }
        
        Point2D personLocation = new Point2D.Double(person.getXLocation(), person.getYLocation());
        
        if (airlock.inAirlock(person)) {
            logger.finer(person + " is entering airlock, but is already in airlock.");
            setPhase(WAITING_INSIDE_AIRLOCK);
        }
        else if (person.getLocationSituation() != LocationSituation.OUTSIDE) {
            logger.finer(person + " is entering airlock, but is already inside.");
            endTask();
        }
        else if (LocalAreaUtil.areLocationsClose(personLocation, insideAirlockPos)) {
            
            // Enter airlock.
            if (airlock.enterAirlock(person, false)) {

                // If airlock has not been activated, activate it.
                if (!airlock.isActivated()) {
                    airlock.activateAirlock(person);
                }

                logger.finer(person + " has entered airlock");

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
            if (LocationSituation.OUTSIDE == person.getLocationSituation()) {
            
                // Walk to inside airlock position.
                addSubTask(new WalkOutside(person, person.getXLocation(), person.getYLocation(), 
                        insideAirlockPos.getX(), insideAirlockPos.getY(), true));
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
        
        logger.finer(person + " waiting inside airlock.");
        
        if (airlock.inAirlock(person)) {
            
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
        else {
            logger.finer(person + " is already internal during waiting inside airlock phase.");
            setPhase(EXITING_AIRLOCK);
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
        
        logger.finer(person + " exiting airlock inside.");
        
        if (interiorAirlockPos == null) {
            interiorAirlockPos = airlock.getAvailableInteriorPosition();
        }
        
        Point2D personLocation = new Point2D.Double(person.getXLocation(), person.getYLocation());
        if (LocalAreaUtil.areLocationsClose(personLocation, interiorAirlockPos)) {
            
            logger.finer(person + " has exited airlock inside.");
            
            setPhase(STORING_EVA_SUIT);
        }
        else {
            
            // Walk to interior airlock position.
            if (airlock.getEntity() instanceof Building) {
                
                Building airlockBuilding = (Building) airlock.getEntity();
                logger.finest(person + " exiting airlock inside " + airlockBuilding);
                addSubTask(new WalkSettlementInterior(person, airlockBuilding, 
                        interiorAirlockPos.getX(), interiorAirlockPos.getY()));
            }
            else if (airlock.getEntity() instanceof Rover) {
                
                Rover airlockRover = (Rover) airlock.getEntity();
                logger.finest(person + " exiting airlock inside " + airlockRover);
                addSubTask(new WalkRoverInterior(person, airlockRover, 
                        interiorAirlockPos.getX(), interiorAirlockPos.getY()));
            }
        }

        // Add experience
        addExperience(time - remainingTime);

        return remainingTime;
    }
    
    /**
     * Performs the storing EVA suit phase of the task.
     * @param time the amount of time to perform the task phase.
     * @return the remaining time after performing the task phase.
     */
    private double storingEVASuitPhase(double time) {
        
        double remainingTime = time;
        
        logger.finer(person + " storing EVA suit");
        
        // Store EVA suit in settlement or rover.
        EVASuit suit = (EVASuit) person.getInventory().findUnitOfClass(EVASuit.class);
        if (suit != null) {
            Inventory suitInv = suit.getInventory();
            Inventory personInv = person.getInventory();
            Inventory entityInv = person.getContainerUnit().getInventory();

            // Unload oxygen from suit.
            AmountResource oxygen = AmountResource.findAmountResource("oxygen");
            double oxygenAmount = suitInv.getAmountResourceStored(oxygen, false);
            double oxygenCapacity = entityInv.getAmountResourceRemainingCapacity(oxygen, true, false);
            if (oxygenAmount > oxygenCapacity) oxygenAmount = oxygenCapacity;
            try {
                suitInv.retrieveAmountResource(oxygen, oxygenAmount);
                entityInv.storeAmountResource(oxygen, oxygenAmount, true);
            }
            catch (Exception e) {}

            // Unload water from suit.
            AmountResource water = AmountResource.findAmountResource("water");
            double waterAmount = suitInv.getAmountResourceStored(water, false);
            double waterCapacity = entityInv.getAmountResourceRemainingCapacity(water, true, false);
            if (waterAmount > waterCapacity) waterAmount = waterCapacity;
            try {
                suitInv.retrieveAmountResource(water, waterAmount);
                entityInv.storeAmountResource(water, waterAmount, true);
            }
            catch (Exception e) {}

            // Return suit to entity's inventory.
            // logger.info(person.getName() + " putting away EVA suit into " + entity.getName());
            personInv.retrieveUnit(suit);
            entityInv.storeUnit(suit);
        }
        else {
            logger.severe(person.getName() + " doesn't have an EVA suit to put away.");
        }
        
        endTask();
        
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

        // Experience points adjusted by person's "Experience Aptitude" attribute.
        NaturalAttributeManager nManager = person.getNaturalAttributeManager();
        int experienceAptitude = nManager.getAttribute(NaturalAttribute.EXPERIENCE_APTITUDE);
        double experienceAptitudeModifier = (((double) experienceAptitude) - 50D) / 100D;
        evaExperience += evaExperience * experienceAptitudeModifier;
        evaExperience *= getTeachingExperienceModifier();
        person.getMind().getSkillManager().addExperience(SkillType.EVA_OPERATIONS, evaExperience);
    }

    /**
     * Checks if a person can enter an airlock from an EVA.
     * @param person the person trying to enter
     * @param airlock the airlock to be used.
     * @return true if person can enter the airlock 
     */
    public static boolean canEnterAirlock(Person person, Airlock airlock) {
        return true;
    }
    
    @Override
    public void endTask() {
        super.endTask();
        
        // Clear the person as the airlock operator if task ended prematurely.
        if ((airlock != null) && person.equals(airlock.getOperator())) {
            logger.severe(person + " ending entering airlock task prematurely, " +
                    "clearing as airlock operator for " + airlock.getEntityName());
            airlock.clearOperator();
        }
    }

    @Override
    public int getEffectiveSkillLevel() {
        SkillManager manager = person.getMind().getSkillManager();
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