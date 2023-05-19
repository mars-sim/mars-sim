/*
 * Mars Simulation Project
 * ToggleResourceProcessMeta.java
 * @date 2022-09-05
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.ArrayList;
import java.util.List;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.goods.GoodsManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.fav.FavoriteType;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.task.ToggleResourceProcess;
import org.mars_sim.msp.core.person.ai.task.util.MetaTask;
import org.mars_sim.msp.core.person.ai.task.util.SettlementMetaTask;
import org.mars_sim.msp.core.person.ai.task.util.SettlementTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotType;
import org.mars_sim.msp.core.structure.OverrideType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.ResourceProcess;
import org.mars_sim.msp.core.structure.building.function.ResourceProcessing;

/**
 * Meta task for the ToggleResourceProcess task.
 */
public class ToggleResourceProcessMeta extends MetaTask implements SettlementMetaTask {
	/**
	 * Represents a job to toggle a Resource process in a building.
	 */
    private static class ToggleProcessJob extends SettlementTask {
		
		private static final long serialVersionUID = 1L;

        private Building processBuilding;
		private ResourceProcess process;

        public ToggleProcessJob(SettlementMetaTask mt, Building processBuilding, ResourceProcess process, double score) {
			super(mt, "Toggle " + process.getProcessName() + " @ " + processBuilding.getName(), score);
            this.processBuilding = processBuilding;
			this.process = process;
        }

        @Override
        public Task createTask(Person person) {
            return new ToggleResourceProcess(person, processBuilding, process);
        }

        @Override
        public Task createTask(Robot robot) {
            return new ToggleResourceProcess(robot, processBuilding, process);
        }
    }

	/** default logger. */
//	private static SimLogger logger = SimLogger.getLogger(ToggleResourceProcessMeta.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.toggleResourceProcess"); //$NON-NLS-1$
	
	private static final double URGENT_FACTOR = 5;
	
    public ToggleResourceProcessMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.TINKERING);
		setPreferredJob(JobType.TECHNICIAN, JobType.ENGINEER);

		addPreferredRobot(RobotType.REPAIRBOT);
	}

	/**
	 * Robots can toggle resource processes.
	 * @param t Task 
	 * @param r Robot making the request
	 */
	@Override
	public double getRobotSettlementModifier(SettlementTask t, Robot r) {
		return 1D;
	}

	/**
	 * Evaluates if a Person can do a Settlement task, based on in settlement.
	 * @param t Task 
	 * @param p Person making the request
	 */
	@Override
	public double getPersonSettlementModifier(SettlementTask t, Person p) {
		if (p.isInSettlement()) {
			return getPersonModifier(p);
		}
		return 0D;
	}

	/**
	 * Build a list of TaskJob covering the most suitable Resoruce Processes to toggle.
	 * @param settlement Settlement to check
	 */
	@Override
	public List<SettlementTask> getSettlementTasks(Settlement settlement) {
		List<SettlementTask> tasks = new ArrayList<>();

		if ((settlement != null) && !settlement.getProcessOverride(OverrideType.RESOURCE_PROCESS)) {

			// Get the most suitable process per Building; not each process as too many will be created
			for (Building building : settlement.getBuildingManager().getBuildings(FunctionType.RESOURCE_PROCESSING)) {
				// In this building, select the best resource to compete
				SettlementTask entry = selectMostPosNegResourceProcess(building);
				if (entry != null) {
					tasks.add(entry);
				}
			}
		}

		return tasks;
	}

	/**
	 * Select a resource process (from a building) based on its resource score.
	 *
	 * @param building the building
	 * @return the resource process to toggle or null if none.
	 */
	private SettlementTask selectMostPosNegResourceProcess(Building building) {
		ResourceProcess mostPosProcess = null;
		ResourceProcess mostNegProcess = null;
		double highest = 0;
		double lowest = 0;

		Settlement settlement = building.getSettlement();
		GoodsManager goodsManager = settlement.getGoodsManager();
		double regStored = settlement.getAmountResourceStored(ResourceUtil.regolithID);
		double iceStored = settlement.getAmountResourceStored(ResourceUtil.iceID);

		double hydrogenVP = goodsManager.getGoodValuePoint(ResourceUtil.hydrogenID);
		double methaneVP = goodsManager.getGoodValuePoint(ResourceUtil.methaneID);
		double methanolVP = goodsManager.getGoodValuePoint(ResourceUtil.methanolID);
		double waterVP = goodsManager.getGoodValuePoint(ResourceUtil.waterID);
		double oxygenVP = goodsManager.getGoodValuePoint(ResourceUtil.oxygenID);

		for (ResourceProcess process : building.getResourceProcessing().getProcesses()) {
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
					// need to shut it down
					score *= URGENT_FACTOR;
				}

				else if (score > 0 && !process.isProcessRunning()) {
					// need to turn it on
					score *= URGENT_FACTOR;
				}

				else if (score < 0 && !process.isProcessRunning()) {
					// let it continue not running. No need to turn it on.
					continue;
				}

				// This is bad and the logic is very fragile being based on the Process Name !!
				String name = process.getProcessName().toLowerCase();

				boolean sel = name.equalsIgnoreCase(ResourceProcessing.SELECTIVE);
				boolean olefin = name.equalsIgnoreCase(ResourceProcessing.OLEFIN);
				boolean sab = name.equalsIgnoreCase(ResourceProcessing.SABATIER);
				boolean reg = name.contains(ResourceProcessing.REGOLITH);
				boolean ice = name.equalsIgnoreCase(ResourceProcessing.ICE);
				boolean ppa = name.equalsIgnoreCase(ResourceProcessing.PPA);
				boolean cfr = name.equalsIgnoreCase(ResourceProcessing.CFR);
				boolean ogs = name.equalsIgnoreCase(ResourceProcessing.OGS);

				if (reg) {
					score *= goodsManager.getDemandValueWithID(ResourceUtil.regolithID) * (1 + regStored);
				}

				else if (ice) {
					score *= goodsManager.getDemandValueWithID(ResourceUtil.iceID) * (1 + iceStored);
				}

				else if (ppa) {
					score *= hydrogenVP / methaneVP;
				}

				else if (cfr) {
					score *= waterVP / hydrogenVP;
				}

				else if (sab) {
					score *= waterVP * methaneVP / hydrogenVP;
				}

				else if (sel) {
					score *= methanolVP / methaneVP / oxygenVP;
				}
				
				else if (olefin) {
					score *= goodsManager.getDemandValueWithID(ResourceUtil.ethyleneID) 
							* goodsManager.getDemandValueWithID(ResourceUtil.prophyleneID) / methanolVP;
				}
				
				else if (ogs) {
					score *= hydrogenVP * oxygenVP / waterVP;
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
			return new ToggleProcessJob(this, building, bestProcess, bestScore);
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
			score = (score * 2) + (100D * ((toggleTime[1] - toggleTime[0])/toggleTime[1]));
		}
		return score;
	}

}
