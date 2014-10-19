/**
 * Mars Simulation Project
 * Cooking.java
 * @version 3.07 2014-10-15
 * @author Scott Davis
 *
 * 2014-10-15 mkung: Fixed the crash by checking if there is any food available
 * 	Added new method checkAmountOfFood() for CookMeal.java to call ahead of time to 
 *  see if new crop harvest comes in.
 *  				
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private boolean foodisAvailable = true;
    private int cookCapacity;
    private List<CookedMeal> meals;
    private double cookingWorkTime;

    /**
     * Constructor.
     * @param building the building this function is for.
     * @throws BuildingException if error in constructing function.
     */
    public Cooking(Building building) {
        // Use Function constructor.
        super(FUNCTION, building);

        cookingWorkTime = 0D;
        meals = new ArrayList<CookedMeal>();

        BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();

        this.cookCapacity = config.getCookCapacity(building.getName());

        // Load activity spots
        loadActivitySpots(config.getCookingActivitySpots(building.getName()));
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
            if (!newBuilding && building.getName().equalsIgnoreCase(buildingName) && !removedBuilding) {
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
            if (meal.getQuality() > bestQuality) bestQuality = meal.getQuality();
        }

        return bestQuality;
    }

    /**
     * Cleanup kitchen after mealtime.
     */
    public void cleanup() {
        cookingWorkTime = 0D;
    }

    /**
     * Adds cooking work to this facility. 
     * The amount of work is dependent upon the person's cooking skill.
     * @param workTime work time (millisols)
     * 2014-10-08 mkung: rewrote this function to highlight the while loop. 
     * 					moved remaining tasks into a new method cookingChoice()
     * 2014-10-15 mkung: Fixed the no available food crash by checking if the total food available
     *  				is more than 0.5 kg, 
     */
    public void addWork(double workTime) {
        //logger.info("addWork() : cookingWorkTime is " + cookingWorkTime);
        //logger.info("addWork() : workTime is " + workTime);
    	cookingWorkTime += workTime;       
        //logger.info("addWork() : cookingWorkTime is " + cookingWorkTime);
        //logger.info("addWork() : workTime is " + workTime);
        
    	// check if there are new harvest, if it does, set foodisAvailable to true
    	double foodAvailable = checkAmountOfFood();
    	
    	if (foodAvailable >= 0.5) 
    		foodisAvailable = true;
    	
     	while ((cookingWorkTime >= COOKED_MEAL_WORK_REQUIRED) && (foodisAvailable) ){      	
            cookingChoice();
        } // end of while
     } // end of void addWork()
    
    public double checkAmountOfFood() {

        AmountResource fruits = AmountResource.findAmountResource("Fruit Group");
        double fruitsAvailable = getBuilding().getInventory().getAmountResourceStored(fruits, false);
        AmountResource grains = AmountResource.findAmountResource("Grain Group");
        double grainsAvailable = getBuilding().getInventory().getAmountResourceStored(grains, false);
        AmountResource legumes = AmountResource.findAmountResource("Legume Group");
        double legumesAvailable = getBuilding().getInventory().getAmountResourceStored(legumes, false);
        AmountResource spices = AmountResource.findAmountResource("Spice Group");
        double spicesAvailable = getBuilding().getInventory().getAmountResourceStored(spices, false);
        AmountResource veg = AmountResource.findAmountResource("Vegetable Group");
        double vegAvailable = getBuilding().getInventory().getAmountResourceStored(veg, false);
        
        // totalAV  = vegAV + legumeAV +...
        return fruitsAvailable + grainsAvailable + legumesAvailable + spicesAvailable + vegAvailable;
   	
    }
    
    
    
    /**
     * Orders of cooking 
     * @param none
     * 2014-10-08 mkung: 1st choice: making vegetables soup / make salad bowl 
     * 					 2nd cooked fry rice / high-fiber wheat bread sandwich
    // TODO: let the cook choose what kind of meal to cook based on his preference
    // TODO: create a method to make the codes more compact 
     */
    public void  cookingChoice() {
    	
        int mealQuality = getBestCookSkill();
        MarsClock time = (MarsClock) Simulation.instance().getMasterClock().getMarsClock().clone();

        PersonConfig config = SimulationConfig.instance().getPersonConfiguration();
        double foodAmount = config.getFoodConsumptionRate() * (1D / 3D);   
       	
        AmountResource fruits = AmountResource.findAmountResource("Fruit Group");
        double fruitsAvailable = getBuilding().getInventory().getAmountResourceStored(fruits, false);
        AmountResource grains = AmountResource.findAmountResource("Grain Group");
        double grainsAvailable = getBuilding().getInventory().getAmountResourceStored(grains, false);
        AmountResource legumes = AmountResource.findAmountResource("Legume Group");
        double legumesAvailable = getBuilding().getInventory().getAmountResourceStored(legumes, false);
        AmountResource spices = AmountResource.findAmountResource("Spice Group");
        double spicesAvailable = getBuilding().getInventory().getAmountResourceStored(spices, false);
        AmountResource veg = AmountResource.findAmountResource("Vegetable Group");
        double vegAvailable = getBuilding().getInventory().getAmountResourceStored(veg, false);
        
        double totalAvailable = fruitsAvailable + grainsAvailable + legumesAvailable + spicesAvailable + vegAvailable;
       	
        
        //double totalAvailable = checkAmountOfFood();
        
        // 2014-10-15 mkung: Checked if the total food available is more than 0.5 kg food in total
        if (totalAvailable > 0.5) { 
        	//foodAvailable = true;
       
	        double fruitsFraction = foodAmount * fruitsAvailable / totalAvailable;
	        double grainsFraction = foodAmount * grainsAvailable / totalAvailable;
	        double legumesFraction = foodAmount * legumesAvailable / totalAvailable;
	        double spicesFraction = foodAmount * spicesAvailable / totalAvailable;
	        double vegFraction = foodAmount * vegAvailable / totalAvailable;
	        
	        //logger.info("cookingChoice() : total Food Available is " + Math.round(totalAvailable) + " kg");
	
	        //logger.info("cookingChoice() : amount to cook is " + foodAmount + " kg");
	
	        
	        getBuilding().getInventory().retrieveAmountResource(fruits, fruitsFraction);
	        //	System.out.println("Cooking.java : addWork() : cooking vegetables using "  
	      	//		+ foodAmount + ", vegetables remaining is " + (foodAvailable-foodAmount) );
	        meals.add(new CookedMeal(mealQuality, time));
	        
	        //logger.info("cookingChoice() : meals.size() is " + meals.size() );
	        
	        if (logger.isLoggable(Level.FINEST)) {
	        	logger.finest(getBuilding().getBuildingManager().getSettlement().getName() + 
	        			" has prepared " + meals.size() + " delicious fruits (quality is " + mealQuality + ")");
	        }
	  
	        getBuilding().getInventory().retrieveAmountResource(grains, grainsFraction);
	        //	System.out.println("Cooking.java : addWork() : cooking vegetables using "  
	      	//		+ foodAmount + ", vegetables remaining is " + (foodAvailable-foodAmount) );
	        meals.add(new CookedMeal(mealQuality, time));
	        if (logger.isLoggable(Level.FINEST)) {
	        	logger.finest(getBuilding().getBuildingManager().getSettlement().getName() + 
	        			" has stir-fried or cooked " + meals.size() + " rice or pasta (quality is " + mealQuality + ")");
	        }
	  
	        getBuilding().getInventory().retrieveAmountResource(legumes, legumesFraction);
	        //	System.out.println("Cooking.java : addWork() : cooking vegetables using "  
	      	//		+ foodAmount + ", vegetables remaining is " + (foodAvailable-foodAmount) );
	        meals.add(new CookedMeal(mealQuality, time));
	        if (logger.isLoggable(Level.FINEST)) {
	        	logger.finest(getBuilding().getBuildingManager().getSettlement().getName() + 
	        			" has prepared a side dish with " + meals.size() + " beans and/or peas (quality is " + mealQuality + ")");
	        }
	  
	        getBuilding().getInventory().retrieveAmountResource(spices, spicesFraction);
	        //	System.out.println("Cooking.java : addWork() : cooking vegetables using "  
	      	//		+ foodAmount + ", vegetables remaining is " + (foodAvailable-foodAmount) );
	        meals.add(new CookedMeal(mealQuality, time));
	        if (logger.isLoggable(Level.FINEST)) {
	        	logger.finest(getBuilding().getBuildingManager().getSettlement().getName() + 
	        			" has seasoned the dish with " + meals.size() + " herbs and spices (quality is " + mealQuality + ")");
	        }
	  
	        getBuilding().getInventory().retrieveAmountResource(veg, vegFraction);
	        //	System.out.println("Cooking.java : addWork() : cooking vegetables using "  
	      	//		+ foodAmount + ", vegetables remaining is " + (foodAvailable-foodAmount) );
	        meals.add(new CookedMeal(mealQuality, time));
	        if (logger.isLoggable(Level.FINEST)) {
	        	logger.finest(getBuilding().getBuildingManager().getSettlement().getName() + 
	        			" has mixed a salad bowl with " + meals.size() + " fresh vegetables (quality is " + mealQuality + ")");
	        }
	 
	        //logger.info("cookingChoice() : cookingWorkTime is " + cookingWorkTime);
	        //logger.info("cookingChoice() : COOKED_MEAL_WORK_REQUIRED is " + COOKED_MEAL_WORK_REQUIRED);
	
	        cookingWorkTime -= COOKED_MEAL_WORK_REQUIRED; 
	        
	        //logger.info("cookingChoice() : cookingWorkTime is " + cookingWorkTime);
	        //logger.info("cookingChoice() : COOKED_MEAL_WORK_REQUIRED is " + COOKED_MEAL_WORK_REQUIRED);
        } 
        else { 
        	foodisAvailable = false;
         	   logger.info("cookingChoice() : no more fresh food available for meal! Wait until the next harvest");        	
        }
    }

    /**
     * Time passing for the building.
     * @param time amount of time passing (in millisols)
     * @throws BuildingException if error occurs.
     * 2014-10-08: mkung - Packed expired meal into food (turned 1 meal unit into 1 food unit)
     */
    public void timePassing(double time) {

        // Move expired meals back to food again (refrigerate leftovers).
   
        Iterator<CookedMeal> i = meals.iterator();
        while (i.hasNext()) {
            CookedMeal meal = i.next();
            MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
            if (MarsClock.getTimeDiff(meal.getExpirationTime(), currentTime) < 0D) {
                try {
                    PersonConfig config = SimulationConfig.instance().getPersonConfiguration();
                    AmountResource food = AmountResource.findAmountResource("food");
                    double foodAmount = config.getFoodConsumptionRate() * (1D / 3D);
                    double foodCapacity = getBuilding().getInventory().getAmountResourceRemainingCapacity(
                            food, false, false);
                    if (foodAmount > foodCapacity) 
                    	foodAmount = foodCapacity;
                			//logger.info("timePassing() : pack & convert .5 kg expired meal into .5 kg food");
                			// Turned 1 cooked meal unit into 1 food unit
                    getBuilding().getInventory().storeAmountResource(food, foodAmount , false);
                    i.remove();

                    if(logger.isLoggable(Level.FINEST)) {
                        logger.finest("Cooked meal expiring at " + 
                                getBuilding().getBuildingManager().getSettlement().getName());
                    }
                }
                catch (Exception e) {}
            }
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