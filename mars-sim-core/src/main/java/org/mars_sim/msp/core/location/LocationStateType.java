/**
 * Mars Simulation Project
 * LocationStateType.java
 * @version 3.1.0 2016-11-21
 * @author Manny Kung
 */

package org.mars_sim.msp.core.location;

import org.mars_sim.msp.core.Msg;

public enum LocationStateType {

	//INSIDE_BUILDING					(Msg.getString("LocationStateType.insideBuilding")), //$NON-NLS-1$
	INSIDE_SETTLEMENT				(Msg.getString("LocationStateType.insideSettlement")), //$NON-NLS-1$
	INSIDE_VEHICLE					(Msg.getString("LocationStateType.insideVehicle")), //$NON-NLS-1$
//	INSIDE_VEHICLE_INSIDE_GARAGE	(Msg.getString("LocationStateType.insideVehicle")), //$NON-NLS-1$
	ON_A_PERSON						(Msg.getString("LocationStateType.onAPerson")), //$NON-NLS-1$
	OUTSIDE_ON_MARS					(Msg.getString("LocationStateType.outsideOnMars")), //$NON-NLS-1$
	OUTSIDE_SETTLEMENT_VICINITY		(Msg.getString("LocationStateType.settlementVincinity")) //$NON-NLS-1$
	;

	public static LocationStateType[] TYPES = new LocationStateType[]{
			//INSIDE_BUILDING,
			INSIDE_SETTLEMENT,
			INSIDE_VEHICLE,
			//INSIDE_VEHICLE_INSIDE_GARAGE.
			ON_A_PERSON,
			OUTSIDE_ON_MARS,
			OUTSIDE_SETTLEMENT_VICINITY
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
