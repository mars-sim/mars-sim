/**
 * Mars Simulation Project
 * LocationStateType.java
 * @version 3.1.2 2020-09-02
 * @author Manny Kung
 */

package org.mars_sim.msp.core.location;

import org.mars_sim.msp.core.Msg;

/**
 * The LocationStateType enum class keeps track of the location state of an unit
 * 
 * @apiNote (1) : WITHIN_SETTLEMENT_VICINITY should be an intermediate state between being INSIDE_SETTLEMENT and OUTSIDE_ON_MARS 
 *                but has NEVER been used this way. 
 *
 * @apiNote (2) : IN_AIRLOCK is an intermediate state between being INSIDE_SETTLEMENT and OUTSIDE_ON_MARS for a person
 * 
 * @apiNote (3) : WITHIN_SETTLEMENT_VICINITY can be used by a person or a vehicle.
 *
 * @apiNote (4) : If a vehicle may be in a garage inside a building, this vehicle is INSIDE_SETTLEMENT.
 *                If a vehicle is parked right outside a settlement, this vehicle is OUTSIDE_SETTLEMENT_VICINITY.
 *
 */
public enum LocationStateType {

	INSIDE_SETTLEMENT				(Msg.getString("LocationStateType.insideSettlement")), //$NON-NLS-1$
	INSIDE_VEHICLE					(Msg.getString("LocationStateType.insideVehicle")), //$NON-NLS-1$
//	IN_AIRLOCK						(Msg.getString("LocationStateType.inAirlock")), //$NON-NLS-1$
	ON_A_PERSON_OR_ROBOT			(Msg.getString("LocationStateType.onAPersonOrRobot")), //$NON-NLS-1$
//	INSIDE_EVASUIT					(Msg.getString("LocationStateType.insideEVASuit")), //$NON-NLS-1$
	OUTSIDE_ON_MARS					(Msg.getString("LocationStateType.outsideOnMars")), //$NON-NLS-1$
	WITHIN_SETTLEMENT_VICINITY		(Msg.getString("LocationStateType.settlementVincinity")), //$NON-NLS-1$
	IN_OUTER_SPACE					(Msg.getString("LocationStateType.outerspace")), //$NON-NLS-1$
	UNKNOWN							(Msg.getString("LocationStateType.unknown")) //$NON-NLS-1$
	;

	public static LocationStateType[] TYPES = new LocationStateType[]{
			INSIDE_SETTLEMENT,
			INSIDE_VEHICLE,
//			IN_AIRLOCK,
			ON_A_PERSON_OR_ROBOT,
//			INSIDE_EVASUIT,
			OUTSIDE_ON_MARS,
			WITHIN_SETTLEMENT_VICINITY,
			IN_OUTER_SPACE,
			UNKNOWN
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
	    return UNKNOWN;
	}
}
