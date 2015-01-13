/**
 * Mars Simulation Project
 * HotMeal.java
 * @version 3.07 2014-12-12
 * @author Manny Kung
 */
package org.mars_sim.msp.core.structure.building.function.cooking;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


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
	//2014-12-12 Added isItAvailable, ingredientDryWeight
	private boolean isItAvailable;
	private double ingredientDryMass;
	
	public HotMeal(Cooking kitchen) {
		this.kitchen = kitchen;
		//setUpMeal(); 
	}
	
	public HotMeal(int mealID, String nameOfMeal, double oil, double salt, 
			String mealCategory) { //, boolean isItAvailable) {
		
		this.mealID = mealID;
		this.mealName = nameOfMeal;
		this.oil = oil;
		this.salt = salt;
		this.mealCategory = mealCategory;
	}
	

	// called by constructor in Cooking.java
	public void addIngredient(int ingredientID, String name, double proportion) {//, boolean isItAvailable) {
		ingredientList.add(new Ingredient(ingredientID, name, proportion));//, isItAvailable));
		//System.out.println("ingredientList is " +	ingredientList);
	}
	
	//2014-12-11 Added setIngredient
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
	
    public void destroy() {
    	ingredientList.clear();
        ingredientList = null;
        kitchen = null;
        
    }

 	
}