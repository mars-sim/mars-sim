/*
 * Mars Simulation Project
 * AbstractMission.java
 * @date 2025-07-06
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.mission;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.mars_sim.core.Entity;
import com.mars_sim.core.Simulation;
import com.mars_sim.core.Unit;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.data.UnitSet;
import com.mars_sim.core.environment.SurfaceFeatures;
import com.mars_sim.core.events.HistoricalEvent;

import com.mars_sim.core.events.HistoricalEventManager;
import com.mars_sim.core.events.HistoricalEventType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.mission.MissionObjective;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PersonConfig;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.mission.meta.AbstractMetaMission;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.social.RelationshipUtil;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskManager;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.project.Stage;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.robot.ai.job.RobotJob;
import com.mars_sim.core.structure.ObjectiveType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.time.MasterClock;
import com.mars_sim.core.time.Temporal;
import com.mars_sim.core.tool.Conversion;
import com.mars_sim.core.tool.RandomUtil;


/**
 * The AbstractMission class represents a large multi-person task There is at most one
 * instance of a mission per person. A Mission may have one or more people
 * associated with it.
 */
public abstract class AbstractMission implements Mission, Temporal {

	// Plain POJO to help score potential mission members
	private static final class MemberScore {
		Person candidate;
		double score;

		private MemberScore(Person candidate, double personValue) {
			super();
			this.candidate = candidate;
			this.score = personValue;
		}

		public double getScore() {
			return score;
		}

		@Override
		public String toString() {
			return "MemberScore [candidate=" + candidate + ", score=" + score + "]";
		}
	}

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(AbstractMission.class.getName());

	private static final int MAX_CAP = 8;

	private static final MissionPhase COMPLETED_PHASE = new MissionPhase("completed", Stage.CLOSEDOWN);
	private static final MissionPhase ABORTED_PHASE = new MissionPhase("aborted", Stage.CLOSEDOWN);
	protected static final MissionPhase REVIEWING = new MissionPhase("reviewing", Stage.PREPARATION);
	private static final MissionPhase INIT_PHASE = new MissionPhase("initial", Stage.INITIAL);

	protected static final MissionStatus NOT_ENOUGH_MEMBERS = new MissionStatus("Mission.status.noMembers");
	private static final MissionStatus MISSION_NOT_APPROVED = new MissionStatus("Mission.status.notApproved");
	private static final MissionStatus MISSION_ACCOMPLISHED = new MissionStatus("Mission.status.accomplished");
	public static final MissionStatus MISSION_ABORTED_BY_PLAYER = new MissionStatus("Mission.status.abortedByPlayer");
	public static final MissionStatus MISSION_MEDICAL_EMERGENCY = new MissionStatus("Mission.status.medicalEmergency");
	
	private static final String INTERNAL_PROBLEM = "Mission.status.internalProblem";
	
	// Data members
	/** The number of people that can be in the mission. */
	private int missionCapacity;
	/** The mission priority (between 1 and 5, with 1 the lowest, 5 the highest) */
	private int priority = 2;
	/** Unique identifier  */
	protected int identifier;
	
	/** Has the current phase ended? */
	private boolean phaseEnded = false;
	/** True if mission is completed. */
	private boolean done = false;
	private boolean aborted = false;
	
	/** The mission string. */
	private String missionString;
	/** The sol sortie string */
	private String solSortieString;
	/** The mission designation string. */
	private String missionDesignationString = "";
	/** The description of the current phase of operation. */
	private String phaseDescription;
	
	/** The mission type enum. */
	private MissionType missionType;

	/** The current phase of the mission. */
	private MissionPhase phase = INIT_PHASE;
	/** Time the phase started */
	private MarsTime phaseStartTime;
	/** The log of mission activity	 */
	private MissionLog log;
	/** The name of the starting member */
	private Worker startingMember;
	/** The mission plan. */
	private MissionPlanning plan;
	
	/** A list of mission status. */
	private Set<MissionStatus> missionStatus;
	
	/** A set of those who sign up for this mission. After the mission is over, it will still be retained and will not be deleted. */
	private Set<Worker> signUp;

	private List<MissionObjective> objectives = new ArrayList<>();

	/** 
	 * A collection of those who are actually went on the mission.
	 * After the mission is over. All members will be removed 
	 * and the collection will become empty.
	 */
	private Set<Worker> members;
	
	// transient members
	/** Mission listeners. */
	private transient List<MissionListener> listeners;

	// Static members
	protected static UnitManager unitManager;
	private static HistoricalEventManager eventManager;
	protected static MissionManager missionManager;
	protected static SurfaceFeatures surfaceFeatures;
	protected static PersonConfig personConfig;
	private static MasterClock clock;

