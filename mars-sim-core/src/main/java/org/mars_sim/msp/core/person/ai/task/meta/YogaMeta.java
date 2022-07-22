/**
 * Mars Simulation Project
 * YogaMeta.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.task.Workout;
import org.mars_sim.msp.core.person.ai.task.Yoga;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskTrait;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * Meta task for the Yoga task.
 */
public class YogaMeta extends MetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.yoga"); //$NON-NLS-1$
 
    public YogaMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.NONWORK_HOUR);
		setTrait(TaskTrait.TREATMENT, TaskTrait.AGILITY, TaskTrait.RELAXATION);
	}
    
    @Override
    public Task constructInstance(Person person) {
        return new Yoga(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;

        if (!person.getPreference().isTaskDue(this) && person.isInSettlement()) {

            // Probability affected by the person's stress and fatigue.
            PhysicalCondition condition = person.getPhysicalCondition();
            double stress = condition.getStress();
            double fatigue = condition.getFatigue();
            double hunger = condition.getHunger();
            
            if (fatigue > 1000 || hunger > 750)
            	return 0;
            
        	// Doing yoga is less popular than doing regular workout
            result += fatigue / 20D;
            if (result < 0D) {
                result = 0D;
            }
            
            double pref = person.getPreference().getPreferenceScore(this);
         	result += pref * 1.5D;
         	
            if (pref > 0) {
            	result *= (1 + stress/20.0);
            }
            else
            	result = 0;

            // Get an available gym.
            Building building = Workout.getAvailableGym(person);
            if (building != null) {
                result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, building);
                result *= TaskProbabilityUtil.getRelationshipModifier(person, building);
            } // a person can still have workout on his own without a gym in MDP Phase 1-3
        }
        
        return result;
    }
}
