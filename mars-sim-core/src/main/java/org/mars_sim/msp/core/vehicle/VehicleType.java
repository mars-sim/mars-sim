/**
 * Mars Simulation Project
 * VehicleType.java
 * @version 3.1.0 2016-10-06
 * @author Manny Kung
 *
 */
package org.mars_sim.msp.core.vehicle;

public enum VehicleType {

	LUV					("light utility vehicle"),
	EXPLORER_ROVER 		("explorer rover"),
	TRANSPORT_ROVER		("transport rover"),
	CARGO_ROVER			("cargo rover")	;
	
	private String name;

	private VehicleType(String name) {
		this.name = name;
	}

	public String getName() {
		// TODO change all names to i18n-keys for accessing messages.properties
		return this.name;
	}
}
