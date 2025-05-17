/*
 * Mars Simulation Project
 * FoodUtil.java
 * @date 2022-06-25
 * @author Manny Kung
 */
package com.mars_sim.core.food;

import java.util.List;

import com.mars_sim.core.goods.GoodType;
import com.mars_sim.core.resource.AmountResource;
import com.mars_sim.core.resource.ResourceUtil;

/**
 * Utility class for food information.
 */
public class FoodUtil {

	// Data members
	private static List<Food> foodList;

	/**
	 * Private constructor for utility class.
	 */
	private FoodUtil() {
	}

	/**
	 * Gets a list of all food in the simulation.
	 * 
	 * @return list of food
	 */
	public static List<Food> getFoodList() {

		if (foodList == null) {
			populateFoodList();
		}

		return foodList;
	}


	/**
	 * Creates the food instance from the amount resource.
	 * 
	 * @param resource
	 * @return
	 */
	private static Food createFoodResource(AmountResource resource) {
		if (resource == null) {
			throw new IllegalArgumentException("Resource cannot be null");
		}
		
		GoodType type = resource.getGoodType();

		// There is a direct mapping between the Foodtype enums to the equivalent
		// GoodType enum
		FoodType foodType = FoodType.valueOf(type.name());
		return new Food(resource.getName(), resource.getID(), foodType);
	}

	/**
	 * Populates the food list with all food.
	 */
	private static void populateFoodList() {
		foodList = ResourceUtil.getAmountResources().stream()
			.filter(ar -> ar.isEdible())
			.map(FoodUtil::createFoodResource)
			.sorted()
			.toList();

	}
}
