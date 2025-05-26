/*
 * Mars Simulation Project
 * EatDrink.java
 * @date 2023-11-24
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task;

import java.util.Set;
import java.util.logging.Level;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingCategory;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.Function;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.building.function.cooking.PreparedDish;
import com.mars_sim.core.building.function.cooking.Cooking;
import com.mars_sim.core.building.function.task.CookMeal;
import com.mars_sim.core.environment.MarsSurface;
import com.mars_sim.core.equipment.Container;
import com.mars_sim.core.equipment.EVASuit;
import com.mars_sim.core.equipment.ResourceHolder;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.person.ai.task.meta.EatDrinkMeta;
import com.mars_sim.core.person.ai.task.util.MetaTaskUtil;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.Vehicle;

/**
 * The EatDrink class is a task for eating a meal. The duration of the task is 40
 * millisols. Note: Eating a meal reduces hunger to 0.
 */
public class EatDrink extends Task {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(EatDrink.class.getName());

	/** Task name */
	private static final String EATING_PRESERVED = Msg.getString("Task.description.eatDrink.preserved"); //$NON-NLS-1$
	private static final String DRINKING = Msg.getString("Task.description.eatDrink.drinking"); //$NON-NLS-1$
	private static final String EATING = Msg.getString("Task.description.eatDrink.eating"); //$NON-NLS-1$
	private static final String NAME = EATING;

	/** Task phases. */
	private static final TaskPhase LOOK_FOR_FOOD = new TaskPhase(Msg.getString("Task.phase.lookingforFood")); //$NON-NLS-1$
	private static final TaskPhase EAT_MEAL = new TaskPhase(Msg.getString("Task.phase.eatingMeal")); //$NON-NLS-1$
	private static final TaskPhase EAT_PRESERVED_FOOD = new TaskPhase(Msg.getString("Task.phase.eatingFood")); //$NON-NLS-1$
	private static final TaskPhase DRINK_WATER = new TaskPhase(Msg.getString("Task.phase.drinkingWater")); //$NON-NLS-1$

	private static final int NUMBER_OF_MEAL_PER_SOL = 3;
	
	/** The conversion ratio of hunger to one serving of food. */	
	private static final int HUNGER_RATIO_PER_FOOD_SERVING = 300;
	/** The conversion ratio of thirst to one serving of water. */	
	private static final int THIRST_PER_WATER_SERVING = 450;
	/** The minimal amount of resource to be retrieved. */
	private static final double MIN = 0.001;
	/** The amount of preserved food for carrying in a person [in kg]. */
	private static final double PACKED_PRESERVED_FOOD_CARRIED = .2; 
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = -1.2D;
	/** Mass (kg) of single napkin for meal. */
	private static final double NAPKIN_MASS = .0025D;

	private double foodConsumedPerServing;

	private double millisolPerKgFood;

	// Data members
	private boolean food = false;
	private boolean water = false;
	private boolean hasNapkin = false;
	
	private int meals = 0;
	
	private double foodAmount = 0;
	/** how much eaten [in kg]. */
	private double cumulativeProportion = 0;
	private double totalEatingTime = 0D;
	private double eatingDuration = 0D;
	private double waterEachServing;

	private PreparedDish cookedMeal;
	private Cooking kitchen;
	private PhysicalCondition pc;

