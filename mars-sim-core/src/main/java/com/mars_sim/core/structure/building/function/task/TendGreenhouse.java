/*
 * Mars Simulation Project
 * TendGreenhouse.java
 * @date 2024-02-03
 * @author Scott Davis
 */
package com.mars_sim.core.structure.building.function.task;

import com.mars_sim.core.UnitType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.structure.building.function.Research;
import com.mars_sim.core.structure.building.function.farming.Crop;
import com.mars_sim.core.structure.building.function.farming.CropSpec;
import com.mars_sim.core.structure.building.function.farming.Farming;
import com.mars_sim.tools.Msg;
import com.mars_sim.tools.util.RandomUtil;

/**
 * The TendGreenhouse class is a task for tending the greenhouse in a
 * settlement. This is an effort driven task.
 */
public class TendGreenhouse extends TendHousekeeping {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(TendGreenhouse.class.getName());

	/** static members */
	/** The stress modified per millisol. */
	private static final double CROP_RESILIENCY = Crop.CROP_RESILIENCY;
	
	private static final String NAME = Msg.getString("Task.description.tendGreenhouse"); //$NON-NLS-1$
	
	private static final String GROWING_DETAIL = Msg.getString("Task.description.tendGreenhouse.grow.detail"); //$NON-NLS-1$
	private static final String DONE_GROWING = "Done with growing ";

	private static final String SEED = Msg.getString("Task.description.tendGreenhouse.seed"); //$NON-NLS-1$
	private static final String DONE_SEEDING = Msg.getString("Task.description.tendGreenhouse.seed.done"); //$NON-NLS-1$
	
	private static final String TEND = Msg.getString("Task.description.tendGreenhouse.tend"); //$NON-NLS-1$
	private static final String DONE_TENDING = Msg.getString("Task.description.tendGreenhouse.tend.done"); //$NON-NLS-1$
		
	private static final String SAMPLE = Msg.getString("Task.description.tendGreenhouse.sample");
	private static final String SAMPLE_DETAIL = Msg.getString("Task.description.tendGreenhouse.sample.detail");

	
	private static final String TISSUES_IN_LAB = " tissues in botany lab.";

	
	/** Task phases. */
	static final TaskPhase TENDING = new TaskPhase(Msg.getString("Task.phase.tending")); //$NON-NLS-1$
	static final TaskPhase SAMPLING = new TaskPhase(Msg.getString("Task.phase.sampling")); //$NON-NLS-1$
	static final TaskPhase TRANSFERRING_SEEDLING = new TaskPhase(Msg.getString("Task.phase.transferring")); //$NON-NLS-1$
	static final TaskPhase GROWING_TISSUE = new TaskPhase(Msg.getString("Task.phase.growingTissue")); //$NON-NLS-1$
	
	private static final ExperienceImpact IMPACT = new ExperienceImpact(100D,
												NaturalAttributeType.EXPERIENCE_APTITUDE, true,
												-1.1D, SkillType.BOTANY);

	// Data members
	/** The goal of the task at hand. */
	private String goal;
	/** The previous tended crop. */
	private String previousCropName;
	/** The greenhouse the person is tending. */
	private Farming greenhouse;
	/** The crop to be worked on. */
	private Crop needyCrop;
	/** The crop spec to be selected to plant. */
	private CropSpec cropSpec;
	
