/**
 * Mars Simulation Project
 * ConstructionSiteTest.java
 * @version 3.1.0 2017-09-11
 * @author Scott Davis
 */

package com.mars_sim.core.building.construction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mars_sim.core.building.construction.ConstructionSite.ConstructionPhase;
import com.mars_sim.core.map.location.BoundedObject;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.resource.ItemResource;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.SettlementParameters;
import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.VehicleType;

/**
 * Unit test for the ConstructionSite class.
 */
public class ConstructionSiteTest extends MarsSimUnitTest {

    private static final String WORKSHOP = "Workshop";
    private static final int BUILDING_WIDTH = 10;
    private static final int BUILDING_LENGTH = 20;
    private static final BoundedObject PLACE = new BoundedObject(LocalPosition.DEFAULT_POSITION,
                                        BUILDING_WIDTH, BUILDING_LENGTH, 0);
    private static final double PARTIAL_LOAD = 1D;

    // Data members
    private ConstructionStageInfo foundationInfo = null;
    private ConstructionStageInfo frameInfo = null;
    private ConstructionStageInfo buildingInfo = null;
    private List<ConstructionPhase> phases = null;

    @BeforeEach
    @Override
    public void init() {
        super.init();
        
        Map<Integer, Integer> parts = new HashMap<>(1);
        ItemResource fiber = ItemResourceUtil.findItemResource(ItemResourceUtil.FIBERGLASS_ID);        
        parts.put(fiber.getID(), 10 * (int)PARTIAL_LOAD);

        Map<Integer, Double> resources = new HashMap<>(1);
        resources.put(ResourceUtil.SAND_ID, 10 * PARTIAL_LOAD);

        ItemResource atth = ItemResourceUtil.findItemResource(ItemResourceUtil.PNEUMATIC_DRILL_ID);
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

    @Test
    void testQuickConstruction() {
        var s = buildSettlement("quick");
        s.getPreferences().putValue(SettlementParameters.QUICK_CONST, Boolean.TRUE);

        var site = new ConstructionSite(s, "Site1", WORKSHOP, phases, PLACE);

        var stage = site.getCurrentConstructionStage();

        assertTrue(stage.getRequiredWorkTime() < foundationInfo.getWorkTime(), "Work Time");

        var originalReqResources = foundationInfo.getResources();
        var sampleResource = RandomUtil.getARandSet(originalReqResources.keySet());
        var resources = stage.getResources();
        assertTrue(resources.get(sampleResource).getRequired() < foundationInfo.getResources().get(sampleResource), 
                   "Resource");
    }

    @Test
    public void testDemolish() {
        var s = buildSettlement("demo");

        var demoPhases = List.of(new ConstructionPhase(buildingInfo, false));
        var site = new ConstructionSite(s, "Site1", WORKSHOP, demoPhases, PLACE);

        var stage = site.getCurrentConstructionStage();

        stage.reclaimParts(100);
        var parts = stage.getParts();
        for(var e : parts.entrySet()) {
            assertTrue(e.getValue().getAvailable() > 0, "Reclaimed " + e.getKey());
            assertTrue(s.getItemResourceStored(e.getKey()) > 0, "Settlement " + e.getKey());
        }
    }

    @Test
    public void testConstructionResources() {
        var s = buildSettlement("const");
        var site = new ConstructionSite(s, "Site1", WORKSHOP, phases, PLACE);

        var stage = site.getCurrentConstructionStage();

        var resources = stage.getResources();
        var parts = stage.getParts();
        assertTrue(stage.hasMissingConstructionMaterials(), "Stage without resources");
        assertFalse(stage.loadAvailableConstructionMaterials(s), "Load without resources");

        // Partial loading; 1 quantity of each material
        for(var e : resources.entrySet()) {
            var resId = e.getKey();
            var mat = e.getValue();
            assertEquals(foundationInfo.getResources().get(resId), mat.getRequired(), "Resource required " + resId);
            assertEquals(mat.getRequired(), mat.getMissing(), "Resource missing " + resId);

            s.storeAmountResource(resId, PARTIAL_LOAD);
        }
        for(var e : parts.entrySet()) {
            var partId = e.getKey();
            var mat = e.getValue();
            assertEquals(foundationInfo.getParts().get(partId).intValue(), (int)mat.getRequired(), "Part required " + partId);
            assertEquals(mat.getRequired(), mat.getMissing(), "Part missing " + partId);

            s.storeItemResource(partId, (int) PARTIAL_LOAD);
        }

        // Some loaded but not all
        assertFalse(stage.loadAvailableConstructionMaterials(s), "Load without resources");

        // Load full amount
        for(var e : resources.entrySet()) {
            var resId = e.getKey();
            var mat = e.getValue();
            assertEquals(PARTIAL_LOAD, mat.getAvailable(), "Resource partial required " + resId);
            s.storeAmountResource(resId, mat.getMissing());
        }
        for(var e : parts.entrySet()) {
            var partId = e.getKey();
            var mat = e.getValue();
            assertEquals(PARTIAL_LOAD, mat.getAvailable(), "Part partial required " + partId);
            s.storeItemResource(partId, (int) mat.getMissing());
        }

        // Should nto complete load
        assertFalse(stage.hasMissingConstructionMaterials(), "Stage has resources");
        assertTrue(stage.loadAvailableConstructionMaterials(s), "Load resources");
        var resMissing = resources.values().stream()
                            .mapToDouble(m -> m.getMissing())
                            .sum();
        assertEquals(0D, resMissing, "No resources missing");
        var partsMissing = parts.values().stream()
                            .mapToDouble(m -> m.getMissing())
                            .sum();
        assertEquals(0D, partsMissing, "No parts missing");
    }

    /*
     * Test method for 'com.mars_sim.simulation.structure.construction.
     * ConstructionSite.isAllConstructionComplete()'
     */
    @Test
    public void testIsStageComplete() {
        var s = buildSettlement("stage");

        var site = new ConstructionSite(s, "Site1", WORKSHOP, phases, PLACE);

        assertEquals(foundationInfo, site.getCurrentConstructionStage().getInfo(), "Initial stage");

        var stage = site.getCurrentConstructionStage();
        assertEquals(0D, stage.getCompletedWorkTime(), "No work time");
        assertFalse(stage.isComplete());

        stage.addWorkTime(100D);
        assertEquals(100D, stage.getCompletedWorkTime(), "Some work time");
        assertFalse(stage.isComplete());

        stage.addWorkTime(100000D);
        assertEquals(stage.getRequiredWorkTime(), stage.getCompletedWorkTime(), "All work time");
        assertTrue(stage.isComplete());
    }

    /*
     * Test method for 'com.mars_sim.simulation.structure.construction.
     * ConstructionSite.isAllConstructionComplete()'
     */
    @Test
    public void testIsSiteComplete() {
        var s = buildSettlement("stage" );

        var site = new ConstructionSite(s, "Site2", WORKSHOP, phases, PLACE);

        assertEquals(foundationInfo, site.getCurrentConstructionStage().getInfo(), "Initial stage");
        site.getCurrentConstructionStage().addWorkTime(10000D);
        assertTrue(site.getCurrentConstructionStage().isComplete());
        assertFalse(site.isComplete());

        site.advanceToNextPhase();
        assertEquals(frameInfo, site.getCurrentConstructionStage().getInfo(), "Frame stage");
        site.getCurrentConstructionStage().addWorkTime(10000D);
        assertTrue(site.getCurrentConstructionStage().isComplete());
        assertFalse(site.isComplete());

        site.advanceToNextPhase();
        assertEquals(buildingInfo, site.getCurrentConstructionStage().getInfo(), "Building stage");
        site.getCurrentConstructionStage().addWorkTime(10000D);
        assertTrue(site.getCurrentConstructionStage().isComplete());

        assertTrue(site.isComplete());
    }

    /*
     * Test method for 'com.mars_sim.simulation.structure.construction.
     * ConstructionSite.setUndergoingConstruction(boolean)'
     */
    @Test
    public void testSetWorkOnSite() {
        var s = buildSettlement("work");

        var site = new ConstructionSite(s, "Site3", WORKSHOP, phases, PLACE);

        var mission = new MockMission();
        site.setWorkOnSite(mission);
        assertEquals(mission, site.getWorkOnSite());

        site.setWorkOnSite(null);
        assertNull(site.getWorkOnSite());
    }
}