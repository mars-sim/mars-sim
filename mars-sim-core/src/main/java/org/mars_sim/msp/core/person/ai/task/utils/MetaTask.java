/**
 * Mars Simulation Project
 * MetaTask.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.utils;

import java.util.HashSet;
import java.util.Set;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.events.HistoricalEventManager;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.social.RelationshipManager;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.science.ScientificStudyManager;
import org.mars_sim.msp.core.time.MarsClock;

import com.google.common.collect.ImmutableSet;

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
	private final static Set<TaskTrait> PASSIVE_TRAITS
		= ImmutableSet.of(TaskTrait.RELAXATION, TaskTrait.TREATMENT,
						  TaskTrait.LEADERSHIP,
						  TaskTrait.MEDICAL);

	// If a person Job is not the preferred old
	private static final double JOB_BOOST = 1.25D;
	
	/** Probability penalty for starting a non-job-related task. */
	private static final double NON_JOB_PENALTY = .25D;
	
		// TODO not all subcalssess need all these !!!!!!
	protected static Simulation sim = Simulation.instance();
	/** The static instance of the mars clock */
	protected static MarsClock marsClock = sim.getMasterClock().getMarsClock();
	/** The static instance of the event manager */
	protected static HistoricalEventManager eventManager = sim.getEventManager();
	/** The static instance of the relationship manager */
	protected static RelationshipManager relationshipManager = sim.getRelationshipManager();
	/** The static instance of the UnitManager */	
	protected static UnitManager unitManager = sim.getUnitManager();
	/** The static instance of the ScientificStudyManager */
	protected static ScientificStudyManager scientificStudyManager = sim.getScientificStudyManager();
	/** The static instance of the SurfaceFeatures */
	protected static SurfaceFeatures surface = sim.getMars().getSurfaceFeatures();
	/** The static instance of the MissionManager */
	protected static MissionManager missionManager = sim.getMissionManager();
	
	private String name;
	private WorkerType workerType;
	private TaskScope scope;
	private Set<TaskTrait> traits = new HashSet<>();
	private Set<FavoriteType> favourites = new HashSet<>();
	private boolean effortDriven = true;
	private Set<JobType> preferredJob = new HashSet<>();
	
	protected MetaTask(String name, WorkerType workerType, TaskScope scope) {
		super();
		this.name = name;
		this.workerType = workerType;
		this.scope = scope;
	}

	protected void addFavorite(FavoriteType fav) {
		favourites.add(fav);
	}
	
	protected void addTrait(TaskTrait trait) {
		traits.add(trait);
		
		// If effort driven make sure the trait a passive trait
		if (effortDriven) {
			effortDriven = !PASSIVE_TRAITS.contains(trait);
		}
	}
	
	/**
	 * Set the preferred jobs for this Task
	 * @param jobs
	 */
    protected void setPreferredJob(Set<JobType> jobs) {
    	this.preferredJob = jobs;
	}

	/**
	 * Set the preferred jobs for this Task
	 * @param jobs
	 */
    protected void setPreferredJob(JobType... jobs) {
    	for (JobType jobType : jobs) {
			this.preferredJob.add(jobType);
		}
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
	 * Get the Job that is most suitable to this Task.
	 * @return
	 */
	public Set<JobType> getPreferredJob() {
		return preferredJob;
	}
	
	/**
	 * What is the scope for this Task is be done.
	 */
	public final TaskScope getScope() {
		return scope;
	}

	/**
	 * What worker type is supported by this task.
	 * @return
	 */
	public final WorkerType getSupported() {
		return workerType;
	}
	
	/**
	 * Get the traits of this Task
	 * @return
	 */
	public Set<TaskTrait> getTraits() {
		return traits;		
	}

	/**
	 * Get the Person Favourites that are suited to this Task
	 * @return
	 */
	public Set<FavoriteType> getFavourites() {
		return favourites;
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
	 * This will apply a number of modifier to the current score based on the Person
	 * 1. If the task has a Trait that is performance related the Person's performance rating is applied as a modifier
	 * 2. Apply the Job start modifier for this task
	 * 3. Apply the Persons indiviudla preference to this Task
	 * @param score Current base score
	 * @param person Person scoring Task
	 * @return Modified score.
	 */
	protected double applyPersonModifier(double score, Person person) {
        
        // Effort-driven task modifier.
		if (effortDriven) {
			score *= person.getPerformanceRating();
		}
		
		score = applyJobModifier(score, person);

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
	 * @param result
	 * @param person
	 * @return
	 */
	protected double applyJobModifier(double score, Person person) {
		
        // Job modifier. If not myjob then a penalty.
		// But only if the Task has preferred jobs defined
        JobType job = person.getMind().getJob();
        if ((job != null) && !preferredJob.isEmpty()
        		&& !preferredJob.contains(job)) {
            score *= NON_JOB_PENALTY;
        }
        
        return score;
	}

}
