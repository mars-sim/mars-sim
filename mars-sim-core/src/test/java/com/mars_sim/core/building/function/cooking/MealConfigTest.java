package com.mars_sim.core.building.function.cooking;


import com.mars_sim.core.AbstractMarsSimUnitTest;

public class MealConfigTest extends AbstractMarsSimUnitTest {

    public void testGetDishList() {
        var mealConf = getConfig().getMealConfiguration();

        var mains = mealConf.getMainDishList();
        assertFalse("Mains are not empty", mains.isEmpty());

        var sides = mealConf.getSideDishList();
        assertFalse("Sides are not empty", sides.isEmpty());

    }

    public void testGarlicBread() {
        var name = "Garlic Bread";
        var mealConf = getConfig().getMealConfiguration();

        var rice = mealConf.getHotMeal(name);

        assertNotNull(name, rice);
        assertEquals("Name", name, rice.getMealName());
        assertEquals("Salt", 0.01, rice.getSalt());
        assertEquals("Oil", 0.15, rice.getOil());
        assertEquals("Category", MealConfig.SIDE_DISH, rice.getCategory());

        var ingredients = rice.getIngredientList();
        assertEquals("Ingregients", 2, ingredients.size());
    }

    public void testMainDish() {
        var name = "Garlic Tofu and Potatoes";
        var mealConf = getConfig().getMealConfiguration();

        var mainDish = mealConf.getHotMeal(name);

        assertNotNull(name, mainDish);
        assertEquals("Name", name, mainDish.getMealName());
        assertEquals("Salt", 0.01, mainDish.getSalt());
        assertEquals("Oil", 0.005, mainDish.getOil());
        assertEquals("Category", MealConfig.MAIN_DISH, mainDish.getCategory());

        var ingredients = mainDish.getIngredientList();
        assertEquals("Ingregients", 5, ingredients.size());
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
