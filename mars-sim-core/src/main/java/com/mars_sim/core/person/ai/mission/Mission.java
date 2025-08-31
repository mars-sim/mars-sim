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

import com.mars_sim.core.Entity;
import com.mars_sim.core.mission.MissionObjective;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.project.Stage;
import com.mars_sim.core.structure.ObjectiveType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.util.Snapshots;

/**
 * Represents the behave that a Mission exhibits.
 */
public interface Mission extends Entity {

	/**
	 * Aborts the mission via custom reasons. Will stop current phase.
	 *
	 * @param reason Reason to abort
	 */
	void abortMission(String reason);

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
	 * @return mission type
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
	 * @return current stage
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
	 * @param member the member to add
	 */
	void addMember(Worker member);

	/**
	 * Removes a member from the mission.
	 *
	 * @param member to be removed
	 */
	void removeMember(Worker member);

	/**
	 * Returns a set of people and robots who have signed up for this mission.
	 * (This is generally a live view managed by the Mission implementation.)
	 *
	 * @return signup set, possibly empty
	 */
	Set<Worker> getSignup();

	/**
	 * Gets a collection of the members in the mission.
	 * (This is generally a live view managed by the Mission implementation.)
	 *
	 * @return collection of members
	 */
	Collection<Worker> getMembers();

	/**
	 * Provides a CME-safe snapshot copy of current members for iteration.
	 * Use this from parallel / asynchronous code to avoid ConcurrentModificationException.
	 * Returns an immutable empty list when there are no members.
	 *
	 * @return snapshot list of members
	 */
	default List<Worker> snapshotMembers() {
		return Snapshots.list(getMembers());
	}

	/**
	 * Provides a CME-safe snapshot copy of current signups for iteration.
	 * Returns an immutable empty set when there are no signups.
	 *
	 * @return snapshot set of signups
	 */
	default Set<Worker> snapshotSignup() {
		return Snapshots.set(getSignup());
	}

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
	 * Adds a listener.
	 *
	 * @param newListener the listener to add.
	 */
	void addMissionListener(MissionListener newListener);

	/**
	 * Removes a listener.
	 *
	 * @param oldListener the listener to remove.
	 */
	void removeMissionListener(MissionListener oldListener);

	/**
	 * Get the list of objectives for this mission.
	 * @return objectives list (possibly empty)
	 */
	List<MissionObjective> getObjectives();
}
