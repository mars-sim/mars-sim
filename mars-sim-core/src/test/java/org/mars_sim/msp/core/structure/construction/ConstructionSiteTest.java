/**
 * Mars Simulation Project
 * ConstructionSiteTest.java
 * @version 3.07 2014-08-22
 * @author Scott Davis
 */

package org.mars_sim.msp.core.structure.construction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.Phase;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;
//import org.mars_sim.msp.simulation.structure.MockSettlement;
//import org.mars_sim.msp.simulation.structure.Settlement;
//import org.mars_sim.msp.simulation.structure.building.Building;

import junit.framework.TestCase;

/**
 * Unit test for the ConstructionSite class.
 */
public class ConstructionSiteTest extends TestCase {

    // Data members
    ConstructionSite site = null;
    ConstructionStage foundationStage = null;
    ConstructionStage frameStage = null;
    ConstructionStage buildingStage = null;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        site = new ConstructionSite(Settlement.createConstructionStage());

        Map<Part, Integer> parts = new HashMap<Part, Integer>(1);
        parts.put(new Part("test part", 1, "test resource description", 1D, 1), 1);

        Map<AmountResource, Double> resources = new HashMap<AmountResource, Double>(1);
        resources.put(new AmountResource(1, "test resource", "test type", "test resource description", Phase.SOLID, false, false), 1D);

        List<ConstructionVehicleType> vehicles =
            new ArrayList<ConstructionVehicleType>(1);
        List<Part> attachments = new ArrayList<Part>(1);
        attachments.add(new Part("attachment part", 2, "test resource description", 1D, 1));
        vehicles.add(new ConstructionVehicleType("Light Utility Vehicle", LightUtilityVehicle.class,
                attachments));

        ConstructionStageInfo foundationInfo = new ConstructionStageInfo("test foundation info",
                ConstructionStageInfo.FOUNDATION, 10D, 10D, false, 0, false, false, 10000D, 0, null, parts,
                resources, vehicles);
        foundationStage = new ConstructionStage(foundationInfo, site);

        ConstructionStageInfo frameInfo = new ConstructionStageInfo("test frame info",
                ConstructionStageInfo.FRAME, 10D, 10D, false, 0, false, false, 10000D, 0, null, parts,
                resources, vehicles);
        frameStage = new ConstructionStage(frameInfo, site);

        ConstructionStageInfo buildingInfo = new ConstructionStageInfo("Workshop",
                ConstructionStageInfo.BUILDING, 10D, 10D, false, 0, false, false, 10000D, 0, null, parts,
                resources, vehicles);
        buildingStage = new ConstructionStage(buildingInfo, site);
    }

    /*
     * Test method for 'org.mars_sim.msp.simulation.structure.construction.
     * ConstructionSite.isAllConstructionComplete()'
     */
    public void testIsAllConstructionComplete() {

        try {
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
        catch (Exception e) {
            fail(e.getMessage());
        }
    }

    /*
     * Test method for 'org.mars_sim.msp.simulation.structure.construction.
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
     * Test method for 'org.mars_sim.msp.simulation.structure.construction.
     * ConstructionSite.getCurrentConstructionStage()'
     */
    public void testGetCurrentConstructionStage() {
        assertNull(site.getCurrentConstructionStage());

        try {
            site.addNewStage(foundationStage);
            assertEquals(foundationStage, site.getCurrentConstructionStage());

            site.addNewStage(frameStage);
            assertEquals(frameStage, site.getCurrentConstructionStage());

            site.addNewStage(buildingStage);
            assertEquals(buildingStage, site.getCurrentConstructionStage());
        }
        catch (Exception e) {
            fail(e.getMessage());
        }
    }

    /*
     * Test method for 'org.mars_sim.msp.simulation.structure.construction.
     * ConstructionSite.getNextStageType()'
     */
    public void testGetNextStageType() {
        assertEquals(ConstructionStageInfo.FOUNDATION, site.getNextStageType());

        try {
            site.addNewStage(foundationStage);
            assertEquals(ConstructionStageInfo.FRAME, site.getNextStageType());

            site.addNewStage(frameStage);
            assertEquals(ConstructionStageInfo.BUILDING, site.getNextStageType());

            site.addNewStage(buildingStage);
            assertNull(site.getNextStageType());
        }
        catch (Exception e) {
            fail(e.getMessage());
        }
    }

    /*
     * Test method for 'org.mars_sim.msp.simulation.structure.construction.
     * ConstructionSite.addNewStage(ConstructionStage)'
     */
    public void testAddNewStage() {
        try {
            site.addNewStage(foundationStage);
            assertEquals(foundationStage, site.getCurrentConstructionStage());

            site.addNewStage(frameStage);
            assertEquals(frameStage, site.getCurrentConstructionStage());

            site.addNewStage(buildingStage);
            assertEquals(buildingStage, site.getCurrentConstructionStage());
        }
        catch (Exception e) {
            fail(e.getMessage());
        }
    }

    /*
     * Test method for 'org.mars_sim.msp.simulation.structure.construction.
     * ConstructionSite.createBuilding(BuildingManager)'
     */
    // Note: commenting out for now.  Fails on null pointer exception thrown by
    // testSite.createBuilding() due to use of MarsClock from uninitialized Simulation.
    /*
    public void testCreateBuilding() {
        try {
            Settlement settlement = new MockSettlement();
            ConstructionManager constructionManager = settlement.getConstructionManager();
            ConstructionSite testSite = constructionManager.createNewConstructionSite();
            testSite.addNewStage(foundationStage);
            testSite.addNewStage(frameStage);
            testSite.addNewStage(buildingStage);
            Building building = testSite.createBuilding(settlement.getBuildingManager());
            assertNotNull(building);
        }
        catch (Exception e) {
            fail(e.getMessage());
        }
    }
    */
}