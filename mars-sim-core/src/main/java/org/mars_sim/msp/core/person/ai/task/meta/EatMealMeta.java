/**
 * Mars Simulation Project
 * EatMealMeta.java
 * @version 3.1.0 2017-03-08
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.CircadianClock;
import org.mars_sim.msp.core.person.LocationSituation;
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
        double result = 0;
        
        LocationSituation ls = person.getLocationSituation();
        if (ls == LocationSituation.OUTSIDE)
        	return 0;

        PhysicalCondition pc = person.getPhysicalCondition();
        
        double thirst = pc.getThirst();
        double hunger = pc.getHunger();
        double energy = pc.getEnergy();
        
    	CircadianClock cc = person.getCircadianClock();
    	double ghrelin = cc.getSurplusGhrelin();
    	double leptin = cc.getSurplusLeptin();
        // Each meal (.155 kg = .62/4) has an average of 2525 kJ. Thus ~10,000 kJ persson per sol
        
    	if (thirst > 250) {
    		 result = thirst/5;
    	}
        // Only eat a meal if person is sufficiently hungry or low on caloric energy.
    	else if (hunger > 250 || energy < 2525 || ghrelin-leptin > 300) {
        	thirst = thirst / 10;
        	hunger = hunger / 10;
            energy = (2525 - energy) / 100;
            result = thirst + hunger + energy;// +  (ghrelin-leptin - 300);
            if (result <= 0)
            	return 0;
        }
        
        else
        	return 0;

        if (ls == LocationSituation.IN_SETTLEMENT) {

            // Check if a cooked meal is available in a kitchen building at the settlement.
            Cooking kitchen = EatMeal.getKitchenWithMeal(person);
            if (kitchen != null) {
                // Increase probability to eat meal if a cooked meal is available.
            	int num = kitchen.getNumberOfAvailableCookedMeals();
                result *= 1.5 * num;
            }
            else { //no kitchen has available meals
                // If no cooked meal, check if preserved food is available to eat.
                if (!EatMeal.isPreservedFoodAvailable(person)) {
                    // If no preserved food, person can't eat a meal.
                    return 0;
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
        	}
        	else
        		result *= .25D;    
        	
        }
        
        else if (ls == LocationSituation.IN_VEHICLE) {
        	result *= 1D; // ration food a little bit just in case of running out of it
        }
        
    	
        // 2015-06-07 Added Preference modifier
        if (result > 0D) {
            result = result + result * person.getPreference().getPreferenceScore(this)/5D;
        }

        if (result < 0) return 0;

        //if (result > 0) System.out.println(person + "'s EatMealMeta : " + Math.round(result*10D)/10D);
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