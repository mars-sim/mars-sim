/*
 * Mars Simulation Project
 * ProcessParameters.java
 * @date 2024-01-05
 * @author Barry Evans
 */
package com.mars_sim.core.structure;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mars_sim.core.parameter.ParameterCategory;
import com.mars_sim.core.parameter.ParameterValueType;

/**
 * Defines the Parameters values that can be used to control Processes
 */
public class ProcessParameters extends ParameterCategory {

    public static final ParameterCategory INSTANCE = new ProcessParameters();

    
    private ProcessParameters() {
        super("PROCESS_OVERRIDE");
    }

    /**
     * Calculate the possible keys based the range of OverrideType.
     * @return Map from id to the corresponding Spec
     */
    @Override
    protected Map<String, ParameterSpec> calculateSpecs() {
        return Stream.of(OverrideType.values())
	 					.collect(Collectors.toMap(OverrideType::name,
                                        e-> new ParameterSpec(e.name(), e.getName(),
                                                        ParameterValueType.BOOLEAN)));
    }
}
