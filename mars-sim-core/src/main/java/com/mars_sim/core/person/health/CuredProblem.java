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
 * This represents a problem that a Person has suffere and has been cured.
 */
public record CuredProblem(MarsTime start, MarsTime cured, ComplaintReference reference)
    implements Serializable {

    /**
     * Gets the actual Complaint that was cured.
     * This method will be dropped once the ComplaintType is remove in issue #1341
     */
    public Complaint complaint() {
        return reference.getComplaint();
    }
}
