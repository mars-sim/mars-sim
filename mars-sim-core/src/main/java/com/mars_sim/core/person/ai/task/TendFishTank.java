/*
 * Mars Simulation Project
 * TendFishTank.java
 * @date 2023-12-07
 * @author Barry Evans
 */
package com.mars_sim.core.person.ai.task;

import java.util.logging.Level;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.structure.building.function.farming.Fishery;
import com.mars_sim.tools.Msg;
import com.mars_sim.tools.util.RandomUtil;

/**
 * The TendFishTank class is a task for tending the fishery in a
 * settlement. This is an effort driven task.
 */
public class TendFishTank extends Task {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(TendFishTank.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.tendFishTank"); //$NON-NLS-1$
	/** Task phases. */
	private static final TaskPhase TENDING = new TaskPhase(Msg.getString("Task.phase.tending")); //$NON-NLS-1$
	/** Task phases. */
	private static final TaskPhase INSPECTING = new TaskPhase(Msg.getString("Task.phase.inspecting")); //$NON-NLS-1$
	/** Task phases. */
	private static final TaskPhase CLEANING = new TaskPhase(Msg.getString("Task.phase.cleaning")); //$NON-NLS-1$
	/** Task phases. */
	private static final TaskPhase CATCHING = new TaskPhase(Msg.getString("Task.phase.catching")); //$NON-NLS-1$	

	// Limit the maximum time spent on a phase
	private static final double MAX_FISHING = 100D;
	private static final double MAX_TEND = 100D;
	
	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = -1.1D;

	// Data members
	private double fishingTime = 0D;
	private double tendTime = 0D;
	/** The goal of the task at hand. */
	private String cleanGoal;
	/** The goal of the task at hand. */
	private String inspectGoal;
	/** The fish tank the person is tending. */
	private Fishery fishTank;
	/** The building where the fish tank is. */	
	private Building building;
	
	/**
	 * Constructor.
	 * 
	 * @param person the person performing the task.
	 */
	public TendFishTank(Person person, Fishery fishTank) {
		// Use Task constructor
		super(NAME, person, false, false, STRESS_MODIFIER, SkillType.BIOLOGY, 100D);

		if (person.isOutside()) {
			endTask();
			return;
		}

		// Get available fish tank if any.
		this.fishTank = fishTank;
		this.building = fishTank.getBuilding();

		// Walk to fish tank.
		walkToTaskSpecificActivitySpotInBuilding(building, FunctionType.FISHERY, false);	

		int rand = RandomUtil.getRandomInt(6);
		
		double surplus = fishTank.getSurplusStock();
		
		// If it hasn't tended the fish for over 500 millisols, do it now
		if (fishTank.getWeedDemand() > 0) {
			setPhase(TENDING);
			addPhase(TENDING);
			addPhase(INSPECTING);
			addPhase(CLEANING);
		}
		// If surplus is less than zero, do NOT catch any fish
		// Note: may offer exception in future
		else if (rand == 0 && surplus > 0) {
			// Do fishing
			setPhase(CATCHING);
			addPhase(CATCHING);
		}
		else if (rand == 1 || rand == 2) {
			setPhase(CLEANING);
			addPhase(CLEANING);
		}
		else if (rand == 3 || rand == 4) {
			setPhase(INSPECTING);
			addPhase(INSPECTING);
		}
		else {
			setPhase(TENDING);
			addPhase(TENDING);
			addPhase(INSPECTING);
			addPhase(CLEANING);
		}
	}

	/**
	 * Constructor 2.
	 * 
	 * @param robot the robot performing the task.
	 */
	public TendFishTank(Robot robot, Fishery fishTank) {
		// Use Task constructor
		super(NAME, robot, false, false, 0, SkillType.BIOLOGY, 50D);

		// Initialize data members
		if (robot.isOutside()) {
			endTask();
			return;
		}

		// Get available fish tank if any.
		this.fishTank = fishTank;
		this.building = fishTank.getBuilding();

		// Walk to fish tank.
		walkToTaskSpecificActivitySpotInBuilding(building, FunctionType.FISHERY, false);
		
		// Initialize phase
		// Robots don't catch fish
		setPhase(TENDING);
		addPhase(TENDING);
		addPhase(INSPECTING);
		addPhase(CLEANING);
	}

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			return 0;
		} else if (TENDING.equals(getPhase())) {
			return tendingPhase(time);
		} else if (INSPECTING.equals(getPhase())) {
			return inspectingPhase(time);
		} else if (CLEANING.equals(getPhase())) {
			return cleaningPhase(time);
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

		if (isDone()) {
			endTask();
			return time;
		}

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
			fishingTime += time;
			if (fishingTime > MAX_FISHING) {
				logger.log(building, worker, Level.INFO, 0, "Giving up on fishing.", null);
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

		if (isDone()) {
			endTask();
			return time;
		}

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

		// Add experience
		addExperience(time);

		// Check for accident
		checkForAccident(building, time, 0.005);

		if (remainingTime > 0) {
			int rand = RandomUtil.getRandomInt(1);
			if (rand == 0)
				setPhase(INSPECTING);
			else
				setPhase(CLEANING);

			// Scale it back to the. Calculate used time 
			double usedTime = workTime - remainingTime;
			return time - (usedTime / mod);
		}
		else if (tendTime > MAX_TEND) {
//			logger.log(building, worker, Level.INFO, 0, "Ended tending the fish tank.", null);
			endTask();
		}
		tendTime += time;
		
		return 0;
	}
	
	/**
	 * Performs the inspecting phase.
	 * 
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double inspectingPhase(double time) {
		if (inspectGoal == null) {
			inspectGoal = fishTank.getUninspected();
		}

		if (inspectGoal != null) {
			printDescription(Msg.getString("Task.description.tendFishTank.inspect.detail", 
					inspectGoal.toLowerCase()));

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
			
			if (getDuration() <= (getTimeCompleted() + time)) {
				fishTank.markInspected(inspectGoal, workTime);
				endTask();
			}
		}
			
		return 0;
	}


	/**
	 * Sets the description and print the log.
	 * 
	 * @param text
	 */
	private void printDescription(String text) {
		setDescription(text);
		logger.log(fishTank.getBuilding(), worker, Level.FINE, 30_000L, text + ".");
	}
	
	/**
	 * Performs the cleaning phase.
	 * 
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double cleaningPhase(double time) {

		if (cleanGoal == null) {
			cleanGoal = fishTank.getUncleaned();
		}
		
		if (cleanGoal != null) {
			printDescription(Msg.getString("Task.description.tendFishTank.clean.detail", 
					cleanGoal.toLowerCase()));
				
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
			
			if (getDuration() <= (getTimeCompleted() + time)) {
				fishTank.markCleaned(cleanGoal, workTime);
				endTask();
			}
		}
		else
			endTask();
		
		return 0;
	}
}
