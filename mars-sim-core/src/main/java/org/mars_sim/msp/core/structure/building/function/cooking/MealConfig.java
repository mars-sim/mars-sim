/**
 * Mars Simulation Project
 * MealConfig.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */
package org.mars_sim.msp.core.structure.building.function.cooking;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jdom2.Document;
import org.jdom2.Element;
import org.mars_sim.msp.core.resource.ResourceUtil;

/**
 * Provides configuration information about meal. Uses a DOM document to get the
 * information.
 */

public class MealConfig implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final String WATER_CONSUMPTION_RATE = "water-consumption-rate";
	private static final String CLEANING_AGENT_PER_SOL = "cleaning-agent-per-sol";
	private static final String VALUE = "value";

	// Element names
	private static final String MEAL_LIST = "meal-list";
	private static final String MAIN_DISH = "main-dish";
	private static final String SIDE_DISH = "side-dish";
	private static final String INGREDIENT = "ingredient";
	private static final String NAME = "name";
	private static final String ID = "id";
	private static final String MEAL_CATEGORY = "category";
	private static final String INGREDIENT_ID = "id";
	private static final String PROPORTION = "proportion";
	private static final String OIL = "oil";
	private static final String SALT = "salt";

	// water consumption rate, cleaning agent per sol
	private double[] values = new double[] { 0, 0 };

	private static List<HotMeal> mainDishList;
	private static List<HotMeal> sideDishList;
	
	/**
	 * Constructor.
	 * 
	 * @param mealDoc the meal DOM document.
	 */
	public MealConfig(Document mealDoc) {
		// Generate meal list
		createMealList(mealDoc);
		
		values[0] = getValueAsDouble(mealDoc, WATER_CONSUMPTION_RATE);
		values[1] = getValueAsDouble(mealDoc, CLEANING_AGENT_PER_SOL);
	}

	/**
	 * Gets the water consumption rate.
	 * 
	 * @return water rate (kg/meal)
	 * @throws Exception if consumption rate could not be found.
	 */
	public double getWaterConsumptionRate() {
		return values[0];
	}

	/**
	 * Gets average amount of cleaning agent per sol
	 * 
	 * @return rate (kg/sol)
	 * @throws Exception if rate could not be found.
	 */
	public double getCleaningAgentPerSol() {
		return values[1];
	}

	/*
	 * Gets the value of an element as a double
	 * 
	 * @param an element
	 * 
	 * @return a double
	 */
	private double getValueAsDouble(Document mealDoc, String child) {
		Element element = mealDoc.getRootElement().getChild(child);
		String str = element.getAttributeValue(VALUE);
		return Double.parseDouble(str);
	}

	/**
	 * Gets the all dishes list
	 * 
	 * @return a list of all dishes 
	 */
	public static List<HotMeal> getDishList() {
		return Stream.of(sideDishList, mainDishList)
				.flatMap(List<HotMeal>::stream)
				.collect(Collectors.toList());
	}
	
	/**
	 * Gets the main dish meal list
	 * 
	 * @return a list of main dish meals
	 */
	public static List<HotMeal> getMainDishList() {
		return mainDishList;
	}
	
	/**
	 * Gets the side dish meal list
	 * 
	 * @return a list of side dish meals
	 */
	public static List<HotMeal> getSideDishList() {
		return sideDishList;
	}
	
	/**
	 * Creates a list of meal.
	 * 
	 * @return list of meal
	 * @throws Exception when meal could not be parsed.
	 */
	private synchronized void createMealList(Document mealDoc) {
		if (mainDishList != null) {
			// just in case if another thread is being created
			return;
		}	
		if (sideDishList != null) {
			// just in case if another thread is being created
			return;
		}

		Element root = mealDoc.getRootElement();
		Element mealListElement = root.getChild(MEAL_LIST);
		
		
		// Main Dishes
		List<HotMeal> mainDishMeals = new ArrayList<>();
		List<Element> mainDishes = mealListElement.getChildren(MAIN_DISH);

		for (Element mainDish : mainDishes) {

			// Get meal id.
			String sid = mainDish.getAttributeValue(ID);
			int id = Integer.parseInt(sid);

			// Get name.
			String name = "";
			name = mainDish.getAttributeValue(NAME);

			// Get oil
			String oilStr = mainDish.getAttributeValue(OIL).toLowerCase();
			double oil = Double.parseDouble(oilStr);

			// Get salt
			String saltStr = mainDish.getAttributeValue(SALT).toLowerCase();
			double salt = Double.parseDouble(saltStr);

			// Get meal category
			String mealCategory = "";
			mealCategory = mainDish.getAttributeValue(MEAL_CATEGORY);

			// Create meal

			HotMeal aMeal = new HotMeal(id, name, oil, salt, mealCategory); // , isItAvailable);

			// Modify to ingredients = meal.getChildren(INGREDIENT);
			List<Element> ingredients = mainDish.getChildren(INGREDIENT);

			for (Element ingredient : ingredients) {

				// Get id.
				String ingredientIdStr = ingredient.getAttributeValue(INGREDIENT_ID);
				int ingredientId = Integer.parseInt(ingredientIdStr);

				// Get name.
				String ingredientName = "";
				ingredientName = ingredient.getAttributeValue(NAME).toLowerCase();
				int ingredientID = ResourceUtil.findIDbyAmountResourceName(ingredientName);
				// Get proportion
				String proportionStr = ingredient.getAttributeValue(PROPORTION);
				double proportion = Double.parseDouble(proportionStr);

				aMeal.addIngredient(ingredientId, ingredientID, proportion);// , isItAvailable);

			}

			mainDishMeals.add(aMeal);
		}

		mainDishList = Collections.unmodifiableList(mainDishMeals);
		
		// Side Dishes
		List<HotMeal> sideDishMeals = new ArrayList<>();
		List<Element> sideDishes = mealListElement.getChildren(SIDE_DISH);

		for (Element sideDish : sideDishes) {

			// Get meal id.
			String sid = sideDish.getAttributeValue(ID);
			int id = Integer.parseInt(sid);

			// Get name.
			String name = "";
			name = sideDish.getAttributeValue(NAME);

			// Get oil
			String oilStr = sideDish.getAttributeValue(OIL).toLowerCase();
			double oil = Double.parseDouble(oilStr);

			// Get salt
			String saltStr = sideDish.getAttributeValue(SALT).toLowerCase();
			double salt = Double.parseDouble(saltStr);

			// Get meal category
			String mealCategory = "";
			mealCategory = sideDish.getAttributeValue(MEAL_CATEGORY);

			// Create meal

			HotMeal aMeal = new HotMeal(id, name, oil, salt, mealCategory); // , isItAvailable);

			// Modify to ingredients = meal.getChildren(INGREDIENT);
			List<Element> ingredients = sideDish.getChildren(INGREDIENT);

			for (Element ingredient : ingredients) {

				// Get id.
				String ingredientIdStr = ingredient.getAttributeValue(INGREDIENT_ID);
				int ingredientId = Integer.parseInt(ingredientIdStr);

				// Get name.
				String ingredientName = "";
				ingredientName = ingredient.getAttributeValue(NAME).toLowerCase();
				int ingredientID = ResourceUtil.findIDbyAmountResourceName(ingredientName);
				// Get proportion
				String proportionStr = ingredient.getAttributeValue(PROPORTION);
				double proportion = Double.parseDouble(proportionStr);

				aMeal.addIngredient(ingredientId, ingredientID, proportion);// , isItAvailable);

			}

			sideDishMeals.add(aMeal);
		}
		
		sideDishList = Collections.unmodifiableList(sideDishMeals);
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		if (mainDishList != null) {
			mainDishList = null;
		}
		if (sideDishList != null) {
			sideDishList = null;
		}
	}
}
