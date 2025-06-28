package com.mars_sim.core.mission.objectives;



import static org.junit.Assert.assertEquals;

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
        assertEquals("Site 1", 20D, sites.get(1), 0D);
        assertEquals("Site 2", 30D, sites.get(2), 0D);

        var resources = obj.getResourcesCollected();
        assertEquals("Resoruces recorded", 3, resources.size());
        assertEquals("Resource 1", 20D, resources.get(1), 0D);
        assertEquals("Resource 2", 10D, resources.get(2), 0D);
        assertEquals("Resource 3", 20D, resources.get(3), 0D);

    }
}
