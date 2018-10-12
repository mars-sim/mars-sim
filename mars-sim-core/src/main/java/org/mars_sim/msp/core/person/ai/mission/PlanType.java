/**
 * Mars Simulation Project
 * PlanType.java
 * @version 3.1.0 2018-10-10
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person.ai.mission;

import org.mars_sim.msp.core.Msg;

public enum PlanType {

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
