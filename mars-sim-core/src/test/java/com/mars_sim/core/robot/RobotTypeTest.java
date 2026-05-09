package com.mars_sim.core.robot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.mars_sim.core.equipment.EquipmentType;

class RobotTypeTest {
    @ParameterizedTest
    @EnumSource(EquipmentType.class)
    void testGetName(EquipmentType type) {
        int calcId = EquipmentType.getResourceID(type);
        var calcType = EquipmentType.convertID2Type(calcId);
        assertEquals(type, calcType, "Conversion of " + type);
    }
}
