/*
 * Mars Simulation Project
 * Mind.java
 * @date 2025-08-25 (patched v2)
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai;

import java.io.Serializable;
import java.util.concurrent.ThreadLocalRandom;

import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.job.util.AssignmentHistory;
import com.mars_sim.core.person.ai.job.util.AssignmentType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.job.util.JobUtil;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionManager;
import com.mars_sim.core.person.ai.social.Relation;
import com.mars_sim.core.person.ai.social.RelationshipUtil;
import com.mars_sim.core.person.ai.task.util.PersonTaskManager;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.structure.OverrideType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.Temporal;
import com.mars_sim.core.tool.RandomUtil;

/**
 * The Mind class represents a person's mind. It keeps track of missions and
 * tasks which the person is involved.
 */
public class Mind implements Serializable, Temporal {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(Mind.class.getName());

	private static final int MAX_EXECUTE = 100; // Maximum number of iterations of a Task per pulse
	private static final int MAX_ZERO_EXECUTE = 100; // Maximum number of executeTask action that consume no time
	private static final int RELATION_UPDATE_CYCLE = 250;
	private static final int EMOTION_UPDATE_CYCLE = 250;
	private static final double MINIMUM_MISSION_PERFORMANCE = 0.3;
	private static final double SMALL_AMOUNT_OF_TIME = 0.001;

	// ------------------------------------------------------------------------------------
	// Session guard & repick debounce (reduce task churn / Sleep-Eat thrash)
	// ------------------------------------------------------------------------------------
	/** Soft minimum a colonist keeps a task if still valid (millisols). Tunable via -Dmars.sim.task.session.min */
	private static final double MIN_TASK_SESSION_MSOLS =
			Double.parseDouble(System.getProperty("mars.sim.task.session.min", "15.0"));
	/** Minimum time between full re-picks (millisols). Tunable via -Dmars.sim.task.repick.min */
	private static final double MIN_REPICK_INTERVAL_MSOLS =
			Double.parseDouble(System.getProperty("mars.sim.task.repick.min", "1.0"));
	/** Debounce jitter so agents don't retask in lockstep. */
	private static final double REPICK_JITTER_MSOLS = .25D;

	/** Local monotonic sim-time accumulator in millisols (advanced via timePassing). */
	private transient double simTimeMsols = 0D;
	/** When did we last start (or forcibly switch) a task? (sim millisols) */
	private transient Double lastTaskStartMsols;
	/** When did we last perform a full probability pick? (sim millisols) */
	private transient Double lastRepickMsols;
	/** Name of the task when it started (for diagnostics only). */
	private transient String lastStartedTaskName;

	private final int relationUpdate = RandomUtil.getRandomInt(RELATION_UPDATE_CYCLE);
	private final int emotionUpdate = RandomUtil.getRandomInt(EMOTION_UPDATE_CYCLE);

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
	private EmotionManager emotionMgr;
	/** The person's personality trait manager. */
	private PersonalityTraitManager trait;
	/** The person's relationship with others. */
	private Relation relation;

	private static MissionManager missionManager;

	/**
	 * Constructor.
	 *
	 * @param person the person owning this mind
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
		emotionMgr = new EmotionManager(person);
		// Construct the task manager.
		taskManager = new PersonTaskManager(this);
		// Construct the Relation instance.
		relation = new Relation(person);
	}

	/**
	 * Time passing.
	 *
	 * @param pulse the clock pulse
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {
		double time = pulse.getElapsed();

		// Advance local sim time accumulator (millisols)
		if (time > 0D && Double.isFinite(time)) {
			simTimeMsols += time;
		}

		if ((taskManager != null) && (time > 0)) {
			moderateTime(time);
		}

		if (pulse.isNewIntMillisol()) {
			// Update stress based on personality.
			mbti.updateStress(time);

			int msol = pulse.getMarsTime().getMillisolInt();
			if (msol % RELATION_UPDATE_CYCLE == relationUpdate) {
				// Update relationships.
				RelationshipUtil.timePassing(person, time);
			}
			if (msol % EMOTION_UPDATE_CYCLE == emotionUpdate) {
				// Update emotion with the personality vector
				emotionMgr.updateEmotion(trait.getPersonalityVector());
			}
		}

		// Note: for now, a Mayor/Manager cannot switch job
		if (jobLock && job != JobType.POLITICIAN) {
			// Check for the passing of each day
			if (pulse.isNewSol()) {
				// Note: for non-managerial position, the new job needs to be locked in
				// (i.e. no change allowed) until the beginning of the next sol
				jobLock = false;
			}
		}
		else if (job == null) {
			// Assign a new job but do not bypass jobLock
			getAJob(false, JobUtil.SETTLEMENT);
		}

		return true;
	}

	/**
	 * Assigns a job, either at the start of the sim or later.
	 *
	 * @param bypassingJobLock whether to bypass the job lock
	 * @param assignedBy the authority that assigns the job
	 */
	public void getAJob(boolean bypassingJobLock, String assignedBy) {
		// Note: getNewJob() also checks if existing job is "good enough"/ or has good prospect
		JobType newJob = JobUtil.getNewJob(person);
		if (newJob != null)
			assignJob(newJob, bypassingJobLock, assignedBy, AssignmentType.APPROVED, assignedBy);
	}

