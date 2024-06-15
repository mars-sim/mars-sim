/*
 * Mars Simulation Project
 * HealthProblemState.java
 * @date 2024-06-15
 * @author Barry Evans
 */
package com.mars_sim.core.person.health;

import com.mars_sim.tools.Msg;

/**
 * Represents the state of a HealthProblem.
 */
public enum HealthProblemState {
	DEGRADING, BEING_TREATED, RECOVERING, CURED, DEAD;

    private String name;

    private HealthProblemState() {
        name = Msg.getString("HealthProblemState." + name());

    }
    public String getName() {
        return name;
    }
}