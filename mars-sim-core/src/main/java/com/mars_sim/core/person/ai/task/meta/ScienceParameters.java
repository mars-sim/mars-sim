/*
 * Mars Simulation Project
 * SciencePreferences.java
 * @date 2024-01-05
 * @author Barry Evans
 */
package com.mars_sim.core.person.ai.task.meta;

import com.mars_sim.core.parameter.ParameterCategory;
import com.mars_sim.core.parameter.ParameterValueType;
import com.mars_sim.core.science.ScienceType;

/**
 * Defines the potential Parameter values that control scientific research.
 */
public class ScienceParameters extends ParameterCategory {

    private static final long serialVersionUID = 1L;
	public static final ParameterCategory INSTANCE = new ScienceParameters();

    private ScienceParameters() {
        super("SCIENCE");
        
        for(var mt : ScienceType.values()) {
            addParameter(mt.name(), mt.getName(), ParameterValueType.DOUBLE);
        }
    }
}
