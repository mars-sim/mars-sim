/**
 * Mars Simulation Project
 * VehicleType.java
 * @version 3.1.0 2017-09-07
 * @author Manny Kung
 *
 */
package org.mars_sim.msp.core.vehicle;

import org.mars_sim.msp.core.Msg;

public enum VehicleType {
		
	LUV					(Msg.getString("VehicleType.luv")), //$NON-NLS-1$
	EXPLORER_ROVER 		(Msg.getString("VehicleType.explorer")), //$NON-NLS-1$ 
	TRANSPORT_ROVER		(Msg.getString("VehicleType.transport")), //$NON-NLS-1$ 
	CARGO_ROVER			(Msg.getString("VehicleType.cargo")); //$NON-NLS-1$
	
	//Note: the vehicle types used in message.properties must match those in vehicles.xml.
	
	private String name;

	private VehicleType(String name) {
		this.name = name;
	}

	public String getName() {
		// TODO change all names to i18n-keys for accessing messages.properties
		return this.name;
	}
}
