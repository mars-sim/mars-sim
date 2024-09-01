/**
 * Mars Simulation Project
 * SuppliesManifest.java
 * @date 2024-09-01
 * @author Barry Evans
 */
package com.mars_sim.core.resource;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a manifest of Supplies covering mandatory and optional reources & items.
 */
public class SuppliesManifest {

    private Map<Integer, Double> mandatoryAmount;
    private Map<Integer, Double> optionalAmount;
    private Map<Integer, Integer> mandatoryEqm;
    private Map<Integer, Integer> optionalEqm;
    private Map<Integer, Integer> mandatoryItem;
    private Map<Integer, Integer> optionalItem;
    
    /**
     * Preload the manifest. This constructor is only used by the old AbstractVehicleMission class
     * @param mandatoryResources
     * @param optionalResources
     * @param mandatoryEqm
     * @param optionalEqm
     */
    public SuppliesManifest(Map<Integer, Number> mandatoryResources, Map<Integer, Number> optionalResources,
            Map<Integer, Integer> mandatoryEqm, Map<Integer, Integer> optionalEqm) {
        // Split the mandatory into seperate maps
        this.mandatoryAmount = new HashMap<>();
        this.mandatoryItem = new HashMap<>();
        for(var m : mandatoryResources.entrySet())  {
            int resourceId = m.getKey();
            if (resourceId < ResourceUtil.FIRST_ITEM_RESOURCE_ID)
                mandatoryAmount.put(resourceId, m.getValue().doubleValue());
            else
                mandatoryItem.put(resourceId, m.getValue().intValue());
        }

        this.optionalAmount = new HashMap<>();
        this.optionalItem = new HashMap<>();
        for(var m : optionalResources.entrySet())  {
            int resourceId = m.getKey();
            if (resourceId < ResourceUtil.FIRST_ITEM_RESOURCE_ID)
                optionalAmount.put(resourceId, m.getValue().doubleValue());
            else
                optionalItem.put(resourceId, m.getValue().intValue());
        }


        this.mandatoryEqm = mandatoryEqm;
        this.optionalEqm = optionalEqm;
    }

    /**
     * Deep copy of an existing manifest.
     * @param source Source manifest
     */
    public SuppliesManifest(SuppliesManifest source) {
        this.mandatoryAmount =  new HashMap<>(source.mandatoryAmount);
        this.optionalAmount = new HashMap<>(source.optionalAmount);
        this.mandatoryEqm = new HashMap<>(source.mandatoryEqm);
        this.optionalEqm = new HashMap<>(source.optionalEqm);
        this.mandatoryItem =  new HashMap<>(source.mandatoryItem);
        this.optionalItem = new HashMap<>(source.optionalItem);
    }

    public SuppliesManifest() {
        mandatoryAmount = new HashMap<>();
        optionalAmount = new HashMap<>();
        mandatoryEqm = new HashMap<>();
        optionalEqm = new HashMap<>();
        mandatoryItem = new HashMap<>();
        optionalItem = new HashMap<>();
    }

    /**
     * Add an amount of resource to the manifest. This is cummulative and will increase what resoruce
     * total is already present.
     * @param resourceId ID of the resource added
     * @param amount Amount of resource to add
     * @param mandatory Is it mandatory
     */
    public void addAmount(int resourceId, double amount, boolean mandatory) {
        Map<Integer, Double> selected = (mandatory ? mandatoryAmount : optionalAmount);
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
        Map<Integer, Integer> selected = (mandatory ? mandatoryItem : optionalItem);
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
     * The amount resources needed in the manifest.
     * @param mandatory Get mandatory resoruces
     * @return
     */
    public Map<Integer, Double> getAmounts(boolean mandatory) {
        return (mandatory ? mandatoryAmount : optionalAmount);
    }

    /**
     * The Items needed in the manifest.
     * @param mandatory Get mandatory items
     * @return
     */
    public Map<Integer, Integer> getItems(boolean mandatory) {
        return (mandatory ? mandatoryItem : optionalItem);
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