	/**
	 * Constructor.
	 *
	 * @param missionType
	 * @param startingMember
	 */
	protected AbstractMission(MissionType missionType, Worker startingMember) {
		// Initialize data members

		this.solSortieString = missionManager.computeSolSortieString();
		this.identifier = missionManager.getIdentifier();
		
		this.missionString = missionType.getName() + " " + solSortieString;
		this.missionType = missionType;
		this.startingMember = startingMember;

		missionStatus = new HashSet<>();
		members = new UnitSet<>();
		done = false;
		phaseDescription = "";
		missionCapacity = MAX_CAP;
		
		signUp = new UnitSet<>();
		
		log = new MissionLog();
		
		Person person = (Person) startingMember;

		if (person.isInSettlement()) {

			// Created mission starting event.
			registerHistoricalEvent(person, HistoricalEventType.MISSION_START, "Mission Starting");

			// Log mission starting.
			int n = members.size();
			String appendStr = "";
			if (n == 0)
				appendStr = ".";
			else if (n == 1)
				appendStr = "' with 1 other.";
			else
				appendStr = "' with " + n + " others.";

			logger.log(startingMember, Level.INFO, 0,
					"Began organizing " + missionString + appendStr);

			// Add starting member to mission.
			startingMember.setMission(this);

			// Note: do NOT set his shift to ON_CALL yet.
			// let the mission lead have more sleep before departing
		}
	}
	
	/**
	 * Adds a listener.
	 *
	 * @param newListener the listener to add.
	 */
	@Override
	public final void addMissionListener(MissionListener newListener) {
		if (listeners == null) {
			listeners = new CopyOnWriteArrayList<>();
		}
		synchronized (listeners) {
			if (!listeners.contains(newListener)) {
				listeners.add(newListener);
			}
		}
	}

	/**
	 * Removes a listener.
	 *
	 * @param oldListener the listener to remove.
	 */
	@Override
	public final void removeMissionListener(MissionListener oldListener) {
		if ((listeners != null) && listeners.contains(oldListener)) {
			synchronized (listeners) {
				listeners.remove(oldListener);
			}
		}
	}

	/**
	 * Fires a mission update event.
	 *
	 * @param updateType the update type.
	 */
	protected final void fireMissionUpdate(MissionEventType updateType) {
		fireMissionUpdate(updateType, this);
	}

	/**
	 * Fires a mission update event.
	 *
	 * @param addMemberEvent the update type.
	 * @param target         the event target or null if none.
	 */
	protected final void fireMissionUpdate(MissionEventType addMemberEvent, Object target) {
		if (listeners != null) {
			synchronized (listeners) {
				for (MissionListener l : listeners) {
					l.missionUpdate(new MissionEvent(this, addMemberEvent, target));
				}
			}
		}
	}

	/**
	 * Gets the string representation of this mission.
	 */
	public String toString() {
		return missionString;
	}

	/**
	 * Gets the Martian time instance.
	 * 
	 * @return
	 */
	protected MarsTime getMarsTime() {
		return clock.getMarsTime();
	}
	
	/**
	 * Gets the master clock instance.
	 * 
	 * @return
	 */
	protected MasterClock getMasterClock() {
		return clock;
	}
	
	/**
	 * Adds a member.
	 * 
	 * @param member
	 */
	@Override
	public void addMember(Worker member) {
		if (!members.contains(member)) {
			members.add(member);

			signUp.add(member);
			registerHistoricalEvent(member, HistoricalEventType.MISSION_JOINING,
									"Adding a member");
	
			fireMissionUpdate(MissionEventType.ADD_MEMBER_EVENT, member);

			logger.log(member, Level.FINER, 0, "Just got added to " + missionString + ".");
		}
	}

	/**
	 * Registers this historical mission event about a member.
	 * 
	 * @param member
	 * @param type
	 * @param message
	 */
	protected void registerHistoricalEvent(Worker member, HistoricalEventType type, String message) {
		Unit container = null;
		Coordinates coordinates = null;
		if (member.isInSettlement()) {
			Building workPlace = member.getBuildingLocation();
			if (workPlace != null) {
				container = workPlace;
			}
			else {
				container = member.getAssociatedSettlement();
			}
			coordinates = member.getAssociatedSettlement().getCoordinates();
		} else if (member.isInVehicle()) {
			container = member.getVehicle();
			coordinates = member.getVehicle().getCoordinates();
		} else {
			container = null;
			coordinates = member.getCoordinates();
		}

		// Creating mission joining event.
		HistoricalEvent newEvent = new HistoricalEvent(type, this, message,
														missionString, member.getName(), container,
														getAssociatedSettlement(), coordinates);
		eventManager.registerNewEvent(newEvent);
	}