	/**
	 * Constructor.
	 *
	 * @param person the person performing the task.
	 * @param farm Farm needing tender
	 */
	public TendGreenhouse(Worker farmer, Farming greenhouse, TaskPhase activity) {
		// Use Task constructor
		super(NAME, farmer, greenhouse.getHousekeeping(), IMPACT, RandomUtil.getRandomDouble(10, 50));

		if (farmer.isOutside()) {
			endTask();
			return;
		}

		// Get available greenhouse if any.
		this.greenhouse = greenhouse;

		// Walk to greenhouse.
		walkToTaskSpecificActivitySpotInBuilding(greenhouse.getBuilding(), FunctionType.FARMING, false);

		setPhase(activity);
	}

	
	@Override
	protected double performMappedPhase(double time) {
		time = super.performMappedPhase(time);
		if (isDone()) {
			return time;
		} else if (TENDING.equals(getPhase())) {
			return tendingPhase(time);
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
	static TaskPhase selectActivity(Farming greenhouse) {
		if (greenhouse.getNumCrops2Plant() > 0) {
			return TRANSFERRING_SEEDLING;
		}
		else {	
			// Choose the activity from the base 4 non-crop tasks then 2 slots per needy crop.
			int probability = 4 + greenhouse.getNumNeedTending() * 2;
			int rand = RandomUtil.getRandomInt(probability);

			if (rand == 0)
				return INSPECTING;
			else if (rand == 1)
				return CLEANING;
			else if (rand == 2)
				return SAMPLING;
			else if (rand == 3)
				return GROWING_TISSUE;
			else {
				if (greenhouse.getNeedyCrop() == null) {
					// Hmm shouldn't happen
					return INSPECTING;
				}
				else {
					return TENDING;
				}
			}
		}
	}

	/**
	 * Sets the task description of being done with tending crops.
	 */
	private void setDescriptionCropDone() {
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
		double mod = 1;
		
		if (worker.getUnitType() == UnitType.ROBOT) {
			mod = .3 * RandomUtil.getRandomDouble(.85, 1.15);
		}
		
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
			setDescription(TEND + " " + previousCropName, false);


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
		
		double mod = 1;
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
				updateDescription(DONE_SEEDING + " " + cropSpec.getName());
			}
			else {
				// Find another task
				endTask();
				
				return time / 2.0;
			}
			
			addExperience(workTime);
	
		}
		
		else if (greenhouse.getNumCrops2Plant() > 0 && getDuration() <= (getTimeCompleted() + time)) {
			greenhouse.plantSeedling(cropSpec, getTimeCompleted() + time, worker);
			updateDescription(SEED + " " + cropSpec + ".");
				
			addExperience(workTime);
		}
		
		else {
			// Find another task
			endTask();
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
		
		// Check if the lab is available
		if (!greenhouse.checkBotanyLab())  {
			endTask();
		}
	
		if (goal == null) {
			goal = greenhouse.chooseCrop2Extract(Farming.STANDARD_AMOUNT_TISSUE_CULTURE);
			if (goal != null) {
				greenhouse.getResearch().addToIncubator(goal, Farming.STANDARD_AMOUNT_TISSUE_CULTURE);	
				updateDescription(GROWING_DETAIL + " " + goal.toLowerCase() + " for " + Math.round(time * 100.0)/100.0 + " msol");
			}
			else {
				// Can't find any matured crop to sample
				endTask();
				return time / 2.0;
			}
		}
			

		addExperience(time);

		if (getDuration() <= (getTimeCompleted() + time)) {		

			greenhouse.getResearch().harvestTissue(worker);

			greenhouse.getResearch().increaseEntropy(time * Research.ENTROPY_FACTOR);
			
			logger.info(worker, DONE_GROWING + goal + TISSUES_IN_LAB);
					
			// Reset goal to null
			goal = null;
			
			endTask();
		}

		return 0;
	}

	/**
	 * Performs the sampling phase in the botany lab
	 *
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double samplingPhase(double time) {

		updateDescription(SAMPLE);
				
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
					
					double mod = 1;
					// Determine amount of effective work time based on "Botany" skill
					int greenhouseSkill = getEffectiveSkillLevel();
					if (greenhouseSkill <= 0) {
						mod *= RandomUtil.getRandomDouble(.5, 1.0);
					} else {
						mod *= RandomUtil.getRandomDouble(.5, 1.0) * greenhouseSkill * 1.2;
					}
	
					double workTime = time * mod;
					
					updateDescription(SAMPLE_DETAIL + " " + Farming.TISSUE + " for " + Math.round(time * 100.0)/100.0 + " msol");
			
					addExperience(workTime);
				}
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
