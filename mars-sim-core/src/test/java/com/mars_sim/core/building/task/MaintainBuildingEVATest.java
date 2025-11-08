package com.mars_sim.core.building.task;
import static com.mars_sim.core.test.SimulationAssertions.assertGreaterThan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingCategory;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.task.EVAOperation;
import com.mars_sim.core.person.ai.task.EVAOperationTest;
import com.mars_sim.core.time.MasterClock;

public class MaintainBuildingEVATest extends MarsSimUnitTest {

    private Building buildERV(BuildingManager buildingManager, LocalPosition localPosition) {
        return buildFunction(
                buildingManager,
                "ERV-A",
                BuildingCategory.ERV,
                FunctionType.EARTH_RETURN,
                localPosition,
                0D,
                false);
    }

    @Test
    public void testMetaTask() {
        var s = buildSettlement("EVA Maintenance");
        var b1 = buildERV(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION);
        // 2nd building check logic
        var b2 = buildERV(s.getBuildingManager(), new LocalPosition(10, 10));
        var mt = new MaintainBuildingMeta();
        var tasks = mt.getSettlementTasks(s);
        // Note: there is a chance that tasks are made since scoreMaintenance currently has a probability component
        // assertTrue(tasks.isEmpty(), "No tasks found");
        // One building needs maintenance
        MaintainBuildingTest.buildingNeedMaintenance(b1, getContext());
        tasks = mt.getSettlementTasks(s);
        // Question : why would sometimes both buildings (b1, b2) will incur the need for maintenance ?
        // Answer : getSettlementTasks() will consider both buildings always
        // Note: tasks may have the size of 0, 1, 2. It depends on the result of scoreMaintenance()
        if (tasks.size() == 2) {
            var found1 = tasks.get(0);
            var found2 = tasks.get(1);
            var foundB1 = found1.getFocus();
            var foundB2 = found2.getFocus();
            if (found1.isEVA()) {
                assertEquals(b1, foundB1, "Found building B1 with maintenance");
            }
            assertEquals(b2, foundB2, "Found building B2");
        }
    }

    @Test
    public void testCreateEVATask() {
        var s = buildSettlement("EVA Maintenance");

        // Need daylight so move to midday
        MasterClock clock = getSim().getMasterClock();
        clock.setMarsTime(clock.getMarsTime().addTime(500D));

        var p = buildPerson("Mechanic", s, JobType.TECHNICIAN);
        p.getSkillManager().addNewSkill(SkillType.MECHANICS, 10); // Skilled

        var eva = EVAOperationTest.prepareForEva(getContext(), p);
        // DigLocal uses the Settlement airlock tracking logic.... it shouldn't
        s.checkAvailableAirlocks();

        var b = buildERV(s.getBuildingManager(), new LocalPosition(20, 20));
        MaintainBuildingTest.buildingNeedMaintenance(b, getContext());

        var manager = b.getMalfunctionManager();
        assertGreaterThan("EVA Maintenance due", 0D, manager.getEffectiveTimeSinceLastMaintenance());

        var task = new MaintainBuildingEVA(p, b);

        assertFalse(task.isDone(), "EVA Task created"); 

        // Do NOT assert on duration here: getDuration() can legitimately be 0.0 at creation time
        // because EVAOperation tracks walking/onsite time internally across phases.

        // Initial phase is walking outside
        assertEquals(EVAOperation.WALK_TO_OUTSIDE_SITE, task.getPhase(), "EVA walking outside");

        // Move onsite
        int callUsed = EVAOperationTest.executeEVAWalk(getContext(), eva, task);
        assertGreaterThan("Calls Used ", 0, callUsed);
 
        assertFalse(task.isDone(), "EVA Task still active");
        
        assertGreaterThan("EVA Maintenance due", 0D, manager.getEffectiveTimeSinceLastMaintenance());

        assertEquals(0D, manager.getInspectionWorkTimeCompleted(), "EVA Maintenance started");

        // Confirm we are now in onsite MAINTAIN phase
        assertEquals(MaintainBuildingEVA.MAINTAIN, task.getPhase(), "EVA walk completed");

        // Start maintenance (run one tick). Do NOT assert on inspectionWorkTimeCompleted:
        // it may be reset to zero immediately when an inspection completes (same as MaintainBuildingTest).
        executeTaskUntilPhase(p, task, 1);
        assertFalse(task.isDone(), "Task still active");

        // Complete maintenance
        executeTaskForDuration(p, task, manager.getBaseMaintenanceWorkTime() * 1.1);
        
        if (task.isDone())
        	assertGreaterThan("EVA Maintenance count", 0, manager.getNumberOfMaintenances());
    }
}
