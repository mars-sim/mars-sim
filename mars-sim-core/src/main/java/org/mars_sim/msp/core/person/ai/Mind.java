/*
 * Mars Simulation Project
 * Mind.java
 * @date 2022-08-06
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai;

import java.io.Serializable;
import java.util.List;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.environment.SurfaceFeatures;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.util.JobAssignmentType;
import org.mars_sim.msp.core.person.ai.job.util.JobHistory;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.job.util.JobUtil;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.person.ai.social.Relation;
import org.mars_sim.msp.core.person.ai.social.RelationshipUtil;
import org.mars_sim.msp.core.person.ai.task.util.PersonTaskManager;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.structure.OverrideType;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.Temporal;
import org.mars_sim.msp.core.tool.MathUtils;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The Mind class represents a person's mind. It keeps track of missions and
 * tasks which the person is involved.
 */
public class Mind implements Serializable, Temporal {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(Mind.class.getName());

	private static final int MAX_EXECUTE = 100; // Maximum number of iterations of a Task per pulse
	private static final int MAX_ZERO_EXECUTE = 100; // Maximum number of executeTask action that consume no time
	private static final int STRESS_UPDATE_CYCLE = 10;
	private static final double MINIMUM_MISSION_PERFORMANCE = 0.3;
	private static final double FACTOR = .05;
	private static final double SMALL_AMOUNT_OF_TIME = 0.001;

	// Data members
	/** Is the job locked so another can't be chosen? */
	private boolean jobLock;

	/** The person owning this mind. */
	private Person person = null;

	/** The person's task manager. */
	private PersonTaskManager taskManager;
	/** The person's current mission (if any). */
	private Mission mission;
	/** The person's job. */
	private JobType job;
	/** The person's personality. */
	private MBTIPersonality mbti;
	/** The person's emotional states. */
	private EmotionManager emotion;
	/** The person's personality trait manager. */
	private PersonalityTraitManager trait;
	/** The person's relationship with others. */
	private Relation relation;
	
	private static MissionManager missionManager;
	private static SurfaceFeatures surfaceFeatures;

	static {
		Simulation sim = Simulation.instance();
		// Load the mission manager
		missionManager = sim.getMissionManager();
		// Load SurfaceFeatures
		surfaceFeatures = sim.getSurfaceFeatures();
	}

	/**
	 * Constructor 1.
	 *
	 * @param person the person owning this mind
	 * @throws Exception if mind could not be created.
	 */
	public Mind(Person person) {
		// Initialize data members
		this.person = person;
		mission = null;
		job = null;
		jobLock = false;

		// Construct the Big Five personality trait.
		trait = new PersonalityTraitManager(person);
		// Construct the MBTI personality type.
		mbti = new MBTIPersonality(person);
		// Construct the emotion states.
		emotion = new EmotionManager(person);
		// Construct the task manager.
		taskManager = new PersonTaskManager(this);
		// Construct the Relation instance.
		relation = new Relation(person);
	}

	/**
	 * Time passing.
	 *
	 * @param time the time passing (millisols)
	 * @throws Exception if error.
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {
		if (taskManager != null) {
			taskManager.timePassing(pulse);
			// Decides what tasks to inject time
			if (pulse.getElapsed() > 0)
				decideTask(pulse.getElapsed());
		}

		int msol = pulse.getMarsTime().getMillisolInt();
		if (msol % STRESS_UPDATE_CYCLE == 0) {
			// Update stress based on personality.
			mbti.updateStress(pulse.getElapsed());
			// Update emotion
			updateEmotion();
			// Update relationships.
			RelationshipUtil.timePassing(person, pulse.getElapsed());
		}

		// Note : for now, a Mayor/Manager cannot switch job
		if (jobLock && job != JobType.POLITICIAN) {
			// check for the passing of each day
			if (pulse.isNewSol()) {
				// Note: for non-managerial position, the new job needs to be locked in
				// (i.e. no change allowed) until the beginning of the next sol
				jobLock = false;
			}
		} else if (job == null) {
			// Assign a new job but do not bypass jobLock
			getAJob(false, JobUtil.SETTLEMENT);
		}

		return true;
	}

	/**
	 * Assigns a job, either at the start of the sim or later.
	 *
	 * @param bypassingJobLock 
	 * @param assignedBy the authority that assigns the job
	 */
	public void getAJob(boolean bypassingJobLock, String assignedBy) {
		// Note: getNewJob() also checks if existing job is "good enough"/ or has good prospect
		JobType newJob = JobUtil.getNewJob(person);
		if (newJob != null)
			assignJob(newJob, bypassingJobLock, assignedBy, JobAssignmentType.APPROVED, assignedBy);
	}

