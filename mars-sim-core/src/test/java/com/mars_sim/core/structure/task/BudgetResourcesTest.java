package com.mars_sim.core.structure.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.structure.task.BudgetResources.ReviewGoal;
import com.mars_sim.core.structure.task.BudgetResourcesMeta.BudgetResourcesJob;

public class BudgetResourcesTest extends MarsSimUnitTest{
    @Test
    public void testCreateSettlementwaterReviewTask() {
        var s = buildSettlement("Budget", true);
        var p = buildPerson("Accountant", s);

        int diff = s.getRationing().reviewRationingLevel();

        s.getRationing().setReviewDue(true);
        assertTrue(s.getRationing().isReviewDue(), "Settlement water needs review");
        var task = new BudgetResources(p, ReviewGoal.WATER_RATIONING);
        assertFalse(task.isDone(), "Task is active");
        assertFalse(s.getRationing().isReviewDue(), "Settlement water needs no review");

        // Continue to complete review
        var ph = task.getPhase();
        executeTaskUntilPhase(p, task, 1000);
        assertFalse(task.isDone(), "Task is still active");
        assertNotEquals(ph, task.getPhase(), "Phase changed");

        // Approval
        executeTaskForDuration(p, task, task.getTimeLeft());
        assertTrue(task.isDone(), "Task is done");
    }

    @Test
    public void testCreateIceReviewTask() {
        var s = buildSettlement("Budget", true);
        var b = buildAccommodation(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D, 0);
        var p = buildPerson("Accountant", s);
        BuildingManager.addToActivitySpot(p, b, FunctionType.LIVING_ACCOMMODATION);

        s.setIceReviewDue(true);
        assertTrue(s.isIceReviewDue(), "Ice Probability needs review");
        var task = new BudgetResources(p, ReviewGoal.ICE_RESOURCE);
        assertTrue(s.getIceDemandCache() != s.getRecommendedIceDemand(), "Ice Cache is not the same as the new Ice Prob");
        assertFalse(task.isDone(), "Task is active");
        assertTrue(s.isIceReviewDue(), "Ice Prob review is due");

        // Continue to complete review
        var ph = task.getPhase();
        executeTaskUntilPhase(p, task, 1000);
        assertFalse(task.isDone(), "Task is still active");
        assertNotEquals(ph, task.getPhase(), "Phase changed");

        // Approval
        executeTaskForDuration(p, task, task.getTimeLeft());
        assertTrue(task.isDone(), "Task is done");
        assertFalse(s.isIceReviewDue(), "Ice Prob review is no longer due");
        // ice approval is now set to be due
        assertTrue(s.isIceApprovalDue(), "Ice Prob approval is due");
    }

    @Test
    public void testCreateResourceReviewTask() {
        var s = buildSettlement("Budget", true);
        var p = buildPerson("Accountant", s);

        var task = new BudgetResources(p, ReviewGoal.LIFE_RESOURCE);
        assertFalse(task.isDone(), "Task is active");

        // Continue to complete review
        var ph = task.getPhase();
        executeTaskUntilPhase(p, task, 1000);
        assertTrue(!task.isDone() || task.injectDemand(), "Task is either still active or has failed");
        assertNotEquals(ph, task.getPhase(), "Phase changed");

        // Approval
        executeTaskForDuration(p, task, task.getTimeLeft());
        assertTrue(task.isDone(), "Task is done");
    }

    @Test
    public void testResourceReviewResetTask() {
        var s = buildSettlement("Budget", true);
        var p = buildPerson("Accountant", s);

        var gm = s.getGoodsManager();
        int resources = gm.getResourceReviewDue();
        assertGreaterThan("Resources needing review", 0D, resources);

        for(int i = 0; i < resources; i++) {
            var task = new BudgetResources(p, ReviewGoal.LIFE_RESOURCE);
            assertFalse(task.isDone(), "Task is active");
            assertLessThan("Resource going down", resources, gm.getResourceReviewDue());
        }
        assertEquals(0, gm.getResourceReviewDue(), "No resources needing review");

        // Try one more task
        var task = new BudgetResources(p, ReviewGoal.LIFE_RESOURCE);
        assertTrue(task.isDone(), "Task found no resource");

        gm.resetEssentialsReview();
        assertEquals(resources, gm.getResourceReviewDue(), "Resoruces reset");
    }

    @Test
    public void testBudgetResourceMeta() {
        // Build a Settlement needing water review
        var s = buildSettlement("Budget", true);
        buildPerson("Accountant", s); // Need at least 1 person for water demand

        s.getRationing().reviewRationingLevel();
        s.getRationing().setReviewDue(true);

        var mt = new BudgetResourcesMeta();

        var tasks = mt.getSettlementTasks(s);

        // Expect one per review goal
        assertEquals(2, tasks.size(), "Expect settlement tasks");

        // Check each task
        Set<ReviewGoal> found = new HashSet<>();
        for(var t : tasks) {
            BudgetResourcesJob brj = (BudgetResourcesJob) t;
            var goal = brj.getGoal();
            found.add(goal);
            int expect = switch(goal) {
                case ICE_RESOURCE -> 1;
                case REGOLITH_RESOURCE -> 1;
                case LIFE_RESOURCE -> s.getGoodsManager().getResourceReviewDue();
                case WATER_RATIONING -> 1;
            };

            assertEquals(expect, brj.getDemand(), "Expected demaind for " + goal.name());
        }
        assertEquals(2, found.size(), "Found goals");
    }
}
