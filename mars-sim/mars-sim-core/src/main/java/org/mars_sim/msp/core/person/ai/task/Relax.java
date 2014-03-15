/**
 * Mars Simulation Project
 * Relax.java
 * @version 3.06 2014-02-26
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;

/** 
 * The Relax class is a simple task that implements resting and doing nothing for a while.
 * The duration of the task is by default chosen randomly, up to 100 millisols.
 */
class Relax
extends Task
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(Relax.class.getName());

	// Task phase
	private static final String RELAXING = "Relaxing";

	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = -.5D;

	/** 
	 * Constructor.
	 * @param person the person to perform the task
	 */
	public Relax(Person person) {
		super("Relaxing", person, false, false, STRESS_MODIFIER, true, 10D + 
				RandomUtil.getRandomDouble(40D));

		// If person is in a settlement, try to find a place to relax.
		if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {      	
			try {
				Building recBuilding = getAvailableRecreationBuilding(person);
				if (recBuilding != null) {
					// Walk to recreation building.
					walkToRecreationBuilding(recBuilding);
				}
			}
			catch (Exception e) {
				logger.log(Level.SEVERE,"Relax.constructor(): " + e.getMessage());
				endTask();
			}
		}

		// Initialize phase
		addPhase(RELAXING);
		setPhase(RELAXING);
	}

	/** 
	 * Returns the weighted probability that a person might perform this task.
	 * It should return a 0 if there is no chance to perform this task given the person and his/her situation.
	 * @param person the person to perform the task
	 * @return the weighted probability that a person might perform this task
	 */
	public static double getProbability(Person person) {
		double result = 10D;

		// Stress modifier
		result += person.getPhysicalCondition().getStress();

		// Crowding modifier
		if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
			try {
				Building recBuilding = getAvailableRecreationBuilding(person);
				if (recBuilding != null) {
					result *= Task.getCrowdingProbabilityModifier(person, recBuilding);
					result *= Task.getRelationshipModifier(person, recBuilding);
				}
			}
			catch (Exception e) {
				logger.log(Level.SEVERE,"Relax.getProbability(): " + e.getMessage());
			}
		}

		return result;
	}

	/**
	 * Walk to recreation building.
	 * @param recreationBuilding the recreation building.
	 */
	private void walkToRecreationBuilding(Building recreationBuilding) {

		// Determine location within recreation building.
		// TODO: Use action point rather than random internal location.
		Point2D.Double buildingLoc = LocalAreaUtil.getRandomInteriorLocation(recreationBuilding);
		Point2D.Double settlementLoc = LocalAreaUtil.getLocalRelativeLocation(buildingLoc.getX(), 
				buildingLoc.getY(), recreationBuilding);

		if (Walk.canWalkAllSteps(person, settlementLoc.getX(), settlementLoc.getY(), 
				recreationBuilding)) {

			// Add subtask for walking to recreation building.
			addSubTask(new Walk(person, settlementLoc.getX(), settlementLoc.getY(), 
					recreationBuilding));
		}
		else {
			logger.fine(person.getName() + " unable to walk to recreation building " + 
					recreationBuilding.getName());
			endTask();
		}
	}

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		}
		else if (RELAXING.equals(getPhase())) {
			return relaxingPhase(time);
		}
		else {
			return time;
		}
	}

	/**
	 * Performs the relaxing phase of the task.
	 * @param time the amount of time (millisol) to perform the phase.
	 * @return the amount of time (millisol) left after performing the phase.
	 */
	private double relaxingPhase(double time) {
		// Do nothing
		return 0D; 
	}

	@Override
	protected void addExperience(double time) {
		// This task adds no experience.
	}

	/**
	 * Gets an available recreation building that the person can use.
	 * Returns null if no recreation building is currently available.
	 * @param person the person
	 * @return available recreation building
	 */
	private static Building getAvailableRecreationBuilding(Person person) {

		Building result = null;

		if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
			BuildingManager manager = person.getSettlement().getBuildingManager();
			List<Building> recreationBuildings = manager.getBuildings(BuildingFunction.RECREATION);
			recreationBuildings = BuildingManager.getNonMalfunctioningBuildings(recreationBuildings);
			recreationBuildings = BuildingManager.getLeastCrowdedBuildings(recreationBuildings);

			if (recreationBuildings.size() > 0) {
				Map<Building, Double> recreationBuildingProbs = BuildingManager.getBestRelationshipBuildings(
						person, recreationBuildings);
				result = RandomUtil.getWeightedRandomObject(recreationBuildingProbs);
			}
		}

		return result;
	}

	@Override
	public int getEffectiveSkillLevel() {
		return 0;	
	}

	@Override
	public List<SkillType> getAssociatedSkills() {
		List<SkillType> results = new ArrayList<SkillType>(0);
		return results;
	}
}