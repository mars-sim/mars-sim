package com.mars_sim.core.vehicle.task;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.task.EVAOperationTest;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.mapdata.location.LocalPosition;

public class UnloadVehicleEVATest extends AbstractMarsSimUnitTest {
   public void testCreateTask() {
        var s = buildSettlement("Vehicle base");

        // Load the vehicle
        var v = buildRover(s, "rover1", new LocalPosition(10, 10));
        v.storeAmountResource(ResourceUtil.oxygenID, 10D);
        v.storeAmountResource(ResourceUtil.foodID, 10D);
        v.storeItemResource(ItemResourceUtil.garmentID, 10);
        assertGreaterThan("Initial stored mass", 0D, v.getStoredMass());

        var p = buildPerson("Mechanic", s, JobType.TECHNICIAN);
        p.getSkillManager().addNewSkill(SkillType.MECHANICS, 10); // Skilled
        var eva = EVAOperationTest.prepareForEva(this, p);

        var task = new UnloadVehicleEVA(p, v);
        assertFalse("Task created", task.isDone()); 

        // Move onsite
        EVAOperationTest.executeEVAWalk(this, eva, task);

        // Do maintenance and advance to return
        executeTaskUntilPhase(p, task, 1000);
        assertEquals("Final stored mass", 0D, v.getStoredMass());

        // Return to base
        EVAOperationTest.executeEVAWalk(this, eva, task);
        assertTrue("Task completed", task.isDone()); 
    }

    public void testMetaTask() {
        var s = buildSettlement("Vehicle base", true);

        var v = buildRover(s, "rover1", new LocalPosition(10, 10));

        var mt = new UnloadVehicleMeta();

        // Skip empty vehicle
        var tasks = mt.getSettlementTasks(s);
        assertEquals("Mo unload tasks found", 0, tasks.size());

        // Load and make reserved
        v.storeAmountResource(ResourceUtil.oxygenID, 10D);
        v.storeAmountResource(ResourceUtil.foodID, 10D);
        v.storeItemResource(ItemResourceUtil.garmentID, 10);
        v.setReservedForMission(true);

        // Skip reserved vehicle
        tasks = mt.getSettlementTasks(s);
        assertEquals("Skip reserved vehicle", 0, tasks.size());

        // Unreserved vehicle
        v.setReservedForMission(false);
        tasks = mt.getSettlementTasks(s);
        assertEquals("Found vehicle", 1, tasks.size());
        // No garages so EVA 
        var t = tasks.get(0);
        assertTrue("Task is EVA", t.isEVA());
    }
}
