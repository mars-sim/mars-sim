/**
 * Mars Simulation Project
 * Mission.java
 * @version 3.07 2015-03-01
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
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ShiftType;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.social.RelationshipManager;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.resource.Resource;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.ai.job.RobotJob;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.tool.Conversion;

/**
 * The Mission class represents a large multi-person task
 * There is at most one instance of a mission per person.
 * A Mission may have one or more people associated with it.
 */
public abstract class Mission
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static Logger logger = Logger.getLogger(Mission.class.getName());
	// Global mission identifier
	private static int missionIdentifer = 0;
	
	public static String SUCCESSFULLY_ENDED_CONSTRUCTION = "Successfully ended construction";
	public static String SUCCESSFULLY_DISEMBARKED = "Successfully disembarked";
	public static String USER_ABORTED_MISSION = "User aborted mission";
	public static String UNREPAIRABLE_MALFUNCTION = "unrepairable malfunction";
	public static String NO_RESERVABLE_VEHICLES = "No reservable vehicles";
	public static String NOT_ENOUGH_RESOURCES_TO_CONTINUE = "Not enough resources to continue";
	public static String NO_EMERGENCY_SETTLEMENT_DESTINATION_FOUND = "No emergency settlement destination found.";
	
	// Unique identifier
	private int identifier;
	
	// Data members
	/** Mission members. */
//	private Collection<Person> people;
//	private Collection<Robot> robots;
	private Collection<MissionMember> members;
	/** Name of mission. */
	private String name;
	/** Description of the mission. */
	private String description;
	/** The minimum number of members for mission. */
//	private int minPeople;
//	private int minRobots;
	private int minMembers;
	/** True if mission is completed. */
	private boolean done;
	/** A collection of the mission's phases. */
	private Collection<MissionPhase> phases;
	/** The current phase of the mission. */
	private MissionPhase phase;
	/** The description of the current phase of operation. */
	private String phaseDescription;
	/** Has the current phase ended? */
	private boolean phaseEnded;
	/** The number of people that can be in the mission. */
	private int missionCapacity;
	/** Mission listeners. */
	private transient List<MissionListener> listeners;


	/**
	 * Must be synchronised to prevent duplicate ids being assigned via different threads.
	 * @return
	 */
	private static synchronized int getNextIdentifier() {
		return missionIdentifer++;
	}
	
	public Mission(String name, MissionMember startingMember, int minMembers) {
		// Initialize data members
		this.identifier = getNextIdentifier();
		this.name = name;
		description = name;
//		people = new ConcurrentLinkedQueue<Person>();
//		robots = new ConcurrentLinkedQueue<Robot>();
		members = new ConcurrentLinkedQueue<MissionMember>();
		done = false;
		phase = null;
		phaseDescription = null;
		phases = new ArrayList<MissionPhase>();
		phaseEnded = false;
//		this.minPeople = minPeople;
		this.minMembers = minMembers;
		missionCapacity = Integer.MAX_VALUE;
		listeners = Collections.synchronizedList(new ArrayList<MissionListener>());

		// Created mission starting event.
		HistoricalEvent newEvent = null;

		newEvent = new MissionHistoricalEvent(startingMember, this, EventType.MISSION_START);

        Simulation.instance().getEventManager().registerNewEvent(newEvent);

        // Log mission starting.
        logger.info(description + " started by " + startingMember.getName() + " at "  + startingMember.getSettlement());

        // Add starting member to mission.
        // 2015-11-01 Temporarily set the shift type to none during the mission
        startingMember.setMission(this);
        if (startingMember instanceof Person)
        	startingMember.setShiftType(ShiftType.ON_CALL);

	}


	public int getIdentifier() {
		return identifier;
	}
	
	/**
	 * Generate the type from the Class name. Not ideal.
	 * @return
	 */
	public String getType() {
		return getClass().getSimpleName();
	}
	
	/**
	 * Adds a listener.
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
	 * @param updateType the update type.
	 */
	protected final void fireMissionUpdate(MissionEventType updateType) {
		fireMissionUpdate(updateType, null);
	}

	/**
	 * Fire a mission update event.
	 * @param addMemberEvent the update type.
	 * @param target the event target or null if none.
	 */
	protected final void fireMissionUpdate(MissionEventType addMemberEvent, Object target) {
		if (listeners == null) listeners = Collections.synchronizedList(new ArrayList<MissionListener>());
		synchronized(listeners) {
			Iterator<MissionListener> i = listeners.iterator();
			while (i.hasNext()) i.next().missionUpdate(
					new MissionEvent(this, addMemberEvent, target));
		}
	}

	/**
	 * Gets the string representation of this mission.
	 */
	public String toString() {
		return description;
	}