	/**
	 * Constructor.
	 *
	 * @param person the person to perform the task
	 */
	public EatDrink(Person person) {
		super(NAME, person, false, false, STRESS_MODIFIER, 10
				+ RandomUtil.getRandomDouble(-2, 2));

		pc = person.getPhysicalCondition();

		// Checks if this person has eaten too much already 
		if (pc.eatTooMuch()
			// Checks if this person has drank enough water already
			&& pc.drinkEnoughWater()) {
			clearTask("Consumed enough today already.");
			return;
		}
		
		double dur = getDuration();
		eatingDuration = dur;

		foodConsumedPerServing = personConfig.getFoodConsumptionRate() / NUMBER_OF_MEAL_PER_SOL;			
		millisolPerKgFood = HUNGER_RATIO_PER_FOOD_SERVING / foodConsumedPerServing; 
		
		// ~.03 kg per serving
		waterEachServing = pc.getWaterConsumedPerServing();

		////////////////////

		double waterAmount = 0;

		foodAmount = person.getAmountResourceStored(ResourceUtil.FOOD_ID);
		waterAmount = person.getAmountResourceStored(ResourceUtil.WATER_ID);
		
		var container = person.getContainerUnit();
		if (container instanceof ResourceHolder c) {
			// Take preserved food from inventory if it is available.
			if (foodAmount == 0)
				foodAmount = c.getAmountResourceStored(ResourceUtil.FOOD_ID);
			if (waterAmount == 0)
				waterAmount = c.getAmountResourceStored(ResourceUtil.WATER_ID);
		}

		// If still no water, check bottle
		if ((waterAmount == 0) && person.hasThermalBottle()) {
			var bottle = person.lookForThermalBottle();
			waterAmount = bottle.getAllAmountResourceStored(ResourceUtil.WATER_ID);
		}

		// Check if a cooked meal is available in a kitchen building at the settlement.
		Cooking kitchen0 = EatDrink.getKitchenWithMeal(person);
		if (kitchen0 != null) {
			meals = kitchen0.getNumberOfAvailableCookedMeals();
		}

		boolean hungry = pc.isHungry();
		boolean thirsty = pc.isThirsty();

		/////////////////////////////////////////////////

		if (person.isInSettlement()) {
			
			checkSettlement(hungry, thirsty, waterAmount);
		}

		else if (person.isInVehicle()) {

			checkPersonVehicle(person.getVehicle(), hungry, thirsty);
		}

		else {
			// if a person is on EVA suit
			if (thirsty && waterAmount > 0) {
				water = true;
			}
		}

		if (!food && !water) {
			endTask();
		}

		if (food)
			goForFood();
		
		if (water)
			goForWater();
	}
		
	/**
	 * Checks if the settlement has food and water.
	 * 
	 * @param hungry
	 * @param thirsty
	 * @param waterAmount
	 */
	private void checkSettlement(boolean hungry, boolean thirsty, double waterAmount) {
		Building currentBuilding = BuildingManager.getBuilding(person);
		if (currentBuilding != null && currentBuilding.getCategory() != BuildingCategory.EVA) {
			// Check if there is a local dining building.
        	Building diningBuilding = BuildingManager.getAvailableFunctionTypeBuilding(person, FunctionType.DINING);
        	
        	if (diningBuilding != null) {
        		// Initiates a walking task to go back to the settlement
        		walkToDiningLoc(diningBuilding, false);
        	}			
		}

		if (hungry && (foodAmount > 0 || meals > 0)) {
			food = true;
		}

		if (thirsty && waterAmount > 0) {
			water = true;
		}
	}
	
	/**
	 * Checks if the person or vehicle has food and water.
	 * 
	 * @param container
	 * @param hungry
	 * @param thirsty
	 */
	private void checkPersonVehicle(Vehicle container, boolean hungry, boolean thirsty) {

		foodAmount = person.getAmountResourceStored(ResourceUtil.FOOD_ID);
		
		if (hungry && (foodAmount > 0)) {
			food = true;
		}
		else {
			foodAmount = container.getAmountResourceStored(ResourceUtil.FOOD_ID);
			
			if (hungry && (foodAmount > 0)) {
				food = true;
			}
		}

		var waterAmount = person.getAmountResourceStored(ResourceUtil.WATER_ID);
			
		if (thirsty && waterAmount > 0) {
			water = true;
		}
		else {
			waterAmount = container.getAmountResourceStored(ResourceUtil.WATER_ID);
			
			if (thirsty && waterAmount > 0) {
				water = true;
			}
		}
	}
	
	private void goForFood() {

		if (person.isInSettlement()) {

			if (CookMeal.isMealTime(person.getAssociatedSettlement(), 0)) {
				
				if (meals > 0) {				
					goDining();
				}
				
				else {
					checkFoodDessertAmount();
				}
			}
			else {
				checkFoodDessertAmount();
			}
		}

		else if (person.isInVehicle()) {
			
			checkFoodDessertAmount();
		}
	}

