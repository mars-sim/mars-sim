package com.mars_sim.core.building.function.farming.task;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingCategory;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.structure.Settlement;

public class TendGreenhouseTest extends AbstractMarsSimUnitTest {
    
    private static final String GREENHOUSE = "Inground Greenhouse";

    private Building buildGreenhouse(Settlement s) {
        var building = buildFunction(s.getBuildingManager(), GREENHOUSE, BuildingCategory.FARMING,
                            FunctionType.FARMING, LocalPosition.DEFAULT_POSITION, 0D, true);
    
    
		var spec = simConfig.getBuildingConfiguration().getFunctionSpec(GREENHOUSE, FunctionType.RESEARCH);

	    building.addFunction(spec);
		s.getBuildingManager().refreshFunctionMapForBuilding(building);
        return building;
    }

    public void testPersonSampling() {
        var s = buildSettlement("Fish", true);
        var b = buildGreenhouse(s);
        var p = buildPerson("gardener", s, JobType.BOTANIST, b, FunctionType.FARMING);
        var farm = b.getFarming();

        var task = new TendGreenhouse(p, farm, TendGreenhouse.SAMPLING);
        assertFalse("Tending task created", task.isDone());

        executeTask(p, task, 10000);

        assertTrue("Tending task completed", task.isDone());       
    }

    public void testPersonTransfer() {
        var s = buildSettlement("Fish");
        var b = buildGreenhouse(s);
        var p = buildPerson("gardener", s, JobType.BOTANIST, b, FunctionType.FARMING);
        var farm = b.getFarming();

        var task = new TendGreenhouse(p, farm, TendGreenhouse.TRANSFERRING_SEEDLING);
        assertFalse("Seeding task created", task.isDone());

        executeTask(p, task, 5000);

        assertTrue("Seeding task completed", task.isDone());       
    }

    public void testPersonTending() {
        var s = buildSettlement("Fish");
        var b = buildGreenhouse(s);
        var p = buildPerson("gardener", s, JobType.BOTANIST, b, FunctionType.FARMING);
        var farm = b.getFarming();

        var task = new TendGreenhouse(p, farm, TendGreenhouse.TENDING);
        assertFalse("Tending task created", task.isDone());

        executeTask(p, task, 5000);

        assertTrue("Tending task completed", task.isDone());       

        assertGreaterThan("Cummulative work", 0D, farm.getCumulativeWorkTime());
    }

    public void testPersonGrowingTissue() {
        var s = buildSettlement("Fish");
        var b = buildGreenhouse(s);
        var p = buildPerson("gardener", s, JobType.BOTANIST, b, FunctionType.FARMING);
        var farm = b.getFarming();

        var task = new TendGreenhouse(p, farm, TendGreenhouse.GROWING_TISSUE);
        assertFalse("Growing task created", task.isDone());

        executeTask(p, task, 5000);

        assertTrue("Growing task completed", task.isDone());       
    }
}
