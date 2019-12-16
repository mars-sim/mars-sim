/**
 * Mars Simulation Project
 * EatNDrinkMeta.java
 * @version 3.1.0 2017-03-08
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.task.CookMeal;
import org.mars_sim.msp.core.person.ai.task.EatDrink;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.cooking.Cooking;

/**
 * Meta task for the EatNDrink task.
 */
public class EatDrinkMeta implements MetaTask, Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static Logger logger = Logger.getLogger(EatDrinkMeta.class.getName());
	private static String loggerName = logger.getName();
	private static String sourceName = loggerName.substring(loggerName.lastIndexOf(".") + 1, loggerName.length());
	
	private static final double SMALL_AMOUNT = 0.01;
	
	/** Task name */
	private static final String NAME = Msg.getString("Task.description.eatDrink"); //$NON-NLS-1$

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public Task constructInstance(Person person) {
		return new EatDrink(person);
	}

	@Override
	public double getProbability(Person person) {
		double result = 0;

		Unit container = person.getContainerUnit();
		Inventory inv = null;
//		double foodAmount = 0;
		double waterAmount = 0;
		if (person.isInside()) {
			inv = container.getInventory();	
			// Take preserved food from inventory if it is available.
//			foodAmount = inv.getAmountResourceStored(ResourceUtil.foodID, false);
			waterAmount = inv.getAmountResourceStored(ResourceUtil.waterID, false);
		}
		
		PhysicalCondition pc = person.getPhysicalCondition();

		double thirst = pc.getThirst();
		double hunger = pc.getHunger();
		double energy = pc.getEnergy();

		boolean notHungry = !pc.isHungry();
		boolean notThirsty = !pc.isThirsty();
		
		// CircadianClock cc = person.getCircadianClock();
		// double ghrelin = cc.getSurplusGhrelin();
		// double leptin = cc.getSurplusLeptin();
		// Each meal (.155 kg = .62/4) has an average of 2525 kJ. Thus ~10,000 kJ
		// persson per sol

		if (notHungry && notThirsty)
			// if not thirsty and not hungry
			return 0;
		
		// When thirst is greater than 100, a person may start feeling thirsty
		else  {			
			if (!notThirsty) {
				if (waterAmount < SMALL_AMOUNT)
					return 0;
				result = 2 * (thirst - PhysicalCondition.THIRST_THRESHOLD); //Math.pow((thirst - PhysicalCondition.THIRST_THRESHOLD), 3);
	//			logger.info(person + "'s thirst p : " +  Math.round(result*10D)/10D);
			}
			
			// Only eat a meal if person is sufficiently hungry or low on caloric energy.
			else if (!notHungry) {// || ghrelin-leptin > 300) {
				result += hunger / 2D;
				if (energy < 2525)
					result += (2525 - energy) / 30D; // (ghrelin-leptin - 300);
	//			logger.info(person + "'s hunger p : " +  Math.round(result*10D)/10D);
			}
		}

		if (person.isInSettlement()) {

			if (!CookMeal.isLocalMealTime(person.getCoordinates(), 0)) {
				// If it's not meal time yet, reduce the probability
				result /= 2D;
			}
			
			// Check if a cooked meal is available in a kitchen building at the settlement.
			Cooking kitchen = EatDrink.getKitchenWithMeal(person);
			if (kitchen != null) {
				// Increase probability to eat meal if a cooked meal is available.
				result *= 1.5 * kitchen.getNumberOfAvailableCookedMeals();
			} 
			
			else { // no kitchen has available meals
					// If no cooked meal, check if preserved food is available to eat.
				if (!EatDrink.isPreservedFoodAvailable(person)) {
					// If no food, person can still eat dessert					
					if (EatDrink.getKitchenWithDessert(person) == null) {
						// If no preserved food, person can still drink
						if (notThirsty)
							result /= 3;
					}
					result /= 1.5;
				}
				else
					result *= 1.1;
			}

			// Check if there is a local dining building.
			Building diningBuilding = EatDrink.getAvailableDiningBuilding(person, false);
			if (diningBuilding != null) {
				// Modify probability by social factors in dining building.
				result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, diningBuilding);
				result *= TaskProbabilityUtil.getRelationshipModifier(person, diningBuilding);
//				logger.info(person + "'s diningBuilding p : " +  Math.round(result*10D)/10D);
			}
		}

		else if (person.isInVehicle()) {
			// Note: consider how is the probability compared to that of inside a settlement 
			// may be a person is more likely to become thirsty and/or hungry due to on-call shift ?
			
//			if (!CookMeal.isLocalMealTime(person.getCoordinates(), 0))
//				result /= 3;

			// If no cooked meal, check if preserved food is available to eat.
			if (!EatDrink.isPreservedFoodAvailable(person)) {
				// If no food, person can still eat dessert					
				if (EatDrink.getKitchenWithDessert(person) == null) {
					// If no preserved food, person can still drink
					if (notThirsty)
						result /= 3;
				}
				result /= 1.5;
			}
			else
				result *= 1.1;
			
			// TODO : how to ration food and water if running out of it ?
		} 
		
		else if (person.isOutside()) {

			if (notHungry && notThirsty) {
				return 0;
			}
			
			else if (!notThirsty) {
				// Note: a person may drink water from EVA suit while being outside doing EVA
				result /= 2;
			}
			
			// Note: a person cannot consume food while being outside doing EVA
			
//			else if (CookMeal.isLocalMealTime(person.getCoordinates(), 10)) {
//				result = hunger; 
//			} else
//				result /= 4;
		}

		// Add Preference modifier
		if (result > 0D) {
			result = result + result * person.getPreference().getPreferenceScore(this) / 8D;
		}

		if (result < 0)
			return 0;

//		 if (result > 500) 
//			 logger.warning(person + "'s EatMealMeta : " 
//				 +  Math.round(result*10D)/10D);
		 
		return result;
	}

	@Override
	public Task constructInstance(Robot robot) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getProbability(Robot robot) {
		// TODO Auto-generated method stub
		return 0;
	}
}