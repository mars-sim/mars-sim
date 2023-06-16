/*
 * Mars Simulation Project
 * VehicleType.java
 * @date 2023-04-24
 * @author Manny Kung
 *
 */
package org.mars_sim.msp.core.vehicle;

import java.util.HashSet;
import java.util.Set;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.resource.ResourceUtil;

public enum VehicleType {
		
	LUV					(Msg.getString("VehicleType.luv")), //$NON-NLS-1$
	DELIVERY_DRONE		(Msg.getString("VehicleType.deliveryDrone")), //$NON-NLS-1$
	EXPLORER_ROVER 		(Msg.getString("VehicleType.explorer")), //$NON-NLS-1$ 
	TRANSPORT_ROVER		(Msg.getString("VehicleType.transport")), //$NON-NLS-1$ 
	CARGO_ROVER			(Msg.getString("VehicleType.cargo")); //$NON-NLS-1$
	
	//Note: the vehicle types used in message.properties must match those in vehicles.xml.
	
	private String name;
	
	private static Set<Integer> idSet;
	
	private VehicleType(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}
	
	/**
	 * Gets a set of vehicle resource ids.
	 * 
	 * @return
	 */
	public static Set<Integer> getIDs() {
		if (idSet == null) {
			idSet = new HashSet<Integer>();
			for (VehicleType e : VehicleType.values()) {
				idSet.add(e.ordinal() + ResourceUtil.FIRST_VEHICLE_RESOURCE_ID);
			}
		}
		return idSet;
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
		if (type == EXPLORER_ROVER
				|| type == TRANSPORT_ROVER
				|| type == CARGO_ROVER) 
			return true;
		return false;
	}	
}
