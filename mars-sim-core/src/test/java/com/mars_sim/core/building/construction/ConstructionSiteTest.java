/**
 * Mars Simulation Project
 * ConstructionSiteTest.java
 * @version 3.1.0 2017-09-11
 * @author Scott Davis
 */

package com.mars_sim.core.building.construction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.map.location.BoundedObject;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.resource.ItemResource;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.Part;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.vehicle.VehicleType;

/**
 * Unit test for the ConstructionSite class.
 */
public class ConstructionSiteTest extends AbstractMarsSimUnitTest {

    private static final String WORKSHOP = "Workshop";
    private static final BoundedObject PLACE = new BoundedObject(LocalPosition.DEFAULT_POSITION,
                                        BUILDING_WIDTH, BUILDING_LENGTH, 0);

    // Data members
    ConstructionStageInfo foundationInfo = null;
    ConstructionStageInfo frameInfo = null;
    ConstructionStageInfo buildingInfo = null;

    @Override
    public void setUp() {
        super.setUp();
        
        Map<Integer, Integer> parts = new HashMap<>(1);
        
        Part ir = ItemResourceUtil.findItemResource(ItemResourceUtil.pneumaticDrillID);
        parts.put(ir.getID(), 1);

        Map<Integer, Double> resources = new HashMap<>(1);

        resources.put(ResourceUtil.SAND_ID, 1D);

        ItemResource atth = ItemResourceUtil.findItemResource(ItemResourceUtil.pneumaticDrillID);
        parts.put(atth.getID(), 1);

        var attachments = List.of(atth.getID());

        var vehicles = List.of(new ConstructionVehicleType(VehicleType.LUV, attachments));

        foundationInfo = new ConstructionStageInfo("test foundation info",
                ConstructionStageInfo.Stage.FOUNDATION, BUILDING_WIDTH, BUILDING_LENGTH, "length", false, 0, true, false, 10000D, 0, null, parts,
                resources, vehicles);

        frameInfo = new ConstructionStageInfo("test frame info",
                ConstructionStageInfo.Stage.FRAME, BUILDING_WIDTH, BUILDING_LENGTH, "length", false, 0, true, false, 10000D, 0, null, parts,
                resources, vehicles);

        buildingInfo = new ConstructionStageInfo("Workshop",
                ConstructionStageInfo.Stage.BUILDING, BUILDING_WIDTH, BUILDING_LENGTH, "length", false, 0, true, false, 10000D, 0, null, parts,
                resources, vehicles);
    }

    /*
     * Test method for 'com.mars_sim.simulation.structure.construction.
     * ConstructionSite.isAllConstructionComplete()'
     */
    public void testIsStageComplete() {
        var s = buildSettlement();

        var site = new ConstructionSite(s, WORKSHOP, true, foundationInfo, PLACE);

        assertEquals("Inital stage", foundationInfo, site.getCurrentConstructionStage().getInfo());

        var stage = site.getCurrentConstructionStage();
        assertEquals("No work time", 0D, stage.getCompletedWorkTime());
        assertFalse(stage.isComplete());

        stage.addWorkTime(100D);
        assertEquals("Some work time", 100D, stage.getCompletedWorkTime());
        assertFalse(stage.isComplete());

        stage.addWorkTime(100000D);
        assertEquals("All work time", stage.getRequiredWorkTime(), stage.getCompletedWorkTime());
        assertTrue(stage.isComplete());
    }

    /*
     * Test method for 'com.mars_sim.simulation.structure.construction.
     * ConstructionSite.isAllConstructionComplete()'
     */
    public void testIsSiteComplete() {
        var s = buildSettlement();

        var site = new ConstructionSite(s, WORKSHOP, true, foundationInfo, PLACE);

        assertEquals("Inital stage", foundationInfo, site.getCurrentConstructionStage().getInfo());
        site.getCurrentConstructionStage().addWorkTime(10000D);
        assertTrue(site.getCurrentConstructionStage().isComplete());
        assertFalse(site.isAllConstructionComplete());

        site.addNewStage(frameInfo);
        assertEquals("Frame stage", frameInfo, site.getCurrentConstructionStage().getInfo());
        site.getCurrentConstructionStage().addWorkTime(10000D);
        assertTrue(site.getCurrentConstructionStage().isComplete());
        assertFalse(site.isAllConstructionComplete());

        site.addNewStage(buildingInfo);
        assertEquals("Building stage", buildingInfo, site.getCurrentConstructionStage().getInfo());
        site.getCurrentConstructionStage().addWorkTime(10000D);
        assertTrue(site.getCurrentConstructionStage().isComplete());

        assertTrue(site.isAllConstructionComplete());
    }

    /*
     * Test method for 'com.mars_sim.simulation.structure.construction.
     * ConstructionSite.setUndergoingConstruction(boolean)'
     */
    public void testSetWorkOnSite() {
        var s = buildSettlement();

        var site = new ConstructionSite(s, WORKSHOP, true, foundationInfo, PLACE);

        site.setWorkOnSite(true);
        assertTrue(site.isWorkOnSite());

        site.setWorkOnSite(false);
        assertFalse(site.isWorkOnSite());
    }
}