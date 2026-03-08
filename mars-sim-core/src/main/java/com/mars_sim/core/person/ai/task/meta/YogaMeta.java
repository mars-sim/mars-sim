/*
 * Mars Simulation Project
 * YogaMeta.java
 * @date 2025-08-19
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task.meta;

import java.util.List;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.person.ai.task.Yoga;
import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.tool.Msg;

/**
 * Meta task for the Yoga task.
 */
public class YogaMeta extends FactoryMetaTask {

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

    /**
     * Assess this Person for Yoga based on their physical condition/
     * @param person Beign assessed
     * @return Yoak tasks that can be performed
     */
    @Override
    public List<TaskJob> getTaskJobs(Person person) {

        if (person.isInVehicle()
            || !person.getPreference().isTaskDue(this)
            || !person.isInSettlement()) {
            return EMPTY_TASKLIST;
        }

        // Probability affected by the person's stress and fatigue.
        PhysicalCondition condition = person.getPhysicalCondition();
        double stress = condition.getStress();
        double fatigue = condition.getFatigue();
        double kJ = condition.getEnergy();
        double hunger = condition.getHunger();
        double painTolernce = condition.getMusclePainTolerance();
        double soreness = condition.getMuscleSoreness();
        double muscleHealth = condition.getMuscleHealth();
        
        if (kJ < 1500 || fatigue > 750 || hunger > 750)
            return EMPTY_TASKLIST;
        
        double base = kJ/2000 
            		// Note: The desire to exercise increases linearly right after waking up
            		// from bed up to the first 333 msols
            		// After the first 333 msols, it decreases linearly for the rest of the day
            		+ Math.max(333 - fatigue, -666)
            		// Note: muscle condition affects the desire to exercise
            		- painTolernce/2.5 - muscleHealth/2.5 + soreness 
            		+ stress / 10
            		- person.getCircadianClock().getTodayExerciseTime() * 5;

        if (base <= 0)
            return EMPTY_TASKLIST;

        RatingScore result = new RatingScore(base/10D);  // Workout score is divided by 10 as well  
        result = assessPersonSuitability(result, person);

        // Get an available gym.
        Building building =  BuildingManager.getAvailableFunctionTypeBuilding(person, FunctionType.EXERCISE);
        result = assessBuildingSuitability(result, building, person);
    
        return createTaskJobs(result);
    }
}
