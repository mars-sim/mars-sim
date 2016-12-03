/**
 * Mars Simulation Project
 * Favorite.java
 * @version 3.08 2015-03-24
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person;

import java.io.Serializable;
import java.util.List;

import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.structure.building.function.cooking.HotMeal;
import org.mars_sim.msp.core.structure.building.function.cooking.MealConfig;
import org.mars_sim.msp.core.structure.building.function.cooking.PreparingDessert;

public class Favorite implements Serializable {
	
    /** default serial id. */
    private static final long serialVersionUID = 1L;

	private String favoriteMainDish;
	private String favoriteSideDish;
	private String favoriteDessert;
	private String favoriteActivity;
	
	private List<HotMeal> mealConfigMealList;
	private String[] availableDesserts;
	
	private String[] availableActivities = 
    	{ 	"Tending Plants",
			"Workout",		
			"Research",
			"Field Work",
			"Tinkering",
			"Lab Experimentation",
			"Cooking",
			"Construction",
			"Operations"
		};

	public Favorite(Person person) {	
    	MealConfig mealConfig = SimulationConfig.instance().getMealConfiguration();
        mealConfigMealList = mealConfig.getMealList();
        availableDesserts = PreparingDessert.getArrayOfDesserts();	    	
	}
	
	public String getRandomMainDish() {
		String result = "";
    	int num = RandomUtil.getRandomInt(mealConfigMealList.size()-1);
		result = mealConfigMealList.get(num).getMealName();
		return result;
	}

	
	public String getRandomSideDish() {
		String result = "";
    	int num = RandomUtil.getRandomInt(mealConfigMealList.size()-1);
		result = mealConfigMealList.get(num).getMealName();		
		return result;
	}

	public String getRandomDessert() {
		String result = "";		 
    	int rand = RandomUtil.getRandomInt(availableDesserts.length - 1); 
    	result = availableDesserts[rand];		
		return result;
	}

	public String getRandomActivity() {
		String result = "";		 
    	int num = RandomUtil.getRandomInt(availableActivities.length - 1); 
		result = availableActivities[num];		
		return result;
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
	
	public String getFavoriteActivity() {
		return favoriteActivity;
	}
	
	public void setFavoriteMainDish(String name) {
		favoriteMainDish = name;
	}
	
	public void setFavoriteSideDish(String name) {
		favoriteSideDish = name;
	}
	
	public void setFavoriteDessert(String name) {
		favoriteDessert = name;
	}	
	
	public void setFavoriteActivity(String name) {
		favoriteActivity = name;
	}		
	
	
}


