/*
 * Mars Simulation Project
 * JobAssignmentType.java
 * @date 2022-07-06
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person.ai.job.util;

import org.mars_sim.msp.core.Msg;

public enum JobAssignmentType {

	PENDING					(Msg.getString("JobAssignmentType.pending")), //$NON-NLS-1$
	APPROVED				(Msg.getString("JobAssignmentType.approved")), //$NON-NLS-1$
	NOT_APPROVED			(Msg.getString("JobAssignmentType.notApproved")); //$NON-NLS-1$

	private String name;

	/** hidden constructor. */
	private JobAssignmentType(String name) {
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
