/*
 * Mars Simulation Project
 * TendFishTank.java
 * @date 2023-12-07
 * @author Barry Evans
 */
package com.mars_sim.core.building.function.farming.task;


import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.building.function.farming.Fishery;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;

/**
 * The TendFishTank class is a task for tending the fishery in a
 * settlement. This is an effort driven task.
 */
public class TendFishTank extends TendHousekeeping {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(TendFishTank.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.tendFishTank"); //$NON-NLS-1$
	static final TaskPhase TENDING = new TaskPhase(Msg.getString("Task.phase.tending")); //$NON-NLS-1$
	static final TaskPhase CATCHING = new TaskPhase(Msg.getString("Task.phase.catching")); //$NON-NLS-1$	

	// Limit the maximum time spent on a phase
	static final double MAX_FISHING = 100D;
	static final double MAX_TEND = 100D;

	private static final ExperienceImpact IMPACT = new ExperienceImpact(100D,
													NaturalAttributeType.EXPERIENCE_APTITUDE, false,
													-1.1D, SkillType.ASTROBIOLOGY);

	// Data members
	private double completedTime = 0D;
	/** The fish tank the person is tending. */
	private Fishery fishTank;
	/** The building where the fish tank is. */	
	private Building building;

	/**
	 * Worker tends a fish tank for a specific subtask
	 * @param fisher
	 * @param fishTank
	 * @param subTask
	 */
	TendFishTank(Worker fisher, Fishery fishTank, TaskPhase subTask) {
		// Use Task constructor
		super(NAME, fisher, fishTank.getHousekeeping(), IMPACT, 0D);

		if (fisher.isOutside()) {
			endTask();
			return;
		}

		// Get available fish tank if any.
		this.fishTank = fishTank;
		this.building = fishTank.getBuilding();

		// Walk to fish tank.
		walkToTaskSpecificActivitySpotInBuilding(building, FunctionType.FISHERY, false);	

		setPhase(subTask);
	}

	/**
	 * Select the best subtask for a Worker to apply to a FishTank
	 * @param fishTank The tank being processed
	 * @param doFishing Including fishing
	 * @return
	 */
	static TaskPhase selectActivity(Fishery fishTank, boolean doFishing) {
		TaskPhase selected;
		int rand = RandomUtil.getRandomInt(6);
				
		// If it hasn't tended the fish for over 500 millisols, do it now
		if (fishTank.getWeedDemand() > 0) {
			selected = TENDING;
		}

		// Note: may offer exception in future
		else if (rand == 0 && doFishing && fishTank.canCatchFish()) {
			// Do fishing
			selected = CATCHING;
		}
		else if (rand == 1 || rand == 2) {
			selected = CLEANING;
		}
		else if (rand == 3 || rand == 4) {
			selected = INSPECTING;
		}
		else {
			selected = TENDING;
		}

		return selected;
	}

	@Override
	protected double performMappedPhase(double time) {
		time = super.performMappedPhase(time);
		if (isDone()) {
			return time;
		} else if (TENDING.equals(getPhase())) {
			return tendingPhase(time);
		} else if (CATCHING.equals(getPhase())) {
			return catchingPhase(time);
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
	private double catchingPhase(double time) {

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

		double remainingTime = fishTank.catchFish(worker, workTime);

		// Assume tending can reduce stress
		if (person != null)
			person.getPhysicalCondition().reduceStress(time);
		
		// Add experience
		addExperience(time);

		// Check for accident
		checkForAccident(building, time, 0.003);

		if ((remainingTime > 0) || (fishTank.getSurplusStock() == 0)) {
			endTask();

			// Scale it back to the. Calculate used time 
			double usedTime = workTime - remainingTime;
			return time - (usedTime / mod);
		}
		else {
			completedTime += time;
			if (completedTime > MAX_FISHING) {
				logger.info(worker, "Giving up on fishing.");
				endTask();
			}
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

		double remainingTime = fishTank.tendWeeds(workTime);

		// Assume tending can reduce stress
		if (person != null)
			person.getPhysicalCondition().reduceStress(time);
		
		// Add experience
		addExperience(time);

		// Check for accident
		checkForAccident(building, time, 0.005);

		completedTime += time;

		if ((remainingTime > 0) || (completedTime > MAX_TEND)) {
			endTask();
		}
		
		return 0;
	}
}
