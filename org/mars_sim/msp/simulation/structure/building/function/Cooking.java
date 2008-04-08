/**
 * Mars Simulation Project
 * Cooking.java
 * @version 2.81 2007-08-27
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.structure.building.function;

import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.ai.Skill;
import org.mars_sim.msp.simulation.person.ai.task.*;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.simulation.time.MarsClock;

/**
 * The Cooking class is a building function for cooking meals.
 */
public class Cooking extends Function implements Serializable {
    
    
	private static String CLASS_NAME = 
	    "org.mars_sim.msp.simulation.structure.building.function.Cooking";
	
	private static Logger logger = Logger.getLogger(CLASS_NAME);

	public static final String NAME = "Cooking";
	
	// The base amount of work time (cooking skill 0) to produce a cooked meal.
	public static final double COOKED_MEAL_WORK_REQUIRED = 20D;

	// Data members
	private int cookCapacity;
	private List<CookedMeal> meals;
	private double cookingWorkTime;
	
	/**
	 * Constructor
	 * @param building the building this function is for.
	 * @throws BuildingException if error in constructing function.
	 */
	public Cooking(Building building) throws BuildingException {
		// Use Function constructor.
		super(NAME, building);
		
		cookingWorkTime = 0D;
		meals = new ArrayList<CookedMeal>();
		
		BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
		
		try {
			this.cookCapacity = config.getCookCapacity(building.getName());
		}
		catch (Exception e) {
			throw new BuildingException("Cooking.constructor: " + e.getMessage());
		}		
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
        
		if (getBuilding().hasFunction(LifeSupport.NAME)) {
			try {
				LifeSupport lifeSupport = (LifeSupport) getBuilding().getFunction(LifeSupport.NAME);
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
		
		if (getBuilding().hasFunction(LifeSupport.NAME)) {
			try {
				LifeSupport lifeSupport = (LifeSupport) getBuilding().getFunction(LifeSupport.NAME);
				Iterator<Person> i = lifeSupport.getOccupants().iterator();
				while (i.hasNext()) {
					Person person = i.next();
					Task task = person.getMind().getTaskManager().getTask();
					if (task instanceof CookMeal) {
						int cookingSkill = person.getMind().getSkillManager().getEffectiveSkillLevel(Skill.COOKING);
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
	 */
	public void addWork(double workTime) throws BuildingException {
		cookingWorkTime += workTime;
		while (cookingWorkTime >= COOKED_MEAL_WORK_REQUIRED) {
			int mealQuality = getBestCookSkill();
			MarsClock time = (MarsClock) Simulation.instance().getMasterClock().getMarsClock().clone();
			
			try {
				PersonConfig config = SimulationConfig.instance().getPersonConfiguration();
				double foodAmount = config.getFoodConsumptionRate() * (1D / 3D);
				getBuilding().getInventory().retrieveAmountResource(AmountResource.FOOD, foodAmount);
			}
			catch (InventoryException e) {
				throw new BuildingException("Not enough food in settlement to cook.");
			}
			catch (Exception e) {
				throw new BuildingException("Error getting configuration data.");
			}
			
			meals.add(new CookedMeal(mealQuality, time));
			cookingWorkTime -= COOKED_MEAL_WORK_REQUIRED;
			
			if(logger.isLoggable(Level.FINEST)) {
			logger.finest(getBuilding().getBuildingManager().getSettlement().getName() + 
				" has " + meals.size() + " hot meals, quality=" + mealQuality);
			}
		}
	}
	
	/**
	 * Time passing for the building.
	 * @param time amount of time passing (in millisols)
	 * @throws BuildingException if error occurs.
	 */
	public void timePassing(double time) throws BuildingException {
		
		// Move expired meals back to food again (refrigerate leftovers).
		Iterator<CookedMeal> i = meals.iterator();
		while (i.hasNext()) {
			CookedMeal meal = i.next();
			MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
			if (MarsClock.getTimeDiff(meal.getExpirationTime(), currentTime) < 0D) {
				try {
					PersonConfig config = SimulationConfig.instance().getPersonConfiguration();
					double foodAmount = config.getFoodConsumptionRate() * (1D / 3D);
					double foodCapacity = getBuilding().getInventory().getAmountResourceRemainingCapacity(AmountResource.FOOD, false);
					if (foodAmount > foodCapacity) foodAmount = foodCapacity;
					getBuilding().getInventory().storeAmountResource(AmountResource.FOOD, foodAmount, false);
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
}