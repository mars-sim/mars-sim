package com.mars_sim.core.building.construction;

import static org.junit.Assert.assertNotEquals;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.map.location.LocalPosition;

public class BuildingPlacementTest extends AbstractMarsSimUnitTest {

    public void testPositionSite() {
        // Build one other building to force a placement activitiy
        var s = buildSettlement("position", true);
        buildAccommodation(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D, 0);

        var placement = new BuildingPlacement(s);

        var site = new ConstructionSite(s);
        var origPosn = site.getPosition();

        // Place it so it moves
        placement.positionSite(site);

        assertNotEquals(origPosn, site.getPosition());
    }
}
