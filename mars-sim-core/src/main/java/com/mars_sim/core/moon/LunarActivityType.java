/*
 * Mars Simulation Project
 * LunarActivityType.java
 * @date 2024-02-17
 * @author Manny Kung
 */
package com.mars_sim.core.moon;

public enum LunarActivityType {
	
	DEVELOPMENT			("Development"),
	ECONOMIC			("Economic"),
	INDUSTRIAL			("Industrial"),
	RESEARCH			("Research"),
	;

	private String name;

	/** hidden constructor. */
	private LunarActivityType(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}
}
