/**
 * Mars Simulation Project
 * EatMeal.java
 * @version 2.75 2003-04-27
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.simulation.Mars;
import org.mars_sim.msp.simulation.RandomUtil;
import org.mars_sim.msp.simulation.SimulationProperties;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.Building;
import org.mars_sim.msp.simulation.structure.building.BuildingException;
import org.mars_sim.msp.simulation.structure.building.InhabitableBuilding;
import org.mars_sim.msp.simulation.structure.building.function.Dining;

/** The EatMeal class is a task for eating a meal.
 *  The duration of the task is 20 millisols.
 *
 *  Note: Eating a meal reduces hunger to 0.
 */
class EatMeal extends Task implements Serializable {

    // Data members
    private double duration = 20D; // The predetermined duration of task in millisols

    /** Constructs a EatMeal object
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     */
    public EatMeal(Person person, Mars mars) {
        super("Eating a meal", person, false, mars);
        
        String location = person.getLocationSituation();
        
        // If person is in a settlement, try to find a dining area.
        if (location.equals(Person.INSETTLEMENT)) {
            Dining diningroom = getAvailableDiningBuilding(person);
            InhabitableBuilding building = (InhabitableBuilding) diningroom;
            
            if (diningroom != null) {
                try {
                    if (!building.containsPerson(person)) building.addPerson(person);
                }
                catch (BuildingException e) {
                    System.out.println("EatMeal: " + e.getMessage());
                }
            }
            else {
                // Add stress increase later.
            }
        }
        else if (location.equals(Person.OUTSIDE)) endTask();
    }

    /** Returns the weighted probability that a person might perform this task.
     *
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     *  @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person, Mars mars) {

        double result = person.getPhysicalCondition().getHunger() - 250D;
        if (result < 0D) result = 0D;
        
        if (person.getLocationSituation().equals(Person.OUTSIDE)) result = 0D;
	
        return result;
    }

    /** This task allows the person to eat for the duration.
     *  @param time the amount of time to perform this task (in millisols)
     *  @return amount of time remaining after finishing with task (in millisols)
     */
    double performTask(double time) {
        double timeLeft = super.performTask(time);
        if (subTask != null) return timeLeft;

        person.setHunger(0D);
        timeCompleted += time;
        if (timeCompleted > duration) {
            SimulationProperties properties = mars.getSimulationProperties();
            person.consumeFood(properties.getPersonFoodConsumption() * (1D / 3D));
            endTask();
            return timeCompleted - duration;
        }
        else return 0D;
    }
    
    /**
     * Gets an available dining building that the person can use.
     * Returns null if no dining building is currently available.
     *
     * @param person the person
     * @return available dining building
     */
    private static Dining getAvailableDiningBuilding(Person person) {
     
        Dining result = null;
     
        String location = person.getLocationSituation();
        if (location.equals(Person.INSETTLEMENT)) {
            Settlement settlement = person.getSettlement();
            List dininglist = new ArrayList();
            Iterator i = settlement.getBuildingManager().getBuildings(Dining.class).iterator();
            while (i.hasNext()) {
                Dining dining = (Dining) i.next();
                boolean malfunction = ((Building) dining).getMalfunctionManager().hasMalfunction();
                if (!malfunction) dininglist.add(dining);
            }
            
            if (dininglist.size() > 0) {
                // Pick random dining building from list.
                int rand = RandomUtil.getRandomInt(dininglist.size() - 1);
                result = (Dining) dininglist.get(rand);
            }
        }
        
        return result;
    }
}
