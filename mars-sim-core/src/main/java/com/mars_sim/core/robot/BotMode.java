/*
 * Mars Simulation Project
 * BotMode.java
 * @date 2025-08-07
 * @author Manny Kung
 *
 */
package com.mars_sim.core.robot;

import com.mars_sim.core.tool.Msg;

/**
 * Robot status mode. They can be either primary or secondary.
 */
public enum BotMode {
	
	CHARGING 				(false),
	MAINTENANCE 			(false),
	MALFUNCTION 			(false),
	OUT_OF_POWER			(false),
	POWER_SAVE				(false),
	NOMINAL 				(true)
	;
	
	private String name;
	private boolean primary;

	private BotMode(boolean primary) {
        this.name = Msg.getStringOptional("BotMode", name());
		this.primary = primary;
	}

	public String getName() {
		return this.name;
	}

	public boolean isPrimary() {
		return primary;
	}
}
