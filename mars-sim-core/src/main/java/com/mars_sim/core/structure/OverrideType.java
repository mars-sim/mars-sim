/**
 * Mars Simulation Project
 * OverrideType.java
 * @date 2023-07-04
 * @author Barry Evans
 */
package com.mars_sim.core.structure;

import com.mars_sim.core.tool.Msg;

public enum OverrideType {
	CONSTRUCTION,
	DIG_LOCAL_REGOLITH,
	DIG_LOCAL_ICE,
	FOOD_PRODUCTION,
	MISSION,
	RESOURCE_PROCESS,
	WASTE_PROCESSING;

	private String name;

	private OverrideType() {
        this.name = Msg.getStringOptional("OverrideType", name());
	}

	/** gives the internationalized name of this enum for display in user interface. */
	public String getName() {
		return this.name;
	}
}
