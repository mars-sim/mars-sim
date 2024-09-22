package com.mars_sim.core.equipment;

import static org.junit.Assert.assertNotEquals;

import com.mars_sim.core.AbstractMarsSimUnitTest;

public class EquipmentFactoryTest extends AbstractMarsSimUnitTest {
    public void testCreateEquipment() {
        var s = buildSettlement("Eqm");

        for(EquipmentType et : EquipmentType.values()) {
            var e = EquipmentFactory.createEquipment(et, s);
            assertTrue(et.name() + " created in Settlement", s.getEquipmentSet().contains(e));
            assertEquals(et.name() + " equipment type", et, e.getEquipmentType());
        }
    }

    public void testGetEquipmentMass() {

        for(EquipmentType et : EquipmentType.values()) {
            var m = EquipmentFactory.getEquipmentMass(et);
            assertNotEquals(et.name() + " valid mass", EquipmentFactory.DEFAULT_MASS, m);
        }
    }
}