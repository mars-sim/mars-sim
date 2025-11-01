package com.mars_sim.core.structure;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.resource.ItemResourceUtil;

class SettlementTemplateConfigTest {
    private SimulationConfig config;

    @BeforeEach
    void setUp() {
        config = SimulationConfig.loadConfig();
    }

    @Test
    void testGetAll() {
        var st = config.getSettlementTemplateConfiguration();

        var known = st.getKnownItems();
        assertFalse(known.isEmpty(), "Settlement templates defined");

        var name = st.getItemNames();
        assertEquals(name.size(), known.size(), "Names and template counts");
    }

    @Test
    void testHubBase() {
        var st = config.getSettlementTemplateConfiguration();

        var hubBase = st.getItem("Hub Base");
        assertNotNull(hubBase, "Hub Base template found");
        assertEquals("Hub Base", hubBase.getName(), "Name of template");

        assertEquals("Standard 4 Shift", hubBase.getShiftDefinition().getName(), "Shift pattern");
    
        var supplies = hubBase.getSupplies();
        assertFalse(supplies.getEquipment().isEmpty(), "Equipment is empty");

        assertFalse(supplies.getParts().isEmpty(), "Parts is empty");
        int num = supplies.getParts().get(
                            ItemResourceUtil.findItemResource("biosensor"));
        assertEquals(52, num, "Biosensor"); // biosensors are now in certain packages. Total of 52 for Hub Base
        assertTrue(supplies.getParts().get(
                            ItemResourceUtil.findItemResource(ItemResourceUtil.SLS_3D_PRINTER_ID)) > 0, "Has printers");

        assertFalse(supplies.getBins().isEmpty(), "Bins is empty");
        assertFalse(supplies.getBuildings().isEmpty(), "Buildings is empty");
        assertFalse(supplies.getResources().isEmpty(), "Resources is empty");
        assertFalse(supplies.getVehicles().isEmpty(), "Vehciles is empty");

        assertFalse(hubBase.getPredefinedRobots().isEmpty(), "Robots is empty");

        assertEquals(1, hubBase.getResupplyMissionTemplates().size(), "Supply mission");

        assertEquals("MS", hubBase.getSponsor().getName(), "Sponsor");

    }
}
