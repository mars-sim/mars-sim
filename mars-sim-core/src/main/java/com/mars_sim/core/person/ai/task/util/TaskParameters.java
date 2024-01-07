/*
 * Mars Simulation Project
 * TaskParameters.java
 * @date 2024-01-05
 * @author Barry Evans
 */
package com.mars_sim.core.person.ai.task.util;

import java.util.Map;
import java.util.stream.Collectors;

import com.mars_sim.core.parameter.ParameterCategory;
import com.mars_sim.core.parameter.ParameterValueType;

/**
 * Defines the potential Parameters values for the Task weights
 */
public class TaskParameters extends ParameterCategory{

    public static final ParameterCategory INSTANCE = new TaskParameters();

    private TaskParameters() {
        super("TASK_WEIGHT");
    }

    /**
     * Calculate the possible keys based the range of MetaTasks defined.
     * @return Map from id to the corresponding Spec
     */
    @Override
    protected Map<String, ParameterSpec> calculateSpecs() {
        return MetaTaskUtil.getAllMetaTasks().stream()
	 					.collect(Collectors.toMap(MetaTask::getID,
                                    e-> new ParameterSpec(e.getID(), e.getName(), ParameterValueType.DOUBLE)));
    }
}
