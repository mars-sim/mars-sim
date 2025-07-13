package com.mars_sim.core.structure;


import java.util.Collections;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.mineral.RandomMineralFactory;

public class ExplorationManagerTest extends AbstractMarsSimUnitTest {
    public void testCreateARegionOfInterest() {
        var s = buildSettlement();

        var eMgr = new ExplorationManager(s);

        // Find a random location within 20K with minerals
        var mm = getSim().getSurfaceFeatures().getMineralMap();
        RandomMineralFactory.createLocalConcentration(mm, s.getCoordinates());
        var found = mm.findRandomMineralLocation(s.getCoordinates(), 20, Collections.emptyList());

        assertTrue("No declared locations", eMgr.getDeclaredROIs().isEmpty());

        // Create a site 1KM from the base
        var siteLocn = found.getKey();
        var newRoI = eMgr.createROI(siteLocn, 100);

        assertNotNull("New RoI created", newRoI);
        assertEquals("New ROI coordinates", siteLocn, newRoI.getCoordinates());
        assertNull("New RoI settlement", newRoI.getOwner());
        assertFalse("New RoI not claimed", newRoI.isClaimed());
        assertEquals("Declared locations", 1, eMgr.getDeclaredROIs().size());

        newRoI.setClaimed(s.getReportingAuthority());
        assertTrue("New RoI claimed", newRoI.isClaimed());

    }

     public void testCreateUnclaimedARegionOfInterest() {
        var locn = new Coordinates("10N", "10E");

        // Find a random location within 20K with minerals
        var sf = getSim().getSurfaceFeatures();
        var mm = sf.getMineralMap();
        RandomMineralFactory.createLocalConcentration(mm, locn);
        var found = mm.findRandomMineralLocation(locn, 20, Collections.emptyList());

        // Create a site 1KM from the base, no settlemetn as unclaimed
        var siteLocn = found.getKey();
        var newRoI = sf.createROI(siteLocn, 100);

        assertNotNull("New RoI created", newRoI);
        assertEquals("New ROI coordinates", siteLocn, newRoI.getCoordinates());
        assertNull("New RoI has no settlement", newRoI.getOwner());
        assertFalse("New RoI not claimed", newRoI.isClaimed());
    }

    public void testStatistics() {
        var s = buildSettlement("Test", false, Coordinates.getRandomLocation());

        var eMgr = new ExplorationManager(s);

        // Find a random location within 20K with minerals
        var sf = getSim().getSurfaceFeatures();
        var mm = sf.getMineralMap();
        RandomMineralFactory.createLocalConcentration(mm, s.getCoordinates());

        eMgr.acquireNearbyMineralLocation(100);

        var place = eMgr.acquireNearbyMineralLocation(100);
        var l = eMgr.createROI(place, 100);
        eMgr.claimSite(l);
        var dist1 = place.getDistance(s.getCoordinates());

        place = eMgr.acquireNearbyMineralLocation(100);
        eMgr.createROI(place, 100);
        var dist2 = place.getDistance(s.getCoordinates());

        assertEquals("Nearby locations", 3, eMgr.getNearbyMineralLocations().size());
        assertEquals("Declared locations at settlement", 2, eMgr.getDeclaredROIs().size());

        assertEquals("All locatinos", 2, sf.getAllPossibleRegionOfInterestLocations().size());

        var claimedStats = eMgr.getStatistics(ExplorationManager.CLAIMED_STAT);
        assertEquals("Claimed mean", dist1, claimedStats.mean());

        var unclaimedStats = eMgr.getStatistics(ExplorationManager.UNCLAIMED_STAT);
        assertEquals("Unclaimed mean", dist2, unclaimedStats.mean());

        var siteStats = eMgr.getStatistics(ExplorationManager.SITE_STAT);
        assertEquals("Site mean", (dist1 + dist2)/2, siteStats.mean(), 0.00001);

        assertEquals("Number of Mineral sites", 2, eMgr.getDeclaredROIs().size());
        assertEquals("Number of Claimed", 1, eMgr.numDeclaredROIs(true));
        assertEquals("Number of Unclaimed", 1, eMgr.numDeclaredROIs(false));

    }
}
