/**
 * Mars Simulation Project
 * ThermalGeneration.java
 * @version 3.1.2 2020-09-02
 * @author stpa
 */

package org.mars_sim.msp.core.structure.building.function;

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
	
	public final String getName() {
		return this.string;
	}

	@Override
	public final String toString() {
		return getName();
	}
}