	/**
	 * Decides what tasks to take for a given amount of time.
	 * 
	 * @param time time in millisols
	 * @throws Exception if error during action.
	 */
	private void decideTask(double time) {
		double remainingTime = time;
		double pulseTime = Task.getStandardPulseTime();
		while (remainingTime > 0 && pulseTime > 0) {
			// Vary the amount of time to be injected
			double rand = RandomUtil.getRandomDouble(.8, 1);
			double deltaTime = pulseTime * rand;
			if (remainingTime > deltaTime) {
				// Call takeAction to perform a task and consume the pulse time.
				takeAction(deltaTime);
				// Reduce the total time by the pulse time
				remainingTime -= deltaTime;
			}
			else {
				// Call takeAction to perform a task and consume the pulse time.
				takeAction(remainingTime);
				// Reduce the total time by the pulse time
				remainingTime = 0;
			}
		}
	}

	/**
	 * Takes appropriate action for a given amount of time.
	 *
	 * @param time time in millisols
	 * @throws Exception if error during action.
	 */
	private void takeAction(double time) {
		double pulseTime = time;
		int zeroCount = 0;    // Count the number of conseq. zero executions
		int callCount = 0;
		// Loop around using up time; recursion can blow stack memory
		do {
			// Perform a task if the person has one, or determine a new task/mission.
			if (taskManager.hasActiveTask()) {
				// Call executeTask
				double remainingTime = taskManager.executeTask(pulseTime, person.getPerformanceRating());

				// A task is return a bad remaining time.
				// Cause of Issue#290
				if (!Double.isFinite(remainingTime) || (remainingTime < 0)) {
					// Likely to be a defect in a Task
					logger.warning(person, 20_000, "Calling '"
							+ taskManager.getTaskName() + "' and return an invalid time " + remainingTime);
					return;
				}
				
				// Can not return more time than originally available
				if (remainingTime > pulseTime) {
					// Likely to be a defect in a Task or rounding problem
					logger.warning(person, 20_000, "'"
							+ taskManager.getTaskName() + "' and return a remaining time " + remainingTime
							+ " larger than the original " + pulseTime);
					return;
				}

				// Safety check to track a repeating Task loop
				if (callCount >= MAX_EXECUTE) {
					logger.warning(person, 20_000, "Calling '"
							+ taskManager.getTaskName() + "' for "
							+ callCount + " iterations.");
					callCount++;
					return;
				}
				if (remainingTime == pulseTime) {
					// No time has been consumed previously
					// This is not supposed to happen but still happens a lot.
					// Reduce the time by standardPulseTime
					remainingTime = pulseTime - Task.standardPulseTime; 
					// Reset the idle counter					
					if (zeroCount++ >= MAX_ZERO_EXECUTE) {
						return;
					}
				}
				else {
					zeroCount = 0;
				}
				pulseTime = remainingTime;
			}
			else {
				// Look for a new task
				lookForATask();
				
				// Don't have an active task. Consume time
				if (!taskManager.hasActiveTask()) {
					// Didn't find a new Task so abort action
					pulseTime = 0;
				}
			}
			
		} while (pulseTime > SMALL_AMOUNT_OF_TIME);
	}

