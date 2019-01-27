/**
 * Mars Simulation Project
 * VehicleType.java
 * @version 3.1.0 2017-09-07
 * @author Manny Kung
 *
 */
package org.mars_sim.msp.core.vehicle;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.resource.ResourceUtil;

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
	
	public static int convertName2ID(String name) {
		if (name != null) {
	    	for (VehicleType e : VehicleType.values()) {
	    		if (name.equalsIgnoreCase(e.name)) {
	    			return e.ordinal() + ResourceUtil.FIRST_VEHICLE_RESOURCE_ID;
	    		}
	    	}
		}
		return -1;
	}
	
	public static VehicleType convertID2Type(int id) {
		return VehicleType.values()[id - ResourceUtil.FIRST_VEHICLE_RESOURCE_ID];
	}
	
	public static int getVehicleID(VehicleType type) {
		if (type == LUV) return LUV.ordinal() + ResourceUtil.FIRST_VEHICLE_RESOURCE_ID;
		else if (type == EXPLORER_ROVER) return EXPLORER_ROVER.ordinal() + ResourceUtil.FIRST_VEHICLE_RESOURCE_ID;
		else if (type == TRANSPORT_ROVER) return TRANSPORT_ROVER.ordinal() + ResourceUtil.FIRST_VEHICLE_RESOURCE_ID;
		else if (type == CARGO_ROVER) return CARGO_ROVER.ordinal() + ResourceUtil.FIRST_VEHICLE_RESOURCE_ID;
		else return -1;
	}
}
