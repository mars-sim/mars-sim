/**
 * Mars Simulation Project
 * TaskRelax.java
 * @version 2.72 2001-05-31
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.task;

import org.mars_sim.msp.simulation.*;

/** The TaskRelax class is a simple task that implements resting and doing nothing for a while.
 *  The duration of the task is by default chosen randomly, up to one day (approx).
 *  An alternative constructor allows the duration to be set to a given number of seconds.
 *
 *  Note: Personal mental stress may be added later, which this task could be used to reduce.
 */
class TaskRelax extends Task {

    // Data members
    private double duration; // The predetermined duration in seconds of the task

    /** Constructs a TaskRelax object
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     */
    public TaskRelax(Person person, VirtualMars mars) {
        super("Relaxing", person, mars);

        duration = Math.round(Math.random() * (8D * 60D * 60D));
    }

    /** Constructor to relax for a given number of seconds. 
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     *  @param seconds the amount of time to relax (in seconds)
     */
    TaskRelax(Person person, VirtualMars mars, double seconds) {
        this(person, mars);

        duration = seconds;
    }

    /** Returns the weighted probability that a person might perform this task.
     * It should return a 0 if there is no chance to perform this task given the person and his/her situation.
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     *  @return the weighted probability that a person might perform this task
     */
    public static int getProbability(Person person, VirtualMars mars) {
        return 50;
    }

    /** This task simply waits until the set duration of the task is complete, then ends the task. 
     *  @param seconds the number of seconds to perform the task
     */
    void doTask(double seconds) {
        super.doTask(seconds);
        if (subTask != null)
            return;

        timeCompleted += seconds;
        if (timeCompleted > duration)
            isDone = true;
    }
}

