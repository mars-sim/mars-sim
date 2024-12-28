/*
 * Mars Simulation Project
 * FoodGenerator.java
 * @date 2024-02-23
 * @author Barry Evans
 */
package com.mars_sim.tools.helpgenerator;

import java.util.List;
import java.util.Map;

import com.mars_sim.core.food.FoodProductionProcessInfo;

/**
 * Generates help content for Food Production processes
 */
class FoodGenerator extends TypeGenerator<FoodProductionProcessInfo> {

    static final String TYPE_NAME = "food";

    FoodGenerator(HelpContext parent) {
        super(parent, TYPE_NAME, "Food Recipe", "Recipe (Food Process) to create Food",
        "food_production");
    }

    /**
     * Add specifics of how the Food process creates and consumes resources
     * @param process The process to display
     * @param scope Scope of the properties to use for the template
     * @return
     */
    @Override
    protected void addEntityProperties(FoodProductionProcessInfo process, Map<String,Object> scope) {

        addProcessInputOutput(scope,
                "Ingredients", toQuantityItems(process.getInputList()),
                "Outcomes", toQuantityItems(process.getOutputList()));
    }

    /**
     * Get a list of all configured food processes
     */
    @Override
    protected List<FoodProductionProcessInfo> getEntities() {
		return getParent().getConfig().getFoodProductionConfiguration()
                        .getProcessList().stream()
		 							.sorted((o1, o2)->o1.getName().compareTo(o2.getName()))
									.toList();
	
    }

    @Override
    protected String getEntityName(FoodProductionProcessInfo v) {
        return v.getName();
    }
}
