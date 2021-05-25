/**
 * Mars Simulation Project
 * MetaTask.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.utils;

import java.util.HashSet;
import java.util.Set;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.events.HistoricalEventManager;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.social.RelationshipManager;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.science.ScientificStudyManager;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * Class for a meta task, responsible for determining task probability and
 * constructing task instances.
 */
public abstract class MetaTask {
	/**
	 *  Defines the type of Worker support by this Task
	 */
	protected enum WorkerType {
		PERSON, ROBOT, BOTH;
	}
	
	/**
	 *  Defines the scope of this Task
	 */
	protected enum TaskScope {
		ANY_HOUR, WORK_HOUR, NONWORK_HOUR;
	}
	
	// TODO not all subcalssess need all these !!!!!!
	protected static Simulation sim = Simulation.instance();
	/** The static instance of the mars clock */
	protected static MarsClock marsClock = sim.getMasterClock().getMarsClock();
	/** The static instance of the event manager */
	protected static HistoricalEventManager eventManager = sim.getEventManager();
	/** The static instance of the relationship manager */
	protected static RelationshipManager relationshipManager = sim.getRelationshipManager();
	/** The static instance of the UnitManager */	
	protected static UnitManager unitManager = sim.getUnitManager();
	/** The static instance of the ScientificStudyManager */
	protected static ScientificStudyManager scientificStudyManager = sim.getScientificStudyManager();
	/** The static instance of the SurfaceFeatures */
	protected static SurfaceFeatures surface = sim.getMars().getSurfaceFeatures();
	/** The static instance of the MissionManager */
	protected static MissionManager missionManager = sim.getMissionManager();
	
	private String name;
	private WorkerType workerType;
	private TaskScope scope;
	private Set<TaskTrait> traits = new HashSet<>();
	private Set<FavoriteType> favourites = new HashSet<>();
	
	protected MetaTask(String name, WorkerType workerType, TaskScope scope) {
		super();
		this.name = name;
		this.workerType = workerType;
		this.scope = scope;
	}

	protected void addFavorite(FavoriteType fav) {
		favourites.add(fav);
	}
	
	protected void addTrait(TaskTrait trait) {
		traits.add(trait);
	}
	
	/**
	 * Gets the associated task name.
	 * 
	 * @return task name string.
	 */
	public final String getName() {
		return name;
	}

	/**
	 * What is the scope for this Task is be done.
	 */
	public final TaskScope getScope() {
		return scope;
	}

	/**
	 * What worker type is supported by this task.
	 * @return
	 */
	public final WorkerType getSupported() {
		return workerType;
	}
	
	/**
	 * Constructs an instance of the associated task. Is a Factory method and should
	 * be implemented by the subclass.
	 * Governed by the {@link #getSupported()} method.
	 * 
	 * @param person the person to perform the task.
	 * @return task instance.
	 */
	public Task constructInstance(Person person) {
		throw new UnsupportedOperationException("Can not create " + name + " for Person.");
	}

	/**
	 * Constructs an instance of the associated task. Is a Factory method and should
	 * be implemented by the subclass.
	 * Governed by the {@link #getSupported()} method.
	 * 
	 * @param person the person to perform the task.
	 * @return task instance.
	 */
	public Task constructInstance(Robot robot) {
		throw new UnsupportedOperationException("Can not create " + name + " for Robot.");
	}

	/**
	 * Gets the weighted probability value that the person might perform this task.
	 * A probability weight of zero means that the task has no chance of being
	 * performed by the person.
	 * 
	 * @param person the person to perform the task.
	 * @return weighted probability value (0 -> positive value).
	 */
	public double getProbability(Person person) {
		throw new UnsupportedOperationException("Can not calculated the probability of " + name + " for Person.");
	}

	/**
	 * Gets the weighted probability value that the person might perform this task.
	 * A probability weight of zero means that the task has no chance of being
	 * performed by the robot.
	 * 
	 * @param robot the robot to perform the task.
	 * @return weighted probability value (0 -> positive value).
	 */
	public double getProbability(Robot robot) {
		throw new UnsupportedOperationException("Can not calculated the probability of " + name + " for Robot.");
	}

	public Set<TaskTrait> getTraits() {
		return traits;		
	}

	public Set<FavoriteType> getFavourites() {
		return favourites;
	}
}
