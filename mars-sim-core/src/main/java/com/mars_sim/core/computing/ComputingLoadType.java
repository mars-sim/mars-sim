/*
 * Mars Simulation Project
 * ComputingLoadType.java
 * @date 2024-08-11
 * @author Manny Kung
 */

package com.mars_sim.core.computing;

import com.mars_sim.core.Named;

import com.mars_sim.core.tool.Msg;

public enum ComputingLoadType implements Named {

	LOW, MID, HIGH, HEAVY;
	
	private String name;

	/** Hidden constructor. */
	private ComputingLoadType() {
		this.name = Msg.getStringOptional("ComputingLoadType", name());
	}

	@Override
	public final String getName() {
		return this.name;
	}
}
