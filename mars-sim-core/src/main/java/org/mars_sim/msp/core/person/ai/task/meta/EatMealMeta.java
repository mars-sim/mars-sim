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
import org.mars_sim.msp.core.person.ai.task.Task;
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
	private static final String NAME = Msg.getString("Task.description.eatMealMeta"); //$NON-NLS-1$

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

		// CircadianClock cc = person.getCircadianClock();
		// double ghrelin = cc.getSurplusGhrelin();
		// double leptin = cc.getSurplusLeptin();
		// Each meal (.155 kg = .62/4) has an average of 2525 kJ. Thus ~10,000 kJ
		// persson per sol

		// When thirst is greater than 100, a person may start feeling thirsty
		if (thirst > PhysicalCondition.THIRST_THRESHOLD) {
			result = thirst;
			pc.setThirsty(true);
		}
		// Only eat a meal if person is sufficiently hungry or low on caloric energy.
		else if (hunger > 250 || energy < 2525) {// || ghrelin-leptin > 300) {
			result = thirst * 3.0;
			result += hunger / 8D;
			result += (2525 - energy) / 50D; // + (ghrelin-leptin - 300);
			if (result <= 0)
				return 0;
		}

		else
			return 0;

		if (person.isInSettlement()) {

			// Check if a cooked meal is available in a kitchen building at the settlement.
			Cooking kitchen = EatMeal.getKitchenWithMeal(person);
			if (kitchen != null) {
				// Increase probability to eat meal if a cooked meal is available.
				result *= 1.5 * kitchen.getNumberOfAvailableCookedMeals();
			} else { // no kitchen has available meals
						// If no cooked meal, check if preserved food is available to eat.
				if (!EatMeal.isPreservedFoodAvailable(person)) {
					// If no preserved food, person can't eat a meal.
					return result / 5D;
				}
			}

			// Check if there is a local dining building.
			Building diningBuilding = EatMeal.getAvailableDiningBuilding(person, false);
			if (diningBuilding != null) {
				// Modify probability by social factors in dining building.
				result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, diningBuilding);
				result *= TaskProbabilityUtil.getRelationshipModifier(person, diningBuilding);
			}

			if (CookMeal.isMealTime(person.getCoordinates())) {
				result *= 4D;
			} else
				result *= 1D;

		}

		else if (person.isInVehicle()) {
			// if (!EatMeal.isPreservedFoodAvailable(person)) {
			// If no preserved food, person can't eat a meal.
			// return 0;
			// }
			// higher probability than inside a settlement since a person is more likely to
			// become thirsty due to on-call shift.
			if (CookMeal.isMealTime(person.getCoordinates())) {
				result *= 5D;
			} else
				result *= 2.5;

			// TODO : how to ration food and water if running out of it ?
		} 
		
		else if (person.isOutside()) {

			if (!pc.isThirsty()) {
				return 0;
			}
			else if (CookMeal.isMealTime(person.getCoordinates())) {
				result *= 3D;
			} 
			else
				result *= 1.5;
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