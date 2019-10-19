/**
 * Mars Simulation Project
 * MetaTask.java
 * @version 3.1.0 2017-08-30
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.utils;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.events.HistoricalEventManager;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.social.RelationshipManager;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.science.ScientificStudyManager;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * Interface for a meta task, responsible for determining task probability and
 * constructing task instances.
 */
public interface MetaTask {

	static Simulation sim = Simulation.instance();
	/** The static instance of the mars clock */
	static MarsClock marsClock = sim.getMasterClock().getMarsClock();
	/** The static instance of the event manager */
	static HistoricalEventManager eventManager = sim.getEventManager();
	/** The static instance of the relationship manager */
	static RelationshipManager relationshipManager = sim.getRelationshipManager();
	/** The static instance of the UnitManager */	
	static UnitManager unitManager = sim.getUnitManager();
	/** The static instance of the ScientificStudyManager */
	static ScientificStudyManager scientificStudyManager = sim.getScientificStudyManager();
	/** The static instance of the SurfaceFeatures */
	static SurfaceFeatures surface = sim.getMars().getSurfaceFeatures();
	/** The static instance of the MissionManager */
	static MissionManager missionManager = sim.getMissionManager();

	/**
	 * Gets the associated task name.
	 * 
	 * @return task name string.
	 */
	public String getName();

	/**
	 * Constructs an instance of the associated task.
	 * 
	 * @param person the person to perform the task.
	 * @return task instance.
	 */
	public Task constructInstance(Person person);

	public Task constructInstance(Robot robot);

	/**
	 * Gets the weighted probability value that the person might perform this task.
	 * A probability weight of zero means that the task has no chance of being
	 * performed by the person.
	 * 
	 * @param person the person to perform the task.
	 * @return weighted probability value (0 -> positive value).
	 */
	public double getProbability(Person person);

	public double getProbability(Robot robot);
}