/*
 * Mars Simulation Project
 * ProcessParameters.java
 * @date 2024-01-05
 * @author Barry Evans
 */
package com.mars_sim.core.structure;

import com.mars_sim.core.parameter.ParameterEnumCategory;
import com.mars_sim.core.parameter.ParameterValueType;

/**
 * Defines the Parameters values that can be used to control Processes
 */
public class ProcessParameters extends ParameterEnumCategory<OverrideType> {

	public static final ProcessParameters INSTANCE = new ProcessParameters();

    private ProcessParameters() {
        super("PROCESS_OVERRIDE", ParameterValueType.BOOLEAN, OverrideType.class);
    }
}
