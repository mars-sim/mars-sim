package com.mars_sim.core.structure.task;

import static org.junit.Assert.assertNotEquals;

import java.util.HashSet;
import java.util.Set;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.structure.task.BudgetResources.ReviewGoal;
import com.mars_sim.core.structure.task.BudgetResourcesMeta.BudgetResourcesJob;

public class BudgetResourcesTest extends AbstractMarsSimUnitTest{
    public void testCreateSettlementwaterReviewTask() {
        var s = buildSettlement("Budget", true);
        var p = buildPerson("Accountant", s);

        int diff = s.getRationing().reviewRationingLevel();

        s.getRationing().setReviewDue(true);
        assertTrue("Settlement water needs review", s.getRationing().isReviewDue());
        var task = new BudgetResources(p, ReviewGoal.WATER_RATIONING);
        assertFalse("Task is active", task.isDone());
        assertFalse("Settlement water needs no review", s.getRationing().isReviewDue());

        // Continue to complete review
        var ph = task.getPhase();
        executeTaskUntilPhase(p, task, 1000);
        assertFalse("Task is still active", task.isDone());
        assertNotEquals("Phase changed", ph, task.getPhase());

        // Approval
        executeTaskForDuration(p, task, task.getTimeLeft());
        assertTrue("Task is done", task.isDone());
    }

    public void testCreateIceReviewTask() {
        var s = buildSettlement("Budget", true);
        var b = buildAccommodation(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D, 0);
        var p = buildPerson("Accountant", s);
        BuildingManager.addToActivitySpot(p, b, FunctionType.LIVING_ACCOMMODATION);

        s.setIceReviewDue(true);
        assertTrue("Ice Probability needs review", s.isIceReviewDue());
        var task = new BudgetResources(p, ReviewGoal.ICE_RESOURCE);
        assertTrue("Ice Cache is not the same as the new Ice Prob", 
        		s.getIceDemandCache() != s.getRecommendedIceDemand());
        assertFalse("Task is active", task.isDone());
        assertTrue("Ice Prob review is due", s.isIceReviewDue());

        // Continue to complete review
        var ph = task.getPhase();
        executeTaskUntilPhase(p, task, 1000);
        assertFalse("Task is still active", task.isDone());
        assertNotEquals("Phase changed", ph, task.getPhase());

        // Approval
        executeTaskForDuration(p, task, task.getTimeLeft());
        assertTrue("Task is done", task.isDone());
        assertFalse("Ice Prob review is no longer due", s.isIceReviewDue());
        // ice approval is now set to be due
        assertTrue("Ice Prob approval is due", s.isIceApprovalDue());
    }

    public void testCreateResourceReviewTask() {
        var s = buildSettlement("Budget", true);
        var p = buildPerson("Accountant", s);

        var task = new BudgetResources(p, ReviewGoal.LIFE_RESOURCE);
        assertFalse("Task is active", task.isDone());

        // Continue to complete review
        var ph = task.getPhase();
        executeTaskUntilPhase(p, task, 1000);
        assertTrue("Task is either still active or has failed", !task.isDone() || task.injectDemand());
        assertNotEquals("Phase changed", ph, task.getPhase());

        // Approval
        executeTaskForDuration(p, task, task.getTimeLeft());
        assertTrue("Task is done", task.isDone());
    }

    public void testResourceReviewResetTask() {
        var s = buildSettlement("Budget", true);
        var p = buildPerson("Accountant", s);

        var gm = s.getGoodsManager();
        int resources = gm.getResourceReviewDue();
        assertGreaterThan("Resources needing review", 0D, resources);

        for(int i = 0; i < resources; i++) {
            var task = new BudgetResources(p, ReviewGoal.LIFE_RESOURCE);
            assertFalse("Task is active", task.isDone());
            assertLessThan("Resource going down", resources, gm.getResourceReviewDue());
        }
        assertEquals("No resources needing review", 0, gm.getResourceReviewDue());

        // Try one more task
        var task = new BudgetResources(p, ReviewGoal.LIFE_RESOURCE);
        assertTrue("Task found no resource", task.isDone());

        gm.resetEssentialsReview();
        assertEquals("Resoruces reset", resources, gm.getResourceReviewDue());
    }

    public void testBudgetResourceMeta() {
        // Build a Settlement needing water review
        var s = buildSettlement("Budget", true);
        buildPerson("Accountant", s); // Need at least 1 person for water demand

        s.getRationing().reviewRationingLevel();
        s.getRationing().setReviewDue(true);

        var mt = new BudgetResourcesMeta();

        var tasks = mt.getSettlementTasks(s);

        // Expect one per review goal
        assertEquals("Expect settlement tasks", 2, tasks.size());

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

            assertEquals("Expected demaind for " + goal.name(), expect, brj.getDemand());
        }
        assertEquals("Found goals", 2, found.size());
    }
}
