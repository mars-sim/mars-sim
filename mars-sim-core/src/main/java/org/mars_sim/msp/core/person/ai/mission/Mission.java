/*
 * Mars Simulation Project
 * Mission.java
 * @date 2022-08-10
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.io.Serializable;
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

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.data.UnitSet;
import org.mars_sim.msp.core.environment.SurfaceFeatures;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.events.HistoricalEventManager;
import org.mars_sim.msp.core.logging.Loggable;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.ShiftType;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.social.RelationshipUtil;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.Worker;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.ai.job.RobotJob;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.Temporal;
import org.mars_sim.msp.core.tool.Conversion;
import org.mars_sim.msp.core.tool.RandomUtil;


/**
 * The Mission class represents a large multi-person task There is at most one
 * instance of a mission per person. A Mission may have one or more people
 * associated with it.
 */
public abstract class Mission implements Serializable, Temporal {

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
	private static final SimLogger logger = SimLogger.getLogger(Mission.class.getName());

	private static final String OUTSIDE = "Outside";

	private static final int MAX_CAP = 8;

	// Global mission identifier
	private static int missionIdentifer = 1;

	/**
	 * The marginal factor for the amount of water to be brought during a mission.
	 */
	public static final double WATER_MARGIN = 1.00;
	/**
	 * The marginal factor for the amount of oxygen to be brought during a mission.
	 */
	public static final double OXYGEN_MARGIN = 1.5;
	/**
	 * The marginal factor for the amount of food to be brought during a mission.
	 */
	public static final double FOOD_MARGIN = 2.25;
	/**
	 * The marginal factor for the amount of dessert to be brought during a mission.
	 */
	public static final double DESSERT_MARGIN = 1.25;

	protected static final MissionStatus NOT_ENOUGH_MEMBERS = new MissionStatus("Mission.status.noMembers");
	private static final MissionStatus MISSION_NOT_APPROVED = new MissionStatus("Mission.status.notApproved");
	private static final MissionStatus MISSION_ACCOMPLISHED = new MissionStatus("Mission.status.accomplished");
	private static final String INTERNAL_PROBLEM = "Mission.status.internalProblem";


	// Data members
	/** The number of people that can be in the mission. */
	private int missionCapacity;
	/** The mission priority (between 1 and 5, with 1 the lowest, 5 the highest) */
	private int priority = 2;
	/** The recorded number of team members. */
	private int recordMembersNum;
	
	/** Has the current phase ended? */
	private boolean phaseEnded;
	/** True if mission is completed. */
	private boolean done = false;

	/** The name of the vehicle reserved. */
	private String vehicleReserved;
	/** The Name of this mission. */
	private String missionName;
	/** The description of this mission. */
	private String description;
	/** The full mission designation. */
	private String fullMissionDesignation = "";
	/** A record of participating members. */
	private String memberRecord = "";
	
	/** The mission type enum. */
	private MissionType missionType;

	/** A list of mission status. */
	private Set<MissionStatus> missionStatus;
	/** The current phase of the mission. */
	private MissionPhase phase;
	/** The description of the current phase of operation. */
	private String phaseDescription;
	/** Time the phase started */
	private MarsClock phaseStartTime;
	/** Log of mission activity	 */
	private List<MissionLogEntry> log = new ArrayList<>();

	/** The name of the starting member */
	private Worker startingMember;
	/** The mission plan. */
	private MissionPlanning plan;

	/** 
	 * A set of those who sign up for this mission. 
	 * After the mission is over. It will be retained and will not be deleted.
	 */
	private Set<Worker> signUp;

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
	protected static Simulation sim = Simulation.instance();

	protected static UnitManager unitManager;
	protected static HistoricalEventManager eventManager;
	protected static MissionManager missionManager;
	protected static SurfaceFeatures surfaceFeatures;
	protected static PersonConfig personConfig;
	protected static MarsClock marsClock;

