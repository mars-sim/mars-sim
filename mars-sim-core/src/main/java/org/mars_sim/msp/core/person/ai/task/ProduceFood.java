/**
 * Mars Simulation Project
 * FoodProductionGood.java
 * @version 3.07 2015-02-02
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.foodProduction.FoodProductionProcess;
import org.mars_sim.msp.core.foodProduction.FoodProductionProcessInfo;
import org.mars_sim.msp.core.foodProduction.FoodProductionUtil;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.NaturalAttribute;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RoboticAttribute;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.FoodProduction;

/**
 * A task for working on a foodProduction process.
 */
public class ProduceFood
extends Task
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(ProduceFood.class.getName());

	/** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.produceFood"); //$NON-NLS-1$
	
    /** Task phases. */
    private static final TaskPhase PRODUCE_FOOD = new TaskPhase(Msg.getString(
            "Task.phase.produceFood")); //$NON-NLS-1$

	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = .2D;

	// Data members
	/** The foodProduction foodFactory the person is using. */
	private FoodProduction foodFactory;

	private SkillManager skillManager;
	
	/**
	 * Constructor.
	 * @param person the person to perform the task
	 */
	public ProduceFood(Person person) {
		super(NAME, person, true, false, STRESS_MODIFIER, true, 
				10D + RandomUtil.getRandomDouble(50D));

		// Initialize data members
		if (person.getSettlement() != null) {
		    setDescription(Msg.getString("Task.description.produceFood.detail", 
                    person.getSettlement().getName())); //$NON-NLS-1$
		}
		else {
			endTask();
		}

		// Get available foodProduction foodFactory if any.
		Building foodProductionBuilding = getAvailableFoodProductionBuilding(person);
		if (foodProductionBuilding != null) {
			foodFactory = (FoodProduction) foodProductionBuilding.getFunction(BuildingFunction.FOOD_PRODUCTION);

			// Walk to foodProduction building.
			walkToActivitySpotInBuilding(foodProductionBuilding, false);
		} 
		else {
			endTask();
		}

		skillManager = person.getMind().getSkillManager();			
		
		// Initialize phase
		addPhase(PRODUCE_FOOD);
		setPhase(PRODUCE_FOOD);
	}
	
	public ProduceFood(Robot robot) {
		super(NAME, robot, true, false, STRESS_MODIFIER, true, 
				10D + RandomUtil.getRandomDouble(50D));

		// Initialize data members
		if (robot.getSettlement() != null) {
		    setDescription(Msg.getString("Task.description.produceFood.detail", 
                    robot.getSettlement().getName())); //$NON-NLS-1$
		}
		else {
			endTask();
		}

		// Get available foodProduction foodFactory if any.
		Building foodProductionBuilding = getAvailableFoodProductionBuilding(robot);
		if (foodProductionBuilding != null) {
			foodFactory = (FoodProduction) foodProductionBuilding.getFunction(BuildingFunction.FOOD_PRODUCTION);

			// Walk to foodProduction building.
			walkToActivitySpotInBuilding(foodProductionBuilding, false);
		} 
		else {
			endTask();
		}

		skillManager = robot.getBotMind().getSkillManager();
		
		// Initialize phase
		addPhase(PRODUCE_FOOD);
		setPhase(PRODUCE_FOOD);
	}
    @Override
    protected BuildingFunction getRelatedBuildingFunction() {
        return BuildingFunction.FOOD_PRODUCTION;
    }
    
    protected BuildingFunction getRelatedBuildingRoboticFunction() {
        return BuildingFunction.FOOD_PRODUCTION;
    }
    

	/** 
	 * Cancel any foodProduction processes that's beyond the skill of any people 
	 * associated with the settlement.
	 * @param person the person
	 */
	public static void cancelDifficultFoodProductionProcesses(Person person) {

		Settlement settlement = person.getSettlement();
		if (settlement != null) {
		    int highestSkillLevel = getHighestSkillAtSettlement(settlement);

			BuildingManager manager = person.getSettlement().getBuildingManager();
			Iterator<Building> j = manager.getBuildings(BuildingFunction.FOOD_PRODUCTION).iterator();
			while (j.hasNext()) {
				Building building = (Building) j.next();
				FoodProduction foodProductionFunction = (FoodProduction) building.getFunction(BuildingFunction.FOOD_PRODUCTION);
				List<FoodProductionProcess> processes = new ArrayList<FoodProductionProcess>(
						foodProductionFunction.getProcesses());
				Iterator<FoodProductionProcess> k = processes.iterator();
				while (k.hasNext()) {
					FoodProductionProcess process = k.next();
					int processSkillLevel = process.getInfo().getSkillLevelRequired();
					if (processSkillLevel > highestSkillLevel) {
						// Cancel foodProduction process.
						foodProductionFunction.endFoodProductionProcess(process, true);
					}
				}
			}
		}
	}

	public static void cancelDifficultFoodProductionProcesses(Robot robot) {

		Settlement settlement = robot.getSettlement();
		if (settlement != null) {
			int highestSkillLevel = getHighestSkillAtSettlement(settlement);

			BuildingManager buildingManager = robot.getSettlement().getBuildingManager();
			Iterator<Building> j = buildingManager.getBuildings(BuildingFunction.FOOD_PRODUCTION).iterator();
			while (j.hasNext()) {
				Building building = (Building) j.next();
				FoodProduction foodProductionFunction = (FoodProduction) building.getFunction(BuildingFunction.FOOD_PRODUCTION);
				List<FoodProductionProcess> processes = new ArrayList<FoodProductionProcess>(
						foodProductionFunction.getProcesses());
				Iterator<FoodProductionProcess> k = processes.iterator();
				while (k.hasNext()) {
					FoodProductionProcess process = k.next();
					int processSkillLevel = process.getInfo().getSkillLevelRequired();
					if (processSkillLevel > highestSkillLevel) {
						// Cancel foodProduction process.
						foodProductionFunction.endFoodProductionProcess(process, true);
					}
				}
			}
		}
	}
	
	/**
	 * Gets the highest skill level for food production at a settlement.
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
            //if (skillManager == null)
				skillManager = tempPerson.getMind().getSkillManager();
            int skill = skillManager.getSkillLevel(SkillType.COOKING) * 5;
            skill += skillManager.getSkillLevel(SkillType.MATERIALS_SCIENCE) * 2;
            skill = (int) Math.round(skill / 7D);
            if (skill > highestSkillLevel) {
                highestSkillLevel = skill;
            }
        }
	    
        skillManager = null;
        // Get highest robot skill level.
        Iterator<Robot> j = settlement.getAllAssociatedRobots().iterator();
        
        while (j.hasNext()) {
            Robot tempRobot = j.next();
            //if (skillManager == null)
            	skillManager = tempRobot.getBotMind().getSkillManager();
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
	 * Gets an available foodProduction building that the person can use. Returns
	 * null if no foodProduction building is currently available.
	 * @param person the person
	 * @return available foodProduction building
	 */
	public static Building getAvailableFoodProductionBuilding(Person person) {

		Building result = null;

		SkillManager skillManager = person.getMind().getSkillManager();
        int skill = skillManager.getEffectiveSkillLevel(SkillType.COOKING) * 5;
        skill += skillManager.getEffectiveSkillLevel(SkillType.MATERIALS_SCIENCE) * 2;
        skill = (int) Math.round(skill / 7D);

		if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
			BuildingManager buildingManager = person.getSettlement().getBuildingManager();
			List<Building> foodProductionBuildings = buildingManager.getBuildings(BuildingFunction.FOOD_PRODUCTION);
			foodProductionBuildings = BuildingManager.getNonMalfunctioningBuildings(foodProductionBuildings);
			foodProductionBuildings = getFoodProductionBuildingsNeedingWork(foodProductionBuildings, skill);
			foodProductionBuildings = getBuildingsWithProcessesRequiringWork(foodProductionBuildings, skill);
			foodProductionBuildings = getHighestFoodProductionTechLevelBuildings(foodProductionBuildings);
			foodProductionBuildings = BuildingManager.getLeastCrowdedBuildings(foodProductionBuildings);

			if (foodProductionBuildings.size() > 0) {
				Map<Building, Double> foodProductionBuildingProbs = BuildingManager.getBestRelationshipBuildings(
						person, foodProductionBuildings);
				result = RandomUtil.getWeightedRandomObject(foodProductionBuildingProbs);
			}
		}

		return result;
	}

	public static Building getAvailableFoodProductionBuilding(Robot robot) {

		Building result = null;
		int skill = robot.getProduceFoodSkill();
		//SkillManager skillManager = robot.getBotMind().getSkillManager();
		//int skill = skillManager.getEffectiveSkillLevel(SkillType.COOKING) * 5;
        //skill += skillManager.getEffectiveSkillLevel(SkillType.MATERIALS_SCIENCE) * 2;
        //skill = (int) Math.round(skill / 7D);

		if (robot.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
			BuildingManager buildingManager = robot.getSettlement().getBuildingManager();
			List<Building> foodProductionBuildings = buildingManager.getBuildings(BuildingFunction.FOOD_PRODUCTION);
			foodProductionBuildings = BuildingManager.getNonMalfunctioningBuildings(foodProductionBuildings);
			foodProductionBuildings = getFoodProductionBuildingsNeedingWork(foodProductionBuildings, skill);
			foodProductionBuildings = getBuildingsWithProcessesRequiringWork(foodProductionBuildings, skill);
			foodProductionBuildings = getHighestFoodProductionTechLevelBuildings(foodProductionBuildings);
			foodProductionBuildings = BuildingManager.getLeastCrowdedBuildings(foodProductionBuildings);

			if (foodProductionBuildings.size() > 0) {
				//Map<Building, Double> foodProductionBuildingProbs = BuildingManager.getBestRelationshipBuildings(
						//robot, foodProductionBuildings);
				//result = RandomUtil.getWeightedRandomObject(foodProductionBuildingProbs);
              	int selected = RandomUtil.getRandomInt(foodProductionBuildings.size()-1);
            	result = foodProductionBuildings.get(selected);			
			}
		}

		return result;
	}
	/**
	 * Gets a list of foodProduction buildings needing work from a list of
	 * buildings with the foodProduction function.
	 * @param buildingList list of buildings with the foodProduction function.
	 * @param skill the materials science skill level of the person.
	 * @return list of foodProduction buildings needing work.
	 */
	private static List<Building> getFoodProductionBuildingsNeedingWork(
			List<Building> buildingList, int skill) {

		List<Building> result = new ArrayList<Building>();

		Iterator<Building> i = buildingList.iterator();
		while (i.hasNext()) {
			Building building = i.next();
			FoodProduction foodProductionFunction = (FoodProduction) building.getFunction(BuildingFunction.FOOD_PRODUCTION);
			if (foodProductionFunction.requiresFoodProductionWork(skill)) {
				result.add(building);
			}
		}

		return result;
	}

	/**
	 * Gets a subset list of foodProduction buildings with processes requiring
	 * work.
	 * @param buildingList the original building list.
	 * @param skill the materials science skill level of the person.
	 * @return subset list of buildings with processes requiring work, or
	 *         original list if none found.
	 */
	private static List<Building> getBuildingsWithProcessesRequiringWork(
			List<Building> buildingList, int skill) {

		List<Building> result = new ArrayList<Building>();

		// Add all buildings with processes requiring work.
		Iterator<Building> i = buildingList.iterator();
		while (i.hasNext()) {
			Building building = i.next();
			if (hasProcessRequiringWork(building, skill)) {
				result.add(building);
			}
		}

		// If no building with processes requiring work, return original list.
		if (result.size() == 0) {
			result = buildingList;
		}

		return result;
	}

	/**
	 * Checks if foodProduction building has any processes requiring work.
	 * @param foodProductionBuilding the foodProduction building.
	 * @param skill the materials science skill level of the person.
	 * @return true if processes requiring work.
	 */
	public static boolean hasProcessRequiringWork(Building foodProductionBuilding, int skill) {

		boolean result = false;

		FoodProduction foodProductionFunction = (FoodProduction) foodProductionBuilding.getFunction(BuildingFunction.FOOD_PRODUCTION);
		Iterator<FoodProductionProcess> i = foodProductionFunction.getProcesses().iterator();
		while (i.hasNext()) {
			FoodProductionProcess process = i.next();
			boolean workRequired = (process.getWorkTimeRemaining() > 0D);
			boolean skillRequired = (process.getInfo().getSkillLevelRequired() <= skill);
			if (workRequired && skillRequired) result = true;
		}

		return result;
	}

	/**
	 * Gets a subset list of foodProduction buildings with the highest tech level
	 * from a list of buildings with the foodProduction function.
	 * @param buildingList list of buildings with the foodProduction function.
	 * @return subset list of highest tech level buildings.
	 */
	private static List<Building> getHighestFoodProductionTechLevelBuildings(
			List<Building> buildingList) {

		List<Building> result = new ArrayList<Building>();

		int highestTechLevel = 0;
		Iterator<Building> i = buildingList.iterator();
		while (i.hasNext()) {
			Building building = i.next();
			FoodProduction foodProductionFunction = (FoodProduction) building.getFunction(BuildingFunction.FOOD_PRODUCTION);
			if (foodProductionFunction.getTechLevel() > highestTechLevel) {
				highestTechLevel = foodProductionFunction.getTechLevel();
			}
		}

		Iterator<Building> j = buildingList.iterator();
		while (j.hasNext()) {
			Building building = j.next();
			FoodProduction foodProductionFunction = (FoodProduction) building.getFunction(BuildingFunction.FOOD_PRODUCTION);
			if (foodProductionFunction.getTechLevel() == highestTechLevel) {
				result.add(building);
			}
		}

		return result;
	}

	/**
	 * Gets the highest foodProduction process goods value for the person and the
	 * foodProduction building.
	 * @param person the person to perform foodProduction.
	 * @param foodProductionBuilding the foodProduction building.
	 * @return highest process good value.
	 */
	public static double getHighestFoodProductionProcessValue(Person person, 
			Building foodProductionBuilding) {

		SkillManager skillManager = person.getMind().getSkillManager();
		int skill = skillManager.getEffectiveSkillLevel(SkillType.COOKING) * 5;
        skill += skillManager.getEffectiveSkillLevel(SkillType.MATERIALS_SCIENCE) * 2;
        skill = (int) Math.round(skill / 7D);

		return getHighestProcessValue(skill, foodProductionBuilding);
	}

	public static double getHighestFoodProductionProcessValue(Robot robot, 
			Building foodProductionBuilding) {

	    SkillManager skillManager = robot.getBotMind().getSkillManager();
        int skill = skillManager.getEffectiveSkillLevel(SkillType.COOKING) * 5;
        skill += skillManager.getEffectiveSkillLevel(SkillType.MATERIALS_SCIENCE) * 2;
        skill = (int) Math.round(skill / 7D);

		return getHighestProcessValue(skill, foodProductionBuilding);
	}
	
	public static double getHighestProcessValue(int skillLevel, Building foodProductionBuilding) {
		
		double highestProcessValue = 0D;
		FoodProduction foodProductionFunction = (FoodProduction) foodProductionBuilding.getFunction(BuildingFunction.FOOD_PRODUCTION);
		int techLevel = foodProductionFunction.getTechLevel();

		Iterator<FoodProductionProcessInfo> i = FoodProductionUtil.getFoodProductionProcessesForTechSkillLevel(
				techLevel, skillLevel).iterator();
		while (i.hasNext()) {
			FoodProductionProcessInfo process = i.next();
			if (FoodProductionUtil.canProcessBeStarted(process, foodProductionFunction) || 
					isProcessRunning(process, foodProductionFunction)) {
				Settlement settlement = foodProductionBuilding.getBuildingManager().getSettlement();
				double processValue = FoodProductionUtil.getFoodProductionProcessValue(process, settlement);
				if (processValue > highestProcessValue) {
					highestProcessValue = processValue;
				}
			}
		}

		return highestProcessValue;
		
	}

	
	@Override
	protected void addExperience(double time) {
		// Add experience to "Materials Science" skill
		// (1 base experience point per 100 millisols of work)
		// Experience points adjusted by person's "Experience Aptitude"
		// attribute.
		double newPoints = time / 100D;
		if (person != null) {
			int experienceAptitude = person.getNaturalAttributeManager().getAttribute(NaturalAttribute.EXPERIENCE_APTITUDE);
			newPoints += newPoints * ((double) experienceAptitude - 50D) / 100D;
			newPoints *= getTeachingExperienceModifier();
	        skillManager.addExperience(
	                SkillType.COOKING, newPoints * 5 / 7D);
	        skillManager.addExperience(SkillType.MATERIALS_SCIENCE,
	                newPoints *2 / 7D);
		}
		else if (robot != null) {
			int experienceAptitude = robot.getRoboticAttributeManager().getAttribute(RoboticAttribute.EXPERIENCE_APTITUDE);
			newPoints += newPoints * ((double) experienceAptitude - 50D) / 100D;
			newPoints *= getTeachingExperienceModifier();
	        skillManager.addExperience(
	                SkillType.COOKING, newPoints * 5 / 7D);
	        skillManager.addExperience(SkillType.MATERIALS_SCIENCE,
	                newPoints *2 / 7D);
		}
	}

	@Override
	public List<SkillType> getAssociatedSkills() {
		List<SkillType> results = new ArrayList<SkillType>(1);
		results.add(SkillType.MATERIALS_SCIENCE);
		results.add(SkillType.COOKING);
		return results;
	}

	@Override
	public int getEffectiveSkillLevel() {
        double result = 0;
        //SkillManager manager = null;
		//if (person != null) 
		//	manager = person.getMind().getSkillManager();			
		//else if (robot != null)
		//	manager = robot.getBotMind().getSkillManager();	
		result += skillManager.getEffectiveSkillLevel(SkillType.COOKING) * 5;
		result += skillManager.getEffectiveSkillLevel(SkillType.MATERIALS_SCIENCE) * 2;
		
        return (int) Math.round(result / 7D);
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
	 * Perform the foodProduction phase.
	 * @param time the time to perform (millisols)
	 * @return remaining time after performing (millisols)
	 */
	private double foodProductionPhase(double time) {

		// Check if foodFactory has malfunction.
		if (foodFactory.getBuilding().getMalfunctionManager().hasMalfunction()) {
			endTask();
			return time;
		}

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
			workTime += workTime * (.2D * (double) skill);
		}

		// Apply work time to foodProduction processes.
		while ((workTime > 0D) && !isDone()) {
			FoodProductionProcess process = getRunningFoodProductionProcess();
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
			} else {
				
				if (person != null) {
					if (!person.getSettlement().getFoodProductionOverride()) {
						process = createNewFoodProductionProcess();
					}
				}
				else if (robot != null) {
					if (!robot.getSettlement().getFoodProductionOverride()) {
						process = createNewFoodProductionProcess();
					}
				}

				if (process == null) {
					endTask();
				}
			}
		}

		// Add experience
		addExperience(time);

		// Check for accident in foodFactory.
		checkForAccident(time);

		return 0D;
	}

	/**
	 * Gets an available running foodProduction process.
	 * @return process or null if none.
	 */
	private FoodProductionProcess getRunningFoodProductionProcess() {
		FoodProductionProcess result = null;

		int skillLevel = getEffectiveSkillLevel();

		Iterator<FoodProductionProcess> i = foodFactory.getProcesses().iterator();
		while (i.hasNext() && (result == null)) {
			FoodProductionProcess process = i.next();
			if ((process.getInfo().getSkillLevelRequired() <= skillLevel) && 
					(process.getWorkTimeRemaining() > 0D)) {
				result = process;
			}
		}

		return result;
	}

	/**
	 * Checks if a process type is currently running at a foodProduction
	 * building.
	 * @param processInfo the process type.
	 * @param foodProductionBuilding the foodProduction building.
	 * @return true if process is running.
	 */
	private static boolean isProcessRunning(FoodProductionProcessInfo processInfo, 
			FoodProduction foodProductionBuilding) {
		boolean result = false;

		Iterator<FoodProductionProcess> i = foodProductionBuilding.getProcesses().iterator();
		while (i.hasNext()) {
			FoodProductionProcess process = i.next();
			if (process.getInfo().getName() == processInfo.getName()) {
				result = true;
			}
		}

		return result;
	}

	/**
	 * Creates a new foodProduction process if possible.
	 * @return the new foodProduction process or null if none.
	 */
	private FoodProductionProcess createNewFoodProductionProcess() {
		FoodProductionProcess result = null;

		if (foodFactory.getTotalProcessNumber() < foodFactory.getConcurrentProcesses()) {

			int skillLevel = getEffectiveSkillLevel();
			int techLevel = foodFactory.getTechLevel();

			// Determine all foodProduction processes that are possible and profitable.
			Map<FoodProductionProcessInfo, Double> processProbMap = new HashMap<FoodProductionProcessInfo, Double>();
			Iterator<FoodProductionProcessInfo> i = FoodProductionUtil.getFoodProductionProcessesForTechSkillLevel(
					techLevel, skillLevel).iterator();
			while (i.hasNext()) {
				FoodProductionProcessInfo processInfo = i.next();
				if (FoodProductionUtil.canProcessBeStarted(processInfo, foodFactory)) {
					double processValue = 0;
					
					if (person != null) 
						processValue = FoodProductionUtil.getFoodProductionProcessValue(processInfo, 
								person.getSettlement());
					else if (robot != null)
						processValue = FoodProductionUtil.getFoodProductionProcessValue(processInfo, 
							robot.getSettlement());
					
					if (processValue > 0D) {
						processProbMap.put(processInfo, processValue);
					}
				}
			}

			// Randomly choose among possible foodProduction processes based on their relative profitability. 
			FoodProductionProcessInfo chosenProcess = null;
			if (!processProbMap.isEmpty()) {
				chosenProcess = RandomUtil.getWeightedRandomObject(processProbMap);
			}

			// Create chosen foodProduction process.
			if (chosenProcess != null) {
				result = new FoodProductionProcess(chosenProcess, foodFactory);
				foodFactory.addProcess(result);
			}
		}

		return result;
	}

	/**
	 * Check for accident in foodProduction building.
	 * @param time the amount of time working (in millisols)
	 */
	private void checkForAccident(double time) {

		double chance = .001D;

		// Materials science skill modification.
		int skill = getEffectiveSkillLevel();
		if (skill <= 3) {
			chance *= (4 - skill);
		}
		else {
			chance /= (skill - 2);
		}

		// Modify based on the foodFactory building's wear condition.
		chance *= foodFactory.getBuilding().getMalfunctionManager().getWearConditionAccidentModifier();

		if (RandomUtil.lessThanRandPercent(chance * time)) {
			if (person != null) 
				logger.info(person.getName() + " has accident while performing food production.");				
			else if (robot != null)
				logger.info(robot.getName() + " has accident while performing food production.");
			
			foodFactory.getBuilding().getMalfunctionManager().accident();
		}
	}

	@Override
	public void destroy() {
		super.destroy();

		foodFactory = null;
	}
}