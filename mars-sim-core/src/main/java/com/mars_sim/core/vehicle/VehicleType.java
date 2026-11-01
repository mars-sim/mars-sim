/*
 * Mars Simulation Project
 * VehicleType.java
 * @date 2023-06-16
 * @author Manny Kung
 *
 */
package com.mars_sim.core.vehicle;

import java.util.Set;

import com.mars_sim.core.Named;

import com.mars_sim.core.resource.ResourceType;
import com.mars_sim.core.tool.Msg;

public enum VehicleType implements Named {
			
	// Note: these vehicle types must also match those in vehicles.xml.
	LUV,
	DELIVERY_DRONE,CARGO_DRONE,PASSENGER_DRONE,
	EXPLORER_ROVER,TRANSPORT_ROVER,CARGO_ROVER;

	public static final Set<VehicleType> ROVER_TYPES = Set.of(EXPLORER_ROVER, TRANSPORT_ROVER, CARGO_ROVER);

	private String name;

	private VehicleType() {
        this.name = Msg.getStringOptional("VehicleType", name());
	}

	@Override
	public String getName() {
		return this.name;
	}

	/**
	 * Gets the unique resource id for this VehicleType.
	 * The resource id is used to reference the VehicleType in the Inventory.
	 */
	public int getResourceID() {
		return this.ordinal() + ResourceType.FIRST_VEHICLE_RESOURCE_ID;
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
	 * Converts vehicle id to vehicle type.
	 * 
	 * @param id
	 * @return
	 */
	public static VehicleType convertID2Type(int id) {
		return VehicleType.values()[id - ResourceType.FIRST_VEHICLE_RESOURCE_ID];
	}
		
	/**
	 * Is this vehicle a rover ?
	 * 
	 * @param type
	 * @return
	 */
	public static boolean isRover(VehicleType type) {
		return ROVER_TYPES.contains(type);
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
