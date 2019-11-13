/**
 * Mars Simulation Project
 * EatMealMeta.java
 * @version 3.1.0 2017-03-08
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.task.CookMeal;
import org.mars_sim.msp.core.person.ai.task.EatMeal;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.cooking.Cooking;

/**
 * Meta task for the EatMeal task.
 */
public class EatMealMeta implements MetaTask, Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.eatMeal"); //$NON-NLS-1$

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public Task constructInstance(Person person) {
		return new EatMeal(person);
	}

	@Override
	public double getProbability(Person person) {
		double result = 0;

		PhysicalCondition pc = person.getPhysicalCondition();

		double thirst = pc.getThirst();
		double hunger = pc.getHunger();
		double energy = pc.getEnergy();

		boolean notHungry = !pc.isHungry();
		boolean notThirsty = !pc.isThirsty();
		
		// CircadianClock cc = person.getCircadianClock();
		// double ghrelin = cc.getSurplusGhrelin();
		// double leptin = cc.getSurplusLeptin();
		// Each meal (.155 kg = .62/4) has an average of 2525 kJ. Thus ~10,000 kJ
		// persson per sol

		// When thirst is greater than 100, a person may start feeling thirsty
		if (!notThirsty) {
			result = Math.pow((thirst - PhysicalCondition.THIRST_THRESHOLD), 3);
		}
		
		// Only eat a meal if person is sufficiently hungry or low on caloric energy.
		if (!notHungry) {// || ghrelin-leptin > 300) {
			result += hunger / 4D;
			if (energy < 2525)
				result += (2525 - energy) / 50D; // (ghrelin-leptin - 300);
		}

		else if (notHungry && notThirsty)
			// if not thirsty and not hungry
			result = 0;

		if (person.isInSettlement()) {

			if (!CookMeal.isLocalMealTime(person.getCoordinates(), 0)) {
				// If it's not meal time yet, reduce the probability
				result /= 4D;
			}
			
			// Check if a cooked meal is available in a kitchen building at the settlement.
			Cooking kitchen = EatMeal.getKitchenWithMeal(person);
			if (kitchen != null) {
				// Increase probability to eat meal if a cooked meal is available.
				result *= 1.5 * kitchen.getNumberOfAvailableCookedMeals();
			} 
			
			else { // no kitchen has available meals
					// If no cooked meal, check if preserved food is available to eat.
				if (!EatMeal.isPreservedFoodAvailable(person)) {
					// If no preserved food, person can still drink
					result /= 2;
				}
			}

			// Check if there is a local dining building.
			Building diningBuilding = EatMeal.getAvailableDiningBuilding(person, false);
			if (diningBuilding != null) {
				// Modify probability by social factors in dining building.
				result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, diningBuilding);
				result *= TaskProbabilityUtil.getRelationshipModifier(person, diningBuilding);
			}

		}

		else if (person.isInVehicle()) {
			// Note: consider how is the probability compared to that of inside a settlement 
			// may be a person is more likely to become thirsty and/or hungry due to on-call shift ?
			
			if (!CookMeal.isLocalMealTime(person.getCoordinates(), 0))
				result /= 4;

			if (!EatMeal.isPreservedFoodAvailable(person)) {
				// If no preserved food, person can still drink
				result /= 2;
			}
			// TODO : how to ration food and water if running out of it ?
		} 
		
		else if (person.isOutside()) {

			if (notHungry && notThirsty) {
				// person cannot consume food while being outside doing EVA
				return 0;
			}
			else if (CookMeal.isLocalMealTime(person.getCoordinates(), 10)) {
				result = hunger; 
			} else
				result /= 4;
		}

		// Add Preference modifier
		if (result > 0D) {
			result = result + result * person.getPreference().getPreferenceScore(this) / 5D;
		}

		if (result < 0)
			return 0;

		// if (result > 0) System.out.println(person + "'s EatMealMeta : " +
		// Math.round(result*10D)/10D);
		return result;
	}

	@Override
	public Task constructInstance(Robot robot) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getProbability(Robot robot) {
		// TODO Auto-generated method stub
		return 0;
	}
}