/*
 * Mars Simulation Project
 * CuredProblem.java
 * @date 2024-07-28
 * @author Barry Evans
 */
package com.mars_sim.core.person.health;

import java.io.Serializable;

import com.mars_sim.core.time.MarsTime;

/**
 * This represents a problem that a Person has suffered and has been cured.
 */
public record CuredProblem(MarsTime start, MarsTime cured, ComplaintReference reference)
    implements Serializable {

    /**
     * Gets the actual Complaint that was cured.
     */
    public Complaint complaint() {
        return reference.getComplaint();
    }
}
