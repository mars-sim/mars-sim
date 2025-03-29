/**
 * Mars Simulation Project
 * ConstructionStageTest.java
 * @version 3.1.0 2017-09-11
 * @author Scott Davis
 */

package com.mars_sim.core.building.construction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.goods.GoodType;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.Part;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.MockSettlement;
import com.mars_sim.core.vehicle.VehicleType;

import junit.framework.TestCase;

/**
 * Test case for the ConstructionStage class.
 */
public class ConstructionStageTest extends TestCase {

    private static final String SMALL_HAMMER = "small hammer";

    // Data members
    private ConstructionStageInfo foundationInfo;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        SimulationConfig.loadConfig();
        Simulation.instance().testRun();

        Part smallHammer = (Part) ItemResourceUtil.findItemResource(SMALL_HAMMER);
        Map<Integer, Integer> parts = new HashMap<>(1);
        parts.put(smallHammer.getID(), 1);

        Map<Integer, Double> resources = new HashMap<>(1);
        resources.put(ResourceUtil.methaneAR.getID(), 1D);

        List<ConstructionVehicleType> vehicles =
            new ArrayList<>(1);
        List<Integer> attachments = new ArrayList<>(1);
        attachments.add(new Part("attachment part", 2, "test resource description", GoodType.UTILITY, 1D, 1).getID());
        vehicles.add(new ConstructionVehicleType(VehicleType.LUV, attachments));

        foundationInfo = new ConstructionStageInfo("test foundation info",
                ConstructionStageInfo.Stage.FOUNDATION, 10D, 10D, "length", false, 0, false, false, 10000D, 0, null,
                parts, resources, vehicles);
    }

    /*
     * Test method for 'com.mars_sim.simulation.structure
     * .construction.ConstructionStage.ConstructionStage(ConstructionStageInfo)'
     */
    public void testConstructionStage() {
        ConstructionSite site = new ConstructionSite(new MockSettlement());
        ConstructionStage stage = new ConstructionStage(foundationInfo, site);
        assertNotNull(stage);
    }

    /*
     * Test method for 'com.mars_sim.simulation.structure
     * .construction.ConstructionStage.getInfo()'
     */
    public void testGetInfo() {
        ConstructionSite site = new ConstructionSite(new MockSettlement());
        ConstructionStage stage = new ConstructionStage(foundationInfo, site);
        assertEquals(foundationInfo, stage.getInfo());
    }

    /*
     * Test method for 'com.mars_sim.simulation.structure
     * .construction.ConstructionStage.getCompletedWorkTime()'
     */
    public void testGetCompletedWorkTime() {
        ConstructionSite site = new ConstructionSite(new MockSettlement());
        ConstructionStage stage = new ConstructionStage(foundationInfo, site);
        assertEquals(0D, stage.getCompletedWorkTime());
    }

    /*
     * Test method for 'com.mars_sim.simulation.structure
     * .construction.ConstructionStage.addWorkTime(double)'
     */
    public void testAddWorkTime() {
        ConstructionSite site = new ConstructionSite(new MockSettlement());
        ConstructionStage stage = new ConstructionStage(foundationInfo, site);
        stage.addWorkTime(5000D);
        assertEquals(5000D, stage.getCompletedWorkTime());
        stage.addWorkTime(5000D);
        assertEquals(10000D, stage.getCompletedWorkTime());
        stage.addWorkTime(5000D);
        assertEquals(10000D, stage.getCompletedWorkTime());
    }

    /*
     * Test method for 'com.mars_sim.simulation.structure
     * .construction.ConstructionStage.isComplete()'
     */
    public void testIsComplete() {
        ConstructionSite site = new ConstructionSite(new MockSettlement());
        ConstructionStage stage = new ConstructionStage(foundationInfo, site);
        stage.addWorkTime(5000D);
        assertFalse(stage.isComplete());
        stage.addWorkTime(10000D);
        assertTrue(stage.isComplete());
    }
}