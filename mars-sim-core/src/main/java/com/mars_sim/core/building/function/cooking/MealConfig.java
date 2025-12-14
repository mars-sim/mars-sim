/*
 * Mars Simulation Project
 * MealConfig.java
 * @date 2022-08-30
 * @author Manny Kung
 */
package com.mars_sim.core.building.function.cooking;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;

import com.mars_sim.core.building.function.farming.CropConfig;
import com.mars_sim.core.configuration.ConfigHelper;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.PersonConfig;
import com.mars_sim.core.resource.ResourceUtil;

/**
 * Provides configuration information about meal. Uses a DOM document to get the
 * information.
 */
public class MealConfig {

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(MealConfig.class.getName());

	private static final String WATER_CONSUMPTION_RATE = "water-consumption-rate";
	private static final String CLEANING_AGENT_PER_SOL = "cleaning-agent-per-sol";
	private static final String NUMBER_OF_MEAL_PER_SOL = "meals-per-sol";


	// Element names
	private static final String DISH = "dish";
	private static final String INGREDIENT = "ingredient";
	private static final String NAME = "name";
	private static final String PROPORTION = "proportion";
	private static final String OIL = "oil";
	private static final String SALT = "salt";

	private double cleaningAgentPerSol;
	private double waterConsumptionRate;
	private double dryMassPerServing;

	private List<DishRecipe> dishList;

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

		// Generate dish list
		dishList = new ArrayList<>();
		dishList.addAll(parseDishList(root, DishCategory.MAIN, cropConfig));
		int mainDish = dishList.size();
		dishList.addAll(parseDishList(root, DishCategory.SIDE, cropConfig));
		int sideDish = dishList.size() - mainDish;
		dishList.addAll(parseDishList(root, DishCategory.DESSERT, cropConfig));
		int dessert = dishList.size() - mainDish - sideDish;
		logger.info("main dishes: " + mainDish + "  side dishes: " + sideDish + "   desserts: " + dessert);
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
	public List<DishRecipe> getDishList() {
		return dishList;
	}
	
	/**
	 * Gets the dish list for a specific category.
	 * 
	 * @return a list of  dish meals
	 */
	public List<DishRecipe> getDishList(DishCategory category) {
		return dishList.stream()
				.filter(dish -> dish.getCategory() == category)
				.toList();
	}

	
	/**
	 * Creates a list of meal.
	 * 
	 * @return list of meal
	 */
	private List<DishRecipe> parseDishList(Element root, DishCategory cat, CropConfig crops) {
		
		var dishNodes = root.getChild(cat.name().toLowerCase() + "-list");

		// Main Dishes
		List<DishRecipe> dishes = new ArrayList<>();

		for (Element mainDish : dishNodes.getChildren(DISH)) {
			var meal = parseHotMeal(mainDish, cat, crops);
			dishes.add(meal);
		}

		return dishes;
	}

	private DishRecipe parseHotMeal(Element mealNode, DishCategory category, CropConfig crops) {
		
		String name = mealNode.getAttributeValue(NAME);
		double oil = ConfigHelper.getOptionalAttributeDouble(mealNode, OIL, 0D);
		double salt = ConfigHelper.getOptionalAttributeDouble(mealNode, SALT, 0D);

		List<Ingredient> ingredients = new ArrayList<>();
		for (Element ingredient : mealNode.getChildren(INGREDIENT)) {

			// Get name.
			String ingredientName = ingredient.getAttributeValue(NAME).toLowerCase();
			int ingredientID = ResourceUtil.findIDbyAmountResourceName(ingredientName);
			double proportion = ConfigHelper.getAttributeDouble(ingredient, PROPORTION);
			boolean mandatory = ingredients.size() <= 2;
			double impact = 1 - (0.25 * (ingredients.size() -2));

			ingredients.add(new Ingredient(ingredientID, proportion, mandatory, impact));	

		}

		computeDryMass(ingredients, crops);
		return new DishRecipe(name, oil, salt, ingredients, category);
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
	public DishRecipe getHotMeal(String dish) {
		for (DishRecipe hm : getDishList()) {
			if (hm.getName().equalsIgnoreCase(dish))
				return hm;
		}
		return null;
	}
}
