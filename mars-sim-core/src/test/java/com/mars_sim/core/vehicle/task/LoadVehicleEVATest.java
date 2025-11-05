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
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.resource.SuppliesManifest;

public class LoadVehicleEVATest extends MarsSimUnitTest {
    @Test
    public void testCreateTask() {
        var s = buildSettlement("Vehicle base");

        // Load the vehicle
        var v = buildRover(s, "rover1", new LocalPosition(10, 10), EXPLORER_ROVER);
        var p = buildPerson("Mechanic", s, JobType.TECHNICIAN);
        p.getSkillManager().addNewSkill(SkillType.AREOLOGY, 10); // Skilled
        var eva = EVAOperationTest.prepareForEva(this, p);

        // Create a loading plan and preload Settlement
        var resources = new SuppliesManifest();
        resources.addAmount(ResourceUtil.OXYGEN_ID, 10D, true);
        resources.addAmount(ResourceUtil.WATER_ID, 10D, true);
        resources.addAmount(ResourceUtil.FOOD_ID, 10D, true);
        LoadControllerTest.loadSettlement(s, resources);
        v.setLoading(resources);

        var task = new LoadVehicleEVA(p, v);
        assertFalse(task.isDone(), "Task created"); 

        // Move onsite
        EVAOperationTest.executeEVAWalk(this, eva, task);

        // Do maintenance and advance to return
        executeTaskUntilPhase(p, task, 1000);
        assertGreaterThan("Final stored mass", 0D, v.getStoredMass());

        // Return to base
        EVAOperationTest.executeEVAWalk(this, eva, task);
        assertTrue(task.isDone(), "Task completed"); 

    }

    @Test
    public void testMetaTask() {
        var s = buildSettlement("Vehicle base", true);

        // Load the vehicle
        var v = buildRover(s, "rover1", new LocalPosition(10, 10), EXPLORER_ROVER);
        buildRover(s, "rover2", new LocalPosition(10, 13), EXPLORER_ROVER);

        var mt = new LoadVehicleMeta();

        // Check with no loading
        var tasks = mt.getSettlementTasks(s);
        assertTrue(tasks.isEmpty(), "No load tasks found");

        // Set rover to be loading
        var resources = new SuppliesManifest();
        resources.addAmount(ResourceUtil.OXYGEN_ID, 10D, true);
        v.setLoading(resources);

        tasks = mt.getSettlementTasks(s);
        assertEquals(1, tasks.size(), "One load tasks found");
        var t = tasks.get(0);
        assertTrue(t.isEVA(), "Load in eva");
        assertEquals(v, t.getFocus(), "Correct vehicle selected");
    }
}
