/**
 * Mars Simulation Project
 * ManufactureConstructionMaterials.java
 * @version 3.1.0 2017-09-13
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.manufacture.ManufactureProcess;
import org.mars_sim.msp.core.manufacture.ManufactureProcessInfo;
import org.mars_sim.msp.core.manufacture.ManufactureProcessItem;
import org.mars_sim.msp.core.manufacture.ManufactureUtil;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.ItemType;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.Manufacture;
import org.mars_sim.msp.core.structure.construction.ConstructionStageInfo;
import org.mars_sim.msp.core.structure.construction.ConstructionUtil;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * A task for working on a manufacturing process to produce construction
 * materials.
 */
public class ManufactureConstructionMaterials extends Task implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
//	private static Logger logger = Logger.getLogger(ManufactureConstructionMaterials.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.manufactureConstructionMaterials"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase MANUFACTURE = new TaskPhase(Msg.getString("Task.phase.manufacture")); //$NON-NLS-1$

	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = .1D;

//	/** List of construction material resources. */
//	private List<Integer> constructionResources;
//	/** List of construction material parts. */
//	private List<Integer> constructionParts;

	// Data members
	/** The manufacturing workshop the person is using. */
	private Manufacture workshop;

	/**
	 * Constructor.
	 * 
	 * @param person the person to perform the task
	 */
	public ManufactureConstructionMaterials(Person person) {
		super(NAME, person, true, false, STRESS_MODIFIER, true, 25);

		// Initialize data members
		if (person.getSettlement() != null) {
			setDescription(Msg.getString("Task.description.manufactureConstructionMaterials.detail",
					person.getSettlement().getName())); // $NON-NLS-1$
		} else {
			endTask();
		}

		// Get available manufacturing workshop if any.
		Building manufactureBuilding = getAvailableManufacturingBuilding(person);
		if (manufactureBuilding != null) {
			workshop = manufactureBuilding.getManufacture();

			// Walk to manufacturing building.
			walkToActivitySpotInBuilding(manufactureBuilding, false);
		} else {
			endTask();
		}

		// Initialize phase
		addPhase(MANUFACTURE);
		setPhase(MANUFACTURE);
	}

	/**
	 * Constructor.
	 * 
	 * @param person the person to perform the task
	 */
	public ManufactureConstructionMaterials(Robot robot) {
		super(NAME, robot, true, false, STRESS_MODIFIER, true, 10D + RandomUtil.getRandomDouble(50D));

		// Initialize data members
		if (robot.getSettlement() != null) {
			setDescription(Msg.getString("Task.description.manufactureConstructionMaterials.detail",
					robot.getSettlement().getName())); // $NON-NLS-1$
		} else {
			endTask();
		}

		// Get available manufacturing workshop if any.
		Building manufactureBuilding = getAvailableManufacturingBuilding(robot);
		if (manufactureBuilding != null) {
			workshop = manufactureBuilding.getManufacture(); // (Manufacture)
																// manufactureBuilding.getFunction(FunctionType.MANUFACTURE);

			// Walk to manufacturing building.
			walkToActivitySpotInBuilding(manufactureBuilding, false);
		} else {
			endTask();
		}

		// Initialize phase
		addPhase(MANUFACTURE);
		setPhase(MANUFACTURE);
	}

	@Override
	public FunctionType getLivingFunction() {
		return FunctionType.MANUFACTURE;
	}

	/**
	 * Gets an available manufacturing building that the person can use. Returns
	 * null if no manufacturing building is currently available.
	 * 
	 * @param person the person
	 * @return available manufacturing building
	 */
	public static Building getAvailableManufacturingBuilding(Person person) {

		Building result = null;

		SkillManager skillManager = person.getSkillManager();
		int skill = skillManager.getEffectiveSkillLevel(SkillType.MATERIALS_SCIENCE);

		if (person.isInSettlement()) {// .getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
			BuildingManager manager = person.getSettlement().getBuildingManager();
			List<Building> manufacturingBuildings = manager.getBuildings(FunctionType.MANUFACTURE);
			manufacturingBuildings = BuildingManager.getNonMalfunctioningBuildings(manufacturingBuildings);
			manufacturingBuildings = getManufacturingBuildingsNeedingWork(manufacturingBuildings, skill);
			manufacturingBuildings = getBuildingsWithProcessesRequiringWork(manufacturingBuildings, skill);
			manufacturingBuildings = getHighestManufacturingTechLevelBuildings(manufacturingBuildings);
			manufacturingBuildings = BuildingManager.getLeastCrowdedBuildings(manufacturingBuildings);

			if (manufacturingBuildings.size() > 0) {
				Map<Building, Double> manufacturingBuildingProbs = BuildingManager.getBestRelationshipBuildings(person,
						manufacturingBuildings);
				result = RandomUtil.getWeightedRandomObject(manufacturingBuildingProbs);
			}
		}

		return result;
	}

	public static Building getAvailableManufacturingBuilding(Robot robot) {

		Building result = null;

		SkillManager skillManager = robot.getSkillManager();
		int skill = skillManager.getEffectiveSkillLevel(SkillType.MATERIALS_SCIENCE);

		if (robot.isInSettlement()) {// .getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
			BuildingManager manager = robot.getSettlement().getBuildingManager();
			List<Building> manufacturingBuildings = manager.getBuildings(FunctionType.MANUFACTURE);
			manufacturingBuildings = BuildingManager.getNonMalfunctioningBuildings(manufacturingBuildings);
			manufacturingBuildings = getManufacturingBuildingsNeedingWork(manufacturingBuildings, skill);
			manufacturingBuildings = getBuildingsWithProcessesRequiringWork(manufacturingBuildings, skill);
			manufacturingBuildings = getHighestManufacturingTechLevelBuildings(manufacturingBuildings);
			manufacturingBuildings = BuildingManager.getLeastCrowdedBuildings(manufacturingBuildings);

			if (manufacturingBuildings.size() > 0) {
//                Map<Building, Double> manufacturingBuildingProbs = BuildingManager.
//                        getBestRelationshipBuildings(robot, manufacturingBuildings);
				result = manufacturingBuildings.get(RandomUtil.getRandomInt(manufacturingBuildings.size() - 1));
			}
		}

		return result;
	}

	/**
	 * Gets a list of manufacturing buildings needing work from a list of buildings
	 * with the manufacture function.
	 * 
	 * @param buildingList list of buildings with the manufacture function.
	 * @param skill        the materials science skill level of the person.
	 * @return list of manufacture buildings needing work.
	 */
	private static List<Building> getManufacturingBuildingsNeedingWork(List<Building> buildingList, int skill) {

		List<Building> result = new ArrayList<Building>();

		Iterator<Building> i = buildingList.iterator();
		while (i.hasNext()) {
			Building building = i.next();
			Manufacture manufacturingFunction = building.getManufacture(); // (Manufacture)
																			// building.getFunction(FunctionType.MANUFACTURE);
			if (manufacturingFunction.requiresManufacturingWork(skill)) {
				result.add(building);
			}
		}

		return result;
	}

	/**
	 * Gets a subset list of manufacturing buildings with processes requiring work.
	 * 
	 * @param buildingList the original building list.
	 * @param skill        the materials science skill level of the person.
	 * @return subset list of buildings with processes requiring work, or original
	 *         list if none found.
	 */
	private static List<Building> getBuildingsWithProcessesRequiringWork(List<Building> buildingList, int skill) {

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
	 * Checks if manufacturing building has any processes requiring work.
	 * 
	 * @param manufacturingBuilding the manufacturing building.
	 * @param skill                 the materials science skill level of the person.
	 * @return true if processes requiring work.
	 */
	public static boolean hasProcessRequiringWork(Building manufacturingBuilding, int skill) {

		boolean result = false;

		Manufacture manufacturingFunction = manufacturingBuilding.getManufacture();// (Manufacture)
																					// manufacturingBuilding.getFunction(FunctionType.MANUFACTURE);
		Iterator<ManufactureProcess> i = manufacturingFunction.getProcesses().iterator();
		while (i.hasNext()) {
			ManufactureProcess process = i.next();
			if (producesConstructionMaterials(process)) {
				boolean workRequired = (process.getWorkTimeRemaining() > 0D);
				boolean skillRequired = (process.getInfo().getSkillLevelRequired() <= skill);
				if (workRequired && skillRequired) {
					result = true;
				}
			}
		}

		return result;
	}

	/**
	 * Checks if a manufacture process produces construction materials.
	 * 
	 * @param process the manufacture process.
	 * @return true if produces construction materials.
	 */
	private static boolean producesConstructionMaterials(ManufactureProcess process) {
		return producesConstructionMaterials(process.getInfo());
	}

	/**
	 * Gets a subset list of manufacturing buildings with the highest tech level
	 * from a list of buildings with the manufacture function.
	 * 
	 * @param buildingList list of buildings with the manufacture function.
	 * @return subset list of highest tech level buildings.
	 */
	private static List<Building> getHighestManufacturingTechLevelBuildings(List<Building> buildingList) {

		List<Building> result = new ArrayList<Building>();

		int highestTechLevel = 0;
		Iterator<Building> i = buildingList.iterator();
		while (i.hasNext()) {
			Building building = i.next();
			Manufacture manufacturingFunction = building.getManufacture();// (Manufacture)
																			// building.getFunction(FunctionType.MANUFACTURE);
			if (manufacturingFunction.getTechLevel() > highestTechLevel) {
				highestTechLevel = manufacturingFunction.getTechLevel();
			}
		}

		Iterator<Building> j = buildingList.iterator();
		while (j.hasNext()) {
			Building building = j.next();
			Manufacture manufacturingFunction = building.getManufacture();// (Manufacture)
																			// building.getFunction(FunctionType.MANUFACTURE);
			if (manufacturingFunction.getTechLevel() == highestTechLevel) {
				result.add(building);
			}
		}

		return result;
	}

	/**
	 * Gets the highest manufacturing process goods value for the person and the
	 * manufacturing building.
	 * 
	 * @param person                the person to perform manufacturing.
	 * @param manufacturingBuilding the manufacturing building.
	 * @return highest process good value.
	 */
	public static double getHighestManufacturingProcessValue(Person person, Building manufacturingBuilding) {

		double highestProcessValue = 0D;

		int skillLevel = person.getSkillManager().getEffectiveSkillLevel(SkillType.MATERIALS_SCIENCE);

		Manufacture manufacturingFunction = manufacturingBuilding.getManufacture();
		int techLevel = manufacturingFunction.getTechLevel();

		Iterator<ManufactureProcessInfo> i = ManufactureUtil
				.getManufactureProcessesForTechSkillLevel(techLevel, skillLevel).iterator();
		while (i.hasNext()) {
			ManufactureProcessInfo process = i.next();
			if (ManufactureUtil.canProcessBeStarted(process, manufacturingFunction)
					|| isProcessRunning(process, manufacturingFunction)) {
				if (producesConstructionMaterials(process)) {
					Settlement settlement = manufacturingBuilding.getSettlement();
					double processValue = ManufactureUtil.getManufactureProcessValue(process, settlement);
					if (processValue > highestProcessValue) {
						highestProcessValue = processValue;
					}
				}
			}
		}

		return highestProcessValue;
	}

	/**
	 * Gets the highest manufacturing process goods value for the person and the
	 * manufacturing building.
	 * 
	 * @param person                the person to perform manufacturing.
	 * @param manufacturingBuilding the manufacturing building.
	 * @return highest process good value.
	 */
	public static double getHighestManufacturingProcessValue(Robot robot, Building manufacturingBuilding) {

		double highestProcessValue = 0D;

		int skillLevel = robot.getSkillManager().getEffectiveSkillLevel(SkillType.MATERIALS_SCIENCE);

		Manufacture manufacturingFunction = manufacturingBuilding.getManufacture();// (Manufacture)
																					// manufacturingBuilding.getFunction(FunctionType.MANUFACTURE);
		int techLevel = manufacturingFunction.getTechLevel();

		Iterator<ManufactureProcessInfo> i = ManufactureUtil
				.getManufactureProcessesForTechSkillLevel(techLevel, skillLevel).iterator();
		while (i.hasNext()) {
			ManufactureProcessInfo process = i.next();
			if (ManufactureUtil.canProcessBeStarted(process, manufacturingFunction)
					|| isProcessRunning(process, manufacturingFunction)) {
				if (producesConstructionMaterials(process)) {
					Settlement settlement = manufacturingBuilding.getSettlement();
					double processValue = ManufactureUtil.getManufactureProcessValue(process, settlement);
					if (processValue > highestProcessValue) {
						highestProcessValue = processValue;
					}
				}
			}
		}

		return highestProcessValue;
	}

	/**
	 * Checks if a manufacture process produces construction materials.
	 * 
	 * @param process the manufacture process.
	 * @return true if produces construction materials.
	 */
	private static boolean producesConstructionMaterials(ManufactureProcessInfo info) {
		boolean result = false;
//		if (constructionResources == null) {
//			determineConstructionResources();
//		}
//		if (constructionParts == null) {
//			determineConstructionParts();
//		}
		List<Integer> constructionResources = determineConstructionResources();
		
		List<Integer> constructionParts = determineConstructionParts();
		
		Iterator<ManufactureProcessItem> i = info.getOutputList().iterator();
		while (!result && i.hasNext()) {
			ManufactureProcessItem item = i.next();
			if (ItemType.AMOUNT_RESOURCE.equals(item.getType())) {
				int resource = ResourceUtil.findIDbyAmountResourceName(item.getName());
				if (constructionResources.contains(resource)) {
					result = true;
				}
			} else if (ItemType.PART.equals(item.getType())) {
				int part = ItemResourceUtil.findIDbyItemResourceName(item.getName());
				if (constructionParts.contains(part)) {
					result = true;
				}
			}
		}

		return result;
	}

	/**
	 * Determines all resources needed for construction projects.
	 */
	private static List<Integer> determineConstructionResources() {
		List<Integer> constructionResources = new ArrayList<>();

		Iterator<ConstructionStageInfo> i = ConstructionUtil.getAllConstructionStageInfoList().iterator();
		while (i.hasNext()) {
			ConstructionStageInfo info = i.next();
			if (info.isConstructable()) {
				Iterator<Integer> j = info.getResources().keySet().iterator();
				while (j.hasNext()) {
					Integer resource = j.next();
					if (!constructionResources.contains(resource)) {
						constructionResources.add(resource);
					}
				}
			}
		}
		
		return constructionResources;
	}

	/**
	 * Determines all parts needed for construction projects.
	 */
	private static List<Integer> determineConstructionParts() {
		List<Integer> constructionParts = new ArrayList<>();

		Iterator<ConstructionStageInfo> i = ConstructionUtil.getAllConstructionStageInfoList().iterator();
		while (i.hasNext()) {
			ConstructionStageInfo info = i.next();
			if (info.isConstructable()) {
				Iterator<Integer> j = info.getParts().keySet().iterator();
				while (j.hasNext()) {
					Integer part = j.next();
					if (!constructionParts.contains(part)) {
						constructionParts.add(part);
					}
				}
			}
		}
		
		return constructionParts;
	}

	@Override
	protected void addExperience(double time) {
		// Add experience to "Materials Science" and "Construction" skills
		// (1 base experience point per 100 millisols of work)
		// Experience points adjusted by person's "Experience Aptitude"
		// attribute.
		double newPoints = time / 100D;
		int experienceAptitude = person.getNaturalAttributeManager()
				.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
		newPoints += newPoints * ((double) experienceAptitude - 50D) / 100D;
		newPoints *= getTeachingExperienceModifier();
		person.getSkillManager().addExperience(SkillType.MATERIALS_SCIENCE, newPoints / 2D, time);
		person.getSkillManager().addExperience(SkillType.CONSTRUCTION, newPoints / 2D, time);
	}

	@Override
	public List<SkillType> getAssociatedSkills() {
		List<SkillType> results = new ArrayList<SkillType>(2);
		results.add(SkillType.MATERIALS_SCIENCE);
		results.add(SkillType.CONSTRUCTION);
		return results;
	}

	@Override
	public int getEffectiveSkillLevel() {
		double result = 0;
		SkillManager manager = person.getSkillManager();
		result += manager.getEffectiveSkillLevel(SkillType.MATERIALS_SCIENCE);
		result += manager.getEffectiveSkillLevel(SkillType.CONSTRUCTION);
		return (int) Math.round(result / 2D);
	}

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		} else if (MANUFACTURE.equals(getPhase())) {
			return manufacturePhase(time);
		} else {
			return time;
		}
	}

	/**
	 * Perform the manufacturing phase.
	 * 
	 * @param time the time to perform (millisols)
	 * @return remaining time after performing (millisols)
	 */
	private double manufacturePhase(double time) {

		if (person != null) {
			if (person.isOutside()) {
				endTask();
				return 0;
			}
		}
		else if (robot != null) {
			if (robot.isOutside()) {
				endTask();
				return 0;
			}
		}
		
		// Check if workshop has malfunction.
		if (workshop.getBuilding().getMalfunctionManager().hasMalfunction()) {
			endTask();
			return 0;
		}

		// Determine amount of effective work time based on "Materials Science"
		// skill.
		double workTime = time;
		int skill = getEffectiveSkillLevel();
		if (skill == 0) {
			workTime /= 2;
		} else {
			workTime += workTime * (.2D * (double) skill);
		}

		// Apply work time to manufacturing processes.
		while ((workTime > 0D) && !isDone()) {
			ManufactureProcess process = getRunningManufactureProcess();
			if (process != null) {
				double remainingWorkTime = process.getWorkTimeRemaining();
				double providedWorkTime = workTime;
				if (providedWorkTime > remainingWorkTime) {
					providedWorkTime = remainingWorkTime;
				}
				process.addWorkTime(providedWorkTime);
				workTime -= providedWorkTime;

				if ((process.getWorkTimeRemaining() <= 0D) && (process.getProcessTimeRemaining() <= 0D)) {
					workshop.endManufacturingProcess(process, false);
				}
			} 
			
			else {

				if (person != null) {
					if (!person.getSettlement().getManufactureOverride())
						process = createNewManufactureProcess();
				}
				
				else if (robot != null) {
					if (!robot.getSettlement().getManufactureOverride())
						process = createNewManufactureProcess();
				}
				
				if (process == null) {
					endTask();
				}
			}

			if (process == null)
				// Prints description
				setDescription(Msg.getString("Task.description.manufactureConstructionMaterials.checking")); //$NON-NLS-1$
		}

		// Add experience
		addExperience(time);

		// Check for accident in workshop.
		checkForAccident(time);

		return 0D;
	}

	/**
	 * Gets an available running manufacturing process.
	 * 
	 * @return process or null if none.
	 */
	private ManufactureProcess getRunningManufactureProcess() {
		ManufactureProcess result = null;

		int skillLevel = getEffectiveSkillLevel();

		Iterator<ManufactureProcess> i = workshop.getProcesses().iterator();
		while (i.hasNext() && (result == null)) {
			ManufactureProcess process = i.next();
			if ((process.getInfo().getSkillLevelRequired() <= skillLevel) && (process.getWorkTimeRemaining() > 0D)
					&& producesConstructionMaterials(process)) {
				result = process;
			}
		}

		return result;
	}

	/**
	 * Checks if a process type is currently running at a manufacturing building.
	 * 
	 * @param processInfo         the process type.
	 * @param manufactureBuilding the manufacturing building.
	 * @return true if process is running.
	 */
	private static boolean isProcessRunning(ManufactureProcessInfo processInfo, Manufacture manufactureBuilding) {
		boolean result = false;

		Iterator<ManufactureProcess> i = manufactureBuilding.getProcesses().iterator();
		while (i.hasNext()) {
			ManufactureProcess process = i.next();
			if (process.getInfo().getName() == processInfo.getName()) {
				result = true;
			}
		}

		return result;
	}

	/**
	 * Creates a new manufacturing process if possible.
	 * 
	 * @return the new manufacturing process or null if none.
	 */
	private ManufactureProcess createNewManufactureProcess() {
		ManufactureProcess result = null;

		if (workshop.getTotalProcessNumber() < workshop.getSupportingProcesses()) {

			int skillLevel = getEffectiveSkillLevel();
			int techLevel = workshop.getTechLevel();

			// Determine all manufacturing processes that are possible and profitable.
			Map<ManufactureProcessInfo, Double> processProbMap = new HashMap<ManufactureProcessInfo, Double>();
			Iterator<ManufactureProcessInfo> i = ManufactureUtil
					.getManufactureProcessesForTechSkillLevel(techLevel, skillLevel).iterator();
			while (i.hasNext()) {
				ManufactureProcessInfo processInfo = i.next();

				if (ManufactureUtil.canProcessBeStarted(processInfo, workshop)
						&& producesConstructionMaterials(processInfo)) {
					double processValue = ManufactureUtil.getManufactureProcessValue(processInfo,
							person.getSettlement());
					if (processValue > 0D) {
						processProbMap.put(processInfo, processValue);
					}
				}
			}

			// Randomly choose among possible manufacturing processes based on their
			// relative profitability.
			ManufactureProcessInfo chosenProcess = null;
			if (!processProbMap.isEmpty()) {
				chosenProcess = RandomUtil.getWeightedRandomObject(processProbMap);
			}

			// Create chosen manufacturing process.
			if (chosenProcess != null) {
				result = new ManufactureProcess(chosenProcess, workshop);
				workshop.addProcess(result);

			}
		}

		return result;
	}

	/**
	 * Check for accident in manufacturing building.
	 * 
	 * @param time the amount of time working (in millisols)
	 */
	private void checkForAccident(double time) {

		double chance = .005D;

		// Materials science skill modification.
		int skill = getEffectiveSkillLevel();
		if (skill <= 3) {
			chance *= (4 - skill);
		} else {
			chance /= (skill - 2);
		}

		// Modify based on the workshop building's wear condition.
		chance *= workshop.getBuilding().getMalfunctionManager().getWearConditionAccidentModifier();

		if (RandomUtil.lessThanRandPercent(chance * time)) {

			if (person != null) {
//				logger.info("[" + person.getLocationTag().getShortLocationName() +  "] " + person.getName() + " has an accident while manufacturing construction materials.");
				workshop.getBuilding().getMalfunctionManager().createASeriesOfMalfunctions(person);
			} else if (robot != null) {
//				logger.info("[" + robot.getLocationTag().getShortLocationName() +  "] " + robot.getName() + " has an accident while manufacturing construction materials.");
				workshop.getBuilding().getMalfunctionManager().createASeriesOfMalfunctions(robot);
			}
		}
	}

	@Override
	public void destroy() {
		super.destroy();

		workshop = null;
	}
}