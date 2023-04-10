/*
 * Mars Simulation Project
 * SettlementSupplies.java
 * @date 2023-04-02
 * @author Barry Evans
 */
package org.mars_sim.msp.core.structure;

import java.util.List;
import java.util.Map;

import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.Part;

/**
 * Respresents a set of Supplies provided to a Settlement
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