	private void goDining() {
		
		Building diningBuilding = BuildingManager.getAvailableDiningBuilding(person, false);
		if (diningBuilding != null) {
			// Initialize task phase.
			addPhase(LOOK_FOR_FOOD);
			addPhase(EAT_MEAL);
			setPhase(LOOK_FOR_FOOD);
			
			walkToDiningLoc(diningBuilding, false);
		}

		boolean want2Chat = true;
		// See if a person wants to chat while eating
		int score = person.getPreference().getPreferenceScore(MetaTaskUtil.getConverseMeta());
		if (score > 0)
			want2Chat = true;
		else if (score < 0)
			want2Chat = false;
		else {
			int rand = RandomUtil.getRandomInt(1);
			if (rand == 0)
				want2Chat = false;
		}

		diningBuilding = BuildingManager.getAvailableDiningBuilding(person, want2Chat);
		if (diningBuilding != null)
			// Walk to that building.
			walkToActivitySpotInBuilding(diningBuilding, FunctionType.DINING, true);

		// Take napkin from inventory if available.
		if (person.getSettlement().retrieveAmountResource(ResourceUtil.NAPKIN_ID, NAPKIN_MASS) > 0)
			hasNapkin = true;
	}
	
	
	private void checkFoodDessertAmount() {
		// Initialize task phase.
		addPhase(LOOK_FOR_FOOD);
		addPhase(EAT_PRESERVED_FOOD);
		setPhase(LOOK_FOR_FOOD);
	}
	
	/**
	 * Walks to an activity in the dining building.
	 * 
	 * @param building  the dining building.
	 * @param allowFail true if walking is allowed to fail.
	 */
	protected void walkToDiningLoc(Building building, boolean allowFail) {
		// Find a free spot in the building
		Function f = building.getFunction(FunctionType.DINING);
		LocalPosition loc = f.getAvailableActivitySpot();
		if (loc == null) {
			// Find another spot in the smae building
			f = building.getEmptyActivitySpotFunction();
			if (f == null) {
				return;
			}
			loc = f.getAvailableActivitySpot();
		}

		// Create subtask for walking to destination.
		if (loc != null) {			
			boolean canWalk = createWalkingSubtask(building, loc, allowFail);
		
			if (canWalk) {
				// Set the new position
				person.setPosition(loc);
						
				// Add the person to this activity spot
				f.claimActivitySpot(loc, person);
			}
		}
	}

	private void goForWater() {
		// if water only
		// Initialize task phase.
		addPhase(DRINK_WATER);
		setPhase(DRINK_WATER);
	}

