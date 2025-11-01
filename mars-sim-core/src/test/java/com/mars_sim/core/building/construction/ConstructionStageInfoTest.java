/**
 * Mars Simulation Project
 * ConstructionStageInfoTest.java
 * @version 3.1.0 2017-09-11
 * @author Scott Davis
 */

package com.mars_sim.core.building.construction;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.resource.AmountResource;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.Part;
import com.mars_sim.core.resource.PhaseType;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.vehicle.VehicleType;

/**
 * Unit test for the ConstructionStageInfo class.
 */
class ConstructionStageInfoTest {

    // Data members
    private ConstructionStageInfo info;

    @BeforeEach
    void setUp() {
        SimulationConfig.loadConfig();

        Map<Integer, Integer> parts = new HashMap<>(1);
        Part p = ItemResourceUtil.findItemResource(ItemResourceUtil.BACKHOE_ID);
        parts.put(p.getID(), 1);

        Map<Integer, Double> resources = new HashMap<>(1);

        resources.put(ResourceUtil.SAND_ID, 1D);

        List<ConstructionVehicleType> vehicles =
            new ArrayList<>(1);
        List<Integer> attachments = new ArrayList<>(1);
        
        Part atth = ItemResourceUtil.findItemResource(ItemResourceUtil.PNEUMATIC_DRILL_ID);
        attachments.add(atth.getID());

        vehicles.add(new ConstructionVehicleType(VehicleType.LUV, attachments));

        info = new ConstructionStageInfo("test stage", ConstructionStageInfo.Stage.FOUNDATION, 10D, 10D, 
        		"length", false, 0,
                false, false, 10000D, 1, null, parts, resources, vehicles);
    }

    /*
     * Test method for 'com.mars_sim.simulation.structure.
     * construction.ConstructionStageInfo.getArchitectConstructionSkill()'
     */
    @Test
    void testGetArchitectConstructionSkill() {
        assertEquals(1, info.getArchitectConstructionSkill());
    }

    /*
     * Test method for 'com.mars_sim.simulation.structure.
     * construction.ConstructionStageInfo.getName()'
     */
    @Test
    void testGetName() {
        assertEquals("test stage", info.getName());
    }

    /*
     * Test method for 'com.mars_sim.simulation.structure.
     * construction.ConstructionStageInfo.getParts()'
     */
    @Test
    void testGetParts() {
        Map<Integer, Integer> parts = info.getParts();
        assertNotNull(parts);
        assertEquals(1, parts.size());
        Iterator<Integer> i = parts.keySet().iterator();
        while (i.hasNext()) {
        	Integer id = i.next();
        	Part part = ItemResourceUtil.findItemResource(id);
            assertEquals(ItemResourceUtil.BACKHOE_ID, part.getID());
            assertEquals(1, (int) parts.get(id));
        }
    }

    /*
     * Test method for 'com.mars_sim.simulation.structure.
     * construction.ConstructionStageInfo.getPrerequisiteStage()'
     */
    @Test
    void testGetPrerequisiteStage() {
        assertNull(info.getPrerequisiteStage());
    }

    /*
     * Test method for 'com.mars_sim.simulation.structure.
     * construction.ConstructionStageInfo.getResources()'
     */
    @Test
    void testGetResources() {
        Map<Integer, Double> resources = info.getResources();
        assertNotNull(resources);
        assertEquals(1, resources.size());
        Iterator<Integer> i = resources.keySet().iterator();
        while (i.hasNext()) {
            Integer id = i.next();
            AmountResource resource = ResourceUtil.findAmountResource(id);
            assertEquals( "Sand", resource.getName());
            assertEquals(PhaseType.SOLID, resource.getPhase());
            assertEquals(1D, resources.get(id));
        }
    }

    /*
     * Test method for 'com.mars_sim.simulation.structure.
     * construction.ConstructionStageInfo.getType()'
     */
    @Test
    void testGetType() {
        assertEquals(ConstructionStageInfo.Stage.FOUNDATION, info.getType());
    }

    /*
     * Test method for 'com.mars_sim.simulation.structure.
     * construction.ConstructionStageInfo.getVehicles()'
     */
    @Test
    void testGetVehicles() {
        List<ConstructionVehicleType> vehicles = info.getVehicles();
        assertEquals(1, vehicles.size());
        ConstructionVehicleType vehicle = vehicles.get(0);
        assertEquals(VehicleType.LUV, vehicle.getVehicleType());
        List<Integer> parts = vehicle.getAttachmentParts();
        assertEquals(1, parts.size());
    }

    /*
     * Test method for 'com.mars_sim.simulation.structure.
     * construction.ConstructionStageInfo.getWorkTime()'
     */
    @Test
    void testGetWorkTime() {
        assertEquals(10000D, info.getWorkTime());
    }
}