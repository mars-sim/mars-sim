/*
 * Mars Simulation Project
 * CookMeal.java
 * @date 2022-07-24
 * @author Scott Davis
 */
package com.mars_sim.core.building.function.cooking.task;

import java.util.logging.Level;

import com.mars_sim.core.UnitType;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.building.function.cooking.Cooking;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;

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

	private static final String FINISH_COOKING = Msg.getString("Task.description.cookMeal.detail.finish"); //$NON-NLS-1$
	
	/** Task phases. */
	private static final TaskPhase COOKING = new TaskPhase(Msg.getString("Task.phase.cooking")); //$NON-NLS-1$

	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = -.1D;
	/** The meal preparation time. */
	public static final int PREP_TIME = 15;
	
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
	 * @param chef Chef cooking the meal
	 */
	public CookMeal(Worker chef, Cooking kitchen) {
		// Use Task constructor
		super(NAME, chef, true, false, STRESS_MODIFIER, SkillType.COOKING, 25D);
		
		if (chef.isOutside()) {
			endTask();
		}
		
		// Initialize data members
		setDescription(NAME + " " + getTypeOfMeal());

		// Get an available kitchen.
		this.kitchen = kitchen;
		kitchenBuilding = kitchen.getBuilding();


		// Walk to kitchen building.
		walkToTaskSpecificActivitySpotInBuilding(kitchenBuilding, FunctionType.COOKING, false);

		if (!Cooking.hasMealIngredients(kitchenBuilding.getSettlement())) {
			logger.log(person, Level.WARNING, 10_000, NO_INGREDIENT);

			endTask();
		}
		else {
			// Add task phase
			addPhase(COOKING);
			setPhase(COOKING);
		}
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
		if (!isMealTime(worker.getAssociatedSettlement(), PREP_TIME)) {
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

		if (worker.getUnitType() == UnitType.ROBOT) {
			// A robot moves slower than a person and incurs penalty on workTime
			workTime = time / 3;
		}
		
		// Determine amount of effective work time based on "Cooking" skill.
		int cookingSkill = getEffectiveSkillLevel();
		if (cookingSkill == 0) {
			workTime /= 2;
		} else {
			workTime += workTime * (.2D * cookingSkill);
		}

		// Add this work to the kitchen.
		String nameOfMeal = kitchen.addWork(workTime, worker);
		
		// Add experience
		addExperience(time);

		// Check for accident in kitchen.
		checkForAccident(kitchenBuilding, time, 0.003);

		if (nameOfMeal != null) {
			lastCookedMeal = nameOfMeal;
			String s = FINISH_COOKING + " " + nameOfMeal;
			logger.log(worker, Level.INFO, 4_000, s); 
			setDescription(s);
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

	/**
	 * Checks if it's the meal time.
	 * 
	 * @param timeDiff
	 * @return
	 */
	public static boolean isMealTime(Settlement base, int prepTime) {
		
		int modifiedTime = getMarsTime().getMillisolInt() + prepTime;
		if (modifiedTime >= 1000) {
			modifiedTime -= 1000;
		}

		return base.getMealTimes().isMealTime(modifiedTime);
	}
	
	/**
	 * Gets the name of the meal the person is cooking based on the time.
	 * 
	 * @return mean name ("Breakfast", "Lunch", "Dinner", or "Midnight") or empty string if none.
	 */
	private String getTypeOfMeal() {
		var meal = worker.getAssociatedSettlement().getMealTimes().getActiveMeal(getMarsTime().getMillisolInt());

		String result = "";
		if (meal != null) {
			result = meal.name();
		}
		return result;
	}
	
	@Override
	public void destroy() {
		kitchen = null;
		kitchenBuilding = null;
		super.destroy();
	}
}
