/**
 * Mars Simulation Project
 * ObjectiveType.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package com.mars_sim.core.structure;

import com.mars_sim.core.Named;

import com.mars_sim.core.tool.Msg;

public enum ObjectiveType implements Named {

	BUILDERS_HAVEN,CROP_FARM,MANUFACTURING_DEPOT,RESEARCH_CAMPUS,
	TRANSPORTATION_HUB,TRADE_CENTER,TOURISM;

	private String name;

	/** hidden constructor. */
	private ObjectiveType() {
        this.name = Msg.getStringOptional("ObjectiveType", name());
	}
	
	@Override
	public final String getName() {
		return this.name;
	}
}
