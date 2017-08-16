package org.mars_sim.msp.core.structure.building.function;

/**
 * Mars Simulation Project
 * SolarHeatSource.java
 * @version 3.07 2014-10-17
 * @author Manny Kung
 */

public enum HeatSourceType {

	ELECTRIC_HEATING ("Electric Heating Source"),
	FUEL_HEATING ("Fuel Heating Source"),
	THERMAL_NUCLEAR ("Thermal Nuclear Source"),
	SOLAR_HEATING ("Solar Heating Source");
	//AREOTHERMAL ("Areothermal Heating Source"),

	private String string;

	/** hidden constructor. */
	private HeatSourceType(String string) {
		this.string = string;
	}

	/**
	 * use internationalized strings or no strings at all.
	 * @return {@link String}
	 * @deprecated
	 */
	public String getString() {
		return this.string;
	}
	
	public final String getName() {
		return this.string;
	}

	@Override
	public final String toString() {
		return getName();
	}
}
