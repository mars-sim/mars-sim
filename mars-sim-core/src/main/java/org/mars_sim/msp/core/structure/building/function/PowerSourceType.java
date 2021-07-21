/**
 * Mars Simulation Project
 * ThermalGeneration.java
 * @version 3.2.0 2021-06-20
 * @author stpa
 */

package org.mars_sim.msp.core.structure.building.function;

public enum PowerSourceType {

	SOLAR_POWER 		("Solar Power Source"),
	AREOTHERMAL_POWER 	("Areothermal Power Source"),
	WIND_POWER 			("Wind Power Source"),
	STANDARD_POWER 		("Standard Power Source"),
	FUEL_POWER 			("Fuel Power Source"),
	SOLAR_THERMAL 		("Solar Thermal Power Source");

	private String name;

	/** hidden constructor. */
	private PowerSourceType(String name) {
		this.name = name;
	}
	
	public final String getName() {
		return this.name;
	}

	@Override
	public final String toString() {
		return getName();
	}
	
	public static PowerSourceType getType(String name) {
		if (name != null) {
	    	for (PowerSourceType pst : PowerSourceType.values()) {
	    		if (name.equalsIgnoreCase(pst.name)) {
	    			return pst;
	    		}
	    	}
		}
		
		return null;
	}
}
