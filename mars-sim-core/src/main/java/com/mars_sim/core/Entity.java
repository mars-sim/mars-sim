/*
 * Mars Simulation Project
 * Entity.java
 * @date 2023-05-26
 * @author Barry Evans
 */
package com.mars_sim.core;

import java.io.Serializable;

/**
 * Represents an Entity that is simulated by the system. These could be an active element
 * that is proactively managed or a passive object such as a Mission.
 */
public interface Entity extends Named, Serializable {
	
    public static final String ENTITY_SEPERATOR = " - ";

    /**
     * Get the context of the entity in terms of a textual description.

     * @return This may be null if a top level entity.
     */
    String getContext();
}
