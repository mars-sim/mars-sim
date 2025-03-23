package com.mars_sim.core.building;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.map.location.BoundedObject;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.structure.Settlement;

public class BuildingTest extends AbstractMarsSimUnitTest{

    private static final String LANDER_HAB = "Lander Hab";

    public void testCreateLanderHab() {
        var habSpec = simConfig.getBuildingConfiguration().getBuildingSpec(LANDER_HAB);

        Settlement s = buildSettlement();
        BoundedObject bounds = new BoundedObject(LocalPosition.DEFAULT_POSITION, -1, -1, 90D);
        BuildingTemplate template = new BuildingTemplate("1", 0, LANDER_HAB, "B1", bounds);
        var b = Building.createBuilding(template, s);

        assertNotNull("Building created", b);
        assertEquals("Building position", LocalPosition.DEFAULT_POSITION, b.getPosition());
        assertEquals("Building facing", 90D, b.getFacing());
        assertEquals("Building width", habSpec.getWidth(), b.getWidth());
        assertEquals("Building length", habSpec.getLength(), b.getLength());
        assertEquals("Building name", "B1", b.getName());



        assertEquals("Building function count", habSpec.getFunctionSupported().size(), b.getFunctions().size());
        assertNotNull("Building has life support", b.getLifeSupport());
    }
}
