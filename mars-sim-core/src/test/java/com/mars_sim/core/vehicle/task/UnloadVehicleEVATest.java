package com.mars_sim.core.vehicle.task;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.task.EVAOperationTest;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.vehicle.StatusType;

public class UnloadVehicleEVATest extends AbstractMarsSimUnitTest {
    private static final int ITEM_AMOUNT = 10;
    private static final int RESOURCE_AMOUNT = 10;

    public void testCreateTask() {
        var s = buildSettlement("Vehicle base");

        // Load the vehicle
        var v = buildRover(s, "rover1", new LocalPosition(10, 10));
        v.storeAmountResource(ResourceUtil.OXYGEN_ID, RESOURCE_AMOUNT);
        v.storeAmountResource(ResourceUtil.FOOD_ID, RESOURCE_AMOUNT);
        v.storeItemResource(ItemResourceUtil.garmentID, ITEM_AMOUNT);
        assertGreaterThan("Initial stored mass", 0D, v.getStoredMass());

        var p = buildPerson("Mechanic", s, JobType.TECHNICIAN);
        p.getSkillManager().addNewSkill(SkillType.MECHANICS, 10); // Skilled
        p.getNaturalAttributeManager().adjustAttribute(NaturalAttributeType.STRENGTH, 100);
        var eva = EVAOperationTest.prepareForEva(this, p);

        v.addSecondaryStatus(StatusType.UNLOADING);
        var task = new UnloadVehicleEVA(p, v);
        assertFalse("Task created", task.isDone()); 

        // Move onsite
        EVAOperationTest.executeEVAWalk(this, eva, task);

        // Do maintenance and advance to return
        executeTaskUntilPhase(p, task, 3000);
        assertEquals("Final stored mass", 0D, v.getStoredMass());
        assertFalse("Vehicle has UNLOADING", v.haveStatusType(StatusType.UNLOADING));

        // Oxygen has some from EVA suit as well
        assertGreaterThan("Oxygen unloaded", RESOURCE_AMOUNT, s.getAmountResourceStored(ResourceUtil.OXYGEN_ID));
        assertEquals("Food unloaded", RESOURCE_AMOUNT, Math.round(s.getAmountResourceStored(ResourceUtil.FOOD_ID)));
        assertEquals("Garments unloaded", ITEM_AMOUNT, s.getItemResourceStored(ItemResourceUtil.garmentID));


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
        v.storeAmountResource(ResourceUtil.OXYGEN_ID, 10D);
        v.storeAmountResource(ResourceUtil.FOOD_ID, 10D);
        v.storeItemResource(ItemResourceUtil.garmentID, 10);

        // Skip reserved vehicle
        tasks = mt.getSettlementTasks(s);
        assertEquals("Skip vehicle with wrong state", 0, tasks.size());

        // Unreserved vehicle
        v.addSecondaryStatus(StatusType.UNLOADING);
        tasks = mt.getSettlementTasks(s);
        assertEquals("Found vehicle", 1, tasks.size());
        // No garages so EVA 
        var t = tasks.get(0);
        assertTrue("Task is EVA", t.isEVA());
    }
}
