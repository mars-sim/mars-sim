/**
 * Mars Simulation Project
 * EatMealMeta.java
 * @version 3.08 2015-06-18
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
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
    private static final String NAME = Msg.getString(
            "Task.description.eatMealMeta"); //$NON-NLS-1$

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
        double result = 0D;

        double hunger = person.getPhysicalCondition().getHunger();
        double energy = person.getPhysicalCondition().getEnergy();

        // Only eat a meal if person is sufficiently hungry or low on caloric energy.
        if ((hunger > 250D) || (energy < 2525D)) {
            double hungerFactor = hunger;
            double energyFactor = (2525D - energy) / 100D;
            double avgFactor = (hungerFactor + energyFactor) / 2D;
            if (avgFactor < 0D) {
                avgFactor = 0D;
            }

            result = avgFactor;
        }

        if (result > 0D) {

            if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {

                // Check if a cooked meal is available in a kitchen building at the settlement.
                Cooking kitchen = EatMeal.getKitchenWithMeal(person);
                if (kitchen != null) {
                    // Increase probability to eat meal if a cooked meal is available.
                    result *= 2D;
                }
                else {
                    // If no cooked meal, check if preserved food is available to eat.
                    if (!EatMeal.isPreservedFoodAvailable(person)) {
                        // If no preserved food, person can't eat a meal.
                        result = 0D;
                    }
                }

                // Check if there is a local dining building.
                Building diningBuilding = EatMeal.getAvailableDiningBuilding(person, false);
                if (diningBuilding != null) {
                    // Modify probability by social factors in dining building.
                    result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, diningBuilding);
                    result *= TaskProbabilityUtil.getRelationshipModifier(person, diningBuilding);
                }

                // 2015-06-07 Added Preference modifier
                if (result > 0)
                	result += person.getPreference().getPreferenceScore(this);
                if (result < 0) result = 0;

            }

            else if (!EatMeal.isPreservedFoodAvailable(person)) {

                // If no preserved food available, cannot eat a meal.
                result = 0D;
            }
	    }


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