/**
 * Mars Simulation Project
 * Mind.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.JobAssignmentType;
import org.mars_sim.msp.core.person.ai.job.JobHistory;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.job.JobUtil;
import org.mars_sim.msp.core.person.ai.mission.Delivery;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.social.RelationshipManager;
import org.mars_sim.msp.core.person.ai.task.utils.PersonTaskManager;
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
	private static final int MAX_ZERO_EXECUTE = 10; // Maximum number of executeTask action that consume no time
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

	private static MissionManager missionManager;
	private static RelationshipManager relationshipManager;
	private static SurfaceFeatures surfaceFeatures;

	static {
		Simulation sim = Simulation.instance();
		// Load the mission manager
		missionManager = sim.getMissionManager();
		// Load the relationship manager
		relationshipManager = sim.getRelationshipManager();
		// Load SurfaceFeatures
		surfaceFeatures = sim.getMars().getSurfaceFeatures();
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
		// Construct the task manager
		taskManager = new PersonTaskManager(this);
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
			
			// Take action as necessary.
			takeAction(pulse.getElapsed());
		}
		
		int msol = pulse.getMarsTime().getMillisolInt();
		if (msol % STRESS_UPDATE_CYCLE == 0) {

			// Update stress based on personality.
			mbti.updateStress(pulse.getElapsed());

			// Update emotion
			updateEmotion();
			
			// Update relationships.
			relationshipManager.timePassing(person, pulse.getElapsed());
		}

		// Note : for now a Mayor/Manager cannot switch job
		if (job == JobType.POLITICIAN)
			jobLock = true;

		else {
			if (jobLock) {
				// Note: for non-manager, the new job will be locked in until the beginning of
				// the next day
				// check for the passing of each day
				if (pulse.isNewSol()) {
					jobLock = false;
				}
			} else
				checkJob();
		}
		
		return true;
	}

	/*
	 * Checks if a person has a job. If not, get a new one.
	 */
	private void checkJob() {
		// Check if this person needs to get a new job or change jobs.
		if (job == null) { // removing !jobLock
			// Note: getNewJob() is checking if existing job is "good enough"/ or has good
			// prospect
			JobType newJob = JobUtil.getNewJob(person);
			if (newJob != null) {
				if (newJob != job) {
					assignJob(newJob, false, JobUtil.SETTLEMENT, JobAssignmentType.APPROVED, JobUtil.SETTLEMENT);
				}
			}
		}
	}

	/**
	 * Assigns the first job at the start of the sim
	 * 
	 * @param assignedBy the authority that assigns the job
	 */
	public void getInitialJob(String assignedBy) {
		JobType newJob = JobUtil.getNewJob(person);
		if (newJob != null)
			assignJob(newJob, true, assignedBy, JobAssignmentType.APPROVED, assignedBy);
	}

	/**
	 * Take appropriate action for a given amount of time.
	 * 
	 * @param time time in millisols
	 * @throws Exception if error during action.
	 */
	private void takeAction(double time) {
		double remainingTime = time;
		int zeroCount = 0;    // Count the number of conseq. zero executions
		int callCount = 0;
		// Loop around using up time; recursion can blow stack memory
		do {			
			// Perform a task if the person has one, or determine a new task/mission.
			if (taskManager.hasActiveTask()) {
				double newRemain = taskManager.executeTask(remainingTime, person.getPerformanceRating());

				// A task is return a bad remaining time. 
				// Cause of Issue#290
				if (!Double.isFinite(newRemain) || (newRemain < 0)) {
					// Likely to be a defect in a Task
					logger.warning(person, "Doing '" 
							+ taskManager.getTaskName() + "' returned an invalid time " + newRemain);
					return;
				}
				// Can not return more time than originally available
				else if (newRemain > remainingTime) {
					// Likely to be a defect in a Task or rounding problem
					logger.warning(person, "'" 
							+ taskManager.getTaskName() + "' returned a remaining time " + newRemain
							+ " larger than the original " + remainingTime);
					return;
				}
				
				// Safety check to track a repeating Task loop
				if (callCount++ >= MAX_EXECUTE) {
					logger.warning(person, "Doing '" 
							+ taskManager.getTaskName() + "' done for "
							+ callCount + " iterations.");
					return;
				}
				
				// Consumed time then reset the idle counter
				if (remainingTime == newRemain) {
					if (zeroCount++ >= MAX_ZERO_EXECUTE) {
//						logger.warning(person, "Doing '" 
//								+ taskManager.getTaskName() + "/"
//								+ taskManager.getPhase().getName()
//								+ "' consumed no time for "
//								+ zeroCount + " consecutive iterations.");
						return;
					}
				}
				else {
					zeroCount = 0;
				}
				remainingTime = newRemain;
			}
			else {
				// don't have an active task
				lookForATask();
				if (!taskManager.hasActiveTask()) {
					// Didn't find a new Task so abort action
					remainingTime = 0;
				}
			}
		}
		while (remainingTime > SMALL_AMOUNT_OF_TIME);
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

			// In case of a delivery mission, the bot doesn't need to be onboard
			// If the mission vehicle has embarked but the person is not on board, 
			// then release the person from the mission
			if (!(mission instanceof Delivery) && !(mission.getCurrentMissionLocation().equals(person.getCoordinates()))) {
				mission.removeMember(person);
				logger.info(person, "Not boarded and taken out of " + mission + " mission.");
				mission = null;
			}
			
			else if (mission.getPhase() != null) {
		        boolean inDarkPolarRegion = surfaceFeatures.inDarkPolarRegion(mission.getCurrentMissionLocation());
				double sun = surfaceFeatures.getSunlightRatio(mission.getCurrentMissionLocation());
				if ((sun <= 0.1) && !inDarkPolarRegion) {
						resumeMission(-2);
				}
				
				// Checks if a person is tired, too stressful or hungry and need 
				// to take break, eat and/or sleep
				else if (!person.getPhysicalCondition().isFit()
		        	&& !mission.hasDangerousMedicalProblemsAllCrew()) {
		        	// Cannot perform the mission if a person is not well
		        	// Note: If everyone has dangerous medical condition during a mission, 
		        	// then it won't matter and someone needs to drive the rover home.
					// Add penalty in resuming the mission
					resumeMission(-1);
				}
				else {
					resumeMission(0);
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
		if (mission.canParticipate(person) && person.isFit()) {
			int fitness = person.getPhysicalCondition().computeFitnessLevel();
			int priority = mission.getPriority();
			int rand = RandomUtil.getRandomInt(6);
			if (rand - (fitness)/1.5D <= priority + modifier) {
//						// See if this person can ask for a mission
//						boolean newMission = !hasActiveMission && !hasAMission && !overrideMission && isInMissionWindow;							
				mission.performMission(person);
//						logger.info(person + " was to perform the " + mission + " mission");
			}
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

		// TODO : check if the initiator's role allows the job to be changed
		if (newJob != job) {

			if (bypassingJobLock || !jobLock) {
				job = newJob;
				// Set up 4 approvedBy conditions
				if (approvedBy.equals(JobUtil.SETTLEMENT)) { // automatically approved if pop <= 4
					jh.saveJob(newJob, assignedBy, status, approvedBy, true);
				} else if (approvedBy.equals(JobUtil.USER)) {
					jh.saveJob(newJob, assignedBy, status, approvedBy, true);
				} else if (approvedBy.equals(JobUtil.MISSION_CONTROL)) { // at the start of sim
					jh.saveJob(newJob, assignedBy, status, approvedBy, false);
				} else { // Call JobHistory's saveJob(),
						// approved by a Senior Official");
					jh.saveJob(newJob, assignedBy, status, approvedBy, false);
				}
				
				logger.log(person, Level.CONFIG, 0, "Becomes " + newJob.getName()
								+ ", approved by " + approvedBy + ".");

				person.fireUnitUpdate(UnitEventType.JOB_EVENT, newJob);
				
				// the new job will be Locked in until the beginning of the next day
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
	 * Set this mind as inactive. Needs move work on this; has to abort the Task can
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
			missionManager.addMission(newMission);
			setMission(newMission);
		}
	}

	
	/**
	 * Calls the psi function
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
		
		List<double[]> wVector = emotion.getOmegaVector(); // prior history
		double[] pVector = trait.getPersonalityVector(); // personality
		double[] aVector = emotion.getEmotionInfoVector(); // new stimulus
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
	 * Returns the person's task manager
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
	 * Gets the person's job
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
	 * Set the value of jobLock so that the job can or cannot be changed
	 */
	public void setJobLock(boolean value) {
		jobLock = value;
	}

	public PersonalityTraitManager getTraitManager() {
		return trait;
	}


	/**
	 * Reloads instances after loading from a saved sim
	 * 
	 * @param clock
	 */
	public static void initializeInstances(MissionManager m, RelationshipManager r) {
		relationshipManager = r;
		missionManager = m;
	}
	
	public void reinit() {
		taskManager.reinit();
	}
	
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		person = null;
		taskManager.destroy();
		if (mission != null)
			mission.destroy();
		mission = null;
		job = null;
		if (mbti != null)
			mbti.destroy();
		mbti = null;
	}
}
