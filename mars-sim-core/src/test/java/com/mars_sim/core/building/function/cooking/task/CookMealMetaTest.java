package com.mars_sim.core.building.function.cooking.task;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingCategory;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.MarsTime;

public class CookMealMetaTest extends AbstractMarsSimUnitTest {
    public void testMealTime() {
        var s = buildSettlement();
     
        buildKitchen(s.getBuildingManager());

        setupScenario(s);

        var mt = new CookMealMeta();

        var schedule = s.getMealTimes();

        var breakfast = schedule.getMeals().get(0);
        var preBreakfast = new MarsTime(1, 1, 1, breakfast.period().start() - (CookMeal.PREP_TIME + 1), 0);
		getSim().getMasterClock().setMarsTime(preBreakfast);

        var results = mt.getSettlementTasks(s);
        assertTrue("No meals before breakfast", results.isEmpty());

        // Change to middle of breakfast
        setMidBreakfastTime(s);
        results = mt.getSettlementTasks(s);
        assertEquals("Kitchens for meals", 1, results.size());
    }

    public void testMultiKitchen() {
        var s = buildSettlement();
     
        var b1 = buildKitchen(s.getBuildingManager());
        var b2 = buildKitchen(s.getBuildingManager());

        setupScenario(s);

        var mt = new CookMealMeta();

        setMidBreakfastTime(s);

        var results = mt.getSettlementTasks(s);
        assertEquals("Kitchens for meals", 2, results.size());
        var kitchens = results.stream().map(t -> t.getFocus()).toList();
        assertTrue("Kitchen 1 found", kitchens.contains(b1));
        assertTrue("Kitchen 2 found", kitchens.contains(b2));
    }

    public void testCooks() {
        var s = buildSettlement();
     
        var b = buildKitchen(s.getBuildingManager());
        var k = b.getCooking();

        setupScenario(s);

        var mt = new CookMealMeta();

        setMidBreakfastTime(s);

        var results = mt.getSettlementTasks(s);
        assertEquals("Kitchens for meals", 1, results.size());
        var task = results.get(0);
        assertEquals("Meal task demand", k.getCookCapacity(), task.getDemand());

        var p1 = buildPerson("Chef", s, JobType.CHEF, b, FunctionType.COOKING);
        p1.getTaskManager().replaceTask(new CookMeal(p1, k));
        results = mt.getSettlementTasks(s);
        task = results.get(0);
        assertEquals("Meal task demand after 1 chef", k.getCookCapacity() - 1, task.getDemand());

        var p2 = buildPerson("Chef", s, JobType.CHEF, b, FunctionType.COOKING);
        p2.getTaskManager().replaceTask(new CookMeal(p2, k));
        results = mt.getSettlementTasks(s);
        assertTrue("No meals no space", results.isEmpty());
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
