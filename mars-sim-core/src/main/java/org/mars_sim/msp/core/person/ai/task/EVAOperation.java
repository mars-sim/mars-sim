/**
 * Mars Simulation Project
 * EVAOperation.java
 * @version 2.90 2010-01-20
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;

import org.mars_sim.msp.core.Airlock;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.mars.Mars;
import org.mars_sim.msp.core.person.*;
import org.mars_sim.msp.core.person.ai.Skill;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Airlockable;
import org.mars_sim.msp.core.vehicle.Vehicle;

/** 
 * The EVAOperation class is an abstract task that involves an extra vehicular activity. 
 */
public abstract class EVAOperation extends Task implements Serializable {

    // Task phase names
    protected static final String EXIT_AIRLOCK = "Exit Airlock";
    protected static final String ENTER_AIRLOCK = "Enter Airlock";
    
	// Static members
	private static final double STRESS_MODIFIER = .5D; // The stress modified per millisol.
	public static final double BASE_ACCIDENT_CHANCE = .001; // The base chance of an accident per millisol.
    
    // Data members
    protected boolean exitedAirlock;  // Person has exited the airlock.
    protected boolean enteredAirlock; // Person has entered the airlock.
    private boolean endEVA;           // Flag for ending EVA operation externally. 
    protected Unit containerUnit;        // The unit that is being exited for EVA.
	
    /** 
     * Constructor
     * @param name the name of the task
     * @param person the person to perform the task
     * @throws Exception if task could not be constructed.
     */
    public EVAOperation(String name, Person person) throws Exception { 
        super(name, person, true, false, STRESS_MODIFIER, false, 0D);
        
        // Initialize data members
        exitedAirlock = false;
        enteredAirlock = false;
        containerUnit = person.getTopContainerUnit();
        
        // Add task phases.
        addPhase(EXIT_AIRLOCK);
        addPhase(ENTER_AIRLOCK);
        
        // Set initial phase.
        setPhase(EXIT_AIRLOCK);
    }
    
    public void endEVA() {
    	endEVA = true;
    }

    /**
     * Perform the exit airlock phase of the task.
     *
     * @param time the time to perform this phase (in millisols)
     * @param airlock the airlock
     * @return the time remaining after performing this phase (in millisols)
     * @throws Exception if person cannot exit through the airlock.
     */
    protected double exitAirlock(double time, Airlock airlock) throws Exception {

        if (person.getLocationSituation().equals(Person.OUTSIDE)) {
            // System.out.printl(person.getName() + " exiting airlock of " + airlock.getEntityName());
            exitedAirlock = true;
            return time;
        }
        else {
            if (ExitAirlock.canExitAirlock(person, airlock)) {
                addSubTask(new ExitAirlock(person, airlock));
                return 0D;
            }
            else {
                endTask();
                throw new Exception(person.getName() + " unable to exit airlock of " + airlock.getEntityName());
            }
        }
    }

    /**
     * Perform the enter airlock phase of the task.
     *
     * @param time the time to perform this phase (in millisols)
     * @param airlock the airlock
     * @return the time remaining after performing this phase (in millisols)
     * @throws Exception if person cannot enter the airlock.
     */
    protected double enterAirlock(double time, Airlock airlock) throws Exception {

        if (person.getLocationSituation().equals(Person.OUTSIDE)) {
            if (EnterAirlock.canEnterAirlock(person, airlock)) {
                addSubTask(new EnterAirlock(person, airlock));
                return 0D;
            }
            else {
                endTask();
                throw new Exception(person.getName() + " unable to enter airlock of " + airlock.getEntityName());
            }
        }
        else {
            // logger.info(person.getName() + " entering airlock of " + airlock.getEntityName());
            enteredAirlock = true;
            return time;
        }
    }

