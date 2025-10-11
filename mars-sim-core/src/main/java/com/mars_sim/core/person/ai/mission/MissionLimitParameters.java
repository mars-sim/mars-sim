/*
 * Mars Simulation Project
 * MissionLimitParameters.java
 * @date 2024-01-06
 * @author Barry Evans
 */
package com.mars_sim.core.person.ai.mission;

import com.mars_sim.core.parameter.ParameterCategory;
import com.mars_sim.core.parameter.ParameterValueType;

/**
 * Defines the Parameter values to control the number of Missions allowed
 */
public class MissionLimitParameters extends ParameterCategory {

    private static final long serialVersionUID = 1L;

	public static final ParameterCategory INSTANCE = new MissionLimitParameters();

    /** The total number of missions allowed. */
    public static final String TOTAL_MISSIONS = "total";

    private MissionLimitParameters() {
        super("MISSION_LIMIT");

        addParameter(TOTAL_MISSIONS, "Total Missions", ParameterValueType.INTEGER);

        for(var mt : MissionType.values()) {
            addParameter(mt.name(), mt.getName(), ParameterValueType.INTEGER);
        }
    }
}
