package com.mars_sim.core.building.function.cooking;
import static com.mars_sim.core.test.SimulationAssertions.assertLessThan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;


import com.mars_sim.core.test.MarsSimUnitTest;

public class MealConfigTest extends MarsSimUnitTest {

    @Test
    public void testGetDishList() {
        var mealConf = getConfig().getMealConfiguration();

        var mains = mealConf.getDishList(DishCategory.MAIN);
        assertFalse(mains.isEmpty(), "Mains are not empty");

        var sides = mealConf.getDishList(DishCategory.SIDE);
        assertFalse(sides.isEmpty(), "Sides are not empty");

        var desserts = mealConf.getDishList(DishCategory.DESSERT);
        assertFalse(desserts.isEmpty(), "Desserts are not empty");
    }

    @Test
    public void testGarlicBread() {
        var name = "Garlic Bread";
        var mealConf = getConfig().getMealConfiguration();

        var rice = mealConf.getHotMeal(name);

        assertNotNull(rice, name);
        assertEquals(name, rice.getName(), "Name");
        assertEquals(0.01, rice.getSalt(), "Salt");
        assertEquals(0.15, rice.getOil(), "Oil");
        assertEquals(DishCategory.SIDE, rice.getCategory(), "Category");

        var ingredients = rice.getIngredientList();
        assertEquals(3, ingredients.size(), "Ingredients");
    }

    @Test
    public void testDessertDish() {
        var name = "Strawberries";
        var mealConf = getConfig().getMealConfiguration();

        var dish = mealConf.getHotMeal(name);

        assertNotNull(dish, name);
        assertEquals(name, dish.getName(), "Name");
        assertEquals(0D, dish.getSalt(), "Salt");
        assertEquals(0D, dish.getOil(), "Oil");
        assertEquals(DishCategory.DESSERT, dish.getCategory(), "Category");

        var ingredients = dish.getIngredientList();
        assertEquals(2, ingredients.size(), "Ingredients");
        assertEquals("Strawberry", ingredients.get(0).getName(), "Ingredient 0");
        assertEquals("Sugar", ingredients.get(1).getName(), "Ingredient 1");
    }

    @Test
    public void testMainDish() {
        var name = "Garlic Tofu and Potatoes";
        var mealConf = getConfig().getMealConfiguration();

        var mainDish = mealConf.getHotMeal(name);

        assertNotNull(mainDish, name);
        assertEquals(name, mainDish.getName(), "Name");
        assertEquals(0.01, mainDish.getSalt(), "Salt");
        assertEquals(0.005, mainDish.getOil(), "Oil");
        assertEquals(DishCategory.MAIN, mainDish.getCategory(), "Category");

        var ingredients = mainDish.getIngredientList();
        assertEquals(5, ingredients.size(), "Ingredients");
        assertTrue(ingredients.get(0).isMandatory(), "Ingredient 0 mandatory");
        assertTrue(ingredients.get(1).isMandatory(), "Ingredient 1 mandatory");
        assertTrue(ingredients.get(2).isMandatory(), "Ingredient 2 mandatory");

        assertFalse(ingredients.get(3).isMandatory(), "Ingredient 3 mandatory");
        assertEquals(0.75, ingredients.get(3).getImpact(), "Ingredient 3 impact");
        assertFalse(ingredients.get(4).isMandatory(), "Ingredient 4 mandatory");
        assertEquals(0.5, ingredients.get(4).getImpact(), "Ingredient 4 impact");
    }

    @Test
    public void testAvailableIngredients() {
        var mealConf = getConfig().getMealConfiguration();
        var meal = mealConf.getHotMeal("Garlic Tofu and Potatoes");

        var s = buildSettlement("mock");

        assertFalse(meal.isIngredientsAvailable(s), "Ingredients not available");
    
        // Add just optional ingredients    
        for(var i : meal.getIngredientList()) {
            if (!i.isMandatory()) {
                s.storeAmountResource(i.getAmountResourceID(), i.getDryMass());
            }
        }
        assertFalse(meal.isIngredientsAvailable(s), "Mandatory ingredients not available");
        
        // Add mandatory ingredients    
        for(var i : meal.getIngredientList()) {
            if (i.isMandatory()) {
                s.storeAmountResource(i.getAmountResourceID(), i.getDryMass());
            }
        }
        assertTrue(meal.isIngredientsAvailable(s), "All ingredients available");
    }

    @Test
    public void testRetrieveIngredients() {
        var mealConf = getConfig().getMealConfiguration();
        var meal = mealConf.getHotMeal("Garlic Tofu and Potatoes");

        var s = buildSettlement("mock");

        // Add mandatory ingredients    
        for(var i : meal.getIngredientList()) {
            if (i.isMandatory()) {
                s.storeAmountResource(i.getAmountResourceID(), i.getDryMass());
            }
        }

        // CLaim all ingredients
        var minimalQuality = meal.retrieveIngredients(s);
        for(var i : meal.getIngredientList()) {
            assertEquals(0D, s.getSpecificAmountResourceStored(i.getAmountResourceID()), "Ingredient stored " + i.getName());
        }

        // Add all ingriedients
        for(var i : meal.getIngredientList()) {
            s.storeAmountResource(i.getAmountResourceID(), i.getDryMass());
        }
        var bestQuality = meal.retrieveIngredients(s);
        assertLessThan("Meal quality",  bestQuality, minimalQuality);
    }
}
