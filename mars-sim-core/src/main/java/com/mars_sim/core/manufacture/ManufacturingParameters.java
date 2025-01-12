/*
 * Mars Simulation Project
 * ManufactureParameters.java
 * @date 2024-01-05
 * @author Barry Evans
 */
package com.mars_sim.core.manufacture;

import java.util.HashMap;
import java.util.Map;

import com.mars_sim.core.parameter.ParameterCategory;
import com.mars_sim.core.parameter.ParameterValueType;

/**
 * Defines the parameters values that are applicable to the behaviour of a Manufacturing.
 */
public class ManufacturingParameters extends ParameterCategory {

    private static final long serialVersionUID = 1L;
	public static final ManufacturingParameters INSTANCE = new ManufacturingParameters();
    public static final String NEW_MANU_LIMIT = "new_manu_limit";
    public static final String NEW_MANU_VALUE = "new_manu_value";
    public static final String MAX_QUEUE_SIZE = "max_queue";

    
    private ManufacturingParameters() {
        super("MANUFACTURING");
    }

    /**
     * Build the specs of attributes that control the behaviour of a Settlement.
     */
    @Override
    protected Map<String, ParameterSpec> calculateSpecs() {
        Map<String, ParameterSpec> results = new HashMap<>();
        results.put(NEW_MANU_LIMIT, new ParameterSpec(NEW_MANU_LIMIT,
                    "Manu. process per Sol", ParameterValueType.INTEGER));
        results.put(NEW_MANU_VALUE, new ParameterSpec(NEW_MANU_VALUE,
                    "Manu. Output Value", ParameterValueType.INTEGER));
        results.put(MAX_QUEUE_SIZE, new ParameterSpec(MAX_QUEUE_SIZE,
                    "Maximum queue size", ParameterValueType.INTEGER));
        return results;
    }
}
