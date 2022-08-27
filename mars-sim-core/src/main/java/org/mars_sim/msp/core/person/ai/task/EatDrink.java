/*
 * Mars Simulation Project
 * EatDrink.java
 * @date 2022-07-06
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.environment.MarsSurface;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.equipment.ResourceHolder;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.task.meta.HaveConversationMeta;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingCategory;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.Storage;
import org.mars_sim.msp.core.structure.building.function.cooking.CookedMeal;
import org.mars_sim.msp.core.structure.building.function.cooking.Cooking;
import org.mars_sim.msp.core.structure.building.function.cooking.PreparedDessert;
import org.mars_sim.msp.core.structure.building.function.cooking.PreparingDessert;
import org.mars_sim.msp.core.tool.Conversion;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The EatDrink class is a task for eating a meal. The duration of the task is 40
 * millisols. Note: Eating a meal reduces hunger to 0.
 */
public class EatDrink extends Task implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(EatDrink.class.getName());

	private static final int HUNGER_CEILING = 1000;
	private static final int THIRST_CEILING = 500;
	private static final int RATIO = 1000;
	
	/** The conversion ratio of thirst to one serving of water. */	
	private static final double RATIO_WATER = 75.0;
	/** The minimal amount of resource to be retrieved. */
	private static final double MIN = 0.0001;
	
	/** Task name */
	private static final String JUICE = "juice";
	private static final String MILK = "milk";
	private static final String NAME = Msg.getString("Task.description.eatDrink"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase LOOK_FOR_FOOD = new TaskPhase(Msg.getString("Task.phase.lookingforFood")); //$NON-NLS-1$
	private static final TaskPhase PICK_UP_DESSERT = new TaskPhase(Msg.getString("Task.phase.pickUpDessert")); //$NON-NLS-1$
	private static final TaskPhase EAT_MEAL = new TaskPhase(Msg.getString("Task.phase.eatingMeal")); //$NON-NLS-1$
	private static final TaskPhase EAT_PRESERVED_FOOD = new TaskPhase(Msg.getString("Task.phase.eatingFood")); //$NON-NLS-1$
	private static final TaskPhase EAT_DESSERT = new TaskPhase(Msg.getString("Task.phase.eatingDessert")); //$NON-NLS-1$
	private static final TaskPhase DRINK_WATER = new TaskPhase(Msg.getString("Task.phase.drinkingWater")); //$NON-NLS-1$

	// Static members
	private static final int FOOD_ID = ResourceUtil.foodID;
	private static final int WATER_ID = ResourceUtil.waterID;

	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = -1.2D;
	private static final double DESSERT_STRESS_MODIFIER = -.8D;
	private static final int NUMBER_OF_MEAL_PER_SOL = 3;
	private static final int NUMBER_OF_DESSERT_PER_SOL = 4;
	/** The proportion of the task for eating a meal. */
	private static final double MEAL_EATING_PROPORTION = .75D;
	/** The proportion of the task for eating dessert. */
	private static final double DESSERT_EATING_PROPORTION = .25D;
	/** Percentage chance that preserved food has gone bad. */
	// private static final double PRESERVED_FOOD_BAD_CHANCE = 1D; // in %
	/** Percentage chance that unprepared dessert has gone bad. */
	// private static final double UNPREPARED_DESSERT_BAD_CHANCE = 1D; // in %
	/** Mass (kg) of single napkin for meal. */
	private static final double NAPKIN_MASS = .0025D;

	private static double foodConsumptionRate;
	private static double dessertConsumptionRate;

	// Data members

	private int meals = 0;
	private int desserts = 0;
	private double foodAmount = 0;
	/** how much eaten [in kg]. */
	private double cumulativeProportion = 0;
	private double totalEatingTime = 0D;
	private double eatingDuration = 0D;
	private double totalDessertEatingTime = 0D;
	private double dessertEatingDuration = 0D;
	private double waterEachServing;

	private boolean hasNapkin = false;

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
		super(NAME, person, false, false, STRESS_MODIFIER, 20D
				+ RandomUtil.getRandomDouble(5D) - RandomUtil.getRandomDouble(5D));

		pc = person.getPhysicalCondition();

		double dur = getDuration();
		eatingDuration = dur * MEAL_EATING_PROPORTION;
		dessertEatingDuration = dur * DESSERT_EATING_PROPORTION;

		PersonConfig config = SimulationConfig.instance().getPersonConfig();
		foodConsumptionRate = config.getFoodConsumptionRate() / NUMBER_OF_MEAL_PER_SOL;
		dessertConsumptionRate = config.getDessertConsumptionRate() / NUMBER_OF_DESSERT_PER_SOL;
		// ~.3 kg per serving
		waterEachServing = pc.getWaterConsumedPerServing();

		////////////////////

		double waterAmount = 0;

		Unit container = person.getContainerUnit();
		if (container instanceof ResourceHolder) {
			ResourceHolder rh = (ResourceHolder) container;
			// Take preserved food from inventory if it is available.
			foodAmount = rh.getAmountResourceStored(FOOD_ID);
			waterAmount = rh.getAmountResourceStored(WATER_ID);
		}

		boolean food = false;
		boolean water = false;

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
			Building currentBuilding = BuildingManager.getBuilding(person);
			if (currentBuilding != null && currentBuilding.getCategory() == BuildingCategory.EVA_AIRLOCK) {
				// Walk out of the EVA Airlock
				walkToRandomLocation(false);
			}

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

		//////////////////////////////

		if (!food && !water) {
			endTask();
		}

		//////////////////////////////

		if (food) {

			if (person.isInSettlement()) {

				if (desserts > 0) {
					// Initialize task phase.
					addPhase(PICK_UP_DESSERT);
					addPhase(EAT_DESSERT);
				}

				if (meals > 0) {
					Building diningBuilding = EatDrink.getAvailableDiningBuilding(person, false);
					if (diningBuilding != null) {
						// Initialize task phase.
						addPhase(LOOK_FOR_FOOD);
						addPhase(EAT_MEAL);
					}

					boolean want2Chat = true;
					// See if a person wants to chat while eating
					int score = person.getPreference().getPreferenceScore(new HaveConversationMeta());
					if (score > 0)
						want2Chat = true;
					else if (score < 0)
						want2Chat = false;
					else {
						int rand = RandomUtil.getRandomInt(1);
						if (rand == 0)
							want2Chat = false;
					}

					diningBuilding = EatDrink.getAvailableDiningBuilding(person, want2Chat);
					if (diningBuilding != null)
						// Walk to that building.
						walkToActivitySpotInBuilding(diningBuilding, FunctionType.DINING, true);

					// Take napkin from inventory if available.
					if (person.getSettlement().retrieveAmountResource(ResourceUtil.napkinID, NAPKIN_MASS) > 0)
						hasNapkin = true;
				}

				else if (foodAmount > 0) {
					// Initialize task phase.
					addPhase(LOOK_FOR_FOOD);
					addPhase(EAT_PRESERVED_FOOD);
				}
			}

			else if (person.isInVehicle()) {

				if (desserts > 0) {
					// Initialize task phase.
					addPhase(PICK_UP_DESSERT);
					addPhase(EAT_DESSERT);
				}

				if (foodAmount > 0) {
					// Initialize task phase.
					addPhase(LOOK_FOR_FOOD);
					addPhase(EAT_PRESERVED_FOOD);
				}
			}
		}

		//////////////////////////////

		if (water) {
			// Initialize task phase.
			addPhase(DRINK_WATER);
			setPhase(DRINK_WATER);
		}
		else {
			// Initialize task phase.
			setPhase(LOOK_FOR_FOOD);
		}
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
			return drinkingWaterPhase(time);
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
	private double drinkingWaterPhase(double time) {
	
		// Call to consume water
		consumeWater(true);

		if (meals > 0 || foodAmount > 0)
			setPhase(LOOK_FOR_FOOD);
		else if (desserts > 0)
			setPhase(PICK_UP_DESSERT);
		else
			// Note: must call endTask here to end this task
			super.endTask();

		return 0;
	}

	/**
	 * Perform the pick up the food or the meal phase.
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
				// If no kitchen found, look for preserved food.
//				logger.info(person + " couldn't find a kitchen with cooked meals and will look for preserved food.");
				setPhase(EAT_PRESERVED_FOOD);
			}
		}

		if (kitchen != null) {
			// Walk to kitchen.
			boolean canWalk = walkToActivitySpotInBuilding(kitchen.getBuilding(), FunctionType.DINING, true);

			// Pick up a meal at kitchen if one is available.
			cookedMeal = kitchen.chooseAMeal(person);
			if (cookedMeal != null) {
				setPhase(EAT_MEAL);
//				setDescription(Msg.getString("Task.description.eatDrink.cooked.pickingUp.detail", cookedMeal.getName())); //$NON-NLS-1$
//				LogConsolidated.log(logger, Level.FINE, 0, sourceName,
//						"[" + person.getLocationTag().getLocale() + "] " + person
//								+ " picked up a cooked meal of '" + cookedMeal.getName()
//								+ "' to eat in " + person.getLocationTag().getImmediateLocation() + ".");
			}
			else {
//				logger.info(person + " couldn't find any cooked meals in this kitchen and will look for preserved food.");
				// If no kitchen found, look for preserved food.
				setPhase(EAT_PRESERVED_FOOD);
			}
			
			if (canWalk)
				return remainingTime;
		}

		return time;
	}


	/**
	 * Performs eating preserved food phase
	 *
	 * @param time
	 * @return
	 */
	private double eatingPreservedFoodPhase(double time) {
		double remainingTime = 0;
		double eatingTime = time;

		boolean enoughFood = eatPreservedFood(eatingTime);

		// If not enough preserved food available, change to dessert phase.
		if (!enoughFood) {
			endTask();
			return time;
		}
		else {
			// Report eating preserved food.
			setDescription(Msg.getString("Task.description.eatDrink.preserved")); //$NON-NLS-1$

			if ((totalEatingTime + eatingTime) >= eatingDuration) {
				eatingTime = eatingDuration - totalEatingTime;
			}

			if (eatingTime < time) {
				remainingTime = time - eatingTime;
			}

			if (cumulativeProportion >= foodConsumptionRate) {
				endTask();
				return remainingTime;
			}

			totalEatingTime += eatingTime;

			if (totalEatingTime > getDuration()) {
				endTask();
				return remainingTime;
			}

			consumeWater(false);
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
		double remainingTime = 0;
		double eatingTime = time;
		
		if (!person.isInVehicle()) {
			return time;
		}
		
		if ((totalEatingTime + eatingTime) >= eatingDuration) {
			eatingTime = eatingDuration - totalEatingTime;
		}

		if (eatingTime > 0 && cookedMeal != null) {
			String s = Msg.getString("Task.description.eatDrink.cooked.eating.detail", cookedMeal.getName());
			// Set descriptoin for eating cooked meal.
			setDescription(s); //$NON-NLS-1$
			// Eat cooked meal.
			eatCookedMeal(eatingTime);

			if (cumulativeProportion >= cookedMeal.getDryMass()) {
				endTask();
				return remainingTime;
			}

			// If finished eating, change to dessert phase.
			if (eatingTime < time) {
//						setPhase(PICK_UP_DESSERT);// EATING_DESSERT);
				remainingTime = time - eatingTime;
			}

			totalEatingTime += eatingTime;

			if (totalEatingTime > getDuration()) {
				logger.info(person, 10_000, "Done " + s.toLowerCase() + ".");
				endTask();
				return remainingTime;
			}

			consumeWater(false);

		} else {
			// Eat preserved food if available
			boolean enoughFood = eatPreservedFood(eatingTime);

			// If not enough preserved food available, change to dessert phase.
			if (!enoughFood) {
				setPhase(PICK_UP_DESSERT);
				return remainingTime;
			}
			else {
				// Report eating preserved food.
				setDescription(Msg.getString("Task.description.eatDrink.preserved")); //$NON-NLS-1$
				return remainingTime;
			}
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
					logger.log(worker, Level.FINE, 0, "Picked up prepared dessert '" 
							+ nameOfDessert.getName() + "' to eat/drink");
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
	 * Reduces the hunger level.
	 *
	 * @param hungerRelieved
	 */
	public void reduceHunger(double hungerRelieved) {
		// Note: once a person has eaten a bit of food,
		// the hunger index should be reset to HUNGER_CEILING
		double hunger = pc.getHunger();
		if (hunger > HUNGER_CEILING) {
			hunger = HUNGER_CEILING;
			pc.setHunger(hunger);
			return;
		}

		pc.reduceHunger(hungerRelieved);
	}

	/**
	 * Eats a cooked meal.
	 *
	 * @param eatingTime the amount of time (millisols) to eat.
	 */
	private void eatCookedMeal(double eatingTime) {
		// Obtain the dry mass of the dessert
		double dryMass = cookedMeal.getDryMass();
		// Proportion of meal being eaten over this time period.
		// eatingSpeed ~ 0.3 kg / millisols
		double proportion = person.getEatingSpeed() * eatingTime;

		if (cumulativeProportion > dryMass) {
			double excess = cumulativeProportion - dryMass;
			cumulativeProportion = cumulativeProportion - excess;
			proportion = proportion - excess;
		}

		if (proportion > MIN) {

			// Add to cumulativeProportion
			cumulativeProportion += proportion;
			// Food amount eaten over this period of time.
			double hungerRelieved = RATIO * proportion / dryMass;
			// Record the amount consumed
			pc.recordFoodConsumption(proportion, 1);
			// Change the hunger level after eating
			reduceHunger(hungerRelieved);

			logger.log(worker, Level.FINE, 1000, "Ate '" + cookedMeal.getName());

			// Reduce person's stress over time from eating a cooked meal.
			// This is in addition to normal stress reduction from eating task.
			double stressModifier = STRESS_MODIFIER * (cookedMeal.getQuality() + 1D);
			double deltaStress = stressModifier * eatingTime;
			pc.addStress(-deltaStress);

			// Add caloric energy from meal.
			double caloricEnergyFoodAmount = proportion / dryMass * PhysicalCondition.FOOD_COMPOSITION_ENERGY_RATIO;
			pc.addEnergy(caloricEnergyFoodAmount);
		}
	}

	/**
	 * Eats a meal of preserved food.
	 *
	 * @param eatingTime the amount of time (millisols) to eat.
	 * @return true if enough preserved food available to eat.
	 */
	private boolean eatPreservedFood(double eatingTime) {
		boolean result = true;

		// Proportion of food being eaten over this time period.
		double proportion = person.getEatingSpeed() * eatingTime;

		if ((cumulativeProportion + proportion) > foodConsumptionRate) {
			proportion = foodConsumptionRate - cumulativeProportion;
		}

		if (proportion > MIN) {

			Unit container = person.getContainerUnit();
			if (person.isInside()) {
				// Take preserved food from inventory if it is available.
				boolean haveFood = retrieveAnResource(proportion, FOOD_ID, container);

				if (haveFood) {
					// Record the amount consumed
					pc.recordFoodConsumption(proportion, 0);
					// Add to cumulativeProportion
					cumulativeProportion += proportion;
					// Food amount eaten over this period of time.
					double hungerRelieved = RATIO / foodConsumptionRate * proportion;
					// Consume preserved food after eating
					reduceHunger(hungerRelieved);
					// Add caloric energy from the preserved food.
					double caloricEnergyFoodAmount = PhysicalCondition.FOOD_COMPOSITION_ENERGY_RATIO / foodConsumptionRate * proportion;
					pc.addEnergy(caloricEnergyFoodAmount);

				} else {
					// Not enough food available to eat.
					result = false;
					// Need endTask() below to quit EatDrink
					endTask();
				}
			}
		} else {
			// Person is not inside a container unit, so end task.
			result = false;
			// Need endTask() below to quit EatDrink
			endTask();
		}

		if (totalEatingTime > getDuration())
			endTask();

		return result;
	}

	/**
	 * Gets a resource from a provider.
	 * TODO eventually everything will be a ResourceHolder
	 * 
	 * @param quantity amount to retrieve
	 * @param resourceID Resource to retrieve
	 * @param provider The provider of resources.
	 * @return Did retrieve all ?
	 */
	private boolean retrieveAnResource(double quantity, int resourceID, Unit provider) {
		boolean result = false;
		if (provider instanceof ResourceHolder) {
			ResourceHolder rh = (ResourceHolder) provider;
			return (rh.retrieveAmountResource(resourceID, quantity) == 0D);
		}

		return result;
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
			ResourceHolder rh = (ResourceHolder) provider;
			return rh.getAmountResourceStored(resourceID);
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

		double remainingTime = 0D;

		double eatingTime = time;
		if ((totalDessertEatingTime + eatingTime) >= dessertEatingDuration) {
			eatingTime = dessertEatingDuration - totalDessertEatingTime;
		}

		if (eatingTime > 0D) {

			if (nameOfDessert != null) {
				// Eat prepared dessert.
				checkInDescription(PreparingDessert.convertString2AR(nameOfDessert.getName()), true);
				eatPreparedDessert(eatingTime);

			} else {
				// Eat unprepared dessert (fruit, soymilk, etc).
				boolean enoughDessert = eatUnpreparedDessert(eatingTime);

				if (enoughDessert) {
					checkInDescription(unpreparedDessertAR, false);

					if (cumulativeProportion > nameOfDessert.getDryMass()) {
						endTask();
					}

					// If finished eating, end task.
					if (eatingTime < time) {
						remainingTime = time - eatingTime;
					}

					totalEatingTime += eatingTime;

					if (totalEatingTime > getDuration())
						endTask();

				}

				// If not enough unprepared dessert available, end task.
				else {// if (!enoughDessert) {
					remainingTime = time;
					// Need endTask() below to quit EatDrink
					endTask();
				}
			}
		}

		return remainingTime;
	}

	private void checkInDescription(AmountResource dessertAR, boolean prepared) {
		String s = dessertAR.getName();
		if (s.contains(MILK) || s.contains(JUICE)) {
			if (prepared)
				setDescription(
						Msg.getString("Task.description.eatDrink.preparedDessert.drink", Conversion.capitalize(s))); //$NON-NLS-1$
			else
				setDescription(
						Msg.getString("Task.description.eatDrink.unpreparedDessert.drink", Conversion.capitalize(s))); //$NON-NLS-1$
		} else {
			if (prepared)
				setDescription(Msg.getString("Task.description.eatDrink.preparedDessert.eat", Conversion.capitalize(s))); //$NON-NLS-1$
			else
				setDescription(
						Msg.getString("Task.description.eatDrink.unpreparedDessert.eat", Conversion.capitalize(s))); //$NON-NLS-1$
		}
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

		if (cumulativeProportion > dryMass) {
			double excess = cumulativeProportion - dryMass;
			cumulativeProportion = cumulativeProportion - excess;
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
							containerUnit);

				if (hasDessert) {
					// Record the amount consumed
					pc.recordFoodConsumption(proportion, 2);
					// Consume water
					consumeDessertWater(dryMass);
					// dessert amount eaten over this period of time.
					double hungerRelieved = RATIO * proportion / dryMass;
					// Consume unpreserved dessert.
					reduceHunger(hungerRelieved);

					// Reduce person's stress after eating a prepared dessert.
					// This is in addition to normal stress reduction from eating task.
					double stressModifier = DESSERT_STRESS_MODIFIER * (nameOfDessert.getQuality() + 1D);
					double deltaStress = stressModifier * eatingTime;
					pc.addStress(-deltaStress);

					// Add caloric energy from dessert.
					double caloricEnergy = proportion / dryMass * PhysicalCondition.FOOD_COMPOSITION_ENERGY_RATIO;
					pc.addEnergy(caloricEnergy);

//				} else {
					// Not enough dessert resource available to eat.
	//				result = false;
				}
			}
		}
	}

	/**
	 * Calculates the amount of water to consume during a dessert
	 *
	 * @param dryMass of the dessert
	 */
	private void consumeDessertWater(double dryMass) {
		double currentThirst = Math.min(pc.getThirst(), THIRST_CEILING);
		double waterFinal = Math.min(waterEachServing, currentThirst);
		// Note that the water content within the dessert has already been deducted from
		// the settlement
		// when the dessert was made.
		double waterPortion = 1000 * (PreparingDessert.getDessertMassPerServing() - dryMass);
		if (waterPortion > 0) {
			// Record the amount consumed
			pc.recordFoodConsumption(waterPortion, 3);

			waterFinal = waterFinal - waterPortion;
		}

		if (waterFinal > 0) {
			double newThirst = currentThirst - waterFinal * RATIO_WATER;
			if (newThirst < 0) {
				newThirst = 0;
				pc.setThirst(0);
			}
			else if (newThirst > THIRST_CEILING) {
				newThirst = THIRST_CEILING;
				pc.setThirst(THIRST_CEILING);
			}
			else
				pc.reduceThirst(currentThirst - newThirst);
			
			// Assume dessert can reduce stress
			pc.addStress(-waterFinal);
		}
	}

	/**
	 * Calculates the amount of water to consume
	 *
	 * @param is it water only
	 */
	private void consumeWater(boolean waterOnly) {

		if (!pc.isThirsty())
			return;

		double thirst = pc.getThirst();

		if (thirst > PhysicalCondition.THIRST_THRESHOLD / 2.0) {
			double currentThirst = Math.min(thirst, THIRST_CEILING);
			Unit containerUnit = person.getContainerUnit();
			EVASuit suit = null;

			if (containerUnit != null) {
				if (containerUnit instanceof MarsSurface) {
					// Doing EVA outside. Get water from one's EVA suit
					suit = person.getSuit();
				}
			}

			double waterFinal = Math.min(waterEachServing, currentThirst / RATIO_WATER);

			if (suit != null && waterFinal > 0) {
				double newThirst = currentThirst - waterFinal * RATIO_WATER;
				// Test to see if there's enough water
				boolean haswater = false;
				double amount = waterFinal;

				if (amount > MIN) {
					double available = suit.getAmountResourceStored(WATER_ID);
					
					if (available >= amount)
						haswater = true;
				}
				else
					return;

				if (haswater) {
					newThirst = newThirst - amount * RATIO_WATER;
					if (newThirst < 0) {
						newThirst = 0;
						pc.setThirst(0);
					}
					else if (newThirst > THIRST_CEILING) {
						newThirst = THIRST_CEILING;
						pc.setThirst(THIRST_CEILING);
					}
					else
						pc.reduceThirst(currentThirst - newThirst);

					if (amount > MIN) {
						suit.retrieveAmountResource(WATER_ID, amount);
						// Record the amount consumed
						pc.recordFoodConsumption(amount, 3);
						
						// Track the water consumption
						person.addConsumptionTime(WATER_ID, amount);
						if (waterOnly)
							setDescription(Msg.getString("Task.description.eatDrink.water")); //$NON-NLS-1$
					}
				}
			}

			else if (waterFinal > 0) {
				int level = person.getAssociatedSettlement().getWaterRationLevel();
				double newThirst = currentThirst - waterFinal * RATIO_WATER;
				double [] levels = {1D, 5D, 10D, 15D};

				// Try different level of water
				for (double levelModifier : levels) {
					// Test to see if there's enough water
					boolean haswater = false;
					double amount = Math.max(MIN, waterFinal / levelModifier / level);

					double available = getAmountResourceStored(containerUnit, WATER_ID);
					if (available >= amount)
						haswater = true;

					if (haswater) {
						newThirst = newThirst - amount * RATIO_WATER;
						if (newThirst < 0) {
							newThirst = 0;
							pc.setThirst(0);
						}
						else if (newThirst > THIRST_CEILING) {
							newThirst = THIRST_CEILING;
							pc.setThirst(THIRST_CEILING);
						}
						else
							pc.reduceThirst(currentThirst - newThirst);

						if (amount > MIN) {
							retrieveAnResource(amount, WATER_ID, containerUnit);
							// Record the amount consumed
							pc.recordFoodConsumption(amount, 3);
							// Track the water consumption
							person.addConsumptionTime(WATER_ID, amount);
							if (waterOnly)
								setDescription(Msg.getString("Task.description.eatDrink.water")); //$NON-NLS-1$
						}

						return;
					}
				}
			}
		}
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
			List<AmountResource> availableDessertResources = getAvailableDessertResources(dessertConsumptionRate,
					isThirsty);
			if (availableDessertResources.size() > 0) {

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

			if (cumulativeProportion > dryMass) {
				double excess = cumulativeProportion - dryMass;
				cumulativeProportion = cumulativeProportion - excess;
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
						double hungerRelieved = RATIO * proportion / dessertConsumptionRate;

						// Consume unpreserved dessert.
						reduceHunger(hungerRelieved);

						// Reduce person's stress after eating an unprepared dessert.
						// This is in addition to normal stress reduction from eating task.
						double stressModifier = DESSERT_STRESS_MODIFIER;
						double deltaStress = stressModifier * eatingTime;
						pc.addStress(-deltaStress);

						// Add caloric energy from dessert.
						double caloricEnergy = proportion / dessertConsumptionRate * PhysicalCondition.FOOD_COMPOSITION_ENERGY_RATIO;
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

			AmountResource[] ARs = PreparingDessert.getArrayOfDessertsAR();
			for (AmountResource ar : ARs) {
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
	 * Gets an available dining building that the person can use. Returns null if no
	 * dining building is currently available.
	 *
	 * @param person the person
	 * @return available dining building
	 * @throws BuildingException if error finding dining building.
	 */
	public static Building getAvailableDiningBuilding(Person person, boolean canChat) {
		Building b = person.getBuildingLocation();

		// If this person is located in the settlement
		if (person.isInSettlement()) {
			Settlement settlement = person.getSettlement();
			BuildingManager manager = settlement.getBuildingManager();
			List<Building> diningBuildings = manager.getBuildings(FunctionType.DINING);

			diningBuildings = BuildingManager.getWalkableBuildings(person, diningBuildings);
			if (canChat)
				// Choose between the most crowded or the least crowded dining hall
				diningBuildings = BuildingManager.getChattyBuildings(diningBuildings);
			else
				diningBuildings = BuildingManager.getLeastCrowdedBuildings(diningBuildings);

			if (diningBuildings.size() > 0) {
				Map<Building, Double> diningBuildingProbs = BuildingManager.getBestRelationshipBuildings(person,
						diningBuildings);
				b = RandomUtil.getWeightedRandomObject(diningBuildingProbs);
			}
		}

		return b;
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
