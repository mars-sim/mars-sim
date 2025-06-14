/*
 * Mars Simulation Project
 * DishCategory.java
 * @date 2025-05-26
 * @author Barry Evans
 */
package com.mars_sim.core.building.function.cooking;

import com.mars_sim.core.tool.Msg;

// Category of dishes
public enum DishCategory {
    
	MAIN, SIDE, DESSERT;

    private String name;

    private DishCategory() {
        this.name = Msg.getStringOptional("DishCategory", name());
    }

    public String getName() {
        return name;
    }
}