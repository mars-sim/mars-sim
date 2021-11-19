/*
 * Mars Simulation Project
 * MeteorologyStudyFieldWork.java.java
 * @date 2021-10-12
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.equipment.Container;
import org.mars_sim.msp.core.equipment.ContainerUtil;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.MeteorologyFieldStudy;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * A task for the EVA operation of performing meteorology field work at a
 * research site for a scientific study.
 */
public class MeteorologyStudyFieldWork extends ScientificStudyFieldWork implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(MeteorologyStudyFieldWork.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.meteorologyFieldWork"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase FIELD_WORK = new TaskPhase(Msg.getString("Task.phase.fieldWork.meteorology")); //$NON-NLS-1$

	// Static members
	private static final double AVERAGE_ROCKS_COLLECTED_SITE = 40 + RandomUtil.getRandomDouble(20);
	public static final double AVERAGE_ROCKS_MASS = .5D + RandomUtil.getRandomDouble(.5);

	// Data members
	private double totalCollected = 0;
	private double numSamplesCollected = AVERAGE_ROCKS_COLLECTED_SITE / AVERAGE_ROCKS_MASS;
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
		if (totalCollected < AVERAGE_ROCKS_COLLECTED_SITE)
			collectRocks(time);
		else {
			if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
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
			logger.info(person, 10_000, "collectRockSamples::probability: " + probability);
			
			if (RandomUtil.getRandomDouble(1.0D) <= chance * time) {
				Container box = person.findContainer(EquipmentType.SPECIMEN_BOX, false, -1);
				int rockSampleId = box.getResource();
				if (rockSampleId == -1) {
					// Box is empty so choose at random
					int randomNum = RandomUtil.getRandomInt(((ResourceUtil.rockIDs).length) - 1);
					rockSampleId = ResourceUtil.rockIDs[randomNum];
				}
				logger.info(person, 10_000, "collectRockSamples::randomRock: " + rockSampleId);
				
		        
				double mass = RandomUtil.getRandomDouble(AVERAGE_ROCKS_MASS * 2D);
				double cap = box.getAmountResourceRemainingCapacity(rockSampleId);
				if (mass < cap) {
					double excess = box.storeAmountResource(rockSampleId, mass);
					totalCollected += mass - excess;
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
													ResourceUtil.rockSamplesID);
		if (container != null) {
			return container.transfer(person);
		}
		return false;
	}
}
