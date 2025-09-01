/*
 * Mars Simulation Project
 * VehicleType.java
 * @date 2023-06-16
 * @author Manny Kung
 *
 */
package com.mars_sim.core.vehicle;

import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.tool.Msg;

public enum VehicleType {
		
	LUV,DELIVERY_DRONE,CARGO_DRONE,PASSENGER_DRONE,EXPLORER_ROVER,TRANSPORT_ROVER,CARGO_ROVER;
	
	// Note: these vehicle types are also used in message.properties 
	// they must also match those in vehicles.xml.
	
	private String name;

	private VehicleType() {
        this.name = Msg.getStringOptional("VehicleType", name());
	}

	public String getName() {
		return this.name;
	}

	/**
	 * Converts vehicle name to vehicle type.
	 * 
	 * @param name
	 * @return VehicleType
	 */
	public static VehicleType convertNameToVehicleType(String name) {
		if (name != null) {
	    	for (VehicleType e : VehicleType.values()) {
	    		if (name.equalsIgnoreCase(e.name)) {
	    			return e;
	    		}
	    	}
		}
		return null;
	}
	
	/**
	 * Converts vehicle name to vehicle id.
	 * 
	 * @param name
	 * @return
	 */
	public static int convertName2ID(String name) {
		if (name != null) {
	    	for (VehicleType e : VehicleType.values()) {
	    		if (name.equalsIgnoreCase(e.name)) {
	    			return getVehicleID(e);
	    		}
	    	}
		}
		return -1;
	}
	
	/**
	 * Converts vehicle id to vehicle type.
	 * 
	 * @param id
	 * @return
	 */
	public static VehicleType convertID2Type(int id) {
		return VehicleType.values()[id - ResourceUtil.FIRST_VEHICLE_RESOURCE_ID];
	}
	
	/**
	 * Converts vehicle type to id.
	 * 
	 * @param type
	 * @return
	 */
	public static int getVehicleID(VehicleType type) {
		return ResourceUtil.FIRST_VEHICLE_RESOURCE_ID + type.ordinal();
	}
	
	/**
	 * Is this vehicle a rover ?
	 * 
	 * @param type
	 * @return
	 */
	public static boolean isRover(VehicleType type) {
		return (type == EXPLORER_ROVER
				|| type == TRANSPORT_ROVER
				|| type == CARGO_ROVER);
	}	
	
	/**
	 * Is this vehicle a drone ?
	 * 
	 * @param type
	 * @return
	 */
	public static boolean isDrone(VehicleType type) {
		return (type == DELIVERY_DRONE
				|| type == CARGO_DRONE
				|| type == PASSENGER_DRONE);
	}	
}
