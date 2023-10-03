/**
 * Mars Simulation Project
 * FactoryMetaTask.java
 * @date 2022-11-13
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person.ai.task.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.data.Rating;
import org.mars_sim.msp.core.data.RatingScore;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.robot.Robot;

/**
 * THis represents a MetaTask instance that creates a single Task per evaluaton. These
 * tasks will have no context and rely on the Task to identify the actual target when they start.
 * 
 * The side effect is these instances can support creating contextless Task instances with the need
 * for any extra information to be passed. Hence these are a Factory for their associated Task object.
 */
public abstract class FactoryMetaTask extends MetaTask {
    
	protected static UnitManager unitManager = Simulation.instance().getUnitManager();
	
	protected FactoryMetaTask(String name, WorkerType workerType, TaskScope scope) {
		super(name, workerType, scope);
	}
	
    /**
	 * Constructs an instance of the associated task. Is a Factory method and should
	 * be implemented by the subclass.
	 * 
	 * @param person the person to perform the task.
	 * @param duration
	 * @return task instance.
	 */
	protected Task constructInstance(Person person, int duration) {
		throw new UnsupportedOperationException("Can not create '" + getName() + "' for Person.");
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
	 * @deprecated Replace {@link #getRating(Person)}
	 */
	public double getProbability(Person person) {
		throw new UnsupportedOperationException("Can not calculated the probability of " + getName()  + " for Person.");
	}

	/**
	 * Gets the rating score of a person performing this task.
	 * A score of zero means that the task has no chance of being
	 * performed by the person.
	 * 
	 * @param person the person to perform the task.
	 * @return Rating score
	 */
	protected RatingScore getRating(Person person) {
		// TODO This method is a temporary implementation until all classes converted
		double score = getProbability(person);
		return new RatingScore(score);
	}

	/**
	 * Gets the weighted probability value that the person might perform this task.
	 * A probability weight of zero means that the task has no chance of being
	 * performed by the robot.
	 * 
	 * @param robot the robot to perform the task.
	 * @return weighted probability value (0 -> positive value).
	 * @deprecated Replace with {@link #getProbability(Robot)}
	 */
	public double getProbability(Robot robot) {
		throw new UnsupportedOperationException("Can not calculated the probability of " + getName()  + " for Robot.");
	}

	/**
	 * Gets the RatingScore if this Robot performed this task.
	 * A rating of zero means that the task has no chance of being
	 * performed by the robot.
	 * 
	 * @param robot the robot to perform the task.
	 * @return Rating score
	 */
	protected RatingScore getRating(Robot robot) {
		// TODO This is the default implementation and will be eventually replaced
		double score = getProbability(robot);
		return new RatingScore(score);
	}

    /**
	 * Gets the list of Task that this Person can perform all individually scored.
	 * 
	 * @param person the Person to perform the task.
	 * @return List of TasksJob specifications.
	 */
	public List<TaskJob> getTaskJobs(Person person) {
		return createTaskJob(getRating(person));
	}

	/**
	 * Gets the list of Task that this Robot can perform all individually scored.
	 * 
	 * @param robot the robot to perform the task.
	 * @return List of TasksJob specifications.
	 */
	public List<TaskJob> getTaskJobs(Robot robot) {
		return createTaskJob(getRating(robot));
	}

	
	/**
	 * Creates a TaskJob instance delegate where this instance handles Task creation.
	 * 
	 * @param score Score to the job to create.
	 */
	private List<TaskJob> createTaskJob(RatingScore score) {
		// This is to avoid a massive rework in the subclasses.
		if (score.getScore() <= 0) {
			return Collections.emptyList();
		}

		List<TaskJob> result = new ArrayList<>(1);
		result.add(new BasicTaskJob(this, score));
		return result;
	}

	public String toString() {
		return getName();
	}
}
