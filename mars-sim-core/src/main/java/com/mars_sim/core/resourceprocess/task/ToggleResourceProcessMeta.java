/*
 * Mars Simulation Project
 * ToggleResourceProcessMeta.java
 * @date 2026-07-15
 * @author Scott Davis
 */
package com.mars_sim.core.resourceprocess.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.goods.GoodsManager;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.task.util.MetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementMetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskUtil;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.resourceprocess.ResourceProcess;
import com.mars_sim.core.resourceprocess.ResourceProcessAssessment;
import com.mars_sim.core.resourceprocess.ResourceProcessSpec;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.robot.RobotType;
import com.mars_sim.core.structure.OverrideType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.MathUtils;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;

/**
 * Meta task for the ToggleResourceProcess task.
 */
public class ToggleResourceProcessMeta extends MetaTask implements SettlementMetaTask {
	
	/**
	 * Represents a job to toggle a Resource process in a building.
	 */
    private static class ToggleOffJob extends SettlementTask {
		
		private static final long serialVersionUID = 1L;
		
		private ResourceProcess process;
		
        public ToggleOffJob(SettlementMetaTask mt, Settlement owner, Building processBuilding,
						ResourceProcess process,
						RatingScore score) {
			super(mt, owner, "Toggle Off "
								+ process.getProcessName(), processBuilding, score);
			this.process = process;
        }

        @Override
        public Task createTask(Person person) {
            return new ToggleResourceProcess(person, (Building) getFocus(), process);
        }

        @Override
        public Task createTask(Robot robot) {
            return new ToggleResourceProcess(robot, (Building) getFocus(), process);
        }
		
 		@Override
		public int hashCode() {
			return process.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (super.equals(obj)) {
				// Same building & meta task so compare on Process
				ToggleOffJob other = (ToggleOffJob) obj;
				return process.equals(other.process);
			}
			return false;
		}
    }

    private static class ToggleOnJob extends SettlementTask {
		
		private static final long serialVersionUID = 1L;
		
		private ResourceProcessSpec spec;
		private boolean useWaste;

		public ToggleOnJob(SettlementMetaTask mt, Settlement owner, boolean useWaste,
							ResourceProcessSpec process, RatingScore score) {
			super(mt, owner, "Toggle On " + process.getName(), null, score);
			this.spec = process;
			this.useWaste = useWaste;
        }

        @Override
        public Task createTask(Person person) {
            return new ToggleResourceProcess(person, useWaste, spec);
        }

        @Override
        public Task createTask(Robot robot) {
            return new ToggleResourceProcess(robot, useWaste, spec);
        }
		
 		@Override
		public int hashCode() {
			return super.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (super.equals(obj)) {
				// Same building & meta task so compare on Process
				ToggleOnJob other = (ToggleOnJob) obj;
				return spec.equals(other.spec);
			}
			return false;
		}
    }

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.toggleResourceProcess"); //$NON-NLS-1$
	
	private static final String TOGGLE_TIME = "toggleTime";
	
	private static final double MIN_SCORE = 0.05;
	private static final double MAX_SCORE = 500;
	
	private static final double WASTE_THRESHOLD = 0.3; // % waste need to be available to toggle
	
	private static final double GOD_BIAS = 2048;	
//	private static final double OMNI_BIAS = 1792;
//	private static final double HOVERING = 1536;
	private static final double SIGNIFICANT = 1024;	
	private static final double OVERWHELMING = 768;
	private static final double EXCEEDING = 512;	
	private static final double SUPREME = 256;	
//	private static final double TRENDY = 192;	
	private static final double EXTREME = 128;
	private static final double MEGA = 64;
	private static final double SUPER = 32;
	private static final double GOOD = 16;
	private static final double MID = 8;
	
	private static Map<Integer, Double> moduleFactor = new HashMap<>();
	
