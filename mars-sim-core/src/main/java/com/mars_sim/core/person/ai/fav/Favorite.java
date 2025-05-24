/*
 * Mars Simulation Project
 * Favorite.java
 * @date 2022-08-01
 * @author Manny Kung
 */

package com.mars_sim.core.person.ai.fav;

import java.io.Serializable;
import java.util.logging.Logger;

import com.mars_sim.core.building.function.cooking.HotMeal;
import com.mars_sim.core.building.function.cooking.MealConfig;
import com.mars_sim.core.building.function.cooking.PreparingDessert;
import com.mars_sim.core.building.function.cooking.MealConfig.DishCategory;
import com.mars_sim.core.tool.RandomUtil;

public class Favorite implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    /** default logger. */
	private static final Logger logger = Logger.getLogger(Favorite.class.getName());
	
	private String favoriteMainDish;
	private String favoriteSideDish;
	private String favoriteDessert;
	private FavoriteType favoriteType;

	private static String[] availableDesserts;
	
	private transient HotMeal mainMeal;
	private transient HotMeal sideMeal;
	
	public Favorite(MealConfig meals) {
		
        availableDesserts = PreparingDessert.getArrayOfDesserts();
        
    	favoriteType = determineRandomFavoriteType();
        favoriteMainDish = RandomUtil.getRandomElement(meals.getDishList(DishCategory.MAIN)).getMealName();
    	favoriteSideDish = RandomUtil.getRandomElement(meals.getDishList(DishCategory.SIDE)).getMealName();
    	favoriteDessert = determineRandomDessert();
	}

	public HotMeal getMainDishHotMeal() {
		return mainMeal;
	}
	
	public HotMeal getSideDishHotMeal() {
		return sideMeal;
	}

	/**
	 * Determines a dessert randomly.
	 * 
	 * @return
	 */
	private String determineRandomDessert() {
		String result = "";
    	int rand = RandomUtil.getRandomInt(availableDesserts.length - 1);
    	result = availableDesserts[rand];
		return result;
	}

	/**
	 * Determines a favorite type randomly.
	 * 
	 * @return
	 */
	public FavoriteType determineRandomFavoriteType() {
    	int num = RandomUtil.getRandomInt(FavoriteType.availableFavoriteTypes.length - 1);
		return FavoriteType.availableFavoriteTypes[num];
	}
	
	public boolean isDessert(String name) {
		if (name != null) {
	    	for (String s : availableDesserts) {
	    		if (name.equalsIgnoreCase(s)) {
	    			return true;
	    		}
	    	}
		}
		
		return false;
	}
	
	public boolean isActivity(String name) {
		if (name != null) {
	    	for (FavoriteType f : FavoriteType.values()) {
	    		if (name.equalsIgnoreCase(f.getName())) {
	    			return true;
	    		}
	    	}
		}
		
		return false;
	}
	
	public String getFavoriteMainDish() {
		return favoriteMainDish;
	}

	public String getFavoriteSideDish() {
		return favoriteSideDish;
	}

	public String getFavoriteDessert() {
		return favoriteDessert;
	}

	public FavoriteType getFavoriteActivity() {
		return favoriteType;
	}

	public void setFavoriteMainDish(String name) {
		favoriteMainDish = name;
	}

	public void setFavoriteSideDish(String name) {
		favoriteSideDish = name;
	}

	public void setFavoriteDessert(String name) {
		if (isDessert(name))
			favoriteDessert = name;
		else
			logger.severe("The dessert '" + name + "' does not exist in mars-sim !"); 
	}

	public void setFavoriteActivityType(FavoriteType type) {
		favoriteType = type;
	}
}