//	/**
//	 * Adds a person to the mission.
//	 * @param person to be added
//	 */
//	public final void addPerson(Person person) {
//		if (!people.contains(person)) {
//			people.add(person);
//
//			// Creating mission joining event.
//			HistoricalEvent newEvent = new MissionHistoricalEvent(person, this, EventType.MISSION_JOINING);
//			Simulation.instance().getEventManager().registerNewEvent(newEvent);
//
//			fireMissionUpdate(MissionEventType.ADD_MEMBER_EVENT, person);
//
//			logger.finer(person.getName() + " added to mission: " + name);
//		}
//	}
//
//	/**
//	 * Adds a robot to the mission.
//	 * @param robot to be added
//	 */
//	public final void addRobot(Robot robot) {
//		if (!robots.contains(robot)) {
//			robots.add(robot);
//
//			// Creating mission joining event.
//			HistoricalEvent newEvent = new MissionHistoricalEvent(robot, this, EventType.MISSION_JOINING);
//			Simulation.instance().getEventManager().registerNewEvent(newEvent);
//
//			fireMissionUpdate(MissionEventType.ADD_MEMBER_EVENT, robot);
//
//			logger.finer(robot.getName() + " added to mission: " + name);
//		}
//	}

	public final void addMember(MissionMember member) {
	    if (!members.contains(member)) {
	        members.add(member);

	        // Creating mission joining event.
            HistoricalEvent newEvent = new MissionHistoricalEvent(member, this, EventType.MISSION_JOINING);
            Simulation.instance().getEventManager().registerNewEvent(newEvent);

            fireMissionUpdate(MissionEventType.ADD_MEMBER_EVENT, member);

            logger.finer(member.getName() + " added to mission: " + name);
	    }
	}

