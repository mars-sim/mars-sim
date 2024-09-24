package com.mars_sim.core.structure.task;

import static org.junit.Assert.assertNotEquals;

import java.util.HashSet;
import java.util.Set;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.structure.task.BudgetResources.ReviewGoal;
import com.mars_sim.core.structure.task.BudgetResourcesMeta.BudgetResourcesJob;

public class BudgetResourcesTest extends AbstractMarsSimUnitTest{
    public void testCreateSettlementwaterReviewTask() {
        var s = buildSettlement("Budget", true);
        var p = buildPerson("Accountant", s);

        s.isWaterRatioChanged();
        s.setReviewWaterRatio(true);
        assertTrue("Settlement water needs review", s.canReviewWaterRatio());
        var task = new BudgetResources(p, ReviewGoal.SETTLEMENT_WATER);
        assertFalse("Task is active", task.isDone());
        assertFalse("Settlement water needs no review", s.canReviewWaterRatio());

        // Continue to complete review
        var ph = task.getPhase();
        executeTaskUntilPhase(p, task, 1000);
        assertFalse("Task is still active", task.isDone());
        assertNotEquals("Phase changed", ph, task.getPhase());

        // Approval
        executeTaskForDuration(p, task, task.getTimeLeft());
        assertTrue("Task is done", task.isDone());
    }

    public void testCreateAccomWaterReviewTask() {
        var s = buildSettlement("Budget", true);
        var b = buildAccommodation(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D, 0);
        var accom = b.getLivingAccommodation();
        var p = buildPerson("Accountant", s);
        BuildingManager.addPersonToActivitySpot(p, b, FunctionType.LIVING_ACCOMMODATION);

        accom.unlockWaterRatioReview();
        assertTrue("Accom needs review", accom.canReviewWaterRatio());
        var task = new BudgetResources(p, ReviewGoal.ACCOM_WATER);
        assertFalse("Task is active", task.isDone());
        assertFalse("Accom no review", accom.canReviewWaterRatio());

        // Continue to complete review
        var ph = task.getPhase();
        executeTaskUntilPhase(p, task, 1000);
        assertFalse("Task is still active", task.isDone());
        assertNotEquals("Phase changed", ph, task.getPhase());

        // Approval
        executeTaskForDuration(p, task, task.getTimeLeft());
        assertTrue("Task is done", task.isDone());
    }

    public void testCreateResourceReviewTask() {
        var s = buildSettlement("Budget", true);
        var p = buildPerson("Accountant", s);

        var task = new BudgetResources(p, ReviewGoal.RESOURCE);
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
            var task = new BudgetResources(p, ReviewGoal.RESOURCE);
            assertFalse("Task is active", task.isDone());
            assertLessThan("Resource going down", resources, gm.getResourceReviewDue());
        }
        assertEquals("No resources needing review", 0, gm.getResourceReviewDue());

        // Try one more task
        var task = new BudgetResources(p, ReviewGoal.RESOURCE);
        assertTrue("Task found no resource", task.isDone());

        gm.resetEssentialsReview();
        assertEquals("Resoruces reset", resources, gm.getResourceReviewDue());
    }

    public void testBudgetResourceMeta() {
        // Build a Settlemnt needing water review
        var s = buildSettlement("Budget", true);
        buildPerson("Accountant", s); // Need at least 1 person for water demand

        s.isWaterRatioChanged();
        s.setReviewWaterRatio(true);

        // Build 2 accomodtiom, one for review and the other now
        var bm = s.getBuildingManager();
        var b = buildAccommodation(bm, LocalPosition.DEFAULT_POSITION, 0D, 0);
        var accom = b.getLivingAccommodation();
        accom.unlockWaterRatioReview();
        buildAccommodation(bm, new LocalPosition(10,10), 0D, 1);


        var mt = new BudgetResourcesMeta();

        var tasks = mt.getSettlementTasks(s);

        // Expect one per review goal
        assertEquals("Expecte settlement tasks", 3, tasks.size());

        // Check each task
        Set<ReviewGoal> found = new HashSet<>();
        for(var t : tasks) {
            BudgetResourcesJob brj = (BudgetResourcesJob) t;
            var goal = brj.getGoal();
            found.add(goal);
            int expect = switch(goal) {
                case ACCOM_WATER -> 1;
                case RESOURCE -> s.getGoodsManager().getResourceReviewDue();
                case SETTLEMENT_WATER -> 1;
            };

            assertEquals("Expected demaind for " + goal.name(), expect, brj.getDemand());
        }
        assertEquals("Found goals", 3, found.size());
    }
}
