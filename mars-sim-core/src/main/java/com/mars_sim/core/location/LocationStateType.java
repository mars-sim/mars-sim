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

	INSIDE_SETTLEMENT				(Msg.getString("LocationStateType.insideSettlement")), //$NON-NLS-1$
	INSIDE_VEHICLE					(Msg.getString("LocationStateType.insideVehicle")), //$NON-NLS-1$
	ON_PERSON_OR_ROBOT				(Msg.getString("LocationStateType.onPersonOrRobot")), //$NON-NLS-1$
	SETTLEMENT_VICINITY				(Msg.getString("LocationStateType.settlementVicinity")), //$NON-NLS-1$
	VEHICLE_VICINITY				(Msg.getString("LocationStateType.vehicleVicinity")), //$NON-NLS-1$
	MARS_SURFACE					(Msg.getString("LocationStateType.marsSurface")) //$NON-NLS-1$
	;

	private String name;

	/** hidden constructor. */
	LocationStateType(String name) {
		this.name = name;
	}
	
	public final String getName() {
		return this.name;
	}

	@Override
	public final String toString() {
		return getName();
	}
}
