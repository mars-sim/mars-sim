/**
 * Mars Simulation Project
 * FactoryMetaTask.java
 * @date 2022-11-13
 * @author Barry Evans
 */
package com.mars_sim.core.person.ai.task.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.robot.Robot;

/**
 * THis represents a MetaTask instance that creates a single Task per evaluaton. These
 * tasks will have no context and rely on the Task to identify the actual target when they start.
 * 
 * The side effect is these instances can support creating contextless Task instances with the need
 * for any extra information to be passed. Hence these are a Factory for their associated Task object.
 */
public abstract class FactoryMetaTask extends MetaTask {
    
	protected static UnitManager unitManager = Simulation.instance().getUnitManager();

	protected static final List<TaskJob> EMPTY_TASKLIST = Collections.emptyList();
	
	protected FactoryMetaTask(String name, WorkerType workerType, TaskScope scope) {
		super(name, workerType, scope);
	}

    /**
	 * Constructs an instance of the associated task. Is a Factory method and should
	 * be implemented by the subclass.
	 * 
	 * @param person the person to perform the task.
	 * @return task instance.
	 */
	public Task constructInstance(Person person) {
		throw new UnsupportedOperationException("Can not create '" + getName() + "' for Person.");
	}
	
	/**
	 * Constructs an instance of the associated task. Is a Factory method and should
	 * be implemented by the subclass.
	 * 
	 * @param person the person to perform the task.
	 * @return task instance.
	 */
	public Task constructInstance(Robot robot) {
		throw new UnsupportedOperationException("Can not create " + getName() + " for Robot.");
	}

	/**
	 * Gets the weighted probability value that the person might perform this task.
	 * A probability weight of zero means that the task has no chance of being
	 * performed by the person.
	 * 
	 * @param person the person to perform the task.
	 * @return weighted probability value (0 -> positive value).
	 * @deprecated Replace {@link #getTaskJobs(Person)}
	 */
	public double getProbability(Person person) {
		throw new UnsupportedOperationException("Can not calculated the probability of " + getName()  + " for Person.");
	}

	/**
	 * Gets the weighted probability value that the person might perform this task.
	 * A probability weight of zero means that the task has no chance of being
	 * performed by the robot.
	 * 
	 * @param robot the robot to perform the task.
	 * @return weighted probability value (0 -> positive value).
	 */
	public double getProbability(Robot robot) {
		throw new UnsupportedOperationException("Can not calculated the probability of " + getName()  + " for Robot.");
	}

    /**
	 * Gets the list of Task that this Person can perform all individually scored.
	 * 
	 * @param person the Person to perform the task.
	 * @return List of TasksJob specifications.
	 */
	public List<TaskJob> getTaskJobs(Person person) {
		double score = getProbability(person);
		return createTaskJobs(new RatingScore(score));
	}

	/**
	 * Gets the list of Task that this Robot can perform all individually scored.
	 * 
	 * @param robot the robot to perform the task.
	 * @return List of TasksJob specifications.
	 */
	public List<TaskJob> getTaskJobs(Robot robot) {
		double score = getProbability(robot);
		return createTaskJobs(new RatingScore(score));
	}

	
	/**
	 * Create a list of Task Jobs for this Factory containing a single item.
	 * The item is added if the score is more than zero.
	 * 
	 * @param score Score to the job to create.
	 * @return List contain the task jobs
	 */
	protected List<TaskJob> createTaskJobs(RatingScore score) {
		// This is to avoid a massive rework in the subclasses.
		if (score.getScore() <= 0) {
			return EMPTY_TASKLIST;
		}

		// Put a maximum limit
		score.applyRange(0, 1000D);
		List<TaskJob> result = new ArrayList<>(1);
		result.add(new BasicTaskJob(this, score));
		return result;
	}
}
