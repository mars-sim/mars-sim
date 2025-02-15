/*
 * Mars Simulation Project
 * ToggleResourceProcessMeta.java
 * @date 2024-06-08
 * @author Scott Davis
 */
package com.mars_sim.core.structure.building.function.task;

import java.util.ArrayList;
import java.util.List;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.task.util.MetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementMetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskUtil;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.robot.RobotType;
import com.mars_sim.core.structure.OverrideType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.structure.building.function.ResourceProcess;
import com.mars_sim.core.tool.Msg;

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
	
	private static final double RESOURCE_RATE_0 = 50;
	private static final double RESOURCE_RATE_1 = 500;
	private static final double WASTE_RATE_0 = 20;
	private static final double WASTE_RATE_1 = 200;
	private static final double MAX_SCORE = 500;
	
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
		
		if (!settlement.getProcessOverride(OverrideType.RESOURCE_PROCESS)) {			
			// Get the most suitable process per Building; not each process as too many will be created
			for (Building building : settlement.getBuildingManager().getBuildingSet(FunctionType.RESOURCE_PROCESSING)) {
				// In this building, select the best resource to compete
				selectToggableProcesses(building, 
								building.getResourceProcessing().getProcesses(), 
								false, RESOURCE_RATE_0, RESOURCE_RATE_1, tasks);
			}
		}

		if (!settlement.getProcessOverride(OverrideType.WASTE_PROCESSING)) {
			// Get the most suitable process per Building; not each process as too many will be created
			for (Building building : settlement.getBuildingManager().getBuildingSet(FunctionType.WASTE_PROCESSING)) {
				// In this building, select the best resource to compete
				selectToggableProcesses(building, 
								building.getWasteProcessing().getProcesses(), 
								true, WASTE_RATE_0, WASTE_RATE_1, tasks);
			}
		}
		
		return tasks;
	}

	/**
	 * Register any resource/waste process (from a building) based on its resource score.
	 *
	 * @param building
	 * @param processes
	 * @param isWaste
	 * @param rate0
	 * @param rate1
	 * @param results Holds the list of Task created
	 */
	private void selectToggableProcesses(Building building, 
			List<ResourceProcess> processes, boolean isWaste, 
			double rate0, double rate1, List<SettlementTask> results) {

		Settlement settlement = building.getSettlement();		
		for (ResourceProcess process : processes) {
			// Avoid process that can't be toggled or no point toggling
			if (process.canToggle() && !process.isWorkerAssigned()) {
				RatingScore score = RatingScore.ZERO_RATING;

				// Is either running or not with with input available
				if (process.isInputsPresent(settlement)) {
					// Score each process
					if (isWaste)  {
						score = computeInputScore(settlement, process);
					}
					else {
						score = computeFullScore(settlement, process);	
					}
					// Save the score for that process for displaying its value
					process.setOverallScore(score.getScore());
							
					// Apply standard modifers
					int modules = process.getNumModules();

					// Moderate the score with # of modules
					score.addModifier("modules", (1D/(modules * 2D)));
					score.applyRange(0, MAX_SCORE);		
				}
				else if (process.isProcessRunning()) {
					score = new RatingScore(process.getOverallScore());

					// TODO this should be based on overdue toggle
					score.addModifier("toggleTime", 0.9D);
				}
				
				// Has a score so queue it
				if ((score.getScore() > 0) ) { 
					results.add(new ToggleProcessJob(this, building, process, score));
				}
			}
		}
	}
	
	/**
	 * Gets the composite resource score based on the availability of inputs
	 *
	 * @param settlement the settlement the resource process is at.
	 * @param process    the resource process.
	 * @return the resource score; 

	 */
	private static RatingScore computeInputScore(Settlement settlement, ResourceProcess process) {
		double lowestPercentage = -1;

		// Is tere enugh for a sol'sworth of processing
		for(int id : process.getInputResources()) {
			double percAvailable = 0D;
			if (process.isAmbientInputResource(id)) {
				// Ambient is always avaialble
				percAvailable = 1D;
			}
			else {
				double available = settlement.getAmountResourceStored(id);
				double rate = process.getBaseFullInputRate(id);
				double solConsumption = (rate * 1000D); 
				percAvailable = Math.min(1D, (solConsumption/available));
			}

			// Update the lowest
			if (lowestPercentage < 0) {
				lowestPercentage = percAvailable;
			}
			else {
				lowestPercentage = Math.min(lowestPercentage, percAvailable);
			}
		}

		if (lowestPercentage < 0) {
			return null;
		}

		double rawScore = lowestPercentage * MAX_SCORE;
		process.setInputScore(rawScore);
		return new RatingScore("inputs available", rawScore);
	}

	/**
	 * Gets the composite resource score based on the ratio of
	 * VPs of outputs to VPs of inputs for a resource process.
	 *
	 * @param settlement the settlement the resource process is at.
	 * @param process    the resource process.
	 * @return the resource score; 

	 */
	private static RatingScore computeFullScore(Settlement settlement, ResourceProcess process) {
		// Compute the input score
		double inputValue = process.computeResourcesValue(settlement, true);
		// Save the input score
		process.setInputScore(inputValue);
		// Compute the output score		
		double outputValue = process.computeResourcesValue(settlement, false);
		// Save the output score
		process.setOutputScore(outputValue);
		// Compute the difference
		RatingScore score = new RatingScore("outputValue", outputValue);
		score.addBase("inputValue", -inputValue);

		return score;
	}
}
