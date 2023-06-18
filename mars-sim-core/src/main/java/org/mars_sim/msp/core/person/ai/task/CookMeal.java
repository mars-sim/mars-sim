/*
 * Mars Simulation Project
 * CookMeal.java
 * @date 2022-07-24
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.data.UnitSet;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskPhase;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.cooking.Cooking;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The CookMeal class is a task for cooking meals in a building with the Cooking
 * function. This is an effort driven task.
 */
public class CookMeal extends Task {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(CookMeal.class.getName());


	/** Task name */
	private static final String NAME = Msg.getString("Task.description.cookMeal"); //$NON-NLS-1$

	
	/** Task phases. */
	private static final TaskPhase COOKING = new TaskPhase(Msg.getString("Task.phase.cooking")); //$NON-NLS-1$

	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = -.1D;
	/** The meal preparation time. */
	public static final int PREP_TIME = 15;
	// Starting meal times (millisol) for 0 degrees longitude.
	private static final int BREAKFAST_START = 250; // at 6am
	private static final int LUNCH_START = 500; // at 12 am
	private static final int DINNER_START = 750; // at 6 pm
	// Avoid conflict with TabPanelCooking 
	// when at 0D all yesterday's cookedMeals are removed
	private static final int MIDNIGHT_SNACK_START = 5; 

	// Time (millisols) duration of meals.
	// Note: 80 millisols ~= 2 hours
	private static final int MEALTIME_DURATION = 80; 
	// Note: 40 millisols ~= 1 hour
	private static final int SNACK_TIME_DURATION = 40;
	
	private static final String NO_INGREDIENT = "Cannot cook any meals. None of the ingredients are available.";

	// Data members
	/** The last cooked meal. */
	private String lastCookedMeal;
	/** The kitchen the person is cooking at. */
	private Cooking kitchen;
	private Building kitchenBuilding;

	/**
	 * Constructor.
	 * 
	 * @param person the person performing the task.
	 * @throws Exception if error constructing task.
	 */
	public CookMeal(Person person) {
		// Use Task constructor
		super(NAME, person, true, false, STRESS_MODIFIER, SkillType.COOKING, 25D);
		
		if (person.isOutside()) {
//			walkBackInside();
			endTask();
		}
		
		// Initialize data members
		setDescription(Msg.getString("Task.description.cookMeal.detail", getTypeOfMeal())); // $NON-NLS-1$

		// Get an available kitchen.
		kitchenBuilding = getAvailableKitchen(person);

		if (kitchenBuilding != null) {
			kitchen = kitchenBuilding.getCooking();

			// Walk to kitchen building.
			walkToTaskSpecificActivitySpotInBuilding(kitchenBuilding, FunctionType.COOKING, false);

			// Need to reset numGoodRecipes periodically since it's a cache value
			// and won't get updated unless a meal is cooked.
			// Note: it's reset at least once a day at the end of a sol
			if (!kitchen.canCookMeal()) {
				logger.log(person, Level.WARNING, 10_000, NO_INGREDIENT);

				endTask();
			} else {

				// Add task phase
				addPhase(COOKING);
				setPhase(COOKING);

			}
		} else {
			endTask();
		}
	}

	public CookMeal(Robot robot) {
		// Use Task constructor
		super(NAME, robot, true, false, STRESS_MODIFIER, SkillType.COOKING, 25D);

		// Initialize data members
		setDescription(Msg.getString("Task.description.cookMeal.detail", getTypeOfMeal())); // $NON-NLS-1$

		// Get available kitchen if any.
		kitchenBuilding = getAvailableKitchen(robot);

		if (kitchenBuilding != null) {
			kitchen = kitchenBuilding.getCooking();

			// Walk to kitchen building.
			walkToTaskSpecificActivitySpotInBuilding(kitchenBuilding, FunctionType.COOKING, false);

			if (!kitchen.canCookMeal()) {
				logger.log(robot, Level.WARNING, 5000, NO_INGREDIENT);

				endTask();

			} else {

				// Add task phase
				addPhase(COOKING);
				setPhase(COOKING);
			}
		} else
			endTask();

	}

