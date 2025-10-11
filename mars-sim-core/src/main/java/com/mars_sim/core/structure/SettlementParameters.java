/*
 * Mars Simulation Project
 * SettlementParameters.java
 * @date 2024-01-05
 * @author Barry Evans
 */
package com.mars_sim.core.structure;

import com.mars_sim.core.parameter.ParameterCategory;
import com.mars_sim.core.parameter.ParameterValueType;
import com.mars_sim.core.parameter.ParameterManager.ParameterKey;

/**
 * Defines the parameters values that are applicable to the behaviour of a Settlement.
 */
public final class SettlementParameters extends ParameterCategory {

    private static final long serialVersionUID = 1L;
	public static final SettlementParameters INSTANCE = new SettlementParameters();

    public static final ParameterKey QUICK_CONST = INSTANCE.addParameter(
                        "quick_const","Quick Construction", ParameterValueType.BOOLEAN);
    public static final ParameterKey MAX_EVA = INSTANCE.addParameter(
                        "max_eva","Max EVA", ParameterValueType.INTEGER);
    
    private SettlementParameters() {
        super("CONFIGURATION"); 
    }
}
