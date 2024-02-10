package com.mars_sim.core.structure;

import com.mars_sim.core.AbstractMarsSimUnitTest;

public class SettlementConfigTest extends AbstractMarsSimUnitTest {

    private static final String ALPHA_BASE = "Alpha Base";

    public void testGetEssentialResources() {
        var config = simConfig.getSettlementConfiguration();

        assertTrue("Config has essential resources", !config.getEssentialResources().isEmpty());
    }

    public void testSettlementTemples() {
        var config = simConfig.getSettlementConfiguration();

        assertTrue("Settlement templates defined", !config.getItemNames().isEmpty());
        assertNotNull("Settlement template " + ALPHA_BASE, config.getItem(ALPHA_BASE));
    }

    public void testGetShiftPattern() {
        var config = simConfig.getSettlementConfiguration();

        // Check standard shifts
        testShiftSize(config, SettlementConfig.STANDARD_2_SHIFT, 2);
        testShiftSize(config, SettlementConfig.STANDARD_3_SHIFT, 3);
        testShiftSize(config, SettlementConfig.STANDARD_4_SHIFT, 4);   
    }

    private void testShiftSize(SettlementConfig config, String name, int shifts) {
        var shift = config.getShiftPattern(name);
        assertNotNull("Shift pattern " + name, shift);
        assertEquals("Shift size for " + name, shifts, shift.getShifts().size());
    }
}
