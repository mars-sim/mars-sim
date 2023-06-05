/**
 * Mars Simulation Project
 * MissionManifest.java
 * @date 2023-06-03
 * @author Barry Evans
 */
package org.mars_sim.msp.core.mission;

import java.util.HashMap;
import java.util.Map;

/**
 * Manifest for a Mission covering mandatory and optional reources & items.
 */
public class MissionManifest {

    private Map<Integer, Number> mandatoryResources = new HashMap<>();
    private Map<Integer, Number> optionalResources = new HashMap<>();
    private Map<Integer, Integer> mandatoryItems = new HashMap<>();
    private Map<Integer, Integer> optionalItems = new HashMap<>();

    /**
     * Add an amount of resource to teh manaifest. This is cummulative and will increase what resoruce
     * total is already present.
     * @param resourceId ID of the resource added
     * @param amount Amount of resource to add
     * @param mandatory Is it mandatory
     */
    public void addResource(int resourceId, double amount, boolean mandatory) {
        Map<Integer, Number> selected = (mandatory ? mandatoryResources : optionalResources);
        Number existing = selected.getOrDefault(resourceId, 0D);
        selected.put(resourceId, existing.doubleValue() + amount);
    }

    /**
     * The resources needed in the manifest.
     * @param mandatory Get mandatory resoruces
     * @return
     */
    public Map<Integer, Number> getResources(boolean mandatory) {
        return (mandatory ? mandatoryResources : optionalResources);
    }

    /**
     * The items needed in the manifest.
     * @param mandatory Get mandatory resoruces
     * @return
     */
    public Map<Integer, Integer> getItems(boolean mandatory) {
        return (mandatory ? mandatoryItems : optionalItems);
    }
}
