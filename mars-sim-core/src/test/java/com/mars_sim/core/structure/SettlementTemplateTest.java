package com.mars_sim.core.structure;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mars_sim.core.SimulationConfig;

class SettlementTemplateTest {
	
    private static final String ALPHA_BASE_1 = "Alpha Base 1";
    private static final Object RESUPPLY_MISSION = "Bi-Monthly Delivery";
    private static final String MANIFEST = "Standard Resupply 1";
    private SettlementTemplateConfig config;

    @BeforeEach
    void setUp() {
        config = SimulationConfig.loadConfig().getSettlementTemplateConfiguration();
    }

    @Test
    void testSettlementTemplates() {

        assertTrue(!config.getItemNames().isEmpty(), "Settlement templates defined");
        assertNotNull(config.getItem(ALPHA_BASE_1), "Settlement template " + ALPHA_BASE_1);
    }

    @Test
    void testResupply() {
        var template =  config.getItem(ALPHA_BASE_1);

        var resupplies = template.getResupplyMissionTemplates();
        assertFalse(resupplies.isEmpty(), "Settlement has resupplies");

        var resupply = resupplies.stream().filter(r -> r.getName().equals(RESUPPLY_MISSION))
                                        .findFirst().get();
        assertEquals(MANIFEST, resupply.getManifest().getName(), "Resupply Manifest");
        var schedule = resupply.getSchedule();
        assertEquals(400, schedule.getTimeOfDay(), "Resupply time of day");
        assertEquals(1, schedule.getFirstSol(), "Resupply time to first sol");
        assertEquals(62, schedule.getFrequency(), "Resupply frequency");
    }
}
