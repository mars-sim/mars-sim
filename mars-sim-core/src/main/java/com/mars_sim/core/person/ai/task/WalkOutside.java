/*
 * Mars Simulation Project
 * WalkOutside.java
 * @date 2025-07-22
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task;

import java.util.List;
import java.util.logging.Level;

import com.mars_sim.core.equipment.EVASuit;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.person.ai.task.walk.CollisionPathFinder;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.tool.Msg;

/**
 * A subtask for walking between locations outside of a settlement or vehicle.
 */
public class WalkOutside extends Task {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(WalkOutside.class.getName());

	/** Simple Task name */
	public static final String SIMPLE_NAME = WalkOutside.class.getSimpleName();
	
	/** Task name */
	public static final String NAME = Msg.getString("Task.description.walkOutside"); //$NON-NLS-1$
	
	/** Task phases. */
	private static final TaskPhase WALKING = new TaskPhase(Msg.getString("Task.phase.walking")); //$NON-NLS-1$

	// Static members
	/** The speed factor due to walking in EVA suit. */
	private static final double EVA_MOD = .3;
	/** A very small distance (meters) for measuring how close two positions are. */
	private static final double ONE_CENTIMETER = .01; // within a centimeter
	
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = .3D;
	/** The base chance of an accident per millisol. */
	public static final double BASE_ACCIDENT_CHANCE = .001;

	/** The minimum pulse time for completing a task phase in this class.  */
	private static double minPulseTime = 0; //Math.min(standardPulseTime, MIN_PULSE_TIME);

	// Data members
	private boolean ignoreEndEVA;
	private int walkingPathIndex;

	private List<LocalPosition> walkingPath;

	/**
	 * Constructor 1.
	 *
	 * @param worker               the worker performing the task.
	 * @param start                the starting local location.
	 * @param destination		   the destination local location.
	 * @param ignoreEndEVA         ignore end EVA situations and continue walking
	 *                             task.
	 */
	public WalkOutside(Worker worker, LocalPosition start, LocalPosition destination,
			boolean ignoreEndEVA) {

		// Use Task constructor.
		super(NAME, worker, false, false, STRESS_MODIFIER, SkillType.EVA_OPERATIONS, 100D);

		// Check that the worker is currently outside a settlement or vehicle.
		if (!worker.isOutside())
			throw new IllegalStateException("WalkOutside task started when " + worker + " was " + worker.getLocationStateType());

		init(start, destination, ignoreEndEVA);
	}

