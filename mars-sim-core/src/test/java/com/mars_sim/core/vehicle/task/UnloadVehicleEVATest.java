package com.mars_sim.core.vehicle.task;
import static com.mars_sim.core.test.SimulationAssertions.assertGreaterThan;
import static com.mars_sim.core.test.SimulationAssertions.assertLessThan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.task.EVAOperationTest;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.vehicle.StatusType;

public class UnloadVehicleEVATest extends MarsSimUnitTest {
    private static final int ITEM_AMOUNT = 10;
    private static final double RESOURCE_AMOUNT = 10;

    @Test
    public void testCreateTask() {
        var s = buildSettlement("Vehicle base");

        // Load the vehicle
        var v = buildRover(s, "rover1", new LocalPosition(10, 10));
        v.storeAmountResource(ResourceUtil.OXYGEN_ID, RESOURCE_AMOUNT);
        v.storeAmountResource(ResourceUtil.FOOD_ID, RESOURCE_AMOUNT);
        v.storeItemResource(ItemResourceUtil.GARMENT_ID, ITEM_AMOUNT);
        
        double mass = v.getStoredMass();
        // 10 + 10 + .5 * 10 = 25.0
        System.out.println("vehicle stored mass: " + mass);
        
        assertGreaterThan("Initial stored mass", 0D, mass);

        var p = buildPerson("Mechanic", s, JobType.TECHNICIAN);
        p.getSkillManager().addNewSkill(SkillType.MECHANICS, 10); // Skilled
        p.getNaturalAttributeManager().adjustAttribute(NaturalAttributeType.STRENGTH, 100);
        var eva = EVAOperationTest.prepareForEva(this, p);

        v.addSecondaryStatus(StatusType.UNLOADING);
        var task = new UnloadVehicleEVA(p, v);
        assertFalse(task.isDone(), "Task created"); 

        // Move onsite
        EVAOperationTest.executeEVAWalk(this, eva, task);

        double storedO2Settlement0 = s.getSpecificAmountResourceStored(ResourceUtil.OXYGEN_ID);
//        System.out.println("storedO2Settlement0: " + storedO2Settlement0);
        
//        double storedO2Person = p.getSpecificAmountResourceStored(ResourceUtil.OXYGEN_ID);
//        System.out.println("storedO2Person: " + storedO2Person);
        
//        double storedO2Vehicle = v.getSpecificAmountResourceStored(ResourceUtil.OXYGEN_ID);
//        System.out.println("storedO2Vehicle: " + storedO2Vehicle);
        
        // Do maintenance and advance to return
        executeTaskUntilPhase(p, task, 3000);
        
        mass = v.getStoredMass();
        System.out.println("vehicle stored mass: " + mass);
        
        assertEquals(0D, mass, "Final stored mass");
        assertFalse(v.haveStatusType(StatusType.UNLOADING), "Vehicle has UNLOADING");

        double storedO2Settlement1 = s.getSpecificAmountResourceStored(ResourceUtil.OXYGEN_ID);
        System.out.println("storedO2Settlement1: " + storedO2Settlement1); 
        
        assertLessThan("Oxygen unloaded", storedO2Settlement0 + RESOURCE_AMOUNT, storedO2Settlement1);
        
        double storedFood = s.getSpecificAmountResourceStored(ResourceUtil.FOOD_ID);
        System.out.println("storedFood: " + storedFood);
        
        assertEquals(RESOURCE_AMOUNT, storedFood, "Food unloaded");
        
        int storedGarment = s.getItemResourceStored(ItemResourceUtil.GARMENT_ID);
        System.out.println("storedGarment: " + storedGarment);
        
        assertEquals(ITEM_AMOUNT, storedGarment, "Garments unloaded");

        // Return to base
        EVAOperationTest.executeEVAWalk(this, eva, task);
        assertTrue(task.isDone(), "Task completed"); 
    }

    @Test
    public void testMetaTask() {
        var s = buildSettlement("Vehicle base", true);

        var v = buildRover(s, "rover1", new LocalPosition(10, 10));

        var mt = new UnloadVehicleMeta();

        // Skip empty vehicle
        var tasks = mt.getSettlementTasks(s);
        assertEquals(0, tasks.size(), "Mo unload tasks found");

        // Load and make reserved
        v.storeAmountResource(ResourceUtil.OXYGEN_ID, 10D);
        v.storeAmountResource(ResourceUtil.FOOD_ID, 10D);
        v.storeItemResource(ItemResourceUtil.GARMENT_ID, 10);

        // Skip reserved vehicle
        tasks = mt.getSettlementTasks(s);
        assertEquals(0, tasks.size(), "Skip vehicle with wrong state");

        // Unreserved vehicle
        v.addSecondaryStatus(StatusType.UNLOADING);
        tasks = mt.getSettlementTasks(s);
        assertEquals(1, tasks.size(), "Found vehicle");
        // No garages so EVA 
        var t = tasks.get(0);
        assertTrue(t.isEVA(), "Task is EVA");
    }
}
