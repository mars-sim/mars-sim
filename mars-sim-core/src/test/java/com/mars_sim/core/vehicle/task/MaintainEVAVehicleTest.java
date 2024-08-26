package com.mars_sim.core.vehicle.task;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.task.EVAOperationTest;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.vehicle.StatusType;
import com.mars_sim.mapdata.location.LocalPosition;

public class MaintainEVAVehicleTest extends AbstractMarsSimUnitTest {



    public void testCreateTask() {
        var s = buildSettlement("Vehicle base");

        var v = buildRover(s, "rover1", new LocalPosition(10, 10));
        var mm = v.getMalfunctionManager();
        assertGreaterThan("Vehicle maintenance time", 0D, mm.getEffectiveTimeSinceLastMaintenance());

        var p = buildPerson("Mechanic", s, JobType.TECHNICIAN);
        p.getSkillManager().addNewSkill(SkillType.MECHANICS, 10); // Skilled
        var eva = EVAOperationTest.prepareForEva(this, p);

        var task = new MaintainEVAVehicle(p, v);
        assertFalse("Task created", task.isDone()); 
        assertTrue("Vehicle reserved", v.isReservedForMaintenance());
        assertTrue("Vehicle status is Maintenance", v.haveStatusType(StatusType.MAINTENANCE));

        // Move onsite
        EVAOperationTest.executeEVAWalk(this, eva, task);

        // Do maintenance and advance to return
        executeTaskUntilPhase(p, task, 1000);

        assertEquals("Maintenance time completed reset", 0D,
                            mm.getInspectionWorkTimeCompleted());
        assertFalse("Vehicle not reserved", v.isReservedForMaintenance());
        assertFalse("Vehicle status out of Maintenance", v.haveStatusType(StatusType.MAINTENANCE));
        assertEquals("Vehicle maintenance time reset", 0D, mm.getEffectiveTimeSinceLastMaintenance());
    
        // Return to base
        EVAOperationTest.executeEVAWalk(this, eva, task);
        assertTrue("Task completed", task.isDone()); 
    }

    public void testMetaTask() {
        var s = buildSettlement("Vehicle base");

        var v = buildRover(s, "rover1", new LocalPosition(10, 10));
        var mm = v.getMalfunctionManager();

        // Create a massive pulse to trigger maintenance
        var master = sim.getMasterClock();
        var pulse = new ClockPulse(1, mm.getMaintenancePeriod(), master.getMarsTime(), master, false, false, true, false);
        mm.activeTimePassing(pulse);
        assertGreaterThan("Vehicle maintenance time", 0D, mm.getEffectiveTimeSinceLastMaintenance());

        var mt = new MaintainVehicleMeta();
        var tasks = mt.getSettlementTasks(s);

        assertEquals("Maintenance tasks found", 1, tasks.size());

        // No garages so EVA 
        var t = tasks.get(0);
        assertTrue("Task is EVA", t.isEVA());

    }
}
