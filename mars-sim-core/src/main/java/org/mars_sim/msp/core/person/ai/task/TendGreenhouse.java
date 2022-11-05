/*
 * Mars Simulation Project
 * TendGreenhouse.java
 * @date 2022-08-11
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.util.List;
import java.util.logging.Level;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskPhase;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.farming.Crop;
import org.mars_sim.msp.core.structure.building.function.farming.CropSpec;
import org.mars_sim.msp.core.structure.building.function.farming.Farming;
import org.mars_sim.msp.core.tool.Conversion;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The TendGreenhouse class is a task for tending the greenhouse in a
 * settlement. This is an effort driven task.
 */
public class TendGreenhouse extends Task {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(TendGreenhouse.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.tendGreenhouse"); //$NON-NLS-1$
	/** Task phases. */
	private static final TaskPhase TENDING = new TaskPhase(Msg.getString("Task.phase.tending")); //$NON-NLS-1$
	/** Task phases. */
	private static final TaskPhase INSPECTING = new TaskPhase(Msg.getString("Task.phase.inspecting")); //$NON-NLS-1$
	/** Task phases. */
	private static final TaskPhase CLEANING = new TaskPhase(Msg.getString("Task.phase.cleaning")); //$NON-NLS-1$
	/** Task phases. */
	private static final TaskPhase SAMPLING = new TaskPhase(Msg.getString("Task.phase.sampling")); //$NON-NLS-1$
	/** Task phases. */
	private static final TaskPhase TRANSFERRING_SEEDLING = new TaskPhase(Msg.getString("Task.phase.transferring")); //$NON-NLS-1$
	/** Task phases. */
	private static final TaskPhase GROWING_TISSUE = new TaskPhase(Msg.getString("Task.phase.growingTissue")); //$NON-NLS-1$

	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = -1.1D;

	// Data members
	/** The goal of the task at hand. */
	private String goal;
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
		super(NAME, person, false, false, STRESS_MODIFIER, SkillType.BOTANY, 100D, 10D);

		if (person.isOutside()) {
			endTask();
			return;
		}

		// Get available greenhouse if any.
		this.greenhouse = greenhouse;

		// Walk to greenhouse.
		walkToTaskSpecificActivitySpotInBuilding(greenhouse.getBuilding(), FunctionType.FARMING, false);

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
			// Walk to greenhouse.
			walkToTaskSpecificActivitySpotInBuilding(greenhouse.getBuilding(), FunctionType.FARMING, false);
			
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
	 * Select a suitab le sub-task in the greenhouse
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
		logger.log(greenhouse.getBuilding(), worker, Level.FINE, 30_000L, text + ".");
	}
	
	/**
	 * Sets the task description for tending a specific crop for the person.
	 * 
	 * @param needyCrop
	 */
	public void setCropDescription(Crop needyCrop) {
		logger.log(greenhouse.getBuilding(), worker, Level.FINE, 30_000L, "Tending " + needyCrop.getCropName() + ".");
		setDescription(Msg.getString("Task.description.tendGreenhouse.tend.detail",
				Conversion.capitalize(needyCrop.getCropName())), true);
	}

	/**
	 * Sets the task description of being done with tending crops.
	 */
	public void setDescriptionCropDone() {
		logger.log(greenhouse.getBuilding(), worker, Level.FINE, 30_000L, "Done tending crops.");
		setDescription(Msg.getString("Task.description.tendGreenhouse.tend.done"), false);
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

		if (needyCrop == null)
			needyCrop = greenhouse.getNeedyCrop();
		
		if (needyCrop == null) {
			setDescriptionCropDone();
			return 0;
		}

		boolean needTending = needyCrop.getCurrentWorkRequired() > Crop.CROP_TIME_OFFSET;
		
		if (needTending) {
			remainingTime = tend(time);
			return remainingTime;
		}
		
		// Select a new needy crop
		needyCrop = greenhouse.getNeedyCrop();
		
		if (needyCrop == null) {
			setDescriptionCropDone();
			return 0;
		}
		
		needTending = needyCrop.getCurrentWorkRequired() > -50;
		
		if (needTending) {
			remainingTime = tend(time);
			return remainingTime;
		}

		logger.log(greenhouse.getBuilding(), worker, Level.INFO, 1_000, 
				"Tending " + needyCrop.getCropName() + " was no longer needed.");
		setDescriptionCropDone();
		
		// Set needyCrop to null since needTending is false
		// This allow another needyCrop to be picked
		
		// Select a new needy crop
		needyCrop = greenhouse.getNeedyCrop();	
		if (needyCrop == null) {
			endTask();
		}
		
		return remainingTime;
	}
	
