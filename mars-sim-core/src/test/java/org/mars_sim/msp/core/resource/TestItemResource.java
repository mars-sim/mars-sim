package org.mars_sim.msp.core.resource;

import static org.junit.Assert.assertThrows;

import java.util.Arrays;
import java.util.Collection;


import junit.framework.TestCase;

import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.goods.GoodType;

public class TestItemResource extends TestCase {

    private ItemResource hammer;
    private ItemResource socketWrench;
    private ItemResource pipeWrench;
    private Collection<? extends ItemResource> resources;

    public TestItemResource() {
        super();
    }

    @Override
    public void setUp() throws Exception {
        SimulationConfig.instance().loadConfig();

        // initialize
       	ResourceUtil.getInstance();
        resources = ItemResourceUtil.getItemResources();
        GoodType type = GoodType.TOOL;
        
        hammer = ItemResourceUtil.createItemResource("hammer", 1, "a hand tool", type, 1.4D, 1);
        socketWrench = ItemResourceUtil.createItemResource("socket wrench", 2, "a hand tool", type, .5D, 1);
        pipeWrench = ItemResourceUtil.createItemResource("pipe wrench", 3, "a hand tool", type, 2.5D, 1);

        resources = Arrays.asList(hammer, socketWrench, pipeWrench);

    }

    public void testResourceMass() {
        double hammerMass = hammer.getMassPerItem();
        assertEquals(1.4D, hammerMass, 0D);
    }

    public void testResourceName() {
        String name = hammer.getName();
        assertEquals("hammer", name);
    }

    public void testFindItemResourceNegative() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
        	ItemResourceUtil.findItemResource("test");
        });

        assertEquals("No ItemResource called " + "test", e.getMessage());
    }

    public void testGetItemResourcesContents() {
        assertTrue(resources.contains(hammer));
        assertTrue(resources.contains(socketWrench));
        assertTrue(resources.contains(pipeWrench));
    }
}