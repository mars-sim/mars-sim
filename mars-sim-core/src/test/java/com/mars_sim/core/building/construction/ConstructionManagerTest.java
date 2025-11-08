/**
 * Mars Simulation Project
 * ConstructionManagerTest.java
 * @version 3.1.0 2019-09-20
 * @author Scott Davis
 */

package com.mars_sim.core.building.construction;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.building.construction.ConstructionStageInfo.Stage;

/**
 * Unit test for the ConstructionManager class.
 */
public class ConstructionManagerTest extends MarsSimUnitTest {

    // Depends on buildings.xml
    private static final String LANDER_HAB = "Lander Hab";

    private ConstructionManager buildManager() {
        var s = buildSettlement("mgr", true);
        return new ConstructionManager(s);
    }
    /*
     * Test method for 'com.mars_sim.simulation.structure.construction.
     * ConstructionManager.getConstructionSites()'
     */
    @Test
    public void testCreateNewBuildingSite() {
        var manager = buildManager();

        var buildSite = manager.createNewBuildingSite(getConfig().getBuildingConfiguration().getBuildingSpec(LANDER_HAB));

        assertEquals(1, manager.getConstructionSites().size());
        assertEquals(buildSite, manager.getConstructionSites().get(0));

        ConstructionSite site = manager.getNextConstructionSite(1);
        assertEquals(buildSite, site);
        assertNotNull(site.getCurrentConstructionStage(), "Has stage");
    }

    /*
     * Test method for 'com.mars_sim.simulation.structure.construction.
     * ConstructionManager.getConstructionSitesNeedingMission()'
     */
    @Test
    public void testGetConstructionSitesNeedingMission() {
        var manager = buildManager();
        manager.addBuildingToQueue(LANDER_HAB, null);

        ConstructionSite site2 = manager.getNextConstructionSite(1);
        assertEquals(1, manager.getConstructionSitesNeedingMission().size());

        var mission = new MockMission();
        site2.setWorkOnSite(mission);
        assertEquals(0, manager.getConstructionSitesNeedingMission().size());
    }

    /*
     * Test method for 'com.mars_sim.simulation.structure.construction.
     * ConstructionManager.createNewConstructionSite()'
     */
    @Test
    public void testNoSiteWithoutQueue() {
        var manager = buildManager();
        ConstructionSite site = manager.getNextConstructionSite(1);
        assertNull(site);
    }

    /*
     * Test method queue
     */
    @Test
    public void testAddQueue() {
        var manager = buildManager();

        assertTrue(manager.getBuildingSchedule().isEmpty(), "Building schedule is empty");

        manager.addBuildingToQueue(LANDER_HAB, null);
        var queue = manager.getBuildingSchedule();
        assertEquals(1, queue.size(), "Queued buildings");
        var first = queue.get(0);
        assertEquals(LANDER_HAB, first.getBuildingType(), "Building type on queue");
        assertTrue(first.isReady(), "Build now");

        var site = manager.getNextConstructionSite(1);
        assertTrue(manager.getBuildingSchedule().isEmpty(), "Building schedule becomes empty");
        assertEquals(LANDER_HAB, site.getBuildingName(), "Site build");
    }

    /*
     * Test method queue
     */
    @Test
    public void testAddFuture() {
        var s = buildSettlement("mgr", true);
        var manager = new ConstructionManager(s);

        // Calculate a time in the future
        var futureTime = getSim().getMasterClock().getMarsTime().addTime(500D);

        manager.addBuildingToQueue(LANDER_HAB, futureTime);
        var queue = manager.getBuildingSchedule();
        assertEquals(1, queue.size(), "Queued buildings");
        var first = queue.get(0);
        assertEquals(futureTime, first.getStart(), "Build future");
        assertFalse(first.isReady(), "Build not ready");

        // Advance time
        var newPulse = createPulse(futureTime, false, false);
        s.getFutureManager().timePassing(newPulse);

        // Schedule should be now
        queue = manager.getBuildingSchedule();
        assertEquals(1, queue.size(), "Queued buildings after puls");
        first = queue.get(0);
        assertTrue(first.isReady(), "Build ready");
    }


    @Test
    public void testGetStages() {
        var manager = buildManager();
        var phases = manager.getConstructionStages(LANDER_HAB);

        assertEquals(3, phases.size(), "Phases");

        var foundation = phases.get(0);
        assertEquals(foundation.getName(), "Surface Foundation 9x9");
        assertEquals(Stage.FOUNDATION, foundation.getType());

        var frame = phases.get(1);
        assertEquals(frame.getName(), "Round Hab Frame");
        assertEquals(Stage.FRAME, frame.getType());

        var building = phases.get(2);
        assertEquals(LANDER_HAB, building.getName());
        assertEquals(Stage.BUILDING, building.getType());
    }

}
