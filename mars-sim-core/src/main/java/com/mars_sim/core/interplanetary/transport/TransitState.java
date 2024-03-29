/**
 * Mars Simulation Project
 * TransitState.java
 * @version 3.2.0 2021-06-20
 * @author stpa
 */

package com.mars_sim.core.interplanetary.transport;

import com.mars_sim.tools.Msg;

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

	/** gets the internationalized name for display in user interface. */
	public String getName() {
		return this.name;
	}
}
