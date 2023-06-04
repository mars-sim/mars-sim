/**
 * Mars Simulation Project
 * MissionManifest.java
 * @date 2023-06-03
 * @author Barry Evans
 */
package org.mars_sim.msp.core.mission;

import java.util.Map;

/**
 * Manifest for a Mission covering mandatory and optional reources & items.
 */
public class MissionManifest {

    
    /**
     * Add an amount of resource to teh manaifest. This is cummulative and will increase what resoruce
     * total is already present.
     * @param resourceId ID of the resource added
     * @param amount Amount of resource to add
     * @param mandatory Is it mandatory
     */
    public void addResource(int resourceID, double amount, boolean mandatory) {
    }

    /**
     * The resources needed in the manifest
     * @param mandatory
     * @return
     */
    public Map<Integer, Number> getResources(boolean mandatory) {
        return null;
    }

}
