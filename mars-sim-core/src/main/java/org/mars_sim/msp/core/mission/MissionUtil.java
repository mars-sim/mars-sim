/**
 * Mars Simulation Project
 * MissionUtil.java
 * @date 2023-05-06
 * @author Barry Evans
 */
package org.mars_sim.msp.core.mission;

/**
 * Helper methods for Missions
 */
public final class MissionUtil {
    private MissionUtil() {
        // Stop instantiation
    }

    /**
     * Nasty method to add to abstract Numbers
     */
    static Number numberAdd(Number v1, Number v2) {
        if (v1 instanceof Double d1) {
            return Double.valueOf(d1.doubleValue() + v2.doubleValue());
        }
        else if (v2 instanceof Double d2) {
            return Double.valueOf(d2.doubleValue() + v1.doubleValue());
        }
        
        return Integer.valueOf(v1.intValue() + v2.intValue());
    }
}
