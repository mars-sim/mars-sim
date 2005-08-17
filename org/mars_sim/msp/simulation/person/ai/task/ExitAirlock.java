/**
 * Mars Simulation Project
 * ExitAirlock.java
 * @version 2.78 2005-07-13
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
                inv.takeUnit(suit, person);
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
		person.getSkillManager().addExperience(Skill.EVA_OPERATIONS, evaExperience);
	}

    /**
     * Checks if a person can exit an airlock on an EVA.
     * @param person the person exiting
     * @param airlock the airlock to be used
     * @return true if person can exit the entity 
     */
    public static boolean canExitAirlock(Person person, Airlock airlock) {

        // Check if EVA suit is available.
        // return (goodEVASuitAvailable(airlock.getEntityInventory()));
        boolean result = goodEVASuitAvailable(airlock.getEntityInventory());
        if (!result) {
            EVASuit finalSuit = null;
        
            UnitIterator i = airlock.getEntityInventory().getUnitsOfClass(EVASuit.class).iterator();
            while (i.hasNext() && (finalSuit == null)) {
                EVASuit suit = (EVASuit) i.next();
                boolean fullyLoaded = suit.isFullyLoaded();
                boolean lifeSupport = suit.lifeSupportCheck();
                boolean malfunction = suit.getMalfunctionManager().hasMalfunction();
                if (fullyLoaded && lifeSupport && !malfunction) finalSuit = suit; 
                else {
                    // System.out.println("EVA Suit fullyLoaded: " + fullyLoaded);
                    // System.out.println("EVA Suit lifeSupport: " + lifeSupport);
                    // System.out.println("EVA Suit malfunction: " + malfunction);
                }
            }
        
            // if (finalSuit == null) System.out.println("ExitAirlock.getGoodEVASuit() false");
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
     * @param the inventory to check.
     * @return EVA suit or null if none available.
     */
    public static EVASuit getGoodEVASuit(Inventory inv) {

        EVASuit result = null;
        
        UnitIterator i = inv.getUnitsOfClass(EVASuit.class).iterator();
        while (i.hasNext() && (result == null)) {
            EVASuit suit = (EVASuit) i.next();
            boolean fullyLoaded = suit.isFullyLoaded();
            boolean lifeSupport = suit.lifeSupportCheck();
            boolean malfunction = suit.getMalfunctionManager().hasMalfunction();
            if (fullyLoaded && lifeSupport && !malfunction) result = suit; 
        }
        
        return result;
    }
    
	/**
	 * Gets the effective skill level a person has at this task.
	 * @return effective skill level
	 */
	public int getEffectiveSkillLevel() {
		SkillManager manager = person.getSkillManager();
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