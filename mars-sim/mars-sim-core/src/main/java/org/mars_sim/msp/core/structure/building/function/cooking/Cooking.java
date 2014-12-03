/**
 * Mars Simulation Project
 * Cooking.java
 * @version 3.07 2014-12-02
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
import org.mars_sim.msp.core.structure.building.function.Function;
import org.mars_sim.msp.core.structure.building.function.LifeSupport;
import org.mars_sim.msp.core.time.MarsClock;

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

    /** The base amount of work time (cooking skill 0) to produce a cooked meal. */
    public static final double COOKED_MEAL_WORK_REQUIRED = 20D;

    // Data members
    //private boolean foodIsAvailable = true;
    //private boolean soyIsAvailable = true;
    private int cookCapacity;
    private List<CookedMeal> meals;
    private double cookingWorkTime;
    
    private int numOfCookedMealCache; // in use in timePassing()
    private Inventory inv ;
    
    private HotMeal aMeal;

    /**
     * Constructor.
     * @param building the building this function is for.
     * @throws BuildingException if error in constructing function.
     */
    public Cooking(Building building) {
        // Use Function constructor.
        super(FUNCTION, building);
        
        inv = getBuilding().getInventory();
        //logger.info("just called Cooking's constructor");
        
        cookingWorkTime = 0D;
        meals = new ArrayList<CookedMeal>();

        BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();

        this.cookCapacity = config.getCookCapacity(building.getBuildingType());

        // Load activity spots
        loadActivitySpots(config.getCookingActivitySpots(building.getBuildingType()));
    
    }

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
        return (meals.size() > 0);
    }

    /**
     * Gets the number of cooked meals in this facility.
     * @return number of meals
     */
    public int getNumberOfCookedMeals() {
        return meals.size();
    }

    /**
     * Gets a cooked meal from this facility.
     * @return the meal
     */
    public CookedMeal getCookedMeal() {
        CookedMeal bestMeal = null;
        int bestQuality = -1;
        Iterator<CookedMeal> i = meals.iterator();
        while (i.hasNext()) {
            CookedMeal meal = i.next();
            if (meal.getQuality() > bestQuality) {
                bestQuality = meal.getQuality();
                bestMeal = meal;
            }
        }

        if (bestMeal != null) meals.remove(bestMeal);

        return bestMeal;
    }

    /**
     * Gets the quality of the best quality meal at the facility.
     * @return quality
     */
    public int getBestMealQuality() {
        int bestQuality = 0;
        Iterator<CookedMeal> i = meals.iterator();
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
    }

    // 2014-11-30 Added pickAMeal() 
 	public HotMeal pickAMeal() {

 	  	int upperbound = 9;
    	int lowerbound = 0;

    	int number = ThreadLocalRandom.current().nextInt(upperbound + 1);
    	//int index = ThreadLocalRandom.current().nextInt(10); // 0 to 9
    	//logger.info(" random # is " + number);
 		
	 	HotMeal aMeal = new HotMeal(this);
    	
    	if (number == 0) {
    	
	    	aMeal.addMealName("Kidney Bean Fried Rice with Onion");
	    	aMeal.add("Kidney Bean", .19);
	      	aMeal.add("White Rice", .20);
	      	aMeal.add(aMeal.getAvailableOil(), .02);
	      	aMeal.add("White Onion", .08);
	      	aMeal.add("Table Salt", .01);
	      	
    	} else if (number == 1) {
    	
	    	aMeal.addMealName("Carrot Soup");
	    	aMeal.add("Cabbage", .19);
	      	aMeal.add("Carrot", .17);
	      	aMeal.add("Sesame", .01);
	      	aMeal.add(aMeal.getAvailableOil(), .02);
	      	aMeal.add("Green Onion", .05);
	      	aMeal.add("Tomato", .05);
	      	aMeal.add("Table Salt", .01);
	      	
	      	
    	}  else if (number == 2) {
    	
    		// Garnish the Coleslaw with toasted sesame seeds/chopped 
			//roasted peanuts and shredded carrot and cabbage 
    		
	    	aMeal.addMealName("Cabbage & Carrot Slaw Plate");
	    	aMeal.add("Cabbage", .20);
	      	aMeal.add("Carrot", .20);
	      	aMeal.add(aMeal.getAvailableOil(), .03);
	      	aMeal.add("Sesame", .06);
	      	aMeal.add("Table Salt", .01);
	      	
    	} else if (number == 3) {
    	
    		// clove garlic, finely chopped, shredded carrots
    		// thinly sliced green onions
    		//	Ground black pepper, to taste
    		
	    	aMeal.addMealName("Roasted Garlic, Potato & Lettuce Salad");
	    	aMeal.add("Potato", .22);
	      	aMeal.add("Lettuce", .12);
	      	aMeal.add("Garlic", .08);
	      	aMeal.add("Sesame", .04);
	      	aMeal.add(aMeal.getAvailableOil(), .03);
	      	aMeal.add("Table Salt", .01);
		      	
	    	
		} else if (number == 4) {
	    	
			// 
	    	aMeal.addMealName("Bean Sprout Garlic Stir Fry");
	    	aMeal.add("Soy Sprout", .27);
	      	aMeal.add("Garlic", .04);
	      	aMeal.add("Green Onion", .15);
	      	aMeal.add(aMeal.getAvailableOil(), .03);
	      	aMeal.add("Table Salt", .01);
	      	
		} 
    	
			else if (number == 5) {
	    	
			// 
	    	aMeal.addMealName("Veggie Burger & French Fries");
	    	aMeal.add("Wheat Bun", .05);
	      	aMeal.add("Veggie Patty", .15);
	      	aMeal.add("French Fries", .10);	      	
	      	aMeal.add("Lettuce", .10);
	      	aMeal.add("Tomato", .08);
	      	aMeal.add("White Onion", .02);

		} 
    	
			else if (number == 6) {
		    	
				// 
		    	aMeal.addMealName("Sauteed Swiss Chard w/ Onions Fried Rice");
		    	aMeal.add("Swiss Chard", .25);
		      	aMeal.add("Carrot", .05);
		      	aMeal.add("Garlic", .05);
		      	aMeal.add("White Rice", .13);
		      	aMeal.add("White Onion", .02);

			} 
    	
			else if (number == 7) {
		    	
				// 
		    	aMeal.addMealName("Veggie Sandwich & Peas");
		    	aMeal.add("White Bread", .5);
		      	aMeal.add("Veggie Patty", .15);
		      	aMeal.add("Peas", .09);	      	
		      	aMeal.add("Lettuce", .1);
		      	aMeal.add("Tomato", .08);
		      	aMeal.add("White Onion", .02);
		      	aMeal.add("Table Salt", .01);

			} 
    	
			else if (number == 8) {
		    	
				// 3 stalks celery, rinsed and trimmed, leaves chopped
		    	aMeal.addMealName("Braised Celery with Carrot and Peanuts");
		    	aMeal.add("Celery", .25);
		      	aMeal.add("Carrot", .15);
		      	aMeal.add("Peanut", .09);
		      	aMeal.add("Table Salt", .01);

			} 
		
			else if (number == 9) {
		    	
				// 
		    	aMeal.addMealName("Tofu Menudo");
		    	aMeal.add("Tofu", .15);
		      	aMeal.add("Carrot", .12);
		      	aMeal.add("Potato", .1);
		      	aMeal.add("Tomato", .05);		      	
		      	aMeal.add("Peas", .05);
		      	aMeal.add("White Onion", .02);
		      	aMeal.add("Table Salt", .01);
			} 
    	//logger.info(" meal# is " + number);
    	
    	return aMeal;
	}
	
    /**
     * Adds cooking work to this facility. 
     * The amount of work is dependent upon the person's cooking skill.
     * @param workTime work time (millisols) 
     */
    // 2014-10-08 Rewrote this function to highlight the while loop. 
    // 					moved remaining tasks into a new method cookingChoice()
    // 2014-10-15 Fixed the no available food crash by checking if the total food available
    //  				is more than 0.5 kg
 	// 2014-11-30 Added new features to cooking by calling pickAMeal() and cookAHotMeal()
    public void addWork(double workTime) {
    	cookingWorkTime += workTime;       
        //logger.info("addWork() : cookingWorkTime is " + Math.round(cookingWorkTime *100.0)/100.0);
        //logger.info("addWork() : workTime is " + Math.round(workTime*100.0)/100.0);
  
    	while ( cookingWorkTime >= COOKED_MEAL_WORK_REQUIRED ) {
 
    		boolean exit = false;
    		
    		// pick a meal whose ingredients are available
    		while (!exit) {
    			aMeal = pickAMeal();
    			boolean isAmountAV = checkAmountAvailable(aMeal);
    			if (isAmountAV) {
    				cookAHotMeal(aMeal);
    				exit = true;
    			}
    		}
    		
    	}
     	
     } // end of void addWork()
	
    // 2014-12-01 Created getMealServings()  
	public int getMealServings(List<CookedMeal> mealList, CookedMeal meal) {
		int num = 0;
		String name = meal.getName();
		
		Iterator<CookedMeal> j = meals.iterator();
			while (j.hasNext()) {
    			CookedMeal nowMeal = j.next();
    			String nowMealName = nowMeal.getName();
    			if (nowMealName == name)
    				num++;
			}
		return num;
	}


		
		public String getAnOil() {
			// 
			int upperbound = 4;
	    	int lowerbound = 0;
	    	String oilName = "";
	    	int index = ThreadLocalRandom.current().nextInt(upperbound + 1);
	    	//int index = ThreadLocalRandom.current().nextInt(10); // 0 to 9
	    	//int number = (int)(Math.random() * ((upperbound - lowerbound) + 1) + lowerbound);
	    	
	    	//HotMeal aMeal = new HotMeal();
	    	
	    	if (index == 0) {
	    		oilName = "Soybean Oil";
	    	} else if (index == 1) {
	    		oilName = "Garlic Oil";
	    	} else if (index == 2) {
	    		oilName = "Sesame Oil";
	    	} else if (index == 3) {
	    		oilName = "Peanut Oil";
	    	} else if (index == 4) {
	    		oilName = "Soybean Oil";
	    	} 
	
	    	//logger.info(" Oil is " + oilName);

	    	return oilName;

		}
    
    // 2014-11-29 Created checkAmountAvailable()
    public boolean checkAmountAvailable(HotMeal aMeal) {
    	boolean result = true;

       	List<Ingredient> ingredientList = aMeal.getIngredientList();
        
        Iterator<Ingredient> i = ingredientList.iterator();
        
        while (i.hasNext()) {
        	
	        Ingredient oneIngredient;
	        oneIngredient = i.next();
	        String ingredientName = oneIngredient.getName();
	        double amount = oneIngredient.getAmount();
	            	
	        AmountResource ingredientAR = getFreshFoodAR(ingredientName);
	        double ingredientAvailable = getFreshFood(ingredientAR);
               
	        if (ingredientAvailable > amount) 
	        	result = result && true;
	        else { 
                //logger.info(ingredientName + 
                //"  Required : " + amount + 
                //"  Remaining : " +  ingredientAvailable); 
                result = false;
                }
        }
        //logger.info(" result from checkAmountAvailable() : " + result);
		return result;
    }
    
    // 2014-11-29 Created cookAHotMeal()
    public void cookAHotMeal(HotMeal hotMeal) {

    	List<Ingredient> ingredientList = hotMeal.getIngredientList();    	
     	String nameOfMeal = hotMeal.getMealName();
        boolean isAmountAV = checkAmountAvailable(hotMeal);
        
        if (isAmountAV) {
        	
	        Iterator<Ingredient> i = ingredientList.iterator();
	        
	        while (i.hasNext()) {
	        	
		        Ingredient oneIngredient;
		        oneIngredient = i.next();
		        String ingredientName = oneIngredient.getName();
		        double amount = oneIngredient.getAmount();
		            	
		        AmountResource ingredientAR = getFreshFoodAR(ingredientName);
		        //double ingredientAvailable = getFreshFood(ingredientAR);
                
		        inv.retrieveAmountResource(ingredientAR, amount);
	         
	        } // end of while
	
	       	int mealQuality = getBestCookSkill();
	        MarsClock time = (MarsClock) Simulation.instance().getMasterClock().getMarsClock().clone();

	        CookedMeal meal = new CookedMeal(nameOfMeal, mealQuality, time);
	        //logger.info("New meal : " + meal.getName());    
	    	meals.add(meal);
	  	        
	  	    if (logger.isLoggable(Level.FINEST)) {
	  	        	logger.finest(getBuilding().getBuildingManager().getSettlement().getName() + 
	  	        			" has " + meals.size() + " meal(s) with quality score of " + mealQuality);
	  	    }
	        //logger.info(getBuilding().getBuildingManager().getSettlement().getName() + 
  	        //			" has " + meals.size() + " meal(s) and quality is " + mealQuality);
	        //logger.info(" BestMealQuality : " + getBestMealQuality());      
	  	    cookingWorkTime -= COOKED_MEAL_WORK_REQUIRED; 
	  	        
        }
        
    }
    
    // 2014-12-01 Added getCookedMealList()
    public List<CookedMeal> getCookedMealList() {
    	return meals;
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
         int newNumOfCookedMeal = meals.size();
         //if ( numOfCookedMealCache != newNumOfCookedMeal)
         //	logger.info("Still has " + newNumOfCookedMeal +  " CookedMeal(s)" );
         Iterator<CookedMeal> i = meals.iterator();
         while (i.hasNext()) {
            CookedMeal meal = i.next();
            //logger.info("CookedMeal : " + meal.getName());
            MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
             // Move expired meals back to food again (refrigerate leftovers).
             if (MarsClock.getTimeDiff(meal.getExpirationTime(), currentTime) < 0D) {
                try {
                    PersonConfig config = SimulationConfig.instance().getPersonConfiguration();
                    AmountResource food = AmountResource.findAmountResource(org.mars_sim.msp.core.LifeSupport.FOOD);
                    double foodAmount = config.getFoodConsumptionRate() * (1D / 3D);
                    double foodCapacity = inv.getAmountResourceRemainingCapacity(
                            food, false, false);
                    if (foodAmount > foodCapacity) 
                    	foodAmount = foodCapacity;
                			//logger.info("timePassing() : pack & convert .5 kg expired meal into .5 kg food");
                			// Turned 1 cooked meal unit into 1 food unit
                    foodAmount = Math.round( foodAmount * 1000.0) / 1000.0;
                    inv.storeAmountResource(food, foodAmount , false);
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

        meals.clear();
        meals = null;
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