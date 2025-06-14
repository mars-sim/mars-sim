/*
 * Mars Simulation Project
 * HistoricalEventCategory.java
 * @date 2024-08-10
 * @author stpa
 */

package com.mars_sim.core.events;

import com.mars_sim.core.tool.Msg;

public enum HistoricalEventCategory {

	MEDICAL, MALFUNCTION, MISSION,
	TASK, TRANSPORT, HAZARD;
	
	private String name;

	/** hidden constructor. */
	private HistoricalEventCategory() {
        this.name = Msg.getStringOptional("HistoricalEventCategory", name());
	}

	/** 
	 * Gets the internationalized name for display in user interface.
	 */
	public String getName() {
		return this.name;
	}
}