	public double tend(double time) {
		double remainingTime = 0;
		double workTime = time;
		double mod = 0;
		
		if (worker.getUnitType() == UnitType.PERSON)
			mod = 1;
		else
			mod = .25 * RandomUtil.getRandomDouble(.85, 1.15);
		
		// Determine amount of effective work time based on "Botany" skill
		int greenhouseSkill = getEffectiveSkillLevel();
		if (greenhouseSkill == 0)
			mod *= RandomUtil.getRandomDouble(.85, 1.15);
		else
			mod *= RandomUtil.getRandomDouble(.85, 1.15) * greenhouseSkill * 1.25;
		
		double remain = greenhouse.addWork(workTime * mod, worker, needyCrop);
		
		// Calculate used time
		double usedTime = workTime - remain;
		
		if (usedTime > 0) {
			setCropDescription(needyCrop);

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
			logger.log(greenhouse.getBuilding(), worker, Level.INFO, 30_000, 
					"usedTime: " + usedTime + ".");
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
				printDescription(Msg.getString("Task.description.tendGreenhouse.plant.detail", cropSpec.getName()));
			}
			else {
				// Find another task
				selectTask();
				
				return time / 2.0;
			}
			
			printDescription(Msg.getString("Task.description.tendGreenhouse.transfer"));

			addExperience(workTime);
	
		}
		
		else if (greenhouse.getNumCrops2Plant() > 0 && getDuration() <= (getTimeCompleted() + time)) {
			greenhouse.plantSeedling(cropSpec, getTimeCompleted() + time, worker);
			printDescription("Planting " + cropSpec + ".");
				
			addExperience(workTime);
		}
		
		else {
			// Find another task
			selectTask();
		}
		
		return 0;
	}

	/**
	 * Grows the tissue culture.
	 * 
	 * @param time
	 * @return
	 */
	private double growingTissue(double time) {

		printDescription(Msg.getString("Task.description.tendGreenhouse.grow"));
		
		// Check if the lab is available
		if (!greenhouse.checkBotanyLab())  {
			endTask();
		}
	
		if (goal == null) {
			goal = greenhouse.chooseCrop2Extract(Farming.STANDARD_AMOUNT_TISSUE_CULTURE);
			if (goal != null) {
				greenhouse.getResearch().addToIncubator(goal, Farming.STANDARD_AMOUNT_TISSUE_CULTURE);	
				logger.log(greenhouse.getBuilding(), worker, Level.INFO, 20_000, 
						"Sampled " + goal + " and added to incubator for growing tissues.");
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
			
		printDescription(Msg.getString("Task.description.tendGreenhouse.grow.detail", goal.toLowerCase()));

		createExperienceFromSkill(time);

		if (getDuration() <= (getTimeCompleted() + time)) {		

			greenhouse.getResearch().harvestTissue(worker);
			
			logger.log(greenhouse.getBuilding(), worker, Level.INFO, 0, "Done with growing " + goal + " tissues in botany lab.");
					
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
			printDescription(Msg.getString("Task.description.tendGreenhouse.inspect.detail", goal.toLowerCase()));

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
			printDescription(Msg.getString("Task.description.tendGreenhouse.clean.detail", goal.toLowerCase()));
				
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

		printDescription(Msg.getString("Task.description.tendGreenhouse.sample"));
				
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
					setDescription(Msg.getString("Task.description.tendGreenhouse.sample.detail",
							name) + Farming.TISSUE);
	
					logger.log(greenhouse.getBuilding(), worker, Level.INFO, 30_000,
							"Sampling " + name
							+ " in botany lab.");
					
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
