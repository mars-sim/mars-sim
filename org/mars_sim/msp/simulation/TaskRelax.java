/**
 * Mars Simulation Project
 * TaskRelax.java
 * @version 2.71 2000-09-26
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

/** The TaskRelax class is a simple task that implements resting and doing nothing for a while.
 *  The duration of the task is by default chosen randomly, up to one day (approx).
 *  An alternative constructor allows the duration to be set to a given number of seconds.
 *
 *  Note: Personal mental stress may be added later, which this task could be used to reduce.
 */
class TaskRelax extends Task {

    private int duration; // The predetermined duration in seconds of the task

    public TaskRelax(Person person, VirtualMars mars) {
        super("Relaxing", person, mars);

        duration = (int) Math.round(Math.random() * (8D * 60D * 60D));
    }

    /** Constructor to relax for a given number of seconds. */
    TaskRelax(Person person, VirtualMars mars, int seconds) {
        this(person, mars);

        duration = seconds;
    }

    /** Returns the weighted probability that a person might perform this task.
      * It should return a 0 if there is no chance to perform this task given the person and his/her situation.
      */
    public static int getProbability(Person person, VirtualMars mars) {
        return 50;
    }

    /** This task simply waits until the set duration of the task is complete, then ends the task. */
    void doTask(int seconds) {
        super.doTask(seconds);
        if (subTask != null)
            return;

        timeCompleted += seconds;
        if (timeCompleted > duration)
            isDone = true;
    }
}

