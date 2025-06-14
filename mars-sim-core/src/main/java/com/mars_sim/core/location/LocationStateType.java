/*
 * Mars Simulation Project
 * LocationStateType.java
 * @date 2023-06-05
 * @author Manny Kung
 */

package com.mars_sim.core.location;

import com.mars_sim.core.tool.Msg;

/**
 * The LocationStateType enum class keeps track of the location of a unit.
 */
public enum LocationStateType {
	 
	// INSIDE_SETTLEMENT is for a vehicle may be in a garage inside a building
	// WITHIN_SETTLEMENT_VICINITY can be used by a person or a vehicle
	// WITHIN_SETTLEMENT_VICINITY is for a vehicle is parked right outside a settlement and not on a mission

	INSIDE_SETTLEMENT,INSIDE_VEHICLE,ON_PERSON_OR_ROBOT,
	SETTLEMENT_VICINITY,VEHICLE_VICINITY,MARS_SURFACE;

	private String name;

	/** hidden constructor. */
	LocationStateType() {
        this.name = Msg.getStringOptional("LocationStateType", name());
	}
	
	public final String getName() {
		return this.name;
	}
}
