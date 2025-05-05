package com.mars_sim.core.manufacture.task;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.equipment.EquipmentFactory;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.manufacture.ManufactureProcessInfo;
import com.mars_sim.core.manufacture.ManufacturingManagerTest;
import com.mars_sim.core.manufacture.SalvageProcessInfo;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.process.ProcessInfoTest;

public class ManufactureWorkTaskTest extends AbstractMarsSimUnitTest {


    public void testBuildNitrates() {
        ManufactureProcessInfo processInfo = getConfig().getManufactureConfiguration().getManufactureProcessList().stream()
                            .filter(p -> p.getName().equals("Make Nitrites"))
                            .findAny().get();
        assertTrue("Process has processing time", processInfo.getProcessTimeRequired() > 0);
        
        var s = buildSettlement("Test", true);
        var b = ManufacturingManagerTest.buildWorkshop(this, s.getBuildingManager());
        var w = b.getManufacture();
        
        // Load resources into settlement and add to queue
        ProcessInfoTest.loadSettlement(s, processInfo);
        s.getManuManager().addProcessToQueue(processInfo);

        // Create engineer with skill
        var p = buildPerson("Engineer", s, JobType.ENGINEER, b, FunctionType.MANUFACTURE);
        p.getSkillManager().addNewSkill(SkillType.MATERIALS_SCIENCE, processInfo.getSkillLevelRequired());

        var task = new ManufactureWorkTask(p, b);
        assertFalse("Manufacture task created", task.isDone());

        // Walk to activity spot
        executeTaskUntilSubTask(p, task, 10);
        assertFalse("Walking completed", task.isDone());

        // RUn for a single tick to trigger creation of process
        executeTask(p, task, 1);

        var ps = w.getProcesses();
        assertFalse("Active processes", ps.isEmpty());
        var process = ps.get(0);
        assertEquals("Process name", processInfo, process.getInfo());

        executeTaskForDuration(p, task, processInfo.getWorkTimeRequired() * 1.1);

        assertTrue("Manufacture task completed", task.isDone());       
        assertTrue("Process waiting for processing", process.isActive());       

        assertEquals("All work time completed", 0D, process.getWorkTimeRemaining());
    }

    public void testSalvageCanister() {
        SalvageProcessInfo processInfo = getConfig().getManufactureConfiguration().getSalvageInfoList().stream()
                            .filter(p -> p.getName().equals(SalvageProcessInfo.NAME_PREFIX + "gas canister"))
                            .findAny().get();
        assertEquals("Process has no process time", 0D, processInfo.getProcessTimeRequired());
        
        var s = buildSettlement("Test", true);
        var b = ManufacturingManagerTest.buildWorkshop(this, s.getBuildingManager());
        var w = b.getManufacture();
        
        var canister = EquipmentFactory.createEquipment(EquipmentType.GAS_CANISTER, s);

        // Load resources into settlement and add to queue
        ProcessInfoTest.loadSettlement(s, processInfo);
        s.getManuManager().addSalvageToQueue(processInfo, canister);

        // Create engineer with skill
        var p = buildPerson("Engineer", s, JobType.ENGINEER, b, FunctionType.MANUFACTURE);
        p.getSkillManager().addNewSkill(SkillType.MATERIALS_SCIENCE, processInfo.getSkillLevelRequired());

        var task = new ManufactureWorkTask(p, b);
        assertFalse("Manufacture task created", task.isDone());

        // Walk to activity spot
        executeTaskUntilSubTask(p, task, 10);
        assertFalse("Walking completed", task.isDone());

        // RUn for a single tick to trigger creation of process
        executeTask(p, task, 1);
        var process = w.getProcesses().get(0);
        assertEquals("Process name", processInfo, process.getInfo());

        executeTask(p, task, 750);

        assertFalse("Process completed", process.isActive());  // Salvage has no processimng
        assertTrue("Manufacture task completed", task.isDone());       

        assertEquals("All work time completed", 0D, process.getWorkTimeRemaining());
        assertTrue("No active processes", w.getProcesses().isEmpty());

    }

    public void testSalvageCanisterAutoSelect() {
        SalvageProcessInfo processInfo = getConfig().getManufactureConfiguration().getSalvageInfoList().stream()
                            .filter(p -> p.getName().equals(SalvageProcessInfo.NAME_PREFIX + "gas canister"))
                            .findAny().get();
        assertEquals("Process has no process time", 0D, processInfo.getProcessTimeRequired());
        
        var s = buildSettlement("Test", true);
        var b = ManufacturingManagerTest.buildWorkshop(this, s.getBuildingManager());
        var w = b.getManufacture();
        
        // Create a canister to salvage
        EquipmentFactory.createEquipment(EquipmentType.GAS_CANISTER, s);

        // Load resources into settlement and add to queue
        ProcessInfoTest.loadSettlement(s, processInfo);
        s.getManuManager().addProcessToQueue(processInfo);

        // Create engineer with skill
        var p = buildPerson("Engineer", s, JobType.ENGINEER, b, FunctionType.MANUFACTURE);
        p.getSkillManager().addNewSkill(SkillType.MATERIALS_SCIENCE, processInfo.getSkillLevelRequired());

        var task = new ManufactureWorkTask(p, b);
        assertFalse("Manufacture task created", task.isDone());

        // Walk to activity spot
        executeTaskUntilSubTask(p, task, 10);
        assertFalse("Walking completed", task.isDone());

        // RUn for a single tick to trigger creation of process
        executeTask(p, task, 1);
        var process = w.getProcesses().get(0);
        assertEquals("Process name", processInfo, process.getInfo());

        executeTask(p, task, 750);

        assertTrue("Manufacture task completed", task.isDone());       
        assertFalse("Process completed", process.isActive());  // Salvage has no processimng

        assertEquals("All work time completed", 0D, process.getWorkTimeRemaining());
        assertTrue("No active processes", w.getProcesses().isEmpty());

    }
}
