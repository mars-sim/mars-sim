/*
 * Mars Simulation Project
 * ManufactureConfigTest.java
 * @date 2024-02-28
 * @author Barry Evans
 */

package com.mars_sim.core.manufacture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.google.common.collect.Maps;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.process.ProcessItem;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.ItemType;
import com.mars_sim.core.resource.ResourceUtil;

public class ManufactureConfigTest {

    private static final String MAKE_FERTILIZERS = "Make crop fertilizers";
    private static final String MAKE_RADIO_ANTENNA ="Make radio antenna";
    private static final int FERTILIZER_ALT = 9;
    private static final int FERTILIZER_INPUTS = 10;


    private ManufactureConfig getManufactureConfig() {
        var config = SimulationConfig.instance();
        config.reloadConfig();
        return config.getManufactureConfiguration();
    }

    @Test
    void testProcessesLoaded() {
        var manuProcesses = getManufactureConfig().getManufactureProcessList();
        assertTrue("Manufacturing processes defined", !manuProcesses.isEmpty());

    }

    @Test
    void testPlasticBottle() {
        // Build mapped key on process name
        var processByName =
                    Maps.uniqueIndex(getManufactureConfig().getManufactureProcessList(),
                        ManufactureProcessInfo::getName);
        var fertilizerP = processByName.get(MAKE_FERTILIZERS);
        assertNotNull("Manufacturng processes defined", fertilizerP);
        assertEquals(MAKE_FERTILIZERS + " primary inputs", FERTILIZER_INPUTS, fertilizerP.getInputList().size());

        // Check the alternative are present and they have different inputs
        Set<List<ProcessItem>> alternatives = new HashSet<>();
        alternatives.add(fertilizerP.getInputList());

        for(int i = 1; i <= FERTILIZER_ALT; i++) {
            var found = processByName.get(MAKE_FERTILIZERS + ManufactureConfig.ALT_PREFIX + i);
            assertNotNull(MAKE_FERTILIZERS + " alternative " + i, found);
            assertEquals(MAKE_FERTILIZERS + " alternative " + i + "inputs", FERTILIZER_INPUTS, found.getInputList().size());
            alternatives.add(found.getInputList());
        }

        assertEquals("All alternatives have different inputs", FERTILIZER_ALT + 1, alternatives.size());
    }

    @Test
    void testMakeRadioAntenna() {
        // Build mapped key on process name
        var processByName =
                    Maps.uniqueIndex(getManufactureConfig().getManufactureProcessList(),
                        ManufactureProcessInfo::getName);
        var process = processByName.get(MAKE_RADIO_ANTENNA);
        assertNotNull("Manufacturng processes defined", process);

        List<ProcessItem> expectedInputs = new ArrayList<>();
        expectedInputs.add(createAmount("Polyester Resin", 0.5D));
        expectedInputs.add(createAmount("styrene", 0.5D));
        expectedInputs.add(createPart("fiberglass", 1D));
        expectedInputs.add(createPart("aluminum sheet", 1D));
        expectedInputs.add(createPart("electrical wire", 1D));
        expectedInputs.add(createPart("wire connector", 3D));
        expectedInputs.add(createPart("optical cable", 1D));
        assertEquals("Antenna expected inputs", expectedInputs, process.getInputList());

        List<ProcessItem> expectedOutputs = new ArrayList<>();
        expectedOutputs.add(createPart("radio antenna", 5D));
        assertEquals("Antenna expected outputs", expectedOutputs, process.getOutputList());
    }

    
    public static ProcessItem createPart(String name, double amount) {
        int id = ItemResourceUtil.findIDbyItemResourceName(name);
        return new ProcessItem(id, name, ItemType.PART, amount);
    }

    public static ProcessItem createAmount(String name, double amount) {
        int id = ResourceUtil.findIDbyAmountResourceName(name);
        return new ProcessItem(id, name, ItemType.AMOUNT_RESOURCE, amount);
    }
}
