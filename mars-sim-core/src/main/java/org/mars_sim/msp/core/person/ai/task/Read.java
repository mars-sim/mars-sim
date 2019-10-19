/**
 * Mars Simulation Project
 * Read.java
 * @version 3.1.0 2017-09-13
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;

import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.meta.ReadMeta;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * The Read class is the task of reading
 */
public class Read extends Task implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.read"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase READING = new TaskPhase(Msg.getString("Task.phase.reading")); //$NON-NLS-1$

	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = .1D;

	// private int randomTime;

	/**
	 * Constructor. This is an effort-driven task.
	 * 
	 * @param person the person performing the task.
	 */
	public Read(Person person) {
		// Use Task constructor.
		super(NAME, person, true, false, STRESS_MODIFIER, true, 5D);

		if (person.isInSettlement() || person.isInVehicle()) {

			boolean walkSite = false;

			int score = person.getPreference().getPreferenceScore(new ReadMeta());
			super.setDuration(5 + score);
			// Factor in a person's preference for the new stress modifier
			super.setStressModifier(score / 10D + STRESS_MODIFIER);

			// set the boolean to true so that it won't be done again today
			// person.getPreference().setTaskStatus(this, false);

			if (person.isInSettlement()) {
				// if gym is not available, go back to his quarters
				Building quarters = person.getQuarters();
				if (quarters != null) {
					walkToActivitySpotInBuilding(quarters, FunctionType.LIVING_ACCOMODATIONS, true);
					walkSite = true;
				}

			}

			if (!walkSite) {
				if (person.isInVehicle()) {
					// If person is in rover, walk to passenger activity spot.
					if (person.getVehicle() instanceof Rover) {
						walkToPassengerActivitySpotInRover((Rover) person.getVehicle(), true);
					}
				} else {
					// Walk to random location.
					walkToRandomLocation(true);
				}

			}

			setDescription(Msg.getString("Task.description.read"));

		} else {
			endTask();
		}

		// Initialize phase
		addPhase(READING);
		setPhase(READING);
	}

	/**
	 * Performs reading phase.
	 * 
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double reading(double time) {
		setDescription(Msg.getString("Task.description.read"));//$NON-NLS-1$
		return 0D;
	}

	/**
	 * Gets an available recreation building that the person can use. Returns null
	 * if no recreation building is currently available.
	 * 
	 * @param person the person
	 * @return available recreation building
	 */
	public static Building getAvailableRecreationBuilding(Person person) {

		Building result = null;

		if (person.isInSettlement()) {
			BuildingManager manager = person.getSettlement().getBuildingManager();
			List<Building> recreationBuildings = manager.getBuildings(FunctionType.RECREATION);
			recreationBuildings = BuildingManager.getNonMalfunctioningBuildings(recreationBuildings);
			recreationBuildings = BuildingManager.getLeastCrowdedBuildings(recreationBuildings);

			if (recreationBuildings.size() > 0) {
				Map<Building, Double> recreationBuildingProbs = BuildingManager.getBestRelationshipBuildings(person,
						recreationBuildings);
				result = RandomUtil.getWeightedRandomObject(recreationBuildingProbs);
			}
		}

		return result;
	}

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		} else if (READING.equals(getPhase())) {
			return reading(time);
		} else {
			return time;
		}
	}

	@Override
	protected void addExperience(double time) {
		// This task adds no experience.
	}

	@Override
	public void endTask() {
		super.endTask();
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

	@Override
	public void destroy() {
		super.destroy();

	}
}