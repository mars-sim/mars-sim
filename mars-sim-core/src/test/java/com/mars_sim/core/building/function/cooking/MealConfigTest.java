package com.mars_sim.core.building.function.cooking;


import com.mars_sim.core.AbstractMarsSimUnitTest;

public class MealConfigTest extends AbstractMarsSimUnitTest {

    public void testGetDishList() {
        var mealConf = getConfig().getMealConfiguration();

        var mains = mealConf.getDishList(DishCategory.MAIN);
        assertFalse("Mains are not empty", mains.isEmpty());

        var sides = mealConf.getDishList(DishCategory.SIDE);
        assertFalse("Sides are not empty", sides.isEmpty());

        var desserts = mealConf.getDishList(DishCategory.DESSERT);
        assertFalse("Desserts are not empty", desserts.isEmpty());
    }

    public void testGarlicBread() {
        var name = "Garlic Bread";
        var mealConf = getConfig().getMealConfiguration();

        var rice = mealConf.getHotMeal(name);

        assertNotNull(name, rice);
        assertEquals("Name", name, rice.getName());
        assertEquals("Salt", 0.01, rice.getSalt());
        assertEquals("Oil", 0.15, rice.getOil());
        assertEquals("Category", DishCategory.SIDE, rice.getCategory());

        var ingredients = rice.getIngredientList();
        assertEquals("Ingredients", 3, ingredients.size());
    }

    public void testDessertDish() {
        var name = "Strawberries";
        var mealConf = getConfig().getMealConfiguration();

        var dish = mealConf.getHotMeal(name);

        assertNotNull(name, dish);
        assertEquals("Name", name, dish.getName());
        assertEquals("Salt", 0D, dish.getSalt());
        assertEquals("Oil", 0D, dish.getOil());
        assertEquals("Category", DishCategory.DESSERT, dish.getCategory());

        var ingredients = dish.getIngredientList();
        assertEquals("Ingredients", 2, ingredients.size());
        assertEquals("Ingredient 0", "Strawberry", ingredients.get(0).getName());
        assertEquals("Ingredient 1", "Sugar", ingredients.get(1).getName());
    }

    public void testMainDish() {
        var name = "Garlic Tofu and Potatoes";
        var mealConf = getConfig().getMealConfiguration();

        var mainDish = mealConf.getHotMeal(name);

        assertNotNull(name, mainDish);
        assertEquals("Name", name, mainDish.getName());
        assertEquals("Salt", 0.01, mainDish.getSalt());
        assertEquals("Oil", 0.005, mainDish.getOil());
        assertEquals("Category", DishCategory.MAIN, mainDish.getCategory());

        var ingredients = mainDish.getIngredientList();
        assertEquals("Ingredients", 5, ingredients.size());
        assertTrue("Ingredient 0 mandatory", ingredients.get(0).isMandatory());
        assertTrue("Ingredient 1 mandatory", ingredients.get(1).isMandatory());
        assertTrue("Ingredient 2 mandatory", ingredients.get(2).isMandatory());

        assertFalse("Ingredient 3 mandatory", ingredients.get(3).isMandatory());
        assertEquals("Ingredient 3 impact", 0.75, ingredients.get(3).getImpact());
        assertFalse("Ingredient 4 mandatory", ingredients.get(4).isMandatory());
        assertEquals("Ingredient 4 impact", 0.5, ingredients.get(4).getImpact());
    }

    public void testAvailableIngredients() {
        var mealConf = getConfig().getMealConfiguration();
        var meal = mealConf.getHotMeal("Garlic Tofu and Potatoes");

        var s = buildSettlement();

        assertFalse("Ingredients not available", meal.isIngredientsAvailable(s));
    
        // Add just optional ingredients    
        for(var i : meal.getIngredientList()) {
            if (!i.isMandatory()) {
                s.storeAmountResource(i.getAmountResourceID(), i.getDryMass());
            }
        }
        assertFalse("Mandatory ingredients not available", meal.isIngredientsAvailable(s));
        
        // Add mandatory ingredients    
        for(var i : meal.getIngredientList()) {
            if (i.isMandatory()) {
                s.storeAmountResource(i.getAmountResourceID(), i.getDryMass());
            }
        }
        assertTrue("All ingredients available", meal.isIngredientsAvailable(s));
    }

    public void testRetrieveIngredients() {
        var mealConf = getConfig().getMealConfiguration();
        var meal = mealConf.getHotMeal("Garlic Tofu and Potatoes");

        var s = buildSettlement();

        // Add mandatory ingredients    
        for(var i : meal.getIngredientList()) {
            if (i.isMandatory()) {
                s.storeAmountResource(i.getAmountResourceID(), i.getDryMass());
            }
        }

        // CLaim all ingredients
        var minimalQuality = meal.retrieveIngredients(s);
        for(var i : meal.getIngredientList()) {
            assertEquals("Ingredient stored " + i.getName(), 0D, s.getAmountResourceStored(i.getAmountResourceID()));
        }

        // Add all ingriedients
        for(var i : meal.getIngredientList()) {
            s.storeAmountResource(i.getAmountResourceID(), i.getDryMass());
        }
        var bestQuality = meal.retrieveIngredients(s);
        assertLessThan("Meal quality",  bestQuality, minimalQuality);
    }
}
