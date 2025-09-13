/*
 * Mars Simulation Project
 * GenerationSpec.java
 * @date 2025-09-13
 * @author Barry Evans
 */
package com.mars_sim.core.building.config;

import java.util.List;

/**
 * This defines the specification of a Function that uses Sources for Generation
 */
public class GenerationSpec extends FunctionSpec {

    private List<SourceSpec> sources;

    GenerationSpec(FunctionSpec base, List<SourceSpec> sourceList) {
        super(base);

        this.sources = sourceList;
    }

    public List<SourceSpec> getSources() {
        return sources;
    }
}
