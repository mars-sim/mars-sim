/*
 * Mars Simulation Project
 * TendAlgaePond.java
 * @date 2023-12-07
 * @author Manny
 */
package com.mars_sim.core.person.ai.task;


import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.structure.building.function.farming.AlgaeFarming;
import com.mars_sim.tools.Msg;
import com.mars_sim.tools.util.RandomUtil;

/**
 * The TendAlgaePond class is a task for tending algae pond in a
 * settlement. This is an effort driven task.
 */
public class TendAlgaePond extends Task {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(TendAlgaePond.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.tendAlgaePond"); //$NON-NLS-1$
	
	private static final String INSPECT_DETAIL = Msg.getString("Task.description.tendGreenhouse.inspect.detail");
	
	private static final String CLEAN_DETAIL = Msg.getString("Task.description.tendGreenhouse.clean.detail");
	
	/** Task phases. */
	private static final TaskPhase TENDING = new TaskPhase(Msg.getString("Task.phase.tending")); //$NON-NLS-1$
	/** Task phases. */
	private static final TaskPhase INSPECTING = new TaskPhase(Msg.getString("Task.phase.inspecting")); //$NON-NLS-1$
	/** Task phases. */
	private static final TaskPhase CLEANING = new TaskPhase(Msg.getString("Task.phase.cleaning")); //$NON-NLS-1$
	/** Task phases. */
	private static final TaskPhase HARVESTING = new TaskPhase(Msg.getString("Task.phase.harvesting")); //$NON-NLS-1$	

	// Limit the maximum time spent on a phase
	private static final double MAX_HARVESTING = 100D;
	private static final double MAX_TEND = 100D;
	
	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = -1.1D;

	// Data members
	private double harvestingTime = 0D;
	
	private double totalHarvested;
	
	private double tendTime = 0D;
	
	/** The goal of the task at hand. */
	private String cleanGoal;
	/** The goal of the task at hand. */
	private String inspectGoal;
	/** The algae pond the person is tending. */
	private AlgaeFarming pond;
	/** The building where the algae pond is. */	
	private Building building;
	
	/**
	 * Constructor.
	 * 
	 * @param person the person performing the task.
	 */
	public TendAlgaePond(Person person, AlgaeFarming pond) {
		// Use Task constructor
		super(NAME, person, false, false, STRESS_MODIFIER, SkillType.BIOLOGY, 100D);

		if (person.isOutside()) {
			endTask();
			return;
		}

		// Get available pond if any.
		this.pond = pond;
		this.building = pond.getBuilding();

		// Walk to algae pond.
		walkToTaskSpecificActivitySpotInBuilding(building, FunctionType.ALGAE_FARMING, false);	

		int rand = RandomUtil.getRandomInt(6);
		
		double surplus = pond.getSurplusRatio();
		
		// If it hasn't tended the algae for over 500 millisols, do it now
		if (pond.getNutrientDemand() > 1) {
			setPhase(TENDING);
			addPhase(TENDING);
			addPhase(INSPECTING);
			addPhase(CLEANING);
		}
		// If surplus is less than or equal to 0.5, do NOT harvest
		// Note: may offer exception in future
		else if ((rand == 0 && surplus > 0.5) 
				// If the mass of algae exceeds the ideal mass, harvest it now
				|| surplus > 1) {
			// Harvest
			setPhase(HARVESTING);
			addPhase(HARVESTING);
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
	public TendAlgaePond(Robot robot, AlgaeFarming pond) {
		// Use Task constructor
		super(NAME, robot, false, false, 0, SkillType.BIOLOGY, 50D);

		// Initialize data members
		if (robot.isOutside()) {
			endTask();
			return;
		}

		// Get available greenhouse if any.
		this.pond = pond;
		this.building = pond.getBuilding();

		// Walk to the pond.
		walkToTaskSpecificActivitySpotInBuilding(building, FunctionType.ALGAE_FARMING, false);
		
		// Initialize phase
		// Robots don't harvest algae
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

		double algaeMass = pond.harvestAlgae(worker, workTime);
		if (algaeMass == 0.0) {
			// if algaeMass is zero, none can be harvested
//			logger.log(building, worker, Level.INFO, 0, "Total kg algae harvested: " 
//					+ Math.round(totalHarvested * 100.0)/100.0 , null);
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

		double remainingTime = pond.tending(workTime);

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
//			logger.log(building, worker, Level.INFO, 0, "Ended tending the algae pond.", null);
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
			inspectGoal = pond.getUninspected();
		}

		if (inspectGoal != null) {
			printDescription(INSPECT_DETAIL + inspectGoal.toLowerCase());

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
				pond.markInspected(inspectGoal, workTime);
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
		logger.fine(worker, 30_000L, text + ".");
	}
	
	/**
	 * Performs the cleaning phase.
	 * 
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double cleaningPhase(double time) {

		if (cleanGoal == null) {
			cleanGoal = pond.getUncleaned();
		}
		
		if (cleanGoal != null) {
			printDescription(CLEAN_DETAIL + cleanGoal.toLowerCase());
				
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
				pond.markCleaned(cleanGoal, workTime);
				endTask();
			}
		}
		else
			endTask();
		
		return 0;
	}
}
