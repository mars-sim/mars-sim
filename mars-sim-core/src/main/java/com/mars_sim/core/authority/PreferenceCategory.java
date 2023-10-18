 /*
 * Mars Simulation Project
 * PreferenceCategory.java
 * @date 2023-09-03
 * @author Barry Evans
 */
package com.mars_sim.core.authority;

import com.mars_sim.tools.Msg;

/**
 * Category of a Preference
 */
public enum PreferenceCategory {
    TASK_WEIGHT(PreferenceValueType.DOUBLE), MISSION_WEIGHT(PreferenceValueType.DOUBLE),
    SCIENCE(PreferenceValueType.DOUBLE),
    CONFIGURATION(PreferenceValueType.DOUBLE),
    PROCESS_OVERRIDE(PreferenceValueType.BOOLEAN);

    private String name;
    private PreferenceValueType valueType;

    PreferenceCategory(PreferenceValueType valueType) {
        this.name = Msg.getString("PreferenceCategory." + name().toLowerCase());
        this.valueType = valueType;
    }

    /** gives the internationalized name of this enum for display in user interface. */
    public String getName() {
        return this.name;
    }

    /**
     * What is the type of the value fr this category of preference
     */
    public PreferenceValueType getValueType() {
        return valueType;
    }
}