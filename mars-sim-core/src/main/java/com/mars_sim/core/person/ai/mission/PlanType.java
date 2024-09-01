/**
 * Mars Simulation Project
 * PlanType.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package com.mars_sim.core.person.ai.mission;

import com.mars_sim.core.tool.Msg;

public enum PlanType {

	PREPARING 				("Preparing"),
	PENDING					(Msg.getString("PlanType.pending")), //$NON-NLS-1$
	APPROVED				(Msg.getString("PlanType.approved")), //$NON-NLS-1$
	NOT_APPROVED			(Msg.getString("PlanType.notApproved")); //$NON-NLS-1$

	private String name;

	/** hidden constructor. */
	private PlanType(String name) {
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
