package com.mars_sim.core.structure;


import com.mars_sim.core.AbstractMarsSimUnitTest;

public class SettlementConfigTest extends AbstractMarsSimUnitTest {

    private static final String ALPHA_BASE = "Alpha Base";
    private static final String STANDARD_4_SHIFT = "Standard 4 Shift";
	private static final String STANDARD_3_SHIFT = "Standard 3 Shift";
	private static final String STANDARD_2_SHIFT = "Standard 2 Shift";

    public void testGetEssentialResources() {
        var config = simConfig.getSettlementConfiguration();

        assertTrue("Config has essential resources", !config.getEssentialResources().isEmpty());
    }

    public void testSettlementTemples() {
        var config = simConfig.getSettlementConfiguration();

        assertTrue("Settlement templates defined", !config.getItemNames().isEmpty());
        assertNotNull("Settlement template " + ALPHA_BASE, config.getItem(ALPHA_BASE));
    }

    public void testDefaultShiftPattern() {
        var config = simConfig.getSettlementConfiguration();

        // Check standard shifts
        testShiftSize(config, STANDARD_2_SHIFT, 2);
        testShiftSize(config, STANDARD_3_SHIFT, 3);
        testShiftSize(config, STANDARD_4_SHIFT, 4);   
    }

    private void testShiftSize(SettlementConfig config, String name, int shifts) {
        var shift = config.getShiftByName(name);
        assertNotNull("Shift pattern " + name, shift);
        assertEquals("Shift size for " + name, shifts, shift.getShifts().size());
    }

    public void testShiftByPopulation() {
        var config = simConfig.getSettlementConfiguration();
    
        var large = config.getShiftByPopulation(30);
        assertTrue("Large shift has smaller min pop", 30 > large.getMinPopulation());
        assertEquals("Shift pattern for large", "Standard 3 Shift", large.getName());


        var small = config.getShiftByPopulation(8);
        assertTrue("Small shift has smaller min pop", 8 > small.getMinPopulation());
        assertEquals("Shift pattern for small", "Standard 2 Shift", small.getName());

    }

    public void testGetActivityByPopulation() {
        var config = simConfig.getSettlementConfiguration();
    
        var large = config.getActivityByPopulation(30);
        assertTrue("Large ruleset has smaller min pop", 30 > large.minPop());
        assertEquals("Activity Ruleset for large", "Large Settlement", large.name());


        var small = config.getActivityByPopulation(8);
        assertTrue("Large ruleset has medium min pop", 8 > small.minPop());
        assertEquals("Activity Ruleset for small", "Small Settlement", small.name());

        var verysmall = config.getActivityByPopulation(4);
        assertNull("No activities for vry small population", verysmall);
    }
}