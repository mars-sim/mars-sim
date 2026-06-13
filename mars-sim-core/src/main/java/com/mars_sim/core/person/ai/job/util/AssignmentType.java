/*
 * Mars Simulation Project
 * AssignmentType.java
 * @date 2023-07-17
 * @author Manny Kung
 */

package com.mars_sim.core.person.ai.job.util;

import com.mars_sim.core.Named;

import com.mars_sim.core.tool.Msg;

public enum AssignmentType implements Named {

	PENDING,APPROVED,NOT_APPROVED;

	private String name;

	/** hidden constructor. */
	private AssignmentType() {
        this.name = Msg.getStringOptional("AssignmentType", name());
	}

	@Override
	public final String getName() {
		return this.name;
	}
}
