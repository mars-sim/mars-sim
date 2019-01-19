/**
 * Mars Simulation Project
 * HeatSourceType.java
 * @version 3.1.0 2017-08-24
 * @author Manny Kung
 */

package org.mars_sim.msp.core.structure.building.function;

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
