/*
 * Mars Simulation Project
 * DataType.java
 * @date 2026-08-27
 * @author Manny Kung
 */
package com.mars_sim.core.data.collection;

import com.mars_sim.core.Named;
import com.mars_sim.core.tool.Msg;

public enum DataType implements Named{
	GROUND_DATA,
	AERIAL_DATA,
	GROUND_PHOTO,
	AERIAL_PHOTO;
	
	private String name;

	/** Hidden constructor. */
	private DataType() {
		this.name = Msg.getStringOptional("DataType", name());
	}

	@Override
	public final String getName() {
		return this.name;
	}
}