	/**
	 * Performs the method mapped to the task's current phase.
	 * 
	 * @param time the amount of time the phase is to be performed.
	 * @return the remaining time after the phase has been performed.
	 */
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("The Cooking task phase is null");
		} else if (COOKING.equals(getPhase())) {
			return cookingPhase(time);
		} else {
			return time;
		}
	}

	/**
	 * Performs the cooking phase of the task.
	 * 
	 * @param time the amount of time (millisol) to perform the cooking phase.
	 * @return the amount of time (millisol) left after performing the cooking
	 *         phase.
	 */
	private double cookingPhase(double time) {
		// If kitchen has malfunction, end task.
		if (kitchen.getBuilding().getMalfunctionManager().hasMalfunction()) {
			endTask();
			return time;
		}

		double workTime = time;

		// If meal time is over, end task.
		if (!isLocalMealTime(worker.getCoordinates(), PREP_TIME)) {
			if (lastCookedMeal != null)
				logger.log(worker, Level.FINE, 0, "Ended cooking " + lastCookedMeal + ". The meal time was over.");
			else
				logger.log(worker, Level.FINE, 0, "Ended cooking. The meal time was over.");
			endTask();
			return time;
		}

		// If enough meals have been cooked for this meal, end task.
		if (kitchen.getCookNoMore()) {
			endTask();
			return time;
		}

		if (worker instanceof Robot) {
			// A robot moves slower than a person and incurs penalty on workTime
			workTime = time / 3;
		}
		
		// Determine amount of effective work time based on "Cooking" skill.
		int cookingSkill = getEffectiveSkillLevel();
		if (cookingSkill == 0) {
			workTime /= 2;
		} else {
			workTime += workTime * (.2D * (double) cookingSkill);
		}

		// Add this work to the kitchen.
		String nameOfMeal = kitchen.addWork(workTime, worker);
		
		// Add experience
		addExperience(time);

		// Check for accident in kitchen.
		checkForAccident(kitchenBuilding, time, 0.005);

		if (nameOfMeal != null) {
			lastCookedMeal = nameOfMeal;
			logger.log(worker, Level.INFO, 4_000, Msg.getString("Task.description.cookMeal.detail.finish", nameOfMeal)); // $NON-NLS-1$
			setDescription(Msg.getString("Task.description.cookMeal.detail.finish", nameOfMeal)); // $NON-NLS-1$
			endTask();
		}

		return 0D;
	}

	/**
	 * Gets the kitchen the person is cooking in.
	 * 
	 * @return kitchen
	 */
	public Cooking getKitchen() {
		return kitchen;
	}


	private static int getTimeDifference(Coordinates location) {
		double diff = Math.round(1000.0 * location.getTheta() / 2D / Math.PI);
		// Round it to the closest ceiling multiple of 10
		return (int)(10 * Math.ceil(diff / 10));
	}
	
	/**
	 * Checks if it is currently a meal time at the location. Need to estimate the prepTime 
	 * e.g. the cook needs to be in the chow hall to begin cooking 20 millisols prior to 
	 * 'starting' the meal time.
	 * 
	 * @param location the coordinate location to check for.
	 * @param prepTime the number of millisols prior to meal time that needs to be accounted for.
	 * @return true if meal time
	 */
	public static boolean isLocalMealTime(Coordinates location, int prepTime) {
		int timeDiff = getTimeDifference(location);
		return isMealTime(timeDiff, prepTime);
	}

	public static boolean isMealTime(Robot robot, int prepTime) {
		return isLocalMealTime(robot.getCoordinates(), prepTime);
	}


	public static boolean isMealTime(Person person, int prepTime) {
		return isLocalMealTime(person.getCoordinates(), prepTime);
	}
	
	/**
	 * Returns the start and end of the meal time of interest.
	 * 
	 * @param location
	 * @param code [0: breakfast, 1: lunch, 2: dinner, 3: midnight]
	 * @return
	 */
	public static String getMealTimeString(Coordinates location, int code) {
		int[] time = getMealTime(location, code); 
		return time[0] + " - " + time[1];
	}
	
	/**
	 * Returns the start and end of the meal time of interest.
	 * 
	 * @param location
	 * @param code [0: breakfast, 1: lunch, 2: dinner, 3: midnight]
	 * @return
	 */
	public static int[] getMealTime(Coordinates location, int code) {
		int start = 0;
		int end = 0;
		int timeDiff = getTimeDifference(location);

		if (code == 0) {
			start = BREAKFAST_START + timeDiff;
			end = start + SNACK_TIME_DURATION;
		}
		
		else if (code == 1) {
			start = LUNCH_START + timeDiff;
			end =start + MEALTIME_DURATION;
		}
		
		else if (code == 2) {
			start = DINNER_START + timeDiff;
			end = start + MEALTIME_DURATION;
		}
		
		else if (code == 3) {
			start = MIDNIGHT_SNACK_START + timeDiff;
			end = start + SNACK_TIME_DURATION;
		}

		if (start >= 1000D) {
			start -= 1000D;
		}
		if (end >= 1000D) {
			end -= 1000D;
		}
		return new int[]{start, end};
	}
	
	/**
	 * Checks if it's the meal time.
	 * 
	 * @param timeDiff
	 * @return
	 */
	public static boolean isMealTime(int timeDiff, int prepTime) {

		boolean result = false;
		double timeOfDay = marsClock.getMillisol();
		
		double modifiedTime = timeOfDay + timeDiff + prepTime;
		if (modifiedTime >= 1000D) {
			modifiedTime -= 1000D;
		}

		if ((modifiedTime >= BREAKFAST_START) && (modifiedTime <= (BREAKFAST_START + MEALTIME_DURATION))) {
			result = true;
		}
		if ((modifiedTime >= LUNCH_START) && (modifiedTime <= (LUNCH_START + MEALTIME_DURATION))) {
			result = true;
		}
		if ((modifiedTime >= DINNER_START) && (modifiedTime <= (DINNER_START + MEALTIME_DURATION))) {
			result = true;
		}
		if ((modifiedTime >= MIDNIGHT_SNACK_START)
				&& (modifiedTime <= (MIDNIGHT_SNACK_START + MEALTIME_DURATION))) {
			result = true;
		}
		return result;
	}

	
	/**
	 * Gets the name of the meal the person is cooking based on the time.
	 * 
	 * @return mean name ("Breakfast", "Lunch", "Dinner", or "Midnight") or empty string if none.
	 */
	private String getTypeOfMeal() {
		String result = "";

		double timeOfDay = marsClock.getMillisol();
		int timeDiff = getTimeDifference(worker.getCoordinates());
		double modifiedTime = timeOfDay + timeDiff;
		if (modifiedTime >= 1000D) {
			modifiedTime -= 1000D;
		}

		if ((modifiedTime >= BREAKFAST_START) && (modifiedTime <= (BREAKFAST_START + MEALTIME_DURATION))) {
			result = "Breakfast";
		}
		if ((modifiedTime >= LUNCH_START) && (modifiedTime <= (LUNCH_START + MEALTIME_DURATION))) {
			result = "Lunch";
		}
		if ((modifiedTime >= DINNER_START) && (modifiedTime <= (DINNER_START + MEALTIME_DURATION))) {
			result = "Dinner";
		}
		if ((modifiedTime >= MIDNIGHT_SNACK_START)
				&& (modifiedTime <= (MIDNIGHT_SNACK_START + MEALTIME_DURATION))) {
			result = "Midnight";
		}

		return result;
	}

	/**
	 * Gets an available kitchen building at the person's settlement.
	 * 
	 * @param person the person to check for.
	 * @return kitchen building or null if none available.
	 */
	public static Building getAvailableKitchen(Person person) {
		Building result = null;

		if (person.isInSettlement()) {
			BuildingManager manager = person.getSettlement().getBuildingManager();
			Set<Building> kitchenBuildings = manager.getBuildingSet(FunctionType.COOKING);
			kitchenBuildings = BuildingManager.getNonMalfunctioningBuildings(kitchenBuildings);
			kitchenBuildings = BuildingManager.getLeastCrowdedBuildings(getKitchensNeedingCooks(kitchenBuildings));

			if (kitchenBuildings.size() > 0) {
				result = RandomUtil.getWeightedRandomObject(BuildingManager.getBestRelationshipBuildings(person,
						kitchenBuildings));
			}
		}

		return result;
	}

	/**
	 * Gets available kitchen building.
	 * 
	 * @param robot
	 * @return
	 */
	public static Building getAvailableKitchen(Robot robot) {
		Building result = null;

		if (robot.isInSettlement()) {
			BuildingManager manager = robot.getSettlement().getBuildingManager();
			Set<Building> kitchenBuildings = manager.getBuildingSet(FunctionType.COOKING);
			kitchenBuildings = BuildingManager.getNonMalfunctioningBuildings(kitchenBuildings);
			kitchenBuildings = BuildingManager.getLeastCrowded4BotBuildings(getKitchensNeedingCooks(kitchenBuildings));

			if (kitchenBuildings.size() > 0) {
				return RandomUtil.getARandSet(kitchenBuildings);
			}
		}

		return result;
	}

	/**
	 * Gets a list of kitchen buildings that have room for more cooks.
	 * 
	 * @param kitchenBuildings list of kitchen buildings
	 * @return list of kitchen buildings
	 * @throws BuildingException if error
	 */
	private static Set<Building> getKitchensNeedingCooks(Set<Building> kitchenBuildings) {
		Set<Building> result = new UnitSet<>();

		if (kitchenBuildings != null) {
			Iterator<Building> i = kitchenBuildings.iterator();
			while (i.hasNext()) {
				Building building = i.next();
				if (!building.getCooking().isFull()) {
					result.add(building);
				}
			}
		}

		return result;
	}
	
	public void destroy() {
		kitchen = null;
		kitchenBuilding = null;
	}
}
