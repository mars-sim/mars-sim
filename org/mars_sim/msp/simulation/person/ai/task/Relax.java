/**
 * Mars Simulation Project
 * Relax.java
 * @version 2.75 2003-04-27
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import java.util.Iterator;

import org.mars_sim.msp.simulation.Mars;
import org.mars_sim.msp.simulation.RandomUtil;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.structure.building.BuildingException;
import org.mars_sim.msp.simulation.structure.building.BuildingManager;
import org.mars_sim.msp.simulation.structure.building.InhabitableBuilding;
import org.mars_sim.msp.simulation.structure.building.function.Recreation;

/** The Relax class is a simple task that implements resting and doing nothing for a while.
 *  The duration of the task is by default chosen randomly, up to 100 millisols.
 *
 *  Note: Mental stress may be added later, which this task could be used to reduce.
 */
class Relax extends Task implements Serializable {

    // Data members
    private double duration; // The predetermined duration of task in millisols

    /** Constructs a Relax object
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     */
    public Relax(Person person, Mars mars) {
        super("Relaxing", person, false, mars);

        // If person is in a settlement, try to find a place to relax.
        if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
            BuildingManager buildingManager = person.getSettlement().getBuildingManager();
            InhabitableBuilding relaxBuilding = null;
        
            // Try to find an available recreation building.
            Iterator i = buildingManager.getBuildings(InhabitableBuilding.class).iterator();
            while (i.hasNext()) {
                InhabitableBuilding building = (InhabitableBuilding) i.next();
                if (building instanceof Recreation) {
                    if (building.getAvailableOccupancy() > 0) relaxBuilding = building;
                }
            }
            
            if (relaxBuilding != null) {
                try {
                    if (!relaxBuilding.containsPerson(person)) relaxBuilding.addPerson(person);
                }
                catch (BuildingException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
        
        duration = RandomUtil.getRandomInt(100);
    }

    /** Returns the weighted probability that a person might perform this task.
     *  It should return a 0 if there is no chance to perform this task given the person and his/her situation.
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     *  @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person, Mars mars) {
        return 50D;
    }

    /** This task simply waits until the set duration of the task is complete, then ends the task.
     *  @param time the amount of time to perform this task (in millisols)
     *  @return amount of time remaining after finishing with task (in millisols)
     */
    double performTask(double time) {
        double timeLeft = super.performTask(time);
        if (subTask != null) return timeLeft;

        timeCompleted += time;
        if (timeCompleted > duration) {
            endTask();
            return timeCompleted - duration;
        }
        else return 0;
    }
}

