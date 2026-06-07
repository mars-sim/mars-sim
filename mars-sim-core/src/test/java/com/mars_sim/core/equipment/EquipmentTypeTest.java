package com.mars_sim.core.equipment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class EquipmentTypeTest {
    @ParameterizedTest
    @EnumSource(EquipmentType.class)
    void testGetName(EquipmentType type) {
        int calcId = EquipmentType.getResourceID(type);
        var calcType = EquipmentType.convertID2Type(calcId);
        assertEquals(type, calcType, "Conversion of " + type);
    }
}
