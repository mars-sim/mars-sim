/**
 * Mars Simulation Project
 * MetaMission.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.mission.meta;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.robot.Robot;

/**
 * Interface for a meta mission, responsible for determining mission probability
 * and constructing mission instances.
 */
public interface MetaMission {

    public static final double LIMIT = 100D;
	
	/**
	 * Type of Mission created by this Meta object
	 */
	public MissionType getType();

	/**
	 * Gets the associated mission name.
	 * 
	 * @return mission name string.
	 */
	public String getName();

	/**
	 * Checks the suitability for this Person to be the leader. It currently checks their Job.
	 * 
	 * @param person
	 * @return
	 */
	public double getLeaderSuitability(Person person);

	/**
	 * Constructs an instance of the associated mission.
	 * 
	 * @param person the person to perform the mission.
	 * @param needsReview Mission must be reviewed
	 * @return mission instance.
	 */
	public Mission constructInstance(Person person, boolean needsReview);

	public Mission constructInstance(Robot robot);

	/**
	 * Gets the weighted probability value that the person might perform this
	 * mission. A probability weight of zero means that the mission has no chance of
	 * being performed by the person.
	 * 
	 * @param person the person to perform the mission.
	 * @return Rating of this mission starting
	 */
	public RatingScore getProbability(Person person);

	public double getProbability(Robot robot);
}