//	/**
//	 * Removes a person from the mission.
//	 * @param person to be removed
//	 */
//	public final void removePerson(Person person) {
//		if (people.contains(person)) {
//			people.remove(person);
//
//			// Creating missing finishing event.
//			HistoricalEvent newEvent = new MissionHistoricalEvent(person, this, EventType.MISSION_FINISH);
//			Simulation.instance().getEventManager().registerNewEvent(newEvent);
//
//			fireMissionUpdate(MissionEventType.REMOVE_MEMBER_EVENT, person);
//
//			if ((people.size() == 0) && !done) {
//				endMission("Not enough members.");
//			}
//
//			logger.finer(person.getName() + " removed from mission: " + name);
//		}
//	}
//
//
//	/**
//	 * Removes a person from the mission.
//	 * @param person to be removed
//	 */
//	public final void removeRobot(Robot robot) {
//		if (robots.contains(robot)) {
//			robots.remove(robot);
//
//			// Creating missing finishing event.
//			HistoricalEvent newEvent = new MissionHistoricalEvent(robot, this, EventType.MISSION_FINISH);
//			Simulation.instance().getEventManager().registerNewEvent(newEvent);
//
//			fireMissionUpdate(MissionEventType.REMOVE_MEMBER_EVENT, robot);
//
//			if ((robots.size() == 0) && !done) {
//				endMission("Not enough members.");
//			}
//
//			logger.finer(robot.getName() + " removed from mission: " + name);
//		}
//	}

	/**
     * Removes a member from the mission.
     * @param member to be removed
     */
    public final void removeMember(MissionMember member) {
    	if (members.contains(member)) {
            members.remove(member);
			//logger.info("done removing " + member);

            // Creating missing finishing event.
            //HistoricalEvent newEvent = new MissionHistoricalEvent(member, this, EventType.MISSION_FINISH);
            Simulation.instance().getEventManager().registerNewEvent(new MissionHistoricalEvent(member, this, EventType.MISSION_FINISH));
            fireMissionUpdate(MissionEventType.REMOVE_MEMBER_EVENT, member);

        	if ((members.size() == 0) && !done) {
            	endMission("Not enough members.");
        	}
        	
            // 2015-11-01 Added codes in reassigning a work shift
            if (member instanceof Person) {
            	Person person = (Person) member;
            	person.getMind().setMission(null);
            	
            	ShiftType shift = null;
            	//System.out.println("A mission was ended. Calling removeMember() in Mission.java.   Name : " + person.getName() + "   Settlement : " + person.getSettlement());
            	if (person.getSettlement() != null) {
            		shift = person.getSettlement().getAEmptyWorkShift(-1);
            		person.setShiftType(shift);
            	}
            	else if (person.getVehicle() != null)
            		if (person.getVehicle().getSettlement() != null){
            		shift = person.getVehicle().getSettlement().getAEmptyWorkShift(-1);
            		person.setShiftType(shift);
            	}

            }

            //logger.fine(member.getName() + " removed from mission : " + name);
        }
    }

    /**
     * Determines if a mission includes the given member.
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
     * @return number of members.
     */
    public final int getMembersNumber() {
        return members.size();
    }

	/**
	 * Gets the number of people in the mission.
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
     * @return minimum number of members
     */
    public final int getMinMembers() {
        return minMembers;
    }

//	/**
//	 * Gets the minimum number of people required for mission.
//	 * @return minimum number of people
//	 */
//	public final int getMinPeople() {
//		return minPeople;
//	}

    /**
     * Sets the minimum number of members required for a mission.
     * @param minMembers minimum number of members
     */
    protected final void setMinMembers(int minMembers) {
        this.minMembers = minMembers;
        fireMissionUpdate(MissionEventType.MIN_MEMBERS_EVENT, minMembers);
    }

//	/**
//	 * Sets the minimum number of people required for a mission.
//	 * @param minPeople minimum number of people
//	 */
//	protected final void setMinPeople(int minPeople) {
//		this.minPeople = minPeople;
//		fireMissionUpdate(MissionEventType.MIN_PEOPLE_EVENT, minPeople);
//	}

    /**
     * Gets a collection of the members in the mission.
     * @return collection of members
     */
    public final Collection<MissionMember> getMembers() {
        return new ConcurrentLinkedQueue<MissionMember>(members);
    }

	/**
	 * Gets a collection of the people in the mission.
	 * @return collection of people
	 */
	public final Collection<Person> getPeople() {
		Collection<MissionMember> members = getMembers();
		Collection<Person> people = new ConcurrentLinkedQueue<Person>();
		//Collection<Person> people = members.stream()
	    //.filter(p -> p instanceof Person).collect(Collectors.toList());
		
		Iterator<MissionMember> i = members.iterator();
	    while (i.hasNext()) {
	    	MissionMember m = i.next();
	        if (m instanceof Person) {
	            people.add((Person) m);
	        }
	    }
	    
	    return people;
		//return new ConcurrentLinkedQueue<Person>(people);
	}
	
//	/**
//	 * Determines if a mission includes the given robot.
//	 * @param robot to be checked
//	 * @return true if robot is member of mission
//	 */
//	public final boolean hasRobot(Robot robot) {
//		return robots.contains(robot);
//	}

//	/**
//	 * Gets the number of robots in the mission.
//	 * @return number of robots
//	 */
//	public final int getRobotsNumber() {
//		return robots.size();
//	}

