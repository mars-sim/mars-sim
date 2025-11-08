package com.mars_sim.core.structure;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;


import java.util.Collections;

import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.CoordinatesException;
import com.mars_sim.core.map.location.CoordinatesFormat;
import com.mars_sim.core.mineral.RandomMineralFactory;

public class ExplorationManagerTest extends MarsSimUnitTest {
    @Test
    public void testCreateARegionOfInterest() {
        var s = buildSettlement("mock");

        var eMgr = new ExplorationManager(s);

        // Find a random location within 20K with minerals
        var mm = getSim().getSurfaceFeatures().getMineralMap();
        RandomMineralFactory.createLocalConcentration(mm, s.getCoordinates());
        var found = mm.findRandomMineralLocation(s.getCoordinates(), 20, Collections.emptyList());

        assertTrue(eMgr.getDeclaredROIs().isEmpty(), "No declared locations");

        // Create a site 1KM from the base
        var siteLocn = found.getKey();
        var newRoI = eMgr.createROI(siteLocn, 100);

        assertNotNull(newRoI, "New ROI created");
        assertEquals(siteLocn, newRoI.getCoordinates(), "New ROI coordinates");
        assertNull(newRoI.getOwner(), "New ROI settlement");
        assertFalse(newRoI.isClaimed(), "New ROI not claimed");
        assertEquals(1, eMgr.getDeclaredROIs().size(), "Declared locations");

        newRoI.setClaimed(s.getReportingAuthority());
        assertTrue(newRoI.isClaimed(), "New ROI claimed");

    }

     @Test
     public void testCreateUnclaimedARegionOfInterest() throws CoordinatesException {
        var locn = CoordinatesFormat.fromString("10.0 10.0");

        // Find a random location within 20K with minerals
        var sf = getSim().getSurfaceFeatures();
        var mm = sf.getMineralMap();
        RandomMineralFactory.createLocalConcentration(mm, locn);
        var found = mm.findRandomMineralLocation(locn, 20, Collections.emptyList());

        // Create a site 1KM from the base, no settlemetn as unclaimed
        var siteLocn = found.getKey();
        var newRoI = sf.createROI(siteLocn, 100);

        assertNotNull(newRoI, "New RoI created");
        assertEquals(siteLocn, newRoI.getCoordinates(), "New ROI coordinates");
        assertNull(newRoI.getOwner(), "New RoI has no settlement");
        assertFalse(newRoI.isClaimed(), "New RoI not claimed");
    }

    @Test
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

        assertEquals(3, eMgr.getNearbyMineralLocations().size(), "Nearby locations");
        assertEquals(2, eMgr.getDeclaredROIs().size(), "Declared locations at settlement");

        assertEquals(2, sf.getAllPossibleRegionOfInterestLocations().size(), "All locatinos");

        var claimedStats = eMgr.getStatistics(ExplorationManager.CLAIMED_STAT);
        assertEquals(dist1, claimedStats.mean(), "Claimed mean");

        var unclaimedStats = eMgr.getStatistics(ExplorationManager.UNCLAIMED_STAT);
        assertEquals(dist2, unclaimedStats.mean(), "Unclaimed mean");

        var siteStats = eMgr.getStatistics(ExplorationManager.SITE_STAT);
        assertEquals((dist1 + dist2)/2, siteStats.mean(), 0.00001, "Site mean");

        assertEquals(2, eMgr.getDeclaredROIs().size(), "Number of Mineral sites");
        assertEquals(1, eMgr.numDeclaredROIs(true), "Number of Claimed");
        assertEquals(1, eMgr.numDeclaredROIs(false), "Number of Unclaimed");

    }
}
