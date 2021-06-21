/**
 * Mars Simulation Project
 * Favorite.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;

import org.mars_sim.msp.core.structure.building.function.cooking.HotMeal;
import org.mars_sim.msp.core.structure.building.function.cooking.MealConfig;
import org.mars_sim.msp.core.structure.building.function.cooking.PreparingDessert;
import org.mars_sim.msp.core.tool.RandomUtil;

public class Favorite implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(Favorite.class.getName());
    
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
        
        favoriteMainDish = getRandomMainDish();
    	favoriteSideDish = getRandomSideDish();
    	favoriteDessert = getRandomDessert();
    	favoriteType = getRandomFavoriteType();
    	
        setFavoriteMainDish(favoriteMainDish);
		setFavoriteSideDish(favoriteSideDish);
		setFavoriteDessert(favoriteDessert);
		setFavoriteActivityType(favoriteType);
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

	public FavoriteType getRandomFavoriteType() {
    	int num = RandomUtil.getRandomInt(FavoriteType.availableFavoriteTypes.length - 1);
		return FavoriteType.availableFavoriteTypes[num];
	}

	public boolean isMainDish(String name) {
		if (name != null) {
	    	for (HotMeal hm : mealConfigMealList) {
	    		if (name.equalsIgnoreCase(hm.getMealName())) {
	    			return true;
	    		}
	    	}
		}
		
		return false;
	}
	
	public boolean isSideDish(String name) {
		if (name != null) {
	    	for (HotMeal hm : mealConfigMealList) {
	    		if (name.equalsIgnoreCase(hm.getMealName())) {
	    			return true;
	    		}
	    	}
		}
		
		return false;
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
		if (isMainDish(name))
			favoriteMainDish = name;
		else
			logger.severe("The main dish '" + name + "' does not exist in mars-sim !"); 
	}

	public void setFavoriteSideDish(String name) {
		if (isSideDish(name))
			favoriteSideDish = name;
		else
			logger.severe("The side dish '" + name + "' does not exist in mars-sim !"); 
	}

	public void setFavoriteDessert(String name) {
		if (isDessert(name))
			favoriteDessert = name;
		else
			logger.severe("The dessert '" + name + "' does not exist in mars-sim !"); 
	}

	public void setFavoriteActivity(String type) {
		favoriteType = FavoriteType.fromString(type);
		if (favoriteType == null)
			logger.severe("The activity '" + type + "' does not exist in mars-sim !"); 
	}
	
	public void setFavoriteActivityType(FavoriteType type) {
		favoriteType = type;
	}
	
	public void destroy() {
		favoriteType = null;
		mealConfigMealList.clear();
		mealConfigMealList = null;
		availableDesserts = null;
	}
}