	/**
	 * Performs the method mapped to the task's current phase.
	 *
	 * @param time the amount of time (millisol) the phase is to be performed.
	 * @return the remaining time (millisol) after the phase has been performed.
	 */
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException(person + "'s Task phase is null");
		}

		if (DRINK_WATER.equals(getPhase())) {
			return drinkingWaterPhase();
		} else if (LOOK_FOR_FOOD.equals(getPhase())) {
			return lookingforFoodPhase(time);
		} else if (EAT_MEAL.equals(getPhase())) {
			return eatingMealPhase(time);
		} else if (EAT_PRESERVED_FOOD.equals(getPhase())) {
			return eatingPreservedFoodPhase(time);
		} else {
			return time;
		}
	}

	/**
	 * Performs the drinking water phase of the task.
	 *
	 * @param time the amount of time (millisol) to perform the drinking water phase.
	 * @return the amount of time (millisol) left after performing the drinking water
	 *         phase.
	 */
	private double drinkingWaterPhase() {
		
		// Call to consume water
		if (!person.getPhysicalCondition().drinkEnoughWater())
			calculateWater(true);

		return 0;
	}

	/**
	 * Performs the pick up the food or the meal phase.
	 *
	 * @param time the amount of time (millisol) to perform the phase.
	 * @return the remaining time (millisol) after the phase has been performed.
	 */
	private double lookingforFoodPhase(double time) {
		double remainingTime = 0;

		// Determine preferred kitchen to get meal.
		if (kitchen == null) {
			kitchen = getKitchenWithMeal(person);

			if (kitchen == null) {
				// No kitchen so eat preserved food
				addPhase(EAT_PRESERVED_FOOD);
				setPhase(EAT_PRESERVED_FOOD);
				return time * .75;
			}
		}

		// Walk to kitchen.
		boolean canWalk = walkToActivitySpotInBuilding(kitchen.getBuilding(), FunctionType.DINING, true);

		// Pick up a meal at kitchen if one is available.
		cookedMeal = kitchen.chooseAMeal(person);
		if (cookedMeal != null) {
			setPhase(EAT_MEAL);
		}
		else {
			choosePreservedFood();
			return time * .75;
		}
		
		if (canWalk)
			return remainingTime * .75;

		return remainingTime;
	}

	/**
	 * Chooses either preserved food or dessert randomly.
	 */
	private void choosePreservedFood() {	
		// usually eat preserved food
		addPhase(EAT_PRESERVED_FOOD);
		setPhase(EAT_PRESERVED_FOOD);
	}

	/**
	 * Performs eating preserved food phase.
	 *
	 * @param time
	 * @return
	 */
	private double eatingPreservedFoodPhase(double time) {
		
		// Checks if this person has eaten too much already 
		if (pc.eatTooMuch()) {
			endTask();
			return time;
		}
		
		// Give a person the chance to eat even if in high fatigue
		int rand = RandomUtil.getRandomInt(1);
		if (rand == 1 && pc.getFatigue() > 2_000) {
			endTask();
			return time;
		}
		
		double remainingTime = 0;
		double eatingTime = time;

		double proportion = eatPreservedFood(eatingTime);

		// FUTURE : If not enough preserved food available, 
		// should change to dessert phase.
		
		if (proportion == 0.0) {
			endTask();
			return time;
		}
		else {
			// Report eating preserved food.
			setDescription(EATING_PRESERVED);

			// Make sure it does not exceed eatingDuration
			if ((totalEatingTime + eatingTime) >= eatingDuration) {
				eatingTime = eatingDuration - totalEatingTime;
			}

			if (eatingTime < time) {
				remainingTime = time - eatingTime;
			}

			if (cumulativeProportion + proportion >= foodConsumedPerServing) {
				endTask();
				return remainingTime;
			}
			
			totalEatingTime += eatingTime;
		}

		return remainingTime;
	}

	/**
	 * Eats a meal of preserved food.
	 *
	 * @param eatingTime the amount of time (millisols) to eat.
	 * @return true if enough preserved food available to eat.
	 */
	private double eatPreservedFood(double eatingTime) {

		// Proportion of food being eaten over this time period.
		double proportion = person.getEatingSpeed() * eatingTime;
		if (cumulativeProportion + proportion > foodConsumedPerServing) {
			proportion = foodConsumedPerServing - cumulativeProportion;
		}
		
		if (proportion < MIN) {
			return 0;
		}
				
		// Check directly from settlement
		if (person.isInSettlement()) {
			// Take preserved food from container if it is available.
			double shortfall = person.getSettlement().retrieveAmountResource(ResourceUtil.FOOD_ID, proportion);
			if (proportion > shortfall) {
				proportion -= shortfall;			
			}
		}
		else {
			// When inside a vehicle, retrieve food from a person or from vehicle
		
			if (person.isInVehicle()) {
				// Person will try refraining from eating food while in a vehicle
				proportion *= EatDrinkMeta.VEHICLE_FOOD_RATION;
				proportion = consumeFoodProportion(proportion);
			}
			
			else if (person.getMission() != null) {
				// Person will tends to ration more food while on a mission
				proportion *= EatDrinkMeta.MISSION_FOOD_RATION;
				proportion = consumeFoodProportion(proportion);
			}
		}
		
		if (proportion > 0) {
			// Record the amount consumed
			pc.recordFoodConsumption(proportion, 0);
			// Add to cumulativeProportion
			cumulativeProportion += proportion;
			// Food amount eaten over this period of time.
			double hungerRelieved = millisolPerKgFood * proportion;
			// Consume preserved food after eating
			pc.reduceHunger(hungerRelieved);
			
			// Add caloric energy from the preserved food.
			double caloricEnergyFoodAmount = hungerRelieved * PhysicalCondition.FOOD_COMPOSITION_ENERGY_RATIO;
			pc.addEnergy(caloricEnergyFoodAmount);

		} else {
			// Not enough food available to eat, will end this task.
			proportion = 0;
		}
		
		// Note: only allow this 'luxury' when a person is in a settlement  
		// If on mission, food is limited and should be 'shared'
		if (person.isInSettlement()) {	
			// Take preserved food from container and store it in a person if it is available 
			double shortfall = person.getSettlement().retrieveAmountResource(ResourceUtil.FOOD_ID, PACKED_PRESERVED_FOOD_CARRIED);
			if (shortfall > 0) {
				if (shortfall - PACKED_PRESERVED_FOOD_CARRIED < MIN) {
					logger.info(person, 20_000L, "No preserved food available.");
				}
				else {
					// Store the food on a person
					double excess = person.storeAmountResource(ResourceUtil.FOOD_ID, PACKED_PRESERVED_FOOD_CARRIED - shortfall);
					if (excess > 0) {
						// Transfer any excess that a person cannot carry back to the settlement
						person.getSettlement().storeAmountResource(ResourceUtil.FOOD_ID, excess);
					}
				}
			}
		}				

		return proportion;
	}
	
	/**
	 * Consumes the food proportion.
	 * 
	 * @param proportion
	 * @return
	 */
	private double consumeFoodProportion(double proportion) {
		// Assume the person carries preserved food 	
		double shortfall = person.retrieveAmountResource(ResourceUtil.FOOD_ID, proportion);
		if (shortfall > 0) {
			var container = person.getContainerUnit();
			shortfall = ((ResourceHolder)container).retrieveAmountResource(ResourceUtil.FOOD_ID, shortfall);
		}
		
		return proportion - shortfall;
	}
	
	/**
	 * Performs the eating meal phase of the task.
	 *
	 * @param time the amount of time (millisol) to perform the eating meal phase.
	 * @return the amount of time (millisol) left after performing the eating meal
	 *         phase.
	 */
	private double eatingMealPhase(double time) {
		
		// Checks if this person has eaten too much already 
		if (pc.eatTooMuch()) {
			endTask();
			return time;
		}
		
		// Give a person the chance to eat even if in high fatigue
		int rand = RandomUtil.getRandomInt(1);
		if (rand == 1 && pc.getFatigue() > 2_000) {
			endTask();
			return time;
		}
		
		double remainingTime = 0;
		double eatingTime = time;
		
		if (cumulativeProportion >= cookedMeal.getDryMass()) {
			endTask();
			return remainingTime;
		}

		if ((totalEatingTime + eatingTime) >= eatingDuration) {
			eatingTime = eatingDuration - totalEatingTime;
		}
		
		if (eatingTime > 0) {
			
			String s = EATING + " " + cookedMeal.getName();
			// Set description for eating cooked meal.
			setDescription(s); //$NON-NLS-1$
			// Eat cooked meal.
			eatCookedMeal(eatingTime);
	
			// If finished eating, change to dessert phase.
			if (eatingTime < time) {
				remainingTime = time - eatingTime;
			}

			totalEatingTime += eatingTime;

		} else {
			// Eat preserved food if available
			setPhase(EAT_PRESERVED_FOOD);
		}

		return remainingTime;
	}

	/**
	 * Eats a cooked meal.
	 *
	 * @param eatingTime the amount of time (millisols) to eat.
	 */
	private double eatCookedMeal(double eatingTime) {
		// Obtain the dry mass of the dessert
		double dryMass = cookedMeal.getDryMass();
		// Proportion of meal being eaten over this time period.
		// eatingSpeed ~ 0.1 kg / millisols
		double proportion = person.getEatingSpeed() * eatingTime;

		if (cumulativeProportion + proportion > dryMass) {
			double excess = cumulativeProportion + proportion - dryMass;
			proportion = proportion - excess;
		}
		
		if (proportion > MIN) {
			// Add to cumulativeProportion
			cumulativeProportion += proportion;
			// Food amount eaten over this period of time.
			double hungerRelieved = HUNGER_RATIO_PER_FOOD_SERVING * proportion / dryMass;
			// Record the amount consumed
			pc.recordFoodConsumption(proportion, 1);
			// Change the hunger level after eating
			pc.reduceHunger(hungerRelieved);

			logger.log(worker, Level.FINE, 4_000, "Eating " + cookedMeal.getName() + ".");

			// Reduce person's stress over time from eating a cooked meal.
			// This is in addition to normal stress reduction from eating task.
			double stressModifier = STRESS_MODIFIER * (cookedMeal.getQuality() + 1D);
			double deltaStress = stressModifier * eatingTime;
			pc.reduceStress(deltaStress);

			// Add caloric energy from meal.
			double caloricEnergyFoodAmount = hungerRelieved * PhysicalCondition.FOOD_COMPOSITION_ENERGY_RATIO;
			pc.addEnergy(caloricEnergyFoodAmount);
		}
		
		return proportion;
	}

	/**
	 * Calculates the amount of water to consume.
	 *
	 * @param is it water only
	 */
	private void calculateWater(boolean waterOnly) {

		double amount = RandomUtil.getRandomDouble(waterEachServing / 2, waterEachServing);

		var containerUnit = person.getContainerUnit();
		if (containerUnit instanceof MarsSurface) {
			// Doing EVA outside. Get water from one's EVA suit
			EVASuit suit = person.getSuit();

			double available = suit.getAmountResourceStored(ResourceUtil.WATER_ID);
			
			// Test to see if there's enough water
			if (available >= amount) {
				logger.fine(person, 4_000L, "Drinking " + Math.round(amount * 100.0)/100.0 + " kg of water from " + suit.getName() + ".");
				consumeWater(suit, amount, waterOnly);
			}
			else if (available > 0) {
				amount = available;
				logger.info(person, 10_000L, "Drinking " + Math.round(amount * 100.0)/100.0 + " kg of water from " + suit.getName() + ".");
				consumeWater(suit, amount, waterOnly);
			}
		}
		else if (containerUnit instanceof ResourceHolder foodStore) {
			// Case 2: drink from bottle when being inside the settlement or vehicle			
			// Case 0: drink from thermal bottle
			double availableAmount = 0;
			// Get the bottle the person is carrying
			Container bottle = person.lookForThermalBottle();
			if (bottle != null)  {
				availableAmount = bottle.getAmountResourceStored(ResourceUtil.WATER_ID);
		
				// Case 1: See if there's enough water in the bottle
				if (availableAmount >= amount) {
					consumeWater(bottle, amount, waterOnly);
					return;
				}
				else if (availableAmount > 0) {
					amount = availableAmount;
					consumeWater(bottle, amount, waterOnly);
					return;
				}
				
				// Case 2: See if the person needs to fill up the empty bottle
				if (availableAmount == 0.0) {
					// Retrieve the water from settlement/vehicle
					double missing = foodStore.retrieveAmountResource(ResourceUtil.WATER_ID, 1);
					// Fill up the bottle with water
					if (missing < 1) {
						amount = 1 - missing;
						person.fillUpThermalBottle(amount);
					}
				}	
			}
				
			// Used bottle of none available
			if (person.isInSettlement() ) {		
				int level = person.getAssociatedSettlement().getWaterRationLevel();
				amount = Math.max(MIN, amount / level);
			}
			else if (person.isInVehicle() ) {
				// Person will try refraining from eating food while in a vehicle
				amount *= EatDrinkMeta.VEHICLE_FOOD_RATION;
			}
			else if (person.getMission() != null) {
				// Person will tends to ration food while in a mission
				amount *= EatDrinkMeta.MISSION_FOOD_RATION;
			}

			// Take water carried by person if available
			double available = foodStore.getAmountResourceStored(ResourceUtil.WATER_ID);
			// Test to see if there's enough water
			if (available >= amount) {
				consumeWater(foodStore, amount, waterOnly);
			}
			else if (available > 0) {
				amount = available;
				consumeWater(foodStore, amount, waterOnly);
			}
			else {
				// No water left
				logger.warning(person, "No water left to drink");
				endTask();
			}
		}
		else {
			endTask();
		}
	}

	/**
	 * Consumes the water.
	 * 
	 * @param containerUnit
	 * @param thirst
	 * @param amount
	 * @param waterOnly
	 */
	private void consumeWater(ResourceHolder rh, double amount, boolean waterOnly) {
		// Reduce thirst
		pc.reduceThirst(amount * THIRST_PER_WATER_SERVING);
		// Retrieve the water
		rh.retrieveAmountResource(ResourceUtil.WATER_ID, amount);
		// Record the amount consumed
		pc.recordFoodConsumption(amount, 3);

		if (waterOnly) {
			setDescription(DRINKING + " water");
		}

		if (pc.getThirst() < PhysicalCondition.THIRST_THRESHOLD / 6)
			endTask();
	}
	
	/**
	 * Gets a kitchen in the person's settlement that currently has cooked meals.
	 *
	 * @param person the person to check for
	 * @return the kitchen or null if none.
	 */
	public static Cooking getKitchenWithMeal(Person person) {
		Cooking result = null;

		if (person.isInSettlement()) {
			BuildingManager manager = person.getSettlement().getBuildingManager();
			Set<Building> cookingBuildings = manager.getBuildingSet(FunctionType.COOKING);
			for (Building building : cookingBuildings) {
				Cooking kitchen = building.getCooking();
				if (kitchen.hasCookedMeal()) {
					result = kitchen;
				}
			}
		}

		return result;
	}

	/**
	 * Throws away any napkins.
	 */
	@Override
	protected void clearDown() {
		// Throw away napkin waste if one was used.
		if (hasNapkin && person.isInside()) {
			((ResourceHolder)person.getContainerUnit()).storeAmountResource(ResourceUtil.SOLID_WASTE_ID, NAPKIN_MASS);
		}
	}
}
