/**
 * Mars Simulation Project
 * ExitAirlock.java
 * @version 2.79 2006-06-13
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.Airlock;
import org.mars_sim.msp.simulation.Inventory;
import org.mars_sim.msp.simulation.UnitIterator;
import org.mars_sim.msp.simulation.equipment.EVASuit;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.ai.Skill;
import org.mars_sim.msp.simulation.person.ai.SkillManager;
import org.mars_sim.msp.simulation.resource.AmountResource;

/** 
 * The ExitAirlock class is a task for exiting a airlock from an EVA operation.
 */
public class ExitAirlock extends Task implements Serializable {

	// Task phase
	private static final String EXITING_AIRLOCK = "Exiting Airlock";	
	
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
    public ExitAirlock(Person person, Airlock airlock) throws Exception {
        super("Exiting airlock for EVA", person, true, false, STRESS_MODIFIER, false, 0D);

        // Initialize data members
        description = "Exiting " + airlock.getEntityName() + " for EVA";
        this.airlock = airlock;
        
        // Initialize task phase
        addPhase(EXITING_AIRLOCK);
        setPhase(EXITING_AIRLOCK);

        // System.out.println(person.getName() + " is starting to exit airlock of " + airlock.getEntityName());
    }
    
    /**
     * Performs the method mapped to the task's current phase.
     * @param time the amount of time (millisols) the phase is to be performed.
     * @return the remaining time (millisols) after the phase has been performed.
     * @throws Exception if error in performing phase or if phase cannot be found.
     */
    protected double performMappedPhase(double time) throws Exception {
    	if (getPhase() == null) throw new IllegalArgumentException("Task phase is null");
    	if (EXITING_AIRLOCK.equals(getPhase())) return exitingAirlockPhase(time);
    	else return time;
    }
    
    /**
     * Performs the enter airlock phase of the task.
     * @param time the amount of time to perform the task.
     * @return
     * @throws Exception
     */
    private double exitingAirlockPhase(double time) throws Exception {
    	
        // Get an EVA suit from entity inventory.
        if (!hasSuit) {
            Inventory inv = airlock.getEntityInventory();
            EVASuit suit = getGoodEVASuit(inv);
            if (suit != null) {
                inv.retrieveUnit(suit);
                person.getInventory().storeUnit(suit);
                loadEVASuit(suit);
                hasSuit = true;
            }
        }

        // If person still doesn't have an EVA suit, end task.
        if (!hasSuit) {
            // System.out.println(person.getName() + " does not have an EVA suit, ExitAirlock ended");
            endTask();
            return time;
        }

        // If person is in airlock, wait around.
        if (airlock.inAirlock(person)) {
            // Make sure airlock is activated.
            airlock.activateAirlock();
        }
        else {
            // If person is in entity, try to enter airlock.
            if (!person.getLocationSituation().equals(Person.OUTSIDE)) {
                if (airlock.isInnerDoorOpen()) airlock.enterAirlock(person, true);
                else airlock.requestOpenDoor();
            }
            else {
                // If person is outside, end task.
                endTask();
            }
        }
        
		// Add experience
        addExperience(time);

        return 0D;
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
     * @param the inventory to check.
     * @return EVA suit or null if none available.
     */
    public static EVASuit getGoodEVASuit(Inventory inv) {

        EVASuit result = null;
        
        UnitIterator i = inv.findAllUnitsOfClass(EVASuit.class).iterator();
        while (i.hasNext() && (result == null)) {
            EVASuit suit = (EVASuit) i.next();
            boolean malfunction = suit.getMalfunctionManager().hasMalfunction();
            boolean hasEnoughResources = hasEnoughResourcesForSuit(inv, suit);
            if (!malfunction && hasEnoughResources) result = suit;
        }
        
        return result;
    }
    
    /**
     * Checks if entity unit has enough resource supplies to fill the EVA suit.
     * @param entityInv the entity unit.
     * @param suit the EVA suit.
     * @return true if enough supplies.
     */
    private static boolean hasEnoughResourcesForSuit(Inventory entityInv, EVASuit suit) {
    	
    	Inventory suitInv = suit.getInventory();
    	
    	// Check if enough oxygen.
    	double neededOxygen = suitInv.getAmountResourceRemainingCapacity(AmountResource.OXYGEN);
    	double availableOxygen = entityInv.getAmountResourceStored(AmountResource.OXYGEN);
    	boolean hasEnoughOxygen = (availableOxygen >= neededOxygen);
    	
    	// Check if enough water.
    	double neededWater = suitInv.getAmountResourceRemainingCapacity(AmountResource.WATER);
    	double availableWater = entityInv.getAmountResourceStored(AmountResource.WATER);
    	boolean hasEnoughWater = (availableWater >= neededWater);
    	
    	return hasEnoughOxygen && hasEnoughWater;
    }
    
    /**
     * Loads an EVA suit with resources from the container unit.
     * @param suit the EVA suit.
     * @throws Exception if problem loading supplies.
     */
    private void loadEVASuit(EVASuit suit) throws Exception {
    	
    	Inventory suitInv = suit.getInventory();
    	Inventory entityInv = person.getContainerUnit().getInventory();
    	
    	// Fill oxygen in suit from entity's inventory. 
    	double neededOxygen = suitInv.getAmountResourceRemainingCapacity(AmountResource.OXYGEN);
    	double availableOxygen = entityInv.getAmountResourceStored(AmountResource.OXYGEN);
    	double takenOxygen = neededOxygen;
    	if (takenOxygen > availableOxygen) takenOxygen = availableOxygen;
    	entityInv.retrieveAmountResource(AmountResource.OXYGEN, takenOxygen);
    	suitInv.storeAmountResource(AmountResource.OXYGEN, takenOxygen);

    	// Fill water in suit from entity's inventory.
    	double neededWater = suitInv.getAmountResourceRemainingCapacity(AmountResource.WATER);
    	double availableWater = entityInv.getAmountResourceStored(AmountResource.WATER);
    	double takenWater = neededWater;
    	if (takenWater > availableWater) takenWater = availableWater;
    	entityInv.retrieveAmountResource(AmountResource.WATER, takenWater);
    	suitInv.storeAmountResource(AmountResource.WATER, takenWater);
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
	public List getAssociatedSkills() {
		List results = new ArrayList();
		results.add(Skill.EVA_OPERATIONS);
		return results;
	}
}