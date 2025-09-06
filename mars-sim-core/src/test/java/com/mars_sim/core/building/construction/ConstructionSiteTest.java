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
import com.mars_sim.core.building.construction.ConstructionSite.ConstructionPhase;
import com.mars_sim.core.map.location.BoundedObject;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.resource.ItemResource;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.SettlementParameters;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.VehicleType;

/**
 * Unit test for the ConstructionSite class.
 */
public class ConstructionSiteTest extends AbstractMarsSimUnitTest {

    private static final String WORKSHOP = "Workshop";
    private static final BoundedObject PLACE = new BoundedObject(LocalPosition.DEFAULT_POSITION,
                                        BUILDING_WIDTH, BUILDING_LENGTH, 0);
    private static final double PARTIAL_LOAD = 1D;

    // Data members
    private ConstructionStageInfo foundationInfo = null;
    private ConstructionStageInfo frameInfo = null;
    private ConstructionStageInfo buildingInfo = null;
    private List<ConstructionPhase> phases = null;

    @Override
    public void setUp() {
        super.setUp();
        
        Map<Integer, Integer> parts = new HashMap<>(1);
        ItemResource fiber = ItemResourceUtil.findItemResource(ItemResourceUtil.FIBERGLASS);        
        parts.put(fiber.getID(), 10 * (int)PARTIAL_LOAD);

        Map<Integer, Double> resources = new HashMap<>(1);
        resources.put(ResourceUtil.SAND_ID, 10 * PARTIAL_LOAD);

        ItemResource atth = ItemResourceUtil.findItemResource(ItemResourceUtil.pneumaticDrillID);
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

        phases = List.of(new ConstructionPhase(foundationInfo, true),
                        new ConstructionPhase(frameInfo, true),
                        new ConstructionPhase(buildingInfo, true));
    }

    public void testQuickConstruction() {
        var s = buildSettlement();
        s.getPreferences().putValue(SettlementParameters.INSTANCE, SettlementParameters.QUICK_CONST, Boolean.TRUE);

        var site = new ConstructionSite(s, "Site1", WORKSHOP, phases, PLACE);

        var stage = site.getCurrentConstructionStage();

        assertLessThan("Work Time", foundationInfo.getWorkTime(), stage.getRequiredWorkTime());

        var originalReqResources = foundationInfo.getResources();
        var sampleResource = RandomUtil.getARandSet(originalReqResources.keySet());
        var resources = stage.getResources();
        assertLessThan("Resource", foundationInfo.getResources().get(sampleResource),
                                            resources.get(sampleResource).getRequired());
    }

    public void testDemolish() {
        var s = buildSettlement();

        var demoPhases = List.of(new ConstructionPhase(buildingInfo, false));
        var site = new ConstructionSite(s, "Site1", WORKSHOP, demoPhases, PLACE);

        var stage = site.getCurrentConstructionStage();

        stage.reclaimParts(100);
        var parts = stage.getParts();
        for(var e : parts.entrySet()) {
            assertTrue("Reclaimed " + e.getKey(), e.getValue().getAvailable() > 0);
            assertTrue("Settlement " + e.getKey(), s.getItemResourceStored(e.getKey()) > 0);
        }
    }

    public void testConstructionResources() {
        var s = buildSettlement();
        var site = new ConstructionSite(s, "Site1", WORKSHOP, phases, PLACE);

        var stage = site.getCurrentConstructionStage();

        var resources = stage.getResources();
        var parts = stage.getParts();
        assertTrue("Stage without resources", stage.hasMissingConstructionMaterials());
        assertFalse("Load without resources", stage.loadAvailableConstructionMaterials(s));

        // Partial loading; 1 quantity of each material
        for(var e : resources.entrySet()) {
            var resId = e.getKey();
            var mat = e.getValue();
            assertEquals("Resource required " + resId, foundationInfo.getResources().get(resId), mat.getRequired());
            assertEquals("Resource missing " + resId, mat.getRequired(), mat.getMissing());

            s.storeAmountResource(resId, PARTIAL_LOAD);
        }
        for(var e : parts.entrySet()) {
            var partId = e.getKey();
            var mat = e.getValue();
            assertEquals("Part required " + partId, foundationInfo.getParts().get(partId).intValue(), (int)mat.getRequired());
            assertEquals("Part missing " + partId, mat.getRequired(), mat.getMissing());

            s.storeItemResource(partId, (int) PARTIAL_LOAD);
        }

        // Some loaded but not all
        assertFalse("Load without resources", stage.loadAvailableConstructionMaterials(s));

        // Load full amount
        for(var e : resources.entrySet()) {
            var resId = e.getKey();
            var mat = e.getValue();
            assertEquals("Resource partial required " + resId, PARTIAL_LOAD, mat.getAvailable());
            s.storeAmountResource(resId, mat.getMissing());
        }
        for(var e : parts.entrySet()) {
            var partId = e.getKey();
            var mat = e.getValue();
            assertEquals("Part partial required " + partId, PARTIAL_LOAD, mat.getAvailable());
            s.storeItemResource(partId, (int) mat.getMissing());
        }

        // Should nto complete load
        assertFalse("Stage has resources", stage.hasMissingConstructionMaterials());
        assertTrue("Load resources", stage.loadAvailableConstructionMaterials(s));
        var resMissing = resources.values().stream()
                            .mapToDouble(m -> m.getMissing())
                            .sum();
        assertEquals("No resources missing", 0D, resMissing);
        var partsMissing = parts.values().stream()
                            .mapToDouble(m -> m.getMissing())
                            .sum();
        assertEquals("No parts missing", 0D, partsMissing);
    }

    /*
     * Test method for 'com.mars_sim.simulation.structure.construction.
     * ConstructionSite.isAllConstructionComplete()'
     */
    public void testIsStageComplete() {
        var s = buildSettlement();

        var site = new ConstructionSite(s, "Site1", WORKSHOP, phases, PLACE);

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

        var site = new ConstructionSite(s, "Site2", WORKSHOP, phases, PLACE);

        assertEquals("Inital stage", foundationInfo, site.getCurrentConstructionStage().getInfo());
        site.getCurrentConstructionStage().addWorkTime(10000D);
        assertTrue(site.getCurrentConstructionStage().isComplete());
        assertFalse(site.isComplete());

        site.advanceToNextPhase();
        assertEquals("Frame stage", frameInfo, site.getCurrentConstructionStage().getInfo());
        site.getCurrentConstructionStage().addWorkTime(10000D);
        assertTrue(site.getCurrentConstructionStage().isComplete());
        assertFalse(site.isComplete());

        site.advanceToNextPhase();
        assertEquals("Building stage", buildingInfo, site.getCurrentConstructionStage().getInfo());
        site.getCurrentConstructionStage().addWorkTime(10000D);
        assertTrue(site.getCurrentConstructionStage().isComplete());

        assertTrue(site.isComplete());
    }

    /*
     * Test method for 'com.mars_sim.simulation.structure.construction.
     * ConstructionSite.setUndergoingConstruction(boolean)'
     */
    public void testSetWorkOnSite() {
        var s = buildSettlement();

        var site = new ConstructionSite(s, "Site3", WORKSHOP, phases, PLACE);

        var mission = new MockMission();
        site.setWorkOnSite(mission);
        assertEquals(mission, site.getWorkOnSite());

        site.setWorkOnSite(null);
        assertNull(site.getWorkOnSite());
    }
}