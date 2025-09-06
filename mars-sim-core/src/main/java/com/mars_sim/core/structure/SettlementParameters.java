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
public final class SettlementParameters extends ParameterCategory {

    private static final long serialVersionUID = 1L;
	public static final SettlementParameters INSTANCE = new SettlementParameters();
    public static final String MAX_EVA = "max_eva";
    public static final String QUICK_CONST = "quick_const";
    
    private SettlementParameters() {
        super("CONFIGURATION");
    }

    /**
     * Build the specs of attributes that control the behaviour of a Settlement.
     */
    @Override
    protected Map<String, ParameterSpec> calculateSpecs() {
        Map<String, ParameterSpec> results = new HashMap<>();
        results.put(MAX_EVA, new ParameterSpec(MAX_EVA, "Max EVA", ParameterValueType.INTEGER));
        results.put(QUICK_CONST, new ParameterSpec(QUICK_CONST, "Quick Construction", ParameterValueType.BOOLEAN));
        return results;
    }
}