    /**
     * Checks if situation requires the EVA operation to end prematurely 
     * and the person should return to the airlock.
     * @return true if EVA operation should end
     */
    protected boolean shouldEndEVAOperation() {

        boolean result = false;
        
        // Check end EVA flag.
        if (endEVA) result = true;
        
        // Check if it is night time. 
        Mars mars = Simulation.instance().getMars();
        if (mars.getSurfaceFeatures().getSurfaceSunlight(person.getCoordinates()) == 0) {
            // logger.info(person.getName() + " should end EVA: night time.");
            if (!mars.getSurfaceFeatures().inDarkPolarRegion(person.getCoordinates()))
            	result = true;
        }

        EVASuit suit = (EVASuit) person.getInventory().findUnitOfClass(EVASuit.class);
        if (suit == null) {
            // logger.info(person.getName() + " should end EVA: No EVA suit found.");
            return true;
        }
        Inventory suitInv = suit.getInventory();
	
        try {
        	// Check if EVA suit is at 15% of its oxygen capacity.
        	AmountResource oxygenResource = AmountResource.findAmountResource("oxygen");
        	double oxygenCap = suitInv.getAmountResourceCapacity(oxygenResource);
        	double oxygen = suitInv.getAmountResourceStored(oxygenResource);
        	if (oxygen <= (oxygenCap * .15D)) {
        		// logger.info(person.getName() + " should end EVA: EVA suit oxygen level less than 15%");	
        		result = true;
        	}

        	// Check if EVA suit is at 15% of its water capacity.
        	AmountResource waterResource = AmountResource.findAmountResource("water");
        	double waterCap = suitInv.getAmountResourceCapacity(waterResource);
        	double water = suitInv.getAmountResourceStored(waterResource);
        	if (water <= (waterCap * .15D)) {
        		// logger.info(person.getName() + " should end EVA: EVA suit water level less than 15%");	
        		result = true;
        	}

        	// Check if life support system in suit is working properly.
        	if (!suit.lifeSupportCheck()) {
        		// logger.info(person.getName() + " should end EVA: EVA suit failed life support check.");	
        		result = true;
        	}
        }
        catch (Exception e) {
        	e.printStackTrace(System.err);
        }

        // Check if suit has any malfunctions.
        if (suit.getMalfunctionManager().hasMalfunction()) {
            // logger.info(person.getName() + " should end EVA: EVA suit has malfunction.");	
            result = true;
        }
	
        // Check if person's medical condition is sufficient to continue phase.
        if (person.getPerformanceRating() < .5D) {
            // logger.info(person.getName() + " should end EVA: medical problems.");	
            result = true;
        }
	
        return result;
    }

    /**
     * Check for accident with EVA suit.
     * @param time the amount of time on EVA (in millisols)
     */
    protected void checkForAccident(double time) {

        EVASuit suit = (EVASuit) person.getInventory().findUnitOfClass(EVASuit.class);
        if (suit != null) {
	    
            double chance = BASE_ACCIDENT_CHANCE;

            // EVA operations skill modification.
            int skill = person.getMind().getSkillManager().getEffectiveSkillLevel(Skill.EVA_OPERATIONS);
            if (skill <= 3) chance *= (4 - skill);
            else chance /= (skill - 2);

            // Modify based on the suit's wear condition.
            chance *= suit.getMalfunctionManager().getWearConditionAccidentModifier();
            
            if (RandomUtil.lessThanRandPercent(chance * time)) {
                // logger.info(person.getName() + " has accident during EVA operation.");
                suit.getMalfunctionManager().accident();
            }
        }
    }
    
    /**
     * Gets an available airlock for a person.
     *
     * @return airlock or null if none available
     */
    public static Airlock getAvailableAirlock(Person person) {
        Airlock result = null;
        String location = person.getLocationSituation();
        
        if (location.equals(Person.INSETTLEMENT)) {
            Settlement settlement = person.getSettlement();
            result = settlement.getAvailableAirlock();
        }
        else if (location.equals(Person.INVEHICLE)) {
            Vehicle vehicle = person.getVehicle();
            if (vehicle instanceof Airlockable) 
                result = ((Airlockable) vehicle).getAirlock();
        }
        
        return result;
    }
}