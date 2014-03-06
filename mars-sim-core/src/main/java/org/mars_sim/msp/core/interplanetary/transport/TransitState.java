package org.mars_sim.msp.core.interplanetary.transport;

import org.mars_sim.msp.core.Msg;

/**
 * @author stpa
 * 2014-03-06
 */
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
