/**
 * Mars Simulation Project
 * FoodUtil.java
 * @version 3.1.2 2020-09-02
 * @author Manny Kung
 */
package org.mars_sim.msp.core.foodProduction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

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
			foodList = new ArrayList<Food>();
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
	 * Create the food instance from the amount resource
	 * 
	 * @param resource
	 * @return
	 */
	public static Food createFoodResource(AmountResource resource) {
		if (resource == null) {
			throw new IllegalArgumentException("Resource cannot be null");
		}
		
		FoodType foodType = null;
		String type = resource.getType();

		if (type.equalsIgnoreCase(FoodType.ANIMAL.getName()))
			foodType = FoodType.ANIMAL;
		else if (type.equalsIgnoreCase(FoodType.CHEMICAL.getName()))
			foodType = FoodType.CHEMICAL;
		else if (type.equalsIgnoreCase(FoodType.CROP.getName()))
			foodType = FoodType.CROP;
		else if (type.equalsIgnoreCase(FoodType.DERIVED.getName()))
			foodType = FoodType.DERIVED;
		else if (type.equalsIgnoreCase(FoodType.INSECT.getName()))
			foodType = FoodType.INSECT;
		else if (type.equalsIgnoreCase(FoodType.OIL.getName()))
			foodType = FoodType.OIL;
		else if (type.equalsIgnoreCase(FoodType.ORGANISM.getName()))
			foodType = FoodType.ORGANISM;	
		else if (type.equalsIgnoreCase(FoodType.SOY_BASED.getName()))
			foodType = FoodType.SOY_BASED;
		else if (type.equalsIgnoreCase(FoodType.TISSUE.getName()))
			foodType = FoodType.TISSUE;
		
//		System.out.println(resource.getName() + " : " + type + ", " + foodType + ", " + foodType.getName());
		
		return new Food(resource.getName(), resource, foodType);
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
//		String type = null;
		AmountResource ar = null;
		boolean edible = false;
		Iterator<AmountResource> i = ResourceUtil.getAmountResources().iterator();
		while (i.hasNext()) {
			ar = i.next();
//			type = ar.getType();
			edible = ar.isEdible();
			if (edible) {
				Food food = createFoodResource(ar);
				if (food != null)
					foodList.add(food);
			}
		}
	}

	/**
	 * Gets the mass per item for a food.
	 * 
	 * @param food the food to check.
	 * @return mass (kg) per item (or 1kg for amount resources).
	 * @throws Exception if error getting mass per item.
	 */
	public static double getFoodMassPerItem(Food food) {
//		if (FoodType.AMOUNT_RESOURCE == food.getCategory())
			return 1D;
	}

}
