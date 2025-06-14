/*
 * Mars Simulation Project
 * PowerMode.java
 * @date 2023-05-25
 * @author stpa
 */

package com.mars_sim.core.building.utility.power;

import com.mars_sim.core.tool.Msg;

public enum PowerMode {

	FULL_POWER, LOW_POWER, NO_POWER;

	private String name;

	/** hidden constructor. */
	private PowerMode() {
		this.name = Msg.getStringOptional("PowerMode", name());
	}

	/** gives back an internationalized {@link String} for display in user interface. */
	public String getName() {
		return this.name;
	}
}
