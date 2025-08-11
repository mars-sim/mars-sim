package com.mars_sim.core.manufacture.task;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.manufacture.ManufactureConfig;
import com.mars_sim.core.manufacture.ManufactureProcess;
import com.mars_sim.core.manufacture.ManufacturingManagerTest;
import com.mars_sim.core.tool.RandomUtil;

public class ManufacturingMetaTaskTest extends AbstractMarsSimUnitTest {
	
    double factor = ManufacturingMetaTask.DEMAND_FACTOR;
    
    
    public void testNewManuProcesses() {
        var s = buildSettlement("Workshop", true);
        
        var mgr = s.getManuManager();
        var b = ManufacturingManagerTest.buildWorkshop(this, s.getBuildingManager());
        var m = b.getManufacture();
        var processes = getConfig().getManufactureConfiguration().getManufactureProcessesForTechLevel(
                                    m.getTechLevel());

        var mt = new ManufacturingMetaTask();

        var tasks = mt.getSettlementTasks(s);
        assertTrue("No queue, no Task", tasks.isEmpty());

        // Add processes to the queue
        mgr.addProcessToQueue(RandomUtil.getRandomElement(processes));

        tasks = mt.getSettlementTasks(s);
        assertEquals("Tasks to start new Processes", 1, tasks.size());
        assertEquals("Demand for task", 1, tasks.get(0).getDemand());
        assertEquals("Demand for workshop", b, tasks.get(0).getFocus());

        // Add another process to queue
        mgr.addProcessToQueue(RandomUtil.getRandomElement(processes));
        tasks = mt.getSettlementTasks(s);
        assertEquals("Tasks to start new 2 Processes", 1, tasks.size());
        assertEquals("Demand for 2 task", 2, tasks.get(0).getDemand());
        assertEquals("Demand 2 for workshop", b, tasks.get(0).getFocus());

        // Overload queue
        mgr.addProcessToQueue(RandomUtil.getRandomElement(processes));
        assertEquals("Tasks to start overloaded Processes", 1, tasks.size());
        assertEquals("Demand for overload task", 2, tasks.get(0).getDemand());
        assertEquals("Demand overload for workshop", b, tasks.get(0).getFocus());
    }  

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
        assertEquals("Tasks to start new Processes", 2, tasks.size());
        assertEquals("Demand for 1st task", 2, tasks.get(0).getDemand());
        assertEquals("Demand for 2nd task", 2, tasks.get(1).getDemand());
    }

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
        
        assertEquals("Tasks for running Processes", 1, tasks.size());
        assertEquals("Demand for task", factor + 1, 1.0 * tasks.get(0).getDemand());
        assertEquals("Target for task", b, tasks.get(0).getFocus());

    }

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
        assertTrue("Multiple 3D printers", capacity > 1);

        var processes = getConfig().getManufactureConfiguration().getManufactureProcessesForTechLevel(
                                    m.getTechLevel());
        var p = processes.stream().filter(x -> x.getTooling().equals(printers)).findFirst().orElse(null);
        assertNotNull("Failed to find 3D printer process", p);

        var mt = new ManufacturingMetaTask();

        // Add processes to the workshop
        for(int i = 0; i < capacity; i++) {
            assertTrue("Start process #" + i, (new ManufactureProcess(p, m)).startProcess());
        }

        // Fail due to only 2 3D printers
        assertFalse("Start process without printers", (new ManufactureProcess(p, m)).startProcess());

        var tasks = mt.getSettlementTasks(s);
  
        assertEquals("Tasks for running Processes", 1, tasks.size());
        assertEquals("Demand for task", capacity * factor, 1.0 * tasks.get(0).getDemand());
        assertEquals("Target for task", b, tasks.get(0).getFocus());

    }
}