	/**
	 * Must be synchronised to prevent duplicate ids being assigned via different
	 * threads.
	 *
	 * @return
	 */
	private static synchronized int getNextIdentifier() {
		return missionIdentifer++;
	}

	/**
	 * Constructor.
	 *
	 * @param missionType
	 * @param startingMember
	 */
	protected Mission(MissionType missionType, Worker startingMember) {
		// Initialize data members
		this.missionName = missionType.getName() + " #" + getNextIdentifier();
		this.missionType = missionType;
		this.startingMember = startingMember;
		this.description = missionName;

		missionStatus = new HashSet<>();
		members = new UnitSet<>();
		done = false;
		phase = null;
		phaseDescription = "";
		phaseEnded = false;
		missionCapacity = MAX_CAP;
		
		signUp = new UnitSet<>();

		Person person = (Person) startingMember;

		if (person.isInSettlement()) {

			// Created mission starting event.
			registerHistoricalEvent(person, EventType.MISSION_START, "Mission Starting");

			// Log mission starting.
			int n = members.size();
			String appendStr = "";
			if (n == 0)
				appendStr = ".";
			else if (n == 1)
				appendStr = "' with 1 other.";
			else
				appendStr = "' with " + n + " others.";

			String article = "a ";

			String missionStr = missionName;

			if (!missionStr.toLowerCase().contains("mission"))
				missionStr = missionName + " mission";

			if(Conversion.isVowel(missionName))
				article = "an ";

			logger.log(startingMember, Level.INFO, 0,
					"Began organizing " + article + missionStr + appendStr);

			// Add starting member to mission.
			startingMember.setMission(this);

			// Note: do NOT set his shift to ON_CALL yet.
			// let the mission lead have more sleep before departing
		}

	}

	/**
	 * Gets the date filed timestamp of the mission.
	 *
	 * @return
	 */
	public String getDateFiled() {
		if (!log.isEmpty()) {
			return log.get(0).getTime();
		}
		return "";
	}

	/**
	 * Gets the date embarked timestamp of the mission.
	 *
	 * @return
	 */
	public String getDateEmbarked() {
		// Will be overridden by sub-class
		return "";
	}

	/**
	 * Gets the date returned timestamp of the mission.
	 *
	 * @return
	 */
	public String getDateReturned() {
		if (isDone() && !log.isEmpty()) {
			return log.get(log.size()-1).getTime();
		}

		return "";
	}

