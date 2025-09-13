/*
 * Mars Simulation Project
 * EatDrinkMeta.java
 * @date 2023-06-10
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task.meta;

import java.util.List;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.cooking.Cooking;
import com.mars_sim.core.building.function.cooking.task.CookMeal;
import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.equipment.ResourceHolder;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.task.EatDrink;
import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.Vehicle;

/**
 * Meta task for the EatNDrink task.
 */
public class EatDrinkMeta extends FactoryMetaTask {

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.eatDrink"); //$NON-NLS-1$

	public static final double VEHICLE_FOOD_RATION = .25;
	public static final double MISSION_FOOD_RATION = .5;
		
    public EatDrinkMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.ANY_HOUR);

		setFavorite(FavoriteType.COOKING);
	}

	@Override
	public Task constructInstance(Person person) {
		return new EatDrink(person);
	}

	@Override
	public List<TaskJob> getTaskJobs(Person person) {
		// Checks if this person has eaten too much already 
		if (person.getPhysicalCondition().eatTooMuch()
			// Checks if this person has drank enough water already
			&& person.getPhysicalCondition().drinkEnoughWater()) {
			return EMPTY_TASKLIST;
		}
		
		// Identify the available amount first
		double foodAmount = person.getSpecificAmountResourceStored(ResourceUtil.FOOD_ID);
		double waterAmount = person.getSpecificAmountResourceStored(ResourceUtil.WATER_ID);
		
		var container = person.getContainerUnit();
		if (container instanceof ResourceHolder rh) {
			if (foodAmount == 0)
				foodAmount = rh.getSpecificAmountResourceStored(ResourceUtil.FOOD_ID);
			if (waterAmount == 0)
				waterAmount = rh.getSpecificAmountResourceStored(ResourceUtil.WATER_ID);
		}

		boolean needFood = false;
		boolean needWater = false;
		boolean inSettlement = false;

		double mFactor = 1;
		double dFactor = 1;

		if (CookMeal.isMealTime(person.getAssociatedSettlement(), 0)) {
			// Check if a cooked meal is available in a kitchen building at the settlement.
			Cooking kitchen0 = EatDrink.getKitchenWithMeal(person);
			if (kitchen0 != null) {
				mFactor = 200.0 * kitchen0.getNumberOfAvailableCookedMeals();
			}
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
			
			inSettlement = true;
			needFood = ((hungry || leptinS == 0 || ghrelinS > 0)
					&& (foodAmount > 0 || mFactor > 1 || dFactor > 1));
		}
		
		else if (person.isInVehicle()) {
			
			Vehicle vehicle = person.getVehicle();
			
			if (vehicle.isInGarage()) {
				inSettlement = true;

				// How to make a person walk out of vehicle back to settlement 
				// if hunger is >500 ?
				if (foodAmount == 0)
					foodAmount = vehicle.getSettlement().getSpecificAmountResourceStored(ResourceUtil.FOOD_ID);
				if (waterAmount == 0)
					waterAmount = vehicle.getSettlement().getSpecificAmountResourceStored(ResourceUtil.WATER_ID);
	
				needFood = (hungry && (foodAmount > 0 || dFactor > 1));
			}
			else {
				// One way that prevents a person from eating vehicle food
				// before the mission embarking is
				// by having the person carry food himself
				
				// Note: if not, it may affect the amount of water/food available 
				// for the mission
				if (foodAmount == 0)
					foodAmount = person.getSpecificAmountResourceStored(ResourceUtil.FOOD_ID);
				if (waterAmount == 0)
					waterAmount = person.getSpecificAmountResourceStored(ResourceUtil.WATER_ID);
	
				if (foodAmount == 0)
					foodAmount = vehicle.getSpecificAmountResourceStored(ResourceUtil.FOOD_ID);
				if (waterAmount == 0)
					waterAmount = vehicle.getSpecificAmountResourceStored(ResourceUtil.WATER_ID);
				
				needFood = (hungry && (foodAmount > 0 || dFactor > 1));
			}
		}

		needWater = (thirsty && waterAmount > 0);

		// Calculate score
		RatingScore result = new RatingScore();
		if (needFood) {
			// Only eat a meal if person is sufficiently hungry or low on caloric energy.
			double ghrelin = person.getCircadianClock().getGhrelin();
			double leptin = person.getCircadianClock().getLeptin();	
			double hungerBase = hunger + ghrelin / 2 - leptin / 2;

			if (energy < 2525)
				hungerBase += (2525 - energy) / 30D;
			RatingScore hungerScore = new RatingScore(hungerBase);

			if (inSettlement) {
				// Check if there is a local dining building.
				Building diningBuilding = BuildingManager.getAvailableDiningBuilding(person, false);
				hungerScore = assessBuildingSuitability(hungerScore, diningBuilding, person);
			}
			else if (person.isInVehicle() ) {
				// Person will try refraining from eating food while in a vehicle
				hungerScore.addModifier("vehicle", VEHICLE_FOOD_RATION);
			}
			else if (person.getMission() != null) {
				// Person will tends to ration food while in a missi0on
				hungerScore.addModifier("mission", MISSION_FOOD_RATION);
			}
			hungerScore.addModifier("eatdrink.meals", mFactor);
			hungerScore.addModifier("eatdrink.desserts", dFactor);
			result.addBase("hunger", hungerScore.getScore());
		}

		if (needWater) {
			double thirstBase = .5 * (thirst - PhysicalCondition.THIRST_THRESHOLD);
			if (thirstBase <= 0)
				thirstBase = 0;

			result.addBase("thirst", thirstBase);
		}
		return createTaskJobs(result);
	}
}
