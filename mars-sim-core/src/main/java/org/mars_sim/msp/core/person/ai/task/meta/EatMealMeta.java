/**
 * Mars Simulation Project
 * EatMealMeta.java
 * @version 3.07 2015-01-05
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.EatMeal;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.structure.building.Building;

public class EatMealMeta implements MetaTask {
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.eatMeal"); //$NON-NLS-1$
    
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
        if (hunger > 250D) {
            result = hunger;
        }

        if (person.getLocationSituation() == LocationSituation.OUTSIDE) {
            result = 0D;
        }

        Building building = EatMeal.getAvailableDiningBuilding(person);
        if (building != null) {
            result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, building);
            result *= TaskProbabilityUtil.getRelationshipModifier(person, building);
        }

        // Check if there's a cooked meal at a local kitchen.
        if (EatMeal.getKitchenWithFood(person) != null) {
            result += 300D;
        }
        else {
            // Check if there is food available to eat.
            if (!EatMeal.isFoodAvailable(person)) {
                result = 0D;
            }
        }

        return result;
    }
}