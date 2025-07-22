/*
 * Mars Simulation Project
 * DishRecipe.java
 * @date 2025-05-26
 * @author Barry Evans
 */
package com.mars_sim.core.building.function.cooking;

import java.io.Serializable;
import java.util.List;

import com.mars_sim.core.structure.Settlement;

/**
 * Represents a recipe for creating a Dish.
 */
public class DishRecipe implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private double oil;
	private double salt;

	private String name;
	private DishCategory category;

	private List<Ingredient> ingredientList;

	DishRecipe(String nameOfMeal, double oil, double salt, List<Ingredient> ingredients, DishCategory category) {

		this.name = nameOfMeal;
		this.oil = oil;
		this.salt = salt;
		this.ingredientList = ingredients;
		this.category = category;
	}

	public String getName() {
		return name;
	}

	public DishCategory getCategory() {
		return category;
	}

	@Override
	public String toString() {
		return name;
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

	/**
	 * Checks if all ingredients are available for a particular Settlement.
	 *
	 */
	public boolean isIngredientsAvailable(Settlement settlement) {
		return ingredientList.stream().filter(i -> i.isMandatory()) 
				.allMatch(i -> settlement.getSpecificAmountResourceStored(i.getAmountResourceID()) >= i.getDryMass());
	}

	/***
	 * Converts a numeral quality to letter grade for a meal
	 * 
	 * @param quality
	 * @return grade
	 */
	public static String qualityToString(double quality) {
		int grade = Math.max(-4, (int) quality); // grade cannot be less than -4
	
		return switch (grade) {
			case -4 -> "C-";
			case -3 -> "C";
			case -2 -> "C+";
			case -1 -> "B-";
			case 0 -> "B";
			case 1 -> "B+";
			case 2 -> "A-";
			case 3 -> "A";
			default -> "A+";
		};
	}

	/**
	 * Retrieve the required ingredients from the settlement.
	 * @param s
	 * @return the qualtity of the ingriendent retrieved.
	 */
    public double retrieveIngredients(Settlement s) {
		double quality = 0;

		for (var i : ingredientList) {
			int ingredientID = i.getAmountResourceID();

			double dryMass = i.getDryMass();

			var shortfall = s.retrieveAmountResource(ingredientID, dryMass);

			// Add the effect of the presence of ingredients on meal quality
			if (shortfall == 0) {
				quality += .1;
			}

			else if (i.isMandatory()) {
				return 0;
			}
			// if optional ingredients are NOT presented, there's a penalty to the meal quality
			else {
				quality -= i.getImpact();
			}
		}

		return quality;
    }
}
