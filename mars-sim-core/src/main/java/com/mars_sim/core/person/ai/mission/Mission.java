/*
 * Mars Simulation Project
 * Mission.java
 * @date 2025-07-06
 * @author Barry Evans
 */
package com.mars_sim.core.person.ai.mission;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.mars_sim.core.MonitorableEntity;
import com.mars_sim.core.mission.MissionObjective;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.project.Stage;
import com.mars_sim.core.structure.ObjectiveType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.MarsTime;

/**
 * Represents the behave that a Mission exhibits.
 */
public interface Mission extends MonitorableEntity {

	// Mission event types
	static final String DESIGNATION_EVENT = "designation";
	static final String PHASE_EVENT = "phase";
	static final String PHASE_DESCRIPTION_EVENT = "phase description";
	static final String MIN_MEMBERS_EVENT = "minimum members";
	static final String CAPACITY_EVENT = "capacity";
	static final String ADD_MEMBER_EVENT = "add member";
	static final String REMOVE_MEMBER_EVENT = "remove member";
	static final String STARTING_SETTLEMENT_EVENT = "starting settlement";
	static final String END_MISSION_EVENT = "end mission";

	/**
	 * Aborts the mission via custom reasons. Will stop current phase.
	 * 
	 * @param reason MissionStatus Reason to abort
	 */
	void abortMission(MissionStatus reason);

	/**
	 * Aborts just the current phase, the next phase will be started.
	 */
	void abortPhase();

    /**
	 * Determines if mission is completed.
	 *
	 * @return true if mission is completed
	 */
	boolean isDone();

	/**
	 * Gets the name of the mission.
	 *
	 * @return name of mission
	 */
	String getName();

	/**
	 * Sets the Mission name.
	 * 
	 * @param name New Name.
	 */
    void setName(String name);

	/**
	 * Gets the settlement associated with the mission.
	 *
	 * @return settlement or null if none.
	 */
	Settlement getAssociatedSettlement();

    /**
	 * Gets the mission designation string. Defined only after the mission has been approved and commenced.
	 */
	String getFullMissionDesignation();

	/**
	 * Gets the mission log.
	 */
	MissionLog getLog();

    /**
     * The status flags attached to the Mission.
     */
    Set<MissionStatus> getMissionStatus();
    
    /**
	 * Gets the mission type enum.
	 *
	 * @return
	 */
	MissionType getMissionType();

	/**
	 * Gets the objectives that Mission satisfies. 
	 * 
	 * @return May be an empty set
	 */
	Set<ObjectiveType> getObjectiveSatisified();

    /**
	 * Gets the mission qualification value for the member. Member is qualified in
	 * joining the mission if the value is larger than 0. The larger the
	 * qualification value, the more likely the member will be picked for the
	 * mission.
	 *
	 * @param member the member to check
	 * @return mission qualification value
	 */
	double getMissionQualification(Worker member);

	/**
	 * Gets the stage of the Mission.
	 * 
	 * @return
	 */
	Stage getStage();

    /**
	 * Gets the description of the current phase.
	 *
	 * @return phase description
	 */
	String getPhaseDescription();

	/**
	 * Time that the current phases started
	 */
	MarsTime getPhaseStartTime();

    /**
	 * Returns the mission plan.
	 *
	 * @return {@link MissionPlanning}
	 */
	MissionPlanning getPlan();

    /**
     * Mission priority
     */
    int getPriority();

	/**
	 * Gets the mission capacity for participating people.
	 *
	 * @return mission capacity
	 */
	int getMissionCapacity();

	/**
	 * Adds a member.
	 * 
	 * @param member
	 */
	void addMember(Worker member);

	/**
	 * Removes a member from the mission.
	 *
	 * @param member to be removed
	 */
	void removeMember(Worker member);

	/**
	 * Returns a list of people and robots who have signed up for this mission.
	 * 
	 * @return
	 */
	Set<Worker> getSignup();

    /**
	 * Gets a collection of the members in the mission.
	 *
	 * @return collection of members
	 */
	Collection<Worker> getMembers();

    /**
	 * Returns the starting person.
	 *
	 * @return {@link Person}
	 */
	Person getStartingPerson();

    /**
	 * Performs the mission.
	 *
	 * @param member the member performing the mission.
	 * @return Can the member participate ?
	 */
	boolean performMission(Worker member);

	/**
	 * Gets the list of objectives for this mission.
	 * 
	 * @return
	 */
    List<MissionObjective> getObjectives();

    /**
     * Fires an entity update event.
     *
     * @param eventType the update type.
     * @param target    the event target or null if none.
     */
    void fireMissionUpdate(String eventType, Object target);
}
