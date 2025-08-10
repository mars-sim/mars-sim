/**
 * Mars Simulation Project
 * ConstructionManagerTest.java
 * @version 3.1.0 2019-09-20
 * @author Scott Davis
 */

package com.mars_sim.core.building.construction;

import com.mars_sim.core.AbstractMarsSimUnitTest;

/**
 * Unit test for the ConstructionManager class.
 */
public class ConstructionManagerTest extends AbstractMarsSimUnitTest {

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
    public void testCreateNewBuildingSite() {
        var manager = buildManager();

        var buildSite = manager.createNewBuildingSite(getConfig().getBuildingConfiguration().getBuildingSpec(LANDER_HAB));

        assertEquals(1, manager.getConstructionSites().size());
        assertEquals(buildSite, manager.getConstructionSites().get(0));

        ConstructionSite site = manager.getNextConstructionSite(1);
        assertEquals(buildSite, site);
        assertNotNull("Has stage", site.getCurrentConstructionStage());
    }

    /*
     * Test method for 'com.mars_sim.simulation.structure.construction.
     * ConstructionManager.getConstructionSitesNeedingMission()'
     */
    public void testGetConstructionSitesNeedingMission() {
        var manager = buildManager();
        manager.addBuildingToQueue(LANDER_HAB, null);

        ConstructionSite site2 = manager.getNextConstructionSite(1);
        assertEquals(1, manager.getConstructionSitesNeedingMission(true).size());
        site2.setWorkOnSite(true);
        assertEquals(0, manager.getConstructionSitesNeedingMission(true).size());
    }

    /*
     * Test method for 'com.mars_sim.simulation.structure.construction.
     * ConstructionManager.createNewConstructionSite()'
     */
    public void testNoSiteWithoutQueue() {
        var manager = buildManager();
        ConstructionSite site = manager.getNextConstructionSite(1);
        assertNull(site);
    }

    /*
     * Test method queue
     */
    public void testAddQueue() {
        var manager = buildManager();

        assertTrue("Building schedule is empty", manager.getBuildingSchedule().isEmpty());

        manager.addBuildingToQueue(LANDER_HAB, null);
        var queue = manager.getBuildingSchedule();
        assertEquals("Queued buildings", 1, queue.size());
        var first = queue.get(0);
        assertEquals("Building type on queue", LANDER_HAB, first.getBuildingType());
        assertTrue("Build now", first.isReady());

        var site = manager.getNextConstructionSite(1);
        assertTrue("Building schedule becomes empty", manager.getBuildingSchedule().isEmpty());
        assertEquals("Site build", LANDER_HAB, site.getBuildingName());
    }

    /*
     * Test method queue
     */
    public void testAddFuture() {
        var s = buildSettlement("mgr", true);
        var manager = new ConstructionManager(s);

        // Calculate a time in the future
        var futureTime = getSim().getMasterClock().getMarsTime().addTime(500D);

        manager.addBuildingToQueue(LANDER_HAB, futureTime);
        var queue = manager.getBuildingSchedule();
        assertEquals("Queued buildings", 1, queue.size());
        var first = queue.get(0);
        assertEquals("Build future", futureTime, first.getStart());
        assertFalse("Build not ready", first.isReady());

        // Advance time
        var newPulse = createPulse(futureTime, false, false);
        s.getFutureManager().timePassing(newPulse);

        // Schedule should be now
        queue = manager.getBuildingSchedule();
        assertEquals("Queued buildings after puls", 1, queue.size());
        first = queue.get(0);
        assertTrue("Build ready", first.isReady());
    }
}