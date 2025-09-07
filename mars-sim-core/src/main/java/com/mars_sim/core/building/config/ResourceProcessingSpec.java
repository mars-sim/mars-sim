/*
 * Mars Simulation Project
 * ResourceProcessingSpec.java
 * @date 2025-09-07
 * @author Barry Evans
 */
package com.mars_sim.core.building.config;

import java.util.List;

import com.mars_sim.core.resourceprocess.ResourceProcessEngine;

/**
 * This spec defines how a Function is configured that uses ResourceProcesses
 * which are in turn defined by a list of RersourceProcessignEngine
 */
public class ResourceProcessingSpec extends FunctionSpec {

    private List<ResourceProcessEngine> processes;
    
    ResourceProcessingSpec(FunctionSpec base, List<ResourceProcessEngine> processes) {
        super(base);
        this.processes = processes;
    }

    public List<ResourceProcessEngine> getProcesses() {
        return processes;
    }
}
