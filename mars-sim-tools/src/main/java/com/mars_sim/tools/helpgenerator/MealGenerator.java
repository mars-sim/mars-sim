/*
 * Mars Simulation Project
 * MealGenerator.java
 * @date 2025-02-8
 * @author Barry Evans
 */
package com.mars_sim.tools.helpgenerator;

import java.util.List;

import com.mars_sim.core.building.function.cooking.DishRecipe;

/**
 * Generates help content for HotMeal
 */
class MealGenerator extends TypeGenerator<DishRecipe> {

    static final String TYPE_NAME = "meal";

    MealGenerator(HelpContext parent) {
        super(parent, TYPE_NAME, "Meal", "Meal that are prepared",
        "meals");
                
        // Groups by part type
        setGrouper("Dish Type", r-> r.getCategory().getLabel());
    }

    /**
     * Get a list of all configured food processes
     */
    @Override
    protected List<DishRecipe> getEntities() {
		return getConfig().getMealConfiguration().getDishList().stream()
		 							.sorted((o1, o2)->o1.getName().compareTo(o2.getName()))
									.toList();
	
    }

    @Override
    protected String getEntityName(DishRecipe v) {
        return v.getName();
    }
}