	/**
	 * Looks for a new task
	 */
	private void lookForATask() {

		boolean hasActiveMission = false;
		if (mission != null) {
			if (mission.isDone()) {
				// Set the mission to null since it is done
				mission = null;
			}
			else {
				hasActiveMission = true;
			}
		}

		if (hasActiveMission) {
			if (mission.getMissionType() == MissionType.DELIVERY) {
				// In case of a delivery mission, the person doesn't need to be onboard
				if (mission.getPhase() != null) {
					resumeMission(0);
				}
			}

			else {
				// If the mission vehicle has embarked but the person is not on board,
				// then release the person from the mission

				if (mission.getPhase() != null) {
			        // Missions have to be done and are stressfull so allow high stress.
					if (person.getPhysicalCondition().getPerformanceFactor() < 0.7D)
			        	// Cannot perform the mission if a person is not well
			        	// Note: If everyone has dangerous medical condition during a mission,
			        	// then it won't matter and someone needs to drive the rover home.
						// Add penalty in resuming the mission
						resumeMission(1);
					else {
						resumeMission(2);
					}
				}
			}
		}

		if (!taskManager.hasActiveTask()) {
			// don't have an active mission
			taskManager.startNewTask();
		}
	}

	/**
	 * Resumes a mission
	 *
	 * @param modifier
	 */
	private void resumeMission(int modifier) {
		int fitness = person.getPhysicalCondition().computeFitnessLevel();
		int priority = mission.getPriority();
		int rand = RandomUtil.getRandomInt(5);
		if (rand - (fitness)/1.5D <= priority + modifier) {
			mission.performMission(person);
		}
	}

	/**
	 * Checks if a person can start a new mission
	 *
	 * @return
	 */
	public boolean canStartNewMission() {
		boolean hasAMission = hasAMission();
//		if (hasAMission)
//			logger.info(person + " had the " + mission + " mission");;

		boolean hasActiveMission = hasActiveMission();

		boolean overrideMission = false;

		// Check if mission creation at settlement (if any) is overridden.
		overrideMission = person.getAssociatedSettlement().getProcessOverride(OverrideType.MISSION);

		// Check if it's within the mission request window
		// Within the mission window since the beginning of the work shift
		boolean isInMissionWindow = person.getTaskSchedule().isPersonAtStartOfWorkShift();

		// See if this person can ask for a mission
		return !hasActiveMission && !hasAMission && !overrideMission && isInMissionWindow;
	}

	/**
	 * Reassign the person's job.
	 *
	 * @param newJob           the new job
	 * @param bypassingJobLock
	 */
	public void reassignJob(JobType newJob, boolean bypassingJobLock, String assignedBy, JobAssignmentType status,
			String approvedBy) {
		assignJob(newJob, bypassingJobLock, assignedBy, status, approvedBy);
	}

	/**
	 * Assigns a person a new job.
	 *
	 * @param newJob           the new job
	 * @param bypassingJobLock
	 * @param assignedBy
	 * @param status           of JobAssignmentType
	 * @param approvedBy
	 */
	public void assignJob(JobType newJob, boolean bypassingJobLock, String assignedBy,
			JobAssignmentType status, String approvedBy) {
		JobHistory jh = person.getJobHistory();

		// Future: check if the initiator's role allows the job to be changed
		if (newJob != job) {

			if (bypassingJobLock || !jobLock) {
				// Set to the new job
				job = newJob;
				// Set up 4 approvedBy conditions
				if (approvedBy.equals(JobUtil.SETTLEMENT)) { 
					// Automatically approved if pop <= 4
					jh.saveJob(newJob, assignedBy, status, approvedBy, true);
				} else if (approvedBy.equals(JobUtil.USER)) {
					jh.saveJob(newJob, assignedBy, status, approvedBy, true);
				} else if (approvedBy.equals(JobUtil.MISSION_CONTROL)) { 
					// At the start of sim
					jh.saveJob(newJob, assignedBy, status, approvedBy, false);
				} else { 
					// Approved by a senior official, etc.
					jh.saveJob(newJob, assignedBy, status, approvedBy, false);
				}

//				logger.log(person, Level.CONFIG, 0, "Assigned as " + newJob.getName()
//								+ " by " + approvedBy + ".");

				person.fireUnitUpdate(UnitEventType.JOB_EVENT, newJob);

				// Note: the new job will be Locked in until the beginning of the next day
				jobLock = true;
			}
		}
	}

	/**
	 * Returns true if person has an active mission.
	 *
	 * @return true for active mission
	 */
	public boolean hasActiveMission() {
        return (mission != null) && !mission.isDone();
	}

