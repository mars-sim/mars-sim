/**
 * Mars Simulation Project
 * MissionPhase.java
 * @version 3.07 2014-09-15
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.io.Serializable;

/**
 * A phase of a mission.
 */
public final class MissionPhase implements Serializable {

    // The phase name.
    private String name;
    
    /**
     * Constructor
     * @param the phase name.
     */
    public MissionPhase(String name) {
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