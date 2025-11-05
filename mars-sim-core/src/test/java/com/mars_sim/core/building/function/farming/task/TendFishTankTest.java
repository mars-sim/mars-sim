package com.mars_sim.core.building.function.farming.task;
import static com.mars_sim.core.test.SimulationAssertions.assertGreaterThan;
import static com.mars_sim.core.test.SimulationAssertions.assertLessThan;

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

public class TendFishTankTest extends MarsSimUnitTest {
    private Building buildFishery(Settlement s) {
        return buildFunction(s.getBuildingManager(), "Fish Farm", BuildingCategory.FARMING,
                            FunctionType.FISHERY, LocalPosition.DEFAULT_POSITION, 0D, true);
    }

    @Test
    public void testPersonTending() {
        var s = buildSettlement("Fish");
        var b = buildFishery(s);
        var p = buildPerson("fisherman", s, JobType.ASTROBIOLOGIST, b, FunctionType.FISHERY);
        var tank = b.getFishery();

        var task = new TendFishTank(p, tank, TendFishTank.TENDING);
        assertFalse(task.isDone(), "Tending task created");

        var remaining = executeTaskForDuration(p, task, TendFishTank.MAX_TEND * 1.1);
        assertGreaterThan("Remaining Task time", 0D, remaining);

        assertTrue(task.isDone(), "Tending task completed");       

        assertGreaterThan("Cummulative work", 0D, tank.getCumulativeWorkTime());
    }

    /**
     * This is common and actually testing TendHousekeeping
     */
    @Test
    public void testPersonInspecting() {
        var s = buildSettlement("Fish");
        var b = buildFishery(s);
        var p = buildPerson("fisherman", s, JobType.ASTROBIOLOGIST, b, FunctionType.FISHERY);
        var tank = b.getFishery();
        var origScore = tank.getHousekeeping().getAverageInspectionScore();

        var task = new TendFishTank(p, tank, TendFishTank.INSPECTING);
        assertFalse(task.isDone(), "Inspect task created");

        var remaining = executeTaskForDuration(p, task, TendFishTank.MAX_INSPECT_TIME * 1.1);
        assertGreaterThan("Remaining Task time", 0D, remaining);

        assertTrue(task.isDone(), "Inspect task completed");       
        var newScore = tank.getHousekeeping().getAverageInspectionScore();
        assertGreaterThan("Inspect improved", origScore, newScore);
        assertGreaterThan("Cumulative work", 0D, tank.getCumulativeWorkTime());
    }

    /**
     * This is common and actually testing TendHousekeeping
     */
    @Test
    public void testPersonCleaning() {
        
        var s = buildSettlement("Fish");
        var b = buildFishery(s);
        var p = buildPerson("fisherman", s, JobType.ASTROBIOLOGIST, b, FunctionType.FISHERY);
        var tank = b.getFishery();
        var origClean = tank.getHousekeeping().getAverageCleaningScore();

        var task = new TendFishTank(p, tank, TendFishTank.CLEANING);
        assertFalse(task.isDone(), "Clean task created");

        executeTaskForDuration(p, task, TendFishTank.MAX_CLEANING_TIME * 1.1);

        assertTrue(task.isDone(), "Clean task completed");       
        var newClean = tank.getHousekeeping().getAverageCleaningScore();
        assertGreaterThan("Cleaning improved", origClean, newClean);
        assertGreaterThan("Cumulative work", 0D, tank.getCumulativeWorkTime());
    }

    @Test
    public void testPersonFishing() {

        var s = buildSettlement("Fish");
        var b = buildFishery(s);
        var p = buildPerson("fisherman", s, JobType.ASTROBIOLOGIST, b, FunctionType.FISHERY);

        var tank = b.getFishery();
        tank.addFish(tank.getMaxFish() - tank.getNumFish());
        int origSize = tank.getNumFish();

        var task = new TendFishTank(p, b.getFishery(), TendFishTank.CATCHING);
        assertFalse(task.isDone(), "Fishing task created");

        executeTaskForDuration(p, task, TendFishTank.MAX_FISHING * 1.1);
        assertTrue(task.isDone(), "Fishing task completed");
        
        assertTrue(s.getSpecificAmountResourceStored(ResourceUtil.FISH_MEAT_ID) >0D, "Fish meat created");
        assertTrue(s.getSpecificAmountResourceStored(ResourceUtil.FISH_OIL_ID) >0D, "Fish oil created");
        assertLessThan("Fish count has reduced", origSize, tank.getNumFish());
    }
}
