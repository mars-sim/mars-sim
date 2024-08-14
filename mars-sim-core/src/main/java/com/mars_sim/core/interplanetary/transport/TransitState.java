/*
 * Mars Simulation Project
 * TransitState.java
 * @date 2024-08-10
 * @author stpa
 */

package com.mars_sim.core.interplanetary.transport;

import com.mars_sim.core.tool.Msg;

public enum TransitState {

	PLANNED (Msg.getString("TransitState.planned")), //$NON-NLS-1$
	IN_TRANSIT (Msg.getString("TransitState.inTransit")), //$NON-NLS-1$
	ARRIVED (Msg.getString("TransitState.arrived")), //$NON-NLS-1$
	CANCELED (Msg.getString("TransitState.canceled")); //$NON-NLS-1$

	private String name;

	/** hidden constructor. */
	private TransitState(String name) {
		this.name = name;
	}

	/** 
	 * Gets the internationalized name for display in user interface. 
	 */
	public String getName() {
		return this.name;
	}
}
