/*
 * Mars Simulation Project
 * UnitType.java
 * @date 2023-06-05
 * @author stpa
 */

package com.mars_sim.core;

import com.mars_sim.core.tool.Msg;

public enum UnitType {

	OUTER_SPACE,MARS,MOON,SETTLEMENT,PERSON,
	VEHICLE,CONTAINER,EVA_SUIT,ROBOT,
	BUILDING,CONSTRUCTION;
	
	private String name;

	/** hidden constructor. */
	private UnitType() {
        this.name = Msg.getStringOptional("UnitType", name());
	}

	public String getName() {
		return this.name;
	}
}
