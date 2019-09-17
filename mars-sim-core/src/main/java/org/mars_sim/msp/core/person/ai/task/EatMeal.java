/**
 * Mars Simulation Project
 * EatMeal.java
 * @version 3.1.0 2017-09-13
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.mars.MarsSurface;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.meta.HaveConversationMeta;
import org.mars_sim.msp.core.person.ai.taskUtil.Task;
import org.mars_sim.msp.core.person.ai.taskUtil.TaskPhase;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
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
 * The EatMeal class is a task for eating a meal. The duration of the task is 40
 * millisols. Note: Eating a meal reduces hunger to 0.
 */
public class EatMeal extends Task implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static Logger logger = Logger.getLogger(EatMeal.class.getName());
	private static String loggerName = logger.getName();
	private static String sourceName = loggerName.substring(loggerName.lastIndexOf(".") + 1, loggerName.length());
	
	private static final int HUNGER_CEILING = 1000;
	private static final int THIRST_CEILING = 500;
	
	/** The minimal amount of resource to be retrieved. */
	private static final double MIN = 0.00001;
	
	/** Task name */
	private static final String NAME = Msg.getString("Task.description.eatMeal"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase PICK_UP_MEAL = new TaskPhase(Msg.getString("Task.phase.pickUpMeal")); //$NON-NLS-1$
	private static final TaskPhase PICK_UP_DESSERT = new TaskPhase(Msg.getString("Task.phase.pickUpDessert")); //$NON-NLS-1$
	private static final TaskPhase EAT_MEAL = new TaskPhase(Msg.getString("Task.phase.eatingMeal")); //$NON-NLS-1$
	private static final TaskPhase EAT_DESSERT = new TaskPhase(Msg.getString("Task.phase.eatingDessert")); //$NON-NLS-1$
	private static final TaskPhase DRINK_WATER = new TaskPhase(Msg.getString("Task.phase.drinkingWater")); //$NON-NLS-1$

	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = -.4D;
	private static final double DESSERT_STRESS_MODIFIER = -.4D;
	private static final int NUMBER_OF_MEAL_PER_SOL = 4;
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
	/** how much eaten [in kg]. */ 
	private double cumulativeProportion = 0;
	private double totalMealEatingTime = 0D;
	private double mealEatingDuration = 0D;
	private double totalDessertEatingTime = 0D;
	private double dessertEatingDuration = 0D;
	private double startingHunger;
	private double currentHunger;
	private double thirst;

	private double energy;
	private double waterEachServing;

	private boolean hasNapkin = false;
	private boolean notHungry;
	private boolean notThirsty;

	private CookedMeal cookedMeal;
	private PreparedDessert nameOfDessert;
	private Cooking kitchen;
	private PreparingDessert dessertKitchen;
	private PhysicalCondition condition;

	private AmountResource unpreparedDessertAR;

	/**
	 * Constructor.
	 * 
	 * @param person the person to perform the task
	 */
	public EatMeal(Person person) {
		super(NAME, person, false, false, STRESS_MODIFIER, true, 20D 
				+ RandomUtil.getRandomDouble(10D) - RandomUtil.getRandomDouble(5D));
		// 20 milisols ~ 30 mins

		sourceName = sourceName.substring(sourceName.lastIndexOf(".") + 1, sourceName.length());

		condition = person.getPhysicalCondition();

		waterEachServing = condition.getWaterConsumedPerServing() * 1000D; // about 300 (3 * 1000)

		energy = condition.getEnergy();
		startingHunger = condition.getHunger();
		currentHunger = startingHunger;
		thirst = condition.getThirst();

		notHungry = startingHunger < PhysicalCondition.HUNGER_THRESHOLD && energy > PhysicalCondition.ENERGY_THRESHOLD;
		notThirsty = thirst < PhysicalCondition.THIRST_THRESHOLD;
		
		if (!notThirsty && notHungry) {
			// if a person is thirsty and not hungry, whether he's inside or outside			
			addPhase(DRINK_WATER);
			setPhase(DRINK_WATER);
		}
		
		else if (notThirsty && notHungry) {
			// if a person is not thirsty and not hungry			
			endTask();
		}
		

		// Check if person is outside and is not thirsty
		if (person.isOutside()) {
			// Note : if a person is on EVA suit, he cannot eat 
			// but should be able to drink water from the helmet tube if he's thirsty				
			if (notThirsty) {
//				LogConsolidated.log(Level.WARNING, 3000, sourceName,
//					person + " was trying to eat a meal, but is not inside a settlement/vehicle.");
				endTask();
			}
			else {
				addPhase(DRINK_WATER);
				setPhase(DRINK_WATER);
			}
		}
		
		else if (person.isInVehicle()) {
			if (notHungry) {
				// Initialize task phase.
				addPhase(PICK_UP_DESSERT);
				addPhase(EAT_DESSERT);

				setPhase(PICK_UP_DESSERT);			
			} 
			
			else {
				// Initialize task phase.
				addPhase(PICK_UP_MEAL);
				addPhase(PICK_UP_DESSERT);
				addPhase(EAT_MEAL);
				addPhase(EAT_DESSERT);

				setPhase(PICK_UP_MEAL);
			}	
		}
		
		else if (person.isInSettlement()) {
			
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
			
			Building diningBuilding = EatMeal.getAvailableDiningBuilding(person, want2Chat);
			if (diningBuilding != null) {
				// Walk to that building.
				walkToActivitySpotInBuilding(diningBuilding, FunctionType.DINING, true);
				
				// Take napkin from inventory if available.
				Inventory inv = person.getSettlement().getInventory();
				if (inv != null) {
					if (NAPKIN_MASS > MIN)
						hasNapkin = Storage.retrieveAnResource(NAPKIN_MASS, ResourceUtil.napkinID, inv, false);
				}
			}

			// Initialize data members.
			double dur = getDuration();
			mealEatingDuration = dur * MEAL_EATING_PROPORTION;
			dessertEatingDuration = dur * DESSERT_EATING_PROPORTION;
	
			PersonConfig config = SimulationConfig.instance().getPersonConfig();
			foodConsumptionRate = config.getFoodConsumptionRate() / NUMBER_OF_MEAL_PER_SOL;
			dessertConsumptionRate = config.getDessertConsumptionRate() / NUMBER_OF_DESSERT_PER_SOL;
		
			// if a person is just a little thirsty and NOT that hungry
			if (notHungry) {
				// Initialize task phase.
				addPhase(PICK_UP_DESSERT);
				addPhase(EAT_DESSERT);

				setPhase(PICK_UP_DESSERT);			
			} 
			
			else {
				// Initialize task phase.
				addPhase(PICK_UP_MEAL);
				addPhase(PICK_UP_DESSERT);
				addPhase(EAT_MEAL);
				addPhase(EAT_DESSERT);

				setPhase(PICK_UP_MEAL);
			}
		}		
	}

	@Override
	public FunctionType getLivingFunction() {
		return FunctionType.DINING;
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
		} else if (DRINK_WATER.equals(getPhase())) {
			return drinkingWaterPhase(time);			
		} else if (PICK_UP_MEAL.equals(getPhase())) {
			return pickingUpMealPhase(time);
		} else if (EAT_MEAL.equals(getPhase())) {
			return eatingMealPhase(time);
		} else if (PICK_UP_DESSERT.equals(getPhase())) {
			return pickingUpDessertPhase(time);
		} else if (EAT_DESSERT.equals(getPhase())) {
			return eatingDessertPhase(time);
		} else {
			return time;
		}
	}

	/**
	 * Perform the pick up meal phase.
	 * 
	 * @param time the amount of time (millisol) to perform the phase.
	 * @return the remaining time (millisol) after the phase has been performed.
	 */
	private double pickingUpMealPhase(double time) {

		// Determine preferred kitchen to get meal.
		if (kitchen == null) {
			kitchen = getKitchenWithMeal(person);

			if (kitchen != null) {
				// Walk to kitchen.
				walkToActivitySpotInBuilding(kitchen.getBuilding(), FunctionType.DINING, true);
			} 
			
//			else {
//				// If no kitchen found, look for dessert.
//			}
		}

		if (kitchen != null) {
			// Pick up a meal at kitchen if one is available.
			cookedMeal = kitchen.chooseAMeal(person);
			if (cookedMeal != null) {
				LogConsolidated.log(Level.FINE, 0, sourceName,
						"[" + person.getLocationTag().getLocale() + "] " + person
								+ " picked up a cooked meal '" + cookedMeal.getName() 
								+ "' to eat in " + person.getLocationTag().getImmediateLocation() + ".");
			}
		}

		setPhase(EAT_MEAL);
		return time *.5;
	}

	/**
	 * Performs the drinking water phase of the task.
	 * 
	 * @param time the amount of time (millisol) to perform the drinking water phase.
	 * @return the amount of time (millisol) left after performing the drinking water
	 *         phase.
	 */
	private double drinkingWaterPhase(double time) {
		consumeWater(true);
		endTask();
		return time *.8;
	}
	
	/**
	 * Performs the eating meal phase of the task.
	 * 
	 * @param time the amount of time (millisol) to perform the eating meal phase.
	 * @return the amount of time (millisol) left after performing the eating meal
	 *         phase.
	 */
	private double eatingMealPhase(double time) {
		double remainingTime = 0D;
		double eatingTime = time;
		
		if (person.isInVehicle()) {
			// Eat preserved food.
			setDescription(Msg.getString("Task.description.eatMeal.preserved")); //$NON-NLS-1$
			boolean enoughFood = eatPreservedFood(eatingTime);

			// If not enough preserved food available, change to dessert phase.
			if (!enoughFood) {
				setPhase(PICK_UP_DESSERT);
				remainingTime = time * .6;
			}
		}
		
		else {
	
			if ((totalMealEatingTime + eatingTime) >= mealEatingDuration) {
				eatingTime = mealEatingDuration - totalMealEatingTime;
			}
	
			if (eatingTime > 0D) {
	
				if (cookedMeal != null) {
					// Eat cooked meal.
					setDescription(Msg.getString("Task.description.eatMeal.cooked.detail", cookedMeal.getName())); //$NON-NLS-1$
					eatCookedMeal(eatingTime);
				} else {
					// Eat preserved food.
					setDescription(Msg.getString("Task.description.eatMeal.preserved")); //$NON-NLS-1$
					boolean enoughFood = eatPreservedFood(eatingTime);
	
					// If not enough preserved food available, change to dessert phase.
					if (!enoughFood) {
						setPhase(PICK_UP_DESSERT);
						remainingTime = time * .6;
					}
					// else {
					// consumeWater(false);
					// }
				}
			}

		}
		
		totalMealEatingTime += eatingTime;

		// If finished eating, change to dessert phase.
		if (eatingTime < time) {
			setPhase(PICK_UP_DESSERT);// EATING_DESSERT);
			remainingTime = time * .6 - eatingTime;
		}

		consumeWater(false);

		return remainingTime;
	}

	/**
	 * Perform the pick up dessert phase.
	 * 
	 * @param time the amount of time (millisol) to perform the phase.
	 * @return the remaining time (millisol) after the phase has been performed.
	 */
	private double pickingUpDessertPhase(double time) {

		// Determine preferred kitchen to get dessert.
		if (dessertKitchen == null) {
			dessertKitchen = getKitchenWithDessert(person);

			if (dessertKitchen != null) {
				// Walk to dessert kitchen.
				walkToActivitySpotInBuilding(dessertKitchen.getBuilding(), FunctionType.DINING, true);
			} else {
				// If no dessert kitchen found, go eat meal ?
			}
		}

		if (dessertKitchen != null) {
			// Pick up a dessert at kitchen if one is available.
			nameOfDessert = dessertKitchen.chooseADessert(person);
			if (nameOfDessert != null) {
				LogConsolidated.log(Level.FINE, 0, sourceName,
						"[" + person.getLocationTag().getLocale() + "] " + person
								+ " picked up prepared dessert '" + nameOfDessert.getName() 
								+ "' to eat/drink in " + person.getLocationTag().getImmediateLocation() + ".");
			}
		}

		// if one can't find a dessert kitchen, i.e. in a vehicle
		setPhase(EAT_DESSERT);
		
		return time *.8;
	}

	/**
	 * Eat a cooked meal.
	 * 
	 * @param eatingTime the amount of time (millisols) to eat.
	 */
	private void eatCookedMeal(double eatingTime) {
		double dryMass = cookedMeal.getDryMass();
		// Proportion of meal being eaten over this time period.
//		double mealProportion = eatingTime / mealEatingDuration;
		double proportion = person.getEatingSpeed() * eatingTime;
		cumulativeProportion += proportion;

		if (cumulativeProportion > dryMass) {
			double excess = cumulativeProportion - dryMass;
			cumulativeProportion = cumulativeProportion - excess;
			proportion = proportion - excess;
		}

		// Food amount eaten over this period of time.
//		double foodAmount = foodConsumptionRate * mealProportion;
		double hungerRelieved = 1000 * proportion / dryMass;
				
		// Note: once a person has eaten a bit of food,
		// the hunger index should be reset to HUNGER_CEILING
		if (currentHunger > HUNGER_CEILING)
			currentHunger = HUNGER_CEILING;
		condition.setHunger(currentHunger);
		currentHunger = condition.getHunger();
		// Reduce person's hunger by proportion of meal eaten.
		// Entire meal will reduce person's hunger to 0.
		currentHunger -= (startingHunger * hungerRelieved);
		if (currentHunger < 0D) {
			currentHunger = 0D;
		}

		condition.setHunger(currentHunger);

		// Reduce person's stress over time from eating a cooked meal.
		// This is in addition to normal stress reduction from eating task.
		double mealStressModifier = STRESS_MODIFIER * (cookedMeal.getQuality() + 1D);
		double newStress = condition.getStress() - (mealStressModifier * eatingTime);
		condition.setStress(newStress);

		// Add caloric energy from meal.
		double caloricEnergyFoodAmount = proportion / dryMass * PhysicalCondition.FOOD_COMPOSITION_ENERGY_RATIO;
		condition.addEnergy(caloricEnergyFoodAmount);

	}

	/**
	 * Eat a meal of preserved food.
	 * 
	 * @param eatingTime the amount of time (millisols) to eat.
	 * @return true if enough preserved food available to eat.
	 */
	private boolean eatPreservedFood(double eatingTime) {
		boolean result = true;
		// Determine total preserved food amount eaten during this meal.
		// double totalFoodAmount = config.getFoodConsumptionRate() / NUMBER_OF_MEAL_PER_SOL;

		// Proportion of meal being eaten over this time period.
//		double mealProportion = eatingTime / mealEatingDuration;
		double proportion = person.getEatingSpeed() * eatingTime;
		cumulativeProportion += proportion;

		if (cumulativeProportion > foodConsumptionRate) {
			double excess = cumulativeProportion - foodConsumptionRate;
			cumulativeProportion = cumulativeProportion - excess;
			proportion = proportion - excess;
		}
		
		// Food amount eaten over this period of time.
//		double foodAmount = foodConsumptionRate * mealProportion;
		double hungerRelieved = 1000 * proportion / foodConsumptionRate;
		
		Unit container = person.getContainerUnit();
		if (person.isInside()) {//!(container instanceof MarsSurface)) {
			Inventory inv = container.getInventory();

			// Take preserved food from inventory if it is available.
			boolean haveFood = false;
			if (proportion > MIN)
				haveFood = Storage.retrieveAnResource(proportion, ResourceUtil.foodID, inv, true);
			
			if (haveFood) {
//				LogConsolidated.log(Level.INFO, 1000, sourceName,
//						"[" + person.getLocationTag().getLocale() + "] " + person 
//						+ " just ate " + proportion + " kg of preserved food.");
				// Consume preserved food.
				// Note: once a person has eaten a bit of food,
				// the hunger index should be reset to HUNGER_CEILING
				if (currentHunger > HUNGER_CEILING)
					currentHunger = HUNGER_CEILING;		
				condition.setHunger(currentHunger);
				currentHunger = condition.getHunger();
				// Note : Reduce person's hunger by proportion of meal eaten.
				// Entire meal will reduce person's hunger to 0.
				currentHunger -= (startingHunger * hungerRelieved);
				if (currentHunger < 0D) {
					currentHunger = 0D;
				}

				condition.setHunger(currentHunger);

				// Add caloric energy from the prserved food.
				double caloricEnergyFoodAmount = proportion / foodConsumptionRate * PhysicalCondition.FOOD_COMPOSITION_ENERGY_RATIO;
				condition.addEnergy(caloricEnergyFoodAmount);

			} else {
				// Not enough food available to eat.
				result = false;
			}
		} else {
			// Person is not inside a container unit, so end task.
			result = false;
			endTask();
		}

		return result;
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
				}

				// If not enough unprepared dessert available, end task.
				else {// if (!enoughDessert) {
					remainingTime = time;
					endTask();
				}
			}
		}

		totalMealEatingTime += eatingTime;

		// If finished eating, end task.
		if (eatingTime < time) {
			remainingTime = time - eatingTime;
			endTask();
		}

		return remainingTime;
	}

	private void checkInDescription(AmountResource dessertAR, boolean prepared) {
		String s = dessertAR.getName();
		if (s.contains("milk") || s.contains("juice")) {
			if (prepared)
				setDescription(
						Msg.getString("Task.description.eatMeal.preparedDessert.drink", Conversion.capitalize(s))); //$NON-NLS-1$
			else
				setDescription(
						Msg.getString("Task.description.eatMeal.unpreparedDessert.drink", Conversion.capitalize(s))); //$NON-NLS-1$
		} else {
			if (prepared)
				setDescription(Msg.getString("Task.description.eatMeal.preparedDessert.eat", Conversion.capitalize(s))); //$NON-NLS-1$
			else
				setDescription(
						Msg.getString("Task.description.eatMeal.unpreparedDessert.eat", Conversion.capitalize(s))); //$NON-NLS-1$
		}
	}

	/**
	 * Eat a prepared dessert.
	 * 
	 * @param eatingTime the amount of time (millisols) to eat.
	 */
	private void eatPreparedDessert(double eatingTime) {
		// Obtain the dry mass of the dessert
		double dryMass = nameOfDessert.getDryMass();
		// Proportion of dessert being eaten over this time period.
		double proportion = person.getEatingSpeed() * eatingTime;
		cumulativeProportion += proportion;

		if (cumulativeProportion > dryMass) {
			double excess = cumulativeProportion - dryMass;
			cumulativeProportion = cumulativeProportion - excess;
			proportion = proportion - excess;
		}
		
		// dessert amount eaten over this period of time.
		double hungerRelieved = 1000 * proportion / dryMass;
		
		// Reduce person's stress over time from eating a prepared.
		// This is in addition to normal stress reduction from eating task.
		double mealStressModifier = DESSERT_STRESS_MODIFIER * (nameOfDessert.getQuality() + 1D);
		double newStress = condition.getStress() - (mealStressModifier * eatingTime);
		condition.setStress(newStress);
		
		// Dessert amount eaten over this period of time.
//		double dessertAmount = dessertConsumptionRate * dessertProportion;
		Unit containerUnit = person.getTopContainerUnit();
		
		if (containerUnit != null) {
			Inventory inv = containerUnit.getInventory();
			
			// Take dessert resource from inventory if it is available.
			boolean hasDessert = false;
			if (proportion > MIN) {
				hasDessert = Storage.retrieveAnResource(proportion, nameOfDessert.getName(), inv, true);
			}
			if (hasDessert) {
				// Consume water
				consumeDessertWater(dryMass);
				// Consume unpreserved dessert.
				if (currentHunger > HUNGER_CEILING)
					currentHunger = HUNGER_CEILING;		
				condition.setHunger(currentHunger);
				currentHunger = condition.getHunger();
				// Note : Reduce person's hunger by proportion of dessert eaten.
				// Entire dessert will reduce person's hunger to 0.
				currentHunger -= (startingHunger * hungerRelieved);
				if (currentHunger < 0D) {
					currentHunger = 0D;
				}
				condition.setHunger(currentHunger);
				
				// Add caloric energy from dessert.
				double caloricEnergy = proportion / dryMass * PhysicalCondition.FOOD_COMPOSITION_ENERGY_RATIO;
				condition.addEnergy(caloricEnergy);
				
			} else {
				// Not enough dessert resource available to eat.
//				result = false;
			}

		}

	}

	/**
	 * Calculates the amount of water to consume during a dessert
	 * 
	 * @param dryMass of the dessert
	 */
	public void consumeDessertWater(double dryMass) {
		double currentThirst = Math.min(thirst, 1_000);
		double waterFinal = Math.min(waterEachServing, currentThirst);
		// Note that the water content within the dessert has already been deducted from
		// the settlement
		// when the dessert was made.
		double waterPortion = 1000 * (PreparingDessert.getDessertMassPerServing() - dryMass);
		if (waterPortion > 0) {
			waterFinal = waterFinal - waterPortion;
		}

		if (waterFinal > 0) {
			double new_thirst = (currentThirst - waterFinal) / 8 - waterFinal * 5;
			if (new_thirst < 0)
				new_thirst = 0;
			else if (new_thirst > 500)
				new_thirst = 500;
			condition.setThirst(new_thirst);

			double newStress = condition.getStress() - waterFinal;
			condition.setStress(newStress);
		}
	}

	/**
	 * Calculates the amount of water to consume
	 * 
	 * @param is it water only
	 */
	public void consumeWater(boolean waterOnly) {
		thirst = condition.getThirst();
		notThirsty = thirst < PhysicalCondition.THIRST_THRESHOLD;
		
		if (!notThirsty) {
			double currentThirst = Math.min(thirst, 1_000);
			Unit containerUnit = person.getTopContainerUnit();
			if (containerUnit != null) {
				Inventory inv = containerUnit.getInventory();
				double waterFinal = Math.min(waterEachServing, currentThirst);

				if (waterFinal > 0) {
					int level = person.getAssociatedSettlement().getWaterRation();
					double new_thirst = (currentThirst - waterFinal) / 10;
					// Test to see if there's enough water
					boolean haswater = false;
					double amount = waterFinal / 1000D / level;

					if (amount > MIN)
						haswater = Storage.retrieveAnResource(amount, ResourceUtil.waterID, inv, false);
				
					if (haswater) {
						new_thirst = new_thirst - amount * 5_000;
						if (new_thirst < 0)
							new_thirst = 0;
						else if (new_thirst > THIRST_CEILING)
							new_thirst = THIRST_CEILING;
						condition.setThirst(new_thirst);

						if (amount > MIN) {
							Storage.retrieveAnResource(amount, ResourceUtil.waterID, inv, true);
							// Track the water consumption
							person.addConsumptionTime(1, amount);
							if (waterOnly)
								setDescription(Msg.getString("Task.description.eatMeal.water")); //$NON-NLS-1$
							LogConsolidated.log(Level.FINE, 1000, sourceName,
									"[" + person.getLocationTag().getLocale() + "] " + person
											+ " drank " + Math.round(amount * 1000.0) / 1.0
											+ " mL of water.");
						}
	//					LogConsolidated.log(Level.INFO, 1000, sourceName,
	//						 person + " is drinking " + Math.round(amount * 1000.0)/1000.0 + "kg of water"
	//						 + " thirst : " + Math.round(currentThirst* 100.0)/100.0
	//						 + " waterEachServing : " + Math.round(waterEachServing* 100.0)/100.0
	//						 + " waterFinal : " + Math.round(waterFinal* 100.0)/100.0
	//						 + " new_thirst : " + Math.round(new_thirst* 100.0)/100.0, null);
					}
	
					else if (!haswater) {
						// Test to see if there's just half of the amount of water
						amount = waterFinal / 1000D / level / 1.5;
						if (amount > MIN)
							haswater = Storage.retrieveAnResource(amount, ResourceUtil.waterID, inv, false);
						
						if (haswater) {
							new_thirst = new_thirst - amount * 5_000;
							if (new_thirst < 0)
								new_thirst = 0;
							else if (new_thirst > THIRST_CEILING)
								new_thirst = THIRST_CEILING;
							condition.setThirst(new_thirst);
							
							if (amount > MIN) {
								Storage.retrieveAnResource(amount, ResourceUtil.waterID, inv, true);
								// Track the water consumption
								person.addConsumptionTime(1, amount);
								if (waterOnly)
									setDescription(Msg.getString("Task.description.eatMeal.water")); //$NON-NLS-1$
								LogConsolidated.log(Level.WARNING, 1000, sourceName,
										"[" + person.getLocationTag().getLocale() + "] " + person
												+ " was put on water ration and allocated to drink no more than " 
												+ Math.round(amount * 1000.0) / 1.0
												+ " mL of water.");
							}
						}
						else {
							amount = waterFinal / 1000D / level / 3.0;
							if (amount > MIN)
								haswater = Storage.retrieveAnResource(amount, ResourceUtil.waterID, inv, false);
							
							if (haswater) {
								new_thirst = new_thirst - amount * 5_000;
								if (new_thirst < 0)
									new_thirst = 0;
								else if (new_thirst > THIRST_CEILING)
									new_thirst = THIRST_CEILING;
								condition.setThirst(new_thirst);

								if (amount > MIN) {
									Storage.retrieveAnResource(amount, ResourceUtil.waterID, inv, true);
									// Track the water consumption
									person.addConsumptionTime(1, amount);
									if (waterOnly)
										setDescription(Msg.getString("Task.description.eatMeal.water")); //$NON-NLS-1$
									LogConsolidated.log(Level.WARNING, 1000, sourceName,
											"[" + person.getLocationTag().getLocale() + "] " + person
													+ " was put on water ration and allocated to drink no more than " 
													+ Math.round(amount * 1000.0) / 1.0
													+ " mL of water.");
								}
							}
							
							else {
								amount = waterFinal / 1000D / level / 4.5;
								if (amount > MIN)
									haswater = Storage.retrieveAnResource(amount, ResourceUtil.waterID, inv, false);
								
								if (haswater) {
									new_thirst = new_thirst - amount * 5_000;
									if (new_thirst < 0)
										new_thirst = 0;
									else if (new_thirst > THIRST_CEILING)
										new_thirst = THIRST_CEILING;
									condition.setThirst(new_thirst);

									if (amount > MIN) {
										Storage.retrieveAnResource(amount, ResourceUtil.waterID, inv, true);
										// Track the water consumption
										person.addConsumptionTime(1, amount);
										if (waterOnly)
											setDescription(Msg.getString("Task.description.eatMeal.water")); //$NON-NLS-1$
										LogConsolidated.log(Level.WARNING, 1000, sourceName,
												"[" + person.getLocationTag().getLocale() + "] " + person
														+ " was put on water ration and allocated to drink no more than " 
														+ Math.round(amount * 1000.0) / 1.0
														+ " mL of water.");
									}
								}
							}
						}
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

			boolean isThirsty = false;
			if (condition.getThirst() > 50)
				isThirsty = true;
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
			cumulativeProportion += proportion;

			if (cumulativeProportion > dryMass) {
				double excess = cumulativeProportion - dryMass;
				cumulativeProportion = cumulativeProportion - excess;
				proportion = proportion - excess;
			}
			
			// dessert amount eaten over this period of time.
			double hungerRelieved = 1000 * proportion / dessertConsumptionRate;
			
			Unit containerUnit = person.getTopContainerUnit();
			
			if (containerUnit != null) {
				Inventory inv = containerUnit.getInventory();
				
				// Take dessert resource from inventory if it is available.
				boolean hasDessert = false;
				if (proportion > MIN) {
					hasDessert = Storage.retrieveAnResource(proportion, unpreparedDessertAR, inv, true);
				}
				if (hasDessert) {
					// Consume water inside the dessert
					consumeDessertWater(dryMass);
					// Consume unpreserved dessert.
					if (currentHunger > HUNGER_CEILING)
						currentHunger = HUNGER_CEILING;		
					condition.setHunger(currentHunger);
					currentHunger = condition.getHunger();
					// Note : Reduce person's hunger by proportion of dessert eaten.
					// Entire dessert will reduce person's hunger to 0.
					currentHunger -= (startingHunger * hungerRelieved);
					if (currentHunger < 0D) {
						currentHunger = 0D;
					}
					condition.setHunger(currentHunger);
					
					// Add caloric energy from dessert.
					double caloricEnergy = proportion / dessertConsumptionRate * PhysicalCondition.FOOD_COMPOSITION_ENERGY_RATIO;
					condition.addEnergy(caloricEnergy);
				} else {
					// Not enough dessert resource available to eat.
					result = false;
				}

			}
			// else {
			// Person is not inside a container unit, so end task.
			// result = false;
			// endTask();
			// }
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

		List<AmountResource> result = new ArrayList<AmountResource>();

		Unit containerUnit = person.getContainerUnit();
		if (!(containerUnit instanceof MarsSurface)) {
			Inventory inv = containerUnit.getInventory();

			boolean option = true;

			AmountResource[] ARs = PreparingDessert.getArrayOfDessertsAR();
			for (AmountResource ar : ARs) {
				if (isThirsty)
					option = ar.getName().contains("juice") || ar.getName().contains("milk");
				
				boolean hasAR = false;
				if (amountNeeded > MIN) {
					hasAR = Storage.retrieveAnResource(amountNeeded, ar, inv, false);
				}
				if (option && hasAR) {
					result.add(ar);
				}
			}
		}

		return result;
	}

	/**
	 * Adds experience to the person's skills used in this task.
	 * 
	 * @param time the amount of time (ms) the person performed this task.
	 */
	protected void addExperience(double time) {
		// This task adds no experience.
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
		Building result = null;

		if (person.isInSettlement()) {//LocationSituation.IN_SETTLEMENT == person.getLocationSituation()) {
			Settlement settlement = person.getSettlement();
			BuildingManager manager = settlement.getBuildingManager();
			List<Building> diningBuildings = manager.getBuildings(FunctionType.DINING);
			diningBuildings = BuildingManager.getWalkableBuildings(person, diningBuildings);
//			diningBuildings = BuildingManager.getNonMalfunctioningBuildings(diningBuildings);
			if (canChat)
				// Choose between the most crowded or the least crowded dining hall
				diningBuildings = BuildingManager.getChattyBuildings(diningBuildings);
			else
				diningBuildings = BuildingManager.getLeastCrowdedBuildings(diningBuildings);

			if (diningBuildings.size() > 0) {
				Map<Building, Double> diningBuildingProbs = BuildingManager.getBestRelationshipBuildings(person,
						diningBuildings);
				result = RandomUtil.getWeightedRandomObject(diningBuildingProbs);
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

		if (person.isInSettlement()) {
			Settlement settlement = person.getSettlement();
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
	 * Checks if there is preserved food available for the person.
	 * 
	 * @param person the person to check.
	 * @return true if preserved food is available.
	 */
	public static boolean isPreservedFoodAvailable(Person person) {
		boolean result = false;
		Unit containerUnit = person.getTopContainerUnit();
		if (!(containerUnit instanceof MarsSurface)) {
			Inventory inv = containerUnit.getInventory();

			if (foodConsumptionRate > MIN)
				result = Storage.retrieveAnResource(foodConsumptionRate, ResourceUtil.foodID, inv, false);
		}
		return result;
	}

	@Override
	public int getEffectiveSkillLevel() {
		return 0;
	}

	@Override
	public List<SkillType> getAssociatedSkills() {
		List<SkillType> results = new ArrayList<SkillType>(0);
		return results;
	}

	@Override
	public void endTask() {
		super.endTask();

		// Throw away napkin waste if one was used.
		if (hasNapkin) {
			Unit containerUnit = person.getContainerUnit();
			if (person.isInside()) {//!(containerUnit instanceof MarsSurface)) {
				Inventory inv = containerUnit.getInventory();
				if (NAPKIN_MASS > 0)
					Storage.storeAnResource(NAPKIN_MASS, ResourceUtil.solidWasteID, inv, sourceName + "::endTask");
			}
		}
	}

	@Override
	public void destroy() {
		super.destroy();

		kitchen = null;
		cookedMeal = null;
		dessertKitchen = null;
		nameOfDessert = null;
	}
}