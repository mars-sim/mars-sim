package com.mars_sim.core.building;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.map.location.BoundedObject;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.test.MarsSimUnitTest;

class BuildingTest extends MarsSimUnitTest {

    private static final String LANDER_HAB = "Lander Hab";

    @Test
    void testCreateLanderHab() {
        var habSpec = getConfig().getBuildingConfiguration().getBuildingSpec(LANDER_HAB);

        Settlement s = buildSettlement("S1");
        BoundedObject bounds = new BoundedObject(LocalPosition.DEFAULT_POSITION, -1, -1, 90D);
        BuildingTemplate template = new BuildingTemplate("1", 0, LANDER_HAB, "B1", bounds);
        var b = Building.createBuilding(template, s);

        assertNotNull(b, "Building created");
        assertEquals(LocalPosition.DEFAULT_POSITION, b.getPosition(), "Building position");
        assertEquals(90D, b.getFacing(), "Building facing");
        assertEquals(habSpec.getWidth(), b.getWidth(), "Building width");
        assertEquals(habSpec.getLength(), b.getLength(), "Building length");
        assertEquals("B1", b.getName(), "Building name");



        assertEquals(habSpec.getFunctionSupported().size(), b.getFunctions().size(), "Building function count");
        assertNotNull(b.getLifeSupport(), "Building has life support");
    }
}
