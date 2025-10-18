/*
 * Mars Simulation Project
 * MissionPreferences.java
 * @date 2024-01-05
 * @author Barry Evans
 */
package com.mars_sim.core.person.ai.mission;

import com.mars_sim.core.parameter.ParameterEnumCategory;
import com.mars_sim.core.parameter.ParameterValueType;

/**
 * Defines the Parameter values to control Mission weights
 */
public class MissionWeightParameters extends ParameterEnumCategory<MissionType> {

	public static final MissionWeightParameters INSTANCE = new MissionWeightParameters();

    private MissionWeightParameters() {
        super("MISSION_WEIGHT", ParameterValueType.INTEGER, MissionType.class);
    }
}
