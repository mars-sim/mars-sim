/**
 * Mars Simulation Project
 * HotMeal.java
 * @version 3.07 2014-12-07
 * @author Manny Kung
 */
package org.mars_sim.msp.core.structure.building.function.cooking;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.mars_sim.msp.core.structure.building.function.cooking.Ingredient;

// 2014-11-29 Created HotMeal()
public class HotMeal implements Serializable {
    
	/** default serial id. */
	private static final long serialVersionUID = 1L;


	private List<Ingredient> ingredientList = new ArrayList<Ingredient>();
	private String mealName;
	private int mealID;
	private double oil;
	private double salt;
	private String mealCategory;
	
	private int ingredientID;
	private String ingredientName;
	private double proportion;
	
	private Cooking kitchen;

	public HotMeal(Cooking kitchen) {
		this.kitchen = kitchen;
		//setUpMeal(); 
	}
	
	public HotMeal(int mealID, String nameOfMeal, double oil, double salt, 
			String mealCategory) {
		
		this.mealID = mealID;
		this.mealName = nameOfMeal;
		this.oil = oil;
		this.salt = salt;
		this.mealCategory = mealCategory;
	}
	
	
	public HotMeal() {
		
	}
	
	public HotMeal(int mealID, String mealName) {	
		prepareMeal(mealID, mealName);
	}
	
	public HotMeal(HotMeal meal) {
		ingredientList = meal.ingredientList;
		mealName = meal.mealName;
		mealID = meal.mealID;
	}

	public void prepareMeal(int id, String name) {
		this.mealID = id;
		this.mealName = name;
	}
	
	// called by constructor in Cooking.java
	public void addIngredient(int ingredientID, String name, double proportion) {
		ingredientList.add(new Ingredient(ingredientID, name, proportion));
		
	}
	public void addMealName(String nameOfMeal) {
		this.mealName = nameOfMeal;
	}
	public void addMeal(int mealID, String nameOfMeal, double oil, double salt, 
			String mealCategory) {
		
		this.mealID = mealID;
		this.mealName = nameOfMeal;
		this.oil = oil;
		this.salt = salt;
		this.mealCategory = mealCategory;
	}
	
	
	public String getMealName() {
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

	
	//public HotMeal getMeal(int mealID) {	
		//return new HotMeal(mealID);	
	//}

    public void destroy() {
    	ingredientList.clear();
        ingredientList = null;
    }

 	
}