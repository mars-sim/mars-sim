/*
 * Mars Simulation Project
 * MealGenerator.java
 * @date 2025-02-8
 * @author Barry Evans
 */
package com.mars_sim.tools.helpgenerator;

import java.util.List;

import com.mars_sim.core.building.function.cooking.HotMeal;

/**
 * Generates help content for HotMeal
 */
class MealGenerator extends TypeGenerator<HotMeal> {

    static final String TYPE_NAME = "meal";

    MealGenerator(HelpContext parent) {
        super(parent, TYPE_NAME, "Meal", "Meal that are prepared",
        "meals");
                
        // Groups by part type
        setGrouper("Meal Type", r-> r.getCategory());
    }

    /**
     * Get a list of all configured food processes
     */
    @Override
    protected List<HotMeal> getEntities() {
		return getConfig().getMealConfiguration().getDishList().stream()
		 							.sorted((o1, o2)->o1.getMealName().compareTo(o2.getMealName()))
									.toList();
	
    }

    @Override
    protected String getEntityName(HotMeal v) {
        return v.getMealName();
    }
}
