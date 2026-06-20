/*
 * Mars Simulation Project
 * BodyRegionType.java
 * @date 2022-09-24
 * @author Manny Kung
 */

package com.mars_sim.core.person.health;

import com.mars_sim.core.Named;

import com.mars_sim.core.tool.Msg;

public enum BodyRegionType implements Named {

	BFO,OCULAR,SKIN;

	private String name;

	/** hidden constructor. */
	private BodyRegionType() {
		this.name = Msg.getStringOptional("bodyregiontype", name());
	}

	@Override
	public final String getName() {
		return this.name;
	}

	@Override
	public final String toString() {
		return getName();
	}
}
