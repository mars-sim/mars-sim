/**
 * Mars Simulation Project
 * EVAOperation.java
 * @version 2.76 2004-05-05
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;

import org.mars_sim.msp.simulation.Airlock;
import org.mars_sim.msp.simulation.Inventory;
import org.mars_sim.msp.simulation.Mars;
import org.mars_sim.msp.simulation.RandomUtil;
import org.mars_sim.msp.simulation.Resource;
import org.mars_sim.msp.simulation.equipment.EVASuit;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.vehicle.Airlockable;
import org.mars_sim.msp.simulation.vehicle.Vehicle;

/** 
 * The EVAOperation class is an abstract task that involves an extra vehicular activity. 
 */
abstract class EVAOperation extends Task implements Serializable {

    // Task phase names
    protected static final String EXIT_AIRLOCK = "Exit Airlock";
    protected static final String ENTER_AIRLOCK = "Enter Airlock";
    
	// Static members
	private static final double STRESS_MODIFIER = .5D; // The stress modified per millisol.
    
    // Data members
    protected boolean exitedAirlock;  // Person has exited the airlock.
    protected boolean enteredAirlock; // Person has entered the airlock.
	
    /** 
     * Constructs a EVAOperation object.
     * @param name the name of the task
     * @param person the person to perform the task
     * @param mars the virtual Mars
     */
    public EVAOperation(String name, Person person, Mars mars) { 
        super(name, person, true, false, STRESS_MODIFIER, mars);
    }

    /**
     * Perform the exit airlock phase of the task.
     *
     * @param time the time to perform this phase (in millisols)
     * @param the airlock
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
                addSubTask(new ExitAirlock(person, mars, airlock));
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
                addSubTask(new EnterAirlock(person, mars, airlock));
                return 0D;
            }
            else {
                endTask();
                throw new Exception(person.getName() + " unable to enter airlock of " + airlock.getEntityName());
            }
        }
        else {
            // System.out.println(person.getName() + " entering airlock of " + airlock.getEntityName());
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
        
        // Check if it is night time. 
        if (mars.getSurfaceFeatures().getSurfaceSunlight(person.getCoordinates()) == 0) {
            // System.out.println(person.getName() + " should end EVA: night time.");
            result = true;
        }

        EVASuit suit = (EVASuit) person.getInventory().findUnit(EVASuit.class);
        if (suit == null) {
            // System.out.println(person.getName() + " should end EVA: No EVA suit found.");
            return true;
        }
        Inventory suitInv = suit.getInventory();
	
        // Check if EVA suit is at 15% of its oxygen capacity.
        double oxygenCap = suitInv.getResourceCapacity(Resource.OXYGEN);
        double oxygen = suitInv.getResourceMass(Resource.OXYGEN);
        if (oxygen <= (oxygenCap * .15D)) {
            // System.out.println(person.getName() + " should end EVA: EVA suit oxygen level less than 15%");	
            result = true;
        }

        // Check if EVA suit is at 15% of its water capacity.
        double waterCap = suitInv.getResourceCapacity(Resource.WATER);
        double water = suitInv.getResourceMass(Resource.WATER);
        if (water <= (waterCap * .15D)) {
            // System.out.println(person.getName() + " should end EVA: EVA suit water level less than 15%");	
            result = true;
        }

        // Check if life support system in suit is working properly.
        if (!suit.lifeSupportCheck()) {
            // System.out.println(person.getName() + " should end EVA: EVA suit failed life support check.");	
            result = true;
        }

        // Check if suit has any malfunctions.
        if (suit.getMalfunctionManager().hasMalfunction()) {
            // System.out.println(person.getName() + " should end EVA: EVA suit has malfunction.");	
            result = true;
        }
	
        // Check if person's medical condition is sufficient to continue phase.
        if (person.getPerformanceRating() < .5D) {
            // System.out.println(person.getName() + " should end EVA: medical problems.");	
            result = true;
        }
	
        return result;
    }

    /**
     * Check for accident with EVA suit.
     * @param time the amount of time on EVA (in millisols)
     */
    protected void checkForAccident(double time) {

        EVASuit suit = (EVASuit) person.getInventory().findUnit(EVASuit.class);
        if (suit != null) {
	    
            double chance = .001D;

            // EVA operations skill modification.
            int skill = person.getSkillManager().getEffectiveSkillLevel(Skill.EVA_OPERATIONS);
            if (skill <= 3) chance *= (4 - skill);
            else chance /= (skill - 2);

            if (RandomUtil.lessThanRandPercent(chance * time)) {
                // System.out.println(person.getName() + " has accident during EVA operation.");
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