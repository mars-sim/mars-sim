/*
 * Mars Simulation Project
 * EquipmentType.java
 * @date 2022-10-04
 * @author Manny Kung
 */
package com.mars_sim.core.equipment;

import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.tool.Msg;


/**
 * The EquipmentType enum class is used for distinguishing between various type of equipments
 */
public enum EquipmentType {
	
	// non-container
	EVA_SUIT, 

	// Container 
	BAG, BARREL, GAS_CANISTER, LARGE_BAG,SPECIMEN_BOX,
	THERMAL_BOTTLE, WHEELBARROW;
	
	private String name;	

	private static final int FIRST_EQUIPMENT_RESOURCE_ID = ResourceUtil.FIRST_EQUIPMENT_RESOURCE_ID;
		
	/** hidden constructor. */
	private EquipmentType() {
		this.name = Msg.getStringOptional("EquipmentType", name());
	}

	public String getName() {
		return this.name;
	}

	/**
	 * Obtains the type id (not the ordinal id) of the equipment.
	 * 
	 * @param name
	 * @return type id
	 */
	public static int convertName2ID(String name) {
		if (name != null) {
	    	for (EquipmentType e : EquipmentType.values()) {
	    		if (name.equalsIgnoreCase(e.name)) {
	    			return e.ordinal() + FIRST_EQUIPMENT_RESOURCE_ID;
	    		}
	    	}
		}
		return -1;
	}
	
	/**
	 * Obtains the enum type of the equipment with its type id.
	 * 
	 * @param typeID
	 * @return {@link EquipmentType}
	 */
	public static EquipmentType convertID2Type(int typeID) {
		return EquipmentType.values()[typeID - FIRST_EQUIPMENT_RESOURCE_ID];
	}

	/**
	 * Obtains the enum type of the equipment with its name.
	 * 
	 * @param name
	 * @return {@link EquipmentType}
	 */
	public static EquipmentType convertName2Enum(String name) {
		if (name != null) {
	    	for (EquipmentType et : EquipmentType.values()) {
	    		if (name.equalsIgnoreCase(et.name)) {
	    			return et;
	    		}
	    	}
		}
		throw new IllegalArgumentException("No equipment type called " + name);
	}
	
	/**
	 * Converts an EquipmentType to the associated resourceID.
	 * 
	 * Note : Needs revisiting. Equipment should be referenced by the EquipmentType enum everywhere.
	 * 
	 * @return
	 */
	public static int getResourceID(EquipmentType type) {
		return type.ordinal() + FIRST_EQUIPMENT_RESOURCE_ID;
	}
}