//	/**
//	 * Gets the minimum number of robots required for mission.
//	 * @return minimum number of robots
//	 */
//	public final int getMinRobots() {
//		return minRobots;
//	}

//	/**
//	 * Sets the minimum number of robots required for a mission.
//	 * @param minRobots minimum robots of people
//	 */
//	protected final void setMinRobots(int minRobots) {
//		this.minRobots = minRobots;
//		fireMissionUpdate(MissionEventType.MIN_ROBOTS_EVENT, minRobots);
//	}

//	/**
//	 * Gets a collection of the robots in the mission.
//	 * @return collection of robots
//	 */
//	public final Collection<Robot> getRobots() {
//		return new ConcurrentLinkedQueue<Robot>(robots);
//	}

	/**
	 * Determines if mission is completed.
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
	 * @return name of mission
	 */
	public final String getName() {
		return name;
	}

	/**
	 * Sets the name of the mission.
	 * @param name the new mission name
	 */
	protected final void setName(String name) {
		this.name = name;
		fireMissionUpdate(MissionEventType.NAME_EVENT, name);
	}

	/**
	 * Gets the mission's description.
	 * @return mission description
	 */
	public final String getDescription() {
		return description;
	}

	/**
	 * Sets the mission's description.
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
	 * @return phase
	 */
	public final MissionPhase getPhase() {
		return phase;
	}

	/**
	 * Sets the mission phase.
	 * @param newPhase the new mission phase.
	 * @throws MissionException if newPhase is not in the mission's collection of phases.
	 */
	protected final void setPhase(MissionPhase newPhase) {
		if (newPhase == null) {
		    throw new IllegalArgumentException("newPhase is null");
		}
		else if (phases.contains(newPhase)) {
			phase = newPhase;
			setPhaseEnded(false);
			phaseDescription = null;
			fireMissionUpdate(MissionEventType.PHASE_EVENT, newPhase);
		}
		else {
			throw new IllegalStateException(phase + " : newPhase: " + newPhase + " is not a valid phase for this mission.");
		}
	}

	/**
	 * Adds a phase to the mission's collection of phases.
	 * @param newPhase the new phase to add.
	 */
	public final void addPhase(MissionPhase newPhase) {
		if (newPhase == null) {
			throw new IllegalArgumentException("newPhase is null");
		}
		else if (!phases.contains(newPhase)) {
			phases.add(newPhase);
		}
	}

	/**
	 * Gets the description of the current phase.
	 * @return phase description.
	 */
	public final String getPhaseDescription() {
		if (phaseDescription != null) {
			return Conversion.capitalize(phaseDescription);
		}
		else if (phase != null){
			return Conversion.capitalize(phase.toString());
		}
		else
			return "";
	}

	/**
	 * Sets the description of the current phase.
	 * @param description the phase description.
	 */
	protected final void setPhaseDescription(String description) {
		phaseDescription = description;
		fireMissionUpdate(MissionEventType.PHASE_DESCRIPTION_EVENT, description);
	}

	/**
     * Performs the mission.
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

//	/**
//	 * Performs the mission.
//	 * @param person the person performing the mission.
//	 * @throws MissionException if problem performing the mission.
//	 */
//	public void performMission(Person person) {
//
//		// If current phase is over, decide what to do next.
//		if (phaseEnded) {
//			determineNewPhase();
//		}
//
//		// Perform phase.
//		if (!done) {
//			performPhase(person);
//		}
//	}


//	/**
//	 * Performs the mission.
//	 * @param robot the robot performing the mission.
//	 * @throws MissionException if problem performing the mission.
//	 */
//	public void performMission(Robot robot) {
//
//		// If current phase is over, decide what to do next.
//		if (phaseEnded) {
//			determineNewPhase();
//		}
//
//		// Perform phase.
//		if (!done) {
//			performPhase(robot);
//		}
//	}

	/**
	 * Determines a new phase for the mission when the current phase has ended.
	 * @throws MissionException if problem setting a new phase.
	 */
	protected abstract void determineNewPhase() ;

	/**
     * The member performs the current phase of the mission.
     * @param member the member performing the phase.
     */
    protected void performPhase(MissionMember member) {
        if (phase == null) {
            endMission("Current mission phase is null.");
        }
    }

