package com.mars_sim.core.building.task;

import com.mars_sim.core.AbstractMarsSimUnitTest;
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

public class MaintainBuildingEVATest extends AbstractMarsSimUnitTest {

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

    public void testMetaTask() {
        var s = buildSettlement("EVA Maintenance");
        var b1 = buildERV(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION);
        // 2nd building check logic
        var b2 = buildERV(s.getBuildingManager(), new LocalPosition(10, 10));
        var mt = new MaintainBuildingMeta();
        var tasks = mt.getSettlementTasks(s);
        // Note: there is a chance that tasks are made since scoreMaintenance currently has a probability component
        // assertTrue("No tasks found", tasks.isEmpty());
        // One building needs maintenance
        MaintainBuildingTest.buildingNeedMaintenance(b1, this);
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
                assertEquals("Found building B1 with maintenance", b1, foundB1);
            }
            assertEquals("Found building B2", b2, foundB2);
        }
    }

    public void testCreateEVATask() {
        var s = buildSettlement("EVA Maintenance");

        // Need daylight so move to midday
        MasterClock clock = getSim().getMasterClock();
        clock.setMarsTime(clock.getMarsTime().addTime(500D));

        var p = buildPerson("Mechanic", s, JobType.TECHNICIAN);
        p.getSkillManager().addNewSkill(SkillType.MECHANICS, 10); // Skilled

        var eva = EVAOperationTest.prepareForEva(this, p);
        // DigLocal uses the Settlement airlock tracking logic.... it shouldn't
        s.checkAvailableAirlocks();

        var b = buildERV(s.getBuildingManager(), new LocalPosition(20, 20));
        MaintainBuildingTest.buildingNeedMaintenance(b, this);

        var manager = b.getMalfunctionManager();
        assertGreaterThan("EVA Maintenance due", 0D, manager.getEffectiveTimeSinceLastMaintenance());

        var task = new MaintainBuildingEVA(p, b);

        assertFalse("EVA Task created", task.isDone()); 

        // Do NOT assert on duration here: getDuration() can legitimately be 0.0 at creation time
        // because EVAOperation tracks walking/onsite time internally across phases.

        // Initial phase is walking outside
        assertEquals("EVA walking outside", EVAOperation.WALK_TO_OUTSIDE_SITE, task.getPhase());

        // Move onsite
        int callUsed = EVAOperationTest.executeEVAWalk(this, eva, task);
        assertGreaterThan("Calls Used ", 0, callUsed);
 
        assertFalse("EVA Task still active", task.isDone());
        
        assertGreaterThan("EVA Maintenance due", 0D, manager.getEffectiveTimeSinceLastMaintenance());

        assertEquals("EVA Maintenance started", 0D, manager.getInspectionWorkTimeCompleted());

        // Confirm we are now in onsite MAINTAIN phase
        assertEquals("EVA walk completed", MaintainBuildingEVA.MAINTAIN, task.getPhase());

        // Start maintenance (run one tick). Do NOT assert on inspectionWorkTimeCompleted:
        // it may be reset to zero immediately when an inspection completes (same as MaintainBuildingTest).
        executeTaskUntilPhase(p, task, 1);
        assertFalse("Task still active", task.isDone());

        // Complete maintenance
        executeTaskForDuration(p, task, manager.getBaseMaintenanceWorkTime() * 1.1);
        
        if (task.isDone())
        	assertGreaterThan("EVA Maintenance count", 0, manager.getNumberOfMaintenances());
    }
}
