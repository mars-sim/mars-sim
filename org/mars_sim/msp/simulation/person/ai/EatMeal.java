/**
 * Mars Simulation Project
 * EatMeal.java
 * @version 2.75 2003-03-16
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.simulation.structure.building.function.*;
import java.util.Iterator;
import java.io.Serializable;

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
        
        // If person is in a settlement, try to find a dining area.
        if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
            BuildingManager buildingManager = person.getSettlement().getBuildingManager();
            InhabitableBuilding diningBuilding = null;
        
            // Try to find an available recreation building.
            Iterator i = buildingManager.getBuildings(InhabitableBuilding.class).iterator();
            while (i.hasNext()) {
                InhabitableBuilding building = (InhabitableBuilding) i.next();
                if (building instanceof Dining) {
                    if (building.getAvailableOccupancy() > 0) diningBuilding = building;
                }
            }
            
            if (diningBuilding != null) {
                try {
                    if (!diningBuilding.containsPerson(person))diningBuilding.addPerson(person);
                }
                catch (BuildingException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
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
            done = true;
            return timeCompleted - duration;
        }
        else return 0D;
    }
}