	/**
	 * Returns true if person has a mission.
	 *
	 * @return true for active mission
	 */
	public boolean hasAMission() {
        //			// has a mission but need to determine if this mission is active or not
        //			if (mission.isApproved()
        //				|| (mission.getPlan() != null
        //					&& mission.getPlan().getStatus() != PlanType.NOT_APPROVED))
        return mission != null;
    }

	/**
	 * Sets this mind as inactive. Needs move work on this; has to abort the Task can
	 * not just close it. This abort action would then allow the Mission to be also
	 * aborted.
	 */
	public void setInactive() {
		taskManager.clearAllTasks("Inactive");
		if (hasActiveMission()) {
			mission.removeMember(person);
			mission = null;
		}
	}

	/**
	 * Sets the person's current mission.
	 *
	 * @param newMission the new mission
	 */
	public void setMission(Mission newMission) {
		if (newMission != mission) {
			if (mission != null) {
				mission.removeMember(person);
			}
			mission = newMission;

			if (newMission != null) {
				newMission.addMember(person);
			}

			person.fireUnitUpdate(UnitEventType.MISSION_EVENT, newMission);
		}
	}

	/**
	 * Stops the person's current mission.
	 *
	 */
	public void stopMission() {
		mission = null;
	}

	/**
	 * Determines a new mission for the person.
	 */
	public void getNewMission() {
		// If this Person is too weak then they can not do Missions
		if (person.getPerformanceRating() < MINIMUM_MISSION_PERFORMANCE) {
			return;
		}

		// The previous code was using an extra random that did not add any value because the extra weight was always 0
		Mission newMission = missionManager.getNewMission(person);
		if (newMission != null) {
			setMission(newMission);
		}
	}


	/**
	 * Calls the psi function.
	 *
	 * @param av
	 * @param pv
	 * @return
	 */
	private static double[] callPsi(double[] av, double[] pv) {
		double[] v = new double[2];

		for (int i=0; i<pv.length; i++) {
			if (i == 0) { // Openness
				if (pv[0] > .5) {
					v[0] = av[0] + pv[0]/2D * FACTOR; // Engagement
					v[1] = av[1] + pv[0]/2D * FACTOR; // Valence
				}
				else if (pv[0] < .5) {
					v[0] = av[0] - pv[0] * FACTOR; // Engagement
					v[1] = av[1] - pv[0] * FACTOR; // Valence
				}
			}
			else if (i == 1) { // Conscientiousness
				if (pv[1] > .5) {
//					v[0] = av[0] + pv[1]/2D * FACTOR; // Engagement
					v[1] = av[1] + pv[1]/2D * FACTOR; // Valence
				}
				else if (pv[1] < .5) {
//					v[0] = av[0] - pv[1] * FACTOR; // Engagement
					v[1] = av[1] - pv[1] * FACTOR; // Valence
				}

			}
			else if (i == 2) { // Extraversion
				if (pv[2] > .5) {
					v[0] = av[0] + pv[2]/2D * FACTOR;
//					v[1] = av[1] + pv[2]/2D * FACTOR;
				}
				else if (pv[2] < .5) {
					v[0] = av[0] - pv[2] * FACTOR;
//					v[1] = av[1] - pv[2] * FACTOR;
				}

			}
			else if (i == 3) { // Agreeableness
				if (pv[3] > .5) {
//					v[0] = av[0] + pv[3]/2D * FACTOR; // Engagement
					v[1] = av[1] + pv[3]/2D * FACTOR; // Valence
				}
				else if (pv[3] < .5) {
//					v[0] = av[0] - pv[3] * FACTOR;
					v[1] = av[1] - pv[3] * FACTOR;
				}
			}
			else if (i == 4) { // Neuroticism
				if (pv[4] > .5) {
					v[0] = av[0] - pv[4]/2D * FACTOR;
					v[1] = av[1] - pv[4]/2D * FACTOR;
				}
				else if (pv[4] < .5) {
					v[0] = av[0] + pv[4] * FACTOR;
					v[1] = av[1] + pv[4] * FACTOR;
				}
			}

			if (v[0] > .8)
				v[0] = .8;
			else if (v[0] < 0)
				v[0] = 0;

			if (v[1] > .8)
				v[1] = .8;
			else if (v[1] < 0)
				v[1] = 0;

		}

		return v;
	}

