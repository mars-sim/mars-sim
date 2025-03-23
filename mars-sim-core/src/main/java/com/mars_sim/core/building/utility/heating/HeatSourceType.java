/**
 * Mars Simulation Project
 * HeatSourceType.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package com.mars_sim.core.building.utility.heating;

public enum HeatSourceType {

	ELECTRIC_HEATING 		("Electric Heating Source"),
	FUEL_HEATING 			("Fuel Heating Source"),
	THERMAL_NUCLEAR 		("Thermal Nuclear Source"),
	SOLAR_HEATING 			("Solar Heating Source");
	//AREOTHERMAL ("Areothermal Heating Source"),

	private String string;

	/** hidden constructor. */
	private HeatSourceType(String string) {
		this.string = string;
	}

	public final String getName() {
		return this.string;
	}

	@Override
	public final String toString() {
		return getName();
	}
}