	/**
	 * Moderates the time for decisions.
	 *
	 * @param time in millisols
	 */
	private void moderateTime(double time) {
		double remaining = time;
		double pTime = Task.getStandardPulseTime();
		if (pTime == 0.0) {
			pTime = remaining;
		}
		while (remaining > 0) {
			if (remaining > pTime) {
				// Call takeAction to perform a task and consume the pulse time.
				takeAction(pTime);
				// Reduce the total time by the pulse time
				remaining -= pTime;
			}
			else {
				// Call takeAction to perform a task and consume the pulse time.
				takeAction(remaining);
				// Reduce the total time by the pulse time
				remaining = 0;
			}
		}
	}

	/**
	 * Takes appropriate action for a given amount of time.
	 *
	 * @param time time in millisols
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
				double remainingTime = taskManager.executeTask(pulseTime);

				// A task is returning a bad remaining time. Cause of Issue#290
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
					remainingTime = pulseTime - Task.getStandardPulseTime();
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
	 * Looks for a new task (respects mission, then applies a small debounce).
	 * A soft session guard is enforced when a task has just started to reduce thrash.
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

		if (hasActiveMission && !mission.isDone()) {
			// Missions have to be done and are stressful so allow high stress.
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

		// If still no active task, (re)pick one — but debounce to avoid storms
		if (!taskManager.hasActiveTask()) {

			// Session guard: if we *just* started a task recently and it ended instantly,
			// avoid immediate re-pick storms; also cooperates with manager-level sticky logic.
			if (lastTaskStartMsols != null) {
				double age = Math.abs(simTimeMsols - lastTaskStartMsols.doubleValue());
				if (age < MIN_TASK_SESSION_MSOLS) {
					// Within the commitment window; skip repick this tick.
					return;
				}
			}

			// Debounce full re-pick with small jitter
			if (lastRepickMsols != null) {
				double dt = Math.abs(simTimeMsols - lastRepickMsols.doubleValue());
				double jitter = ThreadLocalRandom.current().nextDouble(0D, REPICK_JITTER_MSOLS);
				if (dt < (MIN_REPICK_INTERVAL_MSOLS + jitter)) {
					return;
				}
			}

			startNewTaskFromManager(false);
			lastRepickMsols = simTimeMsols;
		}
	}

	/**
	 * Ask the PersonTaskManager to start a new task and track session timing.
	 * If {@code forced} is true, we allow the switch even when the previous task would prefer to continue.
	 */
	private void startNewTaskFromManager(boolean forced) {
		final Task old = taskManager.getTask();

		// If forced and there's an old task that can't continue, ensure it's ended.
		if (old != null && (!old.canContinue() || forced)) {
			try {
				old.endTask();
			}
			catch (RuntimeException ex) {
				logger.warning(person, 10_000L, "Suppressing exception while ending task "
						+ safeName(old) + ": " + ex.getMessage());
			}
		}

		// Delegate to task manager
		taskManager.startNewTask();

		// Track session timing for the guard
		final Task current = taskManager.getTask();
		if (current != null) {
			lastTaskStartMsols = simTimeMsols;
			lastStartedTaskName = safeName(current);
		}
	}

	private static String safeName(Task t) {
		try {
			return (t != null ? t.getName() : "<none>");
		}
		catch (Throwable ignore) {
			return "<unavailable>";
		}
	}

	/**
	 * Resumes a mission.
	 *
	 * @param modifier mission-resume bias
	 */
	private void resumeMission(int modifier) {
		int fitness = person.getPhysicalCondition().computeFitnessLevel();
		int priority = mission.getPriority();
		int rand = RandomUtil.getRandomInt(5);
		if (rand - (fitness) / 1.5D <= priority + modifier) {
			mission.performMission(person);
		}
	}

