package com.mars_sim.core.vehicle.task;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.task.EVAOperationTest;
import com.mars_sim.core.person.ai.task.LoadingController;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.mapdata.location.LocalPosition;

public class LoadVehicleEVATest extends AbstractMarsSimUnitTest {
    public void testCreateTask() {
        var s = buildSettlement("Vehicle base");

        // Load the vehicle
        var v = buildRover(s, "rover1", new LocalPosition(10, 10));
        var p = buildPerson("Mechanic", s, JobType.TECHNICIAN);
        p.getSkillManager().addNewSkill(SkillType.AREOLOGY, 10); // Skilled
        var eva = EVAOperationTest.prepareForEva(this, p);

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

        var task = new LoadVehicleEVA(p, lc);
        assertFalse("Task created", task.isDone()); 

        // Move onsite
        EVAOperationTest.executeEVAWalk(this, eva, task);

        // Do maintenance and advance to return
        executeTaskUntilPhase(p, task, 1000);
        assertGreaterThan("Final stored mass", 0D, v.getStoredMass());

        // Return to base
        EVAOperationTest.executeEVAWalk(this, eva, task);
        assertTrue("Task completed", task.isDone()); 

    }
}
