/*
 * Mars Simulation Project
 * EatDrinkMeta.java
 * @date 2022-08-10
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.equipment.ResourceHolder;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.fav.FavoriteType;
import org.mars_sim.msp.core.person.ai.task.CookMeal;
import org.mars_sim.msp.core.person.ai.task.EatDrink;
import org.mars_sim.msp.core.person.ai.task.util.MetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.cooking.Cooking;
import org.mars_sim.msp.core.structure.building.function.cooking.PreparingDessert;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for the EatNDrink task.
 */
public class EatDrinkMeta extends MetaTask {

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.eatDrink"); //$NON-NLS-1$

	private static final int CAP = 6_000;
	
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
		// Checks if this person has eaten too much already 
		if (person.getPhysicalCondition().eatTooMuch()
			// Checks if this person has drank enough water already
			&& person.getPhysicalCondition().drinkEnoughWater()) {
			return 0;
		}
		
		double result = 0;
		double foodAmount = 0;
		double waterAmount = 0;

		Vehicle vehicle = null;
		
		ResourceHolder rh = (ResourceHolder) person;
		foodAmount = rh.getAmountResourceStored(FOOD_ID);
		waterAmount = rh.getAmountResourceStored(WATER_ID);
		
		Unit container = person.getContainerUnit();
		
		if (container != null && container instanceof ResourceHolder) {
			rh = (ResourceHolder) container;
			if (foodAmount == 0)
				foodAmount = rh.getAmountResourceStored(FOOD_ID);
			if (waterAmount == 0)
				waterAmount = rh.getAmountResourceStored(WATER_ID);
		}

		boolean food = false;
		boolean water = false;

		int meals = 0;
		double mFactor = 1;

		int desserts = 0;
		double dFactor = 1;

		if (CookMeal.isMealTime(person, 0)) {
			// Check if a cooked meal is available in a kitchen building at the settlement.
			Cooking kitchen0 = EatDrink.getKitchenWithMeal(person);
			if (kitchen0 != null) {
				meals = kitchen0.getNumberOfAvailableCookedMeals();
				mFactor = 200.0 * meals;
			}
		}

		// Check dessert is available in a kitchen building at the settlement.
		PreparingDessert kitchen1 = EatDrink.getKitchenWithDessert(person);
		if (kitchen1 != null) {
			desserts = kitchen1.getAvailableServingsDesserts();
			dFactor = 100.0 * desserts;
		}

		PhysicalCondition pc = person.getPhysicalCondition();

		double thirst = pc.getThirst();
		double hunger = pc.getHunger();
		double energy = pc.getEnergy();

		boolean hungry = pc.isHungry();
		boolean thirsty = pc.isThirsty();

		double ghrelinS = person.getCircadianClock().getSurplusGhrelin();
		double leptinS = person.getCircadianClock().getSurplusLeptin();
		 
		// Each meal (.155 kg = .62/4) has an average of 2525 kJ. Thus ~10,000 kJ
		// person per sol

		if (person.isInSettlement()) {
			if ((hungry || leptinS == 0 || ghrelinS > 0)
					&& (foodAmount > 0 || meals > 0 || desserts > 0)) {
				food = true;
			}

			if (thirsty && waterAmount > 0) {
				water = true;
			}
		}

		else if (person.isInVehicle()) {

			// Future: how to prevent a person to eat food from the vehicle 
			// before the mission embarking ? 
			
			// Note: it will affect the amount of food available 
			// for the mission
			
			if (UnitType.VEHICLE == container.getUnitType()) {
				vehicle = (Vehicle)container;
				
				if (vehicle.isInSettlement()) {
					// How to make a person walk out of vehicle back to settlement 
					// if hunger is >500 ?
		
					rh = (ResourceHolder) vehicle.getSettlement();
					if (foodAmount == 0)
						foodAmount = rh.getAmountResourceStored(FOOD_ID);
					if (waterAmount == 0)
						waterAmount = rh.getAmountResourceStored(WATER_ID);
		
					if (hungry && (foodAmount > 0 || desserts > 0)) {
						food = true;
					}

					else if (thirsty && waterAmount > 0) {
						water = true;
					}
				}
				
				else if (hungry && (foodAmount > 0 || desserts > 0)) {
					food = true;
				}

				else if (thirsty && waterAmount > 0) {
					water = true;
				}
			}
		}

		else {
			if (thirsty && waterAmount > 0) {
				water = true;
			}
		}


		if (food || water) {
			// Only eat a meal if person is sufficiently hungry or low on caloric energy.
			double h0 = 0;
			if (hungry) {
				
				double ghrelin = person.getCircadianClock().getGhrelin();
				double leptin = person.getCircadianClock().getLeptin();
				
				h0 += hunger * hunger / 50 + ghrelin / 2 - leptin / 2;

				if (energy < 2525)
					h0 += (2525 - energy) / 30D;
				
				if (person.isInSettlement() || (vehicle != null && vehicle.isInSettlement())) {

					// Check if there is a local dining building.
					Building diningBuilding = EatDrink.getAvailableDiningBuilding(person, false);
					if (diningBuilding != null) {
						// Modify probability by social factors in dining building.
						h0 *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, diningBuilding);
						h0 *= TaskProbabilityUtil.getRelationshipModifier(person, diningBuilding);
					}
				}
			}

			double t0 = .5 * (thirst - PhysicalCondition.THIRST_THRESHOLD);
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
			result += result * person.getPreference().getPreferenceScore(this) / 8D;

        if (result > CAP)
        	result = CAP;
        
		return result;
	}
}
