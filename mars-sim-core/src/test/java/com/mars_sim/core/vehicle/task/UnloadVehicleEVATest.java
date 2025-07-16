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
        
        double stored = v.getStoredMass();
        // 10 + 10 + .5 * 10 = 25.0
//        System.out.println("stored: " + stored);
        
        assertGreaterThan("Initial stored mass", 0D, stored);

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
        
        stored = v.getStoredMass();
        System.out.println("stored: " + stored);
        
        assertEquals("Final stored mass", 0D, stored);
        assertFalse("Vehicle has UNLOADING", v.haveStatusType(StatusType.UNLOADING));

        // Oxygen has some from EVA suit as well
        double storedO2 = s.getAmountResourceStored(ResourceUtil.OXYGEN_ID);
        System.out.println("storedO2: " + storedO2);
        
        assertLessThan("Oxygen unloaded", RESOURCE_AMOUNT, storedO2);
        
        double storedFood = s.getAmountResourceStored(ResourceUtil.FOOD_ID);
        System.out.println("storedFood: " + storedFood);
        
        assertEquals("Food unloaded", 0.0, storedFood);
        
        int storedGarment = s.getItemResourceStored(ItemResourceUtil.garmentID);
        System.out.println("storedGarment: " + storedGarment);
        
        assertEquals("Garments unloaded", ITEM_AMOUNT, storedGarment);


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
