/*
 * Mars Simulation Project
 * FoodType.java
 * @date 2022-06-25
 * @author Manny Kung
 */

package com.mars_sim.core.food;

import com.mars_sim.core.tool.Msg;

public enum FoodType {

	ANIMAL, CHEMICAL, CROP, DERIVED, INSECT,
	OIL, ORGANISM, SOY_BASED, TISSUE;
	
	private String name;

	private FoodType() {
        this.name = Msg.getStringOptional("FoodType", name());
	}

	public String getName() {
		return this.name;
	}
}