	/**
	 * A Member leaves the Mission and adjust his work shift.
	 * Note: should use removeMember() to call memberLeave().
	 */
	private final void memberLeave(Worker member) {
		// Added codes in reassigning a work shift
		if (member.getUnitType() == UnitType.PERSON) {
			Person person = (Person) member;
			
			logger.info(person, "Removed from " + member.getMission() + ".");
			
			member.setMission(null);
			person.getTaskManager().recordActivity(getName(), "Leave Mission", "", this);

			
	      	if (RoleType.GUEST != person.getRole().getType()) {      
	      		person.getShiftSlot().setOnCall(false);
	      	}	

			registerHistoricalEvent(person, HistoricalEventType.MISSION_FINISH, "Removing a member");
			fireMissionUpdate(MissionEventType.REMOVE_MEMBER_EVENT, member);
		}
	}
	
	/**
	 * Removes a member from the mission.
	 *
	 * @param member to be removed
	 */
	@Override
	public void removeMember(Worker member) {
		if (members.contains(member)) {
			members.remove(member);

			memberLeave(member);
		}
	}


	/**
	 * Gets a collection of the members in the mission.
	 *
	 * @return collection of members
	 */
	@Override
	public final Set<Worker> getMembers() {
		return members;
	}

	/**
	 * Returns a list of people and robots who have signed up for this mission.
	 * 
	 * @return
	 */
	@Override
	public Set<Worker> getSignup() {
		return signUp;
	}
	
	/**
	 * Adds these members to the mission.
	 * 
	 * @param newMembers Members to add
	 * @param allowRobots Are Robots allowed
	 */
	protected void addMembers(Collection<Worker> newMembers, boolean allowRobots) {
		for(Worker member : newMembers) {
			if (member.getUnitType() == UnitType.PERSON) {
				((Person) member).getMind().setMission(this);
			}
			else {
				if (!allowRobots) {
					throw new IllegalStateException("Mission does not supprot robots");
				}
				((Robot) member).getBotMind().setMission(this);
			}
		}
	}

	/**
	 * Determines if mission is completed.
	 *
	 * @return true if mission is completed
	 */
	@Override
	public final boolean isDone() {
		return done;
	}

	/**
	 * Gets the name of the mission.
	 *
	 * @return name of mission
	 */
	@Override
	public final String getName() {
		return missionString;
	}

	/**
	 * Updates the mission name.
	 * 
	 * @param newName
	 */
	@Override
    public void setName(String newName) {
		this.missionString = newName;
    }

	/**
	 * Context of a mission is the Settlement of the starting member
	 */
	@Override
	public String getContext() {
		return startingMember.getAssociatedSettlement().getName();
	}

	/**
	 * Gets the mission type enum.
	 *
	 * @return
	 */
	@Override
	public MissionType getMissionType() {
		return missionType;
	}

	/**
	 * Gets the Stage.
	 */
	@Override
	public Stage getStage() {
		if (done)
			return Stage.DONE;
		else
			// If no phase that Mission is building built so stage is PREP
			return phase != null ? phase.getStage() : Stage.PREPARATION;
	}

	/**
	 * Gets the current phase of the mission.
	 *
	 * @return phase
	 */
	public MissionPhase getPhase() {
		return phase;
	}

	/**
	 * Sets the mission phase and the current description.
	 *
	 * @param newPhase the new mission phase.
	 * @param subjectOfPhase This is the subject of the phase
	 * @throws MissionException if newPhase is not in the mission's collection of
	 *                          phases.
	 */
	protected void setPhase(MissionPhase newPhase, String subjectOfPhase) {
		if (newPhase == null) {
			throw new IllegalArgumentException("newPhase is null");
		}

		// Move phase on
 		phase = newPhase;
		setPhaseEnded(false);
		phaseStartTime = clock.getMarsTime();

		String template = newPhase.getDescriptionTemplate();
		if (template != null) {
			phaseDescription = MessageFormat.format(template, subjectOfPhase);
		}
		else {
			phaseDescription = "";
		}

		// Add entry to the log
		addMissionLog(newPhase.getName(), getStartingPerson().getName());

		fireMissionUpdate(MissionEventType.PHASE_EVENT, newPhase);
	}

	/**
	 * Adds a mission log.
	 * 
	 * @param entry
	 */
	protected void addMissionLog(String entry) {
		addMissionLog(entry, "");
	}

	/**
	 * Adds a mission log.
	 * 
	 * @param entry
	 * @param enterBy the name of the person who logs this
	 */
	protected void addMissionLog(String entry, String enterBy) {
		log.addEntry(entry, enterBy);
	}
	
	/**
	 * Gets the mission log.
	 */
	@Override
	public MissionLog getLog() {
		return log;
	}

	/**
	 * Gets the start time in the current phase.
	 */
	@Override
	public MarsTime getPhaseStartTime() {
		return phaseStartTime;
	}

	/**
	 * Gets time elapsed [in millisols] in the current phase.
	 */
	protected double getPhaseTimeElapse() {
		return clock.getMarsTime().getTimeDiff(phaseStartTime);
	}

