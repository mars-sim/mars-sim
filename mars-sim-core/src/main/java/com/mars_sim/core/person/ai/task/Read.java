/*
 * Mars Simulation Project
 * Read.java
 * @date 2022-07-16
 * @author Manny Kung
 */
package com.mars_sim.core.person.ai.task;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact.PhysicalEffort;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.Rover;

/**
 * The Read class is the task of reading
 */
public class Read extends Task {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	/** Task name */
	private static final String NAME = Msg.getString("Task.description.read"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase READING = new TaskPhase(Msg.getString("Task.phase.reading")); //$NON-NLS-1$

	private static final FunctionType[] LOCATIONS_WIDE = {FunctionType.DINING, FunctionType.RECREATION};
	private static final FunctionType[] LOCATIONS_SMALL = {FunctionType.RECREATION};
	private static final FunctionType[] LOCATIONS_EMPTY = {};

	private SkillType selectedSkill;
	
	/**
	 * Factory method to create a Read task for a Person. This will select a Skill to read about.
	 * @param p
	 * @return
	 */
	public static Read createTask(Person p) {
		var selected = p.getSkillManager().getARandomSkillType();
		return new Read(p, selected);
	}

	/**
	 * Constructor. This is an effort-driven task.
	 *
	 * @param person the person performing the task.
	 */
	private Read(Person person, SkillType reading) {
		// Use Task constructor. Skill is set later
		super(NAME, person, false, createImpact(reading), RandomUtil.getRandomInt(10, 40));
		this.selectedSkill = reading;
		
		setDescription(Msg.getString("Task.description.read.detail", reading.getName()));

		if (!person.isInside()) {
			endTask();
			return;
		}

		if (person.isInSettlement()) {
			boolean walkDone = false;
			int rand = RandomUtil.getRandomInt(2);
			FunctionType [] locations = switch(rand) {
				case 0 -> LOCATIONS_WIDE;
				case 1 -> LOCATIONS_SMALL;
				default -> LOCATIONS_EMPTY;
			};
			
			boolean anyZone = false;
			int zoneRand = RandomUtil.getRandomInt(9);
			if (zoneRand == 9) {
				// 90% same zone; 10% any zones (including other zones)
				anyZone = true;
			}
			
			// Choose a building in order
			for (var ft : locations) {
				Building b = BuildingManager.getAvailableFunctionBuilding(person, ft, anyZone);
				if (b != null) {
					walkDone = walkToActivitySpotInBuilding(b, ft, true);
					if (walkDone) {
						break;
					}
				}
			}

			if (!walkDone) {
				// Go back to his bed
				walkToBed(person, true);
			}
		}

		else if (person.isInVehicle()) {
			// If person is in rover, walk to passenger activity spot.
			if (person.getVehicle() instanceof Rover rover) {
				walkToPassengerActivitySpotInRover(rover, true);
			} else {
				// Walk to random location.
				walkToRandomLocation(true);
			}
		}

		// Initialize phase
		setPhase(READING);
	}
	
	/**
	 * Creates a custom impact for leaning a certain skill. This has a fixed effective skill
	 * because the skill level of the target skill cannot influence the ability to learn.
	 * 
	 * @param reading
	 * @return
	 */
	private static ExperienceImpact createImpact(SkillType reading) {
		return new ExperienceImpact(0.5D,
						NaturalAttributeType.ACADEMIC_APTITUDE, PhysicalEffort.NONE,
						-0.1D, reading) {

							private static final long serialVersionUID = 1L;

							@Override
							public int getEffectiveSkillLevel(Worker w) {
								return 2;
							}
						};
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

	/**
	 * Waht skill is being read
	 * @return Selected skill
	 */
	public SkillType getReading() {
		return selectedSkill;
	}
}
