/**
 * Mars Simulation Project
 * LandmarkType.java
 * @date 2024-10-13
 * @author Barry Evans
 */
package com.mars_sim.core.environment;

import com.mars_sim.core.tool.Msg;

/**
 * Type of landmarks
 */
public enum LandmarkType {

    AO, CM, CH, AA, FO, LF, ME, MO, PL, TH, VA;

    private String name;

    private LandmarkType() {
        this.name = Msg.getString("LandmarkType." + name());
    }

    /**
     * Get teh human readable name
     * @return
     */
    public String getName() {
        return name;
    }
}
