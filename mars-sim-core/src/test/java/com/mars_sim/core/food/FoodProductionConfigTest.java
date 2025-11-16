package com.mars_sim.core.food;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Maps;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.manufacture.ManufactureConfigTest;
import com.mars_sim.core.process.ProcessItem;

class FoodProductionConfigTest {

    private static final String PACKAGE_FOOD = "Package Preserved Food";
    private static final int PACKAGE_INPUTS = 2;
    private static final String [] PACKAGE_ALTERNATIVES = {
                    "Carrot", "Leaves", "Swiss Chard", "Potato"
    };
    private FoodProductionConfig foodConfig;

    @BeforeEach
    void setUp() {
        var config = SimulationConfig.loadConfig();
        foodConfig = config.getFoodProductionConfiguration();
    }

    @Test
    void testProcessesLoaded() {
        var manuProcesses = foodConfig.getProcessList();
        assertTrue(!manuProcesses.isEmpty(), "Food processes defined");
    }

    @Test
    void testPackageFood() {
        // Build mapped key on process name
        var processByName =
                    Maps.uniqueIndex(foodConfig.getProcessList(),
                        FoodProductionProcessInfo::getName);
        var process = processByName.get(PACKAGE_FOOD);
        assertNotNull(process, "Food processes defined");
        assertEquals(PACKAGE_INPUTS, process.getInputList().size(), "primary inputs");

        // Check the alternative are present and they have different inputs
        Set<List<ProcessItem>> alternatives = new HashSet<>();
        alternatives.add(process.getInputList());

        for(var altName : PACKAGE_ALTERNATIVES) {
            var found = processByName.get(PACKAGE_FOOD + FoodProductionConfig.RECIPE_PREFIX + altName);
            assertNotNull(found, PACKAGE_FOOD + " alternative " + altName);
            assertEquals( PACKAGE_INPUTS, found.getInputList().size(), PACKAGE_FOOD + " alternative  inputs " + altName);
            alternatives.add(found.getInputList());
        }

        assertEquals(PACKAGE_ALTERNATIVES.length + 1, alternatives.size(), "All alternatives have different inputs");
    }

    @Test
    void testMakeSoybean() {
        // Build mapped key on process name
        var processByName =
                    Maps.uniqueIndex(foodConfig.getProcessList(),
                        FoodProductionProcessInfo::getName);
        var process = processByName.get("Process Soybean into Soy Flour");
        assertNotNull(process, "Food processes defined");

        List<ProcessItem> expectedInputs = new ArrayList<>();
        expectedInputs.add(ManufactureConfigTest.createAmount("Soybean", 1D));
        expectedInputs.add(ManufactureConfigTest.createAmount("Water", 1D));
        expectedInputs.add(ManufactureConfigTest.createPart("oven", 1D));

        assertEquals(expectedInputs, process.getInputList(), "Antenna expected inputs");

        List<ProcessItem> expectedOutputs = new ArrayList<>();
        expectedOutputs.add(ManufactureConfigTest.createAmount("Soy Flour", 1D));
        expectedOutputs.add(ManufactureConfigTest.createPart("oven", 1D));

        assertEquals(expectedOutputs, process.getOutputList(), "Antenna expected outputs");

    }

}
