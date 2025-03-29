package com.mars_sim.core.food;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
        assertTrue("Food processes defined", !manuProcesses.isEmpty());
    }

    @Test
    void testPackageFood() {
        // Build mapped key on process name
        var processByName =
                    Maps.uniqueIndex(foodConfig.getProcessList(),
                        FoodProductionProcessInfo::getName);
        var process = processByName.get(PACKAGE_FOOD);
        assertNotNull("Food processes defined", process);
        assertEquals("primary inputs", PACKAGE_INPUTS, process.getInputList().size());

        // Check the alternative are present and they have different inputs
        Set<List<ProcessItem>> alternatives = new HashSet<>();
        alternatives.add(process.getInputList());

        for(var altName : PACKAGE_ALTERNATIVES) {
            var found = processByName.get(PACKAGE_FOOD + FoodProductionConfig.RECIPE_PREFIX + altName);
            assertNotNull(PACKAGE_FOOD + " alternative " + altName, found);
            assertEquals(PACKAGE_FOOD + " alternative  inputs " + altName, PACKAGE_INPUTS, found.getInputList().size());
            alternatives.add(found.getInputList());
        }

        assertEquals("All alternatives have different inputs", PACKAGE_ALTERNATIVES.length + 1, alternatives.size());
    }

    @Test
    void testMakeSoybean() {
        // Build mapped key on process name
        var processByName =
                    Maps.uniqueIndex(foodConfig.getProcessList(),
                        FoodProductionProcessInfo::getName);
        var process = processByName.get("Process Soybean into Soy Flour");
        assertNotNull("Food processes defined", process);

        List<ProcessItem> expectedInputs = new ArrayList<>();
        expectedInputs.add(ManufactureConfigTest.createAmount("Soybean", 1D));
        expectedInputs.add(ManufactureConfigTest.createAmount("Water", 1D));
        expectedInputs.add(ManufactureConfigTest.createPart("oven", 1D));

        assertEquals("Antenna expected inputs", expectedInputs, process.getInputList());

        List<ProcessItem> expectedOutputs = new ArrayList<>();
        expectedOutputs.add(ManufactureConfigTest.createAmount("Soy Flour", 1D));
        expectedOutputs.add(ManufactureConfigTest.createPart("oven", 1D));

        assertEquals("Antenna expected outputs", expectedOutputs, process.getOutputList());

    }

}