//	/**
//	 * The person performs the current phase of the mission.
//	 * @param person the person performing the phase.
//	 * @throws MissionException if problem performing the phase.
//	 */
//	protected void performPhase(Person person) {
//		if (phase == null) {
//			endMission("Current mission phase is null.");
//		}
//	}
//
//	/**
//	 * The robot performs the current phase of the mission.
//	 * @param robot the robot performing the phase.
//	 * @throws MissionException if problem performing the phase.
//	 */
//	protected void performPhase(Robot robot) {
//		if (phase == null) {
//			endMission("Current mission phase is null.");
//		}
//	}

	/**
	 * Gets the mission capacity for participating people.
	 * @return mission capacity
	 */
	public final int getMissionCapacity() {
		return missionCapacity;
	}

	/**
	 * Sets the mission capacity to a given value.
	 * @param newCapacity the new mission capacity
	 */
	protected final void setMissionCapacity(int newCapacity) {
		missionCapacity = newCapacity;
		fireMissionUpdate(MissionEventType.CAPACITY_EVENT, newCapacity);
	}

	/**
	 * Finalizes the mission.
	 * String reason Reason for ending mission.
	 * Mission can override this to perform necessary finalizing operations.
	 */
	public void endMission(String reason) {
		//logger.info("Mission's endMission() is in " + Thread.currentThread().getName() + " Thread");
		//logger.info("Reason : " + reason);
		
		if (!done & reason.equals(SUCCESSFULLY_ENDED_CONSTRUCTION) // Note: !done is very important to keep !
				|| reason.equals(SUCCESSFULLY_DISEMBARKED)
				|| reason.equals(USER_ABORTED_MISSION)) {
			done = true; // Note: done = true is very important to keep !
			fireMissionUpdate(MissionEventType.END_MISSION_EVENT);
			//logger.info("done firing End_Mission_Event");

			if (members != null) {
			    Object[] p = members.toArray();
                for (Object aP : p) {
                    removeMember((MissionMember) aP);
                }
			}

			//logger.info(description + " ending at the " + phase + " phase due to " + reason);
		}
	}

	/**
	 * Adds a new task for a person in the mission.
	 * Task may be not assigned if it is effort-driven and person is too ill
	 * to perform it.
	 * @param person the person to assign to the task
	 * @param task the new task to be assigned
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
     * Adds a new task for a robot in the mission.
     * Task may be not assigned if the robot has a malfunction.
     * @param robot the robot to assign to the task
     * @param task the new task to be assigned
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
			robot.getBotMind().getTaskManager().addTask(task);
		}

		return canPerformTask;
	}

	/**
	 * Checks to see if any of the people in the mission have any dangerous medical
	 * problems that require treatment at a settlement.
	 * Also any environmental problems, such as suffocation.
	 * @return true if dangerous medical problems
	 */
	protected final boolean hasDangerousMedicalProblems() {
		boolean result = false;
		Iterator<MissionMember> i = members.iterator();
		while (i.hasNext()) {
		    MissionMember member = i.next();
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
	 * problems that require treatment at a settlement.
	 * Also any environmental problems, such as suffocation.
	 * @return true if all have dangerous medical problems
	 */
	protected final boolean hasDangerousMedicalProblemsAllCrew() {
		boolean result = true;
		Iterator<MissionMember> i = members.iterator();
		while (i.hasNext()) {
		    MissionMember member = i.next();
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
	 * @return true if emergency.
	 */
	protected boolean hasEmergency() {
		return hasDangerousMedicalProblems();
	}

	/**
	 * Checks if the mission has an emergency situation affecting all the crew.
	 * @return true if emergency affecting all.
	 */
	protected boolean hasEmergencyAllCrew() {
		return hasDangerousMedicalProblemsAllCrew();
	}

	/**
     * Recruits new members into the mission.
     * @param startingMember the mission member starting the mission.
     */
    protected void recruitMembersForMission(MissionMember startingMember) {

        // Get all people qualified for the mission.
        Collection<Person> qualifiedPeople = new ConcurrentLinkedQueue<Person>();
        Iterator <Person> i = Simulation.instance().getUnitManager().getPeople().iterator();
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
            }
            else {
                break;
            }
        }

        // Recruit robots qualified for the mission.
        Iterator <Robot> k = Simulation.instance().getUnitManager().getRobots().iterator();
        while (k.hasNext() && (getMembersNumber() < missionCapacity)) {
            Robot robot = k.next();
            if (isCapableOfMission(robot)) {
                robot.setMission(this);
            }
        }

        if (getMembersNumber() < minMembers) {
            endMission("Not enough members");
        }
    }

//	/**
//	 * Recruits new people into the mission.
//	 * @param startingPerson the person starting the mission.
//	 */
//	protected void recruitPeopleForMission(Person startingPerson) {
//
//		int count = 0;
//		while (count < 4) {
//			count++;
//
//			// Get all people qualified for the mission.
//			Collection<Person> qualifiedPeople = new ConcurrentLinkedQueue<Person>();
//			Iterator <Person> i = Simulation.instance().getUnitManager().getPeople().iterator();
//			while (i.hasNext()) {
//				Person person = i.next();
//				if (isCapableOfMission(person)) {
//					qualifiedPeople.add(person);
//				}
//			}
//
//			// Recruit the most qualified and most liked people first.
//			try {
//				while (qualifiedPeople.size() > 0) {
//					double bestPersonValue = 0D;
//					Person bestPerson = null;
//					Iterator<Person> j = qualifiedPeople.iterator();
//					while (j.hasNext() && (getPeopleNumber() < missionCapacity)) {
//						Person person = j.next();
//						// Determine the person's mission qualification.
//						double qualification = getMissionQualification(person) * 100D;
//
//						// Determine how much the recruiter likes the person.
//						RelationshipManager relationshipManager = Simulation.instance().getRelationshipManager();
//						double likability = relationshipManager.getOpinionOfPerson(startingPerson, person);
//
//						// Check if person is the best recruit.
//						double personValue = (qualification + likability) / 2D;
//						if (personValue > bestPersonValue) {
//							bestPerson = person;
//							bestPersonValue = personValue;
//						}
//					}
//
//					// Try to recruit best person available to the mission.
//					if (bestPerson != null) {
//						recruitPerson(startingPerson, bestPerson);
//						qualifiedPeople.remove(bestPerson);
//					}
//					else break;
//				}
//			}
//			catch (Exception e) {
//				e.printStackTrace(System.err);
//			}
//		}
//
//		if (getPeopleNumber() < minPeople) endMission("Not enough members");
//	}

//	protected void recruitRobotsForMission(Robot startingRobot) {
//
//		int count = 0;
//		while (count < 4) {
//			count++;
//
//			// Get all people qualified for the mission.
//			Collection<Robot> qualifiedRobots = new ConcurrentLinkedQueue<Robot>();
//			Iterator <Robot> i = Simulation.instance().getUnitManager().getRobots().iterator();
//			while (i.hasNext()) {
//				Robot robot = i.next();
//				if (isCapableOfMission(robot)) {
//					qualifiedRobots.add(robot);
//				}
//			}
//
//			// Recruit the most qualified and most liked people first.
//			try {
//				while (qualifiedRobots.size() > 0) {
//					double bestRobotValue = 0D;
//					Robot bestRobot = null;
//					Iterator<Robot> j = qualifiedRobots.iterator();
//					while (j.hasNext() && (getRobotsNumber() < missionCapacity)) {
//						Robot robot = j.next();
//						// Determine the person's mission qualification.
//						double qualification = getMissionQualification(robot) * 100D;
//
//						// Determine how much the recruiter likes the person.
//						//RelationshipManager relationshipManager = Simulation.instance().getRelationshipManager();
//						//double likability = relationshipManager.getOpinionOfPerson(startingPerson, person);
//
//						// Check if robot is the best recruit.
//						double robotValue = qualification ;
//						if (robotValue > bestRobotValue) {
//							bestRobot = robot;
//							bestRobotValue = robotValue;
//					}
//					}
//
//					// Try to recruit best person available to the mission.
//					if (bestRobot != null) {
//						recruitRobot(startingRobot, bestRobot);
//						qualifiedRobots.remove(bestRobot);
//					}
//					else break;
//				}
//			}
//			catch (Exception e) {
//				e.printStackTrace(System.err);
//			}
//		}
//
//		if (getRobotsNumber() < minRobots) endMission("Not enough members");
//	}

	/**
	 * Attempt to recruit a new person into the mission.
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
			}
			else if (recruitmentChance < 0D) {
				recruitmentChance = 0D;
			}

			if (RandomUtil.lessThanRandPercent(recruitmentChance)) {
				recruitee.setMission(this);

				if (recruitee instanceof Person) {
		            ((Person) recruitee).setShiftType(ShiftType.ON_CALL);
		        }
				// robot cannot be a recruitee
		        //else if (recruitee instanceof Robot) {
		        //    ((Robot) recruitee).getTaskSchedule().setShiftType("None");
		        //}
			}
		}
	}
//	private void recruitRobot(Robot recruiter, Robot recruitee) {
//		if (isCapableOfMission(recruitee)) {
//			// Get mission qualification modifier.
//			double qualification = getMissionQualification(recruitee) * 100D;
//
//			//RelationshipManager relationshipManager = Simulation.instance().getRelationshipManager();
//
//			// Get the recruitee's social opinion of the recruiter.
//			//double recruiterLikability = relationshipManager.getOpinionOfRobot(recruitee, recruiter);
//
//			// Get the recruitee's average opinion of all the current mission members.
//			//double groupLikability = relationshipManager.getAverageOpinionOfRobot(recruitee, people);
//
//			double recruitmentChance = qualification;
//			if (recruitmentChance > 100D) {
//				recruitmentChance = 100D;
//			}
//			else if (recruitmentChance < 0D) {
//				recruitmentChance = 0D;
//			}
//
//			if (RandomUtil.lessThanRandPercent(recruitmentChance)) {
//				recruitee.getBotMind().setMission(this);
//			}
//		}
//	}

	/**
     * Checks to see if a member is capable of joining a mission.
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
        }
        else if (member instanceof Robot) {
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

//	/**
//	 * Checks to see if a person is capable of joining a mission.
//	 * @param person the person to check.
//	 * @return true if person could join mission.
//	 */
//	protected boolean isCapableOfMission(Person person) {
//		if (person == null) throw new IllegalArgumentException("person is null");
//
//		// Make sure person isn't already on a mission.
//		if (person.getMind().getMission() == null) {
//			// Make sure person doesn't have any serious health problems.
//			if (!person.getPhysicalCondition().hasSeriousMedicalProblems()) {
//				return true;
//			}
//		}
//
//		return false;
//	}
//	protected boolean isCapableOfMission(Robot robot) {
//		if (robot == null) throw new IllegalArgumentException("robot is null");
//
//		// Make sure robot isn't already on a mission.
//		if (robot.getBotMind().getMission() == null) {
//			// Make sure robot doesn't have any serious health problems.
//			if (!robot.getPhysicalCondition().hasSeriousMedicalProblems())
//				return true;
//		}
//		return false;
//	}

    /**
     * Gets the mission qualification value for the member.
     * Member is qualified in joining the mission if the value is larger than 0.
     * The larger the qualification value, the more likely the member will be picked for the mission.
     * @param member the member to check.
     * @return mission qualification value.
     */
    protected double getMissionQualification(MissionMember member) {

        double result = 0D;

//        if (isCapableOfMission(member)) {

            if (member instanceof Person) {
                Person person = (Person) member;

                // Get base result for job modifier.
                Job job = person.getMind().getJob();
                if (job != null) {
                    result = job.getJoinMissionProbabilityModifier(this.getClass());
                }
            }
            else if (member instanceof Robot) {
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

//	/**
//	 * Gets the mission qualification value for the person.
//	 * Person is qualified and interested in joining the mission if the value is larger than 0.
//	 * The larger the qualification value, the more likely the person will be picked for the mission.
//	 * @param person the person to check.
//	 * @return mission qualification value.
//	 * @throws MissionException if error determining mission qualification.
//	 */
//	protected double getMissionQualification(Person person) {
//
//		double result = 0D;
//
//		if (isCapableOfMission(person)) {
//			// Get base result for job modifier.
//			Job job = person.getMind().getJob();
//			if (job != null) {
//				result = job.getJoinMissionProbabilityModifier(this.getClass());
//			}
//		}
//
//		return result;
//	}
//
//	protected double getMissionQualification(Robot robot) {
//
//		double result = 0D;
//
//		if (isCapableOfMission(robot)) {
//			// Get base result for job modifier.
//			RobotJob job = robot.getBotMind().getRobotJob();
//			if (job != null) {
//				result = job.getJoinMissionProbabilityModifier(this.getClass());
//			}
//		}
//
//		return result;
//	}

	/**
	 * Checks if the current phase has ended or not.
	 * @return true if phase has ended
	 */
	public final boolean getPhaseEnded() {
		return phaseEnded;
	}

	/**
	 * Sets if the current phase has ended or not.
	 * @param phaseEnded true if phase has ended
	 */
	protected final void setPhaseEnded(boolean phaseEnded) {
		this.phaseEnded = phaseEnded;
	}

	/**
	 * Gets the settlement associated with the mission.
	 * @return settlement or null if none.
	 */
	public abstract Settlement getAssociatedSettlement();

	/**
	 * Gets the number and amounts of resources needed for the mission.
	 * @param useBuffer use time buffers in estimation if true.
	 * @return map of amount and item resources and their Double amount or Integer number.
	 */
	public abstract Map<Resource, Number> getResourcesNeededForRemainingMission(boolean useBuffer);

	/**
	 * Gets the number and types of equipment needed for the mission.
	 * @param useBuffer use time buffers in estimation if true.
	 * @return map of equipment class and Integer number.
	 */
	public abstract Map<Class, Integer> getEquipmentNeededForRemainingMission(boolean useBuffer);

	/**
	 * Time passing for mission.
	 * @param time the amount of time passing (in millisols)
	 * @throws Exception if error during time passing.
	 */
	public void timePassing(double time) {
	}

	/**
	 * Associate all mission members with a settlement.
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
	 * @return coordinate location.
	 * @throws MissionException if error determining location.
	 */
	public final Coordinates getCurrentMissionLocation() {

	    Coordinates result = null;

	    if (getMembersNumber() > 0) {
	        MissionMember member = (MissionMember) members.toArray()[0];
	        result = member.getCoordinates();
	    }
		else {
		    throw new IllegalStateException(phase + " : No people or robots in the mission.");
		}

	    return result;
	}

	/**
	 * Gets the number of available EVA suits for a mission at a settlement.
	 * @param settlement the settlement to check.
	 * @return number of available suits.
	 */
	public static int getNumberAvailableEVASuitsAtSettlement(Settlement settlement) {
		int result = 0;

		if (settlement == null) 
			result = 0;
			//throw new NullPointerException();
		
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
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		if (members != null) {
			members.clear();
		}
		members = null;
		name = null;
		description = null;
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