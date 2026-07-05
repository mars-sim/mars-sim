/**
 * Mars Simulation Project
 * MetaMission.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package com.mars_sim.core.mission;

import java.util.Collection;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.Vehicle;

/**
 * Interface for a meta mission, responsible for determining mission probability
 * and constructing mission instances.
 */
public interface MetaMission {

    public static final double LIMIT = 100D;

	// The roster for a potential mission
	record Roster(Person leader, Collection<? extends Worker> members, Vehicle vehicle) {}
	
	/**
	 * Type of Mission created by this Meta object
	 */
	MissionType getType();

	/**
	 * Gets the associated mission name.
	 * 
	 * @return mission name string.
	 */
	String getName();

	/**
	 * Checks the suitability for this Person to be the leader. It currently checks their Job.
	 * 
	 * @param person Potential leader to assess
	 * @return Score of suitability. zero means not suitable at all.
	 */
	double getLeaderSuitability(Person person);

	/**
	 * Gauges the suitability of this worker joining a mission of this type.
	 * @param w Worker
	 * @return Score of qualification. zero means not suitable at all.
	 */
    double getWorkerSuitability(Worker w);

	/**
	 * Constructs an instance of the associated mission.
	 * 
	 * @param person the person to perform the mission.
	 * @param needsReview Mission must be reviewed
	 * @return mission instance.
	 */
	@Deprecated(since = "Should be replaced with MissionFactory.constructInstance")
	default Mission constructInstance(Person person, boolean needsReview) {
		return null;
	}

	/**
	 * Constructs an instance of the associated mission.
	 * 
	 * @param crew The crew for the mission, including leader and members
	 * @param needsReview Mission must be reviewed
	 * @return mission instance.
	 */
	default Mission constructInstance(Roster crew, boolean needsReview) {
		return constructInstance(crew.leader(), needsReview);
	}

	/**
	 * Gets the weighted probability value that the person might perform this
	 * mission. A probability weight of zero means that the mission has no chance of
	 * being performed by the person.
	 * 
	 * @param person the person to perform the mission.
	 * @return Rating of this mission starting
	 */
	default RatingScore getProbability(Person person) {
		return RatingScore.ZERO_RATING;
	}

	/**
	 * Get the minimum number of members needed to perform a Mission of this style.
	 */
	int getMinimumMembers();

	/**
	 * Get the default maximum members in a mission of this style. It can be overridden locally within the actual Mission
	 */
    int getDefaultCapacity();

	/**
	 * Select the most suitable Vehicle for this mission.
	 * @param settlement the settlement to search for vehicles.
	 */
	Vehicle selectVehicle(Settlement settlement);

	/**
	 * Get the maximum number of missions of this type that can be supported by a settlement based on its population.
	 * @param numCitizens The number of citizens in the settlement
	 * @return The maximum number of missions of this type that can be supported by the settlement
	 */
    int getMaxMissions(int numCitizens);

	/**
	 * Get the minimum sol threshold for this mission. This is the minimum sol that must be reached before this mission can be created.
	 * @return minimum sol threshold
	 */
    int getSolThreshold();

}
