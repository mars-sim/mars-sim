/*
 * Mars Simulation Project
 * Favorite.java
 * @date 2022-08-01
 * @author Manny Kung
 */

package com.mars_sim.core.person.ai.fav;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.mars_sim.core.building.function.cooking.DishCategory;
import com.mars_sim.core.building.function.cooking.MealConfig;
import com.mars_sim.core.tool.RandomUtil;

public class Favorite implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
	
	private Set<String> favoriteDishes = new HashSet<>();
	private FavoriteType favoriteType;


	public Favorite(MealConfig meals) {
		        
    	favoriteType = determineRandomFavoriteType();
        favoriteDishes.add(RandomUtil.getRandomElement(meals.getDishList(DishCategory.MAIN)).getName());
		favoriteDishes.add(RandomUtil.getRandomElement(meals.getDishList(DishCategory.SIDE)).getName());
        favoriteDishes.add(RandomUtil.getRandomElement(meals.getDishList(DishCategory.DESSERT)).getName());
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
	

	public Set<String> getFavoriteDishes() {
		return favoriteDishes;
	}

	public FavoriteType getFavoriteActivity() {
		return favoriteType;
	}

	public void setFavoriteDishes(Set<String> favs) {
		favoriteDishes.clear();
		favoriteDishes.addAll(favs);
	}

	public void setFavoriteActivityType(FavoriteType type) {
		favoriteType = type;
	}
}
