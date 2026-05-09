package com.mars_sim.core.equipment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class BinTypeTest {
    @ParameterizedTest
    @EnumSource(BinType.class)
    void testGetName(BinType type) {
        int calcId = BinType.getResourceID(type);
        var calcType = BinType.convertID2Type(calcId);
        assertEquals(type, calcType, "Conversion of " + type);
    }
}
