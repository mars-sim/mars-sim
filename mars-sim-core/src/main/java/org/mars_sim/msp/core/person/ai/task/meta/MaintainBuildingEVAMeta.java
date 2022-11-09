/*
 * Mars Simulation Project
 * MaintainBuildingEVAMeta.java
 * @date 2022-09-18
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.ArrayList;
import java.util.List;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.fav.FavoriteType;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.MaintainBuilding;
import org.mars_sim.msp.core.person.ai.task.MaintainBuildingEVA;
import org.mars_sim.msp.core.person.ai.task.util.MetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskJob;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;

/**
 * Meta task for the MaintainBuildingEVA task.
 */
public class MaintainBuildingEVAMeta extends MetaTask {
/**
     * Represents a Job needed for external maintenance on a Building
     */
    private static class MaintainTaskJob implements TaskJob {

        private double score;
        private Building target;

        public MaintainTaskJob(Building target, double score) {
            this.target = target;
            this.score = score;
        }

        @Override
        public double getScore() {
            return score;
        }

        @Override
        public String getDescription() {
            return "EVA Maintenance @ " + target.getName();
        }

        @Override
        public Task createTask(Person person) {
            return new MaintainBuildingEVA(person, target);
        }

        @Override
        public Task createTask(Robot robot) {
            throw new UnsupportedOperationException("Robots cannot do outside maintenance");
        }
    }

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.maintainBuildingEVA"); //$NON-NLS-1$
	private static final double LOW_THRESHOLD = 2D;
	
    public MaintainBuildingEVAMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.OPERATION, FavoriteType.TINKERING);
		setTrait(TaskTrait.AGILITY, TaskTrait.STRENGTH);
		setPreferredJob(JobType.MECHANICS);
	}

	/**
	 * Find any building that need external EVA maintenance
	 */
    @Override
    public List<TaskJob> getTaskJobs(Person person) {

    	Settlement settlement = person.getSettlement();
        
        if (settlement == null)
        	return null;
        	  	
        // Check if an airlock is available
        if (EVAOperation.getWalkableAvailableAirlock(person, false) == null)
    		return null;

        // Check if it is night time.
        if (EVAOperation.isGettingDark(person))
			return null;

        // Checks if the person's settlement is at meal time and is hungry
        if (EVAOperation.isHungryAtMealTime(person))
			return null;
        
        // Checks if the person is physically fit for heavy EVA tasks
		if (!EVAOperation.isEVAFit(person))
			return null;
        	
    	// Check for radiation events
    	boolean[] exposed = settlement.getExposed();

		if (exposed[2]) {// SEP can give lethal dose of radiation
			return null;
		}
		
		double factor = 2D;
		factor *= getPersonModifier(person);

    	if (exposed[0]) {
			factor = factor/2D;// Baseline can give a fair amount dose of radiation
		}

    	if (exposed[1]) {// GCR can give nearly lethal dose of radiation
			factor = factor/4D;
		}

        if (factor < 0) 
			return null;

        return getSettlementTasks(settlement, factor);
    }

		/**
	 * Find any buildings that need internal maintenance
	 */
	private List<TaskJob> getSettlementTasks(Settlement settlement, double factor) {
		List<TaskJob>  tasks = new ArrayList<>();
	
		for (Building building: settlement.getBuildingManager().getBuildings()) {
			
			boolean inhabitableBuilding = !building.hasFunction(FunctionType.LIFE_SUPPORT);
			if (inhabitableBuilding) {
				double score = MaintainBuildingMeta.scoreMaintenance(building);
				score *= factor;
				if (score >= LOW_THRESHOLD) {
					tasks.add(new MaintainTaskJob(building, score));
				}
			}
		}

		return tasks;
	}
}
