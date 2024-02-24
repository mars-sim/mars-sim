/*
 * Mars Simulation Project
 * FoodGenerator.java
 * @date 2024-02-23
 * @author Barry Evans
 */
package com.mars_sim.tools.helpgenerator;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import com.mars_sim.core.food.FoodProductionProcessInfo;
import com.mars_sim.core.food.FoodProductionProcessItem;
import com.mars_sim.tools.helpgenerator.HelpGenerator.ProcessItem;

/**
 * Generates help content for Food Production processes
 */
class FoodGenerator extends TypeGenerator<FoodProductionProcessInfo> {

    static final String TYPE_NAME = "food";

    FoodGenerator(HelpGenerator parent) {
        super(parent, TYPE_NAME, "Food Recipes", "List of Recipes to create Food");
    }

    /**
     * Generator an output for a specific food process. This will also use the process-input partial template
     * as well as the main food-detail template.
     * @param v Food recipe being rendered
     * @param output Destination of content
     */
    @Override
    public void generateEntity(FoodProductionProcessInfo v, OutputStream output) throws IOException {
        var generator = getParent();

        var vScope = generator.createScopeMap("Food " + v.getName());
        vScope.put("food", v);

        addProcessInputOutput(vScope, "Ingredients", getProcessItemsFromFood(v.getInputList()),
                              "Outcomes", getProcessItemsFromFood(v.getOutputList()));

        generator.generateContent("food-detail", vScope, output);
    }

    static List<ProcessItem> getProcessItemsFromFood(List<FoodProductionProcessItem> items) {
		return items.stream()
					.sorted((o1, o2)->o1.getName().compareTo(o2.getName()))
					.map(v -> HelpGenerator.toProcessItem(v.getName(), v.getType(), v.getAmount()))
					.toList();
	}

    /**
     * Get a list of all configured food processes
     */
    @Override
    protected List<FoodProductionProcessInfo> getEntities() {
		return getParent().getConfig().getFoodProductionConfiguration()
                        .getFoodProductionProcessList().stream()
		 							.sorted((o1, o2)->o1.getName().compareTo(o2.getName()))
									.toList();
	
    }

    @Override
    protected String getEntityName(FoodProductionProcessInfo v) {
        return v.getName();
    }
}
