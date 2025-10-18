/*
 * Mars Simulation Project
 * ParameterCategories.java
 * @date 2025-10-12
 * @author Barry Evans
 */
package com.mars_sim.core.parameter;

import com.mars_sim.core.manufacture.ManufacturingParameters;
import com.mars_sim.core.person.ai.mission.MissionLimitParameters;
import com.mars_sim.core.person.ai.mission.MissionWeightParameters;
import com.mars_sim.core.person.ai.task.util.TaskParameters;
import com.mars_sim.core.science.ScienceParameters;
import com.mars_sim.core.structure.ProcessParameters;
import com.mars_sim.core.structure.SettlementParameters;

/**
 * This utility class holds all the registered ParameterCategories.
 * It provides the groupings to highlight which are applicable to certain styles
 * of ParameterManager usage.
 */
public class ParameterCategories {

    /**
     * All the ParameterCategories that are applicable to Settlements should be in
     * this array.
     */
    private static final ParameterCategory[] SETTLEMENT_CATAGORIES = {ManufacturingParameters.INSTANCE,
                                                        MissionWeightParameters.INSTANCE,
                                                        MissionLimitParameters.INSTANCE,
                                                        ProcessParameters.INSTANCE,
                                                        ScienceParameters.INSTANCE,
                                                        SettlementParameters.INSTANCE,
                                                        TaskParameters.INSTANCE};

    private ParameterCategories() {
        // Utility class
    }

    /**
     * Get the ParameterCategories that are applicable to Settlements.
     * @return
     */
    public static ParameterCategory[] getSettlementCategories() {
        return SETTLEMENT_CATAGORIES.clone();
    }

    /**
     * Fidn the ParameterCategory for the given id.
     * @param categoryId
     * @return
     */
    public static ParameterCategory getCategory(String categoryId) {
       for(ParameterCategory pc : SETTLEMENT_CATAGORIES) {
           if(pc.getId().equals(categoryId)) {
               return pc;
           }
       }
       throw new IllegalArgumentException("No such ParameterCategory: " + categoryId);
    }
}
