package com.mars_sim.core.building.function.cooking.task;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingCategory;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.MarsTime;

public class CookMealMetaTest extends MarsSimUnitTest {
    @Test
    public void testMealTime() {
        var s = buildSettlement("mock");
     
        buildKitchen(s.getBuildingManager());

        setupScenario(s);

        var mt = new CookMealMeta();

        var schedule = s.getMealTimes();

        var breakfast = schedule.getMeals().get(0);
        var preBreakfast = new MarsTime(1, 1, 1, breakfast.period().start() - (CookMeal.PREP_TIME + 1), 0);
		getSim().getMasterClock().setMarsTime(preBreakfast);

        var results = mt.getSettlementTasks(s);
        assertTrue(results.isEmpty(), "No meals before breakfast");

        // Change to middle of breakfast
        setMidBreakfastTime(s);
        results = mt.getSettlementTasks(s);
        assertEquals(1, results.size(), "Kitchens for meals");
    }

    @Test
    public void testMultiKitchen() {
        var s = buildSettlement("mock");
     
        var b1 = buildKitchen(s.getBuildingManager());
        var b2 = buildKitchen(s.getBuildingManager());

        setupScenario(s);

        var mt = new CookMealMeta();

        setMidBreakfastTime(s);

        var results = mt.getSettlementTasks(s);
        assertEquals(2, results.size(), "Kitchens for meals");
        var kitchens = results.stream().map(t -> t.getFocus()).toList();
        assertTrue(kitchens.contains(b1), "Kitchen 1 found");
        assertTrue(kitchens.contains(b2), "Kitchen 2 found");
    }

    @Test
    public void testCooks() {
        var s = buildSettlement("mock");
     
        var b = buildKitchen(s.getBuildingManager());
        var k = b.getCooking();

        setupScenario(s);

        var mt = new CookMealMeta();

        setMidBreakfastTime(s);

        var results = mt.getSettlementTasks(s);
        assertEquals(1, results.size(), "Kitchens for meals");
        var task = results.get(0);
        assertEquals(k.getCookCapacity(), task.getDemand(), "Meal task demand");

        var p1 = buildPerson("Chef", s, JobType.CHEF, b, FunctionType.COOKING);
        p1.getTaskManager().replaceTask(new CookMeal(p1, k));
        results = mt.getSettlementTasks(s);
        task = results.get(0);
        
        String taskName = p1.getTaskManager().getTaskDescription(false);
        System.out.println(p1 + " " + taskName);
        int numCooks = k.getNumCooks();
        System.out.println(p1 + " at " + p1.getBuildingLocation() + " (numCooks: " + numCooks + ")");
        assertEquals(k.getCookCapacity() - 1, task.getDemand(), "Meal task demand after 1 chef");

        var p2 = buildPerson("Chef", s, JobType.CHEF, b, FunctionType.COOKING);
        p2.getTaskManager().replaceTask(new CookMeal(p2, k));
        results = mt.getSettlementTasks(s);
        assertTrue(results.isEmpty(), "No meals no space");
    }

    private void setMidBreakfastTime(Settlement s) {
        var schedule = s.getMealTimes();
        var breakfast = schedule.getMeals().get(0);
        var midBreakfast = new MarsTime(1, 1, 1, breakfast.period().start()+1, 0);
		getSim().getMasterClock().setMarsTime(midBreakfast);
    }

    private void setupScenario(Settlement s) {
        // Add people to generate a demand for Meals
        buildPerson("Hungry 1", s);

        // Add ingreidents for a meal
        var mealConf = getConfig().getMealConfiguration();
        var gBread = mealConf.getHotMeal("Garlic Bread");
        for(var i : gBread.getIngredientList()) {
            s.storeAmountResource(i.getAmountResourceID(), 1000);
        }
    }

    private Building buildKitchen(BuildingManager buildingManager) {
		return buildFunction(buildingManager, "Lander Hab", BuildingCategory.LABORATORY,
							FunctionType.COOKING,  LocalPosition.DEFAULT_POSITION, 0D, true);
    }
}
