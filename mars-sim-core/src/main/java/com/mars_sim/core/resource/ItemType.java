/*
 * Mars Simulation Project
 * ItemType.java
 * @date 2023-07-30
 * @author stpa
 */

package com.mars_sim.core.resource;

/**
 * The ItemType enum class is used for distinguishing between various types.
 */
public enum ItemType {

	AMOUNT_RESOURCE		("Resource"),
	BIN					("Bin"),
	EQUIPMENT			("Equipment"),
	PART				("Part"),
	VEHICLE				("Vehicle"),
	;

	private String name;	

	/** hidden constructor. */
	private ItemType(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	@Override
	public String toString() {
		return this.name;
	}
}
