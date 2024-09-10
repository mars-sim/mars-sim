package com.mars_sim.core.vehicle.task;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.resource.SuppliesManifest;
import com.mars_sim.core.structure.building.function.FunctionType;

public class LoadVehicleGarageTest extends AbstractMarsSimUnitTest {
    public void testCreateTask() {
        var s = buildSettlement("Vehicle base");
        var g = buildGarage(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D, 0);

        // Load the vehicle
        var v = buildRover(s, "rover1", new LocalPosition(10, 10));
        var p = buildPerson("Mechanic", s, JobType.TECHNICIAN, g.getBuilding(), FunctionType.VEHICLE_MAINTENANCE);
        p.getSkillManager().addNewSkill(SkillType.MECHANICS, 10); // Skilled

        // Create a loading plan and preload Settlement
        var resources = new SuppliesManifest();
        resources.addAmount(ResourceUtil.oxygenID, 10D, true);
        resources.addAmount(ResourceUtil.waterID, 10D, true);
        resources.addAmount(ResourceUtil.foodID, 10D, true);
        LoadControllerTest.loadSettlement(s, resources);
        v.setLoading(resources);

        var task = new LoadVehicleGarage(p, v);
        assertFalse("Task created", task.isDone()); 

        // Do maintenance and advance to return
        executeTaskUntilPhase(p, task, 1000);
        assertGreaterThan("Final stored mass", 0D, v.getStoredMass());

        // Return to base
        assertTrue("Task completed", task.isDone()); 
    }

    public void testMetaTask() {
        var s = buildSettlement("Vehicle base", true);
        buildGarage(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D, 0);

        // Load the vehicle
        var v = buildRover(s, "rover1", new LocalPosition(10, 10));
        buildRover(s, "rover2", new LocalPosition(10, 13));

        var mt = new LoadVehicleMeta();

        // Check with no loading
        var tasks = mt.getSettlementTasks(s);
        assertTrue("No load tasks found", tasks.isEmpty());

        // Set rover to be loading
        var resources = new SuppliesManifest();
        resources.addAmount(ResourceUtil.oxygenID, 10D, true);
        v.setLoading(resources);

        tasks = mt.getSettlementTasks(s);
        assertEquals("One load tasks found", 1, tasks.size());
        var t = tasks.get(0);
        assertFalse("Load in not eva", t.isEVA());
        assertEquals("Correct vehicle selected", v, t.getFocus());
    }
}
