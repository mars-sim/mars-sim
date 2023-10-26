/*
 * Mars Simulation Project
 * WorkoutMeta.java
 * @date 2022-07-20
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task.meta;

import java.util.List;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.task.Workout;
import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.tools.Msg;

/**
 * Meta task for the Workout task.
 */
public class WorkoutMeta extends FactoryMetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.workout"); //$NON-NLS-1$

    private static final double FACTOR = 10D;
	
    public WorkoutMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.NONWORK_HOUR);
		setFavorite(FavoriteType.SPORT);
		setTrait(TaskTrait.AGILITY, TaskTrait.RELAXATION);

	}
    
    @Override
    public Task constructInstance(Person person) {
        return new Workout(person);
    }

    /**
	 * Assess if this Person can perform any Workout tasks.
	 * 
	 * @param person the Person to perform the task.
	 * @return List of TasksJob specifications.
	 */
    @Override
	public List<TaskJob> getTaskJobs(Person person) {
               
        if (!person.isInSettlement()) {
            return EMPTY_TASKLIST;
        }

        // Probability affected by the person's stress and fatigue.
        PhysicalCondition condition = person.getPhysicalCondition();
        double stress = condition.getStress();
        double fatigue = condition.getFatigue();
        double kJ = condition.getEnergy();
        double hunger = condition.getHunger();
        double soreness = condition.getMuscleSoreness();
        double painTolernce = condition.getMusclePainTolerance();

        double exerciseMillisols = person.getCircadianClock().getTodayExerciseTime();
            
        if (kJ < 1000 || fatigue > 750 || hunger > 750)
            return EMPTY_TASKLIST;
 
        var result = new RatingScore((kJ/3000 
            		// Note: The desire to exercise increases linearly right after waking up
            		// from bed up to the first 333 msols
            		// After the first 333 msols, it decreases linearly for the rest of the day
            		+ Math.max(333 - fatigue, -666)/10
            		// Note: muscle condition affects the desire to exercise
            		+ painTolernce/2.5 - soreness/2.5 
            		+ stress / 10
            		- exerciseMillisols * 20)/FACTOR); // Why does this use a FACTOR ?

        
        result = assessPersonSuitability(result, person);

        // Get an available gym.
        Building building = BuildingManager.getAvailableFunctionTypeBuilding(person, FunctionType.EXERCISE);
        result = assessBuildingSuitability(result, building, person);

        return createTaskJobs(result);
    }
}
