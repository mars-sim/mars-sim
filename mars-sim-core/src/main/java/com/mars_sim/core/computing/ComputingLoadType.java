/*
 * Mars Simulation Project
 * ComputingLoadType.java
 * @date 2024-08-11
 * @author Manny Kung
 */

package com.mars_sim.core.computing;

import com.mars_sim.tools.Msg;

public enum ComputingLoadType {

	LOW				(Msg.getString("ComputingLoadType.low")), //$NON-NLS-1$
	MID				(Msg.getString("ComputingLoadType.mid")), //$NON-NLS-1$
	HIGH			(Msg.getString("ComputingLoadType.high")), //$NON-NLS-1$
	HEAVY			(Msg.getString("ComputingLoadType.heavy")), //$NON-NLS-1$
	;
	
	private String name;

	/** Hidden constructor. */
	private ComputingLoadType(String name) {
		this.name = name;
	}

	public final String getName() {
		return this.name;
	}

	@Override
	public final String toString() {
		return getName();
	}
}
