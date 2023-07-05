/*
 * Mars Simulation Project
 * LocationStateType.java
 * @date 2023-06-05
 * @author Manny Kung
 */

package org.mars_sim.msp.core.location;

import org.mars.sim.tools.Msg;

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
	WITHIN_SETTLEMENT_VICINITY		(Msg.getString("LocationStateType.settlementVicinity")), //$NON-NLS-1$
	MARS_SURFACE					(Msg.getString("LocationStateType.marsSurface")), //$NON-NLS-1$
	OUTER_SPACE						(Msg.getString("LocationStateType.outerSpace")), //$NON-NLS-1$
	MOON							(Msg.getString("LocationStateType.moon")), //$NON-NLS-1$
	UNKNOWN							(Msg.getString("LocationStateType.unknown")) //$NON-NLS-1$
	;


	// IN_AIRLOCK may be used to denote an intermediate state between being INSIDE_SETTLEMENT and OUTSIDE_ON_MARS for a person
		
	//	IN_AIRLOCK
	//	INSIDE_EVASUIT
	//  INSIDE_BUILDING

	public static LocationStateType[] TYPES = new LocationStateType[]{
			INSIDE_SETTLEMENT,
			INSIDE_VEHICLE,
			ON_PERSON_OR_ROBOT,
			MARS_SURFACE,
			WITHIN_SETTLEMENT_VICINITY,
			OUTER_SPACE,
			UNKNOWN
//			IN_AIRLOCK,
//			INSIDE_EVASUIT,
//  		INSIDE_BUILDING
	};

	public static int numSponsors = TYPES.length;
	
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
	
	public static LocationStateType fromString(String name) {
		if (name != null) {
	    	for (LocationStateType t : LocationStateType.values()) {
	    	  if (name.equalsIgnoreCase(t.name)) {
	        	return t;
	        }
	      }
	    }
	    return UNKNOWN;
	}
}
