/**
 * Mars Simulation Project
 * Mind.java
 * @version 3.1.0 2017-12-16
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PersonalityTraitManager;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.job.JobAssignmentType;
import org.mars_sim.msp.core.person.ai.job.JobHistory;
import org.mars_sim.msp.core.person.ai.job.JobManager;
import org.mars_sim.msp.core.person.ai.job.Politician;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.person.ai.task.TaskManager;
import org.mars_sim.msp.core.structure.ChainOfCommand;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.tool.MathUtils;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The Mind class represents a person's mind. It keeps track of missions and
 * tasks which the person is involved.
 */
public class Mind implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(Mind.class.getName());
	
	private static final double FACTOR = .05;

	// Data members
	/** Is the job locked so another can't be chosen? */
	private boolean jobLock;
	/** The cache for sol. */
	private int solCache = 1;
	/** The cache for msol. */
	private int msolCache = -1;
	/** The cache for msol1. */
	private double msolCache1 = -1D;
	/** The person owning this mind. */
	private Person person = null;
	/** The person's task manager. */
	private TaskManager taskManager;
	/** The person's current mission (if any). */
	private Mission mission;
	/** The person's job. */
	private Job job;
	/** The person's personality. */
	private PersonalityType mbti;
	/** The person's emotional states. */	
	private EmotionManager emotion;

	private PersonalityTraitManager trait;
	/** The person's skill manager. */
	private SkillManager skillManager;

	private MissionManager missionManager;

	private static Simulation sim;

	private MarsClock marsClock;

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

		sim = Simulation.instance();
		if (sim.getMasterClock() != null) // for passing maven test
			marsClock = sim.getMasterClock().getMarsClock();

		// Construct the Big Five personality trait.
		trait = new PersonalityTraitManager(person);
		// Construct the MBTI personality type.
		mbti = new PersonalityType(person);
		// Construct the emotion states.
		emotion = new EmotionManager(person);
		// Construct the task manager
		taskManager = new TaskManager(this);
		// Load the mission manager
		missionManager = sim.getMissionManager();
		// Construct the skill manager.
		skillManager = new SkillManager(person);
	}

	/**
	 * Time passing.
	 * 
	 * @param time the time passing (millisols)
	 * @throws Exception if error.
	 */
	public void timePassing(double time) {
		if (taskManager != null)
			taskManager.recordTask();

		double msol1 = marsClock.getMillisolOneDecimal();

		if (msolCache1 != msol1) {
			msolCache1 = msol1;

			int msol = marsClock.getMillisolInt();

			if (msol % 3 == 0) {
//				msolCache = msol;

				// Update stress based on personality.
				mbti.updateStress(time);
	
				// Update emotion
				updateEmotion();
				
				// Update relationships.
				sim.getRelationshipManager().timePassing(person, time);
			}

			// Note : for now a Mayor/Manager cannot switch job
			if (job instanceof Politician)
				jobLock = true;

			else {
				if (jobLock) {
					// Note: for non-manager, the new job will be locked in until the beginning of
					// the next day
					// check for the passing of each day
					int solElapsed = marsClock.getMissionSol();
					if (solElapsed != solCache) {
						solCache = solElapsed;
						jobLock = false;
					}
				} else
					checkJob();
			}

			// Take action as necessary.
			if (taskManager != null)
				takeAction(time);
		}

	}

	/*
	 * Checks if a person has a job. If not, get a new one.
	 */
	public void checkJob() {
		// Check if this person needs to get a new job or change jobs.
		if (job == null) { // removing !jobLock
			// Note: getNewJob() is checking if existing job is "good enough"/ or has good
			// prospect
			Job newJob = JobManager.getNewJob(person);
			// Already excluded mayor/manager job from being assigned in
			// JobManager.getNewJob()
			String newJobStr = newJob.getName(person.getGender());
			String jobStr = null;
			if (job != null)
				jobStr = job.getName(person.getGender());
			if (newJob != null) {
				if (!newJobStr.equals(jobStr)) {
					// job = newJob;
					setJob(newJob, false, JobManager.SETTLEMENT, JobAssignmentType.APPROVED, JobManager.SETTLEMENT);
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
		// Job newJob = JobManager.getNewJob(person);
		setJob(JobManager.getNewJob(person), true, assignedBy, JobAssignmentType.APPROVED, assignedBy);
	}

	/**
	 * Take appropriate action for a given amount of time.
	 * 
	 * @param time time in millisols
	 * @throws Exception if error during action.
	 */
	public void takeAction(double time) {

		boolean hasActiveTask = taskManager.hasActiveTask();
		// Perform a task if the person has one, or determine a new task/mission.
		if (hasActiveTask) {
			double remainingTime = taskManager.executeTask(time, person.getPerformanceRating());
			if (remainingTime > 0D) {
				// Call takeAction recursively until time = 0
				takeAction(remainingTime);
			}
		} else {
			if ((mission != null) && mission.isDone()) {
				mission = null;
			}

			boolean hasActiveMission = hasActiveMission();// (mission != null);
			// Check if mission creation at settlement (if any) is overridden.
			boolean overrideMission = false;

			if (person.isInSettlement()) {
				overrideMission = person.getSettlement().getMissionCreationOverride();
			}

			if (hasActiveMission) {
				mission.performMission(person);
			}

			// A person has no active task
			if (!taskManager.hasActiveTask()) {
				try {
					getNewAction(true, (!hasActiveMission && !overrideMission));
				} catch (Exception e) {
					logger.log(Level.WARNING, person + " could not get new action", e);
					e.printStackTrace(System.err);
				}
			}

//			 if (hasActiveTask || hasActiveMission) {
//				 takeAction(time);
//			 Recursive calling causing Exception in thread "pool-4-thread-217"
//			 java.lang.StackOverflowError
//			 org.mars_sim.msp.core.person.ai.Mind.takeAction(Mind.java:242)
//			 }
		}
	}

	/**
	 * Reassign the person's job.
	 * 
	 * @param newJob           the new job
	 * @param bypassingJobLock
	 */
	public void reassignJob(String newJobStr, boolean bypassingJobLock, String assignedBy, JobAssignmentType status,
			String approvedBy) {
		// TODO: Add position of the one who approve 
		// Called by
		// (1) ReviewJobReassignment's constructor or
		// (2) TabPanelCareer's actionPerformed() [for a pop <= 4 settlement]
		Job newJob = null;
		Iterator<Job> i = JobManager.getJobs().iterator();
		while (i.hasNext()) {
			Job job = i.next();
			String n = job.getName(person.getGender());
			if (newJobStr.equals(n))
				// gets selectedJob by running through iterator to match it
				newJob = job;
		}

		assignJob(newJob, newJobStr, bypassingJobLock, assignedBy, status, approvedBy);
	}

	/**
	 * Sets the person's job.
	 * 
	 * @param newJob           the new job
	 * @param bypassingJobLock
	 * @param assignedBy
	 * @param status           of JobAssignmentType
	 * @param approvedBy
	 */
	public void setJob(Job newJob, boolean bypassingJobLock, String assignedBy, 
			JobAssignmentType status, String approvedBy) {
		// Called by
		// (1) setRole() in Person.java (if a person has a Manager role type)
		// (2) checkJob() in Mind.java
		// (3) getInitialJob() in Mind.java
		// TODO : if jobLock is true, will it allow the job to be changed?
		String newJobStr = newJob.getName(person.getGender());
		assignJob(newJob, newJobStr, bypassingJobLock, assignedBy, status, approvedBy);
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
	public void assignJob(Job newJob, String newJobStr, boolean bypassingJobLock, String assignedBy,
			JobAssignmentType status, String approvedBy) {
		String jobStr = null;
		JobHistory jh = person.getJobHistory();
		Settlement s = person.getSettlement();

		if (job == null)
			jobStr = null;
		else
			jobStr = job.getName(person.getGender());

		// TODO : check if the initiator's role allows the job to be changed
		if (!newJobStr.equals(jobStr)) {

			if (bypassingJobLock || !jobLock) {
				job = newJob;
				// Set up 4 approvedBy conditions
				if (approvedBy.equals(JobManager.SETTLEMENT)) { // automatically approved if pop <= 4
					jh.saveJob(newJob, assignedBy, status, approvedBy, true);
				} else if (approvedBy.equals(JobManager.USER)) {
					jh.saveJob(newJob, assignedBy, status, approvedBy, true);
				} else if (approvedBy.equals(JobManager.MISSION_CONTROL)) { // at the start of sim
					jh.saveJob(newJob, assignedBy, status, approvedBy, false);
				} else { // Call JobHistory's saveJob(),
						// approved by a Senior Official");
					jh.saveJob(newJob, assignedBy, status, approvedBy, false);
				}

				person.fireUnitUpdate(UnitEventType.JOB_EVENT, newJob);

				// Assign a new role type to the person and others in a settlement
				// immediately after the change of one's job type
				if (s != null) {
					ChainOfCommand cc = s.getChainOfCommand();

					// Assign a role associate with
					if (s.getNumCitizens() >= UnitManager.POPULATION_WITH_MAYOR) {
						cc.set7Divisions(true);
						cc.assignSpecialiststo7Divisions(person);
					}
					else {
						cc.set3Divisions(true);
						cc.assignSpecialiststo3Divisions(person);
					}
				}
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
	 * Set this mind as inactive. Needs move work on this; has to abort the Task can
	 * not just close it. This abort action would then allow the Mission to be also
	 * aborted.
	 */
	public void setInactive() {
		taskManager.clearTask();
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
	 * Determines a new action for the person based on available tasks, missions and
	 * active missions.
	 * 
	 * @param tasks    can the action be a new task?
	 * @param missions can the action be a new mission?
	 */
	public void getNewAction(boolean tasks, boolean missions) {

		// If this Person is too weak then they can not do Missions
		if (person.getPerformanceRating() < 0.5D) {
			missions = false;
		}

		// Get probability weights from tasks, missions and active missions.
		double taskWeights = 0D;
		double missionWeights = 0D;

		// Determine sum of weights based on given parameters
		double weightSum = 0D;

		if (tasks) {
			taskWeights = taskManager.getTotalTaskProbability(false);
			weightSum += taskWeights;
		}

		if (missions) {
			missionWeights = missionManager.getTotalMissionProbability(person);
			weightSum += missionWeights;
		}

		if ((weightSum <= 0D) || (Double.isNaN(weightSum)) || (Double.isInfinite(weightSum))) {
			try {
				TimeUnit.MILLISECONDS.sleep(100L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			throw new IllegalStateException("Mind.getNewAction(): " + person + " weight sum: " + weightSum);
		}

		// Select randomly across the total weight sum.
		double rand = RandomUtil.getRandomDouble(weightSum);

		// Determine which type of action was selected and set new action accordingly.
		if (tasks) {
			if (rand < taskWeights) {
				Task newTask = taskManager.getNewTask();
				if (newTask != null)
					taskManager.addTask(newTask);
				else
					logger.severe(person + " : newTask is null ");

				return;
			} else {
				rand -= taskWeights;
			}
		}

		if (missions) {
			if (rand < missionWeights) {
				Mission newMission = null;
//				logger.info(person.getName() + " is looking at what mission to take on.");
				newMission = missionManager.getNewMission(person);

				if (newMission != null) {
					missionManager.addMission(newMission);
					setMission(newMission);
				}

				return;
			} else {
				rand -= missionWeights;
			}
		}

		// If reached this point, no task or mission has been found.
		logger.severe(person.getName() + " couldn't determine new action - taskWeights: " + taskWeights
				+ ", missionWeights: " + missionWeights);
	}

	/**
	 * Calls the psi function
	 * @param av
	 * @param pv
	 * @return
	 */
	public double[] callPsi(double[] av, double[] pv) {
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
	public PersonalityType getMBTI() {
		return mbti;
	}

	/**
	 * Returns a reference to the Person's skill manager
	 * 
	 * @return the person's skill manager
	 */
	public SkillManager getSkillManager() {
		return skillManager;
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
	public TaskManager getTaskManager() {
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
	public Job getJob() {
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
		skillManager.destroy();
		skillManager = null;
	}
}