	/**
	 * Adds a listener.
	 *
	 * @param newListener the listener to add.
	 */
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
		return missionName;
	}

	/**
	 * Adds a member.
	 * 
	 * @param member
	 */
	public final void addMember(Worker member) {
		if (!members.contains(member)) {
			members.add(member);
			
			recordMembersNum = members.size();
			
			if (member.getUnitType() == UnitType.PERSON) {
				signUp.add(member);
				registerHistoricalEvent((Person) member, EventType.MISSION_JOINING,
									    "Adding a member");
			}

			fireMissionUpdate(MissionEventType.ADD_MEMBER_EVENT, member);

			logger.log(member, Level.FINER, 0, "Just got added to " + missionName + ".");
		}
	}

	/**
	 * Registers this historical event.
	 * 
	 * @param person
	 * @param type
	 * @param message
	 */
	private void registerHistoricalEvent(Person person, EventType type, String message) {
		String loc0 = null;
		String loc1 = null;
		if (person.isInSettlement()) {
			loc0 = person.getBuildingLocation().getNickName();
			loc1 = person.getSettlement().getName();
		} else if (person.isInVehicle()) {
			loc0 = person.getVehicle().getName();

			if (person.getVehicle().getBuildingLocation() != null)
				loc1 = person.getVehicle().getSettlement().getName();
			else
				loc1 = person.getCoordinates().toString();
		} else {
			loc0 = OUTSIDE;
			loc1 = person.getCoordinates().toString();
		}

		// Creating mission joining event.
		HistoricalEvent newEvent = new MissionHistoricalEvent(type, this,
				message, missionName, person.getName(), loc0, loc1, person.getAssociatedSettlement().getName());
		eventManager.registerNewEvent(newEvent);
	}

	public final void adjustShift(Worker member) {
		// Added codes in reassigning a work shift
		if (member.getUnitType() == UnitType.PERSON) {
			Person person = (Person) member;
			person.getMind().stopMission();
			member.setMission(null);

			ShiftType shift = null;
			if (person.getSettlement() != null) {
				shift = person.getSettlement().getAnEmptyWorkShift(-1);
				person.setShiftType(shift);
			}
			else if ((person.getVehicle() != null) && (person.getVehicle().getSettlement() != null)) {
					shift = person.getVehicle().getSettlement().getAnEmptyWorkShift(-1);
					person.setShiftType(shift);
			}

			registerHistoricalEvent(person, EventType.MISSION_FINISH, "Removing a member");
			fireMissionUpdate(MissionEventType.REMOVE_MEMBER_EVENT, member);

			if (getPeopleNumber() == 0 && !done) {
				endMission(NOT_ENOUGH_MEMBERS);
			}
		}
	}
	
	/**
	 * Removes a member from the mission.
	 *
	 * @param member to be removed
	 */
	public final void removeMember(Worker member) {
		if (members.contains(member)) {
			members.remove(member);
			// Adjust the work shift
			adjustShift(member);
		}
	}

	/**
	 * Determines if a mission includes the given member.
	 *
	 * @param member member to be checked
	 * @return true if member is a part of the mission.
	 */
	public final boolean hasMember(Worker member) {
		return members.contains(member);
	}

	/**
	 * Gets the number of members in the mission.
	 *
	 * @return number of members.
	 */
	public final int getMembersNumber() {
		return members.size();
	}
	
	/**
	 * Gets the recorded number of members in the mission.
	 *
	 * @return number of members.
	 */
	public final int getRecordMembersNum() {
		return recordMembersNum;
	}
	
	/**
	 * Gets the number of people in the mission.
	 *
	 * @return number of people
	 */
	public final int getPeopleNumber() {
		return getMembersNumber();
	}

	/**
	 * Gets a collection of the members in the mission.
	 *
	 * @return collection of members
	 */
	public final Collection<Worker> getMembers() {
		return Collections.unmodifiableCollection(members);
	}

	/**
	 * Gets a set of the members in the mission.
	 *
	 * @return collection of members
	 */
	public Set<Worker> getMemberList() {
		return members;
	}
	
	/**
	 * Returns a list of people and robots who have signed up for this mission.
	 * 
	 * @return
	 */
	public Set<Worker> getSignup() {
		return signUp;
	}
	
	/**
	 * Adds a Robot directly.
	 * 
	 * @param member
	 */
	protected void addRobot(Robot member) {
		members.add(member);
		signUp.add(member);
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
	public final boolean isDone() {
		return done;
	}

	/**
	 * Gets the name of the mission.
	 *
	 * @return name of mission
	 */
	public final String getName() {
		return missionName;
	}

	/**
	 * Updates the mission name.
	 * 
	 * @param newName
	 */
    public void setName(String newName) {
		this.missionName = newName;
    }

	/**
	 * Gets the type and identifier of the mission.
	 *
	 * @return type and identifier of the mission
	 * @deprecated use {@link #getName()}
	 */
	public final String getTypeID() {
		return missionName;
	}

	/**
	 * Gets the mission type enum.
	 *
	 * @return
	 */
	public MissionType getMissionType() {
		return missionType;
	}


	/**
	 * Gets the mission's description.
	 *
	 * @return mission description
	 */
	public final String getDescription() {
		return description;
	}

	/**
	 * Sets the mission's description.
	 *
	 * @param description the new description.
	 */
	public final void setDescription(String description) {
		if (!this.description.equals(description)) {
			this.description = description;
			fireMissionUpdate(MissionEventType.DESCRIPTION_EVENT, description);
		}
	}

	/**
	 * Gets the current phase of the mission.
	 *
	 * @return phase
	 */
	public final MissionPhase getPhase() {
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
	protected final void setPhase(MissionPhase newPhase, String subjectOfPhase) {
		if (newPhase == null) {
			throw new IllegalArgumentException("newPhase is null");
		}

		// Move phase on
 		phase = newPhase;
		setPhaseEnded(false);
		phaseStartTime = (MarsClock) marsClock.clone();

		String template = newPhase.getDescriptionTemplate();
		if (template != null) {
			phaseDescription = MessageFormat.format(template, subjectOfPhase);
		}
		else {
			phaseDescription = "";
		}

		// Add entry to the log
		addMissionLog(newPhase.getName());

		fireMissionUpdate(MissionEventType.PHASE_EVENT, newPhase);
	}

	protected void addMissionLog(String entry) {
		String time = marsClock.getTrucatedDateTimeStamp();
		log.add(new MissionLogEntry(time, entry));
	}

	/**
	 * Gets the mission log.
	 */
	public List<MissionLogEntry> getLog() {
		return log;
	}

	/**
	 * Time that the current phases started
	 */
	public MarsClock getPhaseStartTime() {
		return phaseStartTime;
	}

	/**
	 * Gets duration of current Phase.
	 */
	protected double getPhaseDuration() {
		return MarsClock.getTimeDiff(marsClock, phaseStartTime);
	}

	/**
	 * Gets the description of the current phase.
	 *
	 * @return phase description.
	 */
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
	 */
	public void performMission(Worker member) {

		// If current phase is over, decide what to do next.
		if (phaseEnded && !determineNewPhase()) {
			logger.warning(member, "New phase for " + getName()
							+ " cannot be determined for " + phase.getName());
		}

		// Perform phase.
		if (!done) {
			performPhase(member);
		}
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
			endMission(new MissionStatus(INTERNAL_PROBLEM, "Current phase null"));
		}
	}

	/**
	 * Gets the mission capacity for participating people.
	 *
	 * @return mission capacity
	 */
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
	 * Calculate the mission capacity the lower of desired capacity or number of EVASuits.
	 */
	protected void calculateMissionCapacity(int desiredCap) {
		if (!isDone()) {
			// Set mission capacity.
			int availableSuitNum = Mission.getNumberAvailableEVASuitsAtSettlement(getStartingPerson().getAssociatedSettlement());
			if (availableSuitNum < desiredCap) {
				desiredCap = availableSuitNum;
			}
			setMissionCapacity(desiredCap);
		}
	}


	/** 
	 * Abort the current phase; nothign on the base class
	 */
	public void abortPhase() {
		// Do nothing
	}

	/**
	 * Abort the mission. Will not immediately stop
	 */
	public void abortMission() {
		// Normal mission can not be aborted
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
	 * Finalizes the mission. Reason for ending mission. Mission can
	 * override this to perform necessary finalizing operations.
	 *
	 * @param endStatus A status to add for the end of Mission
	 *
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

		// If no mission flags have been added then it was accomplised
		if (missionStatus.isEmpty()) {
			missionStatus.add(MISSION_ACCOMPLISHED);
			addMissionScore();
		}

		done = true; // Note: done = true is very important to keep !
		phase = null; // No more phase

		String listOfStatuses = missionStatus.stream().map(MissionStatus::getName).collect(Collectors.joining(", "));
		
		StringBuilder status = new StringBuilder();
		status.append("Ended the ")
			.append(getName())
			.append(" with the status flag(s): ")
			.append(listOfStatuses);
		logger.info(startingMember, status.toString());

		// Disband the members
		if (members != null && !members.isEmpty()) {
			String listOfMembers = members.stream().map(Worker::getName).collect(Collectors.joining(", "));
			logger.info(startingMember, "Disbanding mission member(s): " + listOfMembers);
			Iterator<Worker> i = getMemberList().iterator();
			while (i.hasNext()) {
				Worker member = i.next();
				i.remove();
				adjustShift(member);
			}	
		}
	}

	/**
	 * Checks if a person has any issues in starting a new task.
	 *
	 * @param person the person to assign to the task
	 * @param task   the new task to be assigned
	 * @return true if task can be performed.
	 */
	protected boolean assignTask(Person person, Task task) {
		boolean canPerformTask = !task.isEffortDriven() || (person.getPerformanceRating() != 0D);

		// If task is effort-driven and person too ill, do not assign task.

        if (canPerformTask) {
			canPerformTask = person.getMind().getTaskManager().addTask(task);
		}

		return canPerformTask;
	}

	/**
	 * Adds a new task for a robot in the mission. Task may be not assigned if the
	 * robot has a malfunction.
	 *
	 * @param robot the robot to assign to the task
	 * @param task  the new task to be assigned
	 * @return true if task can be performed.
	 */
	protected boolean assignTask(Robot robot, Task task) {

		// If robot is malfunctioning, it cannot perform task.
		if (robot.getMalfunctionManager().hasMalfunction()) {
			return false;
		}

		if (!robot.getSystemCondition().isBatteryAbove(5))
			return false;

		return robot.getBotMind().getBotTaskManager().addTask(task);
	}

	/**
	 * Checks to see if any of the people in the mission have any dangerous medical
	 * problems that require treatment at a settlement. Also any environmental
	 * problems, such as suffocation.
	 *
	 * @return true if dangerous medical problems
	 */
	private final boolean hasDangerousMedicalProblems() {
		for (Worker member : members) {
			if (member.getUnitType() == UnitType.PERSON) {
				if (((Person) member).getPhysicalCondition().hasSeriousMedicalProblems()) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Checks to see if any of the people in the mission have any potential medical
	 * problems due to low fitness level that will soon degrade into illness.
	 *
	 * @return true if potential medical problems exist
	 */
	protected final boolean hasAnyPotentialMedicalProblems() {
		for (Worker member : members) {
			if (member.getUnitType() == UnitType.PERSON) {
				if (((Person) member).getPhysicalCondition().computeFitnessLevel() < 2) {
					return true;
				}
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
			if (member.getUnitType() == UnitType.PERSON) {
				if (!((Person) member).getPhysicalCondition().hasSeriousMedicalProblems()) {
					result = false;
				}
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
	 * @param minMembers Minimum number of members requried
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
		for(Person person : possibles) {
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
		int max = 0;

		if (pop < 4)
			max = 1;
		else if (pop >= 4 && pop < 7)
			max = 2;
		else if (pop >= 7 && pop < 10)
			max = 3;
		else if (pop >= 10 && pop < 14)
			max = 4;
		else if (pop >= 14 && pop < 18)
			max = 5;
		else if (pop >= 18 && pop < 23)
			max = 6;
		else if (pop >= 23 && pop < 29)
			max = 7;
		else if (pop >= 29)
			max = 8;

		// 50% tendency to have 1 less person
		int rand = RandomUtil.getRandomInt(1);
		if (rand == 1) {
			if (max >= 5)
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

		if (getMembersNumber() < minMembers) {
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
	public double getMissionQualification(Worker member) {

		double result = 0D;

		if (member.getUnitType() == UnitType.PERSON) {
			Person person = (Person) member;
			result = Math.max(5,  person.getMissionExperience(missionType));

			// Get base result for job modifier.
			Set<JobType> prefered = getPreferredPersonJobs();
			JobType job = person.getMind().getJob();
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
	 * Gets the settlement associated with the mission.
	 *
	 * @return settlement or null if none.
	 */
	public abstract Settlement getAssociatedSettlement();

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
			logger.severe(getName() + ":No starting member");
		}

		return result;
	}

	/**
	 * Gets the number of available EVA suits for a mission at a settlement.
	 *
	 * @param settlement the settlement to check.
	 * @return number of available suits.
	 */
	public static int getNumberAvailableEVASuitsAtSettlement(Settlement settlement) {

		if (settlement == null)
			throw new IllegalArgumentException("Settlement is null");

		int result = settlement.findNumContainersOfType(EquipmentType.EVA_SUIT);

		// Leave one suit for settlement use.
		if (result > 0) {
			result--;
		}
		return result;
	}

	/**
	 * Requests review for the mission.
	 *
	 * @param member the mission lead.
	 */
	protected void requestReviewPhase(Worker member) {
		Person p = (Person)member;

		if (plan == null) {
			plan = new MissionPlanning(this);
			logger.log(member, Level.INFO, 0, "Serving as the mission lead on " + this + ".");

			 missionManager.requestMissionApproving(plan);
		}

		else {
			if (plan.getStatus() == PlanType.NOT_APPROVED) {
				endMission(MISSION_NOT_APPROVED);
			}

			else if (plan.getStatus() == PlanType.APPROVED) {

				createFullDesignation();

				logger.log(p, Level.INFO, 0, "Mission plan for " + getDescription() + " was approved.");

				if (!(this instanceof VehicleMission)) {
					// Set the members' work shift to on-call to get ready
					for (Worker m : members) {
						 ((Person) m).setShiftType(ShiftType.ON_CALL);
					}
				}
				setPhaseEnded(true);
			}
		}
	}

	/**
	 * Returns the mission plan.
	 *
	 * @return {@link MissionPlanning}
	 */
	public MissionPlanning getPlan() {
		return plan;
	}

	/**
	 * Returns the starting person.
	 *
	 * @return {@link Person}
	 */
	public Person getStartingPerson() {
		if (startingMember.getUnitType() == UnitType.PERSON)
			return (Person)startingMember;
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

	public String getFullMissionDesignation() {
		return fullMissionDesignation;
	}

	/**
	 * Creates the mission designation string for this mission.
	 *
	 * @return
	 */
	protected void createFullDesignation() {
		fullMissionDesignation = Conversion.getInitials(getDescription().replace("with", "").trim()) + " "
				+ missionManager.getMissionDesignationString(getAssociatedSettlement().getName());
		
		fireMissionUpdate(MissionEventType.DESIGNATION_EVENT, fullMissionDesignation);
	}

	public void setReservedVehicle(String name) {
		vehicleReserved = name;
	}

	public String getReservedVehicle() {
		return vehicleReserved;
	}

	/**
	 * An internal problem has happened so end the mission.
	 */
	protected void endMissionProblem(Loggable source, String reason) {
		MissionStatus status = new MissionStatus(INTERNAL_PROBLEM, reason);
		logger.severe(source, getName() + ": " + status.getName());
		endMission(status);
	}


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
			addMissionLog(status.getName());
		}
		return newStatus;
	}

	public int getPriority() {
		return priority;
	}

	/**
	 * Checks if this worker can participate.
	 * 
	 * @param worker This maybe used by overridding methods
	 * @return
	 */
	public boolean canParticipate(Worker worker) {
		return true;
	}
	
	/**
	 * Compares if this object equals this instance of mission.
	 */
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (this.getClass() != obj.getClass()) return false;
		Mission m = (Mission) obj;
		return this.missionType == m.getMissionType()
				&& this.missionName.equals(m.getName());
	}

	/**
	 * Gets the hash code for this object.
	 *
	 * @return hash code.
	 */
	public int hashCode() {
		int hashCode = (1 + missionName.hashCode());
		hashCode *= missionType.hashCode();
		return hashCode;
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
	public static void initializeInstances(Simulation si, MarsClock c, HistoricalEventManager e,
			UnitManager u, SurfaceFeatures sf, 
			MissionManager m, PersonConfig pc) {
		sim = si;
		marsClock = c;
		eventManager = e;
		unitManager = u;
		surfaceFeatures = sf;
		missionManager = m;
		personConfig = pc;
	}

	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		if (members != null) {
			members.clear();
		}
		members = null;
		missionName = null;
		phase = null;
		phaseDescription = null;
		if (listeners != null) {
			listeners.clear();
		}
		listeners = null;
	}

}
