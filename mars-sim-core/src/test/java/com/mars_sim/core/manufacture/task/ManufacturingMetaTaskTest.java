package com.mars_sim.core.manufacture.task;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.manufacture.ManufactureConfig;
import com.mars_sim.core.manufacture.ManufactureProcess;
import com.mars_sim.core.manufacture.ManufacturingManagerTest;
import com.mars_sim.core.tool.RandomUtil;

public class ManufacturingMetaTaskTest extends MarsSimUnitTest {
	
    private double factor = ManufacturingMetaTask.DEMAND_FACTOR;
    
    @Test
    public void testNewManuProcesses() {
        var s = buildSettlement("Workshop", true);
        
        var mgr = s.getManuManager();
        var b = ManufacturingManagerTest.buildWorkshop(this, s.getBuildingManager());
        var m = b.getManufacture();
        var processes = getConfig().getManufactureConfiguration().getManufactureProcessesForTechLevel(
                                    m.getTechLevel());

        var mt = new ManufacturingMetaTask();

        var tasks = mt.getSettlementTasks(s);
        assertTrue(tasks.isEmpty(), "No queue, no Task");

        // Add processes to the queue
        mgr.addProcessToQueue(RandomUtil.getRandomElement(processes));

        tasks = mt.getSettlementTasks(s);
        assertEquals(1, tasks.size(), "Tasks to start new Processes");
        assertEquals(1, tasks.get(0).getDemand(), "Demand for task");
        assertEquals(b, tasks.get(0).getFocus(), "Demand for workshop");

        // Add another process to queue
        mgr.addProcessToQueue(RandomUtil.getRandomElement(processes));
        tasks = mt.getSettlementTasks(s);
        assertEquals(1, tasks.size(), "Tasks to start new 2 Processes");
        assertEquals(2, tasks.get(0).getDemand(), "Demand for 2 task");
        assertEquals(b, tasks.get(0).getFocus(), "Demand 2 for workshop");

        // Overload queue
        mgr.addProcessToQueue(RandomUtil.getRandomElement(processes));
        assertEquals(1, tasks.size(), "Tasks to start overloaded Processes");
        assertEquals(2, tasks.get(0).getDemand(), "Demand for overload task");
        assertEquals(b, tasks.get(0).getFocus(), "Demand overload for workshop");
    }  

    @Test
    public void testNewManuProcessesMultiWorkshops() {
        var s = buildSettlement("Workshop", true);
        
        // Build 2 workshops
        ManufacturingManagerTest.buildWorkshop(this, s.getBuildingManager());
        var b = ManufacturingManagerTest.buildWorkshop(this, s.getBuildingManager());
        var m = b.getManufacture();
        var processes = getConfig().getManufactureConfiguration().getManufactureProcessesForTechLevel(
                                    m.getTechLevel());

        var mt = new ManufacturingMetaTask();

        // Add processes to the queue
        var mgr = s.getManuManager();
        mgr.addProcessToQueue(RandomUtil.getRandomElement(processes));
        mgr.addProcessToQueue(RandomUtil.getRandomElement(processes));

        var tasks = mt.getSettlementTasks(s);
        assertEquals(2, tasks.size(), "Tasks to start new Processes");
        assertEquals(2, tasks.get(0).getDemand(), "Demand for 1st task");
        assertEquals(2, tasks.get(1).getDemand(), "Demand for 2nd task");
    }

    @Test
    public void testRunningNewManuProcesses() {
        var s = buildSettlement("Workshop", true);
        
        // Build workshops
        var b = ManufacturingManagerTest.buildWorkshop(this, s.getBuildingManager());
        var m = b.getManufacture();
        var processes = getConfig().getManufactureConfiguration().getManufactureProcessesForTechLevel(
                                    m.getTechLevel());

        var mt = new ManufacturingMetaTask();

        // Add to queue
        var mgr = s.getManuManager();
        mgr.addProcessToQueue(RandomUtil.getRandomElement(processes));

        // Add processes to the workshop
        var p = RandomUtil.getRandomElement(processes);
        (new ManufactureProcess(p, m)).startProcess();

        var tasks = mt.getSettlementTasks(s);
        
        assertEquals(1, tasks.size(), "Tasks for running Processes");
        assertEquals(factor + 1, 1.0 * tasks.get(0).getDemand(), "Demand for task");
        assertEquals(b, tasks.get(0).getFocus(), "Target for task");

    }

    @Test
    public void testRunningManuProcessesMultiWorkshops() {
        var s = buildSettlement("Workshop", true);
        
        // Build 2 workshops
        ManufacturingManagerTest.buildWorkshop(this, s.getBuildingManager());
        var b = ManufacturingManagerTest.buildWorkshop(this, s.getBuildingManager());
        var m = b.getManufacture();

        // Check how many printers are available
        var manuConfig = getConfig().getManufactureConfiguration();
        var printers = manuConfig.getTooling(ManufactureConfig.PRINTER);
        var capacity = m.getToolDetails().get(printers).getCapacity();
        assertTrue(capacity > 1, "Multiple 3D printers");

        var processes = getConfig().getManufactureConfiguration().getManufactureProcessesForTechLevel(
                                    m.getTechLevel());
        var p = processes.stream().filter(x -> x.getTooling().equals(printers)).findFirst().orElse(null);
        assertNotNull(p, "Failed to find 3D printer process");

        var mt = new ManufacturingMetaTask();

        // Add processes to the workshop
        for(int i = 0; i < capacity; i++) {
            assertTrue((new ManufactureProcess(p, m)).startProcess(), "Start process #" + i);
        }

        // Fail due to only 2 3D printers
        assertFalse((new ManufactureProcess(p, m)).startProcess(), "Start process without printers");

        var tasks = mt.getSettlementTasks(s);
  
        assertEquals(1, tasks.size(), "Tasks for running Processes");
        assertEquals(capacity * factor, 1.0 * tasks.get(0).getDemand(), "Demand for task");
        assertEquals(b, tasks.get(0).getFocus(), "Target for task");

    }
}
