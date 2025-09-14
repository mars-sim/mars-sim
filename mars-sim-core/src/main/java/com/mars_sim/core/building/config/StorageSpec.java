/*
 * Mars Simulation Project
 * StorageSpec.java
 * @date 2025-09-14
 * @author Barry Evans
 */
package com.mars_sim.core.building.config;

import java.util.Map;

/**
 * The specification of a Storage Function.
 */
public class StorageSpec extends FunctionSpec {

    private Map<Integer, Double> capacities;
    private Map<Integer, Double> initial;

    public StorageSpec(FunctionSpec base, Map<Integer,Double> initial, Map<Integer,Double> capacity) {
        super(base);

        this.initial = initial;
        this.capacities = capacity;
    }

    public Map<Integer, Double> getCapacityResources() {
        return capacities;
    }

    public Map<Integer, Double> getInitialResources() {
        return initial;
    }
}
