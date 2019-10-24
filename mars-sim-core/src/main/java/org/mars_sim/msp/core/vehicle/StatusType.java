/**
 * Mars Simulation Project
 * StatusType.java
 * @version 3.1.0 2017-10-20
 * @author Manny Kung
 *
 */
package org.mars_sim.msp.core.vehicle;

import org.mars_sim.msp.core.Msg;

public enum StatusType {
	
	GARAGED 			(Msg.getString("StatusType.garaged")), //$NON-NLS-1$
	MAINTENANCE 		(Msg.getString("StatusType.maintenance")), //$NON-NLS-1$
	MALFUNCTION 		(Msg.getString("StatusType.malfunction")), //$NON-NLS-1$
	MOVING 				(Msg.getString("StatusType.moving")), //$NON-NLS-1$
	PARKED 				(Msg.getString("StatusType.parked")), //$NON-NLS-1$
	STUCK				(Msg.getString("StatusType.stuck")), //$NON-NLS-1$
	TOWED 				(Msg.getString("StatusType.towed")), //$NON-NLS-1$	
	TOWING 				(Msg.getString("StatusType.towing")), //$NON-NLS-1$	
	OUT_OF_FUEL 		(Msg.getString("StatusType.outOfFuel")) //$NON-NLS-1$
	;
	
	private String name;

	private StatusType(String name) {
		this.name = name;
	}

	public String getName() {
		// TODO change all names to i18n-keys for accessing messages.properties
		return this.name;
	}
}
