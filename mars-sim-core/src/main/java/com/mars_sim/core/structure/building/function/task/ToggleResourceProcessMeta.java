/*
 * Mars Simulation Project
 * ToggleResourceProcessMeta.java
 * @date 2024-06-08
 * @author Scott Davis
 */
package com.mars_sim.core.structure.building.function.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.robot.RobotType;
import com.mars_sim.core.structure.OverrideType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.structure.building.function.ResourceProcess;
import com.mars_sim.core.structure.building.function.ResourceProcessing;
import com.mars_sim.tools.Msg;
import com.mars_sim.tools.util.RandomUtil;

/**
 * Meta task for the ToggleResourceProcess task.
 */
public class ToggleResourceProcessMeta extends MetaTask implements SettlementMetaTask {
	
	/**
	 * Represents a job to toggle a Resource process in a building.
	 */
    private static class ToggleProcessJob extends SettlementTask {
		
		private static final long serialVersionUID = 1L;
		
		private ResourceProcess process;
		
        public ToggleProcessJob(SettlementMetaTask mt, Building processBuilding, ResourceProcess process,
						RatingScore score) {
			super(mt, "Toggle " + process.getProcessName(), processBuilding, score);
			this.process = process;
        }

		/**
         * The Building holding the process is the focus.
         */
        private Building getProcessBuilding() {
            return (Building) getFocus();
        }

        @Override
        public Task createTask(Person person) {
            return new ToggleResourceProcess(person, getProcessBuilding(), process);
        }

        @Override
        public Task createTask(Robot robot) {
            return new ToggleResourceProcess(robot, getProcessBuilding(), process);
        }
		
 		@Override
		public int hashCode() {
			return super.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (super.equals(obj)) {
				// Same building & meta task so compare on Process
				ToggleProcessJob other = (ToggleProcessJob) obj;
				return process.equals(other.process);
			}
			return false;
		}
    }

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.toggleResourceProcess"); //$NON-NLS-1$
	
	private static final double RESOURCE_RATE_0 = 10;
	private static final double RESOURCE_RATE_1 = 30;
	private static final double WASTE_RATE_0 = 10;
	private static final double WASTE_RATE_1 = 30;
	private static final double MAX_SCORE = 5_000;
	
