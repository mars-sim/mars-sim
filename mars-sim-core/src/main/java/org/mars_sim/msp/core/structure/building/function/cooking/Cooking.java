/**
 * Mars Simulation Project
 * Cooking.java
 * @version 3.1.0 2017-03-03
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function.cooking;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LifeSupportType;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.CookMeal;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.Function;
import org.mars_sim.msp.core.structure.building.function.LifeSupport;
import org.mars_sim.msp.core.structure.building.function.RoboticStation;
import org.mars_sim.msp.core.structure.building.function.Storage;
import org.mars_sim.msp.core.structure.building.function.farming.CropConfig;
import org.mars_sim.msp.core.structure.building.function.farming.CropType;
import org.mars_sim.msp.core.structure.building.function.farming.Farming;
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

    public static final String SODIUM_HYPOCHLORITE = "sodium hypochlorite";
    public static final String FOOD_WASTE = "food waste";
    public static final String GREY_WATER = "grey water";
    public static final String TABLE_SALT = "table salt";
    public static final String SOLID_WASTE = "solid waste";
    public static final String NAPKIN = "napkin";

    public static final String SOYBEAN_OIL = "soybean oil";
    public static final String GARLIC_OIL = "garlic oil";
    public static final String SESAME_OIL = "sesame oil";
    public static final String PEANUT_OIL = "peanut oil";

    public static final int RECHECKING_FREQ = 250; // in millisols

    // 2015-01-12 Dynamically adjusted the rate of generating meals
    //public double mealsReplenishmentRate;
    public static double UP = 0.01;
    public static double DOWN = 0.007;
    public static final int NUMBER_OF_MEAL_PER_SOL = 4;
    public int oil_count = 0;

    public static final double AMOUNT_OF_SALT_PER_MEAL = 0.005D;
    public static final double AMOUNT_OF_OIL_PER_MEAL = 0.01D;

    // The average amount of cleaning agent (kg) used per sol for clean-up
    //public static final double CLEANING_AGENT_PER_SOL = 0.1D;
    private double cleaningAgentPerSol;

    // the average amount of water in kg per cooked meal during meal preparation and clean-up
    //public static final double WATER_USAGE_PER_MEAL = 0.8D;
    private double waterUsagePerMeal;

    private boolean cookNoMore = false;
    private boolean no_oil_last_time = false;

    // Data members
    private List<CookedMeal> cookedMeals = new CopyOnWriteArrayList<>();//<CookedMeal>();
    //private List<CookedMeal> dailyMealList = new ArrayList<CookedMeal>();
	private List<HotMeal> mealConfigMealList; // = new ArrayList<HotMeal>();
    private List<CropType> cropTypeList;
    //private List<String> oilMenu;// = new CopyOnWriteArrayList<>();
    private static List<AmountResource> oilMenuAR;

    private int bestQualityCache = 0;
    private int cookCapacity;
	private int mealCounterPerSol = 0;
	private int solCache = 1;
	private int numCookableMeal;

    private double cookingWorkTime;
    private double dryMassPerServing;

    private String producerName;

	// 2014-12-08 Added multimaps
	private Multimap<String, Integer> qualityMap;
	private Multimap<String, MarsClock> timeMap;

    private Inventory inv;
    private HotMeal aMeal;
    private Settlement settlement;

    public static AmountResource tableSaltAR;
    public static AmountResource NaClOAR;
    public static AmountResource greyWaterAR;
    public static AmountResource waterAR;
    public static AmountResource foodWasteAR;
    public static AmountResource foodAR;


    private Map<AmountResource, Double> ingredientMap = new ConcurrentHashMap<>(); //HashMap<String, Double>();
    //private Map<String, Double> ingredientMap = new ConcurrentHashMap<>(); //HashMap<String, Double>();
    private Map<String, Integer> mealMap = new ConcurrentHashMap<>(); //HashMap<String, Integer>();

    private static Simulation sim = Simulation.instance();
    private static SimulationConfig simulationConfig = SimulationConfig.instance();
    private static BuildingConfig buildingConfig = simulationConfig.getBuildingConfiguration();
    private static CropConfig cropConfig = simulationConfig.getCropConfiguration();
    private static MealConfig mealConfig = simulationConfig.getMealConfiguration();
    private static PersonConfig personConfig = simulationConfig.getPersonConfiguration();
    private static MarsClock marsClock;// = sim.getMasterClock().getMarsClock();

    /**
     * Constructor.
     * @param building the building this function is for.
     * @throws BuildingException if error in constructing function.
     */
    //TODO: create a CookingManager so that many parameters don't have to load multiple times
    public Cooking(Building building) {
        // Use Function constructor.
        super(FUNCTION, building);

        // 2014-12-30 Changed inv to include the whole settlement
        //inv = getBuilding().getSettlementInventory();
        inv = getBuilding().getBuildingManager().getSettlement().getInventory();

        settlement = getBuilding().getBuildingManager().getSettlement();

        //mealsReplenishmentRate = settlement.getMealsReplenishmentRate();

        cookingWorkTime = 0D;

        marsClock = sim.getMasterClock().getMarsClock();

        BuildingConfig buildingConfig = simulationConfig.instance().getBuildingConfiguration(); // need this to pass maven test
        this.cookCapacity = buildingConfig.getCookCapacity(building.getBuildingType());

        // Load activity spots
        loadActivitySpots(buildingConfig.getCookingActivitySpots(building.getBuildingType()));

    	// 2014-12-12 Added cropTypeList
        //TODO: make a map of cropName and water content
		CropConfig cropConfig = SimulationConfig.instance().getCropConfiguration(); // need this to pass maven test
		cropTypeList = cropConfig.getCropList();

        // 2014-12-06 Added calling getMealList() from MealConfig
    	MealConfig mealConfig = SimulationConfig.instance().getMealConfiguration(); // need this to pass maven test
        mealConfigMealList = mealConfig.getMealList();

        // 2016-05-31 Added loading the two parameters from meals.xml
        cleaningAgentPerSol = mealConfig.getCleaningAgentPerSol();
        waterUsagePerMeal = mealConfig.getWaterConsumptionRate();

    	// 2014-12-08 Added multimaps
        qualityMap = ArrayListMultimap.create();
    	timeMap = ArrayListMultimap.create();

        PersonConfig personConfig = SimulationConfig.instance().getPersonConfiguration(); // need this to pass maven test

        tableSaltAR = AmountResource.findAmountResource(TABLE_SALT);
        foodAR = AmountResource.findAmountResource(LifeSupportType.FOOD);
        foodWasteAR = AmountResource.findAmountResource(FOOD_WASTE);
        greyWaterAR = AmountResource.findAmountResource(GREY_WATER);
        NaClOAR = AmountResource.findAmountResource(SODIUM_HYPOCHLORITE);
        waterAR = AmountResource.findAmountResource(LifeSupportType.WATER);

        dryMassPerServing = personConfig.getFoodConsumptionRate() / (double) NUMBER_OF_MEAL_PER_SOL;

       	// 2014-12-12 Added computeDryMass()
        computeDryMass();

        prepareOilMenu();

    }

    public static void prepareOilMenu() {

    	if (oilMenuAR == null) {
	        oilMenuAR = new CopyOnWriteArrayList<AmountResource>();
	        oilMenuAR.add(AmountResource.findAmountResource(SOYBEAN_OIL));
	        oilMenuAR.add(AmountResource.findAmountResource(GARLIC_OIL));
	        oilMenuAR.add(AmountResource.findAmountResource(SESAME_OIL));
	        oilMenuAR.add(AmountResource.findAmountResource(PEANUT_OIL));
    	}
    }


    // 2014-12-12 Created computeDryMass(). Called out once only in Cooking.java's constructor
    public void computeDryMass() {
    	Iterator<HotMeal> i = mealConfigMealList.iterator();

    	while (i.hasNext()) {

    		HotMeal aMeal = i.next();
	        List<Double> proportionList = new CopyOnWriteArrayList<>(); //<Double>();
	        List<Double> waterContentList = new CopyOnWriteArrayList<>(); //ArrayList<Double>();

	       	List<Ingredient> ingredientList = aMeal.getIngredientList();
	        Iterator<Ingredient> j = ingredientList.iterator();
	        while (j.hasNext()) {

		        Ingredient oneIngredient = j.next();
		        String ingredientName = oneIngredient.getName();
		        double proportion = oneIngredient.getProportion();
		        proportionList.add(proportion);

		        // get totalDryMass
				double waterContent = getCropWaterContent(ingredientName);
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

//    public int getHotMealCacheSize() {
//    	return hotMealCacheSize;
//    }

	/**
	 * Gets the water content for a crop.
	 * @return water content ( 1 is equal to 100% )
	 */
    // 2014-12-12 Created getWaterContent()
	public double getCropWaterContent(String name) {
		double w = 0 ;
		Iterator<CropType> i = cropTypeList.iterator();
		while (i.hasNext()) {
			CropType c = i.next();
			String cropName = c.getName();
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
        //BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
        //System.out.println("before calling cookingCapacity");
        double cookingCapacity = buildingConfig.getCookCapacity(buildingName);
        //System.out.println("after calling cookingCapacity");
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
                    if (task instanceof CookMeal) {
                        result++;
                    }
                }

                //2015-12-10 Officiated Chefbot's contribution as cook
                RoboticStation rs = (RoboticStation) getBuilding().getFunction(BuildingFunction.ROBOTIC_STATION);
                Iterator<Robot> j = rs.getRobotOccupants().iterator();
                while (j.hasNext()) {
                    Task task = j.next().getBotMind().getBotTaskManager().getTask();
                    if (task instanceof CookMeal) {
                        result++;
                    }
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
    public int getBestCookSkill(boolean has_oil) {
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
                        if (cookingSkill > result) {
                            result = cookingSkill;
                            if (has_oil && result < 1)
                            	result++;
                        }
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
    	if (cookedMeals != null) {
    		size = cookedMeals.size();
    	}
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
    public CookedMeal chooseAMeal(Person person) {
    	CookedMeal bestFavDish = null;
        CookedMeal bestMeal = null;
        int bestQuality = -1;
      	String mainDish = person.getFavorite().getFavoriteMainDish();
      	String sideDish = person.getFavorite().getFavoriteSideDish();

        Iterator<CookedMeal> i = cookedMeals.iterator();
        while (i.hasNext()) {
            CookedMeal meal = i.next();
            // TODO: define how a person will choose to eat a main dish and/or side dish
            String mealN = meal.getName();
            int mealQ = meal.getQuality();
            if (mealN.equals(mainDish)) {
                // person will choose the main dish
            	if (mealQ > bestQuality) {
	            	// save the one with the best quality
	                bestQuality = mealQ;
	                bestFavDish = meal;
	            	cookedMeals.remove(bestFavDish);
	            	return bestFavDish;
	            }
            }

            else if (mealN.equals(sideDish)) {
                // person will choose side dish
            	if (mealQ > bestQuality) {
	            	// save the one with the best quality
	                bestQuality = mealQ;
	                bestFavDish = meal;
	            	cookedMeals.remove(bestFavDish);
	            	return bestFavDish;
	            }
	        }

            else if (mealQ > bestQuality) {
	            // not his/her fav but still save the one with the best quality
                bestQuality = mealQ;
                bestMeal = meal;
            }

			else {
			    // not his/her fav but still save the one with the best quality
				bestQuality = mealQ;
			    bestMeal = meal;
			}
        }

        if (bestMeal != null) {
            // a person will eat the best quality meal
        	cookedMeals.remove(bestMeal);
        }

        return bestMeal;
    }

    /**
     * Gets the quality of the best quality meal at the facility.
     * @return quality
     */
    public int getBestMealQuality() {

    	int bestQuality = 0;
    	// Question: do we want to remember the best quality ever or just the best quality among the current servings ?
        Iterator<CookedMeal> i = cookedMeals.iterator();
        while (i.hasNext()) {
            //CookedMeal meal = i.next();
            int q = i.next().getQuality();
            if (q > bestQuality)
            	bestQuality = q;
        }

        if (bestQuality > bestQualityCache)
        	bestQualityCache = bestQuality;
        return bestQuality;
    }

    public int getBestMealQualityCache() {
    	getBestMealQuality();
    	return bestQualityCache;
    }

    /**
     * Cleanup kitchen after meal time.
     */
    public void cleanUp() {
        cookingWorkTime = 0D;
        cookNoMore = false;
    }

    /**
     * Check if there should be no more cooking at this kitchen during this meal time.
     * @return true if no more cooking.
     */
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
    public String addWork(double workTime) {
    	String nameOfMeal = null;

    	cookingWorkTime += workTime;

    	if ((cookingWorkTime >= COOKED_MEAL_WORK_REQUIRED) && (!cookNoMore)) {

            double population = getPopulation();
            double maxServings = population * settlement.getMealsReplenishmentRate();

            int numSettlementCookedMeals = getTotalAvailableCookedMealsAtSettlement(settlement);

            //System.out.println("numSettlementCookedMeals : " + numSettlementCookedMeals + "  maxServings " + maxServings);
            if (numSettlementCookedMeals >= maxServings) {
            	cookNoMore = true;
            }

            else {
            	// Randomly pick a meal which ingredients are available
	    		aMeal = getACookableMeal();
	    		if (aMeal != null) {
	            	//System.out.println("aMeal is " + aMeal);
	    			nameOfMeal = cookAHotMeal(aMeal);
	    		}
	    	}
    	}

    	return nameOfMeal;
    }

    /**
     * Gets the total number of available cooked meals at a settlement.
     * @param settlement the settlement.
     * @return number of cooked meals.
     */
    private int getTotalAvailableCookedMealsAtSettlement(Settlement settlement) {

        int result = 0;

        Iterator<Building> i = settlement.getBuildingManager().getBuildings(FUNCTION).iterator();
        while (i.hasNext()) {
            //Building building = i.next();
            //Cooking kitchen = (Cooking) building.getFunction(BuildingFunction.COOKING);
            //result += kitchen.getNumberOfAvailableCookedMeals();
            result += ((Cooking) i.next().getFunction(BuildingFunction.COOKING)).getNumberOfAvailableCookedMeals();
        }

        return result;

    }

    /**
     * Chooses a hot meal recipe that can be cooked here.
     * @return hot meal or null if none available.

 	public HotMeal pickAMeal() {

 	    HotMeal result = null;
 	    // Determine list of meal recipes with available ingredients.
 	    List<HotMeal> availableMeals = getMealRecipesWithAvailableIngredients();
 	    int size = availableMeals.size();
 	    // Randomly choose a meal recipe from those available.
 	    if (size > 0) {
 	        int mealIndex = RandomUtil.getRandomInt(size - 1);
 	        result = availableMeals.get(mealIndex);
 	    }

 	    return result;

	}
     */

    /**
     * Randomly picks a hot meal with its ingredients fully available.
     * @return a hot meal or null if none available.
     */
 	// 2015-12-10 Added getACookableMeal()
 	public HotMeal getACookableMeal() {
/*
 		HotMeal result = null;
 		List<HotMeal> meals = mealConfigMealList;
 		Collections.shuffle(meals);
 	    Iterator<HotMeal> i = mealConfigMealList.iterator();
 	    while (i.hasNext()) {
 	        HotMeal meal = i.next();
 	        if (areAllIngredientsAvailable(meal)) {
 	            result = meal;
 	            break;
 	        }
 	    }

 	    return result;
*/
 		return mealConfigMealList
				.stream()
				.filter(meal -> areAllIngredientsAvailable(meal) == true)
				.findAny().orElse(null);//.get();
 	}


 	/**
 	 * Gets a list of cookable meal with available ingredients.
 	 * @return list of hot meal.
 	 */
 	public List<HotMeal> getMealRecipesWithAvailableIngredients() {
 /*
 		List<HotMeal> result = new CopyOnWriteArrayList<>();
 	    Iterator<HotMeal> i = mealConfigMealList.iterator();
 	    while (i.hasNext()) {
 	        HotMeal meal = i.next();
 	        if (areAllIngredientsAvailable(meal)) {
 	            result.add(meal);
 	        }
 	    }

 	     	    return result;
*/
 		return mealConfigMealList
				.stream()
				.filter(meal -> areAllIngredientsAvailable(meal) == true)
				.collect(Collectors.toList());

 	}

 	/**
 	 * Caches the number of cookable meals (i.e. meals with all the ingredients available)
 	 * @param size
 	 */
 	public void setNumCookableMeal(int size) {
 		numCookableMeal = size;
 	}

 	/**
 	 * Returns the last known number of cookable meals (i.e. a meal with all the ingredients available)
 	 * @return number of meals
 	 */
 	public int getNumCookableMeal() {
 		return numCookableMeal;
 	}

	/**
	 * Checks if all ingredients are available for a particular meal
	 * @param aMeal a hot meal
	 * @return true or false
	 */
    public boolean areAllIngredientsAvailable(HotMeal aMeal) {
/*
      	boolean result = true;
       	List<Ingredient> ingredientList = aMeal.getIngredientList();
        Iterator<Ingredient> i = ingredientList.iterator();

        while (i.hasNext()) {

	        Ingredient oneIngredient;
	        oneIngredient = i.next();
	        //String ingredientName = oneIngredient.getName();
    		AmountResource ingredientAR = oneIngredient.getAR();
	        double dryMass = oneIngredient.getDryMass();

	        // checks if a particular ingredient is available
	        result = retrieveAnIngredientFromMap(dryMass, ingredientAR, false);
        	if (!result) break;
        }

		return result;
*/
 		return aMeal.getIngredientList()
				.stream()
				.allMatch(i -> retrieveAnIngredientFromMap(i.getDryMass(), i.getAR(), false));


    }


    /**
     * Gets the amount of the food item in the whole settlement.
     * @return dessertAvailable
     */
    // 2015-01-02 Modified pickOneOil()
	public AmountResource pickOneOil(double amount) {
/*
	    	List<AmountResource> available_oils = new CopyOnWriteArrayList<>();
	    	int size = oilMenuAR.size();
	    	for (int i=0; i<size; i++) {
	    		AmountResource oilAR = oilMenuAR.get(i);
	    		if (getAmountAvailable(oilAR) > amount)//AMOUNT_OF_OIL_PER_MEAL)
		 	    	available_oils.add(oilAR);
	    	}

	    	int s = available_oils.size();
	    	AmountResource selectedOil = null;
	    	int index = 0;
	    	if (s > 0) {
	    		index = RandomUtil.getRandomInt(s-1);
	    		//System.out.println("index is " + index);
	    		selectedOil = available_oils.get(index);
	    	}
	    	else {
	    		no_oil_last_time = true;
	    		int rand = RandomUtil.getRandomInt(size-1);

		    	inv.addAmountDemand(oilMenuAR.get(rand), amount);
	    		oil_count++;
	    		if (oil_count < 1)
	    			logger.info("Running out of oil in " + getBuilding().getNickName() + " at "+ settlement.getName());
	    	}

	    	//logger.info("oil index : "+ index);
	    	return selectedOil;
*/
	 		return oilMenuAR
					.stream()
					.filter(oil -> inv.getAmountResourceStored(oil, false) > amount)
					.findFirst().orElse(null);//.get();;


		}


    /**
     * Gets the amount of the food item in the whole settlement.
     * @return foodAvailable
    public double getAmountAvailable(AmountResource ar) {
	    //AmountResource foodAR = AmountResource.findAmountResource(name);
		//double foodAvailable = inv.getAmountResourceStored(foodAR, false);
		//return foodAvailable;
		//return inv.getAmountResourceStored(foodAR, false);
		return inv.getAmountResourceStored(ar, false);
	}
     */

    /**
     * Cook a hot meal.
     * @param hotMeal the meal to cook.
     */
    public String cookAHotMeal(HotMeal hotMeal) {

    	List<Ingredient> ingredientList = hotMeal.getIngredientList();
	    Iterator<Ingredient> i = ingredientList.iterator();
	    while (i.hasNext()) {
	        Ingredient oneIngredient = i.next();
	        //String ingredientName = oneIngredient.getName();
    		AmountResource ingredientAR = oneIngredient.getAR();
	        // 2014-12-11 Updated to using dry weight
	        double dryMass = oneIngredient.getDryMass();
	        retrieveAnIngredientFromMap(dryMass, ingredientAR, true);
	    }

	    // consume oil
	    boolean has_oil = false;

	    if (!no_oil_last_time)
	    	has_oil = consumeOil(hotMeal.getOil());

	    // consume salt
	    retrieveAnIngredientFromMap(hotMeal.getSalt(), tableSaltAR, true);

	    // consume water
	    consumeWater();

	    String nameOfMeal = hotMeal.getMealName();
	    //TODO: kitchen equipment and quality of food should affect mealQuality
	    int mealQuality = getBestCookSkill(has_oil);
	    MarsClock expiration = (MarsClock) marsClock.clone();
	    CookedMeal meal = new CookedMeal(nameOfMeal, mealQuality, dryMassPerServing, expiration, producerName, this);
	    logger.finest("A new meal was cooked by : " + meal.getName());
	    cookedMeals.add(meal);
	    mealCounterPerSol++;

	    // 2014-12-08 Added to Multimaps
	    qualityMap.put(nameOfMeal, mealQuality);
	    timeMap.put(nameOfMeal, expiration);

	    logger.finest(getBuilding().getBuildingManager().getSettlement().getName() +
	            " has " + cookedMeals.size() + " meal(s) with quality score of " + mealQuality);

	    cookingWorkTime -= COOKED_MEAL_WORK_REQUIRED;

	    return nameOfMeal;
    }


    public boolean retrieveAnIngredientFromMap(double amount, AmountResource resource, boolean isRetrieving) {
        boolean result = true;
        // 1. check local map cache
        //Object value = resourceMap.get(name);
        if (ingredientMap.containsKey(resource)) {
            //if (value != null) {
            //double cacheAmount = (double) value;
            double cacheAmount = ingredientMap.get(resource);
            // 2. if found, retrieve the resource locally
            // 2a. check if cacheAmount > dryMass
            if (cacheAmount >= amount) {
                // compute new value for key
                // subtract the amount from the cache
                // set result to true
                ingredientMap.put(resource, cacheAmount-amount);
                //result = true && result; // not needed since there is no change to the value of result
            }
            else {
                result = replenishIngredientMap(cacheAmount, amount, resource, isRetrieving);
            }
        }
        else {
            result = replenishIngredientMap(0, amount, resource, isRetrieving);
        }

        return result;
    }

    public boolean replenishIngredientMap(double cacheAmount, double amount, AmountResource resource, boolean isRetrieving) {
        boolean result = true;
        //if (cacheAmount < amount)
        // 2b. if not, retrieve whatever amount from inv
        // Note: retrieve twice the amount to REDUCE frequent calling of retrieveAnResource()
        boolean hasFive = Storage.retrieveAnResource(amount * 5, resource, inv, isRetrieving);
        // 2b1. if inv has it, save it to local map cache
        if (hasFive) {
            // take 5 out, put 4 into resourceMap, use 1 right now
            ingredientMap.put(resource, cacheAmount + amount * 4);
            //result = true && result; // not needed since there is no change to the value of result
        }
        else { // 2b2.
            boolean hasOne = Storage.retrieveAnResource(amount, resource, inv, isRetrieving);
            if (hasOne)
                ; // no change to resourceMap since resourceMap.put(name, cacheAmount);
            else
                result = false;
        }
        return result;
    }

    /**
     * Consumes a certain amount of water for each meal
     */
    // 2015-01-28 Added consumeWater()
    public void consumeWater() {
    	//TODO: need to move the hardcoded amount to a xml file
    	int sign = RandomUtil.getRandomInt(0, 1);
    	double rand = RandomUtil.getRandomDouble(0.2);
    	double usage = waterUsagePerMeal;
    	if (sign == 0)
    		usage = 1 + rand;
    	else
    		usage = 1 - rand;
	    retrieveAnIngredientFromMap(usage, waterAR, true);
		double wasteWaterAmount = usage * .5;
		Storage.storeAnResource(wasteWaterAmount, greyWaterAR, inv);
    }


    // 2015-01-12 Added consumeOil()
    public boolean consumeOil(double oilRequired) {
	    // 2014-12-29 Added pickOneOil()
    	AmountResource oil = pickOneOil(oilRequired);
    	inv.addAmountDemand(oil, oilRequired);
	    if (oil != null) {
		    //may use the default amount of AMOUNT_OF_OIL_PER_MEAL;
	    	retrieveAnIngredientFromMap(oilRequired, oil, true);
	    	return true;
	    }
	    else
		    // oil is not available
	    	return false;
    }

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

     //2014-11-21 Added getFreshFoodAR()
    public AmountResource getFreshFoodAR(String foodGroup) {
        AmountResource freshFoodAR = AmountResource.findAmountResource(foodGroup);
        return freshFoodAR;
    }
    */

    /**
     * Computes amount of fresh food from a particular fresh food amount resource.
     *
     * @param AmountResource of a particular fresh food
     * @return Amount of a particular fresh food in kg, rounded to the 4th decimal places

     //2014-11-21 Added getFreshFood()
    public double getFreshFood(AmountResource ar) {
        double freshFoodAvailable = inv.getAmountResourceStored(ar, false);
        return freshFoodAvailable;
    }
    */

    /**
     * Computes amount of fresh food available from a specified food group.
     *
     * @param String food group
     * @return double amount of fresh food in kg, rounded to the 4th decimal places

     //2014-11-21 Added getFreshFoodAvailable()
    public double getFreshFoodAvailable(String food) {
    	return getFreshFood(getFreshFoodAR(food));
    }
*/

    /**
     * Time passing for the Cooking function in a building.
     * @param time amount of time passing (in millisols)
     */
    public void timePassing(double time) {

	    int millisols =  (int) marsClock.getMillisol();
	    if (millisols % RECHECKING_FREQ == 0) {
	    	// reset
	    	no_oil_last_time = false;
	    }

        if (hasCookedMeal()) {
            double rate = settlement.getMealsReplenishmentRate();

            // Handle expired cooked meals.
            Iterator<CookedMeal> i = cookedMeals.iterator();
            while (i.hasNext()) {
                CookedMeal meal = i.next();
                //MarsClock currentTime = marsClock;
                if (MarsClock.getTimeDiff(meal.getExpirationTime(), marsClock) < 0D) {

                    try {
                        cookedMeals.remove(meal);

                        // Check if cooked meal has gone bad and has to be thrown out.
                        double quality = meal.getQuality() / 2D + 1D;
                        double num = RandomUtil.getRandomDouble(7 * quality + 1);
                        if (num < 1) {
                            Storage.storeAnResource(dryMassPerServing, foodWasteAR, inv);
                            logger.fine(dryMassPerServing  + " kg " + meal.getName()
                                    + " expired, turned bad and discarded at " + getBuilding().getNickName()
                                    + " in " + settlement.getName() );

                        } else {
                            // Convert the meal into preserved food.
                            preserveFood();
                            logger.fine("Meal Expired. Convert "
                                    + dryMassPerServing  + " kg "
                                    + meal.getName()
                                    + " into preserved food at "
                                    + getBuilding().getNickName()
                                    + " in " + settlement.getName() );
                        }

                        // Adjust the rate to go down for each meal that wasn't eaten.
                        if (rate > 0) {
                            rate -= DOWN;
                        }
                        settlement.setMealsReplenishmentRate(rate);
                    }
                    catch (Exception e) {}
                }
            }
        }

        // Check if not meal time, clean up.
        Coordinates location = getBuilding().getBuildingManager().getSettlement().getCoordinates();
        if (!CookMeal.isMealTime(location)) {
            cleanUp();
        }

        // 2015-01-12 Added checkEndOfDay()
        checkEndOfDay();
    }

    // 2015-01-12 Added checkEndOfDay()
	public void checkEndOfDay() {
		if (marsClock == null)
			marsClock = Simulation.instance().getMasterClock().getMarsClock(); // needed for loading a saved sim
	    // Added 2014-12-08 : Sanity check for the passing of each day
		int newSol = marsClock.getSolOfMonth();//getSolElapsedFromStart();
	    if (newSol != solCache) {
	    	// 2015-01-12 Adjust the rate to go up automatically by default
	       	solCache = newSol;
		    double rate = settlement.getMealsReplenishmentRate();
	    	rate += UP;
	        settlement.setMealsReplenishmentRate(rate);
	        // reset back to zero at the beginning of a new day.
	 		mealCounterPerSol = 0;
	        if (!timeMap.isEmpty()) timeMap.clear();
	 		if (!qualityMap.isEmpty()) qualityMap.clear();

	 		// 2015-12-10 Reset the cache value for numCookableMeal
	 		int size = getMealRecipesWithAvailableIngredients().size();
	 		setNumCookableMeal(size);

	 		cleanUpKitchen();

	 		oil_count = 0;
	    }
	}

	// 2015-02-27 Added cleanUpKitchen()
	public void cleanUpKitchen() {
		Storage.retrieveAnResource(cleaningAgentPerSol, NaClOAR, inv, true); //SODIUM_HYPOCHLORITE, inv, true);//AmountResource.
		Storage.retrieveAnResource(cleaningAgentPerSol*10D, waterAR, inv, true);//org.mars_sim.msp.core.LifeSupportType.WATER, inv, true);
	}

	// 2015-01-16 Added salt as preservatives
	public void preserveFood() {
		retrieveAnIngredientFromMap(AMOUNT_OF_SALT_PER_MEAL, tableSaltAR, true); //TABLE_SALT, true);//
		Storage.storeAnResource(dryMassPerServing, foodAR, inv); //LifeSupportType.FOOD, inv);//AmountResource.
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
	public double getFullHeatRequired() {
		return 0;
	}

	@Override
	public double getPoweredDownHeatRequired() {
		return 0;
	}

	public static List<AmountResource> getOilMenuARList() {
		return oilMenuAR;
	}

    @Override
    public void destroy() {
        super.destroy();
        inv = null;
        oilMenuAR = null;
        //cookedMeals.clear();
        cookedMeals = null;
        settlement = null;
        //dailyMealList.clear();
        //dailyMealList = null;
        aMeal = null;
        //mealConfigMealList.clear();
        mealConfigMealList = null;
/*
        //dryFoodAR = null;
        tableSaltAR = null;
        NaClOAR = null;
        greyWaterAR = null;
        //waterAR = null;
        foodWasteAR = null;
        //foodAR = null;
        solidWasteAR = null;
        napkinAR = null;
*/
        //cropTypeList.clear();
        cropTypeList = null;
    }


}