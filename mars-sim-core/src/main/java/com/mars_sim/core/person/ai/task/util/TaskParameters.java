/*
 * Mars Simulation Project
 * TaskParameters.java
 * @date 2024-01-05
 * @author Barry Evans
 */
package com.mars_sim.core.person.ai.task.util;

import java.util.List;

import com.mars_sim.core.parameter.ParameterCategory;
import com.mars_sim.core.parameter.ParameterKey;
import com.mars_sim.core.parameter.ParameterValueType;

/**
 * Defines the potential Parameters values for the Task weights.
 * This is a lazy loading implementation as the MetaTaskUtil is created until later.
 */
public class TaskParameters extends ParameterCategory {

    /**
     * This is a singleton.
     */
	public static final TaskParameters INSTANCE = new TaskParameters();

    private TaskParameters() {
        super("TASK_WEIGHT");
    }

    public void registerMetaTasks(List<MetaTask> metaTasks) {
        metaTasks.forEach(
            mt -> addParameter(mt.getID(), mt.getName(), ParameterValueType.DOUBLE)
        );
    }

    /**
     * MetaTasks are lazy loaded so if a key is requested that does not exist, create it.
     */
    @Override
    protected ParameterKey createMissingKey(String pName) {
        return addParameter(pName, pName, ParameterValueType.DOUBLE);
    }
}
