package com.mars_sim.core.building.function.farming.task;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingCategory;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.structure.Settlement;

public class TendGreenhouseTest extends MarsSimUnitTest {
    
    private static final String GREENHOUSE = "Inground Greenhouse";

    private Building buildGreenhouse(Settlement s) {
        var building = buildFunction(s.getBuildingManager(), GREENHOUSE, BuildingCategory.FARMING,
                            FunctionType.FARMING, LocalPosition.DEFAULT_POSITION, 0D, true);
    
    
		var spec = getConfig().getBuildingConfiguration().getFunctionSpec(GREENHOUSE, FunctionType.RESEARCH);

	    building.addFunction(spec);
		s.getBuildingManager().refreshFunctionMapForBuilding(building);
        return building;
    }

    @Test
    public void testPersonSampling() {
        var s = buildSettlement("Fish", true);
        var b = buildGreenhouse(s);
        var p = buildPerson("gardener", s, JobType.BOTANIST, b, FunctionType.FARMING);
        var farm = b.getFarming();

        var task = new TendGreenhouse(p, farm, TendGreenhouse.SAMPLING);
        assertFalse(task.isDone(), "Tending task created");

        executeTask(p, task, 10000);

        assertTrue(task.isDone(), "Tending task completed");       
    }

    @Test
    public void testPersonTransfer() {
        var s = buildSettlement("Fish");
        var b = buildGreenhouse(s);
        var p = buildPerson("gardener", s, JobType.BOTANIST, b, FunctionType.FARMING);
        var farm = b.getFarming();

        var task = new TendGreenhouse(p, farm, TendGreenhouse.TRANSFERRING_SEEDLING);
        assertFalse(task.isDone(), "Seeding task created");

        executeTask(p, task, 5000);

        assertTrue(task.isDone(), "Seeding task completed");       
    }

    @Test
    public void testPersonTending() {
        var s = buildSettlement("Fish");
        var b = buildGreenhouse(s);
        var p = buildPerson("gardener", s, JobType.BOTANIST, b, FunctionType.FARMING);
        var farm = b.getFarming();

        var task = new TendGreenhouse(p, farm, TendGreenhouse.TENDING);
        assertFalse(task.isDone(), "Tending task created");

        executeTask(p, task, 5000);

        assertTrue(task.isDone(), "Tending task completed");       

        assertGreaterThan("Cummulative work", 0D, farm.getCumulativeWorkTime());
    }

    @Test
    public void testPersonGrowingTissue() {
        var s = buildSettlement("Fish");
        var b = buildGreenhouse(s);
        var p = buildPerson("gardener", s, JobType.BOTANIST, b, FunctionType.FARMING);
        var farm = b.getFarming();

        var task = new TendGreenhouse(p, farm, TendGreenhouse.GROWING_TISSUE);
        assertFalse(task.isDone(), "Growing task created");

        executeTask(p, task, 5000);

        assertTrue(task.isDone(), "Growing task completed");       
    }
}
