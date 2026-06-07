package com.mars_sim.core.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ResourceTypeTest {
    @Test
    void testGetType() {
        // Test that the correct type is returned for each resource ID
        assertEquals(ResourceType.AMOUNT_RESOURCE, ResourceType.getType(ResourceType.FIRST_AMOUNT_RESOURCE_ID));
        assertEquals(ResourceType.ITEM_RESOURCE, ResourceType.getType(ResourceType.FIRST_ITEM_RESOURCE_ID));
        assertEquals(ResourceType.VEHICLE_RESOURCE, ResourceType.getType(ResourceType.FIRST_VEHICLE_RESOURCE_ID));
        assertEquals(ResourceType.EQUIPMENT_RESOURCE, ResourceType.getType(ResourceType.FIRST_EQUIPMENT_RESOURCE_ID));
        assertEquals(ResourceType.ROBOT_RESOURCE, ResourceType.getType(ResourceType.FIRST_ROBOT_RESOURCE_ID));
        assertEquals(ResourceType.BIN_RESOURCE, ResourceType.getType(ResourceType.FIRST_BIN_RESOURCE_ID));
    
        // Test that the correct type is returned for each resource ID
        assertEquals(ResourceType.AMOUNT_RESOURCE, ResourceType.getType(ResourceType.FIRST_AMOUNT_RESOURCE_ID+1));
        assertEquals(ResourceType.ITEM_RESOURCE, ResourceType.getType(ResourceType.FIRST_ITEM_RESOURCE_ID+1));
        assertEquals(ResourceType.VEHICLE_RESOURCE, ResourceType.getType(ResourceType.FIRST_VEHICLE_RESOURCE_ID+1));
        assertEquals(ResourceType.EQUIPMENT_RESOURCE, ResourceType.getType(ResourceType.FIRST_EQUIPMENT_RESOURCE_ID+1));
        assertEquals(ResourceType.ROBOT_RESOURCE, ResourceType.getType(ResourceType.FIRST_ROBOT_RESOURCE_ID+1));
        assertEquals(ResourceType.BIN_RESOURCE, ResourceType.getType(ResourceType.FIRST_BIN_RESOURCE_ID+1));
    }
}
