/**
 * Mars Simulation Project
 * EnterAirlock.java
 * @version 2.75 2004-04-06
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;

import org.mars_sim.msp.simulation.Airlock;
import org.mars_sim.msp.simulation.Inventory;
import org.mars_sim.msp.simulation.Mars;
import org.mars_sim.msp.simulation.Resource;
import org.mars_sim.msp.simulation.Unit;
import org.mars_sim.msp.simulation.equipment.EVASuit;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.person.PersonIterator;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.SettlementIterator;
import org.mars_sim.msp.simulation.vehicle.Airlockable;
import org.mars_sim.msp.simulation.vehicle.Vehicle;
import org.mars_sim.msp.simulation.vehicle.VehicleIterator;

/** 
 * The EnterAirlock class is a task for entering a airlock from an EVA operation. 
 */
class EnterAirlock extends Task implements Serializable {

    // Data members
    private Airlock airlock; // The airlock to be used.

    /** 
     * Constructor
     *
     * @param person the person to perform the task
     * @param mars the virtual Mars
     * @param airlock to be used.
     */
    public EnterAirlock(Person person, Mars mars, Airlock airlock) {
        super("Entering airlock from EVA", person, false, false, mars);

        // Initialize data members
        description = "Entering " + airlock.getEntityName() + " from EVA";
        this.airlock = airlock;

        // System.out.println(person.getName() + " is starting to enter " + airlock.getEntityName());
    }

    /**
     * Constructs a EnterAirlock object without an airlock.
     *
     * @param person the person to perform the task.
     * @param mars the virtual Mars
     */
    public EnterAirlock(Person person, Mars mars) {
        super("Entering airlock from EVA", person, false, false, mars);

        // System.out.println("Enter Airlock due to strange situation.");
        // System.out.println("Illness: " + person.getPhysicalCondition().getHealthSituation());
        // System.out.println("Performance Rating: " + person.getPerformanceRating());
	
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
            SettlementIterator i = mars.getUnitManager().getSettlements().iterator();
            while (i.hasNext() && (airlock == null)) {
                Settlement settlement = i.next();
                if (person.getCoordinates().equals(settlement.getCoordinates())) 
                    airlock = settlement.getAvailableAirlock();
            }
        }

        // If not look for any airlockable vehicles at person's location.
        if (airlock == null) {
            VehicleIterator i = mars.getUnitManager().getVehicles().iterator();
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
     *  @param mars the virtual Mars
     *  @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person, Mars mars) {
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
}
