/**
 * Mars Simulation Project
 * ConstructionManagerTest.java
 * @version 2.85 2008-08-12
 * @author Scott Davis
 */

package org.mars_sim.msp.core.structure.construction;

import org.mars_sim.msp.core.structure.MockSettlement;
import org.mars_sim.msp.core.structure.Settlement;

import junit.framework.TestCase;

/**
 * Unit test for the ConstructionManager class.
 */
public class ConstructionManagerTest extends TestCase {

    // Data members
    ConstructionManager manager;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        Settlement settlement = new MockSettlement();
        manager = new ConstructionManager(settlement);
    }

    /*
     * Test method for 'org.mars_sim.msp.simulation.structure.construction.
     * ConstructionManager.getConstructionSites()'
     */
    public void testGetConstructionSites() {
        ConstructionSite site = manager.createNewConstructionSite();
        manager.createNewConstructionSite();
        assertEquals(2, manager.getConstructionSites().size());
        assertEquals(site, manager.getConstructionSites().get(0));
    }

    /*
     * Test method for 'org.mars_sim.msp.simulation.structure.construction.
     * ConstructionManager.getConstructionSitesNeedingMission()'
     */
    public void testGetConstructionSitesNeedingMission() {
        manager.createNewConstructionSite();
        ConstructionSite site2 = manager.createNewConstructionSite();
        manager.createNewConstructionSite();
        assertEquals(3, manager.getConstructionSitesNeedingMission().size());
        site2.setUndergoingConstruction(true);
        assertEquals(2, manager.getConstructionSitesNeedingMission().size());
    }

    /*
     * Test method for 'org.mars_sim.msp.simulation.structure.construction.
     * ConstructionManager.createNewConstructionSite()'
     */
    public void testCreateNewConstructionSite() {
        ConstructionSite site = manager.createNewConstructionSite();
        assertNotNull(site);
    }

    /*
     * Test method for 'org.mars_sim.msp.simulation.structure.construction.
     * ConstructionManager.getConstructionValues()'
     */
    public void testGetConstructionValues() {
        ConstructionValues values = manager.getConstructionValues();
        assertNotNull(values);
    }
}