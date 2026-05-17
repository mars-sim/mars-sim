package com.mars_sim.core.structure;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.ResourceUtil;

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

        assertFalse(supplies.getParts().isEmpty(), "Parts is empty");
        int num = supplies.getParts().get(
                            ItemResourceUtil.findItemResource("biosensor"));
        assertEquals(52, num, "Biosensor"); // biosensors are now in certain packages. Total of 52 for Hub Base
        assertTrue(supplies.getParts().get(
                            ItemResourceUtil.findItemResource(ItemResourceUtil.SLS_3D_PRINTER_ID)) > 0, "Has printers");

        assertFalse(supplies.getBuildings().isEmpty(), "Buildings is empty");

        Map<String, Double> expectedResources = Map.of(
            "Food", 4320.0,
            "Water", 7200.0,
            "Oxygen", 5760.0
        ); 
        for (var entry : expectedResources.entrySet()) {
            var resource = ResourceUtil.findAmountResource(entry.getKey());
            assertNotNull(resource, "Resource found: " + entry.getKey());
            assertEquals(entry.getValue(), supplies.getResources().get(resource), "Resource amount for " + entry.getKey());
        }

        Map<String, Integer> expectedVehicles = Map.of(
            "Explorer Rover", 2,
            "Long Range Explorer", 2,
            "Transport Rover", 3,
            "Cargo Rover", 3,
            "Light Utility Vehicle", 4,
            "Delivery Drone", 3,
            "Cargo Drone", 2
        );
        assertEquals(expectedVehicles, supplies.getVehicles(), "Vehicles");

        Map<String, Integer> expectedBins = Map.of(
            "crate", 40,
            "basket", 40,
            "pot", 40
        );
        assertEquals(expectedBins, supplies.getBins(), "Bins");

        Map<String, Integer> expectedEquipment = Map.of(
            "barrel", 240,
            "bag", 240,
            "gas canister", 240,
            "large bag", 240,
            "eva suit", 60,
            "specimen box", 240,
            "thermal bottle", 64,
            "wheelbarrow", 48
        );
        assertEquals(expectedEquipment, supplies.getEquipment(), "Equipment");

        Map<String,Integer> expectedRobots = Map.of(
            "ChefBot-Standard", 2,
            "DeliveryBot-Standard", 2,
            "GardenBot-Advanced", 3,
            "MakerBot-Standard", 4,
            "MedicBot-Advanced", 2,
            "RepairBot-Standard", 5
        );
        assertEquals(expectedRobots, supplies.getRobots(), "Robots");

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

        // Convert to list for easier testing
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
