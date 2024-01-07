/*
 * Mars Simulation Project
 * MissionLimitParameters.java
 * @date 2024-01-06
 * @author Barry Evans
 */
package com.mars_sim.core.person.ai.mission;

import java.util.HashMap;
import java.util.Map;

import com.mars_sim.core.parameter.ParameterCategory;
import com.mars_sim.core.parameter.ParameterValueType;

/**
 * Defines the Parameter values to control the number of Missions allowed
 */
public class MissionLimitParameters extends ParameterCategory {

    public static final ParameterCategory INSTANCE = new MissionLimitParameters();

    /** The total number of missions allowed. */
    public static final String TOTAL_MISSIONS = "total";

    private MissionLimitParameters() {
        super("MISSION_LIMIT");
    }

    /**
     * Calculate the possible keys based the range of MissionTypes.
     * @return Map from id to the corresponding Spec
     */
    @Override
    protected Map<String, ParameterSpec> calculateSpecs() {
        Map<String, ParameterSpec> result = new HashMap<>();

        result.putAll(MissionWeightParameters.createMissionTypeParameters(ParameterValueType.INTEGER));
        
        result.put(TOTAL_MISSIONS, new ParameterSpec(TOTAL_MISSIONS, "Total Missions",
                                                    ParameterValueType.INTEGER));
        return result;
    }
}
