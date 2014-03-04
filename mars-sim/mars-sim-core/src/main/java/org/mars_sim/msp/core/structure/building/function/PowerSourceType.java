package org.mars_sim.msp.core.structure.building.function;

/**
 * @author stpa
 * 2014-03-04
 */
public enum PowerSourceType {

	SOLAR ("Solar Power Source"),
	AREAOTHERMAL ("Areothermal Power Source"),
	WIND ("Wind Power Source"),
	STANDARD ("Standard Power Source"),
	FUEL ("Fuel Power Source"),
	SOLAR_THERMAL ("Solar Thermal Power Source");

	private String string;

	/** hidden constructor. */
	private PowerSourceType(String string) {
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
