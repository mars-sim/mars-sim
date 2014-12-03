/**
 * Mars Simulation Project
 * HotMeal.java
 * @version 3.07 2014-12-02
 * @author Manny Kung
 */
package org.mars_sim.msp.core.structure.building.function.cooking;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.mars_sim.msp.core.structure.building.function.cooking.Ingredient;

// 2014-11-29 Created HotMeal()
public class HotMeal implements Serializable {
    
	List<Ingredient> ingredientList = new ArrayList<Ingredient>();
	String nameOfMeal;
	Cooking kitchen;
	
	
	/** default serial id. */
	private static final long serialVersionUID = 1L;

	public HotMeal(Cooking kitchen) {
		this.kitchen = kitchen;
		
	}
	public void add(String name, double amount) {
		ingredientList.add(new Ingredient(name, amount));
	}
	public void addMealName(String nameOfMeal) {
		this.nameOfMeal = nameOfMeal;
	}
	public String getMealName() {
		return nameOfMeal;
	}
	public List<Ingredient> getIngredientList() {
		return ingredientList;
	}
	
	public String getAvailableOil() {
		// pick an oil that's available
    	
    	boolean exit = false;
    	String oil = null;
    	
		while (!exit) {
			oil = kitchen.getAnOil();
			//boolean isAmountAV;
			try { // TODO: need to rewrite in case of exception
				double amount = kitchen.getFreshFoodAvailable(oil);	
				exit = true;
			} catch (Exception e) {}

		} // end of while loop
		return oil;
	}
}