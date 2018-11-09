/**
 * Mars Simulation Project
 * Mission.java
 * @version 3.1.0 2017-05-05
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ShiftType;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.social.RelationshipManager;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.ai.job.RobotJob;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.tool.Conversion;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The Mission class represents a large multi-person task There is at most one
 * instance of a mission per person. A Mission may have one or more people
 * associated with it.
 */
public abstract class Mission implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static Logger logger = Logger.getLogger(Mission.class.getName());

	private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
			logger.getName().length());

	protected static final int MAX_AMOUNT_RESOURCE = ResourceUtil.FIRST_ITEM_RESOURCE;

	public static final String SUCCESSFULLY_ENDED_CONSTRUCTION = "Construction ended.";
	public static final String SUCCESSFULLY_DISEMBARKED = "All members disembarked.";
	public static final String USER_ABORTED_MISSION = "Mission aborted by user.";
	public static final String UNREPAIRABLE_MALFUNCTION = "Unrepairable malfunction.";
	public static final String NO_RESERVABLE_VEHICLES = "No reservable vehicles.";
	public static final String NO_AVAILABLE_VEHICLES = "No available vehicles.";
	public static final String NOT_ENOUGH_RESOURCES = "Not enough resources.";
	public static final String NO_EMERGENCY_SETTLEMENT_DESTINATION_FOUND = "No emergency settlement destination found.";
	public static final String MEDICAL_EMERGENCY = "A member has a medical emergency.";
	public static final String NO_TRADING_SETTLEMENT = "No trading settlement found.";
	public static final String NO_GOOD_EVA_SUIT = "No good EVA suit.";
	public static final String REQUEST_RESCUE = "Requesting rescue.";
	public static final String NO_ONGOING_SCIENTIFIC_STUDY = "No on-going scientific study being conducted in this subject.";
	public static final String VEHICLE_NOT_LOADABLE = "Cannot load resources into the rover.";
	public static final String NO_EXPLORATION_SITES = "Exploration sites could not be determined.";
	public static final String NOT_ENOUGH_MEMBERS = "Not enough members recruited.";
	public static final String NO_MEMBERS_ON_MISSION = "No members available for mission.";
	public static final String MISSION_NOT_APPROVED = "Mission not approved.";
	public static final String TARGET_VEHICLE_NOT_FOUND = "Target vehicle not found.";
	

	public static final String MISSION = " mission";
	public static final String[] EXPRESSIONS = new String[] {
			"Where is everybody when I need someone for ",
			"So no one is available for ",
			"How come no one is available for "
	};
	
	public static final String OUTSIDE = "Outside";

	/**
	 * The marginal factor for the amount of water to be brought during a mission.
	 */
	public final static double WATER_MARGIN = 4.4; // TODO: need to find out why water is running so fast in vehicle
	/**
	 * The marginal factor for the amount of oxygen to be brought during a mission.
	 */
	public final static double OXYGEN_MARGIN = 3.1;
	/**
	 * The marginal factor for the amount of food to be brought during a mission.
	 */
	public final static double FOOD_MARGIN = 2.1;
	/**
	 * The marginal factor for the amount of dessert to be brought during a mission.
	 */
	public final static double DESSERT_MARGIN = 1.5;

	// Global mission identifier
	private static int missionIdentifer = 0;

	// Data members
	/** mission type id */
	private int missionID;
	/** Unique identifier */
	private int identifier;
	/** The minimum number of members for mission. */
	private int minMembers;
	/** The number of people that can be in the mission. */
	private int missionCapacity;
	/** Has the current phase ended? */
	private boolean phaseEnded;
	/** True if mission is completed. */
	private boolean done;
	/** True if the mission has been approved. */
	protected boolean approved = false;
	/** True if the mission has been requested. */
	protected boolean requested = false;
	/** Name of mission. */
	private String missionName;
	/** The description of the current phase of operation. */
	private String phaseDescription;
	

	/** The current phase of the mission. */
	private MissionPhase phase;
	/** The name of the starting member */
	private MissionMember startingMember;
	/** The mission plan. */
	private MissionPlanning plan;

	/** Mission members. */
	private Collection<MissionMember> members;
	/** A collection of the mission's phases. */
	private Collection<MissionPhase> phases;

	// transient members
	/** Mission listeners. */
	private transient List<MissionListener> listeners;

	// Static members

	/**
	 * Must be synchronised to prevent duplicate ids being assigned via different
	 * threads.
	 * 
	 * @return
	 */
	private static synchronized int getNextIdentifier() {
		return missionIdentifer++;
	}

	public Mission(String missionName, MissionMember startingMember, int minMembers) {
		// Initialize data members
		this.identifier = getNextIdentifier();
		this.missionName = missionName;
		this.startingMember = startingMember;
		
		missionID = MissionManager.matchMissionID(missionName);
		
		members = new ConcurrentLinkedQueue<MissionMember>();
		done = false;
		phase = null;
		phaseDescription = null;
		phases = new ArrayList<MissionPhase>();
		phaseEnded = false;
		this.minMembers = minMembers;
		missionCapacity = Integer.MAX_VALUE;
				
		listeners = Collections.synchronizedList(new ArrayList<MissionListener>());

		Person person = (Person) startingMember;
		String loc0 = null;
		String loc1 = null;

		if (person.isInSettlement()) {
			loc0 = person.getBuildingLocation().getNickName();
			loc1 = person.getSettlement().getName();

			// Created mission starting event.
			HistoricalEvent newEvent = new MissionHistoricalEvent(EventType.MISSION_START, this,
					"Mission Starting", missionName, person.getName(), loc0, loc1);

			Simulation.instance().getEventManager().registerNewEvent(newEvent);

			// Log mission starting.
			int n = members.size();
			String str = null;
			if (n == 0)
				str = ".";
			else if (n == 1)
				str = "' with 1 other.";
			else
				str = "' with " + n + " others.";
			
			String article = "a ";
					
			if(Conversion.isVowel(missionName))
				article = "an ";

			LogConsolidated.log(logger, Level.INFO, 500, sourceName, "[" + person.getSettlement() + "] "
					+ startingMember.getName() + " is organizing " + article + missionName + " mission" + str, null);

			// Add starting member to mission.
			// Temporarily set the shift type to none during the mission
			startingMember.setMission(this);

			if (startingMember instanceof Person)
				startingMember.setShiftType(ShiftType.ON_CALL);

		}

	}

	public int getIdentifier() {
		return identifier;
	}

	/**
	 * Generate the type from the Class name. Not ideal.
	 * 
	 * @return
	 */
	public String getType() {
		return getClass().getSimpleName();
	}

	/**
	 * Adds a listener.
	 * 
	 * @param newListener the listener to add.
	 */
	public final void addMissionListener(MissionListener newListener) {
		if (listeners == null) {
			listeners = Collections.synchronizedList(new ArrayList<MissionListener>());
		}
		if (!listeners.contains(newListener)) {
			listeners.add(newListener);
		}
	}

	/**
	 * Removes a listener.
	 * 
	 * @param oldListener the listener to remove.
	 */
	public final void removeMissionListener(MissionListener oldListener) {
		if (listeners == null) {
			listeners = Collections.synchronizedList(new ArrayList<MissionListener>());
		}
		if (listeners.contains(oldListener)) {
			listeners.remove(oldListener);
		}
	}

	/**
	 * Fire a mission update event.
	 * 
	 * @param updateType the update type.
	 */
	protected final void fireMissionUpdate(MissionEventType updateType) {
		fireMissionUpdate(updateType, null);
	}

	/**
	 * Fire a mission update event.
	 * 
	 * @param addMemberEvent the update type.
	 * @param target         the event target or null if none.
	 */
	protected final void fireMissionUpdate(MissionEventType addMemberEvent, Object target) {
		if (listeners == null)
			listeners = Collections.synchronizedList(new ArrayList<MissionListener>());
		synchronized (listeners) {
			Iterator<MissionListener> i = listeners.iterator();
			while (i.hasNext())
				i.next().missionUpdate(new MissionEvent(this, addMemberEvent, target));
		}
	}

	/**
	 * Gets the string representation of this mission.
	 */
	public String toString() {
		return missionName;
	}

	public final void addMember(MissionMember member) {
		if (!members.contains(member)) {
			members.add(member);

			Person person = (Person) member;
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
			HistoricalEvent newEvent = new MissionHistoricalEvent(EventType.MISSION_JOINING, this,
					"Adding a member", missionName, person.getName(), loc0, loc1);

			Simulation.instance().getEventManager().registerNewEvent(newEvent);

			fireMissionUpdate(MissionEventType.ADD_MEMBER_EVENT, member);

			logger.finer(member.getName() + " added to mission: " + missionName);
		}
	}

	/**
	 * Removes a member from the mission.
	 * 
	 * @param member to be removed
	 */
	public final void removeMember(MissionMember member) {
		if (members.contains(member)) {
			members.remove(member);
			// logger.info("done removing " + member);

			// Added codes in reassigning a work shift
			if (member instanceof Person) {
				Person person = (Person) member;
				person.getMind().stopMission();// setMission(null);
				member.setMission(null);

				ShiftType shift = null;
				if (person.getSettlement() != null) {
					shift = person.getSettlement().getAnEmptyWorkShift(-1);
					person.setShiftType(shift);
				} else if (person.getVehicle() != null)
					if (person.getVehicle().getSettlement() != null) {
						shift = person.getVehicle().getSettlement().getAnEmptyWorkShift(-1);
						person.setShiftType(shift);
					}

				// Creating missing finishing event.
//				 HistoricalEvent newEvent = new MissionHistoricalEvent(member, this,
//				 EventType.MISSION_FINISH);

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

				Simulation.instance().getEventManager()
						.registerNewEvent(new MissionHistoricalEvent(EventType.MISSION_FINISH, this,
								"Removing a member", missionName, person.getName(), loc0, loc1));
				fireMissionUpdate(MissionEventType.REMOVE_MEMBER_EVENT, member);

				if ((members.size() == 0) && !done) {
					endMission(NOT_ENOUGH_MEMBERS);
				}

				// logger.fine(member.getName() + " removed from mission : " + name);
			}
		}
	}

	/**
	 * Determines if a mission includes the given member.
	 * 
	 * @param member member to be checked
	 * @return true if member is a part of the mission.
	 */
	public final boolean hasMember(MissionMember member) {
		return members.contains(member);
	}