	/**
	 * Gets the description of the current phase.
	 *
	 * @return phase description.
	 */
	@Override
	public final String getPhaseDescription() {
		if (phaseDescription != null && !phaseDescription.equals("")) {
			return phaseDescription;
		} else if (phase != null) {
			return phase.toString();
		} else
			return "";
	}

	/**
	 * Sets the description of the current phase.
	 *
	 * @param description the phase description.
	 */
	protected final void setPhaseDescription(String description) {
		phaseDescription = description;
		fireMissionUpdate(MissionEventType.PHASE_DESCRIPTION_EVENT, description);
	}

	/**
	 * Performs the mission.
	 *
	 * @param member the member performing the mission.
	 * @return Can the work participate
	 */
	@Override
	public boolean performMission(Worker member) {
		if (!canParticipate(member)) {
			return false;
		}

		// If current phase is over, decide what to do next.
		if (phaseEnded && !determineNewPhase()) {
			logger.warning(member, 60_000L, "New phase for " + getName()
					+ " cannot be determined for " + phase.getName() + ".");
		}

		// Perform phase.
		if (!done) {
			performPhase(member);
		}
		return true;
	}

	/**
	 * Determines a new phase for the mission when the current phase has ended.
	 *
	 * @return Has the new phase been identified
	 * @throws MissionException if problem setting a new phase.
	 */
	protected abstract boolean determineNewPhase();

	/**
	 * The member performs the current phase of the mission.
	 *
	 * @param member the member performing the phase.
	 */
	protected void performPhase(Worker member) {
		if (phase == null) {
			endMissionProblem(member, "Current phase null");
		}

		else if (REVIEWING.equals(getPhase())) {
			requestReviewPhase(member);
		}
	}

	/**
	 * Gets the mission capacity for participating people.
	 *
	 * @return mission capacity
	 */
	@Override
	public final int getMissionCapacity() {
		return missionCapacity;
	}

	/**
	 * Sets the mission capacity to a given value.
	 *
	 * @param newCapacity the new mission capacity
	 */
	protected final void setMissionCapacity(int newCapacity) {
		missionCapacity = newCapacity;
		fireMissionUpdate(MissionEventType.CAPACITY_EVENT, newCapacity);
	}

	/** 
	 * Aborts the current phase; nothing on the base class.
	 */
	public void abortPhase() {
		// Do nothing
	}

	/**
	 * Aborts the mission by the user. Will stop current phase.
	 * 
	 * @param endStatus Cause for abort
	 */
	@Override
	public final void abortMission(MissionStatus endStatus) {
		aborted = true;
		logger.info(getStartingPerson(), "Aborted " + getName() 
			+ ": " + endStatus.getName() + ".");
		
		endMission(endStatus);
	}

	/**
	 * Computes the mission experience score.
	 *
	 * @param reason
	 */
	private void addMissionScore() {
		for (Worker member : members) {
			if (member.getUnitType() == UnitType.PERSON) {
				Person person = (Person) member;

				if (!person.isDeclaredDead()) {
					if (person.getPhysicalCondition().hasSeriousMedicalProblems()) {
						// Note : there is a minor penalty for those who are sick
						// and thus unable to fully function during the mission
						person.addMissionExperience(missionType, 2);
					}
					else if (person.equals(startingMember)) {
						// The mission lead receive extra bonus
						person.addMissionExperience(missionType, 6);
						// Add a leadership point to the mission lead
						person.getNaturalAttributeManager().adjustAttribute(NaturalAttributeType.LEADERSHIP, 1);
					}
					else
						person.addMissionExperience(missionType, 3);
				}
			}
		}
	}


	/**
	 * An internal problem has happened to end the mission.
	 * 
	 * @param source
	 * @param reason
	 */
	protected void endMissionProblem(Entity source, String reason) {
		MissionStatus status = new MissionStatus(INTERNAL_PROBLEM, reason);
		logger.severe(this, "Ended with " + status.getName() + "; source was " + source.getName());
		endMission(status);
	}
	
