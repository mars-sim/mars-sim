/*
 * Mars Simulation Project
 * SettlementParameters.java
 * @date 2024-01-05
 * @author Barry Evans
 */
package com.mars_sim.core.structure;

import java.util.HashMap;
import java.util.Map;

import com.mars_sim.core.parameter.ParameterCategory;
import com.mars_sim.core.parameter.ParameterValueType;

/**
 * Defines the parameters values that are applicable to the behaviour of a Settlement.
 */
public class SettlementParameters extends ParameterCategory {

    public static final ParameterCategory INSTANCE = new SettlementParameters();

    // Prefernec eid for teh maximum of active missions
    public static final String MISSION_LIMIT = "active-missions";
    
    private SettlementParameters() {
        super("CONFIGURATION");
    }

    /**
     * Build the specs of attributes that control the behaviour of a Settlement.
     */
    @Override
    protected Map<String, ParameterSpec> calculateSpecs() {
        Map<String, ParameterSpec> specs = new HashMap<>();
        specs.put(MISSION_LIMIT, new ParameterSpec(MISSION_LIMIT, "Active Missions",
                                            ParameterValueType.INTEGER));
        return specs;
    }
}
