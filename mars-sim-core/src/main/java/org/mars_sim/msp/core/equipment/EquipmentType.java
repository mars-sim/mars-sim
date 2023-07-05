/*
 * Mars Simulation Project
 * EquipmentType.java
 * @date 2022-10-04
 * @author Manny Kung
 */
package org.mars_sim.msp.core.equipment;

import org.mars.sim.tools.Msg;
import org.mars_sim.msp.core.resource.ResourceUtil;


/**
 * The EquipmentType enum class is used for distinguishing between various type of equipments
 */
public enum EquipmentType {
	
	// non-container
	EVA_SUIT			(Msg.getString("EquipmentType.EVASuit")), //$NON-NLS-1$ 

	// Container 
	BAG 				(Msg.getString("EquipmentType.bag")), //$NON-NLS-1$
	BARREL 				(Msg.getString("EquipmentType.barrel")), //$NON-NLS-1$
	GAS_CANISTER		(Msg.getString("EquipmentType.gasCanister")), //$NON-NLS-1$
	LARGE_BAG			(Msg.getString("EquipmentType.largeBag")), //$NON-NLS-1$
	SPECIMEN_BOX		(Msg.getString("EquipmentType.specimenBox")), //$NON-NLS-1$
	THERMAL_BOTTLE		(Msg.getString("EquipmentType.thermalBottle")), //$NON-NLS-1$
	WHEELBARROW			(Msg.getString("EquipmentType.wheelbarrow")); //$NON-NLS-1$
	
	private String name;	

	private static final int FIRST_EQUIPMENT_RESOURCE_ID = ResourceUtil.FIRST_EQUIPMENT_RESOURCE_ID;
		
	/** hidden constructor. */
	private EquipmentType(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	@Override
	public String toString() {
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
