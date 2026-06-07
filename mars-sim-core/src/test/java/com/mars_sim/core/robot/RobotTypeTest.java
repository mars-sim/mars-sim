package com.mars_sim.core.robot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.mars_sim.core.resource.ResourceType;

class RobotTypeTest {
    @ParameterizedTest
    @EnumSource(RobotType.class)
    void testGetName(RobotType type) {
        int calcId = RobotType.getResourceID(type);
        assertEquals(ResourceType.ROBOT_RESOURCE, ResourceType.getType(calcId), "Conversion of " + type);
    }
}