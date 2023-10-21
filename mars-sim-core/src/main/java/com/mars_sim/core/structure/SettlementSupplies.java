/*
 * Mars Simulation Project
 * SettlementSupplies.java
 * @date 2023-07-30
 * @author Barry Evans
 */
package com.mars_sim.core.structure;

import java.util.List;
import java.util.Map;

import com.mars_sim.core.resource.AmountResource;
import com.mars_sim.core.resource.Part;
import com.mars_sim.core.structure.building.BuildingTemplate;

/**
 * Represents a set of Supplies provided to a Settlement.
 */
public interface SettlementSupplies {

    /**
     * Gets the list of building templates.
     * 
     * @return list of building templates.
     */
    List<BuildingTemplate> getBuildings();

    /**
     * Gets a map of vehicle types and number.
     * 
     * @return map.
     */
    Map<String, Integer> getVehicles();

    /**
     * Gets a map of equipment types and number.
     * 
     * @return map.
     */
    Map<String, Integer> getEquipment();

    /**
     * Gets a map of bin types and number.
     * 
     * @return map.
     */
    Map<String, Integer> getBins();
    
    /**
     * Gets a map of resources and amounts.
     * 
     * @return map.
     */
    Map<AmountResource, Double> getResources();

    /**
     * Gets a map of parts and numbers.
     * 
     * @return map.
     */
    Map<Part, Integer> getParts();

}