/**
 * Mars Simulation Project
 * EatDrink.java
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
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
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
 * The EatDrink class is a task for eating a meal. The duration of the task is 40
 * millisols. Note: Eating a meal reduces hunger to 0.
 */
public class EatDrink extends Task implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static Logger logger = Logger.getLogger(EatDrink.class.getName());
	private static String loggerName = logger.getName();
	private static String sourceName = loggerName.substring(loggerName.lastIndexOf(".") + 1, loggerName.length());
	
	private static final int HUNGER_CEILING = 1000;
	private static final int THIRST_CEILING = 500;
	private static final int RATIO = 250;
	
	/** The minimal amount of resource to be retrieved. */
	private static final double MIN = 0.001;
	
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
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = -.75D;
	private static final double DESSERT_STRESS_MODIFIER = -.6D;
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
	private double totalEatingTime = 0D;
	private double eatingDuration = 0D;
	private double totalDessertEatingTime = 0D;
	private double dessertEatingDuration = 0D;
	private double startingHunger;
	private double currentHunger;
	private double thirst;

	private double energy;
	private double waterEachServing;
			
	private boolean hasNapkin = false;
//	private boolean notHungry;
//	private boolean notThirsty;

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
	public EatDrink(Person person) {
		super(NAME, person, false, false, STRESS_MODIFIER, true, 20D 
				+ RandomUtil.getRandomDouble(5D) - RandomUtil.getRandomDouble(5D));
		// 20 milisols ~ 30 mins
//		logger.info("EatMeal " + person + " containerID : " + person.getContainerID());		
		sourceName = sourceName.substring(sourceName.lastIndexOf(".") + 1, sourceName.length());

		condition = person.getPhysicalCondition();

		waterEachServing = condition.getWaterConsumedPerServing() * 1000D; // about 300 (3 * 1000)

		energy = condition.getEnergy();
		startingHunger = condition.getHunger();
		currentHunger = startingHunger;
		thirst = condition.getThirst();

		boolean notHungry = !condition.isHungry();
		boolean notThirsty = !condition.isThirsty();
		
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
				addPhase(EAT_PRESERVED_FOOD);
				addPhase(PICK_UP_DESSERT);
				addPhase(EAT_DESSERT);

				setPhase(EAT_PRESERVED_FOOD);
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
			
			Building diningBuilding = EatDrink.getAvailableDiningBuilding(person, want2Chat);
			if (diningBuilding != null) {
				// Walk to that building.
				walkToActivitySpotInBuilding(diningBuilding, FunctionType.DINING, true);
				
				// Take napkin from inventory if available.
				Inventory inv = person.getSettlement().getInventory();
				if (inv != null) {
					hasNapkin = Storage.retrieveAnResource(NAPKIN_MASS, ResourceUtil.napkinID, inv, false);
				}
			}

			// Initialize data members.
			double dur = getDuration();
			eatingDuration = dur * MEAL_EATING_PROPORTION;
			dessertEatingDuration = dur * DESSERT_EATING_PROPORTION;
	
			PersonConfig config = SimulationConfig.instance().getPersonConfig();
			foodConsumptionRate = config.getFoodConsumptionRate() / NUMBER_OF_MEAL_PER_SOL;
			dessertConsumptionRate = config.getDessertConsumptionRate() / NUMBER_OF_DESSERT_PER_SOL;
		
			// if a person is just a little thirsty and NOT that hungry
			if (notHungry) {
				
				if (notThirsty) {
//					LogConsolidated.log(Level.WARNING, 3000, sourceName,
//						person + " was trying to eat a meal, but is not inside a settlement/vehicle.");
					endTask();
				}
				else {
					addPhase(DRINK_WATER);
					setPhase(DRINK_WATER);
				}		
			} 
			
			else {
				// Initialize task phase.
				addPhase(LOOK_FOR_FOOD);
				addPhase(EAT_PRESERVED_FOOD);
				addPhase(PICK_UP_DESSERT);
				addPhase(EAT_MEAL);
				addPhase(EAT_DESSERT);

				setPhase(LOOK_FOR_FOOD);
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
//			return time;
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
	 * Perform the pick up the food or the meal phase.
	 * 
	 * @param time the amount of time (millisol) to perform the phase.
	 * @return the remaining time (millisol) after the phase has been performed.
	 */
	private double lookingforFoodPhase(double time) {

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
			walkToActivitySpotInBuilding(kitchen.getBuilding(), FunctionType.DINING, true);
			
			// Pick up a meal at kitchen if one is available.
			cookedMeal = kitchen.chooseAMeal(person);
			if (cookedMeal != null) {
				setDescription(Msg.getString("Task.description.eatDrink.cooked.pickingUp.detail", cookedMeal.getName())); //$NON-NLS-1$
				LogConsolidated.log(Level.INFO, 0, sourceName,
						"[" + person.getLocationTag().getLocale() + "] " + person
								+ " picked up a cooked meal '" + cookedMeal.getName() 
								+ "' to eat in " + person.getLocationTag().getImmediateLocation() + ".");
				setPhase(EAT_MEAL);
			}
			else {
//				logger.info(person + " couldn't find any cooked meals in this kitchen and will look for preserved food.");
				// If no kitchen found, look for preserved food.
				setPhase(EAT_PRESERVED_FOOD);
			}
		}

		return time *.9;
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
		// Note: must call endTask here to end this task
		super.endTask();
		return time *.9;
	}
	
	
	/**
	 * Performs eating preserved food phase
	 * 
	 * @param time
	 * @return
	 */
	private double eatingPreservedFoodPhase(double time) {
		double remainingTime = 0D;
		double eatingTime = time;
	
		int rand = RandomUtil.getRandomInt(3);
		
		if (rand == 3) {
			// try out dessert instead of eating preserved food
			setPhase(PICK_UP_DESSERT);
			remainingTime = time * .75;
		}
		else {
			
			boolean enoughFood = eatPreservedFood(eatingTime);
	
			// If not enough preserved food available, change to dessert phase.
			if (!enoughFood) {
				setPhase(PICK_UP_DESSERT);
				remainingTime = time * .5;
			}
			else {
				// Report eating preserved food.
				setDescription(Msg.getString("Task.description.eatDrink.preserved")); //$NON-NLS-1$
				
				if ((totalEatingTime + eatingTime) >= eatingDuration) {
					eatingTime = eatingDuration - totalEatingTime;
				}

				if (cumulativeProportion > foodConsumptionRate) {
					endTask();
				}
				
				if (eatingTime < time) {
					remainingTime = time - eatingTime;
				}

				totalEatingTime += eatingTime;

				if (totalEatingTime > getDuration())
					endTask();
						
				consumeWater(false);
			}
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
		double remainingTime = 0D;
		double eatingTime = time;
		
		if ((totalEatingTime + eatingTime) >= eatingDuration) {
			eatingTime = eatingDuration - totalEatingTime;
		}

		if (!person.isInVehicle()) {
	
			if (eatingTime > 0D) {
	
				if (cookedMeal != null) {
					// Set descriptoin for eating cooked meal.
					setDescription(Msg.getString("Task.description.eatDrink.cooked.eating.detail", cookedMeal.getName())); //$NON-NLS-1$
					// Eat cooked meal.
					eatCookedMeal(eatingTime);
					
					if (cumulativeProportion > cookedMeal.getDryMass()) {
						endTask();
					}
									
					// If finished eating, change to dessert phase.
					if (eatingTime < time) {
//						setPhase(PICK_UP_DESSERT);// EATING_DESSERT);
						remainingTime = time - eatingTime;
					}

					totalEatingTime += eatingTime;

					if (totalEatingTime > getDuration())
						endTask();
					
					consumeWater(false);
					
				} else {
					// Eat preserved food if available
					boolean enoughFood = eatPreservedFood(eatingTime);
	
					// If not enough preserved food available, change to dessert phase.
					if (!enoughFood) {
						setPhase(PICK_UP_DESSERT);
						remainingTime = time * .75;
						return remainingTime;
					}
					else {
						// Report eating preserved food.
						setDescription(Msg.getString("Task.description.eatDrink.preserved")); //$NON-NLS-1$

						remainingTime = time * .75;
						return remainingTime;
					}
				}
			}
		}
		
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
				
				// Pick up a dessert at kitchen if one is available.
				nameOfDessert = dessertKitchen.chooseADessert(person);

				if (nameOfDessert != null) {
					LogConsolidated.log(Level.FINE, 0, sourceName,
							"[" + person.getLocationTag().getLocale() + "] " + person
									+ " picked up prepared dessert '" + nameOfDessert.getName() 
									+ "' to eat/drink in " + person.getLocationTag().getImmediateLocation() + ".");
					
					setPhase(EAT_DESSERT);
					return time *.85;
				}
				
			} else {
				// If no dessert kitchen found, go eat preserved food
				setPhase(EAT_PRESERVED_FOOD);
				return time *.85;
			}
		}

		return time *.75;
	}

	/**
	 * Reduce the hunger level
	 * @param hungerRelieved
	 */
	public void reduceHunger(double hungerRelieved) {
		// Note: once a person has eaten a bit of food,
		// the hunger index should be reset to HUNGER_CEILING
		if (currentHunger > HUNGER_CEILING)
			currentHunger = HUNGER_CEILING;		

		// Note : Reduce person's hunger by proportion of food/dessert eaten.
		currentHunger = currentHunger - hungerRelieved;
		if (currentHunger < 0D) {
			currentHunger = 0D;
		}
		
//		logger.info(person 
//				+ " new Hunger " + Math.round(currentHunger*100.0)/100.0
//				+ "   hungerRelieved " + Math.round(hungerRelieved*100.0)/100.0);
		condition.setHunger(currentHunger);
	}
	
	/**
	 * Eat a cooked meal.
	 * 
	 * @param eatingTime the amount of time (millisols) to eat.
	 */
	private void eatCookedMeal(double eatingTime) {
		// Obtain the dry mass of the dessert
		double dryMass = cookedMeal.getDryMass();
		// Proportion of meal being eaten over this time period.
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
					
//			logger.info(person + " ate '" + cookedMeal.getName()
//					+ "   currentHunger " + Math.round(currentHunger*100.0)/100.0
//					+ "   hungerRelieved " + Math.round(hungerRelieved*100.0)/100.0
//					+ "   proportion " + Math.round(proportion*1000.0)/1000.0
//					+ "   EatingSpeed " + Math.round(person.getEatingSpeed()*1000.0)/1000.0
//					+ "   foodConsumptionRate " + Math.round(foodConsumptionRate*1000.0)/1000.0);
			
			// Change the hunger level after eating
			reduceHunger(hungerRelieved);
	
			// Reduce person's stress over time from eating a cooked meal.
			// This is in addition to normal stress reduction from eating task.
			double stressModifier = STRESS_MODIFIER * (cookedMeal.getQuality() + 1D);
			double newStress = condition.getStress() + (stressModifier * eatingTime);
			condition.setStress(newStress);
	
			// Add caloric energy from meal.
			double caloricEnergyFoodAmount = proportion / dryMass * PhysicalCondition.FOOD_COMPOSITION_ENERGY_RATIO;
			condition.addEnergy(caloricEnergyFoodAmount);
		}
	}

	/**
	 * Eat a meal of preserved food.
	 * 
	 * @param eatingTime the amount of time (millisols) to eat.
	 * @return true if enough preserved food available to eat.
	 */
	private boolean eatPreservedFood(double eatingTime) {
		boolean result = true;

		// Proportion of food being eaten over this time period.
		double proportion = person.getEatingSpeed() * eatingTime;

		if (cumulativeProportion > foodConsumptionRate) {
			double excess = cumulativeProportion - foodConsumptionRate;
			cumulativeProportion = cumulativeProportion - excess;
			proportion = proportion - excess;
		}
		
//		logger.info(person + "  proportion: " + proportion);
		if (proportion > MIN) {

			Unit container = person.getContainerUnit();
			if (person.isInside()) {
				Inventory inv = container.getInventory();
	
				// Take preserved food from inventory if it is available.
				boolean haveFood = Storage.retrieveAnResource(proportion, ResourceUtil.foodID, inv, true);
				
				if (haveFood) {
					// Add to cumulativeProportion
					cumulativeProportion += proportion;
					
					LogConsolidated.log(Level.INFO, 1000, sourceName,
							"[" + person.getLocationTag().getLocale() + "] " + person 
							+ " just ate " + Math.round(proportion*1000.0)/1000.0 + " kg of preserved food.");
					
					// Food amount eaten over this period of time.
					double hungerRelieved = RATIO * proportion / foodConsumptionRate;
//					logger.info(person + "::eatPreservedFood()"
//							+ "   currentHunger " + Math.round(currentHunger*100.0)/100.0
//							+ "   hungerRelieved " + Math.round(hungerRelieved*100.0)/100.0
//							+ "   proportion " + Math.round(proportion*1000.0)/1000.0
//							+ "   EatingSpeed " + Math.round(person.getEatingSpeed()*1000.0)/1000.0
//							+ "   foodConsumptionRate " + Math.round(foodConsumptionRate*1000.0)/1000.0);
					
					// Consume preserved food after eating
					reduceHunger(hungerRelieved);
	
					// Add caloric energy from the prserved food.
					double caloricEnergyFoodAmount = proportion / foodConsumptionRate * PhysicalCondition.FOOD_COMPOSITION_ENERGY_RATIO;
					condition.addEnergy(caloricEnergyFoodAmount);
	
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
	 * Eat a prepared dessert.
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
	//		double dessertAmount = dessertConsumptionRate * dessertProportion;
			Unit containerUnit = person.getTopContainerUnit();
			
			if (containerUnit != null) {
				Inventory inv = containerUnit.getInventory();
				
				// Take dessert resource from inventory if it is available.
				boolean hasDessert = false;
				if (inv != null) {
					// Add to cumulativeProportion
					cumulativeProportion += proportion;
					
					hasDessert = Storage.retrieveAnResource(proportion, nameOfDessert.getName(), inv, true);
				}
				
				if (hasDessert) {
					// Consume water
					consumeDessertWater(dryMass);
					
					// dessert amount eaten over this period of time.
					double hungerRelieved = RATIO * proportion / dryMass;
								
					// Consume unpreserved dessert.
//					logger.info(person + " ate " +  nameOfDessert.getName()
//							+ "   hungerRelieved : " + hungerRelieved);
					reduceHunger(hungerRelieved);
					
					// Reduce person's stress after eating a prepared dessert.
					// This is in addition to normal stress reduction from eating task.
					double stressModifier = DESSERT_STRESS_MODIFIER * (nameOfDessert.getQuality() + 1D);
					double newStress = condition.getStress() + (stressModifier * eatingTime);
					condition.setStress(newStress);
			
					// Add caloric energy from dessert.
					double caloricEnergy = proportion / dryMass * PhysicalCondition.FOOD_COMPOSITION_ENERGY_RATIO;
					condition.addEnergy(caloricEnergy);
					
				} else {
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
	public void consumeDessertWater(double dryMass) {
		double currentThirst = Math.min(thirst, THIRST_CEILING);
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
			else if (new_thirst > THIRST_CEILING)
				new_thirst = THIRST_CEILING;
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
//		logger.info(person + "::consumeWater()");
		thirst = condition.getThirst();
//		boolean notThirsty = !condition.isThirsty();
		
		if (thirst > PhysicalCondition.THIRST_THRESHOLD / 2.0) {
			double currentThirst = Math.min(thirst, THIRST_CEILING);
			Unit containerUnit = person.getContainerUnit();
			Inventory inv = null;
			
			if (containerUnit != null) {			
				if (containerUnit instanceof MarsSurface) {
					// Doing EVA outside. Get water from one's EVA suit
					inv = person.getSuit().getInventory();
				}
				else {
					// In a vehicle or settlement
					inv = containerUnit.getInventory();
				}
			}
			
			double waterFinal = Math.min(waterEachServing, currentThirst);

			if (inv != null && waterFinal > 0) {
				int level = person.getAssociatedSettlement().getWaterRation();
				double new_thirst = (currentThirst - waterFinal) / 10;
				// Test to see if there's enough water
				boolean haswater = false;
				double amount = waterFinal / 1000D / level;

				if (amount > MIN) {
					double available = inv.getAmountResourceStored(ResourceUtil.waterID, false);//Storage.retrieveAnResource(amount, ResourceUtil.waterID, inv, false);
					if (available >= amount)
						haswater = true;
				}				
					
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
							setDescription(Msg.getString("Task.description.eatDrink.water")); //$NON-NLS-1$
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
					if (amount > MIN) {
						double available = inv.getAmountResourceStored(ResourceUtil.waterID, false);//Storage.retrieveAnResource(amount, ResourceUtil.waterID, inv, false);
						if (available >= amount)
							haswater = true;
					}
					
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
								setDescription(Msg.getString("Task.description.eatDrink.water")); //$NON-NLS-1$
							LogConsolidated.log(Level.WARNING, 1000, sourceName,
									"[" + person.getLocationTag().getLocale() + "] " + person
											+ " was put on water ration and allocated to drink no more than " 
											+ Math.round(amount * 1000.0) / 1.0
											+ " mL of water.");
						}
					}
					else {
						amount = waterFinal / 1000D / level / 3.0;

						if (amount > MIN) {
							double available = inv.getAmountResourceStored(ResourceUtil.waterID, false);//Storage.retrieveAnResource(amount, ResourceUtil.waterID, inv, false);
							if (available >= amount)
								haswater = true;
						}
						
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
									setDescription(Msg.getString("Task.description.eatDrink.water")); //$NON-NLS-1$
								LogConsolidated.log(Level.WARNING, 1000, sourceName,
										"[" + person.getLocationTag().getLocale() + "] " + person
												+ " was put on water ration and allocated to drink no more than " 
												+ Math.round(amount * 1000.0) / 1.0
												+ " mL of water.");
							}
						}
						
						else {
							amount = waterFinal / 1000D / level / 4.5;
							if (amount > MIN) {
								double available = inv.getAmountResourceStored(ResourceUtil.waterID, false);//Storage.retrieveAnResource(amount, ResourceUtil.waterID, inv, false);
								if (available >= amount)
									haswater = true;
							}
							
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
										setDescription(Msg.getString("Task.description.eatDrink.water")); //$NON-NLS-1$
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

			if (cumulativeProportion > dryMass) {
				double excess = cumulativeProportion - dryMass;
				cumulativeProportion = cumulativeProportion - excess;
				proportion = proportion - excess;
			}
			
			if (proportion > MIN) {
		
				Unit containerUnit = person.getTopContainerUnit();
				
				Inventory inv = null;
				
				if (containerUnit != null) {			
					if (containerUnit instanceof MarsSurface) {
						// Get dessert from one's EVA suit
						inv = person.getSuit().getInventory();
					}
					else {
						inv = containerUnit.getInventory();
					}
				}
				
				if (inv != null) {
					// Add to cumulativeProportion
					cumulativeProportion += proportion;
					// Take dessert resource from inventory if it is available.
					boolean hasDessert = Storage.retrieveAnResource(proportion, unpreparedDessertAR, inv, true);
					
					if (hasDessert) {
						// Consume water inside the dessert
						consumeDessertWater(dryMass);
						
						// dessert amount eaten over this period of time.
						double hungerRelieved = RATIO * proportion / dessertConsumptionRate;
				
						// Consume unpreserved dessert.
						reduceHunger(hungerRelieved);
						
						// Reduce person's stress after eating an unprepared dessert.
						// This is in addition to normal stress reduction from eating task.
						double stressModifier = DESSERT_STRESS_MODIFIER;
						double newStress = condition.getStress() + (stressModifier * eatingTime);
						condition.setStress(newStress);
				
						// Add caloric energy from dessert.
						double caloricEnergy = proportion / dessertConsumptionRate * PhysicalCondition.FOOD_COMPOSITION_ENERGY_RATIO;
						condition.addEnergy(caloricEnergy);
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

		List<AmountResource> result = new ArrayList<AmountResource>();

		Unit containerUnit = person.getContainerUnit();
		if (!(containerUnit instanceof MarsSurface)) {
			Inventory inv = containerUnit.getInventory();

			boolean option = true;

			AmountResource[] ARs = PreparingDessert.getArrayOfDessertsAR();
			for (AmountResource ar : ARs) {
				if (isThirsty)
					option = ar.getName().contains(JUICE) || ar.getName().contains(MILK);
				
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
			if (inv != null && foodConsumptionRate > MIN)
				result = inv.getAmountResourceStored(ResourceUtil.foodID, false) > MIN;
//				Storage.retrieveAnResource(foodConsumptionRate, ResourceUtil.foodID, inv, false);
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
//		logger.info(person + " called endTask()");
		// Throw away napkin waste if one was used.
		if (hasNapkin) {
			Unit containerUnit = person.getContainerUnit();
			if (person.isInside()) {//!(containerUnit instanceof MarsSurface)) {
				Inventory inv = containerUnit.getInventory();
				if (NAPKIN_MASS > 0)
					Storage.storeAnResource(NAPKIN_MASS, ResourceUtil.solidWasteID, inv, sourceName + "::endTask");
			}
		}
		super.endTask();
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