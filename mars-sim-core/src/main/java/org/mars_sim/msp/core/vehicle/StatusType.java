/**
 * Mars Simulation Project
 * StatusType.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 *
 */
package org.mars_sim.msp.core.vehicle;

import org.mars_sim.msp.core.Msg;

/**
 * Vehicle status types. They can be either primary or secondary.
 * Primary status type are mutually exclusive on a Vehicle.
 */
public enum StatusType {
	
	GARAGED 			(Msg.getString("StatusType.garaged"), true), //$NON-NLS-1$
	MAINTENANCE 		(Msg.getString("StatusType.maintenance"), false), //$NON-NLS-1$
	MALFUNCTION 		(Msg.getString("StatusType.malfunction"), false), //$NON-NLS-1$
	MOVING 				(Msg.getString("StatusType.moving"), true), //$NON-NLS-1$
	PARKED 				(Msg.getString("StatusType.parked"), true), //$NON-NLS-1$
	STUCK				(Msg.getString("StatusType.stuck"), false), //$NON-NLS-1$
	TOWED 				(Msg.getString("StatusType.towed"), false), //$NON-NLS-1$	
	TOWING 				(Msg.getString("StatusType.towing"), false), //$NON-NLS-1$	
	OUT_OF_FUEL 		(Msg.getString("StatusType.outOfFuel"),false) //$NON-NLS-1$
	;
	
	private String name;
	private boolean primary;

	private StatusType(String name, boolean primary) {
		this.name = name;
		this.primary = primary;
	}

	public String getName() {
		return this.name;
	}
	
	public String toString() {
		return this.name;
	}

	public boolean isPrimary() {
		return primary;
	}
}
