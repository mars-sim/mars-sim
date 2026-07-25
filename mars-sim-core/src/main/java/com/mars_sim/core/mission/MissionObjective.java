/*
 * Mars Simulation Project
 * MissionObjective.java
 * @date 2025-06-15
 * @author Barry Evans
 */
package com.mars_sim.core.mission;

import java.io.Serializable;

/**
 * This represents the objective of a mission
 */
public interface MissionObjective extends Serializable{
    public static final String CHANGE_EVENT = "objective:update";

    /**
     * Returns the name for this mission objective.
     *
     * @return the unique identifier
     */
    String getName();
}
