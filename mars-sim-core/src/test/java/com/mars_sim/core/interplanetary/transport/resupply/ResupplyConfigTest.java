package com.mars_sim.core.interplanetary.transport.resupply;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.structure.SettlementTemplateConfigTest;

class ResupplyConfigTest {
    private ResupplyConfig config;

    @BeforeEach
    void setup() {
        var mainConfig = SimulationConfig.loadConfig();
        config = mainConfig.getSettlementTemplateConfiguration().getResupplyConfig();
    }

    @Test
    void testGetAll() {
        var manifests = config.getAll();
        // Just check we get something back and that it has the expected name
        assertTrue(!manifests.isEmpty(), "Expected to find some resupply manifests in the config");
    }

    @Test
    void testResupplyPhase3Manifest() {
        var expectedName = "Resupply for Phase 3";
        var manifest = config.getSupplyManifest(expectedName);
        assertNotNull(manifest, "Expected to find the standard resupply manifest");
        assertEquals(expectedName, manifest.getName(), "Expected manifest name to match");
        assertEquals(4, manifest.getPeople(), "Expected 4 people in manifest");

        var supply = manifest.getSupplies();

        var expectedVehicles = Map.of(
        		"Long Range Explorer", 2,
        		"Transport Rover", 2,
        		"Light Utility Vehicle", 1,
        		"Delivery Drone", 1);
        assertEquals(expectedVehicles, supply.getVehicles(), "Expected vehicles in manifest to match");

        
        var expectedRobots = Map.of(
        		"RepairBot-Standard", 1,
        		"GardenBot-Standard", 1,
        		"MakerBot-Standard", 1);
        assertEquals(expectedRobots, supply.getRobots(), "Expected robots in manifest to match");

        var expectedEqm = Map.of(
                		"eva suit", 24,
                        "barrel", 120,
                        "bag", 120,
                        "gas canister", 120,		
                        "large bag", 120,
                        "specimen box", 120,
                        "thermal bottle", 12,
                        "wheelbarrow", 8);
        assertEquals(expectedEqm, supply.getEquipment(), "Expected equipment in manifest to match");

        var expectedResources = Map.of(
        		"Hydrogen", 2000.0,
        		"Food", 4970.0);
        SettlementTemplateConfigTest.assertResources(expectedResources, supply.getResources());
        
        assertTrue(!supply.getParts().isEmpty(), "Expected to find some parts in the manifest");
    }

}