	/**
	 * Updates the emotion states
	 */
	public void updateEmotion() {
		// Check for stimulus
		emotion.checkStimulus();

//		int dim = emotion.getDimension();
		
		// Get the prior history vector
		List<double[]> wVector = emotion.getOmegaVector(); 
		// Get the personality vector
		double[] pVector = trait.getPersonalityVector(); 
		// Get the new emotional stimulus/Influence vector
		double[] aVector = emotion.getEmotionInfoVector(); 
		// Get the existing emotional State vector
		double[] eVector = emotion.getEmotionVector();

		// Call Psi Function - with desire changes aVector
		double[] psi = callPsi(aVector, pVector);//new double[dim];

		// Call Omega Function - internal changes such as decay of emotional states
		// for normalize vectors
		double[] omega = MathUtils.normalize(wVector.get(wVector.size()-1)); //new double[dim];

		double[] newE = new double[2];

		for (int i=0; i<2; i++) {
			newE[i] = (eVector[i] + psi[i] + omega[i]) / 2.05;
		}

		// Find the new emotion vector
		// java.lang.OutOfMemoryError: Java heap space
//		double[] e_tt = DoubleStream.concat(Arrays.stream(eVector),
//				Arrays.stream(psi)).toArray();
//		double[] e_tt2 = DoubleStream.concat(Arrays.stream(e_tt),
//				Arrays.stream(omega)).toArray();
		// java.lang.OutOfMemoryError: Java heap space
//		double[] e_tt = MathUtils.concatAll(eVector, psi, omega);

		if (newE[0] > .8)
			newE[0] = .8;
		else if (newE[0] < 0)
			newE[0] = 0;

		if (newE[1] > .8)
			newE[1] = .8;
		else if (newE[1] < 0)
			newE[1] = 0;

		// Update the emotional states
		emotion.updateEmotion(newE);
	}

	public EmotionManager getEmotion() {
		return emotion;
	}

	/**
	 * Gets the person's MBTI (personality type).
	 *
	 * @return personality type.
	 */
	public MBTIPersonality getMBTI() {
		return mbti;
	}

	/**
	 * Returns the person owning this mind.
	 *
	 * @return person
	 */
	public Person getPerson() {
		return person;
	}

	/**
	 * Returns the person's task manager.
	 *
	 * @return task manager
	 */
	public PersonTaskManager getTaskManager() {
		return taskManager;
	}

	/**
	 * Returns the person's current mission. Returns null if there is no current
	 * mission.
	 *
	 * @return current mission
	 */
	public Mission getMission() {
		return mission;
	}

	/**
	 * Gets the person's job.
	 *
	 * @return job or null if none.
	 */
	public JobType getJob() {
		return job;
	}

	/**
	 * Checks if the person's job is locked and can't be changed.
	 *
	 * @return true if job lock.
	 */
	public boolean getJobLock() {
		return jobLock;
	}

	/*
	 * Sets the value of jobLock so that the job can or cannot be changed.
	 * 
	 * @param value
	 */
	public void setJobLock(boolean value) {
		jobLock = value;
	}

	/**
	 * Gets the PersonalityTraitManager instance.
	 * 
	 * @return the PersonalityTraitManager instance 
	 */
	public PersonalityTraitManager getTraitManager() {
		return trait;
	}

	/**
	 * Gets the relation instance.
	 * 
	 * @return the relation instance 
	 */
	public Relation getRelation( ) {
		return relation;
	}

	/**
	 * Reloads instances after loading from a saved sim.
	 *
	 * @param m missionManager instance
	 */
	public static void initializeInstances(MissionManager m) {
		missionManager = m;
	}

	public void reinit() {
		taskManager.reinit();
	}

	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		person = null;
		taskManager.destroy();
		mission = null;
		job = null;
		if (mbti != null)
			mbti.destroy();
		mbti = null;
	}
}
