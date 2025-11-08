package com.mars_sim.core.process;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.resource.ItemType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.RandomUtil;

public class ProcessInfoTest extends MarsSimUnitTest {
    @Test
    public void testIsResourcesAvailable() {
        var s = buildSettlement("mock");

        var processes = getConfig().getManufactureConfiguration().getManufactureProcessesForTechLevel(1);
        var selected = RandomUtil.getRandomElement(processes);

        assertFalse(selected.isResourcesAvailable(s), "No resources available");

        // Added resources
        loadSettlement(s, selected);
        assertTrue(selected.isResourcesAvailable(s), "Resources available");
    }

    @Test
    public void testIsInput() {
        var p = getConfig().getManufactureConfiguration().getManufactureProcessesForTechLevel(1).get(0);

        for(var o : p.getInputList()) {
            var output = o.getName();
            assertTrue(p.isInput(output), "Process contains " + output);
        }

        assertFalse(p.isInput("Not exists"), "Process does not input");
    }

    @Test
    public void testIsOutput() {
        var p = getConfig().getManufactureConfiguration().getManufactureProcessesForTechLevel(1).get(0);

        for(var o : p.getOutputList()) {
            var output = o.getName();
            assertTrue(p.isOutput(output), "Process contains " + output);
        }

        assertFalse(p.isOutput("Not exists"), "Process does not output");
    }

    /**
     * Load a Settlement with the resources needed for a process
     */
    public static void loadSettlement(Settlement s, ProcessInfo process) {
		for(ProcessItem item : process.getInputList()) {
			if (ItemType.AMOUNT_RESOURCE == item.getType()) {
                s.storeAmountResource(item.getId(), item.getAmount() * 1.1D);
            }
			else if (ItemType.PART == item.getType()) {
                s.storeItemResource(item.getId(), (int)(item.getAmount() * 1.1D));
            }
        }
    }
}
