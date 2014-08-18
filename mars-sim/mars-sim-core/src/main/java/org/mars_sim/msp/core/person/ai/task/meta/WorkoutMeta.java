/**
 * Mars Simulation Project
 * WorkoutMeta.java
 * @version 3.07 2014-08-05
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.person.ai.task.Workout;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * Meta task for the Workout task.
 */
public class WorkoutMeta implements MetaTask {

    // TODO: Use enum instead of string for name for internationalization.
    private static final String NAME = "Exercise";
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new Workout(person);
    }

    @Override
    public double getProbability(Person person) {
        
        double result = 0D;

        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
            // Probability affected by the person's stress and fatigue.
            PhysicalCondition condition = person.getPhysicalCondition();
            result = condition.getStress() - (condition.getFatigue() / 10D)
                    + 20D;
            if (result < 0D) {
                result = 0D;
            }

            // Get an available gym.
            Building building = Workout.getAvailableGym(person);
            if (building != null) {
                result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, building);
                result *= TaskProbabilityUtil.getRelationshipModifier(person, building);
            } 
            else {
                result = 0D;
            }
        }

        // Effort-driven task modifier.
        result *= person.getPerformanceRating();

        return result;
    }
}