/**
 * Mars Simulation Project
 * ConstructionStageInfoTest.java
 * @version 2.85 2008-08-10
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.structure.construction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.resource.Part;
import org.mars_sim.msp.simulation.resource.Phase;
import org.mars_sim.msp.simulation.vehicle.LightUtilityVehicle;

import junit.framework.TestCase;

/**
 * Unit test for the ConstructionStageInfo class.
 */
public class ConstructionStageInfoTest extends TestCase {

    // Data members
    private ConstructionStageInfo info;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        Map<Part, Integer> parts = new HashMap<Part, Integer>(1);
        parts.put(new Part("test part", 1D), 1);
        
        Map<AmountResource, Double> resources = new HashMap<AmountResource, Double>(1);
        resources.put(new AmountResource("test resource", Phase.SOLID, false), 1D);
        
        List<ConstructionVehicleType> vehicles = 
            new ArrayList<ConstructionVehicleType>(1);
        List<Part> attachments = new ArrayList<Part>(1);
        attachments.add(new Part("attachment part", 1D));
        vehicles.add(new ConstructionVehicleType("Light Utility Vehicle", LightUtilityVehicle.class, 
                attachments));
        
        info = new ConstructionStageInfo("test stage", ConstructionSite.FOUNDATION, 10D, 
                1, null, parts, resources, vehicles);
    }
    
    /*
     * Test method for 'org.mars_sim.msp.simulation.structure.
     * construction.ConstructionStageInfo.getArchitectConstructionSkill()'
     */
    public void testGetArchitectConstructionSkill() {
        assertEquals(1, info.getArchitectConstructionSkill());
    }

    /*
     * Test method for 'org.mars_sim.msp.simulation.structure.
     * construction.ConstructionStageInfo.getName()'
     */
    public void testGetName() {
        assertEquals("test stage", info.getName());
    }

    /*
     * Test method for 'org.mars_sim.msp.simulation.structure.
     * construction.ConstructionStageInfo.getParts()'
     */
    public void testGetParts() {
        Map<Part, Integer> parts = info.getParts();
        assertNotNull(parts);
        assertEquals(1, parts.size());
        Iterator<Part> i = parts.keySet().iterator();
        while (i.hasNext()) {
            Part part = i.next();
            assertEquals("test part", part.getName());
            assertEquals(1D, part.getMassPerItem());
            assertEquals(1, (int) parts.get(part));
        }
    }

    /*
     * Test method for 'org.mars_sim.msp.simulation.structure.
     * construction.ConstructionStageInfo.getPrerequisiteStage()'
     */
    public void testGetPrerequisiteStage() {
        assertNull(info.getPrerequisiteStage());
    }

    /*
     * Test method for 'org.mars_sim.msp.simulation.structure.
     * construction.ConstructionStageInfo.getResources()'
     */
    public void testGetResources() {
        Map<AmountResource, Double> resources = info.getResources();
        assertNotNull(resources);
        assertEquals(1, resources.size());
        Iterator<AmountResource> i = resources.keySet().iterator();
        while (i.hasNext()) {
            AmountResource resource = i.next();
            assertEquals("test resource", resource.getName());
            assertEquals(Phase.SOLID, resource.getPhase());
            assertEquals(1D, resources.get(resource));
        }
    }

    /*
     * Test method for 'org.mars_sim.msp.simulation.structure.
     * construction.ConstructionStageInfo.getType()'
     */
    public void testGetType() {
        assertEquals(ConstructionSite.FOUNDATION, info.getType());
    }

    /*
     * Test method for 'org.mars_sim.msp.simulation.structure.
     * construction.ConstructionStageInfo.getVehicles()'
     */
    public void testGetVehicles() {
        List<ConstructionVehicleType> vehicles = info.getVehicles();
        assertEquals(1, vehicles.size());
        ConstructionVehicleType vehicle = vehicles.get(0);
        assertEquals("Light Utility Vehicle", vehicle.getVehicleType());
        assertEquals(LightUtilityVehicle.class, vehicle.getVehicleClass());
        List<Part> parts = vehicle.getAttachmentParts();
        assertEquals(1, parts.size());
        assertEquals("attachment part", parts.get(0).getName());
    }

    /*
     * Test method for 'org.mars_sim.msp.simulation.structure.
     * construction.ConstructionStageInfo.getWorkTime()'
     */
    public void testGetWorkTime() {
        assertEquals(10D, info.getWorkTime());
    }
}