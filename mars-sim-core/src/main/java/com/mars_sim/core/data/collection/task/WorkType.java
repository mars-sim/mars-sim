/*
 * Mars Simulation Project
 * WorkType.java
 * @date 2026-09-17
 * @author Manny Kung
 */

package com.mars_sim.core.data.collection.task;

import com.mars_sim.core.Named;
import com.mars_sim.core.tool.Msg;

public enum WorkType implements Named {
	
	SITE_PREPARATION,
	INSTRUMENT_CALIBRATION,
	DATA_COLLECTION;
	
	private String name;	
	
	/** hidden constructor. */
	private WorkType() {
		this.name = Msg.getStringOptional("WorkType", name());
	}

	@Override
	public String getName() {
		return this.name;
	}
}
