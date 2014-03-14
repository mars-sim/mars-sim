/**
 * Mars Simulation Project
 * ExitAirlock.java
 * @version 3.06 2014-03-14
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Airlock;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Unit;
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
 * The ExitAirlock class is a task for exiting an airlock for an EVA operation.
 */
public class ExitAirlock
extends Task
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(ExitAirlock.class.getName());

	// Task phase
	private static final String PROCURING_EVA_SUIT = "Procuring EVA Suit";
	private static final String WAITING_TO_ENTER_AIRLOCK = "Waiting to Enter Airlock";
	private static final String ENTERING_AIRLOCK = "Entering Airlock";
	private static final String WAITING_INSIDE_AIRLOCK = "Waiting inside Airlock";
	private static final String EXITING_AIRLOCK = "Exiting Airlock";

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

	/** 
	 * Constructor.
	 * @param person the person to perform the task
	 * @param airlock the airlock to use.
	 */
	public ExitAirlock(Person person, Airlock airlock) {
        super("Exiting airlock for EVA", person, true, false, STRESS_MODIFIER, false, 0D);

        // Initialize data members
        setDescription("Exiting " + airlock.getEntityName() + " for EVA");
        this.airlock = airlock;

        // Initialize task phase
        addPhase(PROCURING_EVA_SUIT);
        addPhase(WAITING_TO_ENTER_AIRLOCK);
        addPhase(ENTERING_AIRLOCK);
        addPhase(WAITING_INSIDE_AIRLOCK);
        addPhase(EXITING_AIRLOCK);
        
        setPhase(PROCURING_EVA_SUIT);

        logger.fine(person.getName() + " is starting to exit airlock of " + airlock.getEntityName());
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
        
        logger.finer(person + " procuring EVA suit.");
        
        // Check if person already has EVA suit.
        if (!hasSuit && alreadyHasEVASuit()) {
            hasSuit = true;
        }
        
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
        
        return remainingTime;
    }
    
    /**
     * Performs the waiting to enter airlock phase of the task.
     * @param time the amount of time to perform the task phase.
     * @return the remaining time after performing the task phase.
     */
    private double waitingToEnterAirlockPhase(double time) {
        
        double remainingTime = time;
        
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
        
        logger.finer(person + " entering airlock.");
        
        if (insideAirlockPos == null) {
            insideAirlockPos = airlock.getAvailableAirlockPosition();
        }
        
        if (airlock.inAirlock(person)) {
            logger.finer(person + " is entering airlock, but is already in airlock.");
            setPhase(WAITING_INSIDE_AIRLOCK);
        }
        else if ((person.getXLocation() == insideAirlockPos.getX()) && 
                (person.getYLocation() == insideAirlockPos.getY())) {
            
            logger.finer(person + " is at inside airlock location.");
            
            // Enter airlock.
            if (airlock.enterAirlock(person, true)) {
            
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
            if (LocationSituation.OUTSIDE != person.getLocationSituation()) {
                
                // Walk to inside airlock position.
                if (airlock.getEntity() instanceof Building) {
                    double distance = Point2D.distance(person.getXLocation(), person.getYLocation(), 
                            insideAirlockPos.getX(), insideAirlockPos.getY());
                    logger.finer(person + " walking to inside airlock position, distance: " + distance);
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
            else {
                logger.finer(person + " is entering airlock, but is already inside.");
                setPhase(WAITING_INSIDE_AIRLOCK);
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
            logger.finer(person + " is already outside during waiting inside airlock phase.");
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
        
        logger.finer(person + " exiting airlock outside.");
        
        if (exteriorAirlockPos == null) {
            exteriorAirlockPos = airlock.getAvailableExteriorPosition();
        }
        
        if ((person.getXLocation() == exteriorAirlockPos.getX()) && 
                (person.getYLocation() == exteriorAirlockPos.getY())) {
            
            logger.finer(person + " has exited airlock outside.");
            endTask();
        }
        else {
            // Walk to exterior airlock position.
            addSubTask(new WalkOutside(person, person.getXLocation(), person.getYLocation(), 
                    exteriorAirlockPos.getX(), exteriorAirlockPos.getY(), true));
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

        // Experience points adjusted by person's "Experience Aptitude" attribute.
        NaturalAttributeManager nManager = person.getNaturalAttributeManager();
        int experienceAptitude = nManager.getAttribute(NaturalAttribute.EXPERIENCE_APTITUDE);
        double experienceAptitudeModifier = (((double) experienceAptitude) - 50D) / 100D;
        evaExperience += evaExperience * experienceAptitudeModifier;
        evaExperience *= getTeachingExperienceModifier();
        person.getMind().getSkillManager().addExperience(SkillType.EVA_OPERATIONS, evaExperience);
    }

    /**
     * Checks if a person can exit an airlock on an EVA.
     * @param person the person exiting
     * @param airlock the airlock to be used
     * @return true if person can exit the entity 
     */
    public static boolean canExitAirlock(Person person, Airlock airlock) {

        boolean result = true;
        
        // Check if EVA suit is available.
        if (!goodEVASuitAvailable(airlock.getEntityInventory())) {
            result = false;
        }
        
        // Check if person is incapacitated.
        if (person.getPerformanceRating() == 0D) {
            result = false;
        }
        
        return result;
    }

    /**
     * Checks if the person already has an EVA suit in their inventory.
     * @return true if person already has an EVA suit.
     */
    private boolean alreadyHasEVASuit() {
        boolean result = false;
        EVASuit suit = (EVASuit) person.getInventory().findUnitOfClass(EVASuit.class);
        if (suit != null) {
            result = true;
            logger.severe(person.getName() + " already has an EVA suit in inventory!");
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
        AmountResource oxygen = AmountResource.findAmountResource("oxygen");
        double neededOxygen = suitInv.getAmountResourceRemainingCapacity(oxygen, true, false);
        double availableOxygen = entityInv.getAmountResourceStored(oxygen, false);
        // Make sure there is enough extra oxygen for everyone else.
        availableOxygen -= (neededOxygen * otherPeopleNum);
        boolean hasEnoughOxygen = (availableOxygen >= neededOxygen);

        // Check if enough water.
        AmountResource water = AmountResource.findAmountResource("water");
        double neededWater = suitInv.getAmountResourceRemainingCapacity(water, true, false);
        double availableWater = entityInv.getAmountResourceStored(water, false);
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

        Inventory suitInv = suit.getInventory();
        Inventory entityInv = person.getContainerUnit().getInventory();

        // Fill oxygen in suit from entity's inventory. 
        AmountResource oxygen = AmountResource.findAmountResource("oxygen");
        double neededOxygen = suitInv.getAmountResourceRemainingCapacity(oxygen, true, false);
        double availableOxygen = entityInv.getAmountResourceStored(oxygen, false);
        double takenOxygen = neededOxygen;
        if (takenOxygen > availableOxygen) takenOxygen = availableOxygen;
        try {
            entityInv.retrieveAmountResource(oxygen, takenOxygen);
            suitInv.storeAmountResource(oxygen, takenOxygen, true);
        }
        catch (Exception e) {}

        // Fill water in suit from entity's inventory.
        AmountResource water = AmountResource.findAmountResource("water");
        double neededWater = suitInv.getAmountResourceRemainingCapacity(water, true, false);
        double availableWater = entityInv.getAmountResourceStored(water, false);
        double takenWater = neededWater;
        if (takenWater > availableWater) takenWater = availableWater;
        try {
            entityInv.retrieveAmountResource(water, takenWater);
            suitInv.storeAmountResource(water, takenWater, true);
        }
        catch (Exception e) {}
    }

    @Override
    public void endTask() {
        super.endTask();
        
        // Clear the person as the airlock operator if task ended prematurely.
        if ((airlock != null) && person.equals(airlock.getOperator())) {
            logger.severe(person + " ending exiting airlock task prematurely, " +
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