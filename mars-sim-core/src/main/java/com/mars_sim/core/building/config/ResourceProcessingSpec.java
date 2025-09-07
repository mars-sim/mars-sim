package com.mars_sim.core.building.config;

import java.util.List;

import com.mars_sim.core.building.FunctionSpec;
import com.mars_sim.core.resourceprocess.ResourceProcessEngine;


public class ResourceProcessingSpec extends FunctionSpec {

    private List<ResourceProcessEngine> processes;

    public ResourceProcessingSpec(FunctionSpec base, List<ResourceProcessEngine> processes) {
        super(base);

        this.processes = processes;
    }

    public List<ResourceProcessEngine> getProcesses() {
        return processes;
    }
}
