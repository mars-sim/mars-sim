package com.mars_sim.core.vehicle.task;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.vehicle.StatusType;

public class UnloadVehicleGarageTest extends AbstractMarsSimUnitTest {
    private static final int ITEM_AMOUNT = 10;
    private static final int RESOURCE_AMOUNT = 10;

    public void testCreateTask() {
        var s = buildSettlement("Vehicle base");
        var g = buildGarage(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D, 0);

        // Load the vehicle
        var v = buildRover(s, "rover1", new LocalPosition(10, 10));
        v.storeAmountResource(ResourceUtil.OXYGEN_ID, RESOURCE_AMOUNT);
        v.storeAmountResource(ResourceUtil.FOOD_ID, RESOURCE_AMOUNT);
        v.storeItemResource(ItemResourceUtil.garmentID, ITEM_AMOUNT);
        
        double mass = v.getStoredMass();
//        System.out.println("mass: " + mass);
        
        assertGreaterThan("Initial stored mass", 0D, mass);

        var p = buildPerson("Mechanic", s, JobType.TECHNICIAN, g.getBuilding(), FunctionType.VEHICLE_MAINTENANCE);
        p.getSkillManager().addNewSkill(SkillType.MECHANICS, 10); // Skilled

        v.addSecondaryStatus(StatusType.UNLOADING);
        var task = new UnloadVehicleGarage(p, v);
        assertFalse("Task created", task.isDone()); 

        // Do maintenance and advance to return
        executeTaskUntilPhase(p, task, 1000);
        
        mass = v.getStoredMass();
//        System.out.println("mass: " + mass);
        
        assertEquals("Final stored mass", 0.0, mass);
        assertEquals("Oxygen unloaded", RESOURCE_AMOUNT, Math.round(s.getSpecificAmountResourceStored(ResourceUtil.OXYGEN_ID)));
        assertEquals("Food unloaded", RESOURCE_AMOUNT, Math.round(s.getSpecificAmountResourceStored(ResourceUtil.FOOD_ID)));
        assertEquals("Garments unloaded", ITEM_AMOUNT, s.getItemResourceStored(ItemResourceUtil.garmentID));
        assertFalse("Vehicle has UNLOADING", v.haveStatusType(StatusType.UNLOADING));

        // Return to base
        assertTrue("Task completed", task.isDone()); 
    }

    public void testMetaTask() {
        var s = buildSettlement("Vehicle base", true);
        buildGarage(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D, 0);
        var v = buildRover(s, "rover1", new LocalPosition(10, 10));

        var mt = new UnloadVehicleMeta();

        // Skip empty vehicle
        var tasks = mt.getSettlementTasks(s);
        assertEquals("Mo unload tasks found", 0, tasks.size());

        // Load and make reserved
        v.storeAmountResource(ResourceUtil.OXYGEN_ID, RESOURCE_AMOUNT);
        v.storeAmountResource(ResourceUtil.FOOD_ID, RESOURCE_AMOUNT);
        v.storeItemResource(ItemResourceUtil.garmentID, ITEM_AMOUNT);

        // Skip reserved vehicle
        tasks = mt.getSettlementTasks(s);
        assertEquals("Skip reserved vehicle", 0, tasks.size());

         // Find vehicle with unload status
         v.addSecondaryStatus(StatusType.UNLOADING);
         
        tasks = mt.getSettlementTasks(s);
        assertEquals("Found vehicle", 1, tasks.size());
        // No garages so EVA 
        var t = tasks.get(0);
        assertFalse("Task is not EVA", t.isEVA());
    }
}
