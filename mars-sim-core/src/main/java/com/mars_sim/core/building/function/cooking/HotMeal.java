/*
 * Mars Simulation Project
 * HotMeal.java
 * @date 2022-07-15
 * @author Manny Kung
 */
package com.mars_sim.core.building.function.cooking;

import java.io.Serializable;
import java.util.List;

public class HotMeal implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private double oil;
	private double salt;

	private String mealName;
	private String mealCategory;

	private List<Ingredient> ingredientList;

	HotMeal(String nameOfMeal, double oil, double salt, List<Ingredient> ingredients, String category) {

		this.mealName = nameOfMeal;
		this.oil = oil;
		this.salt = salt;
		this.ingredientList = ingredients;
		this.mealCategory = category;
	}

	public String getMealName() {
		return mealName;
	}

	public String getCategory() {
		return mealCategory;
	}

	@Override
	public String toString() {
		return mealName;
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
}
