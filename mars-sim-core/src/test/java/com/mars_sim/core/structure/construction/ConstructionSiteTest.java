/**
 * Mars Simulation Project
 * ConstructionSiteTest.java
 * @version 3.1.0 2017-09-11
 * @author Scott Davis
 */

package com.mars_sim.core.structure.construction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.resource.AmountResource;
import com.mars_sim.core.resource.ItemResource;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.Part;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.LightUtilityVehicle;

/**
 * Unit test for the ConstructionSite class.
 */
public class ConstructionSiteTest extends AbstractMarsSimUnitTest {

    // Data members
    ConstructionSite site = null;
    ConstructionStage foundationStage = null;
    ConstructionStage frameStage = null;
    ConstructionStage buildingStage = null;

    @Override
    public void setUp() {
        super.setUp();
        
        Settlement settlement = buildSettlement();
        site = new ConstructionSite(settlement);

        Map<Integer, Integer> parts = new HashMap<>(1);
        
        Part ir = ItemResourceUtil.findItemResource(ItemResourceUtil.pneumaticDrillID);
        parts.put(ir.getID(), 1);

        Map<Integer, Double> resources = new HashMap<>(1);

        AmountResource ar = ResourceUtil.sandAR;
        resources.put(ar.getID(), 1D);

        List<ConstructionVehicleType> vehicles =
            new ArrayList<>(1);
        List<Integer> attachments = new ArrayList<>(1);        
        ItemResource atth = ItemResourceUtil.findItemResource(ItemResourceUtil.pneumaticDrillID);
        parts.put(atth.getID(), 1);

        attachments.add(atth.getID());

        vehicles.add(new ConstructionVehicleType("Light Utility Vehicle", LightUtilityVehicle.class,
                attachments));

        ConstructionStageInfo foundationInfo = new ConstructionStageInfo("test foundation info",
                ConstructionStageInfo.Stage.FOUNDATION, 10D, 10D, "length", false, 0, false, false, 10000D, 0, null, parts,
                resources, vehicles);
        foundationStage = new ConstructionStage(foundationInfo, site);

        ConstructionStageInfo frameInfo = new ConstructionStageInfo("test frame info",
                ConstructionStageInfo.Stage.FRAME, 10D, 10D, "length", false, 0, false, false, 10000D, 0, null, parts,
                resources, vehicles);
        frameStage = new ConstructionStage(frameInfo, site);

        ConstructionStageInfo buildingInfo = new ConstructionStageInfo("Workshop",
                ConstructionStageInfo.Stage.BUILDING, 10D, 10D, "length", false, 0, false, false, 10000D, 0, null, parts,
                resources, vehicles);
        buildingStage = new ConstructionStage(buildingInfo, site);
    }

    /*
     * Test method for 'com.mars_sim.simulation.structure.construction.
     * ConstructionSite.isAllConstructionComplete()'
     */
    public void testIsAllConstructionComplete() {

            site.addNewStage(foundationStage);
            foundationStage.addWorkTime(10000D);
            assertTrue(foundationStage.isComplete());

            site.addNewStage(frameStage);
            frameStage.addWorkTime(10000D);
            assertTrue(frameStage.isComplete());

            site.addNewStage(buildingStage);
            buildingStage.addWorkTime(10000D);
            assertTrue(buildingStage.isComplete());

            assertTrue(site.isAllConstructionComplete());
    }

    /*
     * Test method for 'com.mars_sim.simulation.structure.construction.
     * ConstructionSite.setUndergoingConstruction(boolean)'
     */
    public void testSetUndergoingConstruction() {
        assertFalse(site.isUndergoingConstruction());

        site.setUndergoingConstruction(true);
        assertTrue(site.isUndergoingConstruction());

        site.setUndergoingConstruction(false);
        assertFalse(site.isUndergoingConstruction());
    }

    /*
     * Test method for 'com.mars_sim.simulation.structure.construction.
     * ConstructionSite.getCurrentConstructionStage()'
     */
    public void testGetCurrentConstructionStage() {
        assertNull(site.getCurrentConstructionStage());

        site.addNewStage(foundationStage);
        assertEquals(foundationStage, site.getCurrentConstructionStage());

        site.addNewStage(frameStage);
        assertEquals(frameStage, site.getCurrentConstructionStage());

        site.addNewStage(buildingStage);
        assertEquals(buildingStage, site.getCurrentConstructionStage());
    }

    /*
     * Test method for 'com.mars_sim.simulation.structure.construction.
     * ConstructionSite.addNewStage(ConstructionStage)'
     */
    public void testAddNewStage() {
        site.addNewStage(foundationStage);
        assertEquals(foundationStage, site.getCurrentConstructionStage());

        site.addNewStage(frameStage);
        assertEquals(frameStage, site.getCurrentConstructionStage());

        site.addNewStage(buildingStage);
        assertEquals(buildingStage, site.getCurrentConstructionStage());
    }
}