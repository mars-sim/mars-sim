/*
 * Mars Simulation Project
 * MetaTask.java
 * @date 2022-06-24
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.environment.SurfaceFeatures;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.fav.FavoriteType;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotType;
import org.mars_sim.msp.core.structure.RadiationStatus;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * Class for a meta task, responsible for determining task probability and
 * constructing task instances.
 */
public abstract class MetaTask {
	
	/**
	 *  Defines the type of Worker support by this Task
	 */
	protected enum WorkerType {
		PERSON, ROBOT, BOTH;
	}
	
	/**
	 *  Defines the scope of this Task
	 */
	protected enum TaskScope {
		ANY_HOUR, WORK_HOUR, NONWORK_HOUR;
	}
	
	// Traits used to identify non-effort tasks
	private static final Set<TaskTrait> PASSIVE_TRAITS
		= Set.of(TaskTrait.RELAXATION, 
				TaskTrait.TREATMENT,
				TaskTrait.LEADERSHIP,
				TaskTrait.MEDICAL);
	
	/** Probability penalty for starting a non-job-related task. */
	private static final double NON_JOB_PENALTY = .25D;
	

	/** The static instance of the mars clock */
	protected static MarsClock marsClock;
	protected static SurfaceFeatures surfaceFeatures;
	
	private String name;
	private WorkerType workerType;
	private TaskScope scope;
	private Set<TaskTrait> traits = Collections.emptySet();
	private Set<FavoriteType> favourites = Collections.emptySet();
	private boolean effortDriven = true;
	private Set<JobType> preferredJobs = new HashSet<>();
	private Set<RobotType> preferredRobots = new HashSet<>();
	
	protected MetaTask(String name, WorkerType workerType, TaskScope scope) {
		super();
		this.name = name;
		this.workerType = workerType;
		this.scope = scope;
	}

	/**
	 * Defines the Person favourites for this Task. This will overwrite any
	 * previous favourites.
	 * 
	 * @param fav
	 */
	protected void setFavorite(FavoriteType... fav) {
		favourites = Set.of(fav);
	}
	
	/**
	 * Defines the Task traits for this Task. This will overwrite any
	 * previous traits.
	 * 
	 * @param trait
	 */
	protected void setTrait(TaskTrait... trait) {
		traits = Set.of(trait);
		
		// If effort driven make sure the trait is not passive trait
		// Maybe this should apply the reverse and look for effort based traits?
		for (TaskTrait t : traits) {
			if (effortDriven) {
				effortDriven = !PASSIVE_TRAITS.contains(t);
			}			
		}
	}
	
	/**
	 * Sets the preferred jobs for this Task. This overwrites any previous values.
	 * 
	 * @param jobs
	 */
    protected void setPreferredJob(Set<JobType> jobs) {
    	this.preferredJobs = jobs;
	}

	/**
	 * Sets the preferred jobs for this Task.
	 * 
	 * @param jobs
	 */
    protected void setPreferredJob(JobType... jobs) {
        Collections.addAll(this.preferredJobs, jobs);
	}
	
	/**
	 * Gets the associated task name.
	 * 
	 * @return task name string.
	 */
	public final String getName() {
		return name;
	}

	/**
	 * Gets the Job that is most suitable to this Task.
	 * 
	 * @return
	 */
	public Set<JobType> getPreferredJob() {
		return preferredJobs;
	}
	
	/**
	 * Returns the scope for this Task is be done.
	 * 
	 */
	public final TaskScope getScope() {
		return scope;
	}

	/**
	 * Returns worker type supported by this task.
	 * 
	 * @return
	 */
	public final WorkerType getSupported() {
		return workerType;
	}
	
	/**
	 * Gets the traits of this Task.
	 * 
	 * @return
	 */
	public Set<TaskTrait> getTraits() {
		return traits;		
	}

	/**
	 * Gets the Person Favourites that are suited to this Task
	 * 
	 * @return
	 */
	public Set<FavoriteType> getFavourites() {
		return favourites;
	}
		
	/**
	 * Gets the Robots that is most suitable to this Task.
	 * 
	 * @return
	 */
	public Set<RobotType> getPreferredRobot() {
		return preferredRobots;
	}

	/**
	 * Add a type of robot as preferred.
	 * @param rt New robotType
	 */
	protected void addPreferredRobot(RobotType rt) {
		preferredRobots.add(rt);
	}

	/**
	 * Constructs an instance of the associated task. Is a Factory method and should
	 * be implemented by the subclass.
	 * Governed by the {@link #getSupported()} method.
	 * 
	 * @param person the person to perform the task.
	 * @return task instance.
	 */
	public Task constructInstance(Person person) {
		throw new UnsupportedOperationException("Can not create " + name + " for Person.");
	}

	/**
	 * Constructs an instance of the associated task. Is a Factory method and should
	 * be implemented by the subclass.
	 * Governed by the {@link #getSupported()} method.
	 * 
	 * @param person the person to perform the task.
	 * @return task instance.
	 */
	public Task constructInstance(Robot robot) {
		throw new UnsupportedOperationException("Can not create " + name + " for Robot.");
	}

