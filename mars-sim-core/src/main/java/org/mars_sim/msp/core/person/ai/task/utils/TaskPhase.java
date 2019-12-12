/**
 * Mars Simulation Project
 * TaskPhase.java
 * @version 3.1.0 2017-02-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.utils;

import java.io.Serializable;

/**
 * A phase of a task.
 */
public final class TaskPhase implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    // The phase name.
    private String name = "";

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
        return name;
    }
    
	/**
	 * Gets the hash code for this object.
	 * 
	 * @return hash code.
	 */
	public int hashCode() {
		return name.hashCode();
	}
	
    @Override
    public boolean equals(Object obj) {
        if ((obj != null) && (obj instanceof TaskPhase) &&
                ((TaskPhase)obj).getName().equals(name)) {
            return true;
        }
        return false;
    }
    

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
	}
}