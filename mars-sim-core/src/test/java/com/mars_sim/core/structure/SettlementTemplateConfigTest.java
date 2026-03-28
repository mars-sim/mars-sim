package com.mars_sim.core.structure;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Comparator;

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

    @Test
    void testAlphaBase1BuildingPlans() {
        var st = config.getSettlementTemplateConfiguration();

        var alphaBase1 = st.getItem("Alpha Base 1");
        assertNotNull(alphaBase1, "Alpha Base 1 template found");
        assertEquals("Alpha Base 1", alphaBase1.getName(), "Name of template");

        // Test building plans
        var buildingPlans = alphaBase1.getBuildingPlans();
        assertNotNull(buildingPlans, "Building plans not null");
        assertEquals(2, buildingPlans.size(), "Expected 2 building plans");

        // Convert to list for easier testing (TreeSet maintains sorted order)
        var plansList = new ArrayList<>(buildingPlans);
        plansList.sort(Comparator.comparingInt(BuildingPlan::delayInSols));
        
        // First plan should be Inflatable Greenhouse (delay 30)
        assertEquals("Inflatable Greenhouse", plansList.get(0).buildingType(), "First plan building type");
        assertEquals(30, plansList.get(0).delayInSols(), "First plan delay");

        // Second plan should be Manufacturing Workshop (delay 60) 
        assertEquals("Manufacturing Workshop", plansList.get(1).buildingType(), "Second plan building type");
        assertEquals(60, plansList.get(1).delayInSols(), "Second plan delay");
    }
}
