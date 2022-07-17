/*
 * Mars Simulation Project
 * Read.java
 * @date 2022-07-16
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
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
	private static final double STRESS_MODIFIER = -.1D;
	
	/** The selected skill type for this reading session. */
	private SkillType selectedSkill;
	
	/**
	 * Constructor. This is an effort-driven task.
	 *
	 * @param person the person performing the task.
	 */
	public Read(Person person) {
		// Use Task constructor. Skill is set later
		super(NAME, person, true, false, STRESS_MODIFIER, RandomUtil.getRandomInt(5, 20));

		if (person.isInSettlement() || person.isInVehicle()) {

			int score = person.getPreference().getPreferenceScore(new ReadMeta());
			// Modify the duration based on the preference score
			setDuration(getDuration() + score);
			// Factor in a person's preference for the new stress modifier
			setStressModifier(- score / 10D + STRESS_MODIFIER);

			// Set the boolean to true so that it won't be done again today
			// person.getPreference().setTaskStatus(this, false);

			if (person.isInSettlement()) {

				int rand = RandomUtil.getRandomInt(2);

				if (rand == 0) {
					// Find a dining place
					Building dining = EatDrink.getAvailableDiningBuilding(person, false);
					if (dining != null) {
						walkToActivitySpotInBuilding(dining, FunctionType.DINING, true);
					}
					else {
						// Go back to his quarters
						Building quarters = person.getQuarters();
						if (quarters != null) {
							walkToBed(quarters, person, true);
						}
					}
				}

				else if (rand == 1) {
					Building rec = getAvailableRecreationBuilding(person);
					if (rec != null) {
						walkToActivitySpotInBuilding(rec, FunctionType.RECREATION, true);
					}
					else {
						// Go back to his quarters
						Building quarters = person.getQuarters();
						if (quarters != null) {
							walkToBed(quarters, person, true);
						}
					}
				}

				else {
					// Go back to his quarters
					Building quarters = person.getQuarters();
					if (quarters != null) {
						walkToBed(quarters, person, true);
					}
				}
			}

			else if (person.isInVehicle()) {
				// If person is in rover, walk to passenger activity spot.
				if (person.getVehicle() instanceof Rover) {
					walkToPassengerActivitySpotInRover((Rover) person.getVehicle(), true);
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

	/**
	 * Performs reading phase.
	 *
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double reading(double time) {

		// Reading serves to improve skill
		addExperience(time);
		
		return 0D;
	}
	
	@Override
	protected void addExperience(double time) {
        // Experience points adjusted by person's "Experience Aptitude" attribute.
        NaturalAttributeManager nManager = person.getNaturalAttributeManager();
        int aptitude = nManager.getAttribute(NaturalAttributeType.ACADEMIC_APTITUDE);

    	// Pick one skill randomly to improve upon
        if (selectedSkill == null)
        	selectedSkill = person.getSkillManager().getARandomSkillType();
        
    	// Display reading on a particular subject (skill type)
		setDescription(Msg.getString("Task.description.read.detail", selectedSkill.getName()));//$NON-NLS-1$
		
		double learned = 2 * time * (aptitude / 100D) * RandomUtil.getRandomDouble(1);

		person.getSkillManager().addExperience(selectedSkill, learned, time);

	}
}
