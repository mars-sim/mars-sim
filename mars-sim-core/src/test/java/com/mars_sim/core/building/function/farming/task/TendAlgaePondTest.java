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
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;

public class TendAlgaePondTest extends MarsSimUnitTest {
    private Building buildAlgaePond(Settlement s) {
        return buildFunction(s.getBuildingManager(), "Algae Pond", BuildingCategory.FARMING,
                            FunctionType.ALGAE_FARMING, LocalPosition.DEFAULT_POSITION, 0D, true);
    }

    @Test
    public void testPersonTending() {
        var s = buildSettlement("Fish");
        var b = buildAlgaePond(s);
        var p = buildPerson("fisherman", s, JobType.ASTROBIOLOGIST, b, FunctionType.ALGAE_FARMING);
        var pond = b.getAlgae();

        var task = new TendAlgaePond(p, pond, TendAlgaePond.TENDING);
        assertFalse(task.isDone(), "Tending task created");

        var remaining = executeTaskForDuration(p, task, TendAlgaePond.MAX_TEND * 1.1);
        assertGreaterThan("Remaining Task time", 0D, remaining);

        assertTrue(task.isDone(), "Tending task completed");       

        assertGreaterThan("Cummulative work", 0D, pond.getCumulativeWorkTime());
    }

    @Test
    public void testPersonHarvest() {
        var s = buildSettlement("Fish");
        var b = buildAlgaePond(s);
        var p = buildPerson("fisherman", s, JobType.ASTROBIOLOGIST, b, FunctionType.ALGAE_FARMING);
        var pond = b.getAlgae();
        var shortfall = (pond.getIdealAlgae() * 2) - pond.getCurrentAlgae();
        pond.addAlgae(shortfall);

        var task = new TendAlgaePond(p, pond, TendAlgaePond.HARVESTING);
        assertFalse(task.isDone(), "Harvesting task created");

        executeTaskForDuration(p, task, 1.1 * TendAlgaePond.MAX_HARVESTING);

        assertTrue(task.isDone(), "Harvesting task completed");       

        assertTrue(s.getSpecificAmountResourceStored(ResourceUtil.SPIRULINA_ID) >0D, "Algae harvest created");
        assertTrue(s.getSpecificAmountResourceStored(ResourceUtil.FOOD_WASTE_ID) >0D, "Food waste created");
    }
    
}