    public ToggleResourceProcessMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.ANY_HOUR);
		setFavorite(FavoriteType.TINKERING);
		setPreferredJob(JobType.TECHNICIAN, JobType.ENGINEER);

		addPreferredRobot(RobotType.REPAIRBOT, RobotType.CONSTRUCTIONBOT, 
				RobotType.MAKERBOT, RobotType.DELIVERYBOT);
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

		if (settlement == null)
			return tasks;
				
		int rand = RandomUtil.getRandomInt(3);
		
		if (rand <= 2 && !settlement.getProcessOverride(OverrideType.RESOURCE_PROCESS)) {			
			// Get the most suitable process per Building; not each process as too many will be created
			for (Building building : settlement.getBuildingManager().getBuildingSet(FunctionType.RESOURCE_PROCESSING)) {
				// In this building, select the best resource to compete
				SettlementTask entry = 
						selectMostPosNegProcess(building, 
								building.getResourceProcessing().getProcesses(), 
								false, 
								RESOURCE_RATE_0, RESOURCE_RATE_1);
				if (entry != null) {
					tasks.add(entry);
				}
			}
		}

		else if (!settlement.getProcessOverride(OverrideType.WASTE_PROCESSING)) {
			// Get the most suitable process per Building; not each process as too many will be created
			for (Building building : settlement.getBuildingManager().getBuildingSet(FunctionType.WASTE_PROCESSING)) {
				// In this building, select the best resource to compete
				SettlementTask entry = 
						selectMostPosNegProcess(building, 
								building.getWasteProcessing().getProcesses(), 
								true, 
								WASTE_RATE_0, WASTE_RATE_1);
				if (entry != null) {
					tasks.add(entry);
				}
			}
		}
		
		return tasks;
	}

	/**
	 * Selects a resource/waste process (from a building) based on its resource score.
	 *
	 * @param building
	 * @param processes
	 * @param isWaste
	 * @param rate0
	 * @param rate1
	 * @return the selected process to toggle or null if none.
	 */
	private SettlementTask selectMostPosNegProcess(Building building, 
			List<ResourceProcess> processes, boolean isWaste, 
			double rate0, double rate1) {

		// Track a resource process having a positive or negative score 
		boolean scorePositive = false;

		int flip = RandomUtil.getRandomInt(1);
		if (flip == 0) {
			scorePositive = true;
		}

		Settlement settlement = building.getSettlement();

		Map<ResourceProcess, Double> resourceProcessMap = new HashMap<>();
		
		for (ResourceProcess process : processes) {
			if (process.canToggle() && !process.isFlagged()) {
								
				// For each iteration, give the processes with a positive score
				// versus a negative score a chance to run
	
				double score = 0;
				// if score is positive, toggle on 
				// if score is negative, toggle off
				
				if (isWaste) {
					for (int res: process.getInputResources()) {
						double amount = settlement.getAmountResourceStored(res);
						if (amount > 0) {
							score += amount;
						}
					}
				} 
				else {
					score = computeBasicScore(settlement, process);	
					
					// FUTURE: will need to use less fragile method 
					// (other than using process name string)	
					String name = process.getProcessName().toLowerCase();
					
					score = modifyScore(settlement, name, score);
				}
								
				// Limit the score so that all processes have the chance to be picked
				if (score > MAX_SCORE)
					score = MAX_SCORE;
				else if (score < -MAX_SCORE)
					score = -MAX_SCORE;
				
				// Note : Since scorePositive is 50% positive and 50% negative, it will 
				// give equal chance of picking a process with a positive versus 
				// negative score
				
			
				if ((score > 0 && scorePositive)
					|| (score < 0 && !scorePositive)) {
						parseProcess(resourceProcessMap, settlement, process, 
							rate0, rate1, score);
				}
			}
		}

		// Use probability map to obtain the process
		ResourceProcess selectedProcess = RandomUtil.getWeightedRandomObject(resourceProcessMap);
		
		if (selectedProcess != null) {
			double score = resourceProcessMap.get(selectedProcess);
			// Note: For processes having very low score, it will still be selected if 
			// all other processes are having very low score at the same time.
			// Therefore, the second selection process is needed to filter out 
			// and avoid picking a process having a score of, say, 0.5.
			
			// Note: May multiply the score by a factor to boost the chance
			
			if (score > 100 || RandomUtil.getRandomDouble(100) < score) {
				return new ToggleProcessJob(this, building, selectedProcess, new RatingScore(score));
			}
		}

		return null;
	}

	/**
	 * Derives the score for a resource process.
	 * 
	 * @param resourceProcessMap
	 * @param process
	 * @param score
	 */
	private void parseProcess(Map<ResourceProcess, Double> resourceProcessMap, 
			Settlement settlement, ResourceProcess process, 
			double rate0, double rate1, double score) {
			
		// Save the score for that process for displaying its value
		process.setScore(score);
		
		// Check if settlement is missing one or more of the output resources.
		if (process.isOutputsEmpty(settlement)) {
			// will push for toggling on this process to produce more output resources
			if (process.isProcessRunning()) {
				// Note: how to speed up the process to produce more outputs
				// Skip this process. no need to turn it off.
				return;
			} else {
				// will need to push for toggling on this process since output resource is zero
				score = MAX_SCORE;
			}
		}

		// NOTE: Need to detect if the output resource is dwindling

		// Check if settlement is missing one or more of the input resources.
		else if (!process.isInputsPresent(settlement)) { 
			if (process.isProcessRunning()) {
				// will need to push for toggling off this process 
				// since input resource is insufficient
				score = MAX_SCORE;
			} else {
				// Skip this process. no need to turn it on.
				return;
			}
		}

		else {	
			// if output score is greater than the input score
			if (score > 0 && process.isProcessRunning()) {
				// Skip this process. No need to turn it off.
				return;
			}
	
			// if output score is smaller than the input score
			else if (score < 0 && process.isProcessRunning()) {
				// want to shut it down
				score *= rate0;
			}
	
			// if output score is greater than the input score
			else if (score > 0 && !process.isProcessRunning()) {
				// want to turn it on
				score *= rate1;
			}
	
			// if output score is smaller than the input score
			else if (score < 0 && !process.isProcessRunning()) {
				// // Skip this process. no need to turn it on.
				return;
			}
			
			if (score > 1) {
				score = Math.round(score * 10.0)/10.0;
			}
		}
		
		// Save the process and its score into the resource process map
		if (score > 0)
			resourceProcessMap.put(process, score);
		else if (score < 0)
			resourceProcessMap.put(process, -score);
	}
	
	/**
	 * Modifies the score for certain resource processes.
	 * 
	 * @param name
	 * @param settlement
	 * @return
	 */
	private double modifyScore(Settlement settlement, String name, double score) {

		// Selective Partial Oxidation of Methane to Methanol
		boolean oxi = name.contains(ResourceProcessing.OXIDATION);
		// "Methanol-to-olefin (MTO) process"
		boolean olefin = name.contains(ResourceProcessing.OLEFIN);
		// "Sabatier RWGS Reactor"
		boolean sab = name.contains(ResourceProcessing.SABATIER);
		boolean reg = name.contains(ResourceProcessing.REGOLITH);
		boolean ice = name.equalsIgnoreCase(ResourceProcessing.ICE);
		// Plasma Pyrolysis Assembly (PPA) Reactor
		boolean ppa = name.contains(ResourceProcessing.PPA);
		// Carbon Formation Reactor (CFR). Input: O2; Output: H2O
		boolean cfr = name.contains(ResourceProcessing.CFR);
		boolean ogs = name.contains(ResourceProcessing.OGS);

		GoodsManager goodsManager = settlement.getGoodsManager();

		if (reg) {
			double regolithDemand = goodsManager.getDemandValueWithID(ResourceUtil.regolithID);
			double regStored = settlement.getAmountResourceStored(ResourceUtil.regolithID);
			score *= 10000.0 / regolithDemand * (1 + regStored);
		}

		else if (ice) {
			double iceDemand = goodsManager.getDemandValueWithID(ResourceUtil.iceID);
			double iceStored = settlement.getAmountResourceStored(ResourceUtil.iceID);
			score *= 10000.0 / iceDemand * (1 + iceStored);
		}

		else if (ppa) {
			double hydrogenDemand = goodsManager.getDemandValueWithID(ResourceUtil.hydrogenID);
			double methaneDemand = goodsManager.getDemandValueWithID(ResourceUtil.methaneID);
			score *= 0.01 * hydrogenDemand / methaneDemand;
		}

		else if (cfr) {
			double hydrogenDemand = goodsManager.getDemandValueWithID(ResourceUtil.hydrogenID);
			double waterDemand = goodsManager.getDemandValueWithID(ResourceUtil.waterID);
			score *= 2.5 * waterDemand / hydrogenDemand;
		}

		else if (sab) {
			double hydrogenDemand = goodsManager.getDemandValueWithID(ResourceUtil.hydrogenID);
			double methaneDemand = goodsManager.getDemandValueWithID(ResourceUtil.methaneID);
			double waterDemand = goodsManager.getDemandValueWithID(ResourceUtil.waterID);
			score *= 5.0 * waterDemand * methaneDemand / hydrogenDemand;
		}

		else if (oxi) {
			double oxygenDemand = goodsManager.getDemandValueWithID(ResourceUtil.oxygenID);
			double methanolDemand = goodsManager.getDemandValueWithID(ResourceUtil.methanolID);
			double methaneDemand = goodsManager.getDemandValueWithID(ResourceUtil.methaneID);
			score *= 0.01 * methanolDemand / methaneDemand / oxygenDemand;
		}
		
		else if (olefin) {
			double ethyleneDemand = goodsManager.getDemandValueWithID(ResourceUtil.ethyleneID); 
			double prophyleneDemand =  goodsManager.getDemandValueWithID(ResourceUtil.prophyleneID);
			double methanolDemand = goodsManager.getDemandValueWithID(ResourceUtil.methanolID);
			score *= 0.5 * ethyleneDemand * prophyleneDemand / methanolDemand;
		}
		
		else if (ogs) {
			double hydrogenDemand = goodsManager.getDemandValueWithID(ResourceUtil.hydrogenID);
			double oxygenDemand = goodsManager.getDemandValueWithID(ResourceUtil.oxygenID);
			double waterDemand = goodsManager.getDemandValueWithID(ResourceUtil.waterID);
			score *= hydrogenDemand * oxygenDemand / waterDemand;
		}
		
		return score;
	}
	
	/**
	 * Gets the composite resource score based on the ratio of
	 * VPs of outputs to VPs of inputs for a resource process.
	 *
	 * @param settlement the settlement the resource process is at.
	 * @param process    the resource process.
	 * @return the resource score; 
	 * 		if positive, toggle on; if negative, toggle off
	 */
	private static double computeBasicScore(Settlement settlement, ResourceProcess process) {
		double inputValue = process.getResourcesValue(settlement, true);
		double outputValue = process.getResourcesValue(settlement, false);
		double score = outputValue - inputValue;
		int modules = process.getNumModules();
		
		double[] toggleTime = process.getToggleSwitchDuration();
		if ((toggleTime[0] > 0) && !process.isFlagged()) {
			score = score + (100D * ((toggleTime[1] - toggleTime[0])/toggleTime[1]));
		}
		return score / 4 / modules;
	}

}
