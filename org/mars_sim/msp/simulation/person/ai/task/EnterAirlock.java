/**
 * Mars Simulation Project
 * EnterAirlock.java
 * @version 2.77 2004-08-25
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.Airlock;
import org.mars_sim.msp.simulation.Inventory;
import org.mars_sim.msp.simulation.Resource;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.Unit;
import org.mars_sim.msp.simulation.equipment.EVASuit;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.SettlementIterator;
import org.mars_sim.msp.simulation.vehicle.Airlockable;
import org.mars_sim.msp.simulation.vehicle.Vehicle;
import org.mars_sim.msp.simulation.vehicle.VehicleIterator;

/** 
 * The EnterAirlock class is a task for entering a airlock from an EVA operation. 
 */
public class EnterAirlock extends Task implements Serializable {

	// Static members
	private static final double STRESS_MODIFIER = .5D; // The stress modified per millisol.

    // Data members
    private Airlock airlock; // The airlock to be used.

    /** 
     * Constructor
     *
     * @param person the person to perform the task
     * @param airlock to be used.
     */
    public EnterAirlock(Person person, Airlock airlock) {
        super("Entering airlock from EVA", person, false, false, STRESS_MODIFIER);

        // Initialize data members
        description = "Entering " + airlock.getEntityName() + " from EVA";
        this.airlock = airlock;

        // System.out.println(person.getName() + " is starting to enter " + airlock.getEntityName());
    }

    /**
     * Constructs a EnterAirlock object without an airlock.
     *
     * @param person the person to perform the task.
     */
    public EnterAirlock(Person person) {
        super("Entering airlock from EVA", person, false, false, STRESS_MODIFIER);
	
        // Determine airlock from other people on mission.
        if (person.getMind().getMission() != null) {
            PersonIterator i = person.getMind().getMission().getPeople().iterator();
            while (i.hasNext() && (airlock == null)) {
                Person p = i.next();
                if (p != person) {
                    String location = p.getLocationSituation();
                    if (location.equals(Person.INSETTLEMENT)) {
                        airlock = person.getSettlement().getAvailableAirlock();
                    }
                    else if (location.equals(Person.INVEHICLE)) {
                        Vehicle vehicle = person.getVehicle();
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
     * Performs this task for the given amount of time.
     * @param time the amount of time to perform this task (in millisols)
     * @return amount of time remaining after finishing with task (in millisols)
     * @throws Exception if error in performing task.
     */
    double performTask(double time) throws Exception {
        double timeLeft = super.performTask(time);
        if (subTask != null) return timeLeft;
        
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
        
		// Add experience to "EVA Operations" skill.
		// (1 base experience point per 100 millisols of time spent)
		double experience = time / 100D;
		
		// Experience points adjusted by person's "Experience Aptitude" attribute.
		NaturalAttributeManager nManager = person.getNaturalAttributeManager();
		experience += experience * (((double) nManager.getAttribute(NaturalAttributeManager.EXPERIENCE_APTITUDE) - 50D) / 100D);
		experience *= getTeachingExperienceModifier();
		person.getSkillManager().addExperience(Skill.EVA_OPERATIONS, experience);

        return 0D;
    }

    /**
     * Puts the person's EVA suite back into the entity's inventory.
     * EVA Suit is refilled with oxygen and water from the entity's inventory.
     */
    private void putAwayEVASuit() {
       
        EVASuit suit = (EVASuit) person.getInventory().findUnit(EVASuit.class);
        Inventory suitInv = suit.getInventory();
        Inventory personInv = person.getInventory();
        Unit entity = person.getContainerUnit();
        Inventory entityInv = entity.getInventory();

        // Refill oxygen in suit from entity's inventory. 
        double neededOxygen = suitInv.getResourceRemainingCapacity(Resource.OXYGEN);
        double takenOxygen = entityInv.removeResource(Resource.OXYGEN, neededOxygen);
        // System.out.println(person.getName() + " refilling EVA suit with " + takenOxygen + " oxygen.");
        suitInv.addResource(Resource.OXYGEN, takenOxygen);

        // Refill water in suit from entity's inventory.
        double neededWater = suitInv.getResourceRemainingCapacity(Resource.WATER);
        double takenWater = entityInv.removeResource(Resource.WATER, neededWater);
        // System.out.println(person.getName() + " refilling EVA suit with " + takenWater + " water.");
        suitInv.addResource(Resource.WATER, takenWater);

        // Return suit to entity's inventory.
        // System.out.println(person.getName() + " putting away EVA suit into " + entity.getName());
        personInv.takeUnit(suit, (Unit) entity);
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