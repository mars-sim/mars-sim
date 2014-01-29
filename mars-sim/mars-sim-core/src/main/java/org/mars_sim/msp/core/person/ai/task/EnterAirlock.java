/**
 * Mars Simulation Project
 * EnterAirlock.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import org.mars_sim.msp.core.Airlock;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LocalBoundedObject;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.Skill;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Airlockable;
import org.mars_sim.msp.core.vehicle.Vehicle;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/** 
 * The EnterAirlock class is a task for entering a airlock from an EVA operation. 
 */
public class EnterAirlock extends Task implements Serializable {

    private static Logger logger = Logger.getLogger(EnterAirlock.class.getName());

    // Task phase
    private static final String ENTERING_AIRLOCK = "Entering Airlock from Outside";

    // Static members
    private static final double STRESS_MODIFIER = .5D; // The stress modified per millisol.

    // Data members
    private Airlock airlock; // The airlock to be used.

    /** 
     * Constructor
     * @param person the person to perform the task
     * @param airlock to be used.
     * @throws Exception if error constructing task.
     */
    public EnterAirlock(Person person, Airlock airlock) {
        super("Entering airlock from EVA", person, false, false, STRESS_MODIFIER, false, 0D);

        // Initialize data members
        setDescription("Entering " + airlock.getEntityName() + " from EVA");
        this.airlock = airlock;

        // Initialize task phase
        addPhase(ENTERING_AIRLOCK);
        setPhase(ENTERING_AIRLOCK);

        // Move person to random location outside airlock entity.
        movePersonOutsideAirlock();
        
        logger.fine(person.getName() + " is starting to enter " + airlock.getEntityName());
    }

    /**
     * Constructs a EnterAirlock object without an airlock.
     * @param person the person to perform the task.
     * @throws Exception if error constructing task.
     */
    public EnterAirlock(Person person) {
        super("Entering airlock from EVA", person, false, false, STRESS_MODIFIER, false, 0D);

        // Determine airlock from other people on mission.
        if (person.getMind().getMission() != null) {
            Iterator<Person> i = person.getMind().getMission().getPeople().iterator();
            while (i.hasNext() && (airlock == null)) {
                Person p = i.next();
                if (p != person) {
                    String location = p.getLocationSituation();
                    if (location.equals(Person.INSETTLEMENT)) {
                        airlock = p.getSettlement().getClosestAvailableAirlock(person);
                    }
                    else if (location.equals(Person.INVEHICLE)) {
                        Vehicle vehicle = p.getVehicle();
                        if (vehicle instanceof Airlockable) 
                            airlock = ((Airlockable) vehicle).getAirlock();
                    }
                }
            }
        }

        // If not look for any settlements at person's location.
        if (airlock == null) {
            Iterator<Settlement> i = Simulation.instance().getUnitManager().getSettlements().iterator();
            while (i.hasNext() && (airlock == null)) {
                Settlement settlement = i.next();
                if (person.getCoordinates().equals(settlement.getCoordinates())) 
                    airlock = settlement.getClosestAvailableAirlock(person);
            }
        }

        // If not look for any vehicles with airlocks at person's location.
        if (airlock == null) {
            Iterator<Vehicle> i = Simulation.instance().getUnitManager().getVehicles().iterator();
            while (i.hasNext() && (airlock == null)) {
                Vehicle vehicle = i.next();
                if (person.getCoordinates().equals(vehicle.getCoordinates())) {
                    if (vehicle instanceof Airlockable) 
                        airlock = ((Airlockable) vehicle).getAirlock();
                }
            }
        }

        // Initialize task phase
        addPhase(ENTERING_AIRLOCK);
        setPhase(ENTERING_AIRLOCK);

        // If still no airlock, end task.
        if (airlock == null) {
            endTask();
            logger.severe(person.getName() + " cannot find an airlock to enter.");
        }
        else {
            // Move person to random location outside airlock entity.
            movePersonOutsideAirlock();
            
            setDescription("Entering " + airlock.getEntityName() + " from EVA");
            logger.fine(person.getName() + " is starting to enter " + airlock.getEntityName());
        }
    }

    /** Returns the weighted probability that a person might perform this task.
     *  It should return a 0 if there is no chance to perform this task given the person and his/her situation.
     *  @param person the person to perform the task
     *  @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person) {
        double result = 0D;

        if (person.getLocationSituation().equals(Person.OUTSIDE)) {
            result = 1000D;
        }

        return result;
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
            
            // TODO: Add EVA walk to outside location.
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
            Point2D.Double boundedLocalPoint = LocalAreaUtil.getRandomInteriorLocation(entityBounds);
            Point2D.Double newLocation = LocalAreaUtil.getLocalRelativeLocation(boundedLocalPoint.getX(), 
                    boundedLocalPoint.getY(), entityBounds);
            
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
        if (ENTERING_AIRLOCK.equals(getPhase())) return enteringAirlockPhase(time);
        else return time;
    }

    /**
     * Performs the enter airlock phase of the task.
     * @param time the amount of time to perform the task phase.
     * @return remaining time after performing task phase.
     * @throws Exception if error performing task.
     */
    private double enteringAirlockPhase(double time) {
        double remainingTime = time;

        // Check if person is outside or in airlock.
        while ((person.getLocationSituation().equals(Person.OUTSIDE) || airlock.inAirlock(person)) 
                && (remainingTime > 0D)) {

            if (!airlock.inAirlock(person)) {
                
                // If airlock is depressurized and outer door unlocked, enter and activate airlock.
                if (Airlock.DEPRESSURIZED.equals(airlock.getState()) && !airlock.isOuterDoorLocked()) {
                    airlock.enterAirlock(person, false);
                }
                else {
                    // Add person to queue awaiting airlock at outer door if not already.
                    airlock.addAwaitingAirlockOuterDoor(person);
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

        // If person is inside, put stuff away and end task.
        if (!person.getLocationSituation().equals(Person.OUTSIDE) && !airlock.inAirlock(person)) {
            logger.fine(person.getName() + " successfully entered airlock of " + airlock.getEntityName());
            
            // Move person inside airlock entity.
            movePersonInsideAirlock();
            
            putAwayEVASuit();
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
     * Puts the person's EVA suite back into the entity's inventory.
     * EVA Suit is refilled with oxygen and water from the entity's inventory.
     * @throws Exception if error putting away suit.
     */
    private void putAwayEVASuit() {

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
    }

    /**
     * Checks if a person can enter an airlock from an EVA.
     *
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