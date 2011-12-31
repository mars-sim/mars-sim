package org.mars_sim.msp.core.resource;

import java.util.Set;

import junit.framework.TestCase;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;

public class TestItemResource extends TestCase {

//    private Simulation simulation;
    private ItemResource hammer;
    private ItemResource socketWrench;
    private ItemResource pipeWrench;
    private Set<ItemResource> resources;

    public TestItemResource() {
        super();
    }

    public void testResourceMass() {
        double hammerMass = hammer.getMassPerItem();
        assertEquals(1.4D, hammerMass, 0D);
    }

    public void testResourceName() {
        String name = hammer.getName();
        assertEquals("hammer", name);
    }

    public void testFindItemResourcePositive() {
        ItemResource hammerResource = ItemResource.findItemResource("hammer");
        assertEquals(hammer, hammerResource);
    }

    @Override
    public void setUp() throws Exception {
        SimulationConfig.loadConfig();
        Simulation.createNewSimulation();
//        simulation = Simulation.instance();
        hammer = ItemResource.createItemResource("hammer", 1.4D);
        socketWrench = ItemResource.createItemResource("socket wrench", .5D);
        pipeWrench = ItemResource.createItemResource("pipe wrench", 2.5D);
        resources = ItemResource.getItemResources();
    }

    public void testFindItemResourceNegative() {
        ItemResource.findItemResource("test");
    }

    public void testGetItemResourcesContents() {
        assertTrue(resources.contains(hammer));
        assertTrue(resources.contains(socketWrench));
        assertTrue(resources.contains(pipeWrench));
    }
}