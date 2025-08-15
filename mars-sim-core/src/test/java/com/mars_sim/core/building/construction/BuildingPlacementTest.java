package com.mars_sim.core.building.construction;


import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.map.location.LocalPosition;

public class BuildingPlacementTest extends AbstractMarsSimUnitTest {

    public void testPositionSite() {
        // Build one other building to force a placement activitiy
        var s = buildSettlement("position", true);
        buildAccommodation(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D, 0);

        var placement = new BuildingPlacement(s);

        // Place it so it moves
        var greenhouse = getConfig().getBuildingConfiguration().getBuildingSpec("Large Greenhouse");
        var posn = placement.positionSite(greenhouse);

        assertNotNull(posn);
    }
}