    public ToggleResourceProcessMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.ANY_HOUR);
		setFavorite(FavoriteType.TINKERING);
		setPreferredJob(JobType.TECHNICIAN, JobType.ENGINEER);

		addPreferredRobot(RobotType.REPAIRBOT, RobotType.CONSTRUCTIONBOT, 
				RobotType.MAKERBOT, RobotType.MEDICBOT, RobotType.DELIVERYBOT);
	}

	/**
	 * Robots can toggle resource processes.
	 * 
	 * @param t Task 
	 * @param r Robot making the request
	 */
	@Override
	public RatingScore assessRobotSuitability(SettlementTask t, Robot r)  {
        return TaskUtil.assessRobot(t, r);
    }

	/**
	 * Builds a list of TaskJob covering the most suitable Resource Processes to toggle.
	 * 
	 * @param settlement Settlement to check
	 */
	@Override
	public List<SettlementTask> getSettlementTasks(Settlement settlement) {
		List<SettlementTask> tasks = new ArrayList<>();

		Map<ResourceProcessSpec, ResourceProcessAssessment> assessed = new HashMap<>();

		if (!settlement.getProcessOverride(OverrideType.RESOURCE_PROCESS)) {
			Set<Building> buildingSet = settlement.getBuildingManager().getBuildingSet(FunctionType.RESOURCE_PROCESSING);
			
			for (Building building: buildingSet) {
				selectToggableProcesses(building, false, tasks, assessed);
			}
		}

		if (!settlement.getProcessOverride(OverrideType.WASTE_PROCESSING)) {
			Set<Building> buildingSet = settlement.getBuildingManager().getBuildingSet(FunctionType.WASTE_PROCESSING);
			
			for (Building building: buildingSet) {
				selectToggableProcesses(building, true, tasks, assessed);
			}
		}

		return tasks;
	}

	/**
	 * Register any resource/waste process (from a building) based on its resource score.
	 *
	 * @param building
	 * @param isWaste
	 * @param rate0
	 * @param rate1
	 * @param results Holds the list of Task created
	 * @param assessed 
	 */
	private void selectToggableProcesses(Building building, boolean isWaste, List<SettlementTask> results,
			Map<ResourceProcessSpec, ResourceProcessAssessment> assessed) {

		List<SettlementTask> toggleOffTasks = new ArrayList<>();
		Map<SettlementTask, Double> scoreMap = new HashMap<>();
		
		List<ResourceProcess> processes = null;
		if (isWaste) {
			processes = building.getWasteProcessing().getProcesses();
		}
		else
			processes = building.getResourceProcessing().getProcesses();
		
		// Shuffle the list random to vary which process to pick first
		Collections.shuffle(processes);
		var settlement = building.getSettlement();

		int count = 0;

		Collections.shuffle(processes);
		
		for (ResourceProcess process : processes) {
			// Avoid process that can't be toggled or no point toggling
			if (process.canToggle() && !process.isWorkerAssigned()) {

				if (process.isProcessRunning()) {
	
					if (process.getOverallScore() < 1) {
						toggleOffTasks.add(new ToggleOffJob(this, settlement, building, process, new RatingScore(1)));
					}
					// Note: Allow a running process to stop once in a while in order to reduce wear and tear
					// Reduce the likelihood of having to submit ToggleOffJob all the time
//					else if (count == 0 && !process.isProcessLockOn()) {
//						
//						count++;
//					
//						// Note: Pick only the first process
//						var score = new RatingScore(1);
//						// diff is 333 at max
//						double diff = getMarsTime().getTimeDiff(process.getToggleDue());
//						// maxTime is 333 by default
////						double maxTime = process.getSpec().getProcessTime();
//						// modTime is 100 at max
////						double modTime = (diff - maxTime);
//						score.addBase(TOGGLE_TIME, diff);
//
//						if (score.getScore() >= 7.5 * process.getPercentEffort()) { 
//							toggleOffTasks.add(new ToggleOffJob(this, settlement, building, process, score));
//						}
//						else {
//							computeAssessment(assessed, scoreMap, building, process, isWaste);
//						}
//					}
//					else {
//						computeAssessment(assessed, scoreMap, building, process, isWaste);
//					}
				}
				else {
					computeAssessment(assessed, scoreMap, building, process, isWaste);
				}
			}
		}
		
		// Select one toggleOnTask
		SettlementTask toggleOnTask = null;
		
		if (!scoreMap.isEmpty())
			toggleOnTask = RandomUtil.getWeightedRandomObject(scoreMap);
		
		// Add the selected toggleOnTask
		if (toggleOnTask != null)
			results.add(toggleOnTask);
		
		// Add all toggleOffTasks
		results.addAll(toggleOffTasks);
		
	}
		
	/**
	 * Computes the assessment.
	 * 
	 * @param mapToAssess
	 * @param scoreMap
	 * @param building
	 * @param process
	 * @param isWaste
	 */
	private void computeAssessment(Map<ResourceProcessSpec, ResourceProcessAssessment> mapToAssess, Map<SettlementTask, Double> scoreMap, 
			Building building, ResourceProcess process, boolean isWaste) {

		var spec = process.getSpec();
		int modules = process.getNumModules();
		var a = mapToAssess.computeIfAbsent(spec,
					s -> calculateAssessment(building, s, modules, isWaste, scoreMap));
		
		
		process.setAssessment(a);
	}
	
	
	/**
	 * Evaluates and assesses a resource process.
	 * 
	 * @param settlement
	 * @param spec
	 * @param modules
	 * @param isWaste
	 * @param scoreMap
	 * @return
	 */
	private ResourceProcessAssessment calculateAssessment(Building building,
					ResourceProcessSpec spec, int modules, boolean isWaste,
					Map<SettlementTask, Double> scoreMap) {
		ResourceProcessAssessment a = ResourceProcess.DEFAULT_ASSESSMENT;

		Settlement settlement = building.getSettlement();	
		
		var inputsAvaiable = isInputsPresent(settlement, spec);
		if (inputsAvaiable) {
			// Score each process
			RatingScore score;
			if (isWaste)  {
				a = computeWasteProcessOutputScore(settlement, spec);
				score = new RatingScore("waste", a.overallScore());
			}
			else {
				// Compute the input score
				double inputValue = MathUtils.between(computeResourcesValue(settlement, spec, modules, true), MIN_SCORE, MAX_SCORE);
				// Compute the output score		
				double outputValue = MathUtils.between(computeResourcesValue(settlement, spec, modules, false), MIN_SCORE, MAX_SCORE);
						
				a = new ResourceProcessAssessment(inputValue, outputValue,
								MathUtils.between(outputValue/inputValue, MIN_SCORE, 2 * MAX_SCORE), 
								true);
				score = new RatingScore("inputs", 1/inputValue);
				score.addModifier("outputs", outputValue); //'.addBase("inputs", -inputValue);
			}

			if (score.getScore() >= 1) {
				score.applyRange(1, 2 * MAX_SCORE);
				scoreMap.put(new ToggleOnJob(this, settlement, isWaste, spec, score), score.getScore());
			}
		}

		return a;
	}
	
	
	/**
	 * Checks if a resource process spec has all input resources.
	 *
	 * @param settlement the settlement the resource is at.
	 * @param processSpec the resource process spec.
	 * @return false if any input resources are empty.
	 */
	private static boolean isInputsPresent(Settlement settlement, ResourceProcessSpec processSpec) {
		var rh = settlement.getEquipmentInventory();
		for (var amount : processSpec.getMinimumInputs().entrySet()) {
			if (amount.getValue() > rh.getSpecificAmountResourceStored(amount.getKey())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Gets the composite score of a waste process based on the availability of inputs.
	 *
	 * @param settlement the settlement the resource process is at.
	 * @param process    the resource process.
	 * @return the resource assessment
	 */
	private static ResourceProcessAssessment computeWasteProcessOutputScore(Settlement settlement,
							ResourceProcessSpec process) {
		double percentage = 0;
		var rh = settlement.getEquipmentInventory();

		// For now, consider only the input resource for waste processes
		for (int id : process.getInputResources()) {
			double percAvailable;
			if (process.isAmbientInputResource(id)) {
				percAvailable = 1;
			}
			else {
				double cap = rh.getSpecificCapacity(id);
				double stored = rh.getSpecificAmountResourceStored(id);
				
				double rate = process.getBaseInputRate(id); // per sol
				double perSol = process.getProcessTime() / 1000D;
				percAvailable = Math.max(1D, stored / rate / perSol / (cap/2 - stored) / 10);
			}

			percentage = Math.max(percentage, percAvailable);
		}

		// Put a min threshold on the available waste
		double rawScore = 0;
		if (percentage > WASTE_THRESHOLD) {
			rawScore = percentage;
		}
		if (rawScore > MAX_SCORE)
			rawScore = MAX_SCORE;
		
		return new ResourceProcessAssessment(0, rawScore, rawScore, true);
	}

	/**
	 * Gets the total value of a resource process's input or output.
	 *
	 * @param settlement the settlement for the resource process.
	 * @param processSpec
	 * @param modules
	 * @param input      is the resource value for the input?
	 * @return the total value for the input or output.
	 */
	private static double computeResourcesValue(Settlement settlement,
												ResourceProcessSpec processSpec,
												int modules, boolean input) {
		// Set the basic score
		double score = 0.01;
		// Note: beware of not reseting score inside the for loop, 
		// or else losing the carryover from previous calculation

		Set<Integer> set = null;
		if (input)
			set = processSpec.getInputResources();
		else
			set = processSpec.getOutputResources();

		GoodsManager gm = settlement.getGoodsManager();

		for (int resource : set) {
			// Gets the vp for this resource
			double vp = gm.getGoodValuePoint(resource);

			// Gets the supply of this resource
//			double supply = gm.getSupplyScore(resource);

			if (input) {
				// For inputs:

				// Note: mass rate is kg/sol
//				double rate = processSpec.getBaseInputRate(resource);

				double value = vp;
//				score += value;

				if (processSpec.isAmbientInputResource(resource)) {
					// Note: 'Ambient' is used for CO2 and brine water
					// reduce the score in order to encourage this process
					value = value / EXCEEDING;
				}
				else {
					// Note: Mark ambient as 'false' to hint that this process is discouraged
					value = value * MID;
				}
				
				if (ResourceUtil.isRawMaterial(resource)) {   				// all ores, all minerals, sand)
//					|| ResourceUtil.isChemical(resource)) {					// polyurethane, polyester resin, ethylene, ethylene glycol, styrene, propylene 
					score += value / MEGA;
				} else if (ResourceUtil.isCO2(resource)) { 					// CO2	
					score += value / SUPER;
				} else if (ResourceUtil.isHydrogen(resource)) { 			// hydrogen	
					score += value * SUPER;
				} else if (ResourceUtil.isMethane(resource)) { 				// methane
					score += value * SUPER;
				} else if (ResourceUtil.isMethanol(resource)) { 			// methanol
					score += value * SUPER;
				} else if (ResourceUtil.isOxygen(resource)) {  				// oxygen
					score += value * MEGA;
				} else if (ResourceUtil.isDerivedResource(resource)) { 		// glucose, leaves, soil 
					score += value / MEGA;
				} else if (ResourceUtil.isInSitu(resource)					// all regolith types
						|| ResourceUtil.isWasteProduct(resource)) { 		// grey water, black water, * waste
					score += value / OVERWHELMING;
				} else if (ResourceUtil.isTier1Resource(resource)) { 		// ice, brine water, rock salt
					score += value / SIGNIFICANT;
				} else if (ResourceUtil.isWater(resource)) { 				// water
					score += value * MID; 
				} else if (ResourceUtil.isConstructionResource(resource)) {	// GYPSUM_PLASTER_ID, GYPSUM_ID, CEMENT_ID, LIME_ID, ACETYLENE_ID
					score += value / MID;
				} else {
					score += value;
				}
			}

			else {
				// For outputs:

//				double rate = processSpec.getBaseOutputRate(resource);
				
				double value = vp;

				if (processSpec.isCoreOutputResource(resource)) {
					value = value * GOOD;
				}
				
//				score += value;

				// if this resource is ambient or a waste product
				// that the settlement won't keep (e.g. carbon dioxide),
				// then it won't need to check how much it has in stock
				// and it will not be affected by its vp and supply
				if (processSpec.isWasteOutputResource(resource)) {
					// Note: Mark waste as 'true' to hint that this process is encouraged
					score += value * SUPER;
//				} else if (ResourceUtil.isHydrogen(resource)) { 		// hydrogen
//					score += mrate * EXCEEDING;
//				} else if (ResourceUtil.isMethane(resource)) { 			// methane
//					score += mrate * SIGNIFICANT;
//				} else if (ResourceUtil.isMethanol(resource)) { 		// methanol
//					score += mrate * SUPREME;
				} else if (ResourceUtil.isOxygen(resource)) {			// oxygen
					score += value * SUPER;
//				} else if (ResourceUtil.isRawElement(resource)      	// carbon, iron powder, iron oxide
//					|| ResourceUtil.isConstructionResource(resource)) {	// cement, concrete, lime, brick, gypsum plaster			
//					score += value * MEGA;					
				} else if (ResourceUtil.isTier1Resource(resource)) { 	// ice, brine water, rock salt	
					score += value * SUPREME;	
//				} else if (ResourceUtil.isInSitu(resource)) {			// all regolith types
//					score += value * SIGNIFICANT;	
//				} else if (ResourceUtil.isWasteProduct(resource)) {		// Nitrogen, CO, grey/black water, compost, all waste, carbon monoxide			
//					score += value * SUPER;
//				} else if (ResourceUtil.isChemical(resource)) {			// polyurethane, polyester resin, ethylene, ethylene glycol, styrene, propylene 				
//					score += value * SUPREME;
//				} else if (ResourceUtil.isDerivedResource(resource) 	// glucose, leaves, soil
//					|| ResourceUtil.isCriticalResource(resource)) {		// glass
//					score += value * SUPREME;
				} else if (ResourceUtil.isWater(resource)) { 			// water
					score += value * MEGA;
				} else if (ResourceUtil.isRawMaterial(resource)) { 		// all ores, all minerals, sand
					score += value * EXTREME;
				} else
					score += value;
			}
		}
		
		return score;// computeModuleFactor(modules);
	}
	
	/**
	 * Computes the module factor.
	 * 
	 * @param modules
	 * @return
	 */
	private static double computeModuleFactor(int modules) {
		if (modules == 1)
			return 1;
		modules = modules / 2;
		if (!moduleFactor.containsKey(modules)) {
			double value = Math.sqrt(Math.sqrt(modules));
			moduleFactor.put(modules, value);
			return value;
		}
		else {
			return moduleFactor.get(modules);
		}
	}	
}