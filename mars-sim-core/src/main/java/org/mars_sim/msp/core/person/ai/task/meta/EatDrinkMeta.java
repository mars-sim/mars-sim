/**
 * Mars Simulation Project
 * EatNDrinkMeta.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.task.CookMeal;
import org.mars_sim.msp.core.person.ai.task.EatDrink;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.cooking.Cooking;
import org.mars_sim.msp.core.structure.building.function.cooking.PreparingDessert;

/**
 * Meta task for the EatNDrink task.
 */
public class EatDrinkMeta extends MetaTask {
	
	/** Task name */
	private static final String NAME = Msg.getString("Task.description.eatDrink"); //$NON-NLS-1$

    public EatDrinkMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.ANY_HOUR);
		
		setFavorite(FavoriteType.COOKING);
	}
    
	@Override
	public Task constructInstance(Person person) {
		return new EatDrink(person);
	}

	@Override
	public double getProbability(Person person) {
		double result = 0;

		double foodAmount = 0;
		double waterAmount = 0;
		
		Inventory inv = null;
		Unit container = person.getContainerUnit();
		if (container != null) {
			inv = container.getInventory();	
			// Take preserved food from inventory if it is available.
			foodAmount = inv.getAmountResourceStored(ResourceUtil.foodID, false);
			waterAmount = inv.getAmountResourceStored(ResourceUtil.waterID, false);
		}
		
		boolean food = false;
		boolean water = false;
	
		int meals = 0;
		double mFactor = 1;
		
		int desserts = 0;
		double dFactor = 1;
		
		// Check if a cooked meal is available in a kitchen building at the settlement.
		Cooking kitchen0 = EatDrink.getKitchenWithMeal(person);
		if (kitchen0 != null) {
			meals = kitchen0.getNumberOfAvailableCookedMeals();
			mFactor = 1.5 * meals;
		} 
		
		// Check dessert is available in a kitchen building at the settlement.
		PreparingDessert kitchen1 = EatDrink.getKitchenWithDessert(person);
		if (kitchen1 != null) {
			desserts = kitchen1.getAvailableServingsDesserts();
			dFactor = 1.5 * desserts;
		} 
		
		PhysicalCondition pc = person.getPhysicalCondition();

		double thirst = pc.getThirst();
		double hunger = pc.getHunger();
		double energy = pc.getEnergy();

		boolean hungry = pc.isHungry();
		boolean thirsty = pc.isThirsty();
		
		
		// CircadianClock cc = person.getCircadianClock();
		// double ghrelin = cc.getSurplusGhrelin();
		// double leptin = cc.getSurplusLeptin();
		// Each meal (.155 kg = .62/4) has an average of 2525 kJ. Thus ~10,000 kJ
		// persson per sol
		
		if (person.isInSettlement()) {
			if (hungry && (foodAmount > 0 || meals > 0 || desserts > 0)) {
				food = true;
			}
			
			if (thirsty && waterAmount > 0) {
				water = true;
			}
		}
		
		else if (person.isInVehicle()) {
			if (hungry && (foodAmount > 0 || desserts > 0)) {
				food = true;
			}
			
			if (thirsty && waterAmount > 0) {
				water = true;
			}
		}
		
		else {
			if (thirsty && waterAmount > 0) {
				water = true;
			}
		}
		

		if (food || water) {
			// Calculate ...
			// Only eat a meal if person is sufficiently hungry or low on caloric energy.
			double h0 = 0;
			if (hungry) {// || ghrelin-leptin > 300) {
				h0 += hunger / 2D;
				if (energy < 2525)
					h0 += (2525 - energy) / 30D; // (ghrelin-leptin - 300);
				
				if (person.isInSettlement()) {
					
					if (!CookMeal.isLocalMealTime(person.getCoordinates(), 0)) {
						// If it's not meal time yet, reduce the probability
						mFactor /= 5D;
					}
					
					// Check if there is a local dining building.
					Building diningBuilding = EatDrink.getAvailableDiningBuilding(person, false);
					if (diningBuilding != null) {
						// Modify probability by social factors in dining building.
						h0 *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, diningBuilding);
						h0 *= TaskProbabilityUtil.getRelationshipModifier(person, diningBuilding);
//						logger.info(person + "'s diningBuilding p : " +  Math.round(result*10D)/10D);
					}
				}
			}
			
			double t0 = 2 * (thirst - PhysicalCondition.THIRST_THRESHOLD);
			if (t0 <= 0)
				t0 = 0;
			
			result = (h0 * mFactor * dFactor) + t0;
		}
		
		else
			return 0;		

		if (result <= 0)
			return 0;
		else
			// Add Preference modifier
			result = result + result * person.getPreference().getPreferenceScore(this) / 8D;

//		 if (result > 100) 
//			 logger.warning(person + "'s EatMealMeta : " 
//				 +  Math.round(result * 10D)/10D);
		 
		return result;
	}
}
