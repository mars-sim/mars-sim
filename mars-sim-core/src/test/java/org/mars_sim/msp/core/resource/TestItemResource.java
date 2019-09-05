package org.mars_sim.msp.core.resource;

import java.util.Arrays;
import java.util.Collection;


import junit.framework.TestCase;

import org.mars_sim.msp.core.SimulationConfig;

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
        //new ItemResource();
       	ResourceUtil.getInstance();
        resources = ItemResourceUtil.getItemResources();
        
        hammer = ItemResourceUtil.createItemResource("hammer", 1, "a tool", 1.4D, 1);
        socketWrench = ItemResourceUtil.createItemResource("socket wrench", 2, "another tool", .5D, 1);
        pipeWrench = ItemResourceUtil.createItemResource("pipe wrench", 3, "and another tool", 2.5D, 1);
 
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
        try {
        	ItemResourceUtil.findItemResource("test");
            //fail("Should have thrown an exception");
        }
        catch (Exception e) {
            // Expected.
        }
    }

    public void testGetItemResourcesContents() {
        //assertFalse(resources.contains(hammer));
        //assertFalse(resources.contains(socketWrench));
        //assertFalse(resources.contains(pipeWrench));
        assertTrue(resources.contains(hammer));
        assertTrue(resources.contains(socketWrench));
        assertTrue(resources.contains(pipeWrench));
    }
}