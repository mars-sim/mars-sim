package com.mars_sim.core.structure;


import java.util.Collections;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.mineral.RandomMineralFactory;

public class ExplorationManagerTest extends AbstractMarsSimUnitTest {
    public void testCreateARegionOfInterest() {
        var s = buildSettlement();

        var eMgr = new ExplorationManager(s);

        // Find a random location within 20K with minerals
        var mm = getSim().getSurfaceFeatures().getMineralMap();
        RandomMineralFactory.createLocalConcentration(mm, s.getCoordinates());
        var found = mm.findRandomMineralLocation(s.getCoordinates(), 20, Collections.emptyList());

        assertTrue("No declared locations", eMgr.getDeclaredLocations().isEmpty());

        // Create a site 1KM from the base
        var siteLocn = found.getKey();
        var newRoI = eMgr.createARegionOfInterest(siteLocn, 100);

        assertNotNull("New RoI created", newRoI);
        assertEquals("New ROI coordinates", siteLocn, newRoI.getCoordinates());
        assertEquals("New RoI settlement", s, newRoI.getSettlement());
        assertFalse("New RoI not claimed", newRoI.isClaimed());
        assertEquals("Declared locations", 1, eMgr.getDeclaredLocations().size());

        newRoI.setClaimed(true);
        assertTrue("New RoI claimed", newRoI.isClaimed());
    }

    public void testStatistics() {
        var s = buildSettlement();

        var eMgr = new ExplorationManager(s);

        // Find a random location within 20K with minerals
        var mm = getSim().getSurfaceFeatures().getMineralMap();
        RandomMineralFactory.createLocalConcentration(mm, s.getCoordinates());

        var place = eMgr.acquireNearbyMineralLocation(100);
        eMgr.createARegionOfInterest(place, 100);
        var dist1 = place.getDistance(s.getCoordinates());

        place = eMgr.acquireNearbyMineralLocation(100);
        var newRoI2 = eMgr.createARegionOfInterest(place, 100);
        newRoI2.setClaimed(true);
        var dist2 = place.getDistance(s.getCoordinates());

        assertEquals("Nearby locations", 2, eMgr.getNearbyMineralLocations().size());
        assertEquals("Declared locations", 2, eMgr.getDeclaredLocations().size());

        var claimedStats = eMgr.getStatistics(ExplorationManager.CLAIMED_STAT);
        assertEquals("Claimed mean", dist2, claimedStats.mean());

        var unclaimedStats = eMgr.getStatistics(ExplorationManager.UNCLAIMED_STAT);
        assertEquals("Unclaimed mean", dist1, unclaimedStats.mean());

        var siteStats = eMgr.getStatistics(ExplorationManager.SITE_STAT);
        assertEquals("Site mean", (dist1 + dist2)/2, siteStats.mean());

        assertEquals("Number of Declared", 2, eMgr.numDeclaredLocation());
        assertEquals("Number of Claimed", 1, eMgr.numDeclaredLocation(true));
        assertEquals("Number of Unclaimed", 1, eMgr.numDeclaredLocation(false));

    }
}
