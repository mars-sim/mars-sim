/*
 * Mars Simulation Project
 * EatDrink.java
 * @date 2022-07-06
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.mars_sim.msp.core.LocalPosition;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.equipment.Container;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.equipment.ResourceHolder;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.task.meta.ConversationMeta;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskPhase;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingCategory;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.Function;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.Storage;
import org.mars_sim.msp.core.structure.building.function.cooking.CookedMeal;
import org.mars_sim.msp.core.structure.building.function.cooking.Cooking;
import org.mars_sim.msp.core.structure.building.function.cooking.PreparedDessert;
import org.mars_sim.msp.core.structure.building.function.cooking.PreparingDessert;
import org.mars_sim.msp.core.tool.Conversion;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Vehicle;

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
	private static final String NAME = Msg.getString("Task.description.eatDrink"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase LOOK_FOR_FOOD = new TaskPhase(Msg.getString("Task.phase.lookingforFood")); //$NON-NLS-1$
	private static final TaskPhase PICK_UP_DESSERT = new TaskPhase(Msg.getString("Task.phase.pickUpDessert")); //$NON-NLS-1$
	private static final TaskPhase EAT_MEAL = new TaskPhase(Msg.getString("Task.phase.eatingMeal")); //$NON-NLS-1$
	private static final TaskPhase EAT_PRESERVED_FOOD = new TaskPhase(Msg.getString("Task.phase.eatingFood")); //$NON-NLS-1$
	private static final TaskPhase EAT_DESSERT = new TaskPhase(Msg.getString("Task.phase.eatingDessert")); //$NON-NLS-1$
	private static final TaskPhase DRINK_WATER = new TaskPhase(Msg.getString("Task.phase.drinkingWater")); //$NON-NLS-1$

	private static final String JUICE = "juice";
	private static final String MILK = "milk";
	
	// Static members
	private static final int FOOD_ID = ResourceUtil.foodID;
	private static final int WATER_ID = ResourceUtil.waterID;

	private static final int NUMBER_OF_MEAL_PER_SOL = 3;
	private static final int NUMBER_OF_DESSERT_PER_SOL = 4;
	
	/** The conversion ratio of hunger to one serving of food. */	
	private static final int HUNGER_RATIO_PER_FOOD_SERVING = 300;
	/** The conversion ratio of thirst to one serving of water. */	
	private static final int THIRST_PER_WATER_SERVING = 450;
	/** The minimal amount of resource to be retrieved. */
	private static final double MIN = 0.001;
	
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = -1.2D;
	private static final double DESSERT_STRESS_MODIFIER = -.8D;
	/** Mass (kg) of single napkin for meal. */
	private static final double NAPKIN_MASS = .0025D;

	private double foodConsumedPerServing;
	private double dessertConsumedPerServing;

	private double millisolPerKgFood;
	private double millisolPerKgDessert;

	// Data members
	private boolean food = false;
	private boolean water = false;
	private boolean hasNapkin = false;
	
	private int meals = 0;
	private int desserts = 0;
	
	private double foodAmount = 0;
	/** how much eaten [in kg]. */
	private double cumulativeProportion = 0;
	private double totalEatingTime = 0D;
	private double eatingDuration = 0D;
	private double waterEachServing;

	private CookedMeal cookedMeal;
	private PreparedDessert nameOfDessert;
	private Cooking kitchen;
	private PreparingDessert dessertKitchen;
	private PhysicalCondition pc;
	private AmountResource unpreparedDessertAR;

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
		
		dessertConsumedPerServing = personConfig.getDessertConsumptionRate() / NUMBER_OF_DESSERT_PER_SOL;
		millisolPerKgDessert = HUNGER_RATIO_PER_FOOD_SERVING / dessertConsumedPerServing;
		
		// ~.03 kg per serving
		waterEachServing = pc.getWaterConsumedPerServing();

		////////////////////

		double waterAmount = 0;

		ResourceHolder rh = person;
		foodAmount = rh.getAmountResourceStored(FOOD_ID);
		waterAmount = rh.getAmountResourceStored(WATER_ID);
		
		Unit container = person.getContainerUnit();
		if (container instanceof ResourceHolder) {
			// Take preserved food from inventory if it is available.
			rh = (ResourceHolder) container;
			if (foodAmount == 0)
				foodAmount = rh.getAmountResourceStored(FOOD_ID);
			if (waterAmount == 0)
				waterAmount = rh.getAmountResourceStored(WATER_ID);
		}

		// Check if a cooked meal is available in a kitchen building at the settlement.
		Cooking kitchen0 = EatDrink.getKitchenWithMeal(person);
		if (kitchen0 != null) {
			meals = kitchen0.getNumberOfAvailableCookedMeals();
		}

		// Check dessert is available in a kitchen building at the settlement.
		PreparingDessert kitchen1 = EatDrink.getKitchenWithDessert(person);
		if (kitchen1 != null) {
			desserts = kitchen1.getAvailableServingsDesserts();
		}

		boolean hungry = pc.isHungry();
		boolean thirsty = pc.isThirsty();

		/////////////////////////////////////////////////

		if (person.isInSettlement()) {
			
			checkSettlement(hungry, thirsty, waterAmount);
		}

		else if (person.isInVehicle()) {
			
			checkVehicle(container, hungry, thirsty, waterAmount);
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
		
	private void checkSettlement(boolean hungry, boolean thirsty, double waterAmount) {
		Building currentBuilding = BuildingManager.getBuilding(person);
		if (currentBuilding != null && currentBuilding.getCategory() != BuildingCategory.EVA_AIRLOCK) {
			// Check if there is a local dining building.
        	Building diningBuilding = BuildingManager.getAvailableDiningBuilding(person.getSettlement(), person);
        	
        	if (diningBuilding != null) {
        		// Initiates a walking task to go back to the settlement
        		walkToDiningLoc(diningBuilding, false);
        	}			
		}

		if (hungry && (foodAmount > 0 || meals > 0 || desserts > 0)) {
			food = true;
		}

		if (thirsty && waterAmount > 0) {
			water = true;
		}
	}
	
	private void checkVehicle(Unit container, boolean hungry, boolean thirsty, double waterAmount) {
		 
		if (UnitType.VEHICLE == container.getUnitType()) {

			ResourceHolder rh = (ResourceHolder) container;
			if (foodAmount == 0)
				foodAmount = rh.getAmountResourceStored(FOOD_ID);
			if (waterAmount == 0)
				waterAmount = rh.getAmountResourceStored(WATER_ID);
			
			if (hungry && (foodAmount > 0 || desserts > 0)) {
				food = true;
			}

			if (thirsty && waterAmount > 0) {
				water = true;
			}
		}
		
		else {
			if (hungry && (foodAmount > 0 || desserts > 0)) {
				food = true;
			}

			if (thirsty && waterAmount > 0) {
				water = true;
			}
		}
	}
	
	private void goForFood() {

		if (person.isInSettlement()) {

			if (CookMeal.isMealTime(person, 0)) {
				
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
		int score = person.getPreference().getPreferenceScore(new ConversationMeta());
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
		if (person.getSettlement().retrieveAmountResource(ResourceUtil.napkinID, NAPKIN_MASS) > 0)
			hasNapkin = true;
	}
	
	
	private void checkFoodDessertAmount() {
		
		if (foodAmount > 0 && desserts > 0) {
			
			int rand = RandomUtil.getRandomInt(5);
			if (rand == 0) {
				addPhase(PICK_UP_DESSERT);
				addPhase(EAT_DESSERT);
				setPhase(LOOK_FOR_FOOD);
			}
			else {
				addPhase(LOOK_FOR_FOOD);
				addPhase(EAT_PRESERVED_FOOD);
				setPhase(LOOK_FOR_FOOD);
			}
		}
				
		else if (desserts > 0) {
			// Initialize task phase.
			addPhase(PICK_UP_DESSERT);
			addPhase(EAT_DESSERT);
			setPhase(LOOK_FOR_FOOD);
		}

		else if (foodAmount > 0) {
			// Initialize task phase.
			addPhase(LOOK_FOR_FOOD);
			addPhase(EAT_PRESERVED_FOOD);
			setPhase(LOOK_FOR_FOOD);
		}
	}
	
	/**
	 * Walks to an activity in the dining building.
	 * 
	 * @param building  the dining building.
	 * @param allowFail true if walking is allowed to fail.
	 */
	protected void walkToDiningLoc(Building building, boolean allowFail) {

		LocalPosition pos = findDiningSpot(building);

		if (pos != null) {
			// Create subtask for walking to destination.
			createWalkingSubtask(building, pos, allowFail);
		}
	}

	/**
	 * Finds a dining spot in this building.
	 * 
	 * @param building
	 * @return
	 */
	private LocalPosition findDiningSpot(Building building) {

		LocalPosition loc = building.getFunction(FunctionType.DINING).getAvailableActivitySpot(person);
		
		if (loc != null) {
			return loc;
		}
		
		Function f = building.getEmptyActivitySpotFunction();
		if (f == null) {
			return null;
		}

		if (person != null) {
			// Find available activity spot in building.
			loc = f.getAvailableActivitySpot(person);
		} else {
			// Find available activity spot in building.
			loc = f.getAvailableActivitySpot(robot);
		}

		return loc;
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
		} else if (PICK_UP_DESSERT.equals(getPhase())) {
			return pickingUpDessertPhase(time);
		} else if (EAT_DESSERT.equals(getPhase())) {
			return eatingDessertPhase(time);
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
				choosePreservedFoodOrDessert();
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
			choosePreservedFoodOrDessert();
			return time * .75;
		}
		
		if (canWalk)
			return remainingTime * .75;

		return remainingTime;
	}

	/**
	 * Chooses either preserved food or dessert randomly
	 */
	private void choosePreservedFoodOrDessert() {
		int rand = RandomUtil.getRandomInt(5);
		if (rand == 0) {
			addPhase(EAT_DESSERT);
			setPhase(EAT_DESSERT);
		}
		else {
			addPhase(EAT_PRESERVED_FOOD);
			setPhase(EAT_PRESERVED_FOOD);
		}
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

		// If not enough preserved food available, change to dessert phase.
		if (proportion == 0.0) {
			endTask();
			return time;
		}
		else {
			// Report eating preserved food.
			setDescription(Msg.getString("Task.description.eatDrink.preserved")); //$NON-NLS-1$

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
			
			String s = Msg.getString("Task.description.eatDrink.cooked.eating.detail", cookedMeal.getName());
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
	 * Performs the pick up dessert phase.
	 *
	 * @param time the amount of time (millisol) to perform the phase.
	 * @return the remaining time (millisol) after the phase has been performed.
	 */
	private double pickingUpDessertPhase(double time) {
		double remainingTime = 0;

		// Determine preferred kitchen to get dessert.
		if (dessertKitchen == null) {
			dessertKitchen = getKitchenWithDessert(person);

			if (dessertKitchen != null) {
				// Walk to dessert kitchen.
				boolean canWalk = walkToActivitySpotInBuilding(dessertKitchen.getBuilding(), FunctionType.DINING, true);

				// Pick up a dessert at kitchen if one is available.
				nameOfDessert = dessertKitchen.chooseADessert(person);

				if (nameOfDessert != null) {
					logger.log(worker, Level.FINE, 4_000, "Picked up a serving of '" 
							+ nameOfDessert.getName() + ".");
					setPhase(EAT_DESSERT);
					return remainingTime;
				}
				
				if (canWalk)
					return remainingTime;
			}

			else {
				endTask();
				// If no dessert kitchen found, go eat preserved food
				return time;
			}
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

		if (proportion > MIN) {

			Unit container = person.getContainerUnit();
			if (person.isInside()) {
				// Take preserved food from inventory if it is available.
				boolean haveFood = retrieveAnResource(proportion, FOOD_ID, (ResourceHolder)container);

				if (haveFood) {
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
			}
		} else {
			// Person is not inside a container unit, will end this task.
			proportion = 0;
		}

		return proportion;
	}

	/**
	 * Gets a resource from a provider.
	 * 
	 * @param quantity amount to retrieve
	 * @param resourceID Resource to retrieve
	 * @param provider The provider of resources.
	 * @return Did retrieve all ?
	 */
	private boolean retrieveAnResource(double quantity, int resourceID, ResourceHolder provider) {
		return provider.retrieveAmountResource(resourceID, quantity) == 0D;
	}


	/**
	 * Gets the amount of resource stored by a provider.
	 * 
	 * @param provider
	 * @param resourceID
	 * @return
	 */
	private double getAmountResourceStored(Unit provider, int resourceID) {
		if (provider instanceof ResourceHolder) {
			return ((ResourceHolder) provider).getAmountResourceStored(resourceID);
		}

		return 0;
	}

	/**
	 * Performs the eating dessert phase of the task.
	 *
	 * @param time the amount of time (millisol) to perform the eating dessert
	 *             phase.
	 * @return the amount of time (millisol) left after performing the eating
	 *         dessert phase.
	 */
	private double eatingDessertPhase(double time) {
		
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
		
		double remainingTime = 0D;
		double eatingTime = time;
		
		if ((totalEatingTime + eatingTime) >= eatingDuration) {
			eatingTime = eatingDuration - totalEatingTime;
		}

		if (eatingTime > 0D) {

			if (nameOfDessert != null) {
				// Display what dessert to east
				showDessertDescription(PreparingDessert.convertString2AR(nameOfDessert.getName()), true);
				// Eat a serving of prepared dessert
				eatPreparedDessert(eatingTime);

			} else {
				// Eat a serving of unprepared dessert (fruit, soymilk, etc).
				boolean enoughDessert = eatUnpreparedDessert(eatingTime);

				if (enoughDessert) {
					// Display what dessert to east
					showDessertDescription(unpreparedDessertAR, false);
					// Obtain the dry mass of the dessert
					double dryMass = PreparingDessert.getDryMass(PreparingDessert.convertAR2String(unpreparedDessertAR));
		
					if (cumulativeProportion> dryMass) {
						endTask();
					}

					// If finished eating, end task.
					if (eatingTime < time) {
						remainingTime = time - eatingTime;
					}
					
					totalEatingTime += eatingTime;
				}

				// If not enough unprepared dessert available, end task.
				else {
					remainingTime = time;
					// Need endTask() below to quit EatDrink
					endTask();
				}
			}
		}

		return remainingTime;
	}

	private void showDessertDescription(AmountResource dessertAR, boolean prepared) {
		String s = dessertAR.getName();
		String text = "";
		if (s.contains(MILK) || s.contains(JUICE)) {
			if (prepared) {
				text = Msg.getString("Task.description.eatDrink.preparedDessert.drink", Conversion.capitalize(s)); //$NON-NLS-1$
			}
			else {
				text = Msg.getString("Task.description.eatDrink.unpreparedDessert.drink", Conversion.capitalize(s)); //$NON-NLS-1$
			}
			
		} else {
			if (prepared) {
				text = Msg.getString("Task.description.eatDrink.preparedDessert.eat", Conversion.capitalize(s)); //$NON-NLS-1$
			}
				
			else {
				text = Msg.getString("Task.description.eatDrink.unpreparedDessert.eat", Conversion.capitalize(s)); //$NON-NLS-1$
			}
		}
		
		setDescription(text);
		logger.log(worker, Level.FINE, 4_000, text + ".");
	}

	/**
	 * Eats a prepared dessert.
	 *
	 * @param eatingTime the amount of time (millisols) to eat.
	 */
	private void eatPreparedDessert(double eatingTime) {
		// Obtain the dry mass of the dessert
		double dryMass = nameOfDessert.getDryMass();
		// Proportion of dessert being eaten over this time period.
		double proportion = person.getEatingSpeed() * eatingTime;

		if (cumulativeProportion + proportion > dryMass) {
			double excess = cumulativeProportion + proportion - dryMass;
			proportion = proportion - excess;
		}
		
		if (proportion > MIN) {
			// Dessert amount eaten over this period of time.
			Unit containerUnit = person.getTopContainerUnit();

			if (containerUnit != null) {
				// Take dessert resource from inventory if it is available.
				// Add to cumulativeProportion
				cumulativeProportion += proportion;

				boolean hasDessert = retrieveAnResource(proportion,
							ResourceUtil.findIDbyAmountResourceName(nameOfDessert.getName()),
							(ResourceHolder)containerUnit);

				if (hasDessert) {
					// Record the amount consumed
					pc.recordFoodConsumption(proportion, 2);
					// Consume water
					consumeDessertWater(dryMass);
					// dessert amount eaten over this period of time.
					double hungerRelieved = millisolPerKgDessert * proportion;
					// Consume unpreserved dessert.
					pc.reduceHunger(hungerRelieved);

					// Reduce person's stress after eating a prepared dessert.
					// This is in addition to normal stress reduction from eating task.
					double stressModifier = DESSERT_STRESS_MODIFIER * (nameOfDessert.getQuality() + 1D);
					double deltaStress = stressModifier * eatingTime;
					pc.reduceStress(deltaStress);

					// Add caloric energy from dessert.
					double caloricEnergy = hungerRelieved * PhysicalCondition.FOOD_COMPOSITION_ENERGY_RATIO;
					pc.addEnergy(caloricEnergy);
				}
			}
		}
	}

	/**
	 * Calculates the amount of water to consume during a dessert.
	 *
	 * @param dryMass of the dessert
	 */
	private void consumeDessertWater(double dryMass) {
		// Note that the water content within the dessert has already been deducted from
		// the settlement when the dessert was made.
		double proportion = PreparingDessert.getDessertMassPerServing() - dryMass;
		if (proportion > 0) {
			// Record the dessert amount consumed
			pc.recordFoodConsumption(proportion, 2);
			// Record the water amount consumed
			pc.recordFoodConsumption(proportion, 3);
			// Reduce thirst
			pc.reduceThirst(proportion * THIRST_PER_WATER_SERVING);
			// Assume dessert can reduce stress
			pc.reduceStress(proportion * 2);
		}
	}

	/**
	 * Calculates the amount of water to consume.
	 *
	 * @param is it water only
	 */
	private void calculateWater(boolean waterOnly) {

		double amount = RandomUtil.getRandomDouble(waterEachServing / 2, waterEachServing);

		Unit containerUnit = person.getContainerUnit();
		
		EVASuit suit = null;

		if (containerUnit == null) {
			logger.fine(person, 4_000L, "'s container unit is null.");
			endTask();
		}
		
		else if (containerUnit.getUnitType() == UnitType.PLANET) {
			// Doing EVA outside. Get water from one's EVA suit
			suit = person.getSuit();
		}

		// Case 1: drink from EVA suit
		
		if (suit != null) {
			double available = suit.getAmountResourceStored(WATER_ID);
			
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

		// Case 2: drink from bottle when being inside the settlement or vehicle
		else {
			
			// Case 0: drink from thermal bottle
			double availableAmount = 0;
			
			// Get the bottle the person is carrying
			Container bottle = person.lookForThermalBottle();
					
			if (bottle == null && person.isInside()) {
				// Assign the person a thermal bottle
				bottle = person.assignThermalBottle();
				logger.fine(person, 4_000L, "Assigned a thermal bottle.");
			}

			if (bottle != null)  {
				
				availableAmount = bottle.getAmountResourceStored(WATER_ID);
		
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
				if (availableAmount == 0.0 && containerUnit != null) {
					// Retrieve the water from settlement/vehicle
					double missing = ((ResourceHolder)containerUnit).retrieveAmountResource(WATER_ID, 1);
					// Fill up the bottle with water
					if (missing < 1) {
						amount = 1 - missing;
						person.fillUpThermalBottle(amount);
					}
				}	
			}
			
			if (containerUnit != null)  {
				int level = person.getAssociatedSettlement().getWaterRationLevel();
				amount = Math.max(MIN, amount / level);
				
				// for either in settlement or vehicle
				double available = getAmountResourceStored(containerUnit, WATER_ID);
				// Test to see if there's enough water
				if (available >= amount) {
					consumeWater((ResourceHolder)containerUnit, amount, waterOnly);
				}
				else if (available > 0) {
					amount = available;
					consumeWater((ResourceHolder)containerUnit, amount, waterOnly);
				}
			}
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
		rh.retrieveAmountResource(WATER_ID, amount);
		// Record the amount consumed
		pc.recordFoodConsumption(amount, 3);

		if (waterOnly) {
			setDescription(Msg.getString("Task.description.eatDrink.water")); //$NON-NLS-1$
		}

		if (pc.getThirst() < PhysicalCondition.THIRST_THRESHOLD / 6)
			endTask();
	}

	/**
	 * Eat an unprepared dessert.
	 *
	 * @param eatingTime the amount of time (millisols) to eat.
	 * @return true if enough unprepared dessert was available to eat.
	 */
	private boolean eatUnpreparedDessert(double eatingTime) {

		boolean result = true;

		// Determine dessert resource type if not known.
		if (unpreparedDessertAR == null) {

			boolean isThirsty = pc.getThirst() > 50;
            // Determine list of available dessert resources.
			List<AmountResource> availableDessertResources = getAvailableDessertResources(dessertConsumedPerServing,
					isThirsty);
			
			if (!availableDessertResources.isEmpty()) {
				// Randomly choose available dessert resource.
				int index = RandomUtil.getRandomInt(availableDessertResources.size() - 1);
				unpreparedDessertAR = availableDessertResources.get(index);
			} else {
				result = false;
			}
		}

		// Consume portion of unprepared dessert resource.
		if (unpreparedDessertAR != null) {

			// Obtain the dry mass of the dessert
			double dryMass = PreparingDessert.getDryMass(PreparingDessert.convertAR2String(unpreparedDessertAR));
			// Proportion of dessert being eaten over this time period.
			double proportion = person.getEatingSpeed() * eatingTime;

			if (cumulativeProportion + proportion > dryMass) {
				double excess = cumulativeProportion + proportion - dryMass;
				proportion = proportion - excess;
			}

			if (proportion > MIN) {

				Unit containerUnit = person.getTopContainerUnit();

				if (containerUnit != null) {
					// Add to cumulativeProportion
					cumulativeProportion += proportion;
					// Take dessert resource from inventory if it is available.
					boolean hasDessert = Storage.retrieveAnResource(proportion, unpreparedDessertAR, (ResourceHolder) containerUnit, true);

					if (hasDessert) {
						// Consume water inside the dessert
						consumeDessertWater(dryMass);
						// Record the amount consumed
						pc.recordFoodConsumption(proportion, 2);
						// dessert amount eaten over this period of time.
						double hungerRelieved = millisolPerKgDessert * proportion;

						// Consume unpreserved dessert.
						pc.reduceHunger(hungerRelieved);

						// Reduce person's stress after eating an unprepared dessert.
						// This is in addition to normal stress reduction from eating task.
						double stressModifier = DESSERT_STRESS_MODIFIER;
						double deltaStress = stressModifier * eatingTime;
						pc.reduceStress(deltaStress);

						// Add caloric energy from dessert.
						double caloricEnergy = hungerRelieved * PhysicalCondition.FOOD_COMPOSITION_ENERGY_RATIO;
						pc.addEnergy(caloricEnergy);
					} else {
						// Not enough dessert resource available to eat.
						result = false;
					}
				}
			}
		}

		return result;
	}

	/**
	 * Gets a list of available unprepared dessert AmountResource.
	 *
	 * @param amountNeeded the amount (kg) of unprepared dessert needed for eating.
	 * @return list of AmountResource.
	 */
	private List<AmountResource> getAvailableDessertResources(double amountNeeded, boolean isThirsty) {

		List<AmountResource> result = new ArrayList<>();

		Unit containerUnit = person.getContainerUnit();

		if (containerUnit.getUnitType() != UnitType.PLANET) {
			ResourceHolder rh = (ResourceHolder)containerUnit;
			boolean option = true;

			AmountResource[] resources = PreparingDessert.getArrayOfDessertsAR();
			for (AmountResource ar : resources) {
				if (isThirsty)
					option = ar.getName().contains(JUICE) || ar.getName().contains(MILK);

				boolean hasAR = false;
				if (amountNeeded > MIN) {
					hasAR = Storage.retrieveAnResource(amountNeeded, ar, rh, false);
				}
				if (option && hasAR) {
					result.add(ar);
				}
			}
		}

		return result;
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
			List<Building> cookingBuildings = manager.getBuildings(FunctionType.COOKING);
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
	 * Gets a kitchen in the person's settlement that currently has prepared
	 * desserts.
	 *
	 * @param person the person to check for
	 * @return the kitchen or null if none.
	 */
	public static PreparingDessert getKitchenWithDessert(Person person) {
		PreparingDessert result = null;
		Settlement settlement = person.getSettlement();

		if (settlement != null) {
			BuildingManager manager = settlement.getBuildingManager();
			List<Building> dessertBuildings = manager.getBuildings(FunctionType.PREPARING_DESSERT);
			for (Building building : dessertBuildings) {
				PreparingDessert kitchen = building.getPreparingDessert();
				if (kitchen.hasFreshDessert()) {
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
			((ResourceHolder)person.getContainerUnit()).storeAmountResource(ResourceUtil.solidWasteID, NAPKIN_MASS);
		}
	}
}
