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

    public void testBrownRice() {
        var name = "Steamed Brown Rice";
        var mealConf = getConfig().getMealConfiguration();

        var rice = mealConf.getHotMeal(name);

        assertNotNull(name, rice);
        assertEquals("Name", name, rice.getMealName());
        assertEquals("Salt", 0.01, rice.getSalt());
        assertEquals("Oil", 0.05, rice.getOil());
        assertEquals("Category", MealConfig.MAIN_DISH, rice.getCategory());

        var ingredients = rice.getIngredientList();
        assertEquals("Ingregients", 3, ingredients.size());
    }
}
