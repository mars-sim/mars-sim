/*
 * Mars Simulation Project
 * MissionPreferences.java
 * @date 2024-01-05
 * @author Barry Evans
 */
package com.mars_sim.core.person.ai.mission;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    }

    /**
     * Calculate the possible keys based the range of MissionTypes.
     * @return Map from id to the corresponding Spec
     */
    @Override
    protected Map<String, ParameterSpec> calculateSpecs() {
        return createMissionTypeParameters(ParameterValueType.DOUBLE);
    }

    /**
     * Create a set of Parameter specs based on the range of MissionTypes.

     * @param t The type of each spec
     * @return
     */
    static Map<String, ParameterSpec> createMissionTypeParameters(ParameterValueType t) {    
        return Stream.of(MissionType.values())
	 					.collect(Collectors.toMap(MissionType::name,
                                        e-> new ParameterSpec(e.name(), e.getName(), t)));
    }
}
