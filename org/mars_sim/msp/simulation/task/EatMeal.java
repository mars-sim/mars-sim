/**
 * Mars Simulation Project
 * EatMeal.java
 * @version 2.74 2002-01-13
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
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
    public EatMeal(Person person, VirtualMars mars) {
        super("Eating a meal", person, mars);
    }

    /** Returns the weighted probability that a person might perform this task.
     *
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     *  @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person, VirtualMars mars) {

        double result = person.getHunger() - 250D;
        if (result < 0) result = 0;
        
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
        else return 0;
    }
}

