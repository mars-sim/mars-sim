/*
 * Mars Simulation Project
 * ProcessParameters.java
 * @date 2024-01-05
 * @author Barry Evans
 */
package com.mars_sim.core.structure;

import com.mars_sim.core.parameter.ParameterCategory;
import com.mars_sim.core.parameter.ParameterValueType;

/**
 * Defines the Parameters values that can be used to control Processes
 */
public class ProcessParameters extends ParameterCategory {

    private static final long serialVersionUID = 1L;
	public static final ParameterCategory INSTANCE = new ProcessParameters();

    
    private ProcessParameters() {
        super("PROCESS_OVERRIDE");

        for(var mt : OverrideType.values()) {
            addParameter(mt.name(), mt.getName(), ParameterValueType.BOOLEAN);
        }
    }
}
