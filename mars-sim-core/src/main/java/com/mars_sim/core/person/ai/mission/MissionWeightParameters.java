/*
 * Mars Simulation Project
 * MissionPreferences.java
 * @date 2024-01-05
 * @author Barry Evans
 */
package com.mars_sim.core.person.ai.mission;

import com.mars_sim.core.parameter.ParameterCategory;
import com.mars_sim.core.parameter.ParameterValueType;

/**
 * Defines the Parameter values to control Mission weights
 */
public class MissionWeightParameters extends ParameterCategory {

    private static final long serialVersionUID = 1L;
	public static final ParameterCategory INSTANCE = new MissionWeightParameters();

    private MissionWeightParameters() {
        super("MISSION_WEIGHT");

        for(var mt : MissionType.values()) {
            addParameter(mt.name(), mt.getName(), ParameterValueType.INTEGER);
        }
    }
}
