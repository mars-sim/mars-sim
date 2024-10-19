package com.mars_sim.core.mineral;

import com.mars_sim.core.AbstractMarsSimUnitTest;

public class MineralMapConfigTest extends AbstractMarsSimUnitTest {

    public void testGetMineralTypes() {
        var config = simConfig.getMineralMapConfiguration();
        var minerals = config.getMineralTypes();

        assertTrue("Minerals loaded", !minerals.isEmpty());

        // Check one
        MineralType found = minerals.stream()
                .filter(m -> m.getName().equals("Chalcopyrite"))
                .findFirst().orElse(null);
        assertNotNull("Found match", found);
        assertEquals("Frequency", MineralMapConfig.UNCOMMON_FREQUENCY, found.getFrequency());
        assertTrue("Locale contains sedimentary", found.getLocales().contains("sedimentary"));
        assertTrue("Locale contains volcanic", found.getLocales().contains("volcanic"));
    }
}
