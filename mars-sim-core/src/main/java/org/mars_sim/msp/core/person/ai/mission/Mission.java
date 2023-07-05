/*
 * Mars Simulation Project
 * Mission.java
 * @date 2022-09-03
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

import org.mars.sim.mapdata.location.Coordinates;
import org.mars_sim.msp.core.Entity;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.core.project.Stage;
import org.mars_sim.msp.core.structure.ObjectiveType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsTime;

/**
 * Represents the behave that a Mission exhibits.
 */
public interface Mission extends Entity, Serializable {

//	/**
//	 * Aborts the mission via established reasons and/or events.
//	 * 
//	 * @param reason Reason to abort
//	 * @param event Optional type of event to create
//	 */
//	void abortMission(MissionStatus reason, EventType event);

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
	 * Returns the current mission location. 
	 * For a Vehicle Mission used the vehicles position directly.
	 */
	Coordinates getCurrentMissionLocation();

	/**
	 * Gets the settlement associated with the mission.
	 *
	 * @return settlement or null if none.
	 */
	Settlement getAssociatedSettlement();

    /**
	 * Mission designation code. Only defined once mission has started.
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
	 * Get the objectives that Mission satisfies. 
	 * @return May be an empty set.
	 */
	Set<ObjectiveType> getObjectiveSatisified();

    /**
	 * Gets the mission qualification value for the member. Member is qualified in
	 * joining the mission if the value is larger than 0. The larger the
	 * qualification value, the more likely the member will be picked for the
	 * mission.
	 *
	 * @param member the member to check.
	 * @return mission qualification value.
	 */
	double getMissionQualification(Worker member);

	/**
	 * Get the stage of the Mission
	 * @return
	 */
	Stage getStage();

    /**
	 * Gets the description of the current phase.
	 *
	 * @return phase description.
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
	 * Adds a listener.
	 *
	 * @param newListener the listener to add.
	 */
	void addMissionListener(MissionListener newListener);

	/* 
	 * Removes a listener.
	 *
	 * @param oldListener the listener to remove.
	 */
	void removeMissionListener(MissionListener oldListener);
}
