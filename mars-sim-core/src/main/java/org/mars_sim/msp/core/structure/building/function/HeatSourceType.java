package org.mars_sim.msp.core.structure.building.function;

/**
 * Mars Simulation Project
 * SolarHeatSource.java
 * @version 3.07 2014-10-17
 * @author Manny Kung
 */

public enum HeatSourceType {

	//SOLAR ("Solar Power Source"),
	//AREAOTHERMAL ("Areothermal Power Source"),
	ELECTRIC ("Electric Heat Source"),
	FUEL ("Fuel Heat Source"),
	SOLAR_HEAT ("Solar Heat Source");

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
}
