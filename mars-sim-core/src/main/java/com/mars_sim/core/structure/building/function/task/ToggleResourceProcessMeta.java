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
	
	private static final double RESOURCE_URGENT = 2;
	private static final double RESOURCE_DOUBLE_URGENT = 4;
	private static final double WASTE_URGENT = 30;
	private static final double WASTE_DOUBLE_URGENT = 60;
	
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
								RESOURCE_URGENT, RESOURCE_DOUBLE_URGENT);
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
								WASTE_URGENT, WASTE_DOUBLE_URGENT);
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
		ResourceProcess mostPosProcess = null;
		ResourceProcess mostNegProcess = null;
		double highest = 0;
		double lowest = 0;

		Settlement settlement = building.getSettlement();

		Map<ResourceProcess, Double> resourceProcessMap = new HashMap<>();
		
		for (ResourceProcess process : processes) {
			if (process.isToggleAvailable() && !process.isFlagged()) {
								
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
				
				if (score > 10_000)
					score = 10_000;
				else if (score < -10_000)
					score = -10_000;
					
				// Check if settlement is missing one or more of the output resources.
				if (process.isOutputsEmpty(settlement)) {
					// will push for toggling on this process to produce more output resources
					if (process.isProcessRunning()) {
						// Skip this process. No need to turn it on.
						continue;
					} else {
						// will need to push for toggling on this process since output resource is zero
						score *= rate0;
					}
				}

				// NOTE: Need to detect if the output resource is dwindling

				// Check if settlement is missing one or more of the input resources.
				else if (!process.isInputsPresent(settlement)) { 
					if (process.isProcessRunning()) {
						// will need to push for toggling off this process 
						// since input resource is
						// insufficient
						score *= rate0;
					} else {
						// Skip this process. no need to turn it on.
						continue;
					}
				}

				if (score > 0 && process.isProcessRunning()) {
					// Skip this process. No need to turn it off.
					continue;
				}

				else if (score < 0 && process.isProcessRunning()) {
					// want to shut it down
					score *= rate0;
				}

				else if (score > 0 && !process.isProcessRunning()) {
					// want to turn it on
					score *= rate1;
				}

				else if (score < 0 && !process.isProcessRunning()) {
					// // Skip this process. no need to turn it on.
					continue;
				}
				
				// Save the score for that process for displaying its value
				process.setScore(score);
				
				// Save the process and its score into the resource process map
				resourceProcessMap.put(process, Math.abs(score));

//				if (score >= highest) {
//					highest = score;
//					mostPosProcess = process;
//				} else if (score <= lowest) {
//					lowest = score;
//					mostNegProcess = process;
//				}
			}
		}

		
		// Decide whether to create a TaskJob
//		ResourceProcess bestProcess = null;
//		double bestScore = 0;
//		if ((mostPosProcess != null) && (highest >= Math.abs(lowest))) {
//			bestProcess = mostPosProcess;
//			bestScore = highest;
//		}
//		else if (mostNegProcess != null) {
//			bestProcess = mostNegProcess;
//			bestScore = -lowest;
//		}

		// Use probability map to obtain the process
		ResourceProcess bestProcess = RandomUtil.getWeightedRandomObject(resourceProcessMap);
		
		if (bestProcess != null) {
			double bestScore = resourceProcessMap.get(bestProcess);
			return new ToggleProcessJob(this, building, bestProcess, new RatingScore(bestScore));
		}

		return null;
	}

	/**
	 * Modifies the score for certain resource processes.
	 * 
	 * @param name
	 * @param settlement
	 * @return
	 */
	private double modifyScore(Settlement settlement, String name, double score) {

		boolean oxi = name.contains(ResourceProcessing.OXIDATION);
		boolean olefin = name.contains(ResourceProcessing.OLEFIN);
		boolean sab = name.contains(ResourceProcessing.SABATIER);
		boolean reg = name.contains(ResourceProcessing.REGOLITH);
		boolean ice = name.equalsIgnoreCase(ResourceProcessing.ICE);
		boolean ppa = name.contains(ResourceProcessing.PPA);
		boolean cfr = name.contains(ResourceProcessing.CFR);
		boolean ogs = name.contains(ResourceProcessing.OGS);

		GoodsManager goodsManager = settlement.getGoodsManager();
		
		// Need to check why the followings : 
		// The higher the score, the harder the process turns on
		// The lower the score, the easier the process turns on

		if (reg) {
			double regolithDemand = goodsManager.getDemandValueWithID(ResourceUtil.regolithID);
			double regStored = settlement.getAmountResourceStored(ResourceUtil.regolithID);
			score *= 0.25 * regolithDemand * (1 + regStored);
		}

		else if (ice) {
			double iceDemand = goodsManager.getDemandValueWithID(ResourceUtil.iceID);
			double iceStored = settlement.getAmountResourceStored(ResourceUtil.iceID);
			score *= 0.5 * iceDemand * (1 + iceStored);
		}

		else if (ppa) {
			double hydrogenVP = goodsManager.getGoodValuePoint(ResourceUtil.hydrogenID);
			double methaneVP = goodsManager.getGoodValuePoint(ResourceUtil.methaneID);
			score *= 0.5 * hydrogenVP / methaneVP;
		}

		else if (cfr) {
			double hydrogenVP = goodsManager.getGoodValuePoint(ResourceUtil.hydrogenID);
			double waterVP = goodsManager.getGoodValuePoint(ResourceUtil.waterID);
			score *= 0.75 * waterVP / hydrogenVP;
		}

		else if (sab) {
			double hydrogenVP = goodsManager.getGoodValuePoint(ResourceUtil.hydrogenID);
			double methaneVP = goodsManager.getGoodValuePoint(ResourceUtil.methaneID);
			double waterVP = goodsManager.getGoodValuePoint(ResourceUtil.waterID);
			score *= 2.0 * waterVP * methaneVP / hydrogenVP;
		}

		else if (oxi) {
			double oxygenVP = goodsManager.getGoodValuePoint(ResourceUtil.oxygenID);
			double methanolVP = goodsManager.getGoodValuePoint(ResourceUtil.methanolID);
			double methaneVP = goodsManager.getGoodValuePoint(ResourceUtil.methaneID);
			score *= 0.75 * methanolVP / methaneVP / oxygenVP;
		}
		
		else if (olefin) {
			double ethyleneDemand = goodsManager.getDemandValueWithID(ResourceUtil.ethyleneID); 
			double prophyleneDemand =  goodsManager.getDemandValueWithID(ResourceUtil.prophyleneID);
			double methanolVP = goodsManager.getGoodValuePoint(ResourceUtil.methanolID);
			score *= 0.5 * ethyleneDemand * prophyleneDemand / methanolVP;
		}
		
		else if (ogs) {
			double hydrogenVP = goodsManager.getGoodValuePoint(ResourceUtil.hydrogenID);
			double oxygenVP = goodsManager.getGoodValuePoint(ResourceUtil.oxygenID);
			double waterVP = goodsManager.getGoodValuePoint(ResourceUtil.waterID);
			score *= hydrogenVP * oxygenVP / waterVP;
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

		double[] toggleTime = process.getToggleSwitchDuration();
		if ((toggleTime[0] > 0) && !process.isFlagged()) {
			score = score + (100D * ((toggleTime[1] - toggleTime[0])/toggleTime[1]));
		}
		return score / 4;
	}

}
