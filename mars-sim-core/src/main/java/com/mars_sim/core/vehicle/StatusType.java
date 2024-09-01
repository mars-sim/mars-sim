/*
 * Mars Simulation Project
 * StatusType.java
 * @date 2024-07-20
 * @author Manny Kung
 *
 */
package com.mars_sim.core.vehicle;

import com.mars_sim.core.tool.Msg;

/**
 * Vehicle status types. They can be either primary or secondary.
 * Primary status type are mutually exclusive on a Vehicle.
 */
public enum StatusType {
	
	GARAGED 			(true),
	HOVERING 			(true),
	MAINTENANCE 		(false),
	MALFUNCTION 		(false),
	MOVING 				(true),
	PARKED 				(true),
	STUCK				(false),
	TOWED 				(false),	
	TOWING 				(false),	
	OUT_OF_FUEL 		(false),
	OUT_OF_OXIDIZER 	(false), 
	OUT_OF_BATTERY_POWER(false),
	LOADING				(false),
	UNLOADING			(false)
	;
	
	private String name;
	private boolean primary;

	private StatusType(boolean primary) {
		this.name = Msg.getString("StatusType." + name().toLowerCase());
		this.primary = primary;
	}

	public String getName() {
		return this.name;
	}

	public boolean isPrimary() {
		return primary;
	}
}
