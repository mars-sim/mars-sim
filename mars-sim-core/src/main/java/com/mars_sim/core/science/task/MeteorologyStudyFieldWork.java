/*
 * Mars Simulation Project
 * MeteorologyStudyFieldWork.java
 * @date 2023-09-17
 * @author Scott Davis
 */
package com.mars_sim.core.science.task;


import com.mars_sim.core.equipment.Container;
import com.mars_sim.core.equipment.ContainerUtil;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.mission.MeteorologyFieldStudy;
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
	private static final double AVERAGE_ROCK_COLLECTED_SITE = 100 + RandomUtil.getRandomDouble(20);
	public static final double AVERAGE_ROCK_MASS = 2D + RandomUtil.getRandomDouble(.5);

	// Data members
	private double totalCollected = 0;
	private double numSamplesCollected = AVERAGE_ROCK_COLLECTED_SITE / AVERAGE_ROCK_MASS;
	private double chance = numSamplesCollected / MeteorologyFieldStudy.FIELD_SITE_TIME;

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

		// Take specimen containers for rock samples.
		if (!hasSpecimenContainer()) {
			takeSpecimenContainer();
		}
	}

	/**
	 * Performs Rock collecting for this study.
	 * @return
	 */
	@Override
	protected boolean performStudy(double time) {
		boolean completed = false;
		
		// Collect rock samples.
		if (totalCollected < AVERAGE_ROCK_COLLECTED_SITE)
			collectRocks(time);
		else {
			endEVA("Rocks colelcted exceeded set average.");
			completed = true;
		}
		
		return completed;
	}

	/**
	 * Collect rocks if chosen.
	 * 
	 * @param time the amount of time available (millisols).
	 * @throws Exception if error collecting rocks.
	 */
	private void collectRocks(double time) {
		if (hasSpecimenContainer()) {			

			double probability = chance * time;
			logger.info(person, 10_000, "collectRock::probability: " + probability);
			
			if (RandomUtil.getRandomDouble(1.0D) <= probability) {
				// Box is empty so choose at random
				int randomNum = RandomUtil.getRandomInt(((ResourceUtil.ROCK_IDS).length) - 1);
				int rockId = ResourceUtil.ROCK_IDS[randomNum];
				// Question: should we use ROCK_SAMPLES_ID instead of rockId ?
				
				Container box = person.findContainer(EquipmentType.SPECIMEN_BOX, false, -1);
				
				if (box != null) {	
					double mass = RandomUtil.getRandomDouble(AVERAGE_ROCK_MASS / 2D, AVERAGE_ROCK_MASS * 2D);
					double cap = box.getRemainingCombinedCapacity(rockId);
					if (mass <= cap) {
						double excess = box.storeAmountResource(rockId, mass);
						totalCollected += mass - excess;
					}
				}
				else {
					var rockName = ResourceUtil.findAmountResourceName(rockId);
					logger.info(person, 10_000, "No specimen box is available for " + rockName + ".");
					endTask();
				}
			}
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
