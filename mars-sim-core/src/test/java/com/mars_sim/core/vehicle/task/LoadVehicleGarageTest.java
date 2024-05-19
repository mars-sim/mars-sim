package com.mars_sim.core.vehicle.task;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.mapdata.location.LocalPosition;

public class LoadVehicleGarageTest extends AbstractMarsSimUnitTest {
    public void testCreateTask() {
        var s = buildSettlement("Vehicle base");
        var g = buildGarage(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D, 0);

        // Load the vehicle
        var v = buildRover(s, "rover1", new LocalPosition(10, 10));
        var p = buildPerson("Mechanic", s, JobType.TECHNICIAN, g.getBuilding(), FunctionType.VEHICLE_MAINTENANCE);
        p.getSkillManager().addNewSkill(SkillType.MECHANICS, 10); // Skilled

        // Create a loading plan and preload Settlement
        Map<Integer, Number> resources = new HashMap<>();
        resources.put(ResourceUtil.oxygenID, 10D);
        resources.put(ResourceUtil.waterID, 10D);
        resources.put(ResourceUtil.foodID, 10D);
        for(var entry : resources.entrySet()) {
            s.storeAmountResource(entry.getKey(), entry.getValue().doubleValue() * 1.1D);
        }
        LoadingController lc = new LoadingController(s, v, resources, Collections.emptyMap(),
                                        Collections.emptyMap(), Collections.emptyMap());

        var task = new LoadVehicleGarage(p, lc);
        assertFalse("Task created", task.isDone()); 

        // Do maintenance and advance to return
        executeTaskUntilPhase(p, task, 1000);
        assertGreaterThan("Final stored mass", 0D, v.getStoredMass());

        // Return to base
        assertTrue("Task completed", task.isDone()); 
    }
}
