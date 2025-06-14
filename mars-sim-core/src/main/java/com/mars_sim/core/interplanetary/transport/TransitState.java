/*
 * Mars Simulation Project
 * TransitState.java
 * @date 2024-08-10
 * @author stpa
 */

package com.mars_sim.core.interplanetary.transport;

import com.mars_sim.core.tool.Msg;

public enum TransitState {

	PLANNED, IN_TRANSIT, ARRIVED, CANCELED;

	private String name;

	/** hidden constructor. */
	private TransitState() {
		this.name = Msg.getStringOptional("TransitState", name());
	}

	/** 
	 * Gets the internationalized name for display in user interface. 
	 */
	public String getName() {
		return this.name;
	}
}
