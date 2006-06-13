/**
 * Mars Simulation Project
 * EnterAirlock.java
 * @version 2.79 2006-06-13
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import java.util.*;

import org.mars_sim.msp.simulation.Airlock;
import org.mars_sim.msp.simulation.Inventory;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.equipment.EVASuit;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.ai.Skill;
import org.mars_sim.msp.simulation.person.ai.SkillManager;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.SettlementIterator;
import org.mars_sim.msp.simulation.vehicle.Airlockable;
import org.mars_sim.msp.simulation.vehicle.Vehicle;
import org.mars_sim.msp.simulation.vehicle.VehicleIterator;

/** 
 * The EnterAirlock class is a task for entering a airlock from an EVA operation. 
 */
public class EnterAirlock extends Task implements Serializable {
	
	// Task phase
	private static final String ENTERING_AIRLOCK = "Entering Airlock";

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
    public EnterAirlock(Person person, Airlock airlock) throws Exception {
        super("Entering airlock from EVA", person, false, false, STRESS_MODIFIER, false, 0D);

        // Initialize data members
        description = "Entering " + airlock.getEntityName() + " from EVA";
        this.airlock = airlock;
        
        // Initialize task phase
        addPhase(ENTERING_AIRLOCK);
        setPhase(ENTERING_AIRLOCK);

        // System.out.println(person.getName() + " is starting to enter " + airlock.getEntityName());
    }

    /**
     * Constructs a EnterAirlock object without an airlock.
     * @param person the person to perform the task.
     * @throws Exception if erro constructing task.
     */
    public EnterAirlock(Person person) throws Exception {
        super("Entering airlock from EVA", person, false, false, STRESS_MODIFIER, false, 0D);
	
        // Determine airlock from other people on mission.
        if (person.getMind().getMission() != null) {
            PersonIterator i = person.getMind().getMission().getPeople().iterator();
            while (i.hasNext() && (airlock == null)) {
                Person p = i.next();
                if (p != person) {
                    String location = p.getLocationSituation();
                    if (location.equals(Person.INSETTLEMENT)) {
                        airlock = p.getSettlement().getAvailableAirlock();
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
            SettlementIterator i = Simulation.instance().getUnitManager().getSettlements().iterator();
            while (i.hasNext() && (airlock == null)) {
                Settlement settlement = i.next();
                if (person.getCoordinates().equals(settlement.getCoordinates())) 
                    airlock = settlement.getAvailableAirlock();
            }
        }

        // If not look for any airlockable vehicles at person's location.
        if (airlock == null) {
            VehicleIterator i = Simulation.instance().getUnitManager().getVehicles().iterator();
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
        if (airlock == null) endTask();
        else description = "Entering " + airlock.getEntityName() + " from EVA";
    }
   
    /** Returns the weighted probability that a person might perform this task.
     *  It should return a 0 if there is no chance to perform this task given the person and his/her situation.
     *  @param person the person to perform the task
     *  @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person) {
        double result = 0D;

        if (person.getLocationSituation().equals(Person.OUTSIDE)) result = 500D;

        return result;
    }
    
    /**
     * Performs the method mapped to the task's current phase.
     * @param time the amount of time (millisols) the phase is to be performed.
     * @return the remaining time (millisols) after the phase has been performed.
     * @throws Exception if error in performing phase or if phase cannot be found.
     */
    protected double performMappedPhase(double time) throws Exception {
    	if (getPhase() == null) throw new IllegalArgumentException("Task phase is null");
    	if (ENTERING_AIRLOCK.equals(getPhase())) return enteringAirlockPhase(time);
    	else return time;
    }
    
    /**
     * Performs the enter airlock phase of the task.
     * @param time the amount of time to perform the task.
     * @return
     * @throws Exception
     */
    private double enteringAirlockPhase(double time) throws Exception {
        // If person is in airlock, wait around.
        if (airlock.inAirlock(person)) {
            // Make sure airlock is activated.
            airlock.activateAirlock();
        }
        else {
            // If person is outside, try to enter airlock.
            if (person.getLocationSituation().equals(Person.OUTSIDE)) {
                if (airlock.isOuterDoorOpen()) airlock.enterAirlock(person, false);
            	else airlock.requestOpenDoor();
            }
            else {
                // If person is inside, put stuff away and end task.
                putAwayEVASuit();
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
     * Puts the person's EVA suite back into the entity's inventory.
     * EVA Suit is refilled with oxygen and water from the entity's inventory.
     * @throws Exception if error putting away suit.
     */
    private void putAwayEVASuit() throws Exception {
       
        EVASuit suit = (EVASuit) person.getInventory().findUnitOfClass(EVASuit.class);
        if (suit != null) {
        	Inventory suitInv = suit.getInventory();
        	Inventory personInv = person.getInventory();
        	Inventory entityInv = person.getContainerUnit().getInventory();

        	// Unload oxygen from suit.
        	double oxygenAmount = suitInv.getAmountResourceStored(AmountResource.OXYGEN);
        	double oxygenCapacity = entityInv.getAmountResourceRemainingCapacity(AmountResource.OXYGEN);
        	if (oxygenAmount > oxygenCapacity) oxygenAmount = oxygenCapacity;
        	suitInv.retrieveAmountResource(AmountResource.OXYGEN, oxygenAmount);
        	entityInv.storeAmountResource(AmountResource.OXYGEN, oxygenAmount);
        	
        	// Unload water from suit.
        	double waterAmount = suitInv.getAmountResourceStored(AmountResource.WATER);
        	double waterCapacity = entityInv.getAmountResourceRemainingCapacity(AmountResource.WATER);
        	if (waterAmount > waterCapacity) waterAmount = waterCapacity;
        	suitInv.retrieveAmountResource(AmountResource.WATER, waterAmount);
        	entityInv.storeAmountResource(AmountResource.WATER, waterAmount);

        	// Return suit to entity's inventory.
        	// System.out.println(person.getName() + " putting away EVA suit into " + entity.getName());
        	personInv.retrieveUnit(suit);
        	entityInv.storeUnit(suit);
        }
        else throw new Exception("Person doesn't have an EVA suit to put away.");
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