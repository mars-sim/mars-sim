package com.mars_sim.core.manufacture.task;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.equipment.EquipmentFactory;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.manufacture.ManufactureProcessInfo;
import com.mars_sim.core.manufacture.ManufacturingManagerTest;
import com.mars_sim.core.manufacture.SalvageProcessInfo;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.process.ProcessInfoTest;

public class ManufactureWorkTaskTest extends MarsSimUnitTest {


    @Test
    public void testBuildNitrates() {
        ManufactureProcessInfo processInfo = getConfig().getManufactureConfiguration().getManufactureProcessList().stream()
                            .filter(p -> p.getName().equals("Make Nitrites"))
                            .findAny().get();
        assertTrue(processInfo.getProcessTimeRequired() > 0, "Process has processing time");
        
        var s = buildSettlement("Test", true);
        var b = ManufacturingManagerTest.buildWorkshop(getContext(), s.getBuildingManager());
        var w = b.getManufacture();
        
        // Load resources into settlement and add to queue
        ProcessInfoTest.loadSettlement(s, processInfo);
        s.getManuManager().addProcessToQueue(processInfo);

        // Create engineer with skill
        var p = buildPerson("Engineer", s, JobType.ENGINEER, b, FunctionType.MANUFACTURE);
        p.getSkillManager().addNewSkill(SkillType.MATERIALS_SCIENCE, processInfo.getSkillLevelRequired());

        var task = new ManufactureWorkTask(p, b);
        assertFalse(task.isDone(), "Manufacture task created");

        // Walk to activity spot
        executeTaskUntilSubTask(p, task, 10);
        assertFalse(task.isDone(), "Walking completed");

        // RUn for a single tick to trigger creation of process
        executeTask(p, task, 1);

        var ps = w.getProcesses();
        assertFalse(ps.isEmpty(), "Active processes");
        var process = ps.get(0);
        assertEquals(processInfo, process.getInfo(), "Process name");

        executeTaskForDuration(p, task, processInfo.getWorkTimeRequired() * 1.1);

        assertTrue(task.isDone(), "Manufacture task completed");       
        assertTrue(process.isActive(), "Process waiting for processing");       

        assertEquals(0D, process.getWorkTimeRemaining(), "All work time completed");
    }

    @Test
    public void testSalvageCanister() {
        SalvageProcessInfo processInfo = getConfig().getManufactureConfiguration().getSalvageInfoList().stream()
                            .filter(p -> p.getName().equals(SalvageProcessInfo.NAME_PREFIX + "gas canister"))
                            .findAny().get();
        assertEquals(0D, processInfo.getProcessTimeRequired(), "Process has no process time");
        
        var s = buildSettlement("Test", true);
        var b = ManufacturingManagerTest.buildWorkshop(getContext(), s.getBuildingManager());
        var w = b.getManufacture();
        
        var canister = EquipmentFactory.createEquipment(EquipmentType.GAS_CANISTER, s);

        // Load resources into settlement and add to queue
        ProcessInfoTest.loadSettlement(s, processInfo);
        s.getManuManager().addSalvageToQueue(processInfo, canister);

        // Create engineer with skill
        var p = buildPerson("Engineer", s, JobType.ENGINEER, b, FunctionType.MANUFACTURE);
        p.getSkillManager().addNewSkill(SkillType.MATERIALS_SCIENCE, processInfo.getSkillLevelRequired());

        var task = new ManufactureWorkTask(p, b);
        assertFalse(task.isDone(), "Manufacture task created");

        // Walk to activity spot
        executeTaskUntilSubTask(p, task, 10);
        assertFalse(task.isDone(), "Walking completed");

        // RUn for a single tick to trigger creation of process
        executeTask(p, task, 1);
        var process = w.getProcesses().get(0);
        assertEquals(processInfo, process.getInfo(), "Process name");

        executeTask(p, task, 750);

        assertFalse(process.isActive(), "Process completed");  // Salvage has no processimng
        assertTrue(task.isDone(), "Manufacture task completed");       

        assertEquals(0D, process.getWorkTimeRemaining(), "All work time completed");
        assertTrue(w.getProcesses().isEmpty(), "No active processes");

    }

    @Test
    public void testSalvageCanisterAutoSelect() {
        SalvageProcessInfo processInfo = getConfig().getManufactureConfiguration().getSalvageInfoList().stream()
                            .filter(p -> p.getName().equals(SalvageProcessInfo.NAME_PREFIX + "gas canister"))
                            .findAny().get();
        assertEquals(0D, processInfo.getProcessTimeRequired(), "Process has no process time");
        
        var s = buildSettlement("Test", true);
        var b = ManufacturingManagerTest.buildWorkshop(getContext(), s.getBuildingManager());
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
        assertFalse(task.isDone(), "Manufacture task created");

        // Walk to activity spot
        executeTaskUntilSubTask(p, task, 10);
        assertFalse(task.isDone(), "Walking completed");

        // RUn for a single tick to trigger creation of process
        executeTask(p, task, 1);
        var process = w.getProcesses().get(0);
        assertEquals(processInfo, process.getInfo(), "Process name");

        executeTask(p, task, 750);

        assertTrue(task.isDone(), "Manufacture task completed");       
        assertFalse(process.isActive(), "Process completed");  // Salvage has no processimng

        assertEquals(0D, process.getWorkTimeRemaining(), "All work time completed");
        assertTrue(w.getProcesses().isEmpty(), "No active processes");

    }
}
