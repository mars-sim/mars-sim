package com.mars_sim.core.building.construction;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;


import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.map.location.LocalPosition;

public class BuildingPlacementTest extends MarsSimUnitTest {

    @Test
    public void testPositionSite() {
        // Build one other building to force a placement activitiy
        var s = buildSettlement("position", true);
        buildAccommodation(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D);

        var placement = new BuildingPlacement(s);

        // Place it so it moves
        var greenhouse = getConfig().getBuildingConfiguration().getBuildingSpec("Large Greenhouse");
        var posn = placement.positionSite(greenhouse);

        assertNotNull(posn);
    }
}
