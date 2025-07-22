package com.mars_sim.core.building.function.farming.task;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingCategory;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;

public class TendFishTankTest extends AbstractMarsSimUnitTest {
    private Building buildFishery(Settlement s) {
        return buildFunction(s.getBuildingManager(), "Fish Farm", BuildingCategory.FARMING,
                            FunctionType.FISHERY, LocalPosition.DEFAULT_POSITION, 0D, true);
    }

    public void testPersonTending() {
        var s = buildSettlement("Fish");
        var b = buildFishery(s);
        var p = buildPerson("fisherman", s, JobType.BIOLOGIST, b, FunctionType.FISHERY);
        var tank = b.getFishery();

        var task = new TendFishTank(p, tank, TendFishTank.TENDING);
        assertFalse("Tending task created", task.isDone());

        var remaining = executeTaskForDuration(p, task, TendFishTank.MAX_TEND * 1.1);
        assertGreaterThan("Remaining Task time", 0D, remaining);

        assertTrue("Tending task completed", task.isDone());       

        assertGreaterThan("Cummulative work", 0D, tank.getCumulativeWorkTime());
    }

    /**
     * This is common and actually testing TendHousekeeping
     */
    public void testPersonInspecting() {
        var s = buildSettlement("Fish");
        var b = buildFishery(s);
        var p = buildPerson("fisherman", s, JobType.BIOLOGIST, b, FunctionType.FISHERY);
        var tank = b.getFishery();
        var origScore = tank.getHousekeeping().getAverageInspectionScore();

        var task = new TendFishTank(p, tank, TendFishTank.INSPECTING);
        assertFalse("Inspect task created", task.isDone());

        var remaining = executeTaskForDuration(p, task, TendFishTank.MAX_INSPECT_TIME * 1.1);
        assertGreaterThan("Remaining Task time", 0D, remaining);

        assertTrue("Inspect task completed", task.isDone());       
        var newScore = tank.getHousekeeping().getAverageInspectionScore();
        assertGreaterThan("Inspect improved", origScore, newScore);
        assertGreaterThan("Cumulative work", 0D, tank.getCumulativeWorkTime());
    }

    /**
     * This is common and actually testing TendHousekeeping
     */
    public void testPersonCleaning() {
        
        var s = buildSettlement("Fish");
        var b = buildFishery(s);
        var p = buildPerson("fisherman", s, JobType.BIOLOGIST, b, FunctionType.FISHERY);
        var tank = b.getFishery();
        var origClean = tank.getHousekeeping().getAverageCleaningScore();

        var task = new TendFishTank(p, tank, TendFishTank.CLEANING);
        assertFalse("Clean task created", task.isDone());

        executeTaskForDuration(p, task, TendFishTank.MAX_CLEANING_TIME * 1.1);

        assertTrue("Clean task completed", task.isDone());       
        var newClean = tank.getHousekeeping().getAverageCleaningScore();
        assertGreaterThan("Cleaning improved", origClean, newClean);
        assertGreaterThan("Cumulative work", 0D, tank.getCumulativeWorkTime());
    }

    public void testPersonFishing() {

        var s = buildSettlement("Fish");
        var b = buildFishery(s);
        var p = buildPerson("fisherman", s, JobType.BIOLOGIST, b, FunctionType.FISHERY);

        var tank = b.getFishery();
        tank.addFish(tank.getMaxFish() - tank.getNumFish());
        int origSize = tank.getNumFish();

        var task = new TendFishTank(p, b.getFishery(), TendFishTank.CATCHING);
        assertFalse("Fishing task created", task.isDone());

        executeTaskForDuration(p, task, TendFishTank.MAX_FISHING * 1.1);
        assertTrue("Fishing task completed", task.isDone());
        
        assertTrue("Fish meat created", s.getSpecificAmountResourceStored(ResourceUtil.FISH_MEAT_ID) >0D);
        assertTrue("Fish oil created", s.getSpecificAmountResourceStored(ResourceUtil.FISH_OIL_ID) >0D);
        assertLessThan("Fish count has reduced", origSize, tank.getNumFish());
    }
}
