/**
 * Mars Simulation Project
 * Sleep.java
 * @version 2.74 2002-03-11
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai;

import java.io.Serializable;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;

/** The Sleep class is a task for sleeping.
 *  The duration of the task is by default chosen randomly, between 250 - 350 millisols.
 *
 *  Note: Sleeping reduces fatigue.
 */
class Sleep extends Task implements Serializable {

    // Data members
    private double duration; // The predetermined duration of task in millisols

    /** Constructs a Sleep object
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     */
    public Sleep(Person person, Mars mars) {
        super("Sleeping", person, false, mars);

        duration = 250D + RandomUtil.getRandomInt(100);

        // System.out.println(person.getName() + " sleeping with " + person.getFatigue() + " fatigue and visibility: " + mars.getSurfaceFeatures().getSurfaceSunlight(person.getCoordinates()));
    }

    /** Returns the weighted probability that a person might perform this task.
     *  Returns 10 if person's fatigue is over 750.
     *  Returns an additional 50 if it is night time.
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     *  @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person, Mars mars) {
        double result = 0D;

        if (person.getPhysicalCondition().getFatigue() > 750D) {
            result = 25D;
            if (mars.getSurfaceFeatures().getSurfaceSunlight(person.getCoordinates()) == 0)
                result += 50D;
        }

        return result;
    }

    /** This task allows the person to sleep for the duration.
     *  @param time the amount of time to perform this task (in millisols)
     *  @return amount of time remaining after finishing with task (in millisols)
     */
    double performTask(double time) {
        double timeLeft = super.performTask(time);
        if (subTask != null) return timeLeft;

        person.setFatigue(0D);
        timeCompleted += time;
        if (timeCompleted > duration) {
            done = true;
            return timeCompleted - duration;
        }
        else return 0;
    }
}

