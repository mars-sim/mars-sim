/**
 * Mars Simulation Project
 * Cooking.java
 * @version 3.07 2014-10-08
 * @author Scott Davis
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
     */
    public void addWork(double workTime) {
        cookingWorkTime += workTime;
        
        while (cookingWorkTime >= COOKED_MEAL_WORK_REQUIRED) {
        	
            cookingChoice();
            
        } // end of while
     } // end of void addWork()
    
    
    /**
     * Orders of cooking 
     * @param none
     * 2014-10-08 mkung: 1st choice: making vegetables soup / make salad bowl 
     * 					 2nd cooked fry rice / high-fiber wheat bread sandwich
    // TODO: let the cook choose what kind of meal to cook based on his preference
     */
    public void  cookingChoice() {
    	
        int mealQuality = getBestCookSkill();
        MarsClock time = (MarsClock) Simulation.instance().getMasterClock().getMarsClock().clone();

        PersonConfig config = SimulationConfig.instance().getPersonConfiguration();
        double foodAmount = config.getFoodConsumptionRate() * (1D / 3D);

            	 AmountResource food = AmountResource.findAmountResource("vegetables");
                 double foodAvailable = getBuilding().getInventory().getAmountResourceStored(food, false);
                 if (foodAmount <= foodAvailable) {
                       getBuilding().getInventory().retrieveAmountResource(food, foodAmount);
                              //	System.out.println("Cooking.java : addWork() : cooking vegetables using "  
                            	//		+ foodAmount + ", vegetables remaining is " + (foodAvailable-foodAmount) );
                     meals.add(new CookedMeal(mealQuality, time));
                     if (logger.isLoggable(Level.FINEST)) {
                         logger.finest(getBuilding().getBuildingManager().getSettlement().getName() + 
                        " has prepared " + meals.size() + " vegetable soup/salad bowl (quality is " + mealQuality + ")");
                     }
                 } // end of if
                 else {
                	// 2nd choice :  cook grains 
                	 AmountResource food2 = AmountResource.findAmountResource("grains");
                     double foodAvailable2 = getBuilding().getInventory().getAmountResourceStored(food2, false);
                     if (foodAmount <= foodAvailable2) {
                         getBuilding().getInventory().retrieveAmountResource(food2, foodAmount);
                     	//System.out.println("Cooking.java : addWork() : cooking grains, using " 
                    		//	+ foodAmount + ", grains remaining is "+ (foodAvailable2-foodAmount));
                         meals.add(new CookedMeal(mealQuality, time));
                         if (logger.isLoggable(Level.FINEST)) {
                             logger.finest(getBuilding().getBuildingManager().getSettlement().getName() + 
                             " has prepared " + meals.size() + " fry rice / wheat bread sandwich (quality is " + mealQuality + ")");
                         }
                     } // end of if
                     
                     else {
                        // 3rd choice:  Dry/canned food 
                         AmountResource food3 = AmountResource.findAmountResource("food");
                         double foodAvailable3 = getBuilding().getInventory().getAmountResourceStored(food3, false);
                         if (foodAmount <= foodAvailable3) {
                             getBuilding().getInventory().retrieveAmountResource(food3, foodAmount);
                          	//System.out.println("Cooking.java : addWork() : cooking food, using "
                        	//		+ foodAmount + ", food remaining is "  + (foodAvailable3-foodAmount));             	
                             meals.add(new CookedMeal(mealQuality, time));
                             if (logger.isLoggable(Level.FINEST)) {
                                 logger.finest(getBuilding().getBuildingManager().getSettlement().getName() + 
                                 " has prepared " + meals.size() + " hot meals from canned food (quality is " + mealQuality + ")");
                             }
                         } // end of if
                         else {
                        	 //System.out.println("Cooking.java : addWork() : not enough food left. remaing is " + foodAvailable3);
                             Settlement settlement = getBuilding().getBuildingManager().getSettlement();
                             logger.info("Not enough food to cook meal at " + settlement.getName() + 
                            		 " - remaining food available: " + foodAvailable3);
                         } // end of else #3
                         cookingWorkTime -= COOKED_MEAL_WORK_REQUIRED;     
                 } // end of else #2
                     cookingWorkTime -= COOKED_MEAL_WORK_REQUIRED; 
                 } // end of else #1
                 cookingWorkTime -= COOKED_MEAL_WORK_REQUIRED; 
    }

    /**
     * Time passing for the building.
     * @param time amount of time passing (in millisols)
     * @throws BuildingException if error occurs.
     * 2014-10-08: mkung - Packed expired meal into food (turned 1 meal unit into 2 food units)
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
                			//System.out.println("Cooking.java : timePassing() : packing uneated food into storage, 2 * foodAmount is " + foodAmount*2 );
                			// Turned 1 cooked meal unit into 2 food units
                    getBuilding().getInventory().storeAmountResource(food, foodAmount * 2, false);
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
    public double getPowerDownPowerRequired() {
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
}