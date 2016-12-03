/**
 * Mars Simulation Project
 * WorkoutMeta.java
 * @version 3.08 2015-06-08
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.person.ai.task.Workout;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * Meta task for the Workout task.
 */
public class WorkoutMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.workout"); //$NON-NLS-1$

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

        
        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT
        		|| person.getLocationSituation() == LocationSituation.IN_VEHICLE) {

            // Probability affected by the person's stress and fatigue.
            PhysicalCondition condition = person.getPhysicalCondition();
            result = 20D + condition.getStress() * 2D;
            
            double fatigue = condition.getFatigue();
            
            if (fatigue > 2000)
            	result = result - 20D;

            else if (fatigue < 700)
            	result = result + fatigue/10D;
            
            // Get an available gym.
            Building building = Workout.getAvailableGym(person);
            if (building != null) {
                result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, building);
                result *= TaskProbabilityUtil.getRelationshipModifier(person, building);
            } // a person can still have workout on his own without a gym in MDP Phase 1-3

        }

        // Effort-driven task modifier.
        result *= person.getPerformanceRating();

        // Modify if working out is the person's favorite activity.
        if (person.getFavorite().getFavoriteActivity().equalsIgnoreCase("Workout")) {
            result *= 2D;
        }

        // 2015-06-07 Added Preference modifier
        if (result > 0)
         	result = result + result * person.getPreference().getPreferenceScore(this)/5D;

        if (result < 0) result = 0;

    
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