/*
 * Mars Simulation Project
 * MealConfig.java
 * @date 2022-08-30
 * @author Manny Kung
 */
package com.mars_sim.core.building.function.cooking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jdom2.Document;
import org.jdom2.Element;

import com.mars_sim.core.building.function.farming.CropConfig;
import com.mars_sim.core.configuration.ConfigHelper;
import com.mars_sim.core.person.PersonConfig;
import com.mars_sim.core.resource.ResourceUtil;

/**
 * Provides configuration information about meal. Uses a DOM document to get the
 * information.
 */
public class MealConfig {

	public static final String SIDE_DISH = "Side Dish";
	public static final String MAIN_DISH = "Main Dish";

	private static final String WATER_CONSUMPTION_RATE = "water-consumption-rate";
	private static final String CLEANING_AGENT_PER_SOL = "cleaning-agent-per-sol";
	private static final String NUMBER_OF_MEAL_PER_SOL = "meals-per-sol";


	// Element names
	private static final String DISH = "dish";
	private static final String MAIN_LIST = "main-list";
	private static final String SIDE_LIST = "side-list";
	private static final String INGREDIENT = "ingredient";
	private static final String NAME = "name";
	private static final String PROPORTION = "proportion";
	private static final String OIL = "oil";
	private static final String SALT = "salt";

	private double cleaningAgentPerSol;
	private double waterConsumptionRate;
	private double dryMassPerServing;

	private List<HotMeal> mainDishList;
	private List<HotMeal> sideDishList;
	

	/**
	 * Constructor.
	 * 
	 * @param mealDoc the meal DOM document.
	 */
	public MealConfig(Document mealDoc, CropConfig cropConfig, PersonConfig personConfig) {
		var root = mealDoc.getRootElement();

		var globals = root.getChild("global-settings");
		waterConsumptionRate = ConfigHelper.getAttributeDouble(globals, WATER_CONSUMPTION_RATE);
		cleaningAgentPerSol = ConfigHelper.getAttributeDouble(globals, CLEANING_AGENT_PER_SOL);
		var mealsPerSol = ConfigHelper.getAttributeInt(globals, NUMBER_OF_MEAL_PER_SOL);
		dryMassPerServing = personConfig.getFoodConsumptionRate() / mealsPerSol;

		// Generate meal list
		mainDishList = parseMealList(root.getChild(MAIN_LIST), MAIN_DISH, cropConfig);
		sideDishList = parseMealList(root.getChild(SIDE_LIST), SIDE_DISH, cropConfig);
	}

	/**
	 * Gets the dryMass per serving
	 * 
	 */
	public double getDryMassPerServing() {
		return dryMassPerServing;
	}

	/**
	 * Gets the water consumption rate.
	 * 
	 * @return water rate (kg/meal)
	 * @throws Exception if consumption rate could not be found.
	 */
	public double getWaterConsumptionRate() {
		return waterConsumptionRate;
	}

	/**
	 * Gets average amount of cleaning agent per sol.
	 * 
	 * @return rate (kg/sol)
	 * @throws Exception if rate could not be found.
	 */
	public double getCleaningAgentPerSol() {
		return cleaningAgentPerSol;
	}

	/**
	 * Gets the all dishes list.
	 * 
	 * @return a list of all dishes 
	 */
	public List<HotMeal> getDishList() {
		return Stream.of(sideDishList, mainDishList)
				.flatMap(List<HotMeal>::stream)
				.collect(Collectors.toList());
	}
	
	/**
	 * Gets the main dish meal list.
	 * 
	 * @return a list of main dish meals
	 */
	public List<HotMeal> getMainDishList() {
		return mainDishList;
	}
	
	/**
	 * Gets the side dish meal list.
	 * 
	 * @return a list of side dish meals
	 */
	public List<HotMeal> getSideDishList() {
		return sideDishList;
	}
	
	/**
	 * Creates a list of meal.
	 * 
	 * @return list of meal
	 */
	private List<HotMeal> parseMealList(Element dishList, String cat, CropConfig crops) {
		
		// Main Dishes
		List<HotMeal> mainDishMeals = new ArrayList<>();

		for (Element mainDish : dishList.getChildren(DISH)) {
			var meal = parseHotMeal(mainDish, cat, crops);
			mainDishMeals.add(meal);
		}

		return Collections.unmodifiableList(mainDishMeals);
	}

	private HotMeal parseHotMeal(Element mealNode, String category, CropConfig crops) {
		
		String name = mealNode.getAttributeValue(NAME);
		double oil = ConfigHelper.getAttributeDouble(mealNode, OIL);
		double salt = ConfigHelper.getAttributeDouble(mealNode, SALT);

		List<Ingredient> ingredients = new ArrayList<>();
		for (Element ingredient : mealNode.getChildren(INGREDIENT)) {

			// Get name.
			String ingredientName = ingredient.getAttributeValue(NAME).toLowerCase();
			int ingredientID = ResourceUtil.findIDbyAmountResourceName(ingredientName);
			double proportion = ConfigHelper.getAttributeDouble(ingredient, PROPORTION);

			ingredients.add(new Ingredient(ingredients.size(), ingredientID, proportion));

		}

		computeDryMass(ingredients, crops);
		return new HotMeal(name, oil, salt, ingredients, category);
	}

	/**
	 * Computes the dry mass of all ingredients
	 */
	private void computeDryMass(List<Ingredient> ingredients, CropConfig crops) {

		List<Double> proportionList = new ArrayList<>(); 
		List<Double> waterContentList = new ArrayList<>(); 

		for(Ingredient oneIngredient : ingredients) {
			String ingredientName = oneIngredient.getName();
			double proportion = oneIngredient.getProportion();
			proportionList.add(proportion);

			// get totalDryMass
			double waterContent = 0;
			var c = crops.getCropTypeByName(ingredientName);
			if (c != null)
				waterContent = c.getEdibleWaterContent();
			waterContentList.add(waterContent);
		}

		// get total dry weight (sum of each ingredient's dry weight) for a meal
		double totalDryMass = 0;
		int k;
		for (k = 1; k < waterContentList.size(); k++)
			totalDryMass += waterContentList.get(k) + proportionList.get(k);

		// get this fractional number
		double fraction = 0;
		if (totalDryMass > 0) {
			fraction = dryMassPerServing / totalDryMass;
		}

		// get ingredientDryMass for each ingredient
		double ingredientDryMass = 0;
		int l;
		for (l = 0; l < proportionList.size(); l++) {
			ingredientDryMass = fraction * waterContentList.get(l) + proportionList.get(l);
			ingredients.get(l).setDrymass(ingredientDryMass);
		}
	}
	
	/**
	 * Gets an instance of the hot meal in main and side dish list.
	 * 
	 * @param dishList
	 * @param dish
	 * @return
	 */
	public HotMeal getHotMeal(String dish) {
		for (HotMeal hm : getDishList()) {
			if (hm.getMealName().equalsIgnoreCase(dish))
				return hm;
		}
		return null;
	}
}
