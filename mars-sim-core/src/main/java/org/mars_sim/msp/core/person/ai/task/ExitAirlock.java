/**
 * Mars Simulation Project
 * ExitAirlock.java
 * @version 3.04 2013-02-13
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import org.mars_sim.msp.core.Airlock;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LocalBoundedObject;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.Skill;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.resource.AmountResource;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/** 
 * The ExitAirlock class is a task for exiting an airlock for an EVA operation.
 */
public class ExitAirlock extends Task implements Serializable {

    private static Logger logger = Logger.getLogger(ExitAirlock.class.getName());

    // Task phase
    private static final String EXITING_AIRLOCK = "Exiting Airlock from Inside";	

    // Static members
    private static final double STRESS_MODIFIER = .5D; // The stress modified per millisol.

    // Data members
    private Airlock airlock; // The airlock to be used.
    private boolean hasSuit = false; // True if person has an EVA suit.

    /** 
     * Constructs an ExitAirlock object
     * @param person the person to perform the task
     * @param airlock the airlock to use.
     * @throws Exception if error constructing task.
     */
    public ExitAirlock(Person person, Airlock airlock) {
        super("Exiting airlock for EVA", person, true, false, STRESS_MODIFIER, false, 0D);

        // Initialize data members
        setDescription("Exiting " + airlock.getEntityName() + " for EVA");
        this.airlock = airlock;

        // Initialize task phase
        addPhase(EXITING_AIRLOCK);
        setPhase(EXITING_AIRLOCK);
        
        // Move the person to the inside of the airlock.
        movePersonInsideAirlock();

        logger.fine(person.getName() + " is starting to exit airlock of " + airlock.getEntityName());
    }
    
    /**
     * Move the person to the outside of the airlock.
     */
    private void movePersonOutsideAirlock() {
        
        // Move the person to a random location outside the airlock entity.
        if (airlock.getEntity() instanceof LocalBoundedObject) {
            LocalBoundedObject entityBounds = (LocalBoundedObject) airlock.getEntity();
            Point2D.Double newLocation = null;
            boolean goodLocation = false;
            for (int x = 0; (x < 20) && !goodLocation; x++) {
                Point2D.Double boundedLocalPoint = LocalAreaUtil.getRandomExteriorLocation(entityBounds, 1D);
                newLocation = LocalAreaUtil.getLocalRelativeLocation(boundedLocalPoint.getX(), 
                        boundedLocalPoint.getY(), entityBounds);
                goodLocation = LocalAreaUtil.checkLocationCollision(newLocation.getX(), newLocation.getY(), 
                        person.getCoordinates());
            }
            
            person.setXLocation(newLocation.getX());
            person.setYLocation(newLocation.getY());
        }
    }
    
    /**
     * Move the person to the inside of the airlock.
     */
    private void movePersonInsideAirlock() {
        
        // Move the person to a random location inside the airlock entity.
        if (airlock.getEntity() instanceof LocalBoundedObject) {
            LocalBoundedObject entityBounds = (LocalBoundedObject) airlock.getEntity();
            Point2D.Double newLocation = null;
            boolean goodLocation = false;
            for (int x = 0; (x < 20) && !goodLocation; x++) {
                Point2D.Double boundedLocalPoint = LocalAreaUtil.getRandomInteriorLocation(entityBounds);
                newLocation = LocalAreaUtil.getLocalRelativeLocation(boundedLocalPoint.getX(), 
                        boundedLocalPoint.getY(), entityBounds);
                goodLocation = LocalAreaUtil.checkLocationCollision(newLocation.getX(), newLocation.getY(), 
                        person.getCoordinates());
            }
            
            person.setXLocation(newLocation.getX());
            person.setYLocation(newLocation.getY());
        }
    }

    /**
     * Performs the method mapped to the task's current phase.
     * @param time the amount of time (millisols) the phase is to be performed.
     * @return the remaining time (millisols) after the phase has been performed.
     * @throws Exception if error in performing phase or if phase cannot be found.
     */
    protected double performMappedPhase(double time) {
        if (getPhase() == null) throw new IllegalArgumentException("Task phase is null");
        if (EXITING_AIRLOCK.equals(getPhase())) return exitingAirlockPhase(time);
        else return time;
    }

    /**
     * Performs the exit airlock phase of the task.
     * @param time the amount of time to perform the task phase.
     * @return the remaining time after performing the task phase.
     * @throws Exception if error performing task.
     */
    private double exitingAirlockPhase(double time) {

        double remainingTime = time;

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
            logger.info(person.getName() + " does not have an EVA suit, ExitAirlock ended");
            endTask();
            return time;
        }
        
        // Check if person isn't outside.
        while (!person.getLocationSituation().equals(Person.OUTSIDE) && (remainingTime > 0D)) {

            if (!airlock.inAirlock(person)) {
                
                // If airlock is pressurized and inner door unlocked, enter and activate airlock.
                if (Airlock.PRESSURIZED.equals(airlock.getState()) && !airlock.isInnerDoorLocked()) {
                    airlock.enterAirlock(person, true);
                }
                else {
                    // Add person to queue awaiting airlock at inner door if not already.
                    airlock.addAwaitingAirlockInnerDoor(person);    
                }
                
                // If airlock has not been activated, activate it.
                if (!airlock.isActivated()) {
                    airlock.activateAirlock(person);
                }
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
                // If person is not airlock operator, set remaining time to zero.
                remainingTime = 0D;
            }
        }
        
        // If person is outside, end task.
        if (person.getLocationSituation().equals(Person.OUTSIDE)) {
            logger.fine(person.getName() + " successfully exited airlock of " + airlock.getEntityName());
            
            // Move person to the outside of the airlock.
            movePersonOutsideAirlock();
            
            endTask();
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
        int experienceAptitude = nManager.getAttribute(NaturalAttributeManager.EXPERIENCE_APTITUDE);
        double experienceAptitudeModifier = (((double) experienceAptitude) - 50D) / 100D;
        evaExperience += evaExperience * experienceAptitudeModifier;
        evaExperience *= getTeachingExperienceModifier();
        person.getMind().getSkillManager().addExperience(Skill.EVA_OPERATIONS, evaExperience);
    }

    /**
     * Checks if a person can exit an airlock on an EVA.
     * @param person the person exiting
     * @param airlock the airlock to be used
     * @return true if person can exit the entity 
     */
    public static boolean canExitAirlock(Person person, Airlock airlock) {

        // Check if EVA suit is available.
        return (goodEVASuitAvailable(airlock.getEntityInventory()));
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
                if (!malfunction && hasEnoughResources) result = suit;
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
     * @throws Exception if problem loading supplies.
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
    
    /**
     * Gets the effective skill level a person has at this task.
     * @return effective skill level
     */
    public int getEffectiveSkillLevel() {
        SkillManager manager = person.getMind().getSkillManager();
        return manager.getEffectiveSkillLevel(Skill.EVA_OPERATIONS);
    }

    /**
     * Gets a list of the skills associated with this task.
     * May be empty list if no associated skills.
     * @return list of skills as strings
     */
    public List<String> getAssociatedSkills() {
        List<String> results = new ArrayList<String>(1);
        results.add(Skill.EVA_OPERATIONS);
        return results;
    }

    @Override
    public void destroy() {
        super.destroy();

        airlock = null;
    }
}