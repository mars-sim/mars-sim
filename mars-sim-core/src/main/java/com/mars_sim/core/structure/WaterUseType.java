/*
 * Mars Simulation Project
 * WaterUseType.java
 * @date 2022-09-15
 * @author Barry Evans
 */

package com.mars_sim.core.structure;


import com.mars_sim.core.Named;
import com.mars_sim.core.tool.Msg;

public enum WaterUseType implements Named {
	PREP_MEAL,
	PREP_DESSERT,
	CLEAN_MEAL,
	CLEAN_DESSERT;

	private String name;

	private WaterUseType() {
		this.name = Msg.getStringOptional("WaterUseType", name());
	}

	@Override
	public String getName() {
		return name;
	}
}
