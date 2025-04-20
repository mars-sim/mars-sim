package com.mars_sim.core.manufacture.task;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.manufacture.ManufactureProcess;
import com.mars_sim.core.manufacture.ManufacturingManagerTest;
import com.mars_sim.core.tool.RandomUtil;

public class ManufacturingMetaTaskTest extends AbstractMarsSimUnitTest {
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
        assertEquals("Demand for task", 2, tasks.get(0).getDemand());
        assertEquals("Target for task", b, tasks.get(0).getFocus());

    }

    public void testRunningManuProcessesMultiWorkshops() {
        var s = buildSettlement("Workshop", true);
        
        // Build 2 workshops
        ManufacturingManagerTest.buildWorkshop(this, s.getBuildingManager());
        var b = ManufacturingManagerTest.buildWorkshop(this, s.getBuildingManager());
        var m = b.getManufacture();
        var processes = getConfig().getManufactureConfiguration().getManufactureProcessesForTechLevel(
                                    m.getTechLevel());

        var mt = new ManufacturingMetaTask();

        // Add processes to the workshop
        var p = RandomUtil.getRandomElement(processes);
        (new ManufactureProcess(p, m)).startProcess();
        (new ManufactureProcess(p, m)).startProcess();

        var tasks = mt.getSettlementTasks(s);
        assertEquals("Tasks for running Processes", 1, tasks.size());
        assertEquals("Demand for task", 2, tasks.get(0).getDemand());
        assertEquals("Target for task", b, tasks.get(0).getFocus());

    }
}
