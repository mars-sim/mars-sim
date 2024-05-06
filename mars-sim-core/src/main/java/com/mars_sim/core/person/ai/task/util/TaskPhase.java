/*
 * Mars Simulation Project
 * TaskPhase.java
 * @date 2022-07-24
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task.util;

import java.io.Serializable;

/**
 * A phase of a task.
 */
public final class TaskPhase implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    // The phase name.
    private String name;

    private ExperienceImpact impact;

    /**
     * Constructor
     * @param the phase name.
     */
    public TaskPhase(String name) {
        this(name, null);
    }

    /**
     * Constructor
     * @param name the phase name.
     * @param impact This phase as a specific impct on the Worker
     */
    public TaskPhase(String name, ExperienceImpact impact) {
        this.name = name;
        this.impact = impact;
    }

    /**
     * Gets the phase name.
     * @return phase name string.
     */
    public String getName() {
        return name;
    }

    /**
     * Does this phase has a specific impact on the Worker
     * @return May return null if no specific impact
     */
    public ExperienceImpact getImpact() {
        return impact;
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
        return (obj != null) && (obj instanceof TaskPhase) &&
                ((TaskPhase) obj).getName().equals(name);
    }
    

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
	}
}
