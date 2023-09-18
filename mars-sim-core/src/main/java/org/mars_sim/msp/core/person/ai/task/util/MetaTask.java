/*
 * Mars Simulation Project
 * MetaTask.java
 * @date 2022-06-24
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.data.RatingScore;
import org.mars_sim.msp.core.environment.SurfaceFeatures;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.fav.FavoriteType;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.reportingAuthority.PreferenceCategory;
import org.mars_sim.msp.core.reportingAuthority.PreferenceKey;
import org.mars_sim.msp.core.robot.RobotType;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.time.MarsTime;
import org.mars_sim.msp.core.time.MasterClock;

/**
 * Class for a meta task, responsible for determining task probability and
 * constructing task instances.
 */
public abstract class MetaTask {
	
	/**
	 *  Defines the type of Worker support by this Task.
	 */
	protected enum WorkerType {
		PERSON, ROBOT, BOTH;
	}
	
	/**
	 *  Defines the scope of this Task.
	 */
	protected enum TaskScope {
		ANY_HOUR, WORK_HOUR, NONWORK_HOUR
	}
	
	private static final String PERSON_MODIFIER = "person";
	protected static final String BUILDING_MODIFIER = "building";
	private static final String EVA_MODIFIER = "eva";
	private static final String RADIATION_MODIFIER = "radiation";


	// Traits used to identify non-effort tasks
	private static final Set<TaskTrait> PASSIVE_TRAITS
		= Set.of(TaskTrait.RELAXATION, 
				TaskTrait.TREATMENT,
				TaskTrait.LEADERSHIP,
				TaskTrait.MEDICAL);
	
	/** Probability penalty for starting a non-job-related task. */
	private static final double NON_JOB_PENALTY = .25D;
	
	private static final String META = "Meta";
	
	protected static SurfaceFeatures surfaceFeatures;
	private static MasterClock masterClock;
	protected static MissionManager missionManager;
	
	private boolean effortDriven = true;
	
	/* The string name for this meta task. */	
	private String name;
	/* The simple name for this task (Note: it's not the same as its task name found in Msg). */
	private String id;
	
	private WorkerType workerType;
	private TaskScope scope;
	
	private Set<TaskTrait> traits = Collections.emptySet();
	private Set<FavoriteType> favourites = Collections.emptySet();
	private Set<JobType> preferredJobs = new HashSet<>();
	private Set<RobotType> preferredRobots = new HashSet<>();
	private Set<RoleType> preferredRoles = new HashSet<>();
	
	
	/**
	 * Constructor.
	 * 
	 * @param name
	 * @param workerType
	 * @param scope
	 */
	protected MetaTask(String name, WorkerType workerType, TaskScope scope) {
		super();
		this.name = name;
		this.workerType = workerType;
		this.scope = scope;
		this.id = this.getClass().getSimpleName().replace(META, "").toUpperCase();
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
	 * Sets the preferred roles for this Task. This overwrites any previous values.
	 * 
	 * @param jobs
	 */
    protected void setPreferredRole(Set<RoleType> roles) {
    	this.preferredRoles = roles;
	}

	/**
	 * Sets the preferred roles for this Task.
	 * 
	 * @param jobs
	 */
    protected void setPreferredRole(RoleType... roles) {
        Collections.addAll(this.preferredRoles, roles);
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
	 * Gets a unique non-internalised key for this task.
	 * Note getName is an internationalised value.
	 * 
	 * @return the MetaTask class name with "meta" removed
	 */
	public String getID() {
		return id;
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
	 * Gets the Person favourites that are suited to this Task.
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
	 * Adds a type of robot as preferred.
	 * 
	 * @param rt New robotType
	 */
	protected void addPreferredRobot(RobotType rt) {
		preferredRobots.add(rt);
	}

	/**
	 * Sets the preferred jobs for this Task.
	 * 
	 * @param jobs
	 */
    protected void addPreferredRobot(RobotType... rt) {
        Collections.addAll(this.preferredRobots, rt);
	}
	
	
	/**
     * Gets the score for a Settlement task for a person. This considers and EVA factor for eva maintenance.
     * 
	 * @param t Task being scored
	 * @parma p Person requesting work.
	 * @return The score for this person
     */
	public RatingScore assessPersonSuitability(SettlementTask t, Person p) {
        RatingScore factor = RatingScore.ZERO_RATING;
        if (p.isInSettlement()) {
			factor = new RatingScore(t.getScore());
			factor.addModifier(PERSON_MODIFIER, getPersonModifier(p));
			if (t.isEVA()) {
				// EVA factor is the radiation and the EVA modifiers applied extra
				factor.addModifier(RADIATION_MODIFIER, TaskProbabilityUtil.getRadiationModifier(p.getSettlement()));
				factor.addModifier(EVA_MODIFIER, TaskProbabilityUtil.getEVAModifier(p));
			}
		}
		return factor;
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

        score = score * (1 + (person.getPreference().getPreferenceScore(this)/5D));

		// Apply the home base modifier
		score = score * person.getAssociatedSettlement().getPreferenceModifier(
							new PreferenceKey(PreferenceCategory.TASK_WEIGHT, getID()));

        if (score < 0) score = 0;
        return score;
	}

	/**
	 * Applies a modified based on the Job. Rules are:
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
	 * Gets the modifier for a Person using a building.
	 * 
	 * @param building Building the Person is entering
	 * @param person Person working
	 */
	protected static double getBuildingModifier(Building building, Person person) {
		double result = 1D;
		if (building != null) {
			result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, building);
			result *= TaskProbabilityUtil.getRelationshipModifier(person, building);
		}
		return result;
	}

	/**
	 * Get the currnt martian time
	 * @return MarsTime from master clock
	 */
	protected static MarsTime getMarsTime() {
		return masterClock.getMarsTime();
	}

	/**
	 * Get a reference to the master clock
	 * @return
	 */
	protected static MasterClock getMasterClock() {
		return masterClock;
	}

	/**
	 * Attaches to the common controlling classes.
	 * 
	 * @param sim
	 */
	static void initialiseInstances(Simulation sim) {
		masterClock = sim.getMasterClock();
		surfaceFeatures = sim.getSurfaceFeatures();
		missionManager = sim.getMissionManager();
	}
}
