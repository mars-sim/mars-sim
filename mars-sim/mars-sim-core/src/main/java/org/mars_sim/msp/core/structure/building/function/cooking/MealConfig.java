/**
 * Mars Simulation Project
 * MealConfig.java
 * @version 3.07 2014-12-06
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

	// Element names
	//private static final String MEAL_LIST = "meal-list";
	private static final String MEAL = "meal";
	
	private static final String INGREDIENT = "ingredient";
	
	private static final String MEAL_NAME = "name";
	private static final String MEAL_ID = "id";
	private static final String MEAL_CATEGORY = "category";
	
	private static final String INGREDIENT_ID = "id";
	private static final String INGREDIENT_NAME = "name";


	private static final String PROPORTION = "proportion";

	private static final String OIL = "oil";
	//private static final String EDIBLE_WATER_CONTENT = "edible-water-content";
	private static final String SALT = "salt";


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
	 * Gets a list of meal.
	 * @return list of meal
	 * @throws Exception when meal could not be parsed.
	 */
	public List<HotMeal> getMealList() {

		if (mealList == null) {
			mealList = new ArrayList<HotMeal>();

			Element root = mealDoc.getRootElement();
			@SuppressWarnings("unchecked")
			List<Element> meals = root.getChildren(MEAL);

			//Set<Integer> mealIDs = new HashSet<Integer>();
			
			for (Element meal : meals) {	

				// Get meal id.
				String sid = meal.getAttributeValue(MEAL_ID);
				int id = Integer.parseInt(sid);

				// Get name.
				String name = "";
				name = meal.getAttributeValue(MEAL_NAME);
				
				// Get oil
				String oilStr = meal.getAttributeValue(OIL);
				double oil = Double.parseDouble(oilStr);

				// Get salt
				String saltStr = meal.getAttributeValue(SALT);
				double salt = Double.parseDouble(saltStr);

				// Get meal category
				String mealCategory ="";
				mealCategory = meal.getAttributeValue(MEAL_CATEGORY);
				
				// Create meal
				HotMeal aMeal = new HotMeal(id, name, oil, salt, mealCategory);

	

				//Element root = mealDoc.getRootElement();
				List<Element> ingredients = root.getChildren(INGREDIENT);

				//Set<Integer> ingredientIDs = new HashSet<Integer>();
					
				for (Element ingredient : ingredients) {

					// Get id.
					String ingredientIdStr = ingredient.getAttributeValue(INGREDIENT_ID);
					int ingredientId = Integer.parseInt(ingredientIdStr);

					// Get name.
					String ingredientName = "";
					ingredientName = ingredient.getAttributeValue(INGREDIENT_NAME);
					
					// Get proportion 
					String proportionStr = ingredient.getAttributeValue(PROPORTION);
					double proportion = Double.parseDouble(proportionStr);
		
				
					aMeal.addIngredient(ingredientId, ingredientName, proportion);	
				}
			
				System.out.println("meal name is " + aMeal.getMealName());
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
