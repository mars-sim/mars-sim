/*
 * Mars Simulation Project
 * TendAlgaePond.java
 * @date 2023-12-07
 * @author Manny
 */
package com.mars_sim.core.structure.building.function.task;


import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.structure.building.function.farming.AlgaeFarming;
import com.mars_sim.tools.Msg;
import com.mars_sim.tools.util.RandomUtil;

/**
 * The TendAlgaePond class is a task for tending algae pond in a
 * settlement. This is an effort driven task.
 */
public class TendAlgaePond extends TendHousekeeping {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(TendAlgaePond.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.tendAlgaePond"); //$NON-NLS-1$

	/** Task phases. */
	static final TaskPhase TENDING = new TaskPhase(Msg.getString("Task.phase.tending")); //$NON-NLS-1$
	static final TaskPhase HARVESTING = new TaskPhase(Msg.getString("Task.phase.harvesting")); //$NON-NLS-1$	

	// Limit the maximum time spent on a phase
	static final double MAX_HARVESTING = 100D;
	static final double MAX_TEND = 100D;
	
	private static final ExperienceImpact IMPACT = new ExperienceImpact(100D,
													NaturalAttributeType.EXPERIENCE_APTITUDE, false,
													-1.1D, SkillType.BIOLOGY);

	// Data members
	private double harvestingTime = 0D;
	
	private double totalHarvested;
	
	private double tendTime = 0D;
	
	/** The algae pond the person is tending. */
	private AlgaeFarming pond;
	/** The building where the algae pond is. */	
	private Building building;
	
	/**
	 * Constructor.
	 * 
	 * @param farmer the worker performing the task.
	 * @param pond Pond being tendered
	 * @param activity Sub activity to perform
	 */
	TendAlgaePond(Worker farmer, AlgaeFarming pond, TaskPhase activity) {
		// Use Task constructor
		super(NAME, farmer, pond.getHousekeeping(), IMPACT, 0D);

		if (farmer.isOutside()) {
			endTask();
			return;
		}

		// Get available pond if any.
		this.pond = pond;
		this.building = pond.getBuilding();

		// Walk to algae pond.
		walkToTaskSpecificActivitySpotInBuilding(building, FunctionType.ALGAE_FARMING, false);

		setPhase(activity);
	}

	static TaskPhase selectActivity(AlgaeFarming pond, boolean canHarvest) {
		int rand = RandomUtil.getRandomInt(6);
		
		double surplus = pond.getSurplusRatio();
		
		// If it hasn't tended the algae for over 500 millisols, do it now
		if (pond.getNutrientDemand() > 1) {
			return TENDING;
		}
		// If surplus is less than or equal to 0.5, do NOT harvest
		// Note: may offer exception in future
		else if (canHarvest
				// If the mass of algae exceeds the ideal mass, harvest it now
				&& surplus > 1) {
			// Harvest
			return HARVESTING;
		}
		else if (rand == 1 || rand == 2) {
			return CLEANING;
		}
		else if (rand == 3 || rand == 4) {
			return INSPECTING;
		}
		return TENDING;
	}

	@Override
	protected double performMappedPhase(double time) {
		time = super.performMappedPhase(time);
		if (isDone()) {
			return time;
		} else if (TENDING.equals(getPhase())) {
			return tendingPhase(time);
		} else if (HARVESTING.equals(getPhase())) {
			return harvestingPhase(time);
		} else {
			return time;
		}
	}

	/**
	 * Performs the tending phase.
	 * 
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double harvestingPhase(double time) {

		double workTime = time;

		// Check if building has malfunction.
		if (building.getMalfunctionManager() != null && building.getMalfunctionManager().hasMalfunction()) {
			endTask();
			return time;
		}

		double mod = 0;

		if (person != null) {
			mod = 6D;
		}

		else {
			mod = 4D;
		}

		// Determine amount of effective work time based on "Botany" skill
		int skill = getEffectiveSkillLevel();
		if (skill <= 0) {
			mod += RandomUtil.getRandomDouble(.25);
		} else {
			mod += RandomUtil.getRandomDouble(.25) + 1.25 * skill;
		}

		workTime *= mod;

		double algaeMass = pond.harvestAlgae(worker, workTime);
		if (algaeMass == 0.0) {
			// if algaeMass is zero, none can be harvested
			endTask();
			
			// Calculate used time 
			double usedTime = workTime;
			return time - (usedTime / mod);
		}
		
		totalHarvested += algaeMass;
		
		// Add experience
		addExperience(time);

		// Check for accident
		checkForAccident(building, time, 0.003);

		harvestingTime += time;
		
		if (harvestingTime > MAX_HARVESTING) {
			
			logger.info(worker, "Total kg algae harvested: " 
					+ Math.round(totalHarvested * 100.0)/100.0 );
			endTask();
			
			// Calculate used time 
			double usedTime = workTime;
			return time - (usedTime / mod);
		}

		return 0;
	}
	
	
	/**
	 * Performs the tending phase.
	 * 
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double tendingPhase(double time) {

		double workTime = time;

		// Check if building has malfunction.
		if (building.getMalfunctionManager() != null 
				&& building.getMalfunctionManager().hasMalfunction()) {
			endTask();
			return time;
		}

		double mod = 1;

		// Determine amount of effective work time based on skill
		int skill = getEffectiveSkillLevel();
		if (skill > 0) {
			mod += RandomUtil.getRandomDouble(.5, .75) + 0.75 * skill;
		}

		workTime *= mod;

		double remainingTime = pond.tending(workTime);

		// Add experience
		addExperience(time);

		// Check for accident
		checkForAccident(building, time, 0.005);

		if ((remainingTime > 0) || (tendTime > MAX_TEND)) {
			endTask();
		}
		tendTime += time;
		
		return 0;
	}
}
