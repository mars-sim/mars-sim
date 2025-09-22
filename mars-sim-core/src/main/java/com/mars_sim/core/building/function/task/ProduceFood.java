/*
 * Mars Simulation Project
 * ProduceFood.java
 * @date 2022-07-26
 * @author Manny Kung
 */
package com.mars_sim.core.building.function.task;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.FoodProduction;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.data.UnitSet;
import com.mars_sim.core.food.FoodProductionProcess;
import com.mars_sim.core.food.FoodProductionProcessInfo;
import com.mars_sim.core.food.FoodProductionUtil;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillManager;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact.SkillWeight;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.OverrideType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Conversion;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;

/**
 * A task for working on a food production process.
 */
public class ProduceFood extends Task {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(ProduceFood.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.produceFood"); //$NON-NLS-1$
	
	private static final String CHECKING = Msg.getString("Task.description.produceFood.checking"); //$NON-NLS-1$
	
	private static final String DETAIL = Msg.getString("Task.description.produceFood.detail") + " "; //$NON-NLS-1$
    /** Task phases. */
    private static final TaskPhase PRODUCE_FOOD = new TaskPhase(Msg.getString(
            "Task.phase.produceFood")); //$NON-NLS-1$

	
	/** Impact doing this Task */
	private static final ExperienceImpact IMPACT = new ExperienceImpact(100D,
										NaturalAttributeType.EXPERIENCE_APTITUDE, false, .01,
										Set.of(new SkillWeight(SkillType.COOKING, 5),
											   new SkillWeight(SkillType.MATERIALS_SCIENCE, 2)));

	// Data members
	/** The food production foodFactory the person is using. */
	private FoodProduction foodFactory;

	/**
	 * Constructor.
	 * 
	 * @param person the person to perform the task
	 */
	public ProduceFood(Person person) {
		super(NAME, person, true, IMPACT, 25);

		// Initialize data members
		if (person.isInSettlement()) {
			
		    setDescription(NAME);
	
			// Get available food production foodFactory if any.
			Building foodProductionBuilding = getAvailableFoodProductionBuilding(person);
			if (foodProductionBuilding != null) {
				foodFactory = foodProductionBuilding.getFoodProduction();
	
				// Walk to food production building.
				walkToTaskSpecificActivitySpotInBuilding(foodProductionBuilding,
														 FunctionType.FOOD_PRODUCTION, false);
		
				// Initialize phase
				setPhase(PRODUCE_FOOD);
				
			}
			else {
				endTask();
			}

		}
		else {
			endTask();
		}
	}

	public ProduceFood(Robot robot) {
		super(NAME, robot, true, IMPACT,
				10D + RandomUtil.getRandomDouble(50D));

		// Initialize data members
		if (robot.isInSettlement()) {
			
		    setDescription(NAME);
	
			// Get available food production foodFactory if any.
			Building foodProductionBuilding = getAvailableFoodProductionBuilding(robot);
			if (foodProductionBuilding != null) {
				foodFactory = foodProductionBuilding.getFoodProduction();
				// Walk to food production building.
				walkToTaskSpecificActivitySpotInBuilding(foodProductionBuilding,
														FunctionType.FOOD_PRODUCTION, false);
		
				// Initialize phase
				setPhase(PRODUCE_FOOD);
			}
			else {
				endTask();
			}
		}
	}

	/**
	 * Cancels any food production processes that's beyond the skill of any people
	 * associated with the settlement.
	 * 
	 * @param person the person
	 */
	public static void cancelDifficultFoodProductionProcesses(Settlement settlement) {

		if (settlement != null) {
		    int highestSkillLevel = getHighestSkillAtSettlement(settlement);

			Iterator<Building> j = settlement.getBuildingManager().getBuildingSet(FunctionType.FOOD_PRODUCTION).iterator();
			while (j.hasNext()) {
				Building building = j.next();
				FoodProduction foodProductionFunction = building.getFoodProduction();
				Set<FoodProductionProcess> processes = ConcurrentHashMap.newKeySet();
				processes.addAll(foodProductionFunction.getProcesses());
				Iterator<FoodProductionProcess> k = processes.iterator();
				while (k.hasNext()) {
					FoodProductionProcess process = k.next();
					int processSkillLevel = process.getInfo().getSkillLevelRequired();
					if (processSkillLevel > highestSkillLevel) {
						// Cancel food production process.
						foodProductionFunction.endFoodProductionProcess(process, true);
					}
				}
			}
		}
	}

	/**
	 * Gets the highest skill level for food production at a settlement.
	 * 
	 * @param settlement the settlement.
	 * @return the highest person or robot skill level.
	 */
	private static int getHighestSkillAtSettlement(Settlement settlement) {

	    int highestSkillLevel = 0;
	    SkillManager skillManager = null;

	    // Get highest person skill level.
	    Iterator<Person> i = settlement.getAllAssociatedPeople().iterator();
        while (i.hasNext()) {
            Person tempPerson = i.next();
            skillManager = tempPerson.getSkillManager();
            int skill = skillManager.getSkillLevel(SkillType.COOKING) * 5;
            skill += skillManager.getSkillLevel(SkillType.MATERIALS_SCIENCE) * 2;
            skill = (int) Math.round(skill / 7D);
            if (skill > highestSkillLevel) {
                highestSkillLevel = skill;
            }
        }

        // Get highest robot skill level.
        Iterator<Robot> j = settlement.getAllAssociatedRobots().iterator();

        while (j.hasNext()) {
            Robot tempRobot = j.next();
            skillManager = tempRobot.getSkillManager();
            int skill = skillManager.getSkillLevel(SkillType.COOKING) * 5;
            skill += skillManager.getSkillLevel(SkillType.MATERIALS_SCIENCE) * 2;
            skill = (int) Math.round(skill / 7D);
            if (skill > highestSkillLevel) {
                highestSkillLevel = skill;
            }
        }

        return highestSkillLevel;
	}

	/**
	 * Gets an available food production building that the person can use. Returns
	 * null if no food production building is currently available.
	 * 
	 * @param person the person
	 * @return available food production building
	 */
	public static Building getAvailableFoodProductionBuilding(Person person) {

		SkillManager skillManager = person.getSkillManager();
        int skill = skillManager.getEffectiveSkillLevel(SkillType.COOKING) * 5;
        skill += skillManager.getEffectiveSkillLevel(SkillType.MATERIALS_SCIENCE) * 2;
        skill = (int) Math.round(skill / 7D);

		if (person.isInSettlement()) {
			BuildingManager buildingManager = person.getSettlement().getBuildingManager();
			Set<Building> foodProductionBuildings = buildingManager.getBuildingSet(FunctionType.FOOD_PRODUCTION);

			foodProductionBuildings = getFoodProductionBuildingsNeedingWork(foodProductionBuildings, skill);
			foodProductionBuildings = getBuildingsWithProcessesRequiringWork(foodProductionBuildings, skill);
			foodProductionBuildings = getHighestFoodProductionTechLevelBuildings(foodProductionBuildings);
			foodProductionBuildings = BuildingManager.getLeastCrowdedBuildings(foodProductionBuildings);

			if (!foodProductionBuildings.isEmpty()) {
				Map<Building, Double> foodProductionBuildingProbs = BuildingManager.getBestRelationshipBuildings(
						person, foodProductionBuildings);
				return RandomUtil.getWeightedRandomObject(foodProductionBuildingProbs);
			}
		}
		
		return null;
	}

	public static Building getAvailableFoodProductionBuilding(Robot robot) {

		Building result = null;
		int skill = robot.getProduceFoodSkill();

		if (robot.isInSettlement()) {
			BuildingManager buildingManager = robot.getSettlement().getBuildingManager();
			Set<Building> foodProductionBuildings = buildingManager.getBuildingSet(FunctionType.FOOD_PRODUCTION)
					.stream()
					.filter(b -> b.getZone() == robot.getBuildingLocation().getZone()
							&& !b.getMalfunctionManager().hasMalfunction())
					.collect(Collectors.toSet());

			foodProductionBuildings = getFoodProductionBuildingsNeedingWork(foodProductionBuildings, skill);
			foodProductionBuildings = getBuildingsWithProcessesRequiringWork(foodProductionBuildings, skill);
			foodProductionBuildings = getHighestFoodProductionTechLevelBuildings(foodProductionBuildings);
			if (RandomUtil.getRandomInt(2) == 0) // robot is not as inclined to move around
				foodProductionBuildings = BuildingManager.getLeastCrowded4BotBuildings(foodProductionBuildings);

			if (!foodProductionBuildings.isEmpty()) {
            	result = RandomUtil.getARandSet(foodProductionBuildings);
			}
		}

		return result;
	}
	/**
	 * Gets a list of food production buildings needing work from a list of
	 * buildings with the food production function.
	 * 
	 * @param buildingList list of buildings with the food production function.
	 * @param skill the materials science skill level of the person.
	 * @return list of food production buildings needing work.
	 */
	private static Set<Building> getFoodProductionBuildingsNeedingWork(
			Set<Building> buildingList, int skill) {

		Set<Building> result = new UnitSet<>();
		
		Iterator<Building> i = buildingList.iterator();
		while (i.hasNext()) {
			Building building = i.next();
			FoodProduction foodProductionFunction = building.getFoodProduction();
			if (foodProductionFunction.requiresWork(skill)) {
				result.add(building);
			}
		}

		return result;
	}

	/**
	 * Gets a subset list of food production buildings with processes requiring
	 * work.
	 * 
	 * @param buildingList the original building list.
	 * @param skill the materials science skill level of the person.
	 * @return subset list of buildings with processes requiring work, or
	 *         original list if none found.
	 */
	private static Set<Building> getBuildingsWithProcessesRequiringWork(
			Set<Building> buildingList, int skill) {

		Set<Building> result = new UnitSet<>();

		// Add all buildings with processes requiring work.
		Iterator<Building> i = buildingList.iterator();
		while (i.hasNext()) {
			Building building = i.next();
			if (hasProcessRequiringWork(building, skill)) {
				result.add(building);
			}
		}

		// If no building with processes requiring work, return original list.
		if (result.isEmpty()) {
			result = buildingList;
		}

		return result;
	}

	/**
	 * Checks if food production building has any processes requiring work.
	 * 
	 * @param foodProductionBuilding the food production building.
	 * @param skill the materials science skill level of the person.
	 * @return true if processes requiring work.
	 */
	public static boolean hasProcessRequiringWork(Building foodProductionBuilding, int skill) {

		boolean result = false;

		FoodProduction foodProductionFunction = foodProductionBuilding.getFoodProduction();
		for (FoodProductionProcess process : foodProductionFunction.getProcesses()) {
			boolean workRequired = (process.getWorkTimeRemaining() > 0D);
			boolean skillRequired = (process.getInfo().getSkillLevelRequired() <= skill);
			if (workRequired && skillRequired) result = true;
		}

		return result;
	}

	/**
	 * Gets a subset list of food production buildings with the highest tech level
	 * from a list of buildings with the food production function.
	 * 
	 * @param buildingList list of buildings with the food production function.
	 * @return subset list of highest tech level buildings.
	 */
	private static Set<Building> getHighestFoodProductionTechLevelBuildings(
			Set<Building> buildingList) {

		Set<Building> result = new UnitSet<>();

		int highestTechLevel = 0;
		for (Building building : buildingList) {
			FoodProduction foodProductionFunction = building.getFoodProduction();
			if (foodProductionFunction.getTechLevel() > highestTechLevel) {
				highestTechLevel = foodProductionFunction.getTechLevel();
			}
		}

		for (Building building : buildingList) {
			FoodProduction foodProductionFunction = building.getFoodProduction();
			if (foodProductionFunction.getTechLevel() == highestTechLevel) {
				result.add(building);
			}
		}

		return result;
	}

	/**
	 * Gets the highest food production process goods value for the person and the
	 * food production building.
	 * 
	 * @param person the person to perform food production.
	 * @param foodProductionBuilding the food production building.
	 * @return highest process good value.
	 */
	public static double getHighestFoodProductionProcessValue(Person person,
			Building foodProductionBuilding) {

		SkillManager skillManager = person.getSkillManager();
		int skill = skillManager.getEffectiveSkillLevel(SkillType.COOKING) * 5;
        skill += skillManager.getEffectiveSkillLevel(SkillType.MATERIALS_SCIENCE) * 2;
        skill = (int) Math.round(skill / 7D);

		return getHighestProcessValue(skill, foodProductionBuilding);
	}

	public static double getHighestFoodProductionProcessValue(Robot robot,
			Building foodProductionBuilding) {

	    SkillManager skillManager = robot.getSkillManager();
        int skill = skillManager.getEffectiveSkillLevel(SkillType.COOKING) * 5;
        skill += skillManager.getEffectiveSkillLevel(SkillType.MATERIALS_SCIENCE) * 2;
        skill = (int) Math.round(skill / 7D);

		return getHighestProcessValue(skill, foodProductionBuilding);
	}

	public static double getHighestProcessValue(int skillLevel, Building foodProductionBuilding) {
		// This method has 45.5% cpu util
		double highestProcessValue = 0D;
		FoodProduction foodProductionFunction = foodProductionBuilding.getFoodProduction();
		int techLevel = foodProductionFunction.getTechLevel();

		for (FoodProductionProcessInfo process : FoodProductionUtil.getProcessesForTechSkillLevel(
				techLevel, skillLevel)) {

			if (isProcessRunning(process, foodProductionFunction)
					// This method has 29.2% cpu util
					||FoodProductionUtil.canProcessBeStarted(process, foodProductionFunction)) {
				
				Settlement settlement = foodProductionBuilding.getSettlement();
				
				// This method has 16.2% cpu util
				double processValue = FoodProductionUtil.getFoodProductionProcessValue(process, settlement);
				
				if (processValue > highestProcessValue) {
					highestProcessValue = processValue;
				}
			}
		}

		return highestProcessValue;

	}

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		}
		else if (PRODUCE_FOOD.equals(getPhase())) {
			return foodProductionPhase(time);
		}
		else {
			return time;
		}
	}

	/**
	 * Performs the food production phase.
	 * 
	 * @param time the time to perform (millisols)
	 * @return remaining time after performing (millisols)
	 */
	private double foodProductionPhase(double time) {
		
		if (worker.isOutside()) {
			endTask();
			return 0;
		}
		
		// Check if foodFactory has malfunction.
		if (foodFactory.getBuilding().getMalfunctionManager().hasMalfunction()) {
			endTask();
			return 0;
		}

        // Cancel any food production processes that's beyond the skill of anyone
        // associated with the settlement.
        ProduceFood.cancelDifficultFoodProductionProcesses(foodFactory.getBuilding().getSettlement());
        
        double workTime = 0;

		if (person != null) {
	        workTime = time;
		}
		else if (robot != null) {
		     // A robot moves slower than a person and incurs penalty on workTime
	        workTime = time/2;
		}

		int skill = getEffectiveSkillLevel();
		if (skill == 0) {
			workTime /= 2;
		}
		else {
			workTime += workTime * (.2D * skill);
		}
		
		FoodProductionProcess process = null;
		
		// Apply work time to food production processes.
		while (workTime > 0D && !isDone() && getTimeLeft() > 0) {
			process = getRunningFoodProductionProcess();
			if (process != null) {
				double remainingWorkTime = process.getWorkTimeRemaining();
				double providedWorkTime = workTime;
				if (providedWorkTime > remainingWorkTime) {
					providedWorkTime = remainingWorkTime;
				}
				process.addWorkTime(providedWorkTime);
				workTime -= providedWorkTime;

				if ((process.getWorkTimeRemaining() <= 0D) &&
						(process.getProcessTimeRemaining() <= 0D)) {
					foodFactory.endFoodProductionProcess(process, false);
				}
				
				// Insert process into setDescription()
				setDescription(DETAIL + Conversion.capitalize(process.toString()));
				
				if (person != null)
					logger.log(person, Level.FINE, 30_000, "Worked on '" + process.getInfo().getName() + "'.");
				else
					logger.log(robot, Level.FINE, 30_000, "Worked on '" + process.getInfo().getName() + "'.");
				
			} else {
				if (!worker.getAssociatedSettlement().getProcessOverride(OverrideType.FOOD_PRODUCTION)) {
					process = createNewFoodProductionProcess();
				}

				if (process == null) {
					endTask();
					setDescription(CHECKING); 
					return 0;
				}
			}		
		}

		// Add experience
		addExperience(time);

		// Check for accident in foodFactory.
		checkForAccident(foodFactory.getBuilding(), time, 0.003);
		
		return 0D;
	}

	/**
	 * Gets an available running food production process.
	 * 
	 * @return process or null if none.
	 */
	private FoodProductionProcess getRunningFoodProductionProcess() {
		FoodProductionProcess result = null;

		int skillLevel = getEffectiveSkillLevel();
		for (FoodProductionProcess process : foodFactory.getProcesses()) {
			if ((process.getInfo().getSkillLevelRequired() <= skillLevel) &&
					(process.getWorkTimeRemaining() > 0D)) {
				result = process;
			}
		}

		return result;
	}

	/**
	 * Checks if a process type is currently running at a food production
	 * building.
	 * 
	 * @param processInfo the process type.
	 * @param foodProductionBuilding the food production building.
	 * @return true if process is running.
	 */
	private static boolean isProcessRunning(FoodProductionProcessInfo processInfo,
			FoodProduction foodProductionBuilding) {
		boolean result = false;

		for (FoodProductionProcess process : foodProductionBuilding.getProcesses()) {
			if (process.getInfo().getName().equals(processInfo.getName())) {
				result = true;
			}
		}

		return result;
	}

	/**
	 * Creates a new food production process if possible.
	 * @return the new food production process or null if none.
	 */
	private FoodProductionProcess createNewFoodProductionProcess() {
		FoodProductionProcess result = null;

		if (foodFactory.getMaxProcesses() < foodFactory.getCurrentTotalProcesses()) {
			return result;
		}

		int skillLevel = getEffectiveSkillLevel();
		int techLevel = foodFactory.getTechLevel();

		// Determine all food production processes that are possible and profitable.
		Map<FoodProductionProcessInfo, Double> processProbMap = new ConcurrentHashMap<>();
		for (FoodProductionProcessInfo processInfo : FoodProductionUtil.getProcessesForTechSkillLevel(
				techLevel, skillLevel)) {
			if (FoodProductionUtil.canProcessBeStarted(processInfo, foodFactory)) {
				double processValue = FoodProductionUtil.getFoodProductionProcessValue(processInfo,
							worker.getSettlement());

				if (processValue > 0D) {
					processProbMap.put(processInfo, processValue);
				}
			}
		}

		// Randomly choose among possible food production processes based on their relative profitability.
		FoodProductionProcessInfo chosenProcess = null;
		if (!processProbMap.isEmpty()) {
			chosenProcess = RandomUtil.getWeightedRandomObject(processProbMap);
		}

		// Create chosen food production process.
		if (chosenProcess != null) {
			result = new FoodProductionProcess(chosenProcess, foodFactory);
			foodFactory.addProcess(result);
		}

		return result;
	}
}
