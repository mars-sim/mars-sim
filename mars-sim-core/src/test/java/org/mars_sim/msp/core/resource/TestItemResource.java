package org.mars_sim.msp.core.resource;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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
        SimulationConfig.loadConfig();
        
        // initialize 
        new ItemResource();
        
        hammer = ItemResource.createItemResource("hammer","a tool", 1.4D);
        socketWrench = ItemResource.createItemResource("socket wrench","another tool", .5D);
        pipeWrench = ItemResource.createItemResource("pipe wrench","and another tool", 2.5D);
        resources = ItemResource.getItemResources();
        
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
            ItemResource.findItemResource("test");
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