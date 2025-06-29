package com.mars_sim.core.building.function.farming.task;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingCategory;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;

public class TendAlgaePondTest extends AbstractMarsSimUnitTest {
    private Building buildAlgaePond(Settlement s) {
        return buildFunction(s.getBuildingManager(), "Algae Pond", BuildingCategory.FARMING,
                            FunctionType.ALGAE_FARMING, LocalPosition.DEFAULT_POSITION, 0D, true);
    }

    public void testPersonTending() {
        var s = buildSettlement("Fish");
        var b = buildAlgaePond(s);
        var p = buildPerson("fisherman", s, JobType.BIOLOGIST, b, FunctionType.ALGAE_FARMING);
        var pond = b.getAlgae();

        var task = new TendAlgaePond(p, pond, TendAlgaePond.TENDING);
        assertFalse("Tending task created", task.isDone());

        var remaining = executeTaskForDuration(p, task, TendAlgaePond.MAX_TEND * 1.1);
        assertGreaterThan("Remaining Task time", 0D, remaining);

        assertTrue("Tending task completed", task.isDone());       

        assertGreaterThan("Cummulative work", 0D, pond.getCumulativeWorkTime());
    }

    public void testPersonHarvest() {
        var s = buildSettlement("Fish");
        var b = buildAlgaePond(s);
        var p = buildPerson("fisherman", s, JobType.BIOLOGIST, b, FunctionType.ALGAE_FARMING);
        var pond = b.getAlgae();
        var shortfall = (pond.getIdealAlgae() * 2) - pond.getCurrentAlgae();
        pond.addAlgae(shortfall);

        var task = new TendAlgaePond(p, pond, TendAlgaePond.HARVESTING);
        assertFalse("Harvesting task created", task.isDone());

        executeTaskForDuration(p, task, 1.1 * TendAlgaePond.MAX_HARVESTING);

        assertTrue("Harvesting task completed", task.isDone());       

        assertTrue("Algae harvest created", s.getAmountResourceStored(ResourceUtil.SPIRULINA_ID) >0D);
        assertTrue("Food waste created", s.getAmountResourceStored(ResourceUtil.FOOD_WASTE_ID) >0D);
    }
    
}
