/*
 * Mars Simulation Project
 * CookMeal.java
 * @date 2022-07-24
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
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
public class CookMeal extends Task implements Serializable {

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

	// Starting meal times (millisol) for 0 degrees longitude.
	private static final double BREAKFAST_START = 250D; // at 6am
	private static final double LUNCH_START = 500D; // at 12 am
	private static final double DINNER_START = 750D; // at 6 pm
	private static final double MIDNIGHT_SHIFT_MEAL_START = 005D; // avoid conflict with TabPanelCooking when at 0D all
																	// yesterday's cookedMeals are removed

	// Time (millisols) duration of meals.
	private static final double MEALTIME_DURATION = 75D; // 250 milliSol = 6 hours

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
		if (!isLocalMealTime(worker.getCoordinates(), 20)) {
			if (lastCookedMeal != null)
				logger.log(worker, Level.FINE, 0, "Ended cooking " + lastCookedMeal + ". The meal time was over.");
			else
				logger.log(worker, Level.FINE, 0, "Ended cooking. The meal time was over.");
			endTask();
			return time;
		}

		// If enough meals have been cooked for this meal, end task.
		if (kitchen.getCookNoMore()) {
			if (lastCookedMeal != null && !lastCookedMeal.isBlank())
				logger.log(worker, Level.INFO, 0, "Ended cooking " + lastCookedMeal + ". Enough servings cooked.");
//			else
//				logger.log(worker, Level.INFO, 0, "Ended cooking. Enough servings cooked.");
			endTask();
			return time;
		}

		if (worker instanceof Robot) {
			// A robot moves slower than a person and incurs penalty on workTime
			workTime = time / 3;
		}
		
		// Add this work to the kitchen.
		String nameOfMeal = kitchen.addWork(workTime, worker);

		if (nameOfMeal != null) {
			lastCookedMeal = nameOfMeal;
			setDescription(Msg.getString("Task.description.cookMeal.detail.finish", nameOfMeal)); // $NON-NLS-1$

			// Determine amount of effective work time based on "Cooking" skill.
			int cookingSkill = getEffectiveSkillLevel();
			if (cookingSkill == 0) {
				workTime /= 2;
			} else {
				workTime += workTime * (.2D * (double) cookingSkill);
			}

			// Add experience
			addExperience(time);

			// Check for accident in kitchen.
			checkForAccident(kitchenBuilding, time, 0.005);

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
		double timeDiff = 1000D * (location.getTheta() / (2D * Math.PI));
		return isMealTime(timeDiff, prepTime);
	}

	public static boolean isMealTime(Robot robot, int prepTime) {
		return isLocalMealTime(robot.getCoordinates(), prepTime);
	}

	/**
	 * Checks if it's the meal time.
	 * 
	 * @param timeDiff
	 * @return
	 */
	public static boolean isMealTime(double timeDiff, int prepTime) {

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
		if ((modifiedTime >= MIDNIGHT_SHIFT_MEAL_START)
				&& (modifiedTime <= (MIDNIGHT_SHIFT_MEAL_START + MEALTIME_DURATION))) {
			result = true;
		}
		return result;
	}

	/**
	 * Gets the name of the meal the person is cooking based on the time.
	 * 
	 * @return mean name ("Breakfast", "Lunch" or "Dinner) or empty string if none.
	 */
	private String getTypeOfMeal() {
		String result = "";
		double timeDiff = 1000D * (worker.getCoordinates().getTheta() / (2D * Math.PI));

		double timeOfDay = marsClock.getMillisol();

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
		if ((modifiedTime >= MIDNIGHT_SHIFT_MEAL_START)
				&& (modifiedTime <= (MIDNIGHT_SHIFT_MEAL_START + MEALTIME_DURATION))) {
			result = "Midnight Meal";
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
			List<Building> kitchenBuildings = manager.getBuildings(FunctionType.COOKING);
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
			List<Building> kitchenBuildings = manager.getBuildings(FunctionType.COOKING);
			kitchenBuildings = BuildingManager.getNonMalfunctioningBuildings(kitchenBuildings);
			kitchenBuildings = BuildingManager.getLeastCrowded4BotBuildings(getKitchensNeedingCooks(kitchenBuildings));

			if (kitchenBuildings.size() > 0) {
				int selected = RandomUtil.getRandomInt(kitchenBuildings.size() - 1);
				result = kitchenBuildings.get(selected);
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
	private static List<Building> getKitchensNeedingCooks(List<Building> kitchenBuildings) {
		List<Building> result = new ArrayList<>();

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
