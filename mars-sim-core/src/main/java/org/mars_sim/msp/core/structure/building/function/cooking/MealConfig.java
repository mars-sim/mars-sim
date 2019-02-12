/**
 * Mars Simulation Project
 * MealConfig.java
 * @version 3.1.0 2018-11-14
 * @author Manny Kung
 */
package org.mars_sim.msp.core.structure.building.function.cooking;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
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
	private static final String INGREDIENT = "ingredient";
	private static final String MAIN_DISH_NAME = "name";
	private static final String MAIN_DISH_ID = "id";
	private static final String MEAL_CATEGORY = "category";
	private static final String INGREDIENT_ID = "id";
	private static final String INGREDIENT_NAME = "name";
	private static final String PROPORTION = "proportion";
	private static final String OIL = "oil";
	private static final String SALT = "salt";

	// water consumption rate, cleaning agent per sol
	private double[] values = new double[] { 0, 0 };

//	private List<HotMeal> mainDishes;
//	private List<HotMeal> sideDishes;

	private static Document mealDoc;

	private static List<HotMeal> mealList;

	/**
	 * Constructor.
	 * 
	 * @param mealDoc the meal DOM document.
	 */
	public MealConfig(Document mealDoc) {
		this.mealDoc = mealDoc;

		// Generate meal list
		getMealList();
	}

	/**
	 * Gets the water consumption rate.
	 * 
	 * @return water rate (kg/meal)
	 * @throws Exception if consumption rate could not be found.
	 */

	public double getWaterConsumptionRate() {
		if (values[0] != 0)
			return values[0];
		else {
			values[0] = getValueAsDouble(WATER_CONSUMPTION_RATE);
			return values[0];
		}
	}

	/**
	 * Gets average amount of cleaning agent per sol
	 * 
	 * @return rate (kg/sol)
	 * @throws Exception if rate could not be found.
	 */
	public double getCleaningAgentPerSol() {
		if (values[1] != 0)
			return values[1];
		else {
			values[1] = getValueAsDouble(CLEANING_AGENT_PER_SOL);
			return values[1];
		}
	}

	/*
	 * Gets the value of an element as a double
	 * 
	 * @param an element
	 * 
	 * @return a double
	 */
	private double getValueAsDouble(String child) {
		Element root = mealDoc.getRootElement();
		Element element = root.getChild(child);
		String str = element.getAttributeValue(VALUE);
		return Double.parseDouble(str);
	}

	/**
	 * Gets a list of meal.
	 * 
	 * @return list of meal
	 * @throws Exception when meal could not be parsed.
	 */
	public static List<HotMeal> getMealList() {
		if (mealList == null) {
			mealList = new ArrayList<HotMeal>();

			Element root = mealDoc.getRootElement();
			Element mealListElement = root.getChild(MEAL_LIST);
			List<Element> mainDishes = mealListElement.getChildren(MAIN_DISH);

			for (Element mainDish : mainDishes) {

				// Get meal id.
				String sid = mainDish.getAttributeValue(MAIN_DISH_ID);
				int id = Integer.parseInt(sid);

				// Get name.
				String name = "";
				name = mainDish.getAttributeValue(MAIN_DISH_NAME);

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
					ingredientName = ingredient.getAttributeValue(INGREDIENT_NAME).toLowerCase();
					int ingredientID = ResourceUtil.findIDbyAmountResourceName(ingredientName);
					// Get proportion
					String proportionStr = ingredient.getAttributeValue(PROPORTION);
					double proportion = Double.parseDouble(proportionStr);

					aMeal.addIngredient(ingredientId, ingredientID, proportion);// , isItAvailable);

				}

				mealList.add(aMeal);
			}

		}

		return mealList;
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		mealDoc = null;
		if (mealList != null) {
			mealList.clear();
			mealList = null;
		}
	}
}
