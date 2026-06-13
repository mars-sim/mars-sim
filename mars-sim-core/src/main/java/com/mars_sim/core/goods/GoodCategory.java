/*
 * Mars Simulation Project
 * GoodCategory.java
 * @date 2021-06-20
 * @author stpa
 */
package com.mars_sim.core.goods;

import com.mars_sim.core.Named;

import com.mars_sim.core.tool.Msg;

public enum GoodCategory implements Named {

	AMOUNT_RESOURCE, ITEM_RESOURCE, EQUIPMENT, BIN,
	CONTAINER, VEHICLE, ROBOT;
	
	private String name;

	private GoodCategory() {
        this.name = Msg.getStringOptional("GoodCategory", name());
	}

	@Override
	public String getName() {
		return this.name;
	}
}
