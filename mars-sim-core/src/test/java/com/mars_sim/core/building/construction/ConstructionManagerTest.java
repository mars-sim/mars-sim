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
        ConstructionSite site = manager.getNextSite(1);
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
        ConstructionSite site2 = manager.getNextSite(1);
        assertEquals(1, manager.getConstructionSitesNeedingConstructionMission().size());
        site2.setUndergoingConstruction(true);
        assertEquals(0, manager.getConstructionSitesNeedingConstructionMission().size());
    }

    /*
     * Test method for 'com.mars_sim.simulation.structure.construction.
     * ConstructionManager.createNewConstructionSite()'
     */
    public void testCreateNewConstructionSite() {
        var manager = buildManager();
        ConstructionSite site = manager.getNextSite(1);
        assertNotNull(site);
    }

    /*
     * Test method for 'com.mars_sim.simulation.structure.construction.
     * ConstructionManager.getConstructionValues()'
     */
    public void testGetConstructionValues() {
        var manager = buildManager();
        ConstructionValues values = manager.getConstructionValues();
        assertNotNull(values);
    }
}