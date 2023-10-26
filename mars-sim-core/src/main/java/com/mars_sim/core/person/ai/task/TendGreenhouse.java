/*
 * Mars Simulation Project
 * TendGreenhouse.java
 * @date 2023-05-26
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task;

import java.util.List;
import java.util.logging.Level;

import com.mars_sim.core.UnitType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.structure.building.function.farming.Crop;
import com.mars_sim.core.structure.building.function.farming.CropSpec;
import com.mars_sim.core.structure.building.function.farming.Farming;
import com.mars_sim.tools.Msg;
import com.mars_sim.tools.util.RandomUtil;

/**
 * The TendGreenhouse class is a task for tending the greenhouse in a
 * settlement. This is an effort driven task.
 */
public class TendGreenhouse extends Task {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(TendGreenhouse.class.getName());

	/** static members */
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = -1.1D;
	private static final double CROP_RESILIENCY = Crop.CROP_RESILIENCY;
	
	private static final String NAME = Msg.getString("Task.description.tendGreenhouse"); //$NON-NLS-1$
	
	private static final String GROW = Msg.getString("Task.description.tendGreenhouse.grow"); //$NON-NLS-1$
	private static final String GROWING_DETAIL = Msg.getString("Task.description.tendGreenhouse.grow.detail"); //$NON-NLS-1$
	private static final String DONE_GROWING = "Done with growing ";

	private static final String SEED = Msg.getString("Task.description.tendGreenhouse.seed"); //$NON-NLS-1$
	private static final String DONE_SEEDING = Msg.getString("Task.description.tendGreenhouse.seed.done"); //$NON-NLS-1$
	
	private static final String TEND = Msg.getString("Task.description.tendGreenhouse.tend"); //$NON-NLS-1$
	private static final String DONE_TENDING = Msg.getString("Task.description.tendGreenhouse.tend.done"); //$NON-NLS-1$
	
	private static final String TRANSFER = Msg.getString("Task.description.tendGreenhouse.transfer"); //$NON-NLS-1$
	
	private static final String INSPECT_DETAIL = Msg.getString("Task.description.tendGreenhouse.inspect.detail");
	
	private static final String CLEAN_DETAIL = Msg.getString("Task.description.tendGreenhouse.clean.detail");
	
	private static final String SAMPLE = Msg.getString("Task.description.tendGreenhouse.sample");
	private static final String SAMPLE_DETAIL = Msg.getString("Task.description.tendGreenhouse.sample.detail");

	
	private static final String ADDED_TO_INCUBATOR = " and added to incubator for growing tissues.";

	private static final String TISSUES_IN_LAB = " tissues in botany lab.";

	
	/** Task phases. */
	private static final TaskPhase TENDING = new TaskPhase(Msg.getString("Task.phase.tending")); //$NON-NLS-1$
	private static final TaskPhase INSPECTING = new TaskPhase(Msg.getString("Task.phase.inspecting")); //$NON-NLS-1$
	private static final TaskPhase CLEANING = new TaskPhase(Msg.getString("Task.phase.cleaning")); //$NON-NLS-1$
	private static final TaskPhase SAMPLING = new TaskPhase(Msg.getString("Task.phase.sampling")); //$NON-NLS-1$
	private static final TaskPhase TRANSFERRING_SEEDLING = new TaskPhase(Msg.getString("Task.phase.transferring")); //$NON-NLS-1$
	private static final TaskPhase GROWING_TISSUE = new TaskPhase(Msg.getString("Task.phase.growingTissue")); //$NON-NLS-1$
	
	// Data members
	/** The goal of the task at hand. */
	private String goal;
	/** The previous tended crop. */
	private String previousCropName;
	/** The greenhouse the person is tending. */
	private Farming greenhouse;
	/** The task accepted for the duration. */
	private TaskPhase acceptedTask;
	/** The crop to be worked on. */
	private Crop needyCrop;
	/** The crop specs to be selected to plant. */
	private CropSpec cropSpec;
	
	/**
	 * Constructor.
	 *
	 * @param person the person performing the task.
	 * @param farm Farm needing tender
	 */
	public TendGreenhouse(Person person, Farming greenhouse) {
		// Use Task constructor
		super(NAME, person, false, false, STRESS_MODIFIER, SkillType.BOTANY, 100D, RandomUtil.getRandomDouble(10, 50));

		if (person.isOutside()) {
			endTask();
			return;
		}

		// Get available greenhouse if any.
		this.greenhouse = greenhouse;

		// Walk to greenhouse.
		walkToTaskSpecificActivitySpotInBuilding(greenhouse.getBuilding(), FunctionType.FARMING, false);

//		logger.info(person, 20_000L, "Walked to " + greenhouse.getBuilding() + ".");
		
		// Plant a crop or tending a crop
		selectTask();
	}

