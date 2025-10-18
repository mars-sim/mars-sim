/*
 * Mars Simulation Project
 * SciencePreferences.java
 * @date 2024-01-05
 * @author Barry Evans
 */
package com.mars_sim.core.person.ai.task.meta;

import com.mars_sim.core.parameter.ParameterEnumCategory;
import com.mars_sim.core.parameter.ParameterValueType;
import com.mars_sim.core.science.ScienceType;

/**
 * Defines the potential Parameter values that control scientific research.
 */
public class ScienceParameters extends ParameterEnumCategory<ScienceType> {

	public static final ScienceParameters INSTANCE = new ScienceParameters();

    private ScienceParameters() {
        super("SCIENCE", ParameterValueType.DOUBLE, ScienceType.class);
    }
}
