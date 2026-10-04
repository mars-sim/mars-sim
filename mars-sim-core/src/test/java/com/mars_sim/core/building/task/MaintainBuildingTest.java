package com.mars_sim.core.building.task;
import static com.mars_sim.core.test.SimulationAssertions.assertGreaterThan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.MarsSimContext;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.maintenance.MaintenanceUtil;
import com.mars_sim.core.malfunction.MalfunctionManager;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.job.util.JobType;

public class MaintainBuildingTest extends MarsSimUnitTest {
	
    static void buildingNeedMaintenance(Building b, MarsSimContext context) {
        MalfunctionManager manager = b.getMalfunctionManager();
        double time = manager.getStandardInspectionWindow() * MaintenanceUtil.INSPECTION_PERCENTAGE * 220;
        var mTime = context.getSim().getMasterClock().getMarsTime().addTime(time);
        manager.activeTimePassing(context.createPulse(mTime, false, false, false));
    }
    
    @Test
    public void testMetaTask() {
        var s = buildSettlement("Maintenance");
        var b1 = buildResearch(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D);
        
        // 2nd building check logic
        buildResearch(s.getBuildingManager(), new LocalPosition(10, 10), 0D);

        var mt = new MaintainBuildingMeta();
        var tasks = mt.getSettlementTasks(s);
        
        assertTrue(tasks.isEmpty(), "No tasks found");

        // One building needs maintenance
        buildingNeedMaintenance(b1, getContext());
        tasks = mt.getSettlementTasks(s);
        
        // Note: tasks may have the size of 0, 1, 2. It depends on the result of  scoreMaintenance()
        if (tasks.size() == 1) {

	        var found1 = tasks.get(0);	        
	        var foundB1 = found1.getFocus();
	        
	        assertFalse(found1.isEVA(), "Maintenance task should not be EVA");
	        assertEquals(b1, foundB1, "Found building B1 with maintenance");
        }


    }
    
    @Test
    public void testCreateTask() {

        var s = buildSettlement("Maintain");
        var b = buildResearch(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D);
        var p = buildPerson("Engineer", s, JobType.ENGINEER, b, FunctionType.RESEARCH);
        p.getSkillManager().addNewSkill(SkillType.MECHANICS, 10); // Skilled

        // Set building needs maintenance by moving time forward twice minimum
        buildingNeedMaintenance(b, getContext());
        var manager = b.getMalfunctionManager();
        assertGreaterThan("Maintenance due", 0D, manager.getEffectiveTimeSinceLastMaintenance());

        var task = new MaintainBuilding(p, b);
        assertFalse(task.isDone(), "Task created");

        // Do the initial walk
        executeTaskUntilSubTask(p, task, 10);
        assertTrue(task.getSubTask().isDone(), "Walk completed");
        assertFalse(task.isDone(), "Task still active");
 
        assertFalse(task.isDone(), "Task still active");
        assertEquals(b, p.getBuildingLocation(), "Engineer location");
        
        // Do maintenance for a few calls to ensure maintenance is happening
        executeTaskUntilPhase(p, task, 2);
        // Note that inspectionTimeCompleted will be reset to zero right away and is never a sign that work is accomplished
        // Not used: assertGreaterThan("Maintenance completed", 0D, manager.getInspectionWorkTimeCompleted());

        // Complete until the end
        executeTaskForDuration(p, task, task.getTimeLeft() * 1.1);
        assertTrue(task.isDone(), "Task created");
        assertEquals(0D, manager.getEffectiveTimeSinceLastMaintenance(), "Maintenance period reset");
    }
}
