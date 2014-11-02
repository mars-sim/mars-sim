/**
 * Mars Simulation Project
 * MakingSoy.java
 * @version 3.07 2014-10-31
 * @author Manny Kung				
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
 * The MakingSoy class is a building function for making soy products.
 */
public class MakingSoy
extends Function
implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** default logger. */
    private static Logger logger = Logger.getLogger(MakingSoy.class.getName());

    private static final BuildingFunction FUNCTION = BuildingFunction.COOKING;

    /** The base amount of work time (makingSoy skill 0) to produce a cooked meal. */
    public static final double COOKED_MEAL_WORK_REQUIRED = 20D;

    // Data members
    private boolean soyIsAvailable = true;
    private int cookCapacity;
    private List<CookedMeal> meals;
    private double makingSoyWorkTime;

    /**
     * Constructor.
     * @param building the building this function is for.
     * @throws BuildingException if error in constructing function.
     */
    public MakingSoy(Building building) {
        // Use Function constructor.
        super(FUNCTION, building);

        makingSoyWorkTime = 0D;
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

        // Demand is 1 makingSoy capacity for every five inhabitants.
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
                MakingSoy makingSoyFunction = (MakingSoy) building.getFunction(FUNCTION);
                double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
                supply += makingSoyFunction.cookCapacity * wearModifier;
            }
        }

        double makingSoyCapacityValue = demand / (supply + 1D);

        BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
        double makingSoyCapacity = config.getCookCapacity(buildingName);

        return makingSoyCapacity * makingSoyCapacityValue;
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
                        int makingSoySkill = person.getMind().getSkillManager().getEffectiveSkillLevel(SkillType.COOKING);
                        if (makingSoySkill > result) result = makingSoySkill;
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
        makingSoyWorkTime = 0D;
    }

    /**
     * Adds makingSoy work to this facility. 
     * The amount of work is dependent upon the person's makingSoy skill.
     * @param workTime work time (millisols)
     * 2014-10-08 mkung: rewrote this function to highlight the while loop. 
     * 					moved remaining tasks into a new method makingSoyChoice()
     * 2014-10-15 mkung: Fixed the no available food crash by checking if the total food available
     *  				is more than 0.5 kg, 
     */
    public void addWork(double workTime) {
    	makingSoyWorkTime += workTime;       
        //logger.info("addWork() : makingSoyWorkTime is " + makingSoyWorkTime);
        //logger.info("addWork() : workTime is " + workTime);
        
    	// check if there are new harvest, if it does, set soyIsAvailable to true
    	double soybeansAvailable = checkAmountOfSoybeans();
    	
    	if (soybeansAvailable >= 0.5) 
    		soyIsAvailable = true;
    	
     	while ((makingSoyWorkTime >= COOKED_MEAL_WORK_REQUIRED) && (soyIsAvailable) ){      	
            makingSoyChoice();
        } // end of while
     } // end of void addWork()
    
    public double checkAmountOfSoybeans() {

        AmountResource soybeans = AmountResource.findAmountResource("Soybean");
        double soybeansAvailable = getBuilding().getInventory().getAmountResourceStored(soybeans, false);
       
        // totalAV  = vegAV + legumeAV +...
        return soybeansAvailable ;
   	
    }
    
    
    
    /**
     * Orders of makingSoy 
     * @param none
     * 2014-10-08 mkung: 1st choice: making vegetables soup / make salad bowl 
     * 					 2nd cooked fry rice / high-fiber wheat bread sandwich
    // TODO: let the cook choose what kind of meal to cook based on his preference
    // TODO: create a method to make the codes more compact 
     */
    public void  makingSoyChoice() {
    	
        int mealQuality = getBestCookSkill();
        MarsClock time = (MarsClock) Simulation.instance().getMasterClock().getMarsClock().clone();

        PersonConfig config = SimulationConfig.instance().getPersonConfiguration();
        double soybeansAmount = config.getFoodConsumptionRate() * (1D / 3D);   
       	
        AmountResource soybeans = AmountResource.findAmountResource("Soybeans");
        double soybeansAvailable = getBuilding().getInventory().getAmountResourceStored(soybeans, false);
        
        double totalAvailable = soybeansAvailable;
       	
        if (totalAvailable > 0.5) { 
        	//foodAvailable = true;
       
	        double soybeansFraction = soybeansAvailable;
	        
	        //logger.info("makingSoyChoice() : total Food Available is " + Math.round(totalAvailable) + " kg");
	        //logger.info("makingSoyChoice() : amount to cook is " + foodAmount + " kg");
	
	        getBuilding().getInventory().retrieveAmountResource(soybeans, soybeansFraction);
	        //	System.out.println("makingSoy.java : addWork() : makingSoy vegetables using "  
	      	//		+ foodAmount + ", vegetables remaining is " + (foodAvailable-foodAmount) );
	        meals.add(new CookedMeal(mealQuality, time));
	        //logger.info("makingSoyChoice() : meals.size() is " + meals.size() );
	        
	        if (logger.isLoggable(Level.FINEST)) {
	        	logger.finest(getBuilding().getBuildingManager().getSettlement().getName() + 
	        			" has prepared " + meals.size() + " delicious fruits (quality is " + mealQuality + ")");
	        }
	  
	        getBuilding().getInventory().retrieveAmountResource(soybeans, soybeansFraction);
	        //	System.out.println("makingSoy.java : addWork() : makingSoy vegetables using "  
	      	//		+ foodAmount + ", vegetables remaining is " + (foodAvailable-foodAmount) );
	        meals.add(new CookedMeal(mealQuality, time));
	        if (logger.isLoggable(Level.FINEST)) {
	        	logger.finest(getBuilding().getBuildingManager().getSettlement().getName() + 
	        			" has mixed a salad bowl with " + meals.size() + " fresh vegetables (quality is " + mealQuality + ")");
	        }	
	        makingSoyWorkTime -= COOKED_MEAL_WORK_REQUIRED; 
	        //logger.info("makingSoyChoice() : makingSoyWorkTime is " + makingSoyWorkTime);
	        //logger.info("makingSoyChoice() : COOKED_MEAL_WORK_REQUIRED is " + COOKED_MEAL_WORK_REQUIRED);
        } 
        else { 
        	soyIsAvailable = false;
         	   logger.info("makingSoyChoice() : no more soybeans available for meal! Wait until the next harvest");        	
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