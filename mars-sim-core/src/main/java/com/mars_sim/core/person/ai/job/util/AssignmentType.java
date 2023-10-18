/*
 * Mars Simulation Project
 * AssignmentType.java
 * @date 2023-07-17
 * @author Manny Kung
 */

package com.mars_sim.core.person.ai.job.util;

import com.mars_sim.tools.Msg;

public enum AssignmentType {

	PENDING					(Msg.getString("AssignmentType.pending")), //$NON-NLS-1$
	APPROVED				(Msg.getString("AssignmentType.approved")), //$NON-NLS-1$
	NOT_APPROVED			(Msg.getString("AssignmentType.notApproved")); //$NON-NLS-1$

	private String name;

	/** hidden constructor. */
	private AssignmentType(String name) {
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
