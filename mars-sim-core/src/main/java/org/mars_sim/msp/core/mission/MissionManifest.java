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
    private Map<Integer, Integer> mandatoryEqm = new HashMap<>();
    private Map<Integer, Integer> optionalEqm = new HashMap<>();

    /**
     * Add an amount of resource to the manifest. This is cummulative and will increase what resoruce
     * total is already present.
     * @param resourceId ID of the resource added
     * @param amount Amount of resource to add
     * @param mandatory Is it mandatory
     */
    public void addResource(int resourceId, double amount, boolean mandatory) {
        Map<Integer, Number> selected = (mandatory ? mandatoryResources : optionalResources);
        selected.merge(resourceId, amount, (v1,v2) -> v1.doubleValue() + v2.doubleValue());

        // Number existing = selected.getOrDefault(resourceId, 0D);
        // selected.put(resourceId, existing.doubleValue() + amount);
    }

    /**
     * Set the number of an part to load. This is an absolute value and if it is already defined
     * then it will take the mximum
     * @param resourceId ID of the item added
     * @param count Number of items to load
     * @param mandatory Is it mandatory
     */
    public void addItem(int itemId, int count, boolean mandatory) {
        Map<Integer, Number> selected = (mandatory ? mandatoryResources : optionalResources);
        selected.merge(itemId, count, (v1,v2) -> v1.intValue() + v2.intValue());
        // Could take the largest Item demand
        //selected.merge(itemId, count, (v1,v2) -> Math.max(v1.intValue(), v2.intValue()));
    }

    /**
     * Set the number of an equipment to load. This is an absolute value and if it is already defined
     * then it will take the mximum
     * @param equipmentId ID of the item added
     * @param count Number of items to load
     * @param mandatory Is it mandatory
     */
    public void addEquipment(int equipmentId, int count, boolean mandatory) {
        Map<Integer, Integer> selected = (mandatory ? mandatoryEqm : optionalEqm);
        selected.merge(equipmentId, count, (v1,v2) -> Math.max(v1.intValue(), v2.intValue()));
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
     * The equipment needed in the manifest.
     * @param mandatory Get mandatory resoruces
     * @return
     */
    public Map<Integer, Integer> getEquipment(boolean mandatory) {
        return (mandatory ? mandatoryEqm : optionalEqm);
    }
}
