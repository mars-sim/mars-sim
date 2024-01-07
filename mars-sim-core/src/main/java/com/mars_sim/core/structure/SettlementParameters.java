/*
 * Mars Simulation Project
 * SettlementParameters.java
 * @date 2024-01-05
 * @author Barry Evans
 */
package com.mars_sim.core.structure;

import java.util.Collections;
import java.util.Map;

import com.mars_sim.core.parameter.ParameterCategory;

/**
 * Defines the parameters values that are applicable to the behaviour of a Settlement.
 */
public class SettlementParameters extends ParameterCategory {

    public static final ParameterCategory INSTANCE = new SettlementParameters();
    
    private SettlementParameters() {
        super("CONFIGURATION");
    }

    /**
     * Build the specs of attributes that control the behaviour of a Settlement.
     */
    @Override
    protected Map<String, ParameterSpec> calculateSpecs() {
        return Collections.emptyMap();
    }
}
