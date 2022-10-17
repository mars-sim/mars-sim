/*
 * Mars Simulation Project
 * FoodUtil.java
 * @date 2022-06-25
 * @author Manny Kung
 */
package org.mars_sim.msp.core.food;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.goods.GoodType;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ResourceUtil;

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
			foodList = new ArrayList<>();
			populateFoodList();
		}

		return Collections.unmodifiableList(foodList);
	}

	/**
	 * Destroys the current food list.
	 */
	public static void destroyFoodList() {

		if (foodList != null) {
			foodList.clear();
		}

		foodList = null;
	}

	/**
	 * Creates the food instance from the amount resource.
	 * 
	 * @param resource
	 * @return
	 */
	public static Food createFoodResource(AmountResource resource) {
		if (resource == null) {
			throw new IllegalArgumentException("Resource cannot be null");
		}
		
		GoodType type = resource.getGoodType();

		// There is a direct mapping between the Foodtype enums to the equivalent
		// GoodType enum
		FoodType foodType = FoodType.valueOf(type.name());
		return new Food(resource.getName(), resource, foodType, resource.getDemand());
	}

	/**
	 * Checks if a food is valid in the simulation.
	 * 
	 * @param food the food to check.
	 * @return true if food is valid.
	 */
	public static boolean containsFood(Food food) {
		if (food == null) {
			throw new IllegalArgumentException("food cannot be null.");
		}
		return getFoodList().contains(food);
	}

	/**
	 * Populates the food list with all food.
	 */
	private static void populateFoodList() {
		// Populate amount resources.
		populateAmountResources();

		// Sort food by name.
		Collections.sort(foodList);
	}

	/**
	 * Populates the food list with all amount resources.
	 */
	private static void populateAmountResources() {
		AmountResource ar = null;
		Iterator<AmountResource> i = ResourceUtil.getAmountResources().iterator();
		while (i.hasNext()) {
			ar = i.next();
			if (ar.isEdible()) {
				foodList.add(createFoodResource(ar));
			}
		}
	}
}