	private void init(LocalPosition start, LocalPosition destination, boolean ignoreEndEVA) {
		
		// Initialize data members.
		this.ignoreEndEVA = ignoreEndEVA;

		walkingPathIndex = 1;

		// Determine walking path.
		var pathFinder = new CollisionPathFinder(worker, start);
		walkingPath = pathFinder.determineWalkingPath(destination).path();

		// Initialize task phase.
		setPhase(WALKING);
	}

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			logger.severe(worker, "Task phase is null.");
		}
		if (WALKING.equals(getPhase())) {
			return walkingPhase(time);
		} else {
			return time;
		}
	}

	/**
	 * How many waypoints are there?
	 */
	public int getNumberWayPoints() {
		return walkingPath.size();
	}
	
	
	/**
	 * Performs the walking phase of the task.
	 *
	 * @param time the amount of time (millisol) to perform the walking phase.
	 * @return the amount of time (millisol) left after performing the walking
	 *         phase.
	 */
	private double walkingPhase(double time) {
		double remainingTime = time - minPulseTime;
		double timeHours = MarsTime.HOURS_PER_MILLISOL * remainingTime;
		double speedKPH = 0;

		if (person != null) {
			// Check for accident.
			EVASuit suit = person.getSuit();
			if (suit != null) {

				// EVA operations skill modification.
				int skill = person.getSkillManager().getEffectiveSkillLevel(SkillType.EVA_OPERATIONS);
				checkForAccident(suit, time, BASE_ACCIDENT_CHANCE, skill, "EVA");
			}

			// Check for radiation exposure during the EVA operation.
			// checkForRadiation(time);
			// If there are any EVA problems, end walking outside task.
			if (!ignoreEndEVA && (hasEVAProblem(person) || EVAOperation.isGettingDark(person))) {
				endTask();
				return time;
			}
			else
				speedKPH = Walk.PERSON_WALKING_SPEED * person.getWalkSpeedMod() * EVA_MOD;
		}

		else if (robot != null) {
			speedKPH = Walk.ROBOT_WALKING_SPEED * robot.getWalkSpeedMod() * EVA_MOD;
		}

		// Determine walking distance.
		double coveredKm = speedKPH * timeHours;
		double coveredMeters = coveredKm * 1_000;
		double remainingPathDistance = getRemainingPathDistance();

		// Determine time left after walking.
		if (coveredMeters > remainingPathDistance) {
			coveredMeters = remainingPathDistance;
			
			if (speedKPH > 0) {
				double usedTime = MarsTime.convertSecondsToMillisols(coveredMeters / speedKPH * 3.6);
				remainingTime = remainingTime - usedTime;
			}
			
			if (remainingTime < 0)
				remainingTime = 0;
		}
		else {
			remainingTime = 0D; // Use all the remaining time
		}
		
		while (coveredMeters > ONE_CENTIMETER) {
			// Walk to next path location.
			LocalPosition location = walkingPath.get(walkingPathIndex);
			double distanceToLocation = worker.getPosition().getDistanceTo(location);
			
			if (coveredMeters >= distanceToLocation) {

				// Set person at next path location.
				worker.setPosition(location);

				coveredMeters -= distanceToLocation;
				
				if (walkingPath.size() > (walkingPathIndex + 1)) {
					walkingPathIndex++;
				}
			}

			else {
				// Walk in direction of next path location.

				// Determine direction
				//double direction = determineDirection(location.getX(), location.getY());
				double direction = worker.getPosition().getDirectionTo(location);
				
				// Determine person's new location at distance and direction.
				walkInDirection(direction, coveredMeters);

				// Set person at next path location.
//				worker.setPosition(location);

				coveredMeters = 0D;
			}
		}

		// If path destination is reached, end task.
		if (getRemainingPathDistance() <= ONE_CENTIMETER) {

			LocalPosition finalLocation = walkingPath.get(walkingPath.size() - 1);
			
			logger.log(worker, Level.FINER, 5000, "Finished walking to new location outside.");
			
			worker.setPosition(finalLocation);

			endTask();
		}
		
        // Warning: see GitHub issue #1039 for details on return a 
        // non-zero value from this method
//      return remainingTime;
		
        return 0;
	}

	/**
	 * Checks if there is an EVA problem for a person.
	 *
	 * @param person the person.
	 * @return true if an EVA problem.
	 */
	public boolean hasEVAProblem(Person person) {
		return EVAOperation.hasEVASuitProblem(person);
	}

	/**
	 * Walks in a given direction for a given distance.
	 *
	 * @param direction the direction (radians) of travel.
	 * @param distance  the distance (meters) to travel.
	 */
	private void walkInDirection(double direction, double distance) {
		worker.setPosition(worker.getPosition().getPosition(distance, direction) );
	}

	/**
	 * Gets the remaining path distance.
	 *
	 * @return distance (meters).
	 */
	private double getRemainingPathDistance() {

		double result = 0D;

		LocalPosition prevLoc = worker.getPosition();

		for (int x = walkingPathIndex; x < walkingPath.size(); x++) {
			LocalPosition nextLoc = walkingPath.get(x);
			double distance = prevLoc.getDistanceTo(nextLoc);
			result += distance;
			prevLoc = nextLoc;
		}

		return result;
	}

	/**
	 * Does a change of Phase for this Task generate an entry in the Task Schedule ?
	 * 
	 * @return false
	 */
	@Override
	protected boolean canRecord() {
		return false;
	}
}
