/**
 * Mars Simulation Project
 * Favorite.java
 * @version 3.1.0 2017-03-03
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person;

import java.io.Serializable;
import java.util.List;

import org.mars_sim.msp.core.structure.building.function.cooking.HotMeal;
import org.mars_sim.msp.core.structure.building.function.cooking.MealConfig;
import org.mars_sim.msp.core.structure.building.function.cooking.PreparingDessert;
import org.mars_sim.msp.core.tool.RandomUtil;

public class Favorite implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

	private String favoriteMainDish;
	private String favoriteSideDish;
	private String favoriteDessert;
	private FavoriteType favoriteType;

	private static List<HotMeal> mealConfigMealList;
	
	private static String[] availableDesserts;

	public Favorite(Person person) {
//    	MealConfig mealConfig = SimulationConfig.instance().getMealConfiguration();
        mealConfigMealList = MealConfig.getMealList();
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

	public String[] getRandomDishes() {
		String main = "";
    	int rand0 = RandomUtil.getRandomInt(mealConfigMealList.size()-1);
    	main = mealConfigMealList.get(rand0).getMealName();

		String side = main;
		while (side.equals(main)) {
			int rand1 = RandomUtil.getRandomInt(mealConfigMealList.size()-1);
			side = mealConfigMealList.get(rand1).getMealName();
		}

		return new String[]{main, side};
	}

	public String getRandomDessert() {
		String result = "";
    	int rand = RandomUtil.getRandomInt(availableDesserts.length - 1);
    	result = availableDesserts[rand];
		return result;
	}

	public FavoriteType getAFavoriteType() {
    	int num = RandomUtil.getRandomInt(FavoriteType.availableFavoriteTypes.length - 1);
		return FavoriteType.availableFavoriteTypes[num];
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
		favoriteDessert = name;
	}

	public void setFavoriteActivity(FavoriteType name) {
		favoriteType = name;
	}
}
