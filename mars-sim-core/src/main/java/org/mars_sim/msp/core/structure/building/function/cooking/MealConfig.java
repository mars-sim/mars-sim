/**
 * Mars Simulation Project
 * MealConfig.java
 * @version 3.07 2015-02-27
 * @author Manny Kung
 */
package org.mars_sim.msp.core.structure.building.function.cooking;

import java.io.Serializable;
import java.util.ArrayList;
//import java.util.HashSet;
import java.util.List;
//import java.util.Set;




import org.jdom.Document;
import org.jdom.Element;

/**
 * Provides configuration information about meal.
 * Uses a DOM document to get the information.
 */

public class MealConfig
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	
	private static final String WATER_CONSUMPTION_RATE = "water-consumption-rate";
	private static final String CLEANING_AGENT_PER_SOL = "cleaning-agent-per-sol";
	private static final String VALUE= "value";
	
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
	//private static final String EDIBLE_WATER_CONTENT = "edible-water-content";
	private static final String SALT = "salt";
	//2014-12-11 Added isItAvailable
	//private boolean isItAvailable = false;

	private Document mealDoc;
	private List<HotMeal> mealList;
	//private HotMeal meal;
	//private List<Ingredient> ingredientList;
	//private Ingredient ingredient;

	/**
	 * Constructor.
	 * @param mealDoc the meal DOM document.
	 */
	public MealConfig(Document mealDoc) {
		this.mealDoc = mealDoc;
	}

	
	/**
	 * Gets the water consumption rate.
	 * @return water rate (kg/meal)
	 * @throws Exception if consumption rate could not be found.
	 */
	// 2016-05-31 Added getWaterConsumptionRate()
	public double getWaterConsumptionRate() {
		return getValueAsDouble(WATER_CONSUMPTION_RATE);
	}

	
	/**
	 * Gets average amount of cleaning agent per sol
	 * @return rate (kg/sol)
	 * @throws Exception if rate could not be found.
	 */
	// 2016-05-31 Added getCleaningAgentPerSol()
	public double getCleaningAgentPerSol() {
		return getValueAsDouble(CLEANING_AGENT_PER_SOL);
	}

	

	/*
	 * Gets the value of an element as a double
	 * @param an element
	 * @return a double 
	 */
	// 2016-05-31 Added getValueAsDouble()
	private double getValueAsDouble(String child) {
		Element root = mealDoc.getRootElement();
		Element element = root.getChild(child);
		String str = element.getAttributeValue(VALUE);
		return Double.parseDouble(str);
	}
	
	
	/**
	 * Gets a list of meal.
	 * @return list of meal
	 * @throws Exception when meal could not be parsed.
	 */
	public List<HotMeal> getMealList() {
		//System.out.println("calling getMealList()");
		if (mealList == null) {
			mealList = new ArrayList<HotMeal>();
		
			Element root = mealDoc.getRootElement();
			Element mealListElement = root.getChild(MEAL_LIST);
			List<Element> mainDishes = mealListElement.getChildren(MAIN_DISH);

			//Set<Integer> mealIDs = new HashSet<Integer>();

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
				String mealCategory ="";
				mealCategory = mainDish.getAttributeValue(MEAL_CATEGORY);

				// Create meal

				HotMeal aMeal = new HotMeal(id, name, oil, salt, mealCategory); //, isItAvailable);
	    		//System.out.println("MealConfig.java : aMeal is " + aMeal);

				//2014-12-11 Modified to ingredients = meal.getChildren(INGREDIENT);
				List<Element> ingredients = mainDish.getChildren(INGREDIENT);
	    		//System.out.println("MealConfig.java : ingredients is " + ingredients);

				for (Element ingredient : ingredients) {

					// Get id.
					String ingredientIdStr = ingredient.getAttributeValue(INGREDIENT_ID);
					int ingredientId = Integer.parseInt(ingredientIdStr);
					//System.out.println(" ingredientId is " + ingredientId);

					// Get name.
					String ingredientName = "";
					ingredientName = ingredient.getAttributeValue(INGREDIENT_NAME).toLowerCase();

					// Get proportion
					String proportionStr = ingredient.getAttributeValue(PROPORTION);
					double proportion = Double.parseDouble(proportionStr);

					//2014-12-11 Added isItAvailable
					aMeal.addIngredient(ingredientId, ingredientName, proportion);//, isItAvailable);

					//System.out.println("proportion is "+ proportion);
		    		//System.out.println("MealConfig.java : aMeal.getIngredientList() is " + aMeal.getIngredientList());

				}

				//System.out.println("meal name is " + aMeal.getMealName());
				mealList.add(aMeal);
			}

		}
		//logger.info("");
		//System.out.println("mealList size : " + mealList.size());
		return mealList;
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		mealDoc = null;
		if(mealList != null){
			mealList.clear();
			mealList = null;
		}
	}
}