	/**
	 * Finalizes the mission. Reason for ending mission. Mission can
	 * override this to perform necessary finalizing operations.
	 *
	 * @param endStatus A status to add for the end of Mission
	 */
	protected void endMission(MissionStatus endStatus) {
		if (done) {
			logger.warning(startingMember, "Mission " + getName() + " is already ended.");
			return;
		}

		// Ended with a status
		if (endStatus != null) {
			missionStatus.add(endStatus);
		}

		// If no mission flags have been added then it was accomplished
		String listOfStatuses = missionStatus.stream().map(MissionStatus::getName).collect(Collectors.joining(", "));
		MissionPhase finalPhase = ABORTED_PHASE;
		
//		if (missionStatus.isEmpty() && !aborted) {
		if (!aborted) {
			missionStatus.add(MISSION_ACCOMPLISHED);
			addMissionScore();
			finalPhase = COMPLETED_PHASE;
		}
		
		else if (endStatus == NOT_ENOUGH_MEMBERS) {
			finalPhase = ABORTED_PHASE;
			aborted = true;
		}
		
		setPhase(finalPhase, listOfStatuses);
		log.setDone();
		done = true; 
		
		StringBuilder status = new StringBuilder();
		
		if (listOfStatuses.isBlank() || listOfStatuses == null) {
			status.append("Ended the ")
			.append(getName())
			.append(" without status flags.");
		}
		else {
			status.append("Ended the ")
			.append(getName())
			.append(" with status flag(s): ")
			.append(listOfStatuses).append(".");
		}

		logger.info(startingMember, status.toString());

		// Disband the members
		if (members != null && !members.isEmpty()) {
			String listOfMembers = members.stream().map(Worker::getName).collect(Collectors.joining(", "));
			logger.info(startingMember, "Disbanding mission member(s): " + listOfMembers);
			
			// Take a copy as Worker will deregister themselves
			List<Worker> oldMembers = new ArrayList<>(members);
			for(Worker member : oldMembers) {
				removeMember(member);
			}	
			members.clear();
		}
	}
	
	/**
	 * Checks if a worker has any issues in starting a new task.
	 *
	 * @param worker the person to assign to the task
	 * @param newTask   the new task to be assigned
	 * @return true if task can be performed.
	 */
	public boolean assignTask(Worker worker, Task newTask) {
		return assignTask(worker, newTask, false);
	}
	
	/**
	 * Checks if a person has any issues in starting a new task.
	 *
	 * @param person the person to assign to the task
	 * @param newTask   the new task to be assigned
	 * @return true if task can be performed.
	 */
	public boolean assignTask(Person person, Task newTask) {
		return assignTask(person, newTask, false);
	}
	
	/**
	 * Adds a new task for a robot in the mission. Task may be not assigned if the
	 * robot has a malfunction.
	 *
	 * @param robot the robot to assign to the task
	 * @param newTask  the new task to be assigned
	 * @return true if task can be performed.
	 */
	public boolean assignTask(Robot robot, Task newTask) {
		return assignTask(robot, newTask, false);
	}
	
	/**
	 * Checks if a worker has any issues in starting a new task.
	 *
	 * @param worker the worker to assign to the task
	 * @param newTask   the new task to be assigned
	 * @param allowSameTask is it allowed to execute the same task as previous
	 * @return true if task can be performed.
	 */
	public boolean assignTask(Worker worker, Task newTask, boolean allowSameTask) {
		if (worker instanceof Person person) {
			return assignTask(person, newTask, allowSameTask);
		}
		else if (worker instanceof Robot robot)  {
			return assignTask(robot, newTask, allowSameTask);
		}
		
		return false;
	}
	
	/**
	 * Checks if a person has any issues in starting a new task.
	 *
	 * @param person the person to assign to the task
	 * @param newTask   the new task to be assigned
	 * @param allowSameTask is it allowed to execute the same task as previous
	 * @return true if task can be performed.
	 */
	public boolean assignTask(Person person, Task newTask, boolean allowSameTask) {
		return TaskManager.assignTask(person, newTask, allowSameTask);
	}
	
	/**
	 * Adds a new task for a robot in the mission. Task may be not assigned if the
	 * robot has a malfunction.
	 *
	 * @param robot the robot to assign to the task
	 * @param newTask  the new task to be assigned
		 * @param allowSameTask is it allowed to execute the same task as previous
	 * @return true if task can be performed.
	 */
	public boolean assignTask(Robot robot, Task newTask, boolean allowSameTask) {
		return TaskManager.assignTask(robot, newTask, allowSameTask);
	}
	
	/**
	 * Checks to see if any of the people in the mission have any dangerous medical
	 * problems that require treatment at a settlement. Also any environmental
	 * problems, such as suffocation.
	 *
	 * @return true if dangerous medical problems
	 */
	private final boolean hasDangerousMedicalProblems() {
		Person patient = null;
		for (Worker member : members) {
			if ((member.getUnitType() == UnitType.PERSON) 
					&& ((Person) member).getPhysicalCondition().hasSeriousMedicalProblems()) {
				patient = (Person) member;
			}
		}

		if (patient != null) {
			
			if (this instanceof AbstractVehicleMission avm) {
				// Generate historical event by calling AbstractVehicleMission's abortMission
				avm.abortMission(MISSION_MEDICAL_EMERGENCY, HistoricalEventType.MISSION_MEDICAL_EMERGENCY);
			}
			else {
				// Abort the mission and return home
				abortMission(MISSION_MEDICAL_EMERGENCY);
			}
			
			addMissionLog(MISSION_MEDICAL_EMERGENCY.getName(), patient.getName());
		}
		return patient != null;
	}

