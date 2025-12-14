/*
 * Mars Simulation Project
 * MeteorologyStudyFieldWork.java
 * @date 2025-10-11
 * @author Scott Davis
 */
package com.mars_sim.core.science.task;


import java.util.logging.Level;

import com.mars_sim.core.equipment.Container;
import com.mars_sim.core.equipment.ContainerUtil;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.Rover;

/**
 * A task for the EVA operation of performing meteorology field work at a
 * research site for a scientific study.
 */
public class MeteorologyStudyFieldWork extends ScientificStudyFieldWork {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(MeteorologyStudyFieldWork.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.meteorologyFieldWork"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase FIELD_WORK = new TaskPhase(Msg.getString("Task.phase.fieldWork.meteorology"),
						createPhaseImpact(SkillType.METEOROLOGY));

	// Static members
	private static final double AVERAGE_ROCK_COLLECTED_SITE = 100 + RandomUtil.getRandomDouble(-20, 20);
	public static final double AVERAGE_ROCK_MASS = 3 + RandomUtil.getRandomDouble(-1, 1);

	// Data members
	private int rockId = -1;
	  
	private double totalCollected = 0;
	private double rocksToBeCollected = AVERAGE_ROCK_COLLECTED_SITE / AVERAGE_ROCK_MASS;

	/**
	 * Constructor
	 * 
	 * @param person         the person performing the task.
	 * @param leadResearcher the researcher leading the field work.
	 * @param study          the scientific study the field work is for.
	 * @param rover          the rover
	 */
	public MeteorologyStudyFieldWork(Person person, Person leadResearcher, ScientificStudy study, Rover rover) {

		// Use EVAOperation parent constructor.
		super(NAME, FIELD_WORK, person, leadResearcher, study, rover);

		// Box is empty so choose a rock type at random
		int randomNum = RandomUtil.getRandomInt(((ResourceUtil.ROCK_IDS).length) - 1);
		rockId = ResourceUtil.ROCK_IDS[randomNum];
	
		// Take specimen containers for rock samples.
		if (!hasSpecimenContainer()) {
			boolean hasBox = takeSpecimenContainer();

			if (!hasBox) {
				// If specimen containers are not available, end task.
				logger.log(person, Level.WARNING, 5_000,
						"No more specimen box for collecting rocks.");
				endTask();
			}
			else {
				logger.info(person, 5_000, "Expected to collect " 
						+ Math.round(rocksToBeCollected * 10.0)/10.0 + " kg rocks.");
			}
		}
	}

	/**
	 * Performs rock collection for this study.
	 * 
	 * @return
	 */
	@Override
	protected boolean performStudy(double time) {
		boolean completed = false;

		// Collect rock samples.
		if (totalCollected < AVERAGE_ROCK_COLLECTED_SITE) {
			int skill = getEffectiveSkillLevel();
			collectRocks(time * skill);
		}
		else {
			endEVA("Rocks collected exceeded the set average.");
			completed = true;
		}
		
		return completed;
	}

	/**
	 * Collect rocks if chosen.
	 * 
	 * @param timeSkill time multiplying skill
	 * @throws Exception if error collecting rocks.
	 */
	private void collectRocks(double timeSkill) {
		
		if (hasSpecimenContainer()) {

			Container box = person.findContainer(EquipmentType.SPECIMEN_BOX, false, rockId);
			
			if (box != null) {
				double mass = AVERAGE_ROCK_MASS * timeSkill * RandomUtil.getRandomDouble(.5, 2);
				double cap = box.getRemainingCombinedCapacity(rockId);
				if (mass <= cap) {
					double excess = box.storeAmountResource(rockId, mass);
					// May add this in future when objective is added: mission.recordResourceCollected(rockId, mass)
					double collected = mass - excess;
					totalCollected += collected;
					logger.info(person, 10_000, "Collected " + Math.round(collected * 100.0)/100.0 
							+ " kg " + ResourceUtil.findAmountResourceName(rockId) + " into a specimen box.");
				}
				else {
					double excess = box.storeAmountResource(rockId, cap);
					// May add this in future when objective is added: mission.recordResourceCollected(rockId, cap)
					double collected = cap - excess;
					totalCollected += collected;
					endEVA("Specimen box full.");
				}
			}
			else {
				endEVA("No specimen box available for " + ResourceUtil.findAmountResourceName(rockId) + ".");
			}
		}
		else {
			endEVA("No specimen boxes available.");
		}
	}
	
	/**
	 * Checks if the person is carrying a specimen container.
	 * 
	 * @return true if carrying container.
	 */
	private boolean hasSpecimenContainer() {
		return person.containsEquipment(EquipmentType.SPECIMEN_BOX);
	}

	/**
	 * Takes the least full specimen container from the rover, if any are available.
	 * 
	 * @return true if the person receives a specimen container.
	 */
	private boolean takeSpecimenContainer() {
		Container container = ContainerUtil.findLeastFullContainer(
													getRover(), EquipmentType.SPECIMEN_BOX,
													ResourceUtil.ROCK_SAMPLES_ID);
		if (container != null) {
			return container.transfer(person);
		}
		return false;
	}
}
