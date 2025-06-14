/**
 * Mars Simulation Project
 * PlanType.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package com.mars_sim.core.person.ai.mission;

import com.mars_sim.core.tool.Msg;

public enum PlanType {

	PREPARING,PENDING,APPROVED,NOT_APPROVED;

	private String name;

	/** hidden constructor. */
	private PlanType() {
        this.name = Msg.getStringOptional("PlanType", name());
	}

	public final String getName() {
		return this.name;
	}
}
