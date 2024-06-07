/*
 * Mars Simulation Project
 * ToggleResourceProcessMeta.java
 * @date 2022-09-05
 * @author Scott Davis
 */
package com.mars_sim.core.structure.building.function.task;

import java.util.ArrayList;
import java.util.List;

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
	
	private double hydrogenVP = 0;
	private double methaneVP = 0;
	private double methanolVP = 0;
	private double waterVP = 0;
	private double oxygenVP = 0;
	private double regolithDemand = 0;
	private double iceDemand = 0;
	private double ethyleneDemand = 0;
	private double prophyleneDemand = 0;
	private double regStored = 0;
	private double iceStored = 0;
	
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
				// Same building & metatask so compare on Process
				ToggleProcessJob other = (ToggleProcessJob) obj;
				return process.equals(other.process);
			}
			return false;
		}
    }

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.toggleResourceProcess"); //$NON-NLS-1$
	
	private static final double URGENT_FACTOR = 2;
	private static final double DOUBLE_URGENT_FACTOR = 4;
	private static final double WASTE_URGENT_FACTOR = 10;
	private static final double WASTE_DOUBLE_URGENT_FACTOR = WASTE_URGENT_FACTOR * 5;
	
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
			
			GoodsManager goodsManager = settlement.getGoodsManager();
			
			// Recompute VPs
			hydrogenVP = goodsManager.getGoodValuePoint(ResourceUtil.hydrogenID);
			methaneVP = goodsManager.getGoodValuePoint(ResourceUtil.methaneID);
			methanolVP = goodsManager.getGoodValuePoint(ResourceUtil.methanolID);
			waterVP = goodsManager.getGoodValuePoint(ResourceUtil.waterID);
			oxygenVP = goodsManager.getGoodValuePoint(ResourceUtil.oxygenID);
			regolithDemand = goodsManager.getDemandValueWithID(ResourceUtil.regolithID);
			iceDemand = goodsManager.getDemandValueWithID(ResourceUtil.iceID);
			ethyleneDemand = goodsManager.getDemandValueWithID(ResourceUtil.ethyleneID); 
			prophyleneDemand =  goodsManager.getDemandValueWithID(ResourceUtil.prophyleneID);		
			regStored = settlement.getAmountResourceStored(ResourceUtil.regolithID);
			iceStored = settlement.getAmountResourceStored(ResourceUtil.iceID);
			
			// Get the most suitable process per Building; not each process as too many will be created
			for (Building building : settlement.getBuildingManager().getBuildingSet(FunctionType.RESOURCE_PROCESSING)) {
				// In this building, select the best resource to compete
				SettlementTask entry = selectMostPosNegResourceProcess(building, building.getResourceProcessing().getProcesses());
				if (entry != null) {
					tasks.add(entry);
				}
			}
		}

		else if (!settlement.getProcessOverride(OverrideType.WASTE_PROCESSING)) {
			// Get the most suitable process per Building; not each process as too many will be created
			for (Building building : settlement.getBuildingManager().getBuildingSet(FunctionType.WASTE_PROCESSING)) {
				// In this building, select the best resource to compete
				SettlementTask entry = selectMostPosNegWasteProcess(building, building.getWasteProcessing().getProcesses());
				if (entry != null) {
					tasks.add(entry);
				}
			}
		}
		
		return tasks;
	}

	/**
	 * Selects a resource process (from a building) based on its resource score.
	 *
	 * @param building the building
	 * @return the resource process to toggle or null if none.
	 */
	private SettlementTask selectMostPosNegResourceProcess(Building building, List<ResourceProcess> processes) {
		ResourceProcess mostPosProcess = null;
		ResourceProcess mostNegProcess = null;
		double highest = 0;
		double lowest = 0;

		Settlement settlement = building.getSettlement();


		for (ResourceProcess process : processes) {
			if (process.isToggleAvailable() && !process.isFlagged()) {
				double score = computeResourceScore(settlement, process);

				// Check if settlement is missing one or more of the output resources.
				// Will multiply by 10 internally within computeResourcesValue() in
				// ToggleResourceProcess
				
				if (process.isEmptyOutputs(settlement)) {
					// will push for toggling on this process to produce more output resources
					if (process.isProcessRunning()) {
						// no need to change it
						continue;
					} else {
						// will need to push for toggling on this process since output resource is zero
						score *= URGENT_FACTOR;
					}
				}

				// NOTE: Need to detect if the output resource is dwindling

				// Check if settlement is missing one or more of the input resources.
				if (!process.isInputsPresent(settlement)) { 
					if (process.isProcessRunning()) {
						// will need to push for toggling off this process since input resource is
						// insufficient
						score *= URGENT_FACTOR;
					} else {
						// no need to turn it on
						continue;
					}
				}

				if (score > 0 && process.isProcessRunning()) {
					// let it continue running. No need to turn it off.
					continue;
				}

				else if (score < 0 && process.isProcessRunning()) {
					// want to shut it down
					score *= URGENT_FACTOR;
				}

				else if (score > 0 && !process.isProcessRunning()) {
					// want to turn it on
					score *= DOUBLE_URGENT_FACTOR;
				}

				else if (score < 0 && !process.isProcessRunning()) {
					// let it continue not running. No need to turn it on.
					continue;
				}
				
				// FUTURE: will need to use less fragile method 
				// (other than using process name string)
				
				String name = process.getProcessName().toLowerCase();

				score = computeScore(name);

				if (score >= highest) {
					highest = score;
					mostPosProcess = process;
				} else if (score <= lowest) {
					lowest = score;
					mostNegProcess = process;
				}
			}
		}

		// Decide whether to create a TaskJob
		ResourceProcess bestProcess = null;
		double bestScore = 0;
		if ((mostPosProcess != null) && (highest >= Math.abs(lowest))) {
			bestProcess = mostPosProcess;
			bestScore = highest;
		}
		else if (mostNegProcess != null) {
			bestProcess = mostNegProcess;
			bestScore = -lowest;
		}

		if (bestProcess != null) {
			return new ToggleProcessJob(this, building, bestProcess, new RatingScore(bestScore));
		}

		return null;
	}

	private double computeScore(String name) {
		double score = 0;

		boolean oxi = name.contains(ResourceProcessing.OXIDATION);
		boolean olefin = name.contains(ResourceProcessing.OLEFIN);
		boolean sab = name.contains(ResourceProcessing.SABATIER);
		boolean reg = name.contains(ResourceProcessing.REGOLITH);
		boolean ice = name.equalsIgnoreCase(ResourceProcessing.ICE);
		boolean ppa = name.contains(ResourceProcessing.PPA);
		boolean cfr = name.contains(ResourceProcessing.CFR);
		boolean ogs = name.contains(ResourceProcessing.OGS);

		// Notes : 
		// The higher the score, the harder the process executes
		// The lower the score, the easier the process execute

		if (reg) {
			score *= 0.5 * regolithDemand * (1 + regStored);
		}

		else if (ice) {
			score *= iceDemand * (1 + iceStored);
		}

		else if (ppa) {
			score *= .95 * hydrogenVP / methaneVP;
		}

		else if (cfr) {
			score *= .75 * waterVP / hydrogenVP;
		}

		else if (sab) {
			score *= 0.5 * waterVP * methaneVP / hydrogenVP;
		}

		else if (oxi) {
			score *= 0.75 * methanolVP / methaneVP / oxygenVP;
		}
		
		else if (olefin) {
			score *= 0.5 * ethyleneDemand * prophyleneDemand / methanolVP;
		}
		
		else if (ogs) {
			score *= hydrogenVP * oxygenVP / waterVP;
		}
		
		return score;
	}
	
	/**
	 * Selects a waste process (from a building) based on its input resource score.
	 *
	 * @param building the building
	 * @return the resource process to toggle or null if none.
	 */
	private SettlementTask selectMostPosNegWasteProcess(Building building, List<ResourceProcess> processes) {
		ResourceProcess mostPosProcess = null;
		ResourceProcess mostNegProcess = null;
		double highest = 0;
		double lowest = 0;

		Settlement settlement = building.getSettlement();

		for (ResourceProcess process : processes) {
			if (process.isToggleAvailable() && !process.isFlagged()) {
				double score = computeResourceScore(settlement, process);

				for (int res: process.getInputResources()) {
					double amount = settlement.getAmountResourceStored(res);
					if (amount > 0) {
						score = Math.abs(score) * amount;
					}
				}
				
				// Check if settlement is missing one or more of the output resources.
				// Will multiply by 10 internally within computeResourcesValue() in
				// ToggleResourceProcess
				if (process.isEmptyOutputs(settlement)) {
					// will push for toggling on this process to produce more output resources
					if (process.isProcessRunning()) {
						// no need to change it
						continue;
					} else {
						// will need to push for toggling on this process since output resource is zero
						score *= WASTE_URGENT_FACTOR;
					}
				}

				// NOTE: Need to detect if the output resource is dwindling

				// Check if settlement is missing one or more of the input resources.
				if (!process.isInputsPresent(settlement)) { 
					if (process.isProcessRunning()) {
						// will need to push for toggling off this process since input resource is
						// insufficient
						score *= WASTE_URGENT_FACTOR;
					} else {
						// no need to turn it on
						continue;
					}
				}

				if (score > 0 && process.isProcessRunning()) {
					// let it continue running. No need to turn it off.
					continue;
				}

				else if (score < 0 && process.isProcessRunning()) {
					// need to shut it down
					score *= WASTE_URGENT_FACTOR;
				}

				else if (score > 0 && !process.isProcessRunning()) {
					// need to turn it on
					// Biased toward turning it on
					score *= WASTE_DOUBLE_URGENT_FACTOR;
				}

				else if (score < 0 && !process.isProcessRunning()) {
					// let it continue not running. No need to turn it on.
					continue;
				}
				
				if (score >= highest) {
					highest = score;
					mostPosProcess = process;
				} else if (score <= lowest) {
					lowest = score;
					mostNegProcess = process;
				}
			}
		}

		// Decide whether to create a TaskJob
		ResourceProcess bestProcess = null;
		double bestScore = 0;
		if ((mostPosProcess != null) && (highest >= Math.abs(lowest))) {
			bestProcess = mostPosProcess;
			bestScore = highest;
		}
		else if (mostNegProcess != null) {
			bestProcess = mostNegProcess;
			bestScore = -lowest;
		}

		if (bestProcess != null) {
			return new ToggleProcessJob(this, building, bestProcess, new RatingScore(bestScore));
		}

		return null;
	}

	
	/**
	 * Gets the composite resource score based on the ratio of
	 * VPs of outputs to VPs of inputs for a resource process.
	 *
	 * @param settlement the settlement the resource process is at.
	 * @param process    the resource process.
	 * @return the resource score (0 = no need to change); positive number -> demand
	 *         to toggle on; negative number -> demand to toggle off
	 */
	private static double computeResourceScore(Settlement settlement, ResourceProcess process) {
		double inputValue = process.getResourcesValue(settlement, true);
		double outputValue = process.getResourcesValue(settlement, false);
		double score = outputValue - inputValue;

		// Score is influence if a Toggle is active but no one working. Finish Toggles that have started
		double[] toggleTime = process.getToggleSwitchDuration();
		if ((toggleTime[0] > 0) && !process.isFlagged()) {
			score = score + (100D * ((toggleTime[1] - toggleTime[0])/toggleTime[1]));
		}
		return score / 4;
	}

}
