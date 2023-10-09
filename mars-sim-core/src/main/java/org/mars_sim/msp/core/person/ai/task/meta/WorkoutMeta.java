/*
 * Mars Simulation Project
 * WorkoutMeta.java
 * @date 2022-07-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.List;

import org.mars.sim.tools.Msg;
import org.mars_sim.msp.core.data.RatingScore;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.fav.FavoriteType;
import org.mars_sim.msp.core.person.ai.task.Workout;
import org.mars_sim.msp.core.person.ai.task.util.FactoryMetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskJob;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;

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
        double[] muscle = condition.getMusculoskeletal();

        double exerciseMillisols = person.getCircadianClock().getTodayExerciseTime();
            
        if (kJ < 1000 || fatigue > 750 || hunger > 750)
            return EMPTY_TASKLIST;
 
        var result = new RatingScore((kJ/3000 
            		// Note: The desire to exercise increases linearly right after waking up
            		// from bed up to the first 333 msols
            		// After the first 333 msols, it decreases linearly for the rest of the day
            		+ Math.max(333 - fatigue, -666)/10
            		// Note: muscle condition affects the desire to exercise
            		+ muscle[0]/2.5 - muscle[2]/2.5 
            		+ stress / 10
            		- exerciseMillisols * 20)/FACTOR); // Why does this use a FACTOR ?

        
        result = assessPersonSuitability(result, person);

        // Get an available gym.
        Building building = BuildingManager.getAvailableFunctionTypeBuilding(person, FunctionType.EXERCISE);
        result = assessBuildingSuitability(result, building, person);

        return createTaskJobs(result);
    }
}
