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
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskJob;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.OverrideType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.ResourceProcess;
import org.mars_sim.msp.core.structure.building.function.ResourceProcessing;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * Meta task for the ToggleResourceProcess task.
 */
public class ToggleResourceProcessMeta extends MetaTask {
	/**
	 * Represents a job to toggle a Resource process in a building.
	 */
    private static class ToggleProcessJob implements TaskJob {

        private double score;
        private Building processBuilding;
		private ResourceProcess process;

        public ToggleProcessJob(Building processBuilding, ResourceProcess process, double score) {
            this.processBuilding = processBuilding;
			this.process = process;

			if (score > CAP) {
				score = CAP;
			}
            this.score = score;
        }

        @Override
        public double getScore() {
            return score;
        }

        @Override
        public String getDescription() {
            return "Toggle " + process.getProcessName() + " @ " + processBuilding.getName();
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
	
	private static final double FACTOR = 1_000;

	private static final int CAP = 1_000;
	
    public ToggleResourceProcessMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.TINKERING);
		setPreferredJob(JobType.TECHNICIAN, JobType.ENGINEER);
	}

	
    /**
     * Create a task for any Fishery that needs assistence
     */
    @Override
    public List<TaskJob> getTaskJobs(Robot robot) {
		return buildTaskJobs(robot, false);
	}

	/**
     * Create a task for any Fishery that needs assistence
     */
    @Override
    public List<TaskJob> getTaskJobs(Person person) {
		return buildTaskJobs(person, true);
	}

	/**
	 * Build a list of TaskJob covering the most suitable Resproces Processes to toggle.
	 */
	private List<TaskJob> buildTaskJobs(Worker worker, boolean isPerson) {
		List<TaskJob> tasks = new ArrayList<>();

		Settlement settlement = worker.getSettlement();
		
		if ((settlement != null) && !settlement.getProcessOverride(OverrideType.RESOURCE_PROCESS)) {

			// Get the most suitable process per Building; not each process as too many will be created
			for (Building building : settlement.getBuildingManager().getBuildings(FunctionType.RESOURCE_PROCESSING)) {
				// In this building, select the best resource to compete
				TaskJob entry = selectMostPosNegResourceProcess(building, worker, isPerson);
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
	 * @param isPerson
	 * @param worker
	 * @return the resource process to toggle or null if none.
	 */
	private TaskJob selectMostPosNegResourceProcess(Building building, Worker worker, boolean isPerson) {
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
						score *= FACTOR;
					}
				}

				// NOTE: Need to detect if the output resource is dwindling

				// Check if settlement is missing one or more of the input resources.
				if (!process.isInputsPresent(settlement)) { 
					if (process.isProcessRunning()) {
						// will need to push for toggling off this process since input resource is
						// insufficient
						score *= FACTOR;
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
					score *= FACTOR;
				}

				else if (score > 0 && !process.isProcessRunning()) {
					// need to turn it on
					score *= FACTOR;
				}

				else if (score < 0 && !process.isProcessRunning()) {
					// let it continue not running. No need to turn it on.
					continue;
				}

				// This is bad and the logic is very fragile being based on the Process Name !!
				String name = process.getProcessName().toLowerCase();

				boolean sab = name.equalsIgnoreCase(ResourceProcessing.SABATIER);
				boolean reg = name.contains(ResourceProcessing.REGOLITH);
				boolean ice = name.equalsIgnoreCase(ResourceProcessing.ICE);
				boolean ppa = name.equalsIgnoreCase(ResourceProcessing.PPA);
				boolean cfr = name.equalsIgnoreCase(ResourceProcessing.CFR);
				boolean ogs = name.equalsIgnoreCase(ResourceProcessing.OGS);

				if (reg) {
					score *= goodsManager.getAmountDemandValue(ResourceUtil.regolithID) * (1 + regStored * FACTOR);
				}

				else if (ice) {
					score *= goodsManager.getAmountDemandValue(ResourceUtil.iceID) * (1 + iceStored * FACTOR);
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

				else if (ogs) {
					score *= hydrogenVP * oxygenVP / waterVP;
				}

				// Randomize it to give other processes a chance
				if (score > 0.0)
					score = RandomUtil.getRandomDouble(score * .2, score * 5);
				else if (score < 0.0)
					score = -RandomUtil.getRandomDouble(-score * .2, -score * 5);

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
			if (isPerson) {
				// Not great
				bestScore *= getPersonModifier((Person) worker);
			}
			return new ToggleProcessJob(building, bestProcess, bestScore);
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
