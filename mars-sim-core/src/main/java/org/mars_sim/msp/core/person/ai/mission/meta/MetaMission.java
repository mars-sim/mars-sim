/**
 * Mars Simulation Project
 * MetaMission.java
 * @version 3.1.0 2017-09-14
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.science.ScientificStudyManager;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * Interface for a meta mission, responsible for determining mission probability
 * and constructing mission instances.
 */
public interface MetaMission {

	static Simulation sim = Simulation.instance();
    static MissionManager missionManager = sim.getMissionManager();
    static MarsClock marsClock = sim.getMasterClock().getMarsClock();
    static ScientificStudyManager studyManager = sim.getScientificStudyManager();
	
	/**
	 * Gets the associated mission name.
	 * 
	 * @return mission name string.
	 */
	public String getName();

	/**
	 * Constructs an instance of the associated mission.
	 * 
	 * @param person the person to perform the mission.
	 * @return mission instance.
	 */
	public Mission constructInstance(Person person);

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