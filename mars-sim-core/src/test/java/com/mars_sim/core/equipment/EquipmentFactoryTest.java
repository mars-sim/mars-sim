package com.mars_sim.core.equipment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.mars_sim.core.test.MarsSimUnitTest;

class EquipmentFactoryTest extends MarsSimUnitTest {
    @ParameterizedTest
    @EnumSource(EquipmentType.class)
    void testCreateEquipment(EquipmentType et) {
        var s = buildSettlement("Eqm");
        var rh = s.getEquipmentInventory();

        var e = EquipmentFactory.createEquipment(et, s);
        assertTrue(rh.getEquipmentSet().contains(e), et.name() + " created in Settlement");
        assertEquals(et, e.getEquipmentType(), et.name() + " equipment type");
    }

    @ParameterizedTest
    @EnumSource(EquipmentType.class)
    void testEquipmentTypeHasMass(EquipmentType et) {
        var m = EquipmentFactory.getUsualMass(et);
        assertNotEquals(0D, m, et.name() + " valid mass");
    }
}
