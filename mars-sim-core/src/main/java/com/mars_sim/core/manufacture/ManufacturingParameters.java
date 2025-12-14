/*
 * Mars Simulation Project
 * ManufactureParameters.java
 * @date 2024-01-05
 * @author Barry Evans
 */
package com.mars_sim.core.manufacture;

import com.mars_sim.core.parameter.ParameterCategory;
import com.mars_sim.core.parameter.ParameterKey;
import com.mars_sim.core.parameter.ParameterValueType;

/**
 * Defines the parameters values that are applicable to the behaviour of a Manufacturing.
 */
public class ManufacturingParameters extends ParameterCategory {

	public static final ManufacturingParameters INSTANCE = new ManufacturingParameters();

    public static final ParameterKey NEW_MANU_LIMIT = INSTANCE.addParameter("new_manu_limit",
                                            "Manu. process per Sol", ParameterValueType.INTEGER);
    public static final ParameterKey NEW_MANU_VALUE = INSTANCE.addParameter("new_manu_value",
                                            "Manu. Output Value", ParameterValueType.INTEGER);
    public static final ParameterKey MAX_QUEUE_SIZE = INSTANCE.addParameter("max_queue",
                                            "Maximum queue size", ParameterValueType.INTEGER);

    
    private ManufacturingParameters() {
        super("MANUFACTURING");
    }
}
