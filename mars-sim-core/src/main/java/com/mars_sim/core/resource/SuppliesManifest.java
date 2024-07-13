/**
 * Mars Simulation Project
 * SuppliesManifest.java
 * @date 2024-07-13
 * @author Barry Evans
 */
package com.mars_sim.core.resource;

import java.util.HashMap;
import java.util.Map;

/**
 * Manifest of Supplies covering mandatory and optional reources & items.
 */
public class SuppliesManifest {

    private Map<Integer, Number> mandatoryResources = new HashMap<>();
    private Map<Integer, Number> optionalResources = new HashMap<>();
    private Map<Integer, Integer> mandatoryEqm = new HashMap<>();
    private Map<Integer, Integer> optionalEqm = new HashMap<>();

    /**
     * Preload the manifest
     * @param requiredResourcesToLoad
     * @param optionalResourcesToLoad
     * @param requiredEquipmentToLoad
     * @param optionalEquipmentToLoad
     */
    public SuppliesManifest(Map<Integer, Number> mandatoryResources, Map<Integer, Number> optionalResources,
            Map<Integer, Integer> mandatoryEqm, Map<Integer, Integer> optionalEqm) {
        this.mandatoryResources = mandatoryResources;
        this.optionalResources = optionalResources;
        this.mandatoryEqm = mandatoryEqm;
        this.optionalEqm = optionalEqm;
    }

    public SuppliesManifest() {
        mandatoryResources = new HashMap<>();
        optionalResources = new HashMap<>();
        mandatoryEqm = new HashMap<>();
        optionalEqm = new HashMap<>();
    }

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
