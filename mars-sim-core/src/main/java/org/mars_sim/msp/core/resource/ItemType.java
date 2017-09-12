/**
 * Mars Simulation Project
 * ItemType.java
 * @version 3.1.0 2017-09-04
 * @author stpa
 */
package org.mars_sim.msp.core.resource;

/**
 * The ItemType enum class is used for distinguishing between items, vehicles, parts, resources.
 */
public enum ItemType {

	AMOUNT_RESOURCE		("resource"),
	PART				("part"),
	EQUIPMENT			("equipment"),
	VEHICLE				("vehicle");

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
