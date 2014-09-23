/**
 * Mars Simulation Project
 * TaskPhase.java
 * @version 3.07 2014-09-22
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;

/**
 * A phase of a task.
 */
public final class TaskPhase implements Serializable {

    // The phase name.
    private String name;
    
    /**
     * Constructor
     * @param the phase name.
     */
    public TaskPhase(String name) {
        this.name = name;
    }
    
    /**
     * Gets the phase name.
     * @return phase name string.
     */
    public String getName() {
        return name;
    }
    
    @Override
    public String toString() {
        return getName();
    }
}