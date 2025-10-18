/*
 * Mars Simulation Project
 * MissionLimitParameters.java
 * @date 2024-01-06
 * @author Barry Evans
 */
package com.mars_sim.core.person.ai.mission;

import com.mars_sim.core.parameter.ParameterEnumCategory;
import com.mars_sim.core.parameter.ParameterKey;
import com.mars_sim.core.parameter.ParameterValueType;

/**
 * Defines the Parameter values to control the number of Missions allowed
 */
public class MissionLimitParameters extends ParameterEnumCategory<MissionType> {

	public static final MissionLimitParameters INSTANCE = new MissionLimitParameters();

    /** The total number of missions allowed. */
    public static final ParameterKey TOTAL_MISSIONS =
                    INSTANCE.addParameter("total", "Total Missions", ParameterValueType.INTEGER);

    private MissionLimitParameters() {
        super("MISSION_LIMIT", ParameterValueType.INTEGER, MissionType.class);     
    }
}
