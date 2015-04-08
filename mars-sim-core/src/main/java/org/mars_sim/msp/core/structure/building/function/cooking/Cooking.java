/**
 * Mars Simulation Project
 * Cooking.java
 * @version 3.07 2015-01-28
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function.cooking;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.CookMeal;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.CropConfig;
import org.mars_sim.msp.core.structure.building.function.CropType;
import org.mars_sim.msp.core.structure.building.function.Function;
import org.mars_sim.msp.core.structure.building.function.LifeSupport;
import org.mars_sim.msp.core.structure.building.function.Storage;
import org.mars_sim.msp.core.time.MarsClock;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * The Cooking class is a building function for cooking meals.
 */
public class Cooking
extends Function
implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** default logger. */
    private static Logger logger = Logger.getLogger(Cooking.class.getName());

    private static final BuildingFunction FUNCTION = BuildingFunction.COOKING;

    /** The base amount of work time (cooking skill 0) to produce one single cooked meal. */
    public static final double COOKED_MEAL_WORK_REQUIRED = 8D; // 10 milli-sols is 15 mins

    // 2015-01-12 Dynamically adjusted the rate of generating meals
    //public double mealsReplenishmentRate;
    public static double UP = 0.01;
    public static double DOWN = 0.007;
    public static final int NUMBER_OF_MEAL_PER_SOL = 4;

    public static final double AMOUNT_OF_SALT_PER_MEAL = 0.005D;
    public static final double AMOUNT_OF_OIL_PER_MEAL = 0.01D;
    public static final double CLEANING_AGENT_PER_SOL = 0.1D;

    // amount of water in kg per cooked meal during meal preparation and clean-up
    public static final double WATER_USAGE_PER_MEAL = 1.0D;

    private boolean cookNoMore = false;

    // Data members
    private List<CookedMeal> cookedMeals = new ArrayList<CookedMeal>();
    //private List<CookedMeal> dailyMealList = new ArrayList<CookedMeal>();
	private List<HotMeal> mealConfigMealList; // = new ArrayList<HotMeal>();
    private List<CropType> cropTypeList;

    private int cookCapacity;
	private int mealCounterPerSol = 0;
	private int solCache = 1;
	private int numOfCookedMealCache = 0;
    private double cookingWorkTime;
    private double dryMassPerServing;

    private String producerName;

	// 2014-12-08 Added multimaps
	private Multimap<String, Integer> qualityMap;
	private Multimap<String, MarsClock> timeMap;

    private Inventory inv;
    private HotMeal aMeal;
    private Settlement settlement;
    private AmountResource dryFoodAR = null;
    private Building building = null;

    private Map<String, Double> ingredientMap = new HashMap<String, Double>();
    private Map<String, Integer> mealMap = new HashMap<String, Integer>();

    //private List<HotMeal> hotMealCache;
    private int hotMealCacheSize;

    /**
     * Constructor.
     * @param building the building this function is for.
     * @throws BuildingException if error in constructing function.
     */
    //TODO: create a CookingManager so that many parameters don't have to load multiple times
    public Cooking(Building building) {
        // Use Function constructor.
        super(FUNCTION, building);
        this.building = building;

        // 2014-12-30 Changed inv to include the whole settlement
        //inv = getBuilding().getInventory();
        inv = getBuilding().getBuildingManager().getSettlement().getInventory();

        settlement = getBuilding().getBuildingManager().getSettlement();

        //mealsReplenishmentRate = settlement.getMealsReplenishmentRate();

        cookingWorkTime = 0D;

        BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
        this.cookCapacity = config.getCookCapacity(building.getBuildingType());

        // Load activity spots
        loadActivitySpots(config.getCookingActivitySpots(building.getBuildingType()));

    	// 2014-12-12 Added cropTypeList
        //TODO: make a map of cropName and water content
		CropConfig cropConfig = SimulationConfig.instance().getCropConfiguration();
		cropTypeList = cropConfig.getCropList();

        // 2014-12-06 Added calling getMealList() from MealConfig
    	MealConfig mealConfig = SimulationConfig.instance().getMealConfiguration();
        mealConfigMealList = mealConfig.getMealList();

    	// 2014-12-08 Added multimaps
        qualityMap = ArrayListMultimap.create();
    	timeMap = ArrayListMultimap.create();

        PersonConfig personConfig = SimulationConfig.instance().getPersonConfiguration();
        dryFoodAR = AmountResource.findAmountResource(org.mars_sim.msp.core.LifeSupport.FOOD);
        dryMassPerServing = personConfig.getFoodConsumptionRate() / (double) NUMBER_OF_MEAL_PER_SOL;

       	// 2014-12-12 Added computeDryMass()
        computeDryMass();
    }

    // 2014-12-12 Created computeDryMass(). Called out once only in Cooking.java's constructor
    public void computeDryMass() {
    	Iterator<HotMeal> i = mealConfigMealList.iterator();

    	while (i.hasNext()) {

    		HotMeal aMeal = i.next();
	        List<Double> proportionList = new ArrayList<Double>();
	        List<Double> waterContentList = new ArrayList<Double>();

	       	List<Ingredient> ingredientList = aMeal.getIngredientList();
	        Iterator<Ingredient> j = ingredientList.iterator();
	        while (j.hasNext()) {

		        Ingredient oneIngredient = j.next();
		        String ingredientName = oneIngredient.getName();
		        double proportion = oneIngredient.getProportion();
		        proportionList.add(proportion);

		        // get totalDryMass
				double waterContent = getWaterContent(ingredientName);
		        waterContentList.add(waterContent);
	        }

	        // get total dry weight (sum of each ingredient's dry weight) for a meal
	        double totalDryMass = 0;
	        int k;
	        for(k = 1; k < waterContentList.size(); k++)
	        	totalDryMass += waterContentList.get(k) + proportionList.get(k) ;

	        // get this fractional number
	        double fraction = 0;
	        fraction = dryMassPerServing / totalDryMass;

		    // get ingredientDryMass for each ingredient
	        double ingredientDryMass = 0;
	        int l;
	        for(l = 0; l < ingredientList.size(); l++) {
	        	ingredientDryMass = fraction * waterContentList.get(l) + proportionList.get(l) ;
	        	ingredientDryMass = Math.round(ingredientDryMass* 1000000.0) / 1000000.0; // round up to 0.0000001 or 1mg
	        	aMeal.setIngredientDryMass(l, ingredientDryMass);
	        }

    	} // end of while (i.hasNext())
    }

    public int getHotMealCacheSize() {
    	return hotMealCacheSize;
    }

	/**
	 * Gets the water content for a crop.
	 * @return water content ( 1 is equal to 100% )
	 */
    // 2014-12-12 Created getWaterContent()
	public double getWaterContent(String name) {
		double w = 0 ;
		Iterator<CropType> i = cropTypeList.iterator();
		while (i.hasNext()) {
			CropType c = i.next();
			String cropName = c.getName();
			double water = c.getEdibleWaterContent();
			if (cropName.equals(name)) {
				w = c.getEdibleWaterContent();
				break;
			}
		}
		return w;
	}

    // 2014-12-08 Added qualityMap
    public Multimap<String, Integer> getQualityMap() {
    	Multimap<String, Integer> qualityMapCache = ArrayListMultimap.create(qualityMap);
    	// Empty out the map so that the next read by TabPanelCooking.java will be brand new cookedMeal
		if (!qualityMap.isEmpty()) {
			qualityMap.clear();
		}

    	return qualityMapCache;
    };

    // 2014-12-08 Added timeMap
    public Multimap<String, MarsClock> getTimeMap() {
    	Multimap<String, MarsClock> timeMapCache = ArrayListMultimap.create(timeMap);
       	// Empty out the map so that the next read by TabPanelCooking.java will be brand new cookedMeal
    	if (!timeMap.isEmpty()) {
    			timeMap.clear();
    		}

    	return timeMapCache;
    };


    /**
     * Gets the value of the function for a named building.
     * @param buildingName the building name.
     * @param newBuilding true if adding a new building.
     * @param settlement the settlement.
     * @return value (VP) of building function.
     * @throws Exception if error getting function value.
     */
    public static double getFunctionValue(String buildingName, boolean newBuilding,
            Settlement settlement) {

        // Demand is 1 cooking capacity for every five inhabitants.
        double demand = settlement.getAllAssociatedPeople().size() / 5D;

        double supply = 0D;
        boolean removedBuilding = false;
        Iterator<Building> i = settlement.getBuildingManager().getBuildings(FUNCTION).iterator();
        while (i.hasNext()) {
            Building building = i.next();
            if (!newBuilding && building.getBuildingType().equalsIgnoreCase(buildingName) && !removedBuilding) {
                removedBuilding = true;
            }
            else {
                Cooking cookingFunction = (Cooking) building.getFunction(FUNCTION);
                double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
                supply += cookingFunction.cookCapacity * wearModifier;
            }
        }

        double cookingCapacityValue = demand / (supply + 1D);

        BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
        double cookingCapacity = config.getCookCapacity(buildingName);

        return cookingCapacity * cookingCapacityValue;
    }

    /**
     * Get the maximum number of cooks supported by this facility.
     * @return max number of cooks
     */
    public int getCookCapacity() {
        return cookCapacity;
    }

    /**
     * Get the current number of cooks using this facility.
     * @return number of cooks
     */
    public int getNumCooks() {
        int result = 0;

        if (getBuilding().hasFunction(BuildingFunction.LIFE_SUPPORT)) {
            try {
                LifeSupport lifeSupport = (LifeSupport) getBuilding().getFunction(BuildingFunction.LIFE_SUPPORT);
                Iterator<Person> i = lifeSupport.getOccupants().iterator();
                while (i.hasNext()) {
                    Task task = i.next().getMind().getTaskManager().getTask();
                    if (task instanceof CookMeal) result++;
                }
            }
            catch (Exception e) {}
        }

        return result;
    }

    /**
     * Gets the skill level of the best cook using this facility.
     * @return skill level.
     */
    public int getBestCookSkill() {
        int result = 0;

        if (getBuilding().hasFunction(BuildingFunction.LIFE_SUPPORT)) {
            try {
                LifeSupport lifeSupport = (LifeSupport) getBuilding().getFunction(BuildingFunction.LIFE_SUPPORT);
                Iterator<Person> i = lifeSupport.getOccupants().iterator();
                while (i.hasNext()) {
                    Person person = i.next();
                    Task task = person.getMind().getTaskManager().getTask();
                    if (task instanceof CookMeal) {
                        int cookingSkill = person.getMind().getSkillManager().getEffectiveSkillLevel(SkillType.COOKING);
                        if (cookingSkill > result) result = cookingSkill;
                    }
                }
            }
            catch (Exception e) {}
        }

        return result;
    }

    /**
     * Checks if there are any cooked meals in this facility.
     * @return true if cooked meals
     */
    public boolean hasCookedMeal() {
    	int size = 0;
    	if (cookedMeals != null)
    		size = cookedMeals.size();
        return (size > 0);
    }

    /**
     * Gets the number of cooked meals in this facility.
     * @return number of meals
     */
    public int getNumberOfAvailableCookedMeals() {
        return cookedMeals.size();
    }

    public int getTotalNumberOfCookedMealsToday() {
        return mealCounterPerSol;
    }

    /**
     * Eats a cooked meal from this facility.
     * @return the meal
     */
    // Called by EatMeal.java's constructor
    // 2015-02-27 Renamed to chooseAMeal(). A person will select the favorite main/side dish if available
    public CookedMeal chooseAMeal(Person person) {
    	CookedMeal result = null;
    	CookedMeal bestFavDish = null;
        CookedMeal bestMeal = null;
        int bestQuality = -1;
      	String mainDish = person.getFavorite().getFavoriteMainDish();
      	String sideDish = person.getFavorite().getFavoriteSideDish();

        Iterator<CookedMeal> i = cookedMeals.iterator();
        while (i.hasNext()) {
            CookedMeal meal = i.next();

            // TODO: currently a person will eat either a main dish or side dish but NOT both.
            if (meal.getName().equals(mainDish) || meal.getName().equals(sideDish) ) {
	            if (meal.getQuality() > bestQuality) {
	                bestQuality = meal.getQuality();
	                bestFavDish = meal;
	            }
	        }

            else if (meal.getQuality() > bestQuality) {
                bestQuality = meal.getQuality();
                bestMeal = meal;
            }

        }

        if (bestFavDish != null) {
        	cookedMeals.remove(bestFavDish);
        	// 2015-01-06 Added dailyMealList
        	//dailyMealList.add(bestMainDish);
        	result = bestFavDish;
        }
        // if a peron's favorite dish is not found
        else if (bestMeal != null) {
        	cookedMeals.remove(bestMeal);
        	// 2015-01-06 Added dailyMealList
        	//dailyMealList.add(bestMeal);
        	result = bestMeal;
        }

        return result;
    }

    /**
     * Gets the quality of the best quality meal at the facility.
     * @return quality
     */
    public int getBestMealQuality() {
        int bestQuality = 0;
        Iterator<CookedMeal> i = cookedMeals.iterator();
        while (i.hasNext()) {
            CookedMeal meal = i.next();
            if (meal.getQuality() > bestQuality)
            	bestQuality = meal.getQuality();
        }

        return bestQuality;
    }

    /**
     * Cleanup kitchen after mealtime.
     */
    public void cleanup() {
        cookingWorkTime = 0D;
        cookNoMore = false;
    }


    // 2015-01-04a Added getCookNoMore()
 	public boolean getCookNoMore() {
 		return cookNoMore;
 	}

 	public int getPopulation() {
        return getBuilding().getBuildingManager().getSettlement().getCurrentPopulationNum();
 	}

    /**
     * Adds cooking work to this facility.
     * The amount of work is dependent upon the person's cooking skill.
     * @param workTime work time (millisols)
     */
 	// Called by CookMeal.java
    public void addWork(double workTime) {

    	cookingWorkTime += workTime;
        //logger.info("addWork() : cookingWorkTime is " + cookingWorkTime );
        //logger.info("addWork() : workTime is " + Math.round(workTime*100.0)/100.0);

    	//if ( !cookNoMore &&
    	if (cookingWorkTime >= COOKED_MEAL_WORK_REQUIRED) {

            double population = getBuilding().getBuildingManager().getSettlement().getCurrentPopulationNum();
            double maxServings = population * settlement.getMealsReplenishmentRate() / 10.0;
            int numOfCookedMeal = cookedMeals.size();
            //System.out.println( " maxServings is " + maxServings);
            //System.out.println( " MEAL_REPLENISHED_RATE is " + MEAL_REPLENISHED_RATE);
            if ( numOfCookedMealCache != numOfCookedMeal ) {
            	//System.out.println( " numOfCookedMeal is " + numOfCookedMeal);
            	numOfCookedMealCache = numOfCookedMeal;
            }

            if (numOfCookedMeal > maxServings)
            	cookNoMore = true;

            else {
            	//System.out.println("calling pickAMeal()");
	    		aMeal = pickAMeal();
	    		if (aMeal != null) {
	    			cookAHotMeal(aMeal);
	    			cookNoMore = true;
	    		}

	    	}
    	}

    }


    /**
     * Chooses a hot meal recipe that can be cooked here.
     * @return hot meal or null if none available.
     */
 	public HotMeal pickAMeal() {

 	    HotMeal result = null;
 	    // Determine list of meal recipes with available ingredients.
 	    List<HotMeal> availableMeals = getMealRecipesWithAvailableIngredients();
 	    // Randomly choose a meal recipe from those available.
 	    if (availableMeals.size() > 0) {
 	        int mealIndex = RandomUtil.getRandomInt(availableMeals.size() - 1);
 	        result = availableMeals.get(mealIndex);
 	    }

 	    return result;
	}

 	/**
 	 * Gets a list of hot meal recipes that have available ingredients.
 	 * @return list of hot meal recipes.
 	 */
 	public List<HotMeal> getMealRecipesWithAvailableIngredients() {
 		List<HotMeal> result = new ArrayList<HotMeal>(mealConfigMealList.size());

 	    Iterator<HotMeal> i = mealConfigMealList.iterator();
 	    while (i.hasNext()) {
 	        HotMeal meal = i.next();
 	        if (isMealAvailable(meal)) {
 	            result.add(meal);
 	        }
 	    }

 		hotMealCacheSize = result.size();
 		//System.out.println("hotMealCacheSize is "+hotMealCacheSize);
 	    return result;
 	}


    // 2014-11-29 Created isMealAvailable()
 	// 2015-02-08 Refactored isMealAvailable()
    public boolean isMealAvailable(HotMeal aMeal) {
    	boolean result = true;

       	List<Ingredient> ingredientList = aMeal.getIngredientList();
        Iterator<Ingredient> i = ingredientList.iterator();

        while (i.hasNext()) {

	        Ingredient oneIngredient;
	        oneIngredient = i.next();
	        String ingredientName = oneIngredient.getName();
	        double dryMass = oneIngredient.getDryMass();

	        result = retrieveAnIngredientFromMap(dryMass, ingredientName, false);
        	if (!result) break;
        	/*
	        if (ingredientMap.containsKey(ingredientName)) {

		        double value = ingredientMap.get(ingredientName);
		        if (value >= dryMass)
		        	result = true;
		        else {
		        	result = retrieveAnResourceFromMap(dryMass, ingredientName, false);
		        	if (!result) break;
			    }

	        } else {
	        	result = retrieveAnResourceFromMap(dryMass, ingredientName, false);
	        	if (!result) break;
	        }
	        */
        }
 		//System.out.println("isMealAvailable is " + result);
		return result;
    }

    /*
    public boolean checkOneIngredient(String ingredientName, double dryMass) {
    	boolean result = true;

        AmountResource ingredientAR = getFreshFoodAR(ingredientName);
        double ingredientAvailable = getFreshFood(ingredientAR);

        // set the safe threshold as dryMass
        if (ingredientAvailable >= dryMass )  {
        	//oneIngredient.setIsItAvailable(true);
        	//result = result && true; // not needed since there is no change to the value of result
        }
        else {
        	//oneIngredient.setIsItAvailable(false);
            result = false;
        }
        return result;
    }
    */

    /**
     * Gets the amount of the food item in the whole settlement.
     * @return dessertAvailable
     */
    // 2015-01-02 Modified pickOneOil()
	public String pickOneOil() {

	    	List<String> oilList = new ArrayList<String>();

	 	    if (getAmountAvailable("Soybean Oil") > AMOUNT_OF_OIL_PER_MEAL)
	 	    	oilList.add("Soybean Oil");
	 	    if (getAmountAvailable("Garlic Oil") > AMOUNT_OF_OIL_PER_MEAL)
	 	    	oilList.add("Garlic Oil");
	 	    if (getAmountAvailable("Sesame Oil") > AMOUNT_OF_OIL_PER_MEAL)
	 	    	oilList.add("Sesame Oil");
	 	    if (getAmountAvailable("Peanut Oil") > AMOUNT_OF_OIL_PER_MEAL)
	 	    	oilList.add("Peanut Oil");

			int upperbound = oilList.size();
	    	int lowerbound = 1;
	    	String selectedOil = "None";

	    	if (upperbound > 1) {
	    		int index = ThreadLocalRandom.current().nextInt(lowerbound, upperbound);
	    		//int number = (int)(Math.random() * ((upperbound - lowerbound) + 1) + lowerbound);
		    	selectedOil = oilList.get(index);
	    	}
	    	else if (upperbound == 1) {
		    	selectedOil = oilList.get(0);
	    	}
	    	else if (upperbound == 0) {
	    		selectedOil = "none";
	    		logger.info("Running out of oil in " + settlement.getName());
	    	}
	    	return selectedOil;
		}


    /**
     * Gets the amount of the food item in the whole settlement.
     * @return foodAvailable
     */
    // 2015-01-02 Added getAmountAvailable
    public double getAmountAvailable(String name) {
	    AmountResource foodAR = AmountResource.findAmountResource(name);
		double foodAvailable = inv.getAmountResourceStored(foodAR, false);
		return foodAvailable;
	}



    // 2014-11-29 Created cookAHotMeal()
    // 2014-12-12 Revised to deduct the dry weight for each ingredient
    public void cookAHotMeal(HotMeal hotMeal) {

    	List<Ingredient> ingredientList = hotMeal.getIngredientList();
	    Iterator<Ingredient> i = ingredientList.iterator();

	        while (i.hasNext()) {

		        Ingredient oneIngredient;
		        oneIngredient = i.next();
		        String ingredientName = oneIngredient.getName();
		        // 2014-12-11 Updated to using dry weight
		        double dryMass = oneIngredient.getDryMass();
		        boolean hasIt = retrieveAnIngredientFromMap(dryMass, ingredientName, true);
		        if (hasIt)
		        	;
	        } // end of while

	        retrieveOil();

	        //boolean hasIt =
	        retrieveAnIngredientFromMap(AMOUNT_OF_SALT_PER_MEAL, "Table Salt", true);
	        //if (hasIt)
	        	//resourceMap.put(ingredientName, cacheAmount-dryMass);

	        useWater();

	    	String nameOfMeal = hotMeal.getMealName();
	    	//TODO: kitchen equipment and quality of food should affect mealQuality
	       	int mealQuality = getBestCookSkill();
	        MarsClock expiration = (MarsClock) Simulation.instance().getMasterClock().getMarsClock().clone();
	        CookedMeal meal = new CookedMeal(nameOfMeal, mealQuality, dryMassPerServing, expiration, producerName, this);
	        //logger.info("a new meal made : " + meal.getName());
	    	cookedMeals.add(meal);
	    	mealCounterPerSol++;
            int numOfCookedMeal = cookedMeals.size();
            if ( numOfCookedMealCache != numOfCookedMeal ) {
            	//System.out.println( " numOfCookedMeal is " + numOfCookedMeal);
            	numOfCookedMealCache = numOfCookedMeal;
            }

	    	// 2014-12-08 Added to Multimaps
	    	qualityMap.put(nameOfMeal, mealQuality);
	    	timeMap.put(nameOfMeal, expiration);

	  	    if (logger.isLoggable(Level.FINEST)) {
	  	        	logger.finest(getBuilding().getBuildingManager().getSettlement().getName() +
	  	        			" has " + cookedMeals.size() + " meal(s) with quality score of " + mealQuality);
	  	    }

	  	    cookingWorkTime -= COOKED_MEAL_WORK_REQUIRED;
    }


  	// 2015-02-08 Added retrieveAnIngredientFromMap()
      public boolean retrieveAnIngredientFromMap(double amount, String name, boolean isRetrieving) {
      	boolean result = true;
  		// 1. check local map cache
  		//Object value = resourceMap.get(name);
      	if (ingredientMap.containsKey(name)) {
  		//if (value != null) {
  			//double cacheAmount = (double) value;
  			double cacheAmount = ingredientMap.get(name);
  		    // 2. if found, retrieve the resource locally
  			// 2a. check if cacheAmount > dryMass
  			if (cacheAmount >= amount) {
  				// compute new value for key
  				// subtract the amount from the cache
  				// set result to true
  				 ingredientMap.put(name, cacheAmount-amount);
  				//result = true && result; // not needed since there is no change to the value of result
  			}
  			else {
  				result = replenishIngredientMap(cacheAmount, amount, name, isRetrieving);
  			}
  		}
  		else {
  			result = replenishIngredientMap(0, amount, name, isRetrieving);
  		}

  		return result;
      }

  	// 2015-02-08 Added replenishIngredientMap()
      public boolean replenishIngredientMap(double cacheAmount, double amount, String name, boolean isRetrieving) {
      	boolean result = true;
      	//if (cacheAmount < amount)
  	    // 2b. if not, retrieve whatever amount from inv
  		// Note: retrieve twice the amount to REDUCE frequent calling of retrieveAnResource()
  		boolean hasFive = Storage.retrieveAnResource(amount * 5, name, inv, isRetrieving);
      	// 2b1. if inv has it, save it to local map cache
      	if (hasFive) {
      		// take 5 out, put 4 into resourceMap, use 1 right now
      		ingredientMap.put(name, cacheAmount + amount * 4);
      		//result = true && result; // not needed since there is no change to the value of result
      	}
      	else { // 2b2.
      		boolean hasOne = Storage.retrieveAnResource(amount, name, inv, isRetrieving);
      		if (hasOne)
  	    		; // no change to resourceMap since resourceMap.put(name, cacheAmount);
      		else
      			result = false;
      	}
      	return result;
      }

    // 2015-01-28 Added useWater()
    public void useWater() {
    	//TODO: need to move the hardcoded amount to a xml file
	    retrieveAnIngredientFromMap(WATER_USAGE_PER_MEAL, org.mars_sim.msp.core.LifeSupport.WATER, true);
		double wasteWaterAmount = WATER_USAGE_PER_MEAL * .95;
		Storage.storeAnResource(wasteWaterAmount, "grey water", inv);
    }


    // 2015-01-12 Added retrieveOil()
    public void retrieveOil() {
	    // 2014-12-29 Added pickOneOil()
	    String oil = pickOneOil();

	    if (!oil.equals("None")) {
	    	retrieveAnIngredientFromMap(AMOUNT_OF_OIL_PER_MEAL, oil, true);
	    }
    }

    /**
     * Retrieves the resource
     * @param name
     * @param requestedAmount

    //2015-02-07 Added retrieveAnResource()
    public boolean retrieveAnResource(double requestedAmount, String name, boolean isRetrieving ) {
    	boolean result = false;
    	try {
	    	AmountResource nameAR = AmountResource.findAmountResource(name);
	        double amountStored = inv.getAmountResourceStored(nameAR, false);
	    	inv.addAmountDemandTotalRequest(nameAR);
	        if (amountStored < requestedAmount) {
	     		//requestedAmount = amountStored;
	    		//logger.warning("Not enough " + name + " for making a meal.");
	    		result = false;
	        }
	    	else if (Math.round(amountStored*10000.0)/10000.0 < 0.0001) {
	    		//logger.warning("no more " + name + " in " + settlement.getName());
	    		result = false;
	    	}
	    	else {
	    		if (isRetrieving) {
		    		inv.retrieveAmountResource(nameAR, requestedAmount);
		    		inv.addAmountDemand(nameAR, requestedAmount);
	    		}
	    		result = true;
	    	}
	    }  catch (Exception e) {
    		logger.log(Level.SEVERE,e.getMessage());
	    }

    	return result;
    }
    */

    public void setChef(String name) {
    	this.producerName = name;
    }

    /**
     * Gets the quantity of one serving of meal
     * @return quantity
     */
    public double getMassPerServing() {
        return dryMassPerServing;
    }

    // 2014-12-01 Added getCookedMealList()
    public List<CookedMeal> getCookedMealList() {
    	return cookedMeals;
    }

    /**
     * Gets the amount resource of the fresh food from a specified food group.
     * @param String food group
     * @return AmountResource of the specified fresh food
     */
     //2014-11-21 Added getFreshFoodAR()
    public AmountResource getFreshFoodAR(String foodGroup) {
        AmountResource freshFoodAR = AmountResource.findAmountResource(foodGroup);
        return freshFoodAR;
    }

    /**
     * Computes amount of fresh food from a particular fresh food amount resource.
     *
     * @param AmountResource of a particular fresh food
     * @return Amount of a particular fresh food in kg, rounded to the 4th decimal places
     */
     //2014-11-21 Added getFreshFood()
    public double getFreshFood(AmountResource ar) {
        double freshFoodAvailable = inv.getAmountResourceStored(ar, false);
        return freshFoodAvailable;
    }

    /**
     * Computes amount of fresh food available from a specified food group.
     *
     * @param String food group
     * @return double amount of fresh food in kg, rounded to the 4th decimal places
     */
     //2014-11-21 Added getFreshFoodAvailable()
    public double getFreshFoodAvailable(String food) {
    	return getFreshFood(getFreshFoodAR(food));
    }


    /**
     * Time passing for the Cooking function in a building.
     * @param time amount of time passing (in millisols)
     * @throws BuildingException if error occurs.
     */
    // 2014-10-08: Currently converting each unit of expired meal into x kg of packed food
    // 2014-11-28 Added anyMeal for checking if any CookedMeal exists
    public void timePassing(double time) {
    	boolean hasAMeal = hasCookedMeal();
	     //logger.info(" hasAMeal : "+ hasAMeal);
    	//TODO: check for whether it is meal hour. if it's not, bypass some steps below
	     if ( hasAMeal ) {
	    	 double rate = settlement.getMealsReplenishmentRate();
	         //int newNumOfCookedMeal = cookedMeals.size();
	         //if ( numOfCookedMealCache != newNumOfCookedMeal)	logger.info("Still has " + newNumOfCookedMeal +  " CookedMeal(s)" );
	         Iterator<CookedMeal> i = cookedMeals.iterator();
	         while (i.hasNext()) {
	            CookedMeal meal = i.next();
	            MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
	            if (MarsClock.getTimeDiff(meal.getExpirationTime(), currentTime) < 0D) {
	            	//dailyMealList.add(meal);
	 	      		try {
	 	      			i.remove();
		            	// 2015-02-27 The probability that the expired dessert is of no good and must be discarded is also dependent upon the food quality
		            	double quality = meal.getQuality()/2D + 1D;
	 	      			double num = RandomUtil.getRandomDouble(8*quality);
	 	      			if (num < 1) {
	 	      				Storage.storeAnResource(dryMassPerServing, "Food Waste", inv);
			            	logger.info(dryMassPerServing  + " kg " + meal.getName()
			                		+ " expired, turned bad and discarded at " + getBuilding().getNickName()
			                		+ " in " + settlement.getName() );
	 	      			}
	 	      			else {
	 	      				preserveFood();
			            	logger.info("Meal Expired. Convert "
			            			+ dryMassPerServing  + " kg "
			                		+ meal.getName()
			                		+ " into preserved food "
			                		+  " at " + getBuilding().getNickName()
			                		+ " in " + settlement.getName() );
			      	   		if(logger.isLoggable(Level.FINEST)) {
			      	            logger.finest("No one is eating " + meal.getName() + ". Convert meal into preserved food at " +
			      	                    getBuilding().getBuildingManager().getSettlement().getName());
			      	   		}
	 	      			}
		      	        // 2015-01-12 Adjust the rate to go down for each meal
			  	    	if (rate > 0 )
			  	    		rate -= DOWN;
				         settlement.setMealsReplenishmentRate(rate);
	 	      		} catch (Exception e) {}
	          	}

	         }
	     }
	     //numOfCookedMealCache = newNumOfCookedMeal;

	     // 2015-01-12 Added checkEndOfDay()
	     checkEndOfDay();
    }
