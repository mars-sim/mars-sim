/*
 * Mars Simulation Project
 * HealthProblemState.java
 * @date 2024-06-15
 * @author Barry Evans
 */
package com.mars_sim.core.person.health;

import com.mars_sim.core.Named;

import com.mars_sim.core.tool.Msg;

/**
 * Represents the state of a HealthProblem.
 */
public enum HealthProblemState implements Named {
	DEGRADING, BEING_TREATED, RECOVERING, CURED, DEAD;

    private String name;

    private HealthProblemState() {
        this.name = Msg.getStringOptional("HealthProblemState", name());
    }

    @Override
    public String getName() {
        return name;
    }
}
