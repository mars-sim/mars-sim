/**
 * Mars Simulation Project
 * Relax.java
 * @version 2.72 2001-07-08
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.task;

import org.mars_sim.msp.simulation.*;

/** The Relax class is a simple task that implements resting and doing nothing for a while.
 *  The duration of the task is by default chosen randomly, up to 100 millisols.
 *
 *  Note: Mental stress may be added later, which this task could be used to reduce.
 */
class Relax extends Task {

    // Data members
    private double duration; // The predetermined duration of task in millisols

    /** Constructs a Relax object
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     */
    public Relax(Person person, VirtualMars mars) {
        super("Relaxing", person, mars);

        duration = RandomUtil.getRandomInt(100);
    }

    /** Returns the weighted probability that a person might perform this task.
     *  It should return a 0 if there is no chance to perform this task given the person and his/her situation.
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     *  @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person, VirtualMars mars) {
        return 50D;
    }

    /** This task simply waits until the set duration of the task is complete, then ends the task. 
     *  @param time the amount of time to perform this task (in millisols)
     *  @return amount of time remaining after finishing with task (in millisols)
     */
    double doTask(double time) {
        double timeLeft = super.doTask(time);
        if (subTask != null) return timeLeft;

        timeCompleted += time;
        if (timeCompleted > duration) {
            isDone = true;
            return timeCompleted - duration;
        }
        else return 0;
    }
}