//	/**
//	 * Determines if a mission includes the given person.
//	 * @param person person to be checked
//	 * @return true if person is member of mission
//	 */
//	public final boolean hasPerson(Person person) {
//		return people.contains(person);
//	}

	/**
	 * Gets the number of members in the mission.
	 * 
	 * @return number of members.
	 */
	public final int getMembersNumber() {
		return members.size();
	}

	/**
	 * Gets the number of people in the mission.
	 * 
	 * @return number of people
	 */
	public final int getPeopleNumber() {
		int result = 0;

		Iterator<MissionMember> i = members.iterator();
		while (i.hasNext()) {
			if (i.next() instanceof Person) {
				result++;
			}
		}

		return result;
	}

	/**
	 * Gets the minimum number of members required for mission.
	 * 
	 * @return minimum number of members
	 */
	public final int getMinMembers() {
		return minMembers;
	}

	/**
	 * Sets the minimum number of members required for a mission.
	 * 
	 * @param minMembers minimum number of members
	 */
	protected final void setMinMembers(int minMembers) {
		this.minMembers = minMembers;
		fireMissionUpdate(MissionEventType.MIN_MEMBERS_EVENT, minMembers);
	}

	/**
	 * Gets a collection of the members in the mission.
	 * 
	 * @return collection of members
	 */
	public final Collection<MissionMember> getMembers() {
		return new ConcurrentLinkedQueue<MissionMember>(members);
	}

	/**
	 * Gets a collection of the people in the mission.
	 * 
	 * @return collection of people
	 */
	public final Collection<Person> getPeople() {
		Collection<MissionMember> members = getMembers();
		Collection<Person> people = new ConcurrentLinkedQueue<Person>();
		// Collection<Person> people = members.stream()
		// .filter(p -> p instanceof Person).collect(Collectors.toList());

		Iterator<MissionMember> i = members.iterator();
		while (i.hasNext()) {
			MissionMember m = i.next();
			if (m instanceof Person) {
				people.add((Person) m);
			}
		}

		return people;
		// return new ConcurrentLinkedQueue<Person>(people);
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
	 * Set mission as done.
	 */
	public void setDone(boolean value) {
		done = value;
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
	 * Sets the name of the mission.
	 * 
	 * @param name the new mission name
	 */
	protected final void setName(String name) {
		this.missionName = name;
		fireMissionUpdate(MissionEventType.NAME_EVENT, name);
	}

	/**
	 * Gets the mission's description.
	 * 
	 * @return mission description
	 */
	public final String getDescription() {
		return missionName;
	}

	/**
	 * Sets the mission's description.
	 * 
	 * @param description the new description.
	 */
	public final void setDescription(String description) {
		if (!this.missionName.equals(description)) {
			this.missionName = description;
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
	 * Sets the mission phase.
	 * 
	 * @param newPhase the new mission phase.
	 * @throws MissionException if newPhase is not in the mission's collection of
	 *                          phases.
	 */
	protected final void setPhase(MissionPhase newPhase) {
		if (newPhase == null) {
			throw new IllegalArgumentException("newPhase is null");
		} else if (phases.contains(newPhase)) {
			phase = newPhase;
			setPhaseEnded(false);
			phaseDescription = null;
			fireMissionUpdate(MissionEventType.PHASE_EVENT, newPhase);
		} else {
			throw new IllegalStateException(
					phase + " : newPhase: " + newPhase + " is not a valid phase for this mission.");
		}
	}

	/**
	 * Adds a phase to the mission's collection of phases.
	 * 
	 * @param newPhase the new phase to add.
	 */
	public final void addPhase(MissionPhase newPhase) {
		if (newPhase == null) {
			throw new IllegalArgumentException("newPhase is null");
		} else if (!phases.contains(newPhase)) {
			phases.add(newPhase);
		}
	}

	/**
	 * Gets the description of the current phase.
	 * 
	 * @return phase description.
	 */
	public final String getPhaseDescription() {
		if (phaseDescription != null) {
			return Conversion.capitalize(phaseDescription);
		} else if (phase != null) {
			return Conversion.capitalize(phase.toString());
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
	public void performMission(MissionMember member) {

		// If current phase is over, decide what to do next.
		if (phaseEnded) {
			determineNewPhase();
		}

		// Perform phase.
		if (!done) {
			performPhase(member);
		}
	}

	/**
	 * Determines a new phase for the mission when the current phase has ended.
	 * 
	 * @throws MissionException if problem setting a new phase.
	 */
	protected abstract void determineNewPhase();

	/**
	 * The member performs the current phase of the mission.
	 * 
	 * @param member the member performing the phase.
	 */
	protected void performPhase(MissionMember member) {
		if (phase == null) {
			endMission("Current mission phase is null.");
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
	 * End the mission collection phase at the current site.
	 */
	protected void endCollectionPhase() {
		if (this instanceof CollectResourcesMission)
			((CollectResourcesMission) this).endCollectingAtSite();
	}

	/**
	 * Have the mission return home and end collection phase if necessary.
	 */
	protected void returnHome() {
		if (this instanceof TravelMission) {
			TravelMission travelMission = (TravelMission) this;
			int offset = 2;
			if (travelMission.getPhase().equals(VehicleMission.TRAVELLING))
				offset = 1;
			travelMission.setNextNavpointIndex(travelMission.getNumberOfNavpoints() - offset);
			travelMission.updateTravelDestination();
			endCollectionPhase();
		}
	}

	/**
	 * Go to the nearest settlement and end collection phase if necessary.
	 */
	// TODO : connect to determineEmergencyDestination() in VehicleMission
	private void goToNearestSettlement() {
		if (this instanceof VehicleMission) {
			VehicleMission vehicleMission = (VehicleMission) this;
			try {
				Settlement nearestSettlement = vehicleMission.findClosestSettlement();
				if (nearestSettlement != null) {
					vehicleMission.clearRemainingNavpoints();
					vehicleMission.addNavpoint(new NavPoint(nearestSettlement.getCoordinates(), nearestSettlement,
							nearestSettlement.getName()));
					vehicleMission.associateAllMembersWithSettlement(nearestSettlement);
					vehicleMission.updateTravelDestination();
					endCollectionPhase();
				}
			} catch (Exception e) {
			}
		}
	}

	/**
	 * Computes the mission experience score
	 * 
	 * @param reason
	 */
	public void addMissionScore(String reason) {
		if (reason.equals(SUCCESSFULLY_DISEMBARKED)) {
			for (MissionMember member : members) {
				if (member instanceof Person) {
					Person person = (Person) member;
					
					if (!person.isDeclaredDead()) {
						if (!person.getPhysicalCondition().hasSeriousMedicalProblems()) {
							person.addMissionExperience(missionID, 5);
						}
						else
							// Note : there is a minor penalty for those who are sick 
							// and thus unable to fully function during the mission
							person.addMissionExperience(missionID, 2.5);
						
						if (person.equals(startingMember)) {
							person.addMissionExperience(missionID, 2.5);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Finalizes the mission. String reason Reason for ending mission. Mission can
	 * override this to perform necessary finalizing operations.
	 */
	public void endMission(String reason) {
//		 if (!(this instanceof RescueSalvageVehicle)) {
//		 returnHome();
//		 goToNearestSettlement();
//		 }

		// Add mission experience score
		addMissionScore(reason);
		
		// Note: !done is very important to keep !
		if (!done) {
			// !reason.equals(USER_ABORTED_MISSION)
			// reason.equals(SUCCESSFULLY_ENDED_CONSTRUCTION) 
			// reason.equals(SUCCESSFULLY_DISEMBARKED)
			// reason.equals(USER_ABORTED_MISSION)

			// TODO : there can be custom reason such as "Equipment EVA Suit 12 cannot be
			// loaded in rover Rahu" with mission name 'Trade With Camp Bradbury'

			LogConsolidated.log(logger, Level.INFO, 3000, sourceName,
					"[" + startingMember.getLocationTag().getQuickLocation() + "] " + startingMember.getName()
							+ " ended the " + missionName + " mission. Reason : " + reason,
					null);

			done = true; // Note: done = true is very important to keep !
			fireMissionUpdate(MissionEventType.END_MISSION_EVENT);
			// logger.info("done firing End_Mission_Event");

			if (members != null) {
				if (!members.isEmpty()) {
					LogConsolidated.log(logger, Level.INFO, 3000, sourceName,
							"[" + startingMember.getLocationTag().getQuickLocation()
									+ "] Disbanding mission member(s) : " + members,
							null);
					Iterator<MissionMember> i = members.iterator();
					while (i.hasNext()) {
						removeMember(i.next());

					}
				}
			}

		} else
			LogConsolidated.log(logger, Level.INFO, 0, sourceName,
					"[" + startingMember.getLocationTag().getQuickLocation() + "] " + startingMember.getName()
							+ " is ending the " + missionName + ". Reason : '" + reason + "'",
					null);
		
		// Proactively call removeMission to update the list in MissionManager right away
//		missionManager.removeMission(this); // not legit ! will crash the mission tab in monitor tool
	}

	/**
	 * Adds a new task for a person in the mission. Task may be not assigned if it
	 * is effort-driven and person is too ill to perform it.
	 * 
	 * @param person the person to assign to the task
	 * @param task   the new task to be assigned
	 * @return true if task can be performed.
	 */
	protected boolean assignTask(Person person, Task task) {
		boolean canPerformTask = true;

		// If task is effort-driven and person too ill, do not assign task.
		if (task.isEffortDriven() && (person.getPerformanceRating() == 0D)) {
			canPerformTask = false;
		}

		if (canPerformTask) {
			person.getMind().getTaskManager().addTask(task);
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
		boolean canPerformTask = true;

		// If robot is malfunctioning, it cannot perform task.
		boolean hasMalfunction = robot.getMalfunctionManager().hasMalfunction();
		if (hasMalfunction) {
			canPerformTask = false;
		}

		if (canPerformTask) {
			robot.getBotMind().getBotTaskManager().addTask(task);
		}

		return canPerformTask;
	}

	/**
	 * Checks to see if any of the people in the mission have any dangerous medical
	 * problems that require treatment at a settlement. Also any environmental
	 * problems, such as suffocation.
	 * 
	 * @return true if dangerous medical problems
	 */
	protected final boolean hasDangerousMedicalProblems() {
		boolean result = false;
		for (MissionMember member : members) {
			if (member instanceof Person) {
				Person person = (Person) member;
				if (person.getPhysicalCondition().hasSeriousMedicalProblems()) {
					result = true;
				}
			}
		}

		return result;
	}

	/**
	 * Checks to see if all of the people in the mission have any dangerous medical
	 * problems that require treatment at a settlement. Also any environmental
	 * problems, such as suffocation.
	 * 
	 * @return true if all have dangerous medical problems
	 */
	protected final boolean hasDangerousMedicalProblemsAllCrew() {
		boolean result = true;
		for (MissionMember member : members) {
			if (member instanceof Person) {
				Person person = (Person) member;
				if (!person.getPhysicalCondition().hasSeriousMedicalProblems()) {
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
	protected boolean hasEmergencyAllCrew() {
		return hasDangerousMedicalProblemsAllCrew();
	}

	/**
	 * Recruits new members into the mission.
	 * 
	 * @param startingMember the mission member starting the mission.
	 */
	protected void recruitMembersForMission(MissionMember startingMember) {

		// Get all people qualified for the mission.
		Collection<Person> qualifiedPeople = new ConcurrentLinkedQueue<Person>();
		Iterator<Person> i = Simulation.instance().getUnitManager().getPeople().iterator();
		while (i.hasNext()) {
			Person person = i.next();
			if (isCapableOfMission(person)) {
				qualifiedPeople.add(person);
			}
		}

		// Recruit the most qualified and most liked people first.
		while (qualifiedPeople.size() > 0) {
			double bestPersonValue = 0D;
			Person bestPerson = null;
			Iterator<Person> j = qualifiedPeople.iterator();
			while (j.hasNext() && (getMembersNumber() < missionCapacity)) {
				Person person = j.next();
				// Determine the person's mission qualification.
				double qualification = getMissionQualification(person) * 100D;

				// Determine how much the recruiter likes the person.
				double likability = 50D;
				if (startingMember instanceof Person) {
					RelationshipManager relationshipManager = Simulation.instance().getRelationshipManager();
					likability = relationshipManager.getOpinionOfPerson((Person) startingMember, person);
				}

				// Check if person is the best recruit.
				double personValue = (qualification + likability) / 2D;
				if (personValue > bestPersonValue) {
					bestPerson = person;
					bestPersonValue = personValue;
				}
			}

			// Try to recruit best person available to the mission.
			if (bestPerson != null) {
				recruitPerson(startingMember, bestPerson);
				qualifiedPeople.remove(bestPerson);
			} else {
				break;
			}
		}
		/*
		 * // Recruit robots qualified for the mission. Iterator <Robot> k =
		 * Simulation.instance().getUnitManager().getRobots().iterator(); while
		 * (k.hasNext() && (getMembersNumber() < missionCapacity)) { Robot robot =
		 * k.next(); if (isCapableOfMission(robot)) { robot.setMission(this); } }
		 */
		if (getMembersNumber() < minMembers) {
			endMission("Not enough members");
		}
	}

	/**
	 * Attempt to recruit a new person into the mission.
	 * 
	 * @param recruiter the mission member doing the recruiting.
	 * @param recruitee the person being recruited.
	 */
	private void recruitPerson(MissionMember recruiter, Person recruitee) {
		if (isCapableOfMission(recruitee)) {
			// Get mission qualification modifier.
			double qualification = getMissionQualification(recruitee) * 100D;

			RelationshipManager relationshipManager = Simulation.instance().getRelationshipManager();

			// Get the recruitee's social opinion of the recruiter.
			double recruiterLikability = 50D;
			if (recruiter instanceof Person) {
				recruiterLikability = relationshipManager.getOpinionOfPerson(recruitee, (Person) recruiter);
			}

			// Get the recruitee's average opinion of all the current mission members.
			List<Person> people = new ArrayList<Person>();
			Iterator<MissionMember> i = members.iterator();
			while (i.hasNext()) {
				MissionMember member = i.next();
				if (member instanceof Person) {
					people.add((Person) member);
				}
			}
			double groupLikability = relationshipManager.getAverageOpinionOfPeople(recruitee, people);

			double recruitmentChance = (qualification + recruiterLikability + groupLikability) / 3D;
			if (recruitmentChance > 100D) {
				recruitmentChance = 100D;
			} else if (recruitmentChance < 0D) {
				recruitmentChance = 0D;
			}

			if (RandomUtil.lessThanRandPercent(recruitmentChance)) {
				recruitee.setMission(this);

				if (recruitee instanceof Person) {
					((Person) recruitee).setShiftType(ShiftType.ON_CALL);
				}
				// robot cannot be a recruitee
				// else if (recruitee instanceof Robot) {
				// ((Robot) recruitee).getTaskSchedule().setShiftType("None");
				// }
			}
		}
	}

	/**
	 * Checks to see if a member is capable of joining a mission.
	 * 
	 * @param member the member to check.
	 * @return true if member could join mission.
	 */
	protected boolean isCapableOfMission(MissionMember member) {
		boolean result = false;

		if (member == null) {
			throw new IllegalArgumentException("member is null");
		}

		if (member instanceof Person) {
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
		} else if (member instanceof Robot) {
			Robot robot = (Robot) member;

			// Make sure robot isn't already on a mission.
			boolean onMission = (robot.getBotMind().getMission() != null);

			// Make sure robot doesn't have a malfunction.
			boolean hasMalfunction = robot.getMalfunctionManager().hasMalfunction();

			// Check if robot is qualified to join the mission.
			boolean isQualified = (getMissionQualification(robot) > 0D);

			if (!onMission && !hasMalfunction && isQualified) {
				result = true;
			}
		}

		return result;
	}

	public double[] computeAverage(List<Double> list) {
		double ave = 0;
		double total = 0;
		int size = list.size();
		if (!list.isEmpty()) {
			for (double i : list) {
				total += i;
			}
			ave = total/size;
		}
		else {
			ave = 0;
		}
		return new double[] {size, ave};
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
	public double getMissionQualification(MissionMember member) {

		double result = 0D;

//        if (isCapableOfMission(member)) {

		if (member instanceof Person) {
			Person person = (Person) member;
			
			// Compute the mission experience score
			if (person.getMissionExperiences().containsKey(missionID)) {
				double[] score = new double[2];
				
				List<Double> list = person.getMissionExperiences().get(missionID);
	
				if (!list.isEmpty()) {
					score = computeAverage(list);
					result = score[0] * score[1] /2.0;
				}
				else
					result = 5;
			}
			else
				result = 5;
			
			// Get base result for job modifier.
			Job job = person.getMind().getJob();
			if (job != null) {
				result *= 5 * job.getJoinMissionProbabilityModifier(this.getClass());
			}
			
		} else if (member instanceof Robot) {
			Robot robot = (Robot) member;

			// Get base result for job modifier.
			RobotJob job = robot.getBotMind().getRobotJob();
			if (job != null) {
				result = job.getJoinMissionProbabilityModifier(this.getClass());
			}
		}
//        }

		return result;
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
	public abstract Map<Integer, Number> getResourcesNeededForRemainingMission(boolean useBuffer);

	/**
	 * Gets the number and types of equipment needed for the mission.
	 * 
	 * @param useBuffer use time buffers in estimation if true.
	 * @return map of equipment class and Integer number.
	 */
	public abstract Map<Integer, Integer> getEquipmentNeededForRemainingMission(boolean useBuffer);

	/**
	 * Time passing for mission.
	 * 
	 * @param time the amount of time passing (in millisols)
	 * @throws Exception if error during time passing.
	 */
	public void timePassing(double time) {
		// add O2 consumption here ?
	}

	/**
	 * Associate all mission members with a settlement.
	 * 
	 * @param settlement the associated settlement.
	 */
	public void associateAllMembersWithSettlement(Settlement settlement) {
		Iterator<MissionMember> i = members.iterator();
		while (i.hasNext()) {
			MissionMember member = i.next();
			member.setAssociatedSettlement(settlement);
		}
	}

	/**
	 * Gets the current location of the mission.
	 * 
	 * @return coordinate location.
	 * @throws MissionException if error determining location.
	 */
	public final Coordinates getCurrentMissionLocation() {

		Coordinates result = null;

		if (getMembersNumber() > 0) {
			MissionMember member = (MissionMember) members.toArray()[0];
			result = member.getCoordinates();
		} else {
			StringBuilder s = new StringBuilder();
			String missionName = this.toString();
			if (missionName.toLowerCase().contains("mission")) {
				if (phase != null) {
					s.append("Anyone can do ").append(phase).append(" for a ").append(missionName).append("?");
				}
				else {
					int rand = RandomUtil.getRandomInt(2);
					String theInitial = missionName.substring(0);
					if (Conversion.isVowel(theInitial))
						s.append(EXPRESSIONS[rand]).append("an ").append(missionName).append("?");
					else
						s.append(EXPRESSIONS[rand]).append("a ").append(missionName).append("?");
				}
			}
			
			else {
				if (phase != null) {
					s.append("Anyone can do ").append(phase).append(" for a ").append(missionName).append(MISSION).append("?");
				}
				else {
					int rand = RandomUtil.getRandomInt(2);
					String theInitial = missionName.substring(0);
					if (Conversion.isVowel(theInitial))
						s.append(EXPRESSIONS[rand]).append("an ").append(missionName).append(MISSION).append("?");
					else
						s.append(EXPRESSIONS[rand]).append("a ").append(missionName).append(MISSION).append("?");
				}
			}
			
			LogConsolidated.log(logger, Level.INFO, 2000, sourceName, s.toString(), null);
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
		int result = 0;

		if (settlement == null)
			result = 0;
		// throw new NullPointerException();

		else {
			result = settlement.getInventory().findNumUnitsOfClass(EVASuit.class);

			// Leave one suit for settlement use.
			if (result > 0) {
				result--;
			}
		}
		return result;
	}

	/**
	 * Request approval from the commander of the settlement for the mission.
	 * 
	 * @param member the mission member currently performing the mission.
	 */	
	protected void requestApprovalPhase(MissionMember member) {	
		Person p = (Person)member;
		
		if (!approved && plan == null) {			
//			System.out.println(p.getName()+ "'s" + this.getDescription() + " is going to make a plan.");
			plan = new MissionPlanning(this, p.getName(), p.getRole().getType());
			
			LogConsolidated.log(logger, Level.INFO, 0, sourceName, "[" + p.getSettlement().getName() + "] " 
					+ p.getName() + " (" + p.getRole().getType() 
					+ ") is requesting approval for " + getDescription(), null);

			 Simulation.instance().getMissionManager().requestMissionApproval(plan);
		}
		
		else if (plan != null && plan.getStatus() == PlanType.NOT_APPROVED) {
			endMission(MISSION_NOT_APPROVED);
		}
		
		if (approved || plan.getStatus() == PlanType.APPROVED) {
			LogConsolidated.log(logger, Level.INFO, 0, sourceName, "[" + p.getSettlement().getName() + "] " 
					+ p.getName() + " (" + p.getRole().getType() 
					+ ")'s " + getDescription() + " is preparing to embark.", null);

			setPhaseEnded(true);
		}
	}

	/**
	 * Sets the mission plan status
	 * 
	 * @param value
	 */
	public void setApproval(boolean value) {
		approved = value;
	}
	
	/**
	 * Checks if the mission plan has been approved
	 * 
	 * @return
	 */
	public boolean isApproved() {
		return approved;
	}
	
	/**
	 * Returns the mission plan
	 * 
	 * @return {@link MissionPlanning}
	 */
	public MissionPlanning getPlan() {
		return plan;
	}
	
	/**
	 * Returns the starting member
	 * 
	 * @return {@link Person}
	 */
	public Person getStartingMember() {
		return (Person)startingMember;
	}
	
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		if (members != null) {
			members.clear();
		}
		members = null;
		missionName = null;
		if (phases != null) {
			phases.clear();
		}
		phases = null;
		phase = null;
		phaseDescription = null;
		if (listeners != null) {
			listeners.clear();
		}
		listeners = null;
	}
}