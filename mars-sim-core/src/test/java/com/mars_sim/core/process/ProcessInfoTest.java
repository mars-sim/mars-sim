package com.mars_sim.core.process;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.resource.ItemType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.RandomUtil;

public class ProcessInfoTest extends AbstractMarsSimUnitTest {
    public void testIsResourcesAvailable() {
        var s = buildSettlement();

        var processes = getConfig().getManufactureConfiguration().getManufactureProcessesForTechLevel(1);
        var selected = RandomUtil.getRandomElement(processes);

        assertFalse("No resources available", selected.isResourcesAvailable(s));

        // Added resources
        loadSettlement(s, selected);
        assertTrue("Resources available", selected.isResourcesAvailable(s));
    }

    public void testIsInput() {
        var p = getConfig().getManufactureConfiguration().getManufactureProcessesForTechLevel(1).get(0);

        for(var o : p.getInputList()) {
            var output = o.getName();
            assertTrue("Process contains " + output, p.isInput(output));
        }

        assertFalse("Process does not input", p.isInput("Not exists"));
    }

    public void testIsOutput() {
        var p = getConfig().getManufactureConfiguration().getManufactureProcessesForTechLevel(1).get(0);

        for(var o : p.getOutputList()) {
            var output = o.getName();
            assertTrue("Process contains " + output, p.isOutput(output));
        }

        assertFalse("Process does not output", p.isOutput("Not exists"));
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
