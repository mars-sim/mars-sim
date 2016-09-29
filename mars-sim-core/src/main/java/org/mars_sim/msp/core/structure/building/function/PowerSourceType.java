package org.mars_sim.msp.core.structure.building.function;

/**
 * @author stpa
 * 2014-03-04
 */
public enum PowerSourceType {

	SOLAR_POWER ("Solar Power Source"),
	AREOTHERMAL_POWER ("Areothermal Power Source"),
	WIND_POWER ("Wind Power Source"),
	STANDARD_POWER ("Standard Power Source"),
	FUEL_POWER ("Fuel Power Source"),
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
	// use getType().getString() 
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
