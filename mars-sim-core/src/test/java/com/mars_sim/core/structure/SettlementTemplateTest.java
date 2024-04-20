package com.mars_sim.core.structure;


import com.mars_sim.core.AbstractMarsSimUnitTest;

public class SettlementTemplateTest extends AbstractMarsSimUnitTest {

    // This test assumes certain characterits of the Alpha Base template
    private static final String ALPHA_BASE = "Alpha Base";
    private static final Object RESUPPLY_MISSION = "Bi-Monthly Delivery";
    private static final String MANIFEST = "Standard Resupply 1";


    public void testSettlementTemplates() {
        var config = simConfig.getSettlementConfiguration();

        assertTrue("Settlement templates defined", !config.getItemNames().isEmpty());
        assertNotNull("Settlement template " + ALPHA_BASE, config.getItem(ALPHA_BASE));
    }

    public void testResupply() {
        var config = simConfig.getSettlementConfiguration();

        var template =  config.getItem(ALPHA_BASE);

        var resupplies = template.getResupplyMissionTemplates();
        assertFalse("Settlement has resupplies", resupplies.isEmpty());

        var resupply = resupplies.stream().filter(r -> r.getName().equals(RESUPPLY_MISSION))
                                        .findFirst().get();
        assertEquals("Resupply Manifest", MANIFEST, resupply.getManifest().name());
        var schedule = resupply.getSchedule();
        assertEquals("Resupply time of day", 400, schedule.getTimeOfDay());
        assertEquals("Resupply time to first sol", 1, schedule.getFirstSol());
        assertEquals("Resupply frequency", 62, schedule.getFrequency());
    }
}