	/**
	 * Constructor 2.
	 *
	 * @param robot the robot performing the task.
	 */
	public TendGreenhouse(Robot robot, Farming greenhouse) {
		// Use Task constructor
		super(NAME, robot, false, false, 0, SkillType.BOTANY, 100D, 50D);

		// Initialize data members
		if (robot.isOutside()) {
			endTask();
			return;
		}

		this.greenhouse = greenhouse;

		// Checks quickly to see if a needy crop is available	
		needyCrop = greenhouse.getNeedyCrop();
		
		if (needyCrop != null) {
			previousCropName = needyCrop.getCropName();
			// Walk to greenhouse.
			walkToTaskSpecificActivitySpotInBuilding(greenhouse.getBuilding(), FunctionType.FARMING, false);
			
//			logger.info(robot, 20_000L, "Tending in " + greenhouse.getBuilding() + ".");
			
			acceptedTask = TENDING;

			addPhase(TENDING);
			setPhase(TENDING);
		}
		else {
			acceptedTask = CLEANING;
		}
		
		// Initialize phase
		addPhase(acceptedTask);
		setPhase(acceptedTask);
	}

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		} else if (TENDING.equals(getPhase())) {
			return tendingPhase(time);
		} else if (INSPECTING.equals(getPhase())) {
			return inspectingPhase(time);
		} else if (CLEANING.equals(getPhase())) {
			return cleaningPhase(time);
		} else if (SAMPLING.equals(getPhase())) {
			return samplingPhase(time);
		} else if (TRANSFERRING_SEEDLING.equals(getPhase())) {
			return transferringSeedling(time);
		} else if (GROWING_TISSUE.equals(getPhase())) {
			return growingTissue(time);
		} else {
			return time;
		}
	}

	/**
	 * Selects a suitable task for the greenhouse.
	 */
	private void selectTask() {
		if (greenhouse.getNumCrops2Plant() > 0) {
			acceptedTask = TRANSFERRING_SEEDLING;
		}
		else {	
			// Choose the activity from the base 4 non-crop tasks then 2 slots per needy crop.
			int probability = 4 + greenhouse.getNumNeedTending() * 2;
			int rand = RandomUtil.getRandomInt(probability);

			if (rand == 0)
				acceptedTask = INSPECTING;
			else if (rand == 1)
				acceptedTask = CLEANING;
			else if (rand == 2)
				acceptedTask = SAMPLING;
			else if (rand == 3)
				acceptedTask = GROWING_TISSUE;
			else {
				needyCrop = greenhouse.getNeedyCrop();
				if (needyCrop == null) {
					// Hmm shouldn't happen
					acceptedTask = INSPECTING;
				}
				else {
					previousCropName = needyCrop.getCropName();
					acceptedTask = TENDING;
				}
			}
		}
			
		addPhase(acceptedTask);
		setPhase(acceptedTask);
	}
	
	/**
	 * Sets the description and print the log.
	 * 
	 * @param text
	 */
	private void printDescription(String text) {
		setDescription(text);
		logger.log(greenhouse.getBuilding(), worker, Level.INFO, 30_000L, text + ".");
	}
	
	/**
	 * Sets the task description for tending a specific crop for the person.
	 * 
	 * @param needyCrop
	 */
	public void setCropDescription() {
//		logger.log(greenhouse.getBuilding(), worker, Level.INFO, 30_000L, "Tending " + needyCrop.getCropName() + ".");
		setDescription(TEND + " " + previousCropName, false);
	}

	/**
	 * Sets the task description of being done with tending crops.
	 */
	public void setDescriptionCropDone() {
//		logger.log(greenhouse.getBuilding(), worker, Level.FINE, 30_000L, 
//				previousCropName + " no longer needed to be tended.");
		setDescription(DONE_TENDING + " " + previousCropName, false);
	}
	
	/**
	 * Performs the tending phase.
	 *
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double tendingPhase(double time) {
		
		double remainingTime = 0;
		
		if (isDone()) {
			return time;
		}
		
    	if (getTimeCompleted() > getDuration()) {
        	endTask();
        	return time;
    	}

		Building farmBuilding = greenhouse.getBuilding();
		
		// Check if greenhouse has malfunction.
		if (farmBuilding.getMalfunctionManager() != null && farmBuilding.getMalfunctionManager().hasMalfunction()) {
			endTask();
			return 0;
		}

		if (needyCrop == null) {
			// Select a new needy crop
			needyCrop = greenhouse.getNeedyCrop();
		}
		
		if (needyCrop == null) {
			setDescriptionCropDone();
			endTask();
			return 0;
		}
		
		previousCropName = needyCrop.getCropName();
		
		boolean needTending = needyCrop.getCurrentWorkRequired() > CROP_RESILIENCY;
		
		if (needTending) {
			remainingTime = tendCrop(time);
			return remainingTime;
		}

		setDescriptionCropDone();

		return remainingTime;
	}
	
	/**
	 * Tends a crop.
	 * 
	 * @param time
	 * @return
	 */
	private double tendCrop(double time) {
		double remainingTime = 0;
		double workTime = time;
		double mod = 0;
		
		if (worker.getUnitType() == UnitType.PERSON)
			mod = 1;
		else
			mod = .3 * RandomUtil.getRandomDouble(.85, 1.15);
		
		// Determine amount of effective work time based on "Botany" skill
		int greenhouseSkill = getEffectiveSkillLevel();
		if (greenhouseSkill == 0)
			mod *= RandomUtil.getRandomDouble(.85, 1.15);
		else
			mod *= RandomUtil.getRandomDouble(.85, 1.15) * greenhouseSkill * 1.1;
		
		double remain = greenhouse.addWork(workTime * mod, worker, needyCrop);
		
		// Calculate used time
		double usedTime = workTime - remain;
		
		if (usedTime > 0) {
			setCropDescription();

			if (remain > 0) {
				// Divided by mod to get back any leftover real time
				remainingTime = time - usedTime;
			}
			
			// Add experience
			addExperience(time);
	
			// Check for accident in greenhouse.
			checkForAccident(greenhouse.getBuilding(), time, 0.005);
		}
		else {
			setDescriptionCropDone();
		}
		
		if (!greenhouse.requiresWork(needyCrop))
			needyCrop = null;
		
		return remainingTime;
	}
	
	/**
	 * Transfers seedlings.
	 * 
	 * @param time
	 * @return
	 */
	private double transferringSeedling(double time) {
		
		double mod = 0;
		// Determine amount of effective work time based on "Botany" skill
		int greenhouseSkill = getEffectiveSkillLevel();
		if (greenhouseSkill <= 0) {
			mod *= RandomUtil.getRandomDouble(.5, 1.0);
		} else {
			mod *= RandomUtil.getRandomDouble(.5, 1.0) * greenhouseSkill * 1.2;
		}

		double workTime = time * mod;
		
		if (cropSpec == null) {
			
			cropSpec = greenhouse.selectSeedling();
			
			if (cropSpec != null) {
				printDescription(DONE_SEEDING + " " + cropSpec.getName());
			}
			else {
				// Find another task
				selectTask();
				
				return time / 2.0;
			}
			
			printDescription(TRANSFER);

			addExperience(workTime);
	
		}
		
		else if (greenhouse.getNumCrops2Plant() > 0 && getDuration() <= (getTimeCompleted() + time)) {
			greenhouse.plantSeedling(cropSpec, getTimeCompleted() + time, worker);
			printDescription(SEED + " " + cropSpec + ".");
				
			addExperience(workTime);
		}
		
		else {
			// Find another task
			selectTask();
		}
		
		return 0;
	}

	/**
	 * Grows tissue culture.
	 * 
	 * @param time
	 * @return
	 */
	private double growingTissue(double time) {

		printDescription(GROW);
		
		// Check if the lab is available
		if (!greenhouse.checkBotanyLab())  {
			endTask();
		}
	
		if (goal == null) {
			goal = greenhouse.chooseCrop2Extract(Farming.STANDARD_AMOUNT_TISSUE_CULTURE);
			if (goal != null) {
				greenhouse.getResearch().addToIncubator(goal, Farming.STANDARD_AMOUNT_TISSUE_CULTURE);	
				logger.log(greenhouse.getBuilding(), worker, Level.INFO, 20_000, 
						"Sampled " + goal + ADDED_TO_INCUBATOR);
			}
			else {
				// Can't find any matured crop to sample
//				logger.log(greenhouse.getBuilding(), worker, Level.INFO, 20_000, 
//						"Can't find matured crops to sample in botany lab.");
				// Find another task
				selectTask();
				
				return time / 2.0;
			}
		}
			
		printDescription(GROWING_DETAIL + " " + goal.toLowerCase());

		createExperienceFromSkill(time);

		if (getDuration() <= (getTimeCompleted() + time)) {		

			greenhouse.getResearch().harvestTissue(worker);

			logger.log(greenhouse.getBuilding(), worker, Level.INFO, 0, DONE_GROWING + goal + TISSUES_IN_LAB);
					
			// Reset goal to null
			goal = null;
			
			endTask();
		}

		return 0;
	}

	/**
	 * Creates experiences based on time and skill.
	 * 
	 * @param time
	 */
	private void createExperienceFromSkill(double time) {
		double mod = 0;
		// Determine amount of effective work time based on "Botany" skill
		int greenhouseSkill = getEffectiveSkillLevel();
		if (greenhouseSkill <= 0) {
			mod *= RandomUtil.getRandomDouble(.5, 1.0);
		} else {
			mod *= RandomUtil.getRandomDouble(.5, 1.0) * greenhouseSkill * 1.2;
		}

		double workTime = time * mod;
		
		addExperience(workTime);
	}
	
	/**
	 * Performs the inspecting phase.
	 *
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double inspectingPhase(double time) {
	
		if (goal == null) {
			List<String> uninspected = greenhouse.getUninspected();
			int size = uninspected.size();
	
			if (size > 0) {
				int rand = RandomUtil.getRandomInt(size - 1);
	
				goal = uninspected.get(rand);
			}
		}

		if (goal != null) {
			printDescription(INSPECT_DETAIL + " " + goal.toLowerCase());

			createExperienceFromSkill(time);
			
			if (getDuration() <= (getTimeCompleted() + time)) {
				greenhouse.markInspected(goal);
				endTask();
			}
		}
		else
			endTask();

		return 0;
	}

	/**
	 * Performs the cleaning phase.
	 *
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double cleaningPhase(double time) {
	
		if (goal == null) {
			List<String> uncleaned = greenhouse.getUncleaned();
			int size = uncleaned.size();
	
			if (size > 0) {
				int rand = RandomUtil.getRandomInt(size - 1);
	
				goal = uncleaned.get(rand);
			}
		}
		
		if (goal != null) {
			printDescription(CLEAN_DETAIL + " " + goal.toLowerCase());
				
			createExperienceFromSkill(time);
			
			if (getDuration() <= (getTimeCompleted() + time)) {
				greenhouse.markCleaned(goal);
				endTask();
			}
		}
		else
			endTask();
			
		return 0;
	}

	/**
	 * Performs the sampling phase in the botany lab
	 *
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double samplingPhase(double time) {

		printDescription(SAMPLE);
				
		CropSpec type = null;

		int rand = RandomUtil.getRandomInt(5);

		if (rand == 0) {
			// Obtain a crop type randomly
			type = cropConfig.getRandomCropType();
		}

		else {
			// Obtain the crop type with the highest VP to work on in the lab
			type = greenhouse.selectVPCrop();
		}

		if (type != null) {
			// Note the lab may or may not have any of this crop left
			boolean hasEmptySpace = greenhouse.checkBotanyLab();

			if (hasEmptySpace) {
				// Inspect a tissue culture
				String name = greenhouse.checkOnCropTissue();
				
				if (name != null) {
					setDescription(SAMPLE_DETAIL + " " + Farming.TISSUE);
	
					logger.log(greenhouse.getBuilding(), worker, Level.INFO, 30_000,
							"Sampling " + name
							+ " in a botany lab.");
					
					double mod = 0;
					// Determine amount of effective work time based on "Botany" skill
					int greenhouseSkill = getEffectiveSkillLevel();
					if (greenhouseSkill <= 0) {
						mod *= RandomUtil.getRandomDouble(.5, 1.0);
					} else {
						mod *= RandomUtil.getRandomDouble(.5, 1.0) * greenhouseSkill * 1.2;
					}
	
					double workTime = time * mod;
					
					addExperience(workTime);
				}
			}
			
			if (getDuration() <= (getTimeCompleted() + time)) {
				endTask();
			}
		}

		return 0;
	}

	/**
	 * Gets the greenhouse the person is tending.
	 *
	 * @return greenhouse
	 */
	public Farming getGreenhouse() {
		return greenhouse;
	}
}
