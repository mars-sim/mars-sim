/**
 * Mars Simulation Project
 * Favorite.java
 * @version 3.08 2015-02-27
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person;

import java.io.Serializable;
import java.util.List;

import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.structure.building.function.cooking.HotMeal;
import org.mars_sim.msp.core.structure.building.function.cooking.MealConfig;

public class Favorite implements Serializable {
	
    /** default serial id. */
    private static final long serialVersionUID = 1L;

	private String favoriteMainDish;
	private String favoriteSideDish;
	
	private List<HotMeal> mealConfigMealList;

	public Favorite(Person person) {
		
    	MealConfig mealConfig = SimulationConfig.instance().getMealConfiguration();
        mealConfigMealList = mealConfig.getMealList();
        
		// randomly assign the main dish
		
		// randomly assign the main dish

        
	}
	
	public String getRandomMainDish() {
		String rand = "";
    	int num = RandomUtil.getRandomInt(mealConfigMealList.size()-1);
		rand = mealConfigMealList.get(num).getMealName();
		return rand;
	}

	
	public String getRandomSideDish() {
		String rand = "";
    	int num = RandomUtil.getRandomInt(mealConfigMealList.size()-1);
		rand = mealConfigMealList.get(num).getMealName();		
		return rand;
	}
	
	
	public String getFavoriteMainDish() {
		return favoriteMainDish;
	}
	
	public String getFavoriteSideDish() {
		return favoriteSideDish;
	}
	
	public void setFavoriteMainDish(String name) {
		favoriteMainDish = name;
	}
	
	public void setFavoriteSideDish(String name) {
		favoriteSideDish = name;
	}
	
	
	
	
	
}


