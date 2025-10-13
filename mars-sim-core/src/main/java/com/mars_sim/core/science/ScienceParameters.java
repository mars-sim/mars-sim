/*
 * Mars Simulation Project
 * SciencePreferences.java
 * @date 2024-01-05
 * @author Barry Evans
 */
package com.mars_sim.core.science;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mars_sim.core.parameter.ParameterCategory;
import com.mars_sim.core.parameter.ParameterValueType;

/**
 * Defines the potential Parameter values that control scientific research.
 */
public class ScienceParameters extends ParameterCategory {

    private static final long serialVersionUID = 1L;
	public static final ParameterCategory INSTANCE = new ScienceParameters();

    private ScienceParameters() {
        super("SCIENCE");
    }

    /**
     * Calculates the possible keys based the range of ScienceTypes.
     * 
     * @return Map from id to the corresponding Spec
     */
    @Override
    protected Map<String, ParameterSpec> calculateSpecs() {
        return Stream.of(ScienceType.values())
	 					.collect(Collectors.toMap(ScienceType::name,
                                    e-> new ParameterSpec(e.name(), e.getName(), ParameterValueType.DOUBLE)));
    }
}
