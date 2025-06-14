/*
 * Mars Simulation Project
 * SystemType.java
 * @date 2023-06-16
 * @author stpa				
 */
package com.mars_sim.core.building.function;

import java.util.Set;

import com.mars_sim.core.tool.Msg;

public enum SystemType {

	BUILDING, EVA_SUIT, ROBOT, ROVER, VEHICLE;
	
	/**
	 * Returns a set of all system types.
	 */
	public static final Set<SystemType> ALL_SYSTEMS =
				Set.of(SystemType.BUILDING,
						SystemType.EVA_SUIT,
						SystemType.ROBOT,
						SystemType.ROVER,
						SystemType.VEHICLE);
	
	private String name;

	/** hidden constructor. */
	private SystemType() {
		this.name = Msg.getStringOptional("SystemType", name());
	}

	public String getName() {
		return this.name;
	}
}
