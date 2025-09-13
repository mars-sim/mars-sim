/*
 * Mars Simulation Project
 * ResearchSpec.java
 * @date 2025-09-13
 * @author Barry Evans
 */
package com.mars_sim.core.building.config;

import java.util.List;

import com.mars_sim.core.science.ScienceType;

/**
 * Specificaion for a Research Function
 */
public class ResearchSpec extends FunctionSpec {

    private List<ScienceType> science;

    ResearchSpec(FunctionSpec base, List<ScienceType> result) {
        super(base);

        this.science = result;
    }

    public List<ScienceType> getScience() {
        return science;
    }
}
