/**
 * Mars Simulation Project
 * Cooking.java
 * @version 3.07 2015-01-04
 * @author Scott Davis 				
 */
package org.mars_sim.msp.core.structure.building.function.cooking;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
    public static final double COOKED_MEAL_WORK_REQUIRED = 10D; // 10 milli-sols is 15 mins

    public static final double MAX_MEAL_PER_PERSON = 1.1;
    
    private boolean cookNoMore = false;
    
    // Data members
    private int cookCapacity;
    private List<CookedMeal> cookedMeals = new ArrayList<CookedMeal>();
    private double cookingWorkTime;
	private List<HotMeal> mealConfigMealList; // = new ArrayList<HotMeal>();
	@SuppressWarnings("unused")
	private int mealCounterPerSol = 0; // used in cookAHotMeal()
	private int dayCache = 1;
	
	// 2014-12-08 Added multimaps
	private Multimap<String, Integer> qualityMap;
	private Multimap<String, MarsClock> timeMap;
	//private Multiset<String> servingsSet;

    @SuppressWarnings("unused")
	private int numOfCookedMealCache; // still in use in timePassing()
    private Inventory inv ;
    
    private HotMeal aMeal;
    //private int availableNumOfMeals;

	// 2014-12-12 Added the followings:
    private double dryWeightPerMeal;
    private AmountResource dryFoodAR;
    private static int NUMBER_OF_MEAL_PER_SOL = 4;
    private List<CropType> cropTypeList;
    
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
        //inv = getBuilding().getInventory();
        inv = getBuilding().getBuildingManager().getSettlement().getInventory();
        
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
           	
    	// 2014-12-12 Moved these from timePassing() to constructor
        PersonConfig personConfig = SimulationConfig.instance().getPersonConfiguration();
        dryFoodAR = AmountResource.findAmountResource(org.mars_sim.msp.core.LifeSupport.FOOD);
        dryWeightPerMeal = personConfig.getFoodConsumptionRate() * (1D / NUMBER_OF_MEAL_PER_SOL);
        
       	// 2014-12-12 Added computeDryWeight()
        computeDryWeight();
    }
    
    // 2014-12-12 Created computeDryWeight(). Called out once only in Cooking.java's constructor
    public void computeDryWeight() {
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
		        //System.out.println(ingredientName + "'s proportion is " + proportion);
		        proportionList.add(proportion);
		            	
		        // get totalDryWeight
				double waterContent = getWaterContent(ingredientName);
				//System.out.println(ingredientName + "'s water content is "+ waterContent);
		        waterContentList.add(waterContent);
	        }
	              
	        // get total dry weight (sum of each ingredient's dry weight) for a meal
	        double totalDryWeight = 0;
	        int k;
	        for(k = 1; k < waterContentList.size(); k++)
	        	totalDryWeight += waterContentList.get(k) + proportionList.get(k) ;
	        //System.out.println("totalDryWeight is " + totalDryWeight);

	        // get this fractional number
	        double fraction = 0;
	        fraction = dryWeightPerMeal / totalDryWeight;
	        //System.out.println("fraction is " + fraction);
	        
		    // get ingredientDryWeight for each ingredient
	        double ingredientDryWeight = 0;
	        int l;
	        for(l = 0; l < ingredientList.size(); l++) {
	        	ingredientDryWeight = fraction * waterContentList.get(l) + proportionList.get(l) ;
	        	ingredientDryWeight = Math.round(ingredientDryWeight* 1000000.0) / 1000000.0; // round up to 0.0000001 or 1mg
	        	//System.out.println("ingredientDryWeight is " + ingredientDryWeight);
	        	aMeal.setIngredientDryWeight(l, ingredientDryWeight);  
	        }

    	} // end of while (i.hasNext()) 	
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
    	if (cookedMeals == null)
    		size = 0;
    	else 
    		size = cookedMeals.size();
    	//System.out.println(" cookedMealList.size() is " + size);
        return (size > 0);
    }

    /**
     * Gets the number of cooked meals in this facility.
     * @return number of meals
     */
    public int getNumberOfCookedMeals() {
        return cookedMeals.size();
    }

    /**
     * Gets a cooked meal from this facility.
     * @return the meal
     */
    // Called by EatMeal.java's constructor
    public CookedMeal getCookedMeal() {
        CookedMeal bestMeal = null;
        int bestQuality = -1;
        Iterator<CookedMeal> i = cookedMeals.iterator();
        while (i.hasNext()) {
            CookedMeal meal = i.next();
            if (meal.getQuality() > bestQuality) {
                bestQuality = meal.getQuality();
                bestMeal = meal;
            }
        }

        if (bestMeal != null) cookedMeals.remove(bestMeal);

        return bestMeal;
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
 	        if (checkAmountAvailable(meal)) {
 	            result.add(meal);
 	        }
 	    }
 	    
 	    return result;
 	}
	/*
 	public void setCookNoMore(boolean value) {
 		cookNoMore = value;
 	}
 	*/
 	
    // 2015-01-04a Added getCookNoMore()
 	public boolean getCookNoMore() {
 		return cookNoMore;
 	}
 	
 	
    /**
     * Adds cooking work to this facility. 
     * The amount of work is dependent upon the person's cooking skill.
     * @param workTime work time (millisols) 
     */
 	// Called by CookMeal.java
    public void addWork(double workTime) {
    	
    	cookingWorkTime += workTime;       
        //logger.info("addWork() : cookingWorkTime is " + Math.round(cookingWorkTime *100.0)/100.0);
        //logger.info("addWork() : workTime is " + Math.round(workTime*100.0)/100.0);

    	while ( cookingWorkTime >= COOKED_MEAL_WORK_REQUIRED ) {
	    	
      		boolean exit = false;
    		
            double population = getBuilding().getBuildingManager().getSettlement().getCurrentPopulationNum();
            double maxServings = population * MAX_MEAL_PER_PERSON;
    		
            double numServings = cookedMeals.size();	
            //System.out.println( " pop is " + population);
            //System.out.println( " numServings is " + numServings);
            //System.out.println( " maxServings is " + maxServings);
            
            if (numServings > maxServings)
            	cookNoMore = true;
    		
    		
	    	while (!cookNoMore) {
	    		aMeal = pickAMeal();
	    		if (aMeal != null) {
	    			cookAHotMeal(aMeal);
	    			cookNoMore = true;
	    			//exit = true;
	    		}
	    	}
    	}
    }
 

	
   /* 
    // 2014-12-01 Created getMealServings()  
	public int getMealServings(List<CookedMeal> mealList, CookedMeal meal) {
		int num = 0;
		String name = meal.getName();
		
		Iterator<CookedMeal> j = mealList.iterator();
			while (j.hasNext()) {
    			CookedMeal nowMeal = j.next();
    			String nowMealName = nowMeal.getName();
    			// count only those that have the same name
    			if (nowMealName == name) // use .equals() to compare value of a String
    				num++;
			}
		return num;
	}
	*/
		
    /**
     * Gets the amount of the food item in the whole settlement.
     * @return foodAvailable
     */
    // 2015-01-02 Added checkAmountAV
    public double checkAmountAV(String name) {
	    AmountResource foodAR = AmountResource.findAmountResource(name);  
		double foodAvailable = inv.getAmountResourceStored(foodAR, false);
		foodAvailable = Math.round(foodAvailable * 1000.0) / 1000.0;
		return foodAvailable;
	}
   
    /**
     * Gets the amount of the food item in the whole settlement.
     * @return dessertAvailable
     */
    // 2015-01-02 Modified getAnOil()
	public String getAnOil() {
		    
	    	List<String> oilList = new ArrayList<String>();

	 	    if (checkAmountAV("Soybean Oil") > 0.05)
	 	    	oilList.add("Soybean Oil");
	 	    if (checkAmountAV("Garlic Oil") > 0.05)
	 	    	oilList.add("Garlic Oil");
	 	    if (checkAmountAV("Sesame Oil") > 0.05)
	 	    	oilList.add("Sesame Oil");
	 	    if (checkAmountAV("Peanut Oil") > 0.05)
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
	    	else if (upperbound == 0)
	    		selectedOil = "none";

	    	return selectedOil;
		}
    
	
    // 2014-11-29 Created checkAmountAvailable()
    public boolean checkAmountAvailable(HotMeal aMeal) {
    	boolean result = true;

       	List<Ingredient> ingredientList = aMeal.getIngredientList();
        Iterator<Ingredient> i = ingredientList.iterator();
        
        //List<Double> proportionList = new ArrayList<Double>();
        //int ii = 0;
        while (i.hasNext()) {
        	
	        Ingredient oneIngredient;
	        oneIngredient = i.next();
	        String ingredientName = oneIngredient.getName();
	        double dryWeight = oneIngredient.getDryWeight();
	        //proportionList.add(proportion);
	            	
	        AmountResource ingredientAR = getFreshFoodAR(ingredientName);
	        double ingredientAvailable = getFreshFood(ingredientAR);
               
	        // set the safe threshold as dryWeight * 3 
	        if (ingredientAvailable > dryWeight * 3 )  {
	        	oneIngredient.setIsItAvailable(true);
	        	result = result && true;	        
	        }
	        else { 
	        	oneIngredient.setIsItAvailable(false);
                //logger.info(ingredientName + 
                //"  Required : " + amount + 
                //"  Remaining : " +  ingredientAvailable); 
                result = false;
                }       
	        //ii++;
        }
        //logger.info(" result from checkAmountAvailable() : " + result);
		return result;
    }
    
    // 2014-11-29 Created cookAHotMeal()
    // 014-12-12 Revised to deduct the dry weight for each ingredient
    public void cookAHotMeal(HotMeal hotMeal) {

    	List<Ingredient> ingredientList = hotMeal.getIngredientList();    	
        //boolean isAmountAV = checkAmountAvailable(hotMeal);
        //if (isAmountAV) {
	    Iterator<Ingredient> i = ingredientList.iterator();
	        
	        while (i.hasNext()) {
	        	
		        Ingredient oneIngredient;
		        oneIngredient = i.next();
		        String ingredientName = oneIngredient.getName();
		        // 2014-12-11 Updated to using dry weight
		        double dryWeight = oneIngredient.getDryWeight();  
		        AmountResource ingredientAR = getFreshFoodAR(ingredientName);
		        inv.retrieveAmountResource(ingredientAR, dryWeight);
	         
	        } // end of while
	        
	        // 2014-12-29 Added oil and salt
		    String oil = getAnOil();
		    double oilAmount = .05;
		    String salt = "Table Salt";
		    double saltAmount = .01;
		    if (!oil.equals("None")) {
		        AmountResource oilAR = getFreshFoodAR(oil);
		        // TODO: Change the hardcoded oilAmount to what's on the meal recipe.xml
		        inv.retrieveAmountResource(oilAR, oilAmount);
		    }
		    
		    AmountResource saltAR = getFreshFoodAR(salt);
		    double saltAvailable = getFreshFood(saltAR);
		        // TODO: Change the hardcoded oilAmount to what's on the meal recipe.xml
			if (saltAvailable > saltAmount) {
		        inv.retrieveAmountResource(saltAR, saltAmount);
		    }    
    
	    	String nameOfMeal = hotMeal.getMealName();
	    	//TODO: kitchen equipment and quality of food should affect mealQuality
	       	int mealQuality = getBestCookSkill();
	        MarsClock expiration = (MarsClock) Simulation.instance().getMasterClock().getMarsClock().clone();
	        //int id = 0;
	        //if (cookedMeals == null) 
	        //	id = 0;
	        //else id = cookedMeals.size();
	        
	        CookedMeal meal = new CookedMeal(nameOfMeal, mealQuality, expiration);
	        //logger.info("a new meal made : " + meal.getName());    
	    	cookedMeals.add(meal);
	    	int size = cookedMeals.size();
	    	mealCounterPerSol++;
	    	//logger.info("# of available meals : " + size);    
	    	//logger.info(mealCounterPerSol + " meals made today");    

	    	// 2014-12-08 Added to Multimaps
	    	qualityMap.put(nameOfMeal, mealQuality);
	    	timeMap.put(nameOfMeal, expiration);
	    	
	  	    if (logger.isLoggable(Level.FINEST)) {
	  	        	logger.finest(getBuilding().getBuildingManager().getSettlement().getName() + 
	  	        			" has " + cookedMeals.size() + " meal(s) with quality score of " + mealQuality);
	  	    }
	        //logger.info(getBuilding().getBuildingManager().getSettlement().getName() + 
  	        //			" has " + meals.size() + " meal(s) and quality is " + mealQuality);
	        //logger.info(" BestMealQuality : " + getBestMealQuality());      
	  	    cookingWorkTime -= COOKED_MEAL_WORK_REQUIRED; 	        
        //}
    }
    
    // 2014-12-01 Added getCookedMealList()
    public List<CookedMeal> getCookedMealList() {
    	return cookedMeals;
    }

    /**
     * Gets the amount resource of the fresh food from a specified food group. 
     * 
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
        // 2014-11-29 Deleted the rounding or java.lang.IllegalStateException
        //return Math.round(freshFoodAvailable* 10000.0) / 10000.0;
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
    // 2014-10-08: Currently converting each unit of expired meal into 0.5 kg of packed food 
    // 2014-11-28 Added anyMeal for checking if any CookedMeal exists
    public void timePassing(double time) {

    	boolean hasAMeal = hasCookedMeal(); 
	     //logger.info(" hasAMeal : "+ hasAMeal);
	     if ( hasAMeal ) {
	         int newNumOfCookedMeal = cookedMeals.size();
	         //if ( numOfCookedMealCache != newNumOfCookedMeal)
	         //	logger.info("Still has " + newNumOfCookedMeal +  " CookedMeal(s)" );
	         Iterator<CookedMeal> i = cookedMeals.iterator();
	         while (i.hasNext()) {
	            CookedMeal meal = i.next();
	            //logger.info("CookedMeal : " + meal.getName());
	            MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
	             
	            // Added 2014-12-08 : Sanity check for the passing of each day
	            int newDay = currentTime.getSolOfMonth();
	            if ( newDay != dayCache) {
	            	// reset back to zero at the beginning of a new day.
	            	//System.out.println("Sol : " + newDay );// + settlement);
	            	
	            	mealCounterPerSol = 0;
	            	if (!timeMap.isEmpty()) {
	    				timeMap.clear();
	    			}
	    			if (!qualityMap.isEmpty()) {
	    				qualityMap.clear();	
	    			}
	            	dayCache = newDay;
	            }
	         
	            // Move expired meals back to food again (refrigerate leftovers).
	             if (MarsClock.getTimeDiff(meal.getExpirationTime(), currentTime) < 0D) {
	                try {
	                	
	      	            double foodCapacity = inv.getAmountResourceRemainingCapacity(dryFoodAR, false, false);
	                    if (dryWeightPerMeal > foodCapacity) 
	                    	dryWeightPerMeal = foodCapacity;
	                			//logger.info("timePassing() : pack & convert expired meal into dried food");
	                			// Turned 1 cooked meal unit into 1 food unit
	                    dryWeightPerMeal = Math.round( dryWeightPerMeal * 1000000.0) / 1000000.0;
	                    // remove the cookedMeal and store it
	                    inv.storeAmountResource(dryFoodAR, dryWeightPerMeal , false);
	                    //logger.info("TimePassing() : Refrigerate " + dryWeightPerMeal + " kg " + dryFoodAR.getName());
	                    i.remove();
	 
	                    if(logger.isLoggable(Level.FINEST)) {
	                        logger.finest("No one is eating " + meal.getName() + ". Thermostabilize it into dry food at " + 
	                                getBuilding().getBuildingManager().getSettlement().getName());
	                    }
	                }
	                catch (Exception e) {}
	            }
	        }
	         numOfCookedMealCache = newNumOfCookedMeal;
    	}
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

        cookedMeals.clear();
        cookedMeals = null;
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
	
    // 2014-12-07 Added nameMap
    //public Multimap<String, Integer> getNameMap() {
    //	return nameMap;
    //};
    
    // 2014-12-08 Added servingsSet
    //public Multiset<String> getServingsSet() {
	//	return servingsSet;
    //};
}