/**
 * Mars Simulation Project
 * CollectRockSamples.java
 * @version 2.72 2001-08-12
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.task;

import org.mars_sim.msp.simulation.*;
import java.io.Serializable;

/** The CollectRockSamples class is a task for collecting rock and soil samples at a site. 
 */
class CollectRockSamples extends Task implements Serializable {

    // Data members
    double samplesCollected; // The total amount of rock samples collected.

    /** Constructs a CollectRockSamples object. 
     *
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     */
    public CollectRockSamples(Person person, VirtualMars mars) {
        super("Collecting rock and soil samples", person, mars);

        // System.out.println(person.getName() + " is collecting rock and soil samples."); 
    }

    /** Performs this task for a given period of time 
     *  @param time amount of time to perform task (in millisols) 
     */
    double performTask(double time) {
        double timeLeft = super.performTask(time);
        if (subTask != null) return timeLeft;

        samplesCollected += time;

        if (samplesCollected <= 100D) {
            done = true;
            return samplesCollected - 100D;
        }

        return 0;
    }
}