	/**
	 * Gets the weighted probability value that the person might perform this task.
	 * A probability weight of zero means that the task has no chance of being
	 * performed by the person.
	 * 
	 * @param person the person to perform the task.
	 * @return weighted probability value (0 -> positive value).
	 */
	public double getProbability(Person person) {
		throw new UnsupportedOperationException("Can not calculated the probability of " + name + " for Person.");
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
		throw new UnsupportedOperationException("Can not calculated the probability of " + name + " for Robot.");
	}
	
	/**
	 * Gets the list of Task that this Person can perform all individually scored.
	 * 
	 * @param person the Person to perform the task.
	 * @return List of TasksJob specifications.
	 */
	public List<TaskJob> getTaskJobs(Person person) {
		return createTaskJob(getProbability(person));
	}

	/**
	 * Gets the list of Task that this Robot can perform all individually scored.
	 * 
	 * @param robot the robot to perform the task.
	 * @return List of TasksJob specifications.
	 */
	public List<TaskJob> getTaskJobs(Robot robot) {
		return createTaskJob(getProbability(robot));
	}

	/**
	 * Creates a TaskJob instance delegate where this instance handles Task creation.
	 * @param score Score to the job to create.
	 */
	private List<TaskJob> createTaskJob(double score) {
		// This is a convience to avoid a massive rework in the subclasses.
		if (score <= 0) {
			return null;
		}

		List<TaskJob> result = new ArrayList<>(1);
		result.add(new BasicTaskJob(this, score));
		return result;
	}

	/**
	 * This will apply a number of modifier to the current score based on the Person.
	 * 1. If the task has a Trait that is performance related the Person's performance rating is applied as a modifier
	 * 2. Apply the Job start modifier for this task
	 * 3. Apply the Persons individual preference to this Task
	 * 
	 * @param score Current base score
	 * @param person Person scoring Task
	 * @return Modified score.
	 * @deprecated Use {@link #getPersonModifier(Person)}
	 */
	protected double applyPersonModifier(double score, Person person) {
		return score * getPersonModifier(person);
	}

	/**
	 * This will apply a number of modifier to the current score based on the Person to produce a modifier.
	 * 1. If the task has a Trait that is performance related the Person's performance rating is applied as a modifier
	 * 2. Apply the Job start modifier for this task
	 * 3. Apply the Persons individual preference to this Task
	 * 
	 * @param person Person scoring Task
	 * @return Modified score.
	 */
	protected double getPersonModifier(Person person) {
        double score = 1D;

        // Effort-driven task modifier.
		if (effortDriven) {
			score *= person.getPerformanceRating();
		}
		
		score *= getJobModifier(person);

        score = score * (1D + (person.getPreference().getPreferenceScore(this)/5D));

        if (score < 0) score = 0;
        
        return score;
	}
	
	/**
	 * Apply a modified based on the Job. Rules are:
	 * 1. Person must have a Job
	 * 2. Task must have Preferred Jobs
	 * 3. If the Person's job is not in the Preferred list then a penalty is applied.
	 * 
	 * @param person
	 * @return
	 */
	protected double getJobModifier(Person person) {
		double score = 1D;

        // Job modifier. If not my job then a penalty.
		// But only if the Task has preferred jobs defined
        JobType job = person.getMind().getJob();
        if ((job != null) && !preferredJobs.isEmpty()
        		&& !preferredJobs.contains(job)) {
            score *= NON_JOB_PENALTY;
        }
        
        return score;
	}

	/**
	 * Get the modifier value for a Task score based on the Radiation events occuring
	 * at a Settlement. Events will scale down teh modifier towards zero.
	 * @param settlement
	 * @return
	 */
    protected double getRadiationModifier(Settlement settlement) {
        RadiationStatus exposed = settlement.getExposed();
        double result = 1D;

        if (exposed.isSEPEvent()) {
            // SEP event stops all activities so zero factor
            result = 0D;
        }

    	if (exposed.isBaselineEvent()) {
    		// Baseline can give a fair amount dose of radiation
			result /= 50D;
		}

    	if (exposed.isGCREvent()) {
    		// GCR can give nearly lethal dose of radiation
			result /= 100D;
		}

        return result;
    }

	/**
	 * Get the modifier for a Person doing an EVA Operation
	 */
	protected double getEVAModifier(Person person) {
		// Check if an airlock is available
		if (EVAOperation.getWalkableAvailableAirlock(person, false) == null)
			return 0;

		// Check if it is night time.
		if (EVAOperation.isGettingDark(person))
			return 0;

		// Checks if the person's settlement is at meal time and is hungry
		if (EVAOperation.isHungryAtMealTime(person))
			return 0;
		
		// Checks if the person is physically fit for heavy EVA tasks
		if (!EVAOperation.isEVAFit(person))
			return 0;
		
		return 1D;
	}

	/**
	 * Attached to the common controllign classes.
	 */
	static void initialiseInstances(Simulation sim) {
		marsClock = sim.getMasterClock().getMarsClock();
		surfaceFeatures = sim.getSurfaceFeatures();
	}
}
