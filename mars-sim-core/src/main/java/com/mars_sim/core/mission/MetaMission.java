/**
 * Mars Simulation Project
 * MetaMission.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package com.mars_sim.core.mission;

import java.util.Collection;
import java.util.Set;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.robot.RobotType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.core.vehicle.VehicleType;

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
	 * Get a description of this mission type. This is used in the UI to describe the mission to the user.
	 */
	String getDescription();

	/**
	 * Can this meta automatically create a mission without user intervention?
	 * If not, then the mission must be reviewed by the user before it can be created.
	 * @return true if the meta can automatically create a mission.
	 */
	boolean isAutomatic();

	/**
	 * Checks the suitability for this Person to be the leader. It currently checks their Job.
	 * 
	 * @param leader Potential leader to assess
	 * @return Score of suitability. zero means not suitable at all.
	 */
	double getLeaderSuitability(Person leader);

	/**
	 * Gauges the suitability of this worker joining a mission of this type.
	 * @param w Worker
	 * @return Score of qualification. zero means not suitable at all.
	 */
    double getWorkerSuitability(Worker w);

	/**
	 * Constructs an instance of the associated mission.
	 * 
	 * @param crew The crew for the mission, including leader and members
	 * @param needsReview Mission must be reviewed
	 * @return mission instance.
	 * @throws MissionCreationException If there is a problem creating the mission.
	 */
	Mission constructInstance(Roster crew, boolean needsReview) throws MissionCreationException;

	/**
	 * Gets the weighted probability value that the person might perform this
	 * mission. A probability weight of zero means that the mission has no chance of
	 * being performed by the person.
	 * 
	 * @param leader the person to perform the mission.
	 * @return Rating of this mission starting
	 */
	default RatingScore getProbability(Person leader) {
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

	/**
	 * Gets the population threshold for this mission. This is the minimum number of citizens that must be present in a settlement before this mission can be created.
	 * @return
	 */
	int getPopThreshold();

    /**
     * Gets the preferred vehicle types for this mission. This is optional and can be left empty.
     * By default no VehicleTypes are preferred. If a VehicleType is preferred then it will be given a higher suitability score.
     * @return Vehicle types to prefer
     */
    Set<VehicleType> getPreferredVehicle();

	/**
	 * Gets the preferred leader job types for this mission.
	 * @return Preferred leader job types
	 */
	Set<JobType> getPreferredLeaderJob();

	/**
	 * Gets the preferred worker job types for this mission.
	 * @return Preferred worker job types
	 */
    Set<JobType> getPreferredWorkerJobs();

	/**
	 * Gets the preferred robot types for this mission. This is optional and can be left empty.
	 * @return Preferred robot types
	 */
	Set<RobotType> getPreferredRobots();
}
