/**
 * Mars Simulation Project
 * MetaMission.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.science.ScientificStudyManager;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * Interface for a meta mission, responsible for determining mission probability
 * and constructing mission instances.
 */
public interface MetaMission {

    public static final double LIMIT = 200D;
    
	static Simulation sim = Simulation.instance();
    static MissionManager missionManager = sim.getMissionManager();
    static MarsClock marsClock = sim.getMasterClock().getMarsClock();
    static ScientificStudyManager studyManager = sim.getScientificStudyManager();
	
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
	 * Check the suitability for this Person to be the leader. It currently checks their Job
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
	 * @return weighted probability value (0 -> positive value).
	 */
	public double getProbability(Person person);

	public double getProbability(Robot robot);
}
