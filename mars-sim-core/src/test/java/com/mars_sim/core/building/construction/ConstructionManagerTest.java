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

    private ConstructionManager buildManager() {
        var s = buildSettlement("mgr", true);
        return new ConstructionManager(s);
    }
    /*
     * Test method for 'com.mars_sim.simulation.structure.construction.
     * ConstructionManager.getConstructionSites()'
     */
    public void testGetConstructionSites() {
        var manager = buildManager();
        ConstructionSite site = manager.getNextConstructionSite(1);
        assertEquals(1, manager.getConstructionSites().size());
        assertEquals(site, manager.getConstructionSites().get(0));

        assertNotNull("Has stage", site.getCurrentConstructionStage());
    }

    /*
     * Test method for 'com.mars_sim.simulation.structure.construction.
     * ConstructionManager.getConstructionSitesNeedingMission()'
     */
    public void testGetConstructionSitesNeedingMission() {
        var manager = buildManager();
        ConstructionSite site2 = manager.getNextConstructionSite(1);
        assertEquals(1, manager.getConstructionSitesNeedingMission(true).size());
        site2.setWorkOnSite(true);
        assertEquals(0, manager.getConstructionSitesNeedingMission(true).size());
    }

    /*
     * Test method for 'com.mars_sim.simulation.structure.construction.
     * ConstructionManager.createNewConstructionSite()'
     */
    public void testCreateNewConstructionSite() {
        var manager = buildManager();
        ConstructionSite site = manager.getNextConstructionSite(1);
        assertNotNull(site);
    }
}