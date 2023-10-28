/*
 * Mars Simulation Project
 * MetaTask.java
 * @date 2022-06-24
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task.util;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.authority.PreferenceCategory;
import com.mars_sim.core.authority.PreferenceKey;
import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.environment.SurfaceFeatures;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.mission.MissionManager;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.robot.RobotType;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.time.MasterClock;
import com.mars_sim.core.vehicle.Vehicle;

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
	
	private static final String EVA_MODIFIER = "eva";
	protected static final String GARAGED_MODIFIER = "garaged";
	protected static final String GOODS_MODIFIER = "goods";
	protected static final String FAV_MODIFIER = "favourite";
	private static final String JOB_MODIFIER = "job";
	protected static final String PERSON_MODIFIER = "person";
	private static final String RADIATION_MODIFIER = "radiation";
	private static final String ROLE_MODIFIER = "role";
    protected static final String STRESS_MODIFIER = "stress";
	private static final String VEHICLE_MODIFIER = "vehicle";

	// Traits used to identify non-effort tasks
	private static final Set<TaskTrait> PASSIVE_TRAITS
		= Set.of(TaskTrait.RELAXATION, 
				TaskTrait.TREATMENT,
				TaskTrait.LEADERSHIP,
				TaskTrait.MEDICAL);
	
	/** Probability penalty for starting a non-job-related task. */
	private static final double ROLE_BONUS = 1.25D;  // Default preferred role bonus
	private static final double NON_ROLE_PENALTY = .25D;  // Weight if not preferred role
	protected static final double JOB_BONUS = 1D;  // Default preferred job bonus
	private static final double NON_JOB_PENALTY = .25D;  // Weight if not preferred job

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
	private Map<JobType,Double> preferredJobs = new EnumMap<>(JobType.class);
	private Set<RobotType> preferredRobots = new HashSet<>();
	private Map<RoleType,Double> preferredRoles = new EnumMap<>(RoleType.class);
	
	
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
		for(JobType j : jobs) {
    		this.preferredJobs.put(j, JOB_BONUS);
		}
	}

	/**
	 * Sets the preferred jobs for this Task.
	 * 
	 * @param jobs
	 */
    protected void setPreferredJob(JobType... jobs) {
		for(JobType j : jobs) {
    		this.preferredJobs.put(j, JOB_BONUS);
		}
	}
	
	/**
	 * Add a preferred job with a specific weight. The weight is appleid to the Rating Score.
	 * @param job
	 * @param w weight.
	 */
    protected void addPreferredJob(JobType job, double w) {
		this.preferredJobs.put(job, w);
	}

	/**
	 * Add a preferred role with a specific weight. The weight is appleid to the Rating Score.
	 * @param role
	 * @param w weight.
	 */
	protected void addPreferredRole(RoleType role, double w) {
		preferredRoles.put(role, w);
	}

	/**
	 * Sets the preferred roles for this Task.
	 * 
	 * @param jobs
	 */
    protected void setPreferredRole(RoleType... roles) {
		for(RoleType r : roles) {
			preferredRoles.put(r, ROLE_BONUS);
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
	 * Gets a unique non-internalised key for this task.
	 * @Note getName is an internationalised value.
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
		return preferredJobs.keySet();
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
	 * @param p Person requesting work.
	 * @return The score for this person
	 * @see #assessPersonSuitability(RatingScore, Person)
     */
	public RatingScore assessPersonSuitability(SettlementTask t, Person p) {
        RatingScore factor = RatingScore.ZERO_RATING;
        if (p.isInSettlement()) {
			factor = new RatingScore(t.getScore());
			factor = assessPersonSuitability(factor, p);
			if (t.isEVA()) {
				// EVA factor is the radiation and the EVA modifiers applied extra
				factor.addModifier(RADIATION_MODIFIER, TaskProbabilityUtil.getRadiationModifier(p.getSettlement()));
				factor.addModifier(EVA_MODIFIER, TaskProbabilityUtil.getEVAModifier(p));
			}
		}
		return factor;
	}

	/**
	 * Assesses the suitability of this person to do Tasks of this MetaType. It does not consider
	 * any of the specific details of the actual Task.
	 * 1. If the task has a Trait that is performance related the Person's performance rating is applied as a modifier
	 * 2. Apply the Job start modifier for this task
	 * 3. Apply the Persons individual preference to this Task
	 * 
	 * @param person Person being assessed
	 * @param score The base rating score that is adjusted to this person
	 * @return
	 */
	protected RatingScore assessPersonSuitability(RatingScore score, Person person) {

        // Effort-driven task modifier.
		if (effortDriven) {
			score.addModifier("effort", person.getPerformanceRating());
		}
		
        // Job modifier. If not my job suitable then a penalty.
	 	// Rules are:
	 	// 1. Person must have a Job
	 	// 2. Task must have Preferred Jobs
	 	// 3. If the Person's job is not in the Preferred list then a penalty is applied. 
		JobType job = person.getMind().getJob();
        if ((job != null) && !preferredJobs.isEmpty()) {
			score.addModifier(JOB_MODIFIER, preferredJobs.getOrDefault(job, NON_JOB_PENALTY));
        }
		
		// Role modifier. If suitable role then add a bonus
	 	// Rules are:
	 	// 1. Person must have a Role
	 	// 2. Role is preferred then add bonus
		var role = person.getRole();
        if ((role != null) && !preferredRoles.isEmpty()) {
			score.addModifier(ROLE_MODIFIER, preferredRoles.getOrDefault(role.getType(), NON_ROLE_PENALTY));
        }

        score.addModifier(FAV_MODIFIER, (1 + (person.getPreference().getPreferenceScore(this)/5D)));

		// Apply the home base modifier
		score.addModifier("settlement", person.getAssociatedSettlement().getPreferenceModifier(
							new PreferenceKey(PreferenceCategory.TASK_WEIGHT, getID())));
		
		return score;
	}

	/**
	 * Assess if this Person is in a moving vehicle. If the Person is in a Vehicle and it is moving
	 * then apply the moving vehicle bonus as a modifier.
	 * @param result Current score
	 * @param person Being assessed
	 * @return
	 */
	protected static RatingScore assessMoving(RatingScore result, Person person) {
		if (person.isInVehicle() && Vehicle.inMovingRover(person)) {
			result.addModifier(VEHICLE_MODIFIER,2);
		}

		return result;
	}	
		
	/**
	 * This will apply a number of modifier to the current score based on the Person to produce a modifier.
	 * 1. If the task has a Trait that is performance related the Person's performance rating is applied as a modifier
	 * 2. Apply the Job start modifier for this task
	 * 3. Apply the Persons individual preference to this Task
	 * 
	 * @deprecated #assessPersonSuitability(RatingScore, Person)
	 * @param person Person scoring Task
	 * @return Modified score.
	 */
	protected double getPersonModifier(Person person) {
        RatingScore temp = new RatingScore(1D);
		assessPersonSuitability(temp, person);
		return temp.getScore();
	}

	/**
	 * Gets the modifier for a Person using a building.
	 * 
	 * @param building Building the Person is entering
	 * @param person Person working
	 * @deprecated Replace with {@link #assessBuildingSuitability(RatingScore, Building, Person)}
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
	 * Assesses the suitability of a Building to do a Task.
	 * 
	 * @param score Base rating
	 * @param building Building the Person is entering
	 * @param person Person working
	 * @return Modified Rating score
	 */
	protected static RatingScore assessBuildingSuitability(RatingScore score, Building building,
															Person person) {
		if (building != null) {
			score.addModifier("crowding",
					TaskProbabilityUtil.getCrowdingProbabilityModifier(person, building));
			score.addModifier("occupants",
					TaskProbabilityUtil.getRelationshipModifier(person, building));
		}

		return score;
	}

	public String toString() {
		return name;
	}
	
	/**
	 * Gets the current Martian time.
	 * 
	 * @return MarsTime from master clock
	 */
	protected static MarsTime getMarsTime() {
		return masterClock.getMarsTime();
	}

	/**
	 * Gets a reference to the master clock.
	 * 
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
