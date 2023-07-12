/*
 * Mars Simulation Project
 * ContainerType.java
 * @date 2023-07-12
 * @author Manny Kung
 */
package org.mars_sim.msp.core.equipment;

import org.mars_sim.msp.core.resource.ResourceUtil;


/**
 * This enum class is used for distinguishing between various types of containers
 */
public enum ContainerType {
	
	// Containers
	CRATE				("Crate"), 
	BASKET				("Basket"), 
	;
	
	private String name;	

	private static final int FIRST_CONTAINER_RESOURCE_ID = ResourceUtil.FIRST_CONTAINER_RESOURCE_ID;
		
	/** hidden constructor. */
	private ContainerType(String name) {
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
	 * Obtains the type id (not the ordinal id) of the container.
	 * 
	 * @param name
	 * @return type id
	 */
	public static int convertName2ID(String name) {
		if (name != null) {
	    	for (ContainerType e : ContainerType.values()) {
	    		if (name.equalsIgnoreCase(e.name)) {
	    			return e.ordinal() + FIRST_CONTAINER_RESOURCE_ID;
	    		}
	    	}
		}
		return -1;
	}
	
	/**
	 * Obtains the enum type of the container with its type id.
	 * 
	 * @param typeID
	 * @return {@link ContainerType}
	 */
	public static ContainerType convertID2Type(int typeID) {
		return ContainerType.values()[typeID - FIRST_CONTAINER_RESOURCE_ID];
	}

	/**
	 * Obtains the enum type of the container with its name.
	 * 
	 * @param name
	 * @return {@link ContainerType}
	 */
	public static ContainerType convertName2Enum(String name) {
		if (name != null) {
	    	for (ContainerType et : ContainerType.values()) {
	    		if (name.equalsIgnoreCase(et.name)) {
	    			return et;
	    		}
	    	}
		}
		throw new IllegalArgumentException("No container type called " + name);
	}
	
	/**
	 * Converts an container type to the associated resourceID.
	 * 
	 * Note : Needs revisiting. Container should be referenced by the ContainerType enum everywhere.
	 * 
	 * @return
	 */
	public static int getResourceID(ContainerType type) {
		return type.ordinal() + FIRST_CONTAINER_RESOURCE_ID;
	}
}
