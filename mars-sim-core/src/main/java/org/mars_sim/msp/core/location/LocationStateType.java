/**
 * Mars Simulation Project
 * LocationStateType.java
 * @version 3.1.0 2016-11-21
 * @author Manny Kung
 */

package org.mars_sim.msp.core.location;

import org.mars_sim.msp.core.Msg;

/**
 * LocationStateType class keeps track of the location state of an unit
 * 
 * @apiNote (1) : OUTSIDE_SETTLEMENT_VICINITY is the intermediate state between being INSIDE_SETTLEMENT (in a garage) and being OUTSIDE_ON_MARS.
 *
 * @apiNote (2) : OUTSIDE_SETTLEMENT_VICINITY can be used by a person or a vehicle.
 *
 * @apiNote (3) : If a vehicle may be in a garage inside a building, this vehicle is INSIDE_SETTLEMENT.
 *                If a vehicle is parked right outside a settlement, this vehicle is OUTSIDE_SETTLEMENT_VICINITY.
 */
public enum LocationStateType {

//	INSIDE_AIRLOCK					(Msg.getString("LocationStateType.insideAirlock")), //$NON-NLS-1$
	INSIDE_SETTLEMENT				(Msg.getString("LocationStateType.insideSettlement")), //$NON-NLS-1$
	INSIDE_VEHICLE					(Msg.getString("LocationStateType.insideVehicle")), //$NON-NLS-1$
	ON_A_PERSON_OR_ROBOT			(Msg.getString("LocationStateType.onAPersonOrRobot")), //$NON-NLS-1$
	INSIDE_EVASUIT					(Msg.getString("LocationStateType.insideEVASuit")), //$NON-NLS-1$
	OUTSIDE_ON_THE_SURFACE_OF_MARS	(Msg.getString("LocationStateType.outsideOnMars")), //$NON-NLS-1$
	WITHIN_SETTLEMENT_VICINITY		(Msg.getString("LocationStateType.settlementVincinity")), //$NON-NLS-1$
	IN_OUTER_SPACE					(Msg.getString("LocationStateType.outerspace")), //$NON-NLS-1$
	UNKNOWN							(Msg.getString("LocationStateType.unknown")) //$NON-NLS-1$
	;

	// Note 0: OUTSIDE_SETTLEMENT_VICINITY is the intermediate state between being INSIDE_SETTLEMENT (in a garage) and being OUTSIDE_ON_MARS
	
	// Note 1: OUTSIDE_SETTLEMENT_VICINITY can be used by a person only
	
	// Note 2: A vehicle may be in a garage inside a building or is parked at a settlement. 
	//         in both cases, this vehicle is INSIDE_SETTLEMENT, not OUTSIDE_SETTLEMENT_VICINITY or OUTSIDE_ON_MARS
	
	public static LocationStateType[] TYPES = new LocationStateType[]{
			INSIDE_SETTLEMENT,
			INSIDE_VEHICLE,
			ON_A_PERSON_OR_ROBOT,
			OUTSIDE_ON_THE_SURFACE_OF_MARS,
			WITHIN_SETTLEMENT_VICINITY
	};

	public static int numSponsors = TYPES.length;
	
	private String name;

	/** hidden constructor. */
	private LocationStateType(String name) {
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
	    return null;
	}

}
