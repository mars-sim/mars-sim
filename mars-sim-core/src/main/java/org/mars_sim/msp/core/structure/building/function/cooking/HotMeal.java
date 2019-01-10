/**
 * Mars Simulation Project
 * HotMeal.java
 * @version 3.1.0 2017-04-26
 * @author Manny Kung
 */
package org.mars_sim.msp.core.structure.building.function.cooking;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.mars_sim.msp.core.structure.building.function.cooking.Ingredient;

public class HotMeal implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private boolean isItAvailable;

	private int mealID;

	private double oil;
	private double salt;

	private String mealName;
	private String mealCategory;

	private List<Ingredient> ingredientList = new CopyOnWriteArrayList<>(); // <Ingredient>();


	public HotMeal(int mealID, String nameOfMeal, double oil, double salt, String mealCategory) {

		this.mealID = mealID;
		this.mealName = nameOfMeal;
		this.oil = oil;
		this.salt = salt;
		this.mealCategory = mealCategory;
	}


	/**
	 * Adds an ingredient
	 * 
	 * @param ingredientID
	 * @param resource
	 * @param proportion
	 */
	public void addIngredient(int ingredientID, int resource, double proportion) {
		ingredientList.add(new Ingredient(ingredientID, resource, proportion));
	}

	public void setIngredient(List<Ingredient> ingredientList, Ingredient ingredient) {
	    int ingredientIndex = ingredientList.indexOf(ingredient);
	    if (ingredientIndex != -1) {
	        ingredientList.set(ingredientIndex, ingredient);
	    }
	}

	public void addMealName(String nameOfMeal) {
		this.mealName = nameOfMeal;
	}

	public void addMeal(int mealID, String nameOfMeal, double oil, double salt,
			String mealCategory, boolean isItAvailable) {

		this.mealID = mealID;
		this.mealName = nameOfMeal;
		this.oil = oil;
		this.salt = salt;
		this.mealCategory = mealCategory;
		this.isItAvailable = isItAvailable;
	}

	//2014-12-11 Added isItAvailable
    public boolean getIsItAvailable() {
    	return isItAvailable;
    }

	public void setIngredientDryMass(int id, double ingredientDryMass) {
		Ingredient ingredient = ingredientList.get(id);
		ingredient.setDryMass(ingredientDryMass);
		ingredientList.set(id, ingredient);
	}

    public void setIsItAvailable(boolean value) {
    	isItAvailable = value;
    }

	public String getMealName() {
		return mealName;
	}

	public String toString() {
		return mealName;
	}

	public int getMealID() {
		return mealID;
	}

	public void setMealID(int id) {
		mealID = id;
	}

	public List<Ingredient> getIngredientList() {
		return ingredientList;
	}

	public double getOil() {
		return oil;
	}

	public double getSalt() {
		return salt;
	}

    public void destroy() {
    	ingredientList.clear();
        ingredientList = null;
    }

}