/*
 * Mars Simulation Project
 * EmergencySupplyObjective.java
 * @date 2025-08-23
 * @author Barry Evans
 */
package com.mars_sim.core.mission.objectives;

import java.util.Map;

import com.mars_sim.core.goods.Good;
import com.mars_sim.core.mission.MissionObjective;
import com.mars_sim.core.structure.Settlement;

/**
 * Holds the objectives for an emergency supply mission.
 */
public class EmergencySupplyObjective implements MissionObjective {
    private static final long serialVersionUID = 1L;
	private Settlement destination;
    private Map<Good,Integer> supplies;
    
    public EmergencySupplyObjective(Settlement destination, Map<Good,Integer> emergencyGoods) {
        this.destination = destination;
        this.supplies = emergencyGoods;
    }

    public Settlement getDestination() {
        return destination;
    }
    
    @Override
    public String getName() {
        return "Emergency Supply to " + destination.getName();
    }

    public Map<Good, Integer> getSupplies() {
        return supplies;
    }
}
