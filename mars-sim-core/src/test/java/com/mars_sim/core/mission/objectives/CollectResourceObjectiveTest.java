package com.mars_sim.core.mission.objectives;


import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CollectResourceObjectiveTest {
    @Test
    void testGetCollected() {
        var obj = new CollectResourceObjective(10D);

        obj.recordResourceCollected(1, 1, 10.0);
        obj.recordResourceCollected(1, 2, 10.0);
        obj.recordResourceCollected(2, 1, 10.0);
        obj.recordResourceCollected(2, 3, 20.0);

        var sites = obj.getCollectedAtSites();
        assertEquals(20D, sites.get(1), 0D);
        assertEquals(30D, sites.get(2), 0D);

        var resources = obj.getResourcesCollected();
        assertEquals(3, resources.size());
        assertEquals(20D, resources.get(1), 0D);
        assertEquals(10D, resources.get(2), 0D);
        assertEquals(20D, resources.get(3), 0D);

    }
}
