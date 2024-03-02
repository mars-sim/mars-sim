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
import com.mars_sim.core.resource.ItemType;

class ManufactureConfigTest {

    private static final String MAKE_FERTILIZER = "Make fertilizers for growing crops";
    private static final int FERTILIZER_ALTNATIVES = 7;
    private static final int FERTILIZER_INPUTS = 9;


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
        var plasticBottle = processByName.get(MAKE_FERTILIZER);
        assertNotNull("Manufacturng processes defined", plasticBottle);
        assertEquals(MAKE_FERTILIZER + " primary inputs", FERTILIZER_INPUTS, plasticBottle.getInputList().size());

        // Check the alternative are present and they have different inputs
        Set<List<ProcessItem>> alternatives = new HashSet<>();
        alternatives.add(plasticBottle.getInputList());

        for(int i = 1; i <= FERTILIZER_ALTNATIVES; i++) {
            var found = processByName.get(MAKE_FERTILIZER + ManufactureConfig.ALT_PREFIX + i);
            assertNotNull(MAKE_FERTILIZER + " alternative " + i, found);
            assertEquals(MAKE_FERTILIZER + " alternative " + i + "inputs", FERTILIZER_INPUTS, found.getInputList().size());
            alternatives.add(found.getInputList());
        }

        assertEquals("All alternatives have different inputs", FERTILIZER_ALTNATIVES + 1, alternatives.size());
    }

    @Test
    void testMakeRadioAntenna() {
        // Build mapped key on process name
        var processByName =
                    Maps.uniqueIndex(getManufactureConfig().getManufactureProcessList(),
                        ManufactureProcessInfo::getName);
        var process = processByName.get("Make radio antenna");
        assertNotNull("Manufacturng processes defined", process);

        List<ProcessItem> expectedInputs = new ArrayList<>();
        expectedInputs.add(new ProcessItem("Polyester Resin", ItemType.AMOUNT_RESOURCE, 0.5D));
        expectedInputs.add(new ProcessItem("styrene", ItemType.AMOUNT_RESOURCE, 0.5D));
        expectedInputs.add(new ProcessItem("fiberglass", ItemType.PART, 1D));
        expectedInputs.add(new ProcessItem("aluminum sheet", ItemType.PART, 1D));
        expectedInputs.add(new ProcessItem("electrical wire", ItemType.PART, 1D));
        expectedInputs.add(new ProcessItem("wire connector", ItemType.PART, 3D));
        expectedInputs.add(new ProcessItem("optical cable", ItemType.PART, 1D));
        assertEquals("Antenna expected inputs", expectedInputs, process.getInputList());

        List<ProcessItem> expectedOutputs = new ArrayList<>();
        expectedOutputs.add(new ProcessItem("radio antenna", ItemType.PART, 5D));
        assertEquals("Antenna expected outputs", expectedOutputs, process.getOutputList());
    }
}