	/**
	 * Checks if a person can start a new mission.
	 *
	 * @return {@code true} if they can start a new mission
	 */
	public boolean canStartNewMission() {
		boolean hasMission = hasAMission();
		boolean activeMission = hasActiveMission();

		// Check if mission creation at settlement (if any) is overridden. Guard against null settlement.
		boolean overrideMission = false;
		Settlement s = person.getAssociatedSettlement();
		if (s != null) {
			overrideMission = s.getProcessOverride(OverrideType.MISSION);
		}

		// See if this person can ask for a mission
		return !activeMission && !hasMission && !overrideMission;
	}

	/**
	 * Reassigns the person's job.
	 *
	 * @param newJob           the new job
	 * @param bypassingJobLock whether to bypass the job lock
	 */
	public void reassignJob(JobType newJob, boolean bypassingJobLock, String assignedBy, AssignmentType status,
			String approvedBy) {
		assignJob(newJob, bypassingJobLock, assignedBy, status, approvedBy);
	}

	/**
	 * Assigns a person a new job.
	 *
	 * @param newJob           the new job
	 * @param bypassingJobLock whether to bypass the job lock
	 * @param assignedBy       who assigned the job
	 * @param status           of JobAssignmentType
	 * @param approvedBy       approver
	 */
	public void assignJob(JobType newJob, boolean bypassingJobLock, String assignedBy,
			AssignmentType status, String approvedBy) {
		AssignmentHistory jh = person.getJobHistory();

		// Future: check if the initiator's role allows the job to be changed
		if (newJob != job) {

			if (bypassingJobLock || !jobLock) {
				// Set to the new job
				job = newJob;
				jh.saveJob(newJob, assignedBy, status, approvedBy);

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
		return mission != null;
	}

	/**
	 * Sets this mind as inactive. Needs more work on this; has to abort the Task, cannot just close it.
	 * This abort action would then allow the Mission to be also aborted.
	 */
	public void setInactive() {
		if (taskManager != null) {
			taskManager.clearAllTasks("Inactive");
		}
		if (hasActiveMission()) {
			mission.removeMember(person);
			mission = null;
		}
	}

	/**
	 * Sets this mind active.
	 */
	public void setActive() {
		if (taskManager != null) {
			taskManager.clearAllTasks("Revived");
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
	 */
	public void stopMission() {
		mission = null;
	}

	/**
	 * Determines a new mission for the person.
	 */
	public Mission startNewMission() {
		boolean isPersonToWeak = person.getPerformanceRating() < MINIMUM_MISSION_PERFORMANCE;

		if (!isPersonToWeak) {
			Mission newMission = missionManager.getNewMission(person);
			if (newMission != null) {
				setMission(newMission);
				return newMission;
			}
		}
		return null;
	}

	/**
	 * Gets the emotion manager.
	 *
	 * @return emotion manager
	 */
	public EmotionManager getEmotion() {
		return emotionMgr;
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
	public Relation getRelation() {
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
		if (taskManager != null) {
			taskManager.reinit();
		}
		// Reset session/repick state
		lastTaskStartMsols = null;
		lastRepickMsols = null;
		lastStartedTaskName = null;
		// Reset local sim clock (safe default)
		simTimeMsols = 0D;
	}

	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		// Avoid potential destroy() recursion between Mind and PersonTaskManager:
		// just clear tasks and drop references. The settlement GC will take care of the rest.
		if (taskManager != null) {
			try {
				taskManager.clearAllTasks("Destroyed");
			}
			catch (Throwable ignored) {
				// Defensive: ignore errors during shutdown
			}
		}
		taskManager = null;

		mission = null;
		job = null;

		if (mbti != null) {
			try { mbti.destroy(); } catch (Throwable ignored) {}
		}
		mbti = null;

		if (emotionMgr != null) {
			try { emotionMgr.destroy(); } catch (Throwable ignored) {}
		}
		emotionMgr = null;

		if (trait != null) {
			try { trait.destroy(); } catch (Throwable ignored) {}
		}
		trait = null;

		if (relation != null) {
			try { relation.destroy(); } catch (Throwable ignored) {}
		}
		relation = null;

		person = null;

		// clear transient references
		lastTaskStartMsols = null;
		lastRepickMsols = null;
		lastStartedTaskName = null;
	}
}
