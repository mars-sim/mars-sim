/*
 * Mars Simulation Project
 * SettlementSuppliesImpl.java
 * @date 2023-07-30
 * @author Barry Evans
 */
package com.mars_sim.core.structure;

import java.util.List;
import java.util.Map;

import com.mars_sim.core.building.BuildingTemplate;
import com.mars_sim.core.resource.AmountResource;
import com.mars_sim.core.resource.Part;

/**
 * Immutable implementation of a Settlemetn Supplies
 */
class SettlementSuppliesImpl implements SettlementSupplies {
	private List<BuildingTemplate> buildings;
	private Map<String, Integer> vehicles;
	private Map<String, Integer> equipment;
	private Map<String, Integer> bins;
	private Map<AmountResource, Double> resources;
	private Map<Part, Integer> parts;

    
    SettlementSuppliesImpl(List<BuildingTemplate> buildings, Map<String, Integer> vehicles,
            Map<String, Integer> equipment, Map<String, Integer> bins,
            Map<AmountResource, Double> resources,
            Map<Part, Integer> parts) {
        this.buildings = buildings;
        this.vehicles = vehicles;
        this.equipment = equipment;
        this.bins = bins;
        this.resources = resources;
        this.parts = parts;
    }

    /**
     * Gets the list of building templates.
     * 
     * @return list of building templates.
     */
    public List<BuildingTemplate> getBuildings() {
        return buildings;
    }

    /**
     * Gets a map of vehicle types and number.
     * 
     * @return map.
     */
    public Map<String, Integer> getVehicles() {
        return vehicles;
    }

    /**
     * Gets a map of equipment types and number.
     * 
     * @return map.
     */
    public Map<String, Integer> getEquipment() {
        return equipment;
    }

    /**
     * Gets a map of bin types and number.
     * 
     * @return map.
     */
    public Map<String, Integer> getBins() {
        return bins;
    }
    
    /**
     * Gets a map of resources and amounts.
     * 
     * @return map.
     */
    public Map<AmountResource, Double> getResources() {
        return resources;
    }

    /**
     * Gets a map of parts and numbers.
     * 
     * @return map.
     */
    public Map<Part, Integer> getParts() {
        return parts;
    }
}