/*
	public boolean storeAnResource(double amount, String name) {
		Inventory inventory = inv;
		return storeAnResource(amount, name, inventory);
	}

	// 2015-02-06 Added storeAnResource()
	public boolean storeAnResource(double amount, String name, Inventory inv) {
		boolean result = false;
		try {
			AmountResource ar = AmountResource.findAmountResource(name);
			double remainingCapacity = inv.getAmountResourceRemainingCapacity(ar, false, false);

			if (remainingCapacity < amount) {
			    // if the remaining capacity is smaller than the harvested amount, set remaining capacity to full
				amount = remainingCapacity;
				result = false;
			    //logger.info("addHarvest() : storage is full!");
			}
			else {
				inv.storeAmountResource(ar, amount, true);
				inv.addAmountSupplyAmount(ar, amount);
				result = true;
			}
		} catch (Exception e) {
    		logger.log(Level.SEVERE,e.getMessage());
		}

		return result;
	}
*/
    // 2015-01-12 Added checkEndOfDay()
	public void checkEndOfDay() {

		MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
	    // Added 2014-12-08 : Sanity check for the passing of each day
		int newSol = currentTime.getSolOfMonth();
	    double rate = settlement.getMealsReplenishmentRate();
	    if ( newSol != solCache) {
	    	// 2015-01-12 Adjust the rate to go up automatically by default
	       	solCache = newSol;
	    	rate += UP;
	        settlement.setMealsReplenishmentRate(rate);
	        // reset back to zero at the beginning of a new day.
	 		mealCounterPerSol = 0;
	        if (!timeMap.isEmpty()) timeMap.clear();
	 		if (!qualityMap.isEmpty()) qualityMap.clear();

	 		cleanUpKitchen();

	       	/*
	       	// TODO: do we need to clear all desserts by the end of the day?
	    	// It is causing the system to hang
	 		boolean hasAMeal = hasCookedMeal();
		    if ( hasAMeal ) {
		    	int newNumOfCookedMeal = cookedMeals.size();
		        //if ( numOfCookedMealCache != newNumOfCookedMeal)	logger.info("Still has " + newNumOfCookedMeal +  " CookedMeal(s)" );
		    	Iterator<CookedMeal> j = cookedMeals.iterator();

		    	while (j.hasNext()) {
		        	try {
		        		j.remove();
		        		refrigerateFood();
		        		// 2015-01-12 For each meal that needs to be refrigerated
		        		// Adjust the rate to go down
		        		if (rate > 0 )
		        			rate -= DOWN;
		                logger.info("End of Day. Refrigerate "
		                 		+ dryMassPerServing + " kg "
		                 		+ dryFoodAR.getName()
		                 		+  " at " + getBuilding().getNickName()
		                 		+ " in " + settlement.getName()
		                 		);
		        	} catch (Exception e) {}
		        	//logger.info("Month " + newMonth + " Sol " + newSol + " : "
			 		//		+ mealCounterPerSol + " meals made yesterday in "
				    //    	+ building.getNickName() + " at " + settlement.getName());
		    	} // end of while (j.hasNext())
		    } // end of if ( hasAMeal )
		    */
	    } // end of if ( newSol != solCache)

	}

	// 2015-02-27 Added cleanUpKitchen()
	public void cleanUpKitchen() {
		Storage.retrieveAnResource(CLEANING_AGENT_PER_SOL, "Sodium Hypochlorite", inv, true);
		Storage.retrieveAnResource(CLEANING_AGENT_PER_SOL*10D, org.mars_sim.msp.core.LifeSupport.WATER, inv, true);
	}

	// 2015-01-16 Added salt as preservatives
	public void preserveFood() {
		retrieveAnIngredientFromMap(AMOUNT_OF_SALT_PER_MEAL, "Table Salt", true);
		Storage.storeAnResource(dryMassPerServing, org.mars_sim.msp.core.LifeSupport.FOOD, inv);
 	}

    /**
     * Gets the amount of power required when function is at full power.
     * @return power (kW)
     */
    public double getFullPowerRequired() {
        return getNumCooks() * 10D;
    }

    /**
     * Gets the amount of power required when function is at power down level.
     * @return power (kW)
     */
    public double getPoweredDownPowerRequired() {
        return 0;
    }

    @Override
    public double getMaintenanceTime() {
        return cookCapacity * 10D;
    }

    @Override
    public void destroy() {
        super.destroy();
        inv = null;
        building = null;
        cookedMeals.clear();
        cookedMeals = null;
        settlement = null;
        //dailyMealList.clear();
        //dailyMealList = null;
        aMeal = null;
        mealConfigMealList.clear();
        mealConfigMealList = null;
        dryFoodAR = null;
        cropTypeList.clear();
        cropTypeList = null;
    }

	@Override
	public double getFullHeatRequired() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getPoweredDownHeatRequired() {
		// TODO Auto-generated method stub
		return 0;
	}

}