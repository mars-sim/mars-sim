package com.mars_sim.core.equipment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.test.MarsSimUnitTest;

public class EquipmentFactoryTest extends MarsSimUnitTest {
    @Test
    public void testCreateEquipment() {
        var s = buildSettlement("Eqm");

        for(EquipmentType et : EquipmentType.values()) {
            var e = EquipmentFactory.createEquipment(et, s);
            assertTrue(s.getEquipmentSet().contains(e), et.name() + " created in Settlement");
            assertEquals(et, e.getEquipmentType(), et.name() + " equipment type");
        }
    }

    @Test
    public void testGetEquipmentMass() {

        for(EquipmentType et : EquipmentType.values()) {
            var m = EquipmentFactory.getEquipmentMass(et);
            assertNotEquals(et.name() + " valid mass", EquipmentFactory.DEFAULT_MASS, m);
        }
    }
}
