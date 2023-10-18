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
public interface Entity extends Serializable {
	
    /**
     * Gets the name of the entity.
     * 
     * @return
     */
    String getName();
}
