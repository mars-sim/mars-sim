package com.mars_sim.core.structure;


import com.mars_sim.core.AbstractMarsSimUnitTest;

public class SettlementTemplateTest extends AbstractMarsSimUnitTest {

    // This test assumes certain characteristics of the Alpha Base template
    private static final String ALPHA_BASE_1 = "Alpha Base 1";
    private static final Object RESUPPLY_MISSION = "Bi-Monthly Delivery";
    private static final String MANIFEST = "Standard Resupply 1";


    public void testSettlementTemplates() {
        var config = simConfig.getSettlementTemplateConfiguration();
//        var config = simConfig.getSettlementConfiguration();

//        assertTrue("Settlement templates defined", !config.getItemNames().isEmpty());
//        assertNotNull("Settlement template " + ALPHA_BASE, config.getItem(ALPHA_BASE));
    }

    public void testResupply() {
        var config = simConfig.getSettlementConfiguration();

        var template =  simConfig.getSettlementTemplateConfiguration().getItem(ALPHA_BASE_1);

        var resupplies = template.getResupplyMissionTemplates();
        assertFalse("Settlement has resupplies", resupplies.isEmpty());

        var resupply = resupplies.stream().filter(r -> r.getName().equals(RESUPPLY_MISSION))
                                        .findFirst().get();
        assertEquals("Resupply Manifest", MANIFEST, resupply.getManifest().getName());
        var schedule = resupply.getSchedule();
        assertEquals("Resupply time of day", 400, schedule.getTimeOfDay());
        assertEquals("Resupply time to first sol", 1, schedule.getFirstSol());
        assertEquals("Resupply frequency", 62, schedule.getFrequency());
    }
}
