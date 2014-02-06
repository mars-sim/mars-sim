package org.mars_sim.msp.core.resource;

/**
 * to distinguish between items, vehicles, parts, resources
 * @author stpa
 * 2014-02-06
 */
public enum Type {

	AMOUNT_RESOURCE ("resource"),
	PART ("part"),
	EQUIPMENT ("equipment"),
	VEHICLE ("vehicle");

	private String name;	

	private Type(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public String toString() {
		return this.name;
	}
}
