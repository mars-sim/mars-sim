/*
 * Mars Simulation Project
 * ResourceIdentifier.java
 * @date 2026-05-03
 * @author Barry Evans
 */
package com.mars_sim.core.resource;

/**
 * This class provides the logic to infer the type of resource based on the resource ID.
 * It also defines the ranges of resource IDs for each type of resource.
 */
public class ResourceType {

    // Types of resources
    public static final int AMOUNT_RESOURCE = 0;
    public static final int ITEM_RESOURCE = 1;
    public static final int VEHICLE_RESOURCE = 2;
    public static final int EQUIPMENT_RESOURCE = 3;
    public static final int ROBOT_RESOURCE = 4;
    public static final int BIN_RESOURCE = 5;


    private static final int TYPE_BIT = 11; // Gives 2048 per range

    // First identifer of the appropriate type. This is used to determine the range of resource IDs for each type.
	public static final int FIRST_AMOUNT_RESOURCE_ID = (AMOUNT_RESOURCE << TYPE_BIT) + 1;  // Start at 1 to avoid using 0 as a valid resource ID
	public static final int FIRST_ITEM_RESOURCE_ID = ITEM_RESOURCE << TYPE_BIT;
	public static final int FIRST_VEHICLE_RESOURCE_ID = VEHICLE_RESOURCE << TYPE_BIT;
	public static final int FIRST_EQUIPMENT_RESOURCE_ID = EQUIPMENT_RESOURCE << TYPE_BIT;
	public static final int FIRST_ROBOT_RESOURCE_ID = ROBOT_RESOURCE << TYPE_BIT;
	public static final int FIRST_BIN_RESOURCE_ID = BIN_RESOURCE << TYPE_BIT;

    private ResourceType() {
        // Private constructor to prevent instantiation
    }
    
    /** 
     * Gets the type of resource based on the resource ID.
      * 
      * @param resourceID the resource ID
      * @return the type of resource
    */
    public static int getType(int resourceID) {
        return resourceID >> TYPE_BIT;
    }


}
