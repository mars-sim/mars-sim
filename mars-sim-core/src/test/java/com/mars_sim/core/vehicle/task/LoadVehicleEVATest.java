package com.mars_sim.core.vehicle.task;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.task.EVAOperationTest;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.resource.SuppliesManifest;
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
        var resources = new SuppliesManifest();
        resources.addResource(ResourceUtil.oxygenID, 10D, true);
        resources.addResource(ResourceUtil.waterID, 10D, true);
        resources.addResource(ResourceUtil.foodID, 10D, true);
        for(var entry : resources.getResources(true).entrySet()) {
            s.storeAmountResource(entry.getKey(), entry.getValue().doubleValue() * 1.1D);
        }
        v.setLoading(resources);

        var task = new LoadVehicleEVA(p, v);
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

    public void testMetaTask() {
        var s = buildSettlement("Vehicle base", true);

        // Load the vehicle
        var v = buildRover(s, "rover1", new LocalPosition(10, 10));
        buildRover(s, "rover2", new LocalPosition(10, 13));

        var mt = new LoadVehicleMeta();

        // Check with no loading
        var tasks = mt.getSettlementTasks(s);
        assertTrue("No load tasks found", tasks.isEmpty());

        // Set rover to be loading
        var resources = new SuppliesManifest();
        resources.addResource(ResourceUtil.oxygenID, 10D, true);
        v.setLoading(resources);

        tasks = mt.getSettlementTasks(s);
        assertEquals("One load tasks found", 1, tasks.size());
        var t = tasks.get(0);
        assertTrue("Load in eva", t.isEVA());
        assertEquals("Correct vehicle selected", v, t.getFocus());
    }
}
