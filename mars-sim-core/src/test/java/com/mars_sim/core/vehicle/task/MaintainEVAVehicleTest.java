package com.mars_sim.core.vehicle.task;
import static com.mars_sim.core.test.SimulationAssertions.assertGreaterThan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.task.EVAOperationTest;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.vehicle.StatusType;

public class MaintainEVAVehicleTest extends MarsSimUnitTest {

    @Test
    public void testCreateTask() {
        var s = buildSettlement("Vehicle base");

        var v = buildRover(s, "rover1", new LocalPosition(10, 10), EXPLORER_ROVER);
        var mm = v.getMalfunctionManager();
        assertGreaterThan("Vehicle maintenance time", 0D, mm.getEffectiveTimeSinceLastMaintenance());

        var p = buildPerson("Mechanic", s, JobType.TECHNICIAN);
        p.getSkillManager().addNewSkill(SkillType.MECHANICS, 10); // Skilled
        var eva = EVAOperationTest.prepareForEva(getContext(), p);

        var task = new MaintainEVAVehicle(p, v);
        assertFalse(task.isDone(), "Task created"); 
        assertTrue(v.isReservedForMaintenance(), "Vehicle reserved");
        assertTrue(v.haveStatusType(StatusType.MAINTENANCE), "Vehicle status is Maintenance");

        // Move onsite
        EVAOperationTest.executeEVAWalk(getContext(), eva, task);

        // Do maintenance and advance to return
        executeTaskUntilPhase(p, task, 100);

        assertEquals(0D, mm.getInspectionWorkTimeCompleted(), "Maintenance time completed reset");
     
        assertFalse(v.haveStatusType(StatusType.MAINTENANCE), "Vehicle status out of Maintenance");
        assertFalse(v.isReservedForMaintenance(), "Vehicle not reserved");
        
        assertGreaterThan("Vehicle maintenance time has been reset and increase again", 0D, mm.getEffectiveTimeSinceLastMaintenance());
    
        // Return to base
        EVAOperationTest.executeEVAWalk(getContext(), eva, task);
        assertTrue(task.isDone(), "Task completed"); 
    }

    @Test
    public void testMetaTask() {
        var s = buildSettlement("Vehicle base");

        var v = buildRover(s, "rover1", new LocalPosition(10, 10), EXPLORER_ROVER);
        var mm = v.getMalfunctionManager();

        // Create a massive pulse to trigger maintenance
        var master = getSim().getMasterClock();
        var pulse = new ClockPulse(1, mm.getStandardInspectionWindow(), master.getMarsTime(), master, false, false, true, false);
        mm.activeTimePassing(pulse);
        assertGreaterThan("Vehicle maintenance time", 0D, mm.getEffectiveTimeSinceLastMaintenance());

        var mt = new MaintainVehicleMeta();
        var tasks = mt.getSettlementTasks(s);

        assertEquals(1, tasks.size(), "Maintenance tasks found");

        // No garages so EVA 
        var t = tasks.get(0);
        assertTrue(t.isEVA(), "Task is EVA");

    }
}
