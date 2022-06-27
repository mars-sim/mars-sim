/*
 * Mars Simulation Project
 * TendGreenhouse.java
 * @date 2022-06-25
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
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
public class TendGreenhouse extends Task implements Serializable {

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
	
	/**
	 * Constructor.
	 *
	 * @param person the person performing the task.
	 */
	public TendGreenhouse(Person person) {
		// Use Task constructor
		super(NAME, person, false, false, STRESS_MODIFIER, SkillType.BOTANY, 100D, 20D);

		if (person.isOutside()) {
			endTask();
			return;
		}

		// Get available greenhouse if any.
		Building farmBuilding = getAvailableGreenhouse(person);

		if (farmBuilding != null) {

			greenhouse = farmBuilding.getFarming();

			// Walk to greenhouse.
			walkToTaskSpecificActivitySpotInBuilding(farmBuilding, FunctionType.FARMING, false);

			// Checks quickly to see if a needy crop is available
			Crop crop = greenhouse.getNeedyCrop(null);
			
			if (crop != null) {
				
				int rand = RandomUtil.getRandomInt(10);
				
				if (rand == 0)
					acceptedTask = INSPECTING;
	
				else if (rand == 1)
					acceptedTask = CLEANING;
	
				else if (rand == 2)
					acceptedTask = SAMPLING;
	
				else  if (rand == 3)
					acceptedTask = GROWING_TISSUE;
				
				else {
					// Plant a crop or tending a crop
					if (greenhouse.getNumCrops2Plant() > 0)
						acceptedTask = TRANSFERRING_SEEDLING;
					else
						acceptedTask = TENDING;
				}
								
				addPhase(acceptedTask);
				setPhase(acceptedTask);
			}
			
			else if (acceptedTask == null) {
				int rand = RandomUtil.getRandomInt(3);
	
				if (rand == 0)
					acceptedTask = INSPECTING;
	
				else if (rand == 1)
					acceptedTask = CLEANING;
	
				else if (rand == 2)
					acceptedTask = SAMPLING;
	
				else 
					acceptedTask = GROWING_TISSUE;

				addPhase(acceptedTask);
				setPhase(acceptedTask);
			}
		}
		else {
			logger.log(person, Level.WARNING, 0, "Could not find a greenhouse to tend.");
			endTask();
		}
	}

	/**
	 * Constructor 2.
	 *
	 * @param robot the robot performing the task.
	 */
	public TendGreenhouse(Robot robot) {
		// Use Task constructor
		super(NAME, robot, false, false, 0, SkillType.BOTANY, 100D, 50D);

		// Initialize data members
		if (robot.isOutside()) {
			endTask();
			return;
		}

		// Get available greenhouse if any.
		Building farmBuilding = getAvailableGreenhouse(robot);
		if (farmBuilding != null) {
			greenhouse = farmBuilding.getFarming();

			// Walk to greenhouse.
			walkToTaskSpecificActivitySpotInBuilding(farmBuilding, FunctionType.FARMING, false);

			// Initialize phase
			addPhase(TENDING);
			setPhase(TENDING);

		} else {
			endTask();
			return;
		}
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
	 * Sets the description and print. the log.
	 * 
	 * @param text
	 */
	private void printDescription(String text) {
		setDescription(text);
		logger.log(greenhouse.getBuilding(), worker, Level.INFO, 30_000, text + ".");
	}
	
	/**
	 * Sets the task description for tending a specific crop for the person.
	 * 
	 * @param needyCrop
	 */
	public void setCropDescription(Crop needyCrop) {
		logger.log(greenhouse.getBuilding(), worker, Level.CONFIG, 30_000L, "Tending " + needyCrop.getCropName() + ".");
		setDescription(Msg.getString("Task.description.tendGreenhouse.tend.detail",
				Conversion.capitalize(needyCrop.getCropName())));
	}

	/**
	 * Sets the task description of being done with tending crops.
	 */
	public void setDescriptionCropDone() {
		logger.log(greenhouse.getBuilding(), worker, Level.CONFIG, 30_000L, "Done tending crops.");
		setDescription(Msg.getString("Task.description.tendGreenhouse.tend.done"));
	}
	
	/**
	 * Performs the tending phase.
	 *
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double tendingPhase(double time) {

		double workTime = 0;

		if (isDone()) {
			return time;
		}

		Building farmBuilding = greenhouse.getBuilding();
		
		// Check if greenhouse has malfunction.
		if (farmBuilding.getMalfunctionManager() != null && farmBuilding.getMalfunctionManager().hasMalfunction()) {
			endTask();
			return time;
		}

		double mod = 0;

		if (worker.getUnitType() == UnitType.PERSON) {
			mod = 2D;
		}

		else {
			mod = 1D;
		}

		// Determine amount of effective work time based on "Botany" skill
		int greenhouseSkill = getEffectiveSkillLevel();
		if (greenhouseSkill <= 0) {
			mod *= RandomUtil.getRandomDouble(.5, 1.0);
		} else {
			mod *= RandomUtil.getRandomDouble(.5, 1.0) * greenhouseSkill * 1.2;
		}

		workTime = time * mod;

		double modRemainingTime = greenhouse.addWork(workTime, this, worker);
		
		// Add experience
		addExperience(time);

		// Check for accident in greenhouse.
		checkForAccident(farmBuilding, time, 0.005D);

		// Calculate used time
		double usedTime = workTime - modRemainingTime;
		
		double remainingTime = 0;
		
		if (modRemainingTime > 0) {
			// Divided by mod to get back any leftover real time
			remainingTime = time - (usedTime / mod);
		}
		
//		logger.log(farmBuilding, worker, Level.INFO, 10_000,
//				"mod: " + mod +
//				"  time: " + time +
//				"  workTime: " + workTime +
//				"  usedTime: " + usedTime + 
//				"  modRemainingTime: " + modRemainingTime +				
//				"  remainingTime: " + remainingTime +
//				"  TimeCompleted: " + getTimeCompleted() +
//				"  duration: " + getDuration()		
//				);
		
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
		
		addExperience(workTime);
		
		printDescription(Msg.getString("Task.description.tendGreenhouse.transfer"));
		
		if (getDuration() <= (getTimeCompleted() + time)) {
			Crop crop = greenhouse.transferSeedling(getTimeCompleted() + time, worker);
			printDescription(Msg.getString("Task.description.tendGreenhouse.plant.detail", crop.getCropName()));
			endTask();
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
		// Obtain the crop with the highest VP to work on in the lab
		CropSpec type = greenhouse.selectVPCrop();
		
		printDescription(Msg.getString("Task.description.tendGreenhouse.grow"));
		
		if (greenhouse.checkBotanyLab(type, worker))  {
			
			printDescription(Msg.getString("Task.description.tendGreenhouse.grow.detail", type.getName().toLowerCase()));
			
			logger.log(greenhouse.getBuilding(), worker, Level.INFO, 30_000, "Growing "
					+ type.getName() + Farming.TISSUE
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
	
			if (getDuration() <= (getTimeCompleted() + time)) {
				endTask();
			}
			
			return 0;
		}

		return time;
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

		printDescription(Msg.getString("Task.description.tendGreenhouse.inspect.detail", goal.toLowerCase()));

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
			greenhouse.markInspected(goal);
			endTask();
		}

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

		printDescription(Msg.getString("Task.description.tendGreenhouse.clean.detail", goal.toLowerCase()));
				
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
			greenhouse.markCleaned(goal);
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
			boolean hasWork = greenhouse.checkBotanyLab(type, worker);

			if (hasWork) {
				
				setDescription(Msg.getString("Task.description.tendGreenhouse.sample.detail",
					type.getName()) + Farming.TISSUE);

				logger.log(greenhouse.getBuilding(), worker, Level.INFO, 30_000,
						"Sampling " + type.getName() + Farming.TISSUE
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

	/**
	 * Gets an available greenhouse that the person can use. Returns null if no
	 * greenhouse is currently available.
	 *
	 * @param person the person
	 * @return available greenhouse
	 */
	public static Building getAvailableGreenhouse(Unit unit) {
		Building result = null;
		Person person = null;
		Robot robot = null;
		BuildingManager buildingManager;

		if (unit.getUnitType() == UnitType.PERSON) {
			person = (Person) unit;
			if (person.isInSettlement()) {
				buildingManager = person.getSettlement().getBuildingManager();
				List<Building> farmBuildings = buildingManager.getFarmsNeedingWork();

				if (farmBuildings != null) {
					if (!farmBuildings.isEmpty()) {
						if (farmBuildings.size() > 0) {
							Map<Building, Double> farmBuildingProbs = BuildingManager
									.getBestRelationshipBuildings(person, farmBuildings);
							result = RandomUtil.getWeightedRandomObject(farmBuildingProbs);
						}
					}
				}
			}
		}

		else {
			robot = (Robot) unit;
			if (robot.isInSettlement()) {
				buildingManager = robot.getSettlement().getBuildingManager();
				List<Building> farmBuildings = buildingManager.getFarmsNeedingWork();

				// Choose the building the robot is at.
				if (farmBuildings != null) {
					if (!farmBuildings.isEmpty()) {
						for (Building b : farmBuildings) {
							if (b == robot.getBuildingLocation())
								return b;
							// Note: choose the farmBuilding closest to the robot
							// Note: check if other robots are already in this farmBuilding, i.e. checking
							// for the crowdliness of this farmBuilding
						}

						if (farmBuildings.size() > 0) {
							result = farmBuildings.get(RandomUtil.getRandomInt(0, farmBuildings.size() - 1));
						}
					}
				}

				// Note: add person's good/bad feeling toward robots
//                int size = farmBuildings.size();
//                //System.out.println("size is "+size);
//                int selected = 0;
//                if (size == 0)
//                	result = null;
//                if (size >= 1) {
//                	selected = RandomUtil.getRandomInt(size-1);
//                	result = farmBuildings.get(selected);
//                }
				// System.out.println("getAvailableGreenhouse() : selected is "+selected);
			}
		}
		return result;
	}
}
