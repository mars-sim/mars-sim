/*
 * Mars Simulation Project
 * BinType.java
 * @date 2023-07-30
 * @author Manny Kung
 */
package org.mars_sim.msp.core.equipment;

import org.mars_sim.msp.core.resource.ResourceUtil;


/**
 * This enum class is used for distinguishing between various types of binn
 */
public enum BinType {
	
	// Containers
	BASKET				("Basket"),
	CRATE				("Crate"), 
	POT					("Pot"),
	;
	
	private String name;	

	private static final int FIRST_BIN_RESOURCE_ID = ResourceUtil.FIRST_BIN_RESOURCE_ID;
		
	/** hidden constructor. */
	private BinType(String name) {
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
	 * Obtains the type id (not the ordinal id) of the bin.
	 * 
	 * @param name
	 * @return type id
	 */
	public static int convertName2ID(String name) {
		if (name != null) {
	    	for (BinType e : BinType.values()) {
	    		if (name.equalsIgnoreCase(e.name)) {
	    			return e.ordinal() + FIRST_BIN_RESOURCE_ID;
	    		}
	    	}
		}
		return -1;
	}
	
	/**
	 * Obtains the enum type of the bin with its type id.
	 * 
	 * @param typeID
	 * @return {@link BinType}
	 */
	public static BinType convertID2Type(int typeID) {
		return BinType.values()[typeID - FIRST_BIN_RESOURCE_ID];
	}

	/**
	 * Obtains the enum type of the bin with its name.
	 * 
	 * @param name
	 * @return {@link BinType}
	 */
	public static BinType convertName2Enum(String name) {
		if (name != null) {
	    	for (BinType et : BinType.values()) {
	    		if (name.equalsIgnoreCase(et.name)) {
	    			return et;
	    		}
	    	}
		}
		throw new IllegalArgumentException("No bin type called " + name);
	}
	
	/**
	 * Converts a bin type to the associated resourceID.
	 * 
	 * Note : Needs revisiting. Bin should be referenced by the BinType enum everywhere.
	 * 
	 * @return
	 */
	public static int getResourceID(BinType type) {
		return type.ordinal() + FIRST_BIN_RESOURCE_ID;
	}
}
