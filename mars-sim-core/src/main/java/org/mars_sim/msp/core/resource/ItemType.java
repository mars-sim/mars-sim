/*
 * Mars Simulation Project
 * ItemType.java
 * @date 2023-07-30
 * @author stpa
 */

package org.mars_sim.msp.core.resource;

/**
 * The ItemType enum class is used for distinguishing between various types.
 */
public enum ItemType {

	AMOUNT_RESOURCE		("resource"),
	BIN					("bin"),
	EQUIPMENT			("equipment"),
	PART				("part"),
	VEHICLE				("vehicle"),
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