	/**
	 * Checks to see if any of the people in the mission have any potential medical
	 * problems due to low fitness level that will soon degrade into illness.
	 *
	 * @return true if potential medical problems exist
	 */
	protected final boolean hasAnyPotentialMedicalProblems() {
		for (Worker member : members) {
			if ((member.getUnitType() == UnitType.PERSON) 
				&& ((Person) member).getPhysicalCondition().computeFitnessLevel() < 2) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks to see if all of the people in the mission have any dangerous medical
	 * problems that require treatment at a settlement. Also any environmental
	 * problems, such as suffocation.
	 *
	 * @return true if all have dangerous medical problems
	 */
	public final boolean hasDangerousMedicalProblemsAllCrew() {
		boolean result = true;
		for (Worker member : members) {
			if ((member.getUnitType() == UnitType.PERSON) 
				&& !((Person) member).getPhysicalCondition().hasSeriousMedicalProblems()) {
				result = false;
			}
		}
		return result;
	}

	/**
	 * Checks if the mission has an emergency situation.
	 *
	 * @return true if emergency.
	 */
	protected boolean hasEmergency() {
		return hasDangerousMedicalProblems();
	}

	/**
	 * Checks if the mission has an emergency situation affecting all the crew.
	 *
	 * @return true if emergency affecting all.
	 */
	public boolean hasEmergencyAllCrew() {
		return hasDangerousMedicalProblemsAllCrew();
	}

	/**
	 * Recruits new members into the mission.
	 *
	 * @param startingMember the mission member starting the mission.
	 * @param sameSettlement do members have to be at the same Settlement as the starting Member
	 * @param minMembers Minimum number of members required
	 */
	protected boolean recruitMembersForMission(Worker startingMember, boolean sameSettlement, int minMembers) {

		// Get all people qualified for the mission.
		Collection<Person> possibles;
		if (sameSettlement) {
			possibles = startingMember.getAssociatedSettlement().getAllAssociatedPeople();
		}
		else {
			possibles = unitManager.getPeople();
		}

		List<MemberScore> qualifiedPeople = new ArrayList<>();
		for (Person person : possibles) {
			if (isCapableOfMission(person)) {
				// Determine the person's mission qualification.
				double qualification = getMissionQualification(person) * 100D;

				// Determine how much the recruiter likes the person.
				double likability = 50D;
				if (startingMember.getUnitType() == UnitType.PERSON) {
					likability = RelationshipUtil.getOpinionOfPerson((Person) startingMember, person);
				}

				// Check if person is the best recruit.
				double personValue = (qualification + likability) / 2D;
				qualifiedPeople.add(new MemberScore(person, personValue));
			}
		}

		int pop = startingMember.getAssociatedSettlement().getNumCitizens();
		int max;
		if (pop < 4)
			max = 1;
		else if (pop < 7)
			max = 2;
		else if (pop < 10)
			max = 3;
		else if (pop < 14)
			max = 4;
		else if (pop < 18)
			max = 5;
		else if (pop < 23)
			max = 6;
		else if (pop < 29)
			max = 7;
		else
			max = 8;

		// 50% tendency to have 1 less person
		int rand = RandomUtil.getRandomInt(1);
		if ((rand == 1) && (max >= 5)) {
			max--;
		}

		// Max can not bigger than mission capacity
		max = Math.min(max, missionCapacity);

		// Recruit the most qualified and most liked people first.
		qualifiedPeople.sort(Comparator.comparing(MemberScore::getScore, Comparator.reverseOrder()));
		while (!qualifiedPeople.isEmpty() && (members.size() < max)) {

			// Try to recruit best person available to the mission.
			MemberScore next = qualifiedPeople.remove(0);
			recruitPerson(startingMember, next.candidate);
		}

		List<Person> tourists = startingMember.getAssociatedSettlement().getTouristList();
		
		// Add a tourist to this mission
		// It's preferable for missions with more than 3 members to add a tourist
		if (!tourists.isEmpty() &&  minMembers > 3 && members.size() < minMembers) {
			tourists.get(0).setMission(this);			
		}
		
		if (members.size() < minMembers) {
			endMission(NOT_ENOUGH_MEMBERS);
			return false;
		}

		return true;
	}

	/**
	 * Attempts to recruit a new person into the mission.
	 *
	 * @param recruiter the mission member doing the recruiting.
	 * @param recruitee the person being recruited.
	 */
	private void recruitPerson(Worker recruiter, Person recruitee) {
		if (isCapableOfMission(recruitee)) {
			// Get mission qualification modifier.
			double qualification = getMissionQualification(recruitee) * 100D;
			// Get the recruitee's social opinion of the recruiter.
			double recruiterLikability = 50D;
			if (recruiter.getUnitType() == UnitType.PERSON) {
				recruiterLikability = RelationshipUtil.getOpinionOfPerson(recruitee, (Person) recruiter);
			}

			// Get the recruitee's average opinion of all the current mission members.
			List<Person> people = new ArrayList<>();
			Iterator<Worker> i = members.iterator();
			while (i.hasNext()) {
				Worker member = i.next();
				if (member.getUnitType() == UnitType.PERSON) {
					people.add((Person) member);
				}
			}
			double groupLikability = RelationshipUtil.getAverageOpinionOfPeople(recruitee, people);

			double recruitmentChance = (qualification + recruiterLikability + groupLikability) / 3D;
			if (recruitmentChance > 100D) {
				recruitmentChance = 100D;
			} else if (recruitmentChance < 0D) {
				recruitmentChance = 0D;
			}

			if (RandomUtil.lessThanRandPercent(recruitmentChance)) {
				recruitee.setMission(this);

				// NOTE: do not set his shift to ON_CALL until after the mission plan has been approved
			}
		}
	}

	/**
	 * Checks to see if a member is capable of joining a mission.
	 *
	 * @param member the member to check.
	 * @return true if member could join mission.
	 */
	protected boolean isCapableOfMission(Worker member) {
		boolean result = false;

		if (member == null) {
			throw new IllegalArgumentException("member is null");
		}

		if (member.getUnitType() == UnitType.PERSON) {
			Person person = (Person) member;

			// Make sure person isn't already on a mission.
			boolean onMission = (person.getMind().getMission() != null);

			// Make sure person doesn't have any serious health problems.
			boolean healthProblem = person.getPhysicalCondition().hasSeriousMedicalProblems();

			// Check if person is qualified to join the mission.
			boolean isQualified = (getMissionQualification(person) > 0D);

			if (!onMission && !healthProblem && isQualified) {
				result = true;
			}
		}

		return result;
	}

	/**
	 * Gets the mission qualification value for the member. Member is qualified in
	 * joining the mission if the value is larger than 0. The larger the
	 * qualification value, the more likely the member will be picked for the
	 * mission.
	 *
	 * @param member the member to check.
	 * @return mission qualification value.
	 */
	@Override
	public double getMissionQualification(Worker member) {

		double result = 0D;

		if (member.getUnitType() == UnitType.PERSON) {
			Person person = (Person) member;
			result = Math.max(5,  person.getMissionExperience(missionType));

			// Get base result for job modifier.
			Set<JobType> prefered = getPreferredPersonJobs();
			JobType job = person.getMind().getJobType();
			double jobModifier;
			if ((prefered != null) && prefered.contains(job)) {
				jobModifier = 1D;
			}
			else {
				jobModifier = 0.5D;
			}

			result = result + 2 * result * jobModifier;
		}
		else {
			Robot robot = (Robot) member;

			// Get base result for job modifier.
			RobotJob job = robot.getBotMind().getRobotJob();
			if (job != null) {
				result = job.getJoinMissionProbabilityModifier(this.getClass());
			}
		}

		return result;
	}

	/**
	 * Gets the preferred Job types.
	 * 
	 * @return
	 */
	protected Set<JobType> getPreferredPersonJobs() {
		return Collections.emptySet();
	}

	@Override
	public Set<ObjectiveType> getObjectiveSatisified() {
		return Collections.emptySet();
	}
	/**
	 * Checks if the current phase has ended or not.
	 *
	 * @return true if phase has ended
	 */
	public final boolean getPhaseEnded() {
		return phaseEnded;
	}

	/**
	 * Sets if the current phase has ended or not.
	 *
	 * @param phaseEnded true if phase has ended
	 */
	protected final void setPhaseEnded(boolean phaseEnded) {
		this.phaseEnded = phaseEnded;
	}

	/**
	 * Gets the number and amounts of resources needed for the mission.
	 *
	 * @param useBuffer use time buffers in estimation if true.
	 * @return map of amount and item resources and their Double amount or Integer
	 *         number.
	 */
	protected abstract Map<Integer, Number> getResourcesNeededForRemainingMission(boolean useBuffer);

	/**
	 * Gets the number and types of equipment needed for the mission.
	 *
	 * @param useBuffer use time buffers in estimation if true.
	 * @return map of equipment types and number.
	 */
	protected Map<Integer, Integer> getEquipmentNeededForRemainingMission(boolean useBuffer) {
		return new HashMap<>();
	}

	/**
	 * Time passing for mission.
	 *
	 * @param time the amount of time passing (in millisols)
	 * @throws Exception if error during time passing.
	 */
	public boolean timePassing(ClockPulse pulse) {
		return true;
	}


	/**
	 * Gets the current location of the mission.
	 *
	 * @return coordinate location.
	 * @throws MissionException if error determining location.
	 */
	public Coordinates getCurrentMissionLocation() {

		Coordinates result = null;

		if (startingMember != null)	{
			Person p = (Person)startingMember;
			Settlement s = p.getSettlement();
			if (s != null)
				return s.getCoordinates();
			if (p.isInVehicle())
				return p.getVehicle().getCoordinates();
			else
				return p.getCoordinates();

		}
		else {
			logger.severe(this, "No starting member");
		}

		return result;
	}

	/**
	 * Requests review for the mission.
	 *
	 * @param member the mission lead.
	 */
	private void requestReviewPhase(Worker member) {
		if (plan == null) {
			throw new IllegalStateException("No Mission plan");
		}

		switch(plan.getStatus()) {
			case NOT_APPROVED:
				endMission(MISSION_NOT_APPROVED);
				break;
			
			case APPROVED:
				createDesignationString();

				logger.info(this, "Mission plan approved.");

				if (!(this instanceof VehicleMission)) {
					// Set the members' work shift to on-call to get ready
					for (Worker m : members) {
						((Person) m).getShiftSlot().setOnCall(true);
					}
				}
				setPhaseEnded(true);
				break;
			default:
				// Nothing to do yet
		}
	}

	/**
	 * Starts reviewing this mission.
	 */
	protected void startReview() {
		setPhase(REVIEWING, null);
		plan = new MissionPlanning(this, getMarsTime().getMissionSol());
	}
	/**
	 * Returns the mission plan.
	 *
	 * @return {@link MissionPlanning}
	 */
	@Override
	public MissionPlanning getPlan() {
		return plan;
	}

	/**
	 * Returns the starting person.
	 *
	 * @return {@link Person}
	 */
	@Override
	public Person getStartingPerson() {
		if (startingMember instanceof Person person)
			return person;
		else
			return null;
	}

	/**
	 * Sets the starting member.
	 *
	 * @param member the new starting member
	 */
	protected final void setStartingMember(Worker member) {
		this.startingMember = member;
		fireMissionUpdate(MissionEventType.STARTING_SETTLEMENT_EVENT);
	}

	/**
	 * Gets the mission designation string. Only defined once mission has started.
	 * 
	 * @return
	 */
	@Override
	public String getFullMissionDesignation() {
		return missionDesignationString;
	}

	/**
	 * Creates the mission designation string for this mission.
	 */
	protected void createDesignationString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append(Conversion.getOneLetterInitial(missionType.getName().replace("with", "").trim()))
			  .append("-")
			  .append(solSortieString)
			  .append("-")
			  .append(getAssociatedSettlement().getSettlementCode())
			  .append('-')
			  .append(identifier);
		
		missionDesignationString = buffer.toString();

		fireMissionUpdate(MissionEventType.DESIGNATION_EVENT, missionDesignationString);
	}

	@Override
	public Set<MissionStatus> getMissionStatus() {
		return missionStatus;
	}

	/**
	 * Adds a new mission status.
	 *
	 * @param status
	 */
	protected boolean addMissionStatus(MissionStatus status) {
		boolean newStatus = missionStatus.add(status);
		if (newStatus) {
			addMissionLog(status.getName(), getStartingPerson().getName());
		}
		return newStatus;
	}

	@Override
	public int getPriority() {
		return priority;
	}

	protected void setPriority(int newPriority) {
		priority = newPriority;
	}
	
	/**
	 * Checks if this worker can participate.
	 * 
	 * @param worker This maybe used by overriding methods
	 * @return
	 */
	protected boolean canParticipate(Worker worker) {
		return true;
	}
	
	protected void addObjective(MissionObjective objective) {
		objectives.add(objective);
	}

	/**
	 * Sets the mission objectives.
	 *
	 * @param objectives the list of objectives
	 */
	protected void setObjectives(List<MissionObjective> objectives) {
		this.objectives = objectives;
	}

	/**
	 * Gets the mission objectives.
	 *
	 * @return list of {@link MissionObjective}
	 */
	@Override
    public List<MissionObjective> getObjectives() {
        return objectives;
    }

	/**
	 * Compares if this object equals this instance of mission.
	 */
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (this.getClass() != obj.getClass()) return false;
		AbstractMission m = (AbstractMission) obj;
		return this.solSortieString == m.solSortieString;
	}

	/**
	 * Gets the hash code for this object.
	 *
	 * @return hash code.
	 */
	public int hashCode() {
		return (1 + identifier) % 64; 
	}

	/**
	 * Reloads instances after loading from a saved sim.
	 *
	 * @param si {@link Simulation}
	 * @param c {@link MarsClock}
	 * @param e {@link HistoricalEventManager}
	 * @param u {@link UnitManager}
	 * @param sf {@link SurfaceFeatures}
	 * @param m {@link MissionManager}
	 */
	public static void initializeInstances(Simulation si, HistoricalEventManager e,
			UnitManager u, SurfaceFeatures sf, 
			MissionManager m, PersonConfig pc) {
		eventManager = e;
		unitManager = u;
		surfaceFeatures = sf;
		missionManager = m;
		personConfig = pc;

		clock = si.getMasterClock();

		MissionLog.initialise(clock);
		MissionUtil.initializeInstances(u, m);
		AbstractMetaMission.initializeInstances(clock);
	}
}
