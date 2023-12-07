/*
 * Mars Simulation Project
 * TendAlgaePondMeta.java
 * @date 2023-09-19
 * @author Manny Kung
 */
package com.mars_sim.core.person.ai.task.meta;

import java.util.ArrayList;
import java.util.List;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.task.TendAlgaePond;
import com.mars_sim.core.person.ai.task.util.MetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementMetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskUtil;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.robot.RobotType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.structure.building.function.farming.AlgaeFarming;
import com.mars_sim.tools.Msg;

/**
 * Meta task for tending algae pond.
 */
public class TendAlgaePondMeta extends MetaTask implements SettlementMetaTask {

    /**
     * Represents the job needed in an algae pond.
     */
    private static class AlgaeTaskJob extends SettlementTask {

		private static final long serialVersionUID = 1L;
	
        private AlgaeFarming pond;

        public AlgaeTaskJob(SettlementMetaTask owner, AlgaeFarming pond, RatingScore score) {
            super(owner, "Tend Algae Pond", pond.getBuilding(), score);
            this.pond = pond;
        }

        @Override
        public Task createTask(Person person) {
            return new TendAlgaePond(person, pond);
        }

        @Override
        public Task createTask(Robot robot) {
            return new TendAlgaePond(robot, pond);
        }
    }

	private static final int BASE_SCORE = 50;
	
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.tendAlgaePond"); //$NON-NLS-1$
	
    public TendAlgaePondMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.ANY_HOUR);
		setFavorite(FavoriteType.TENDING_FARM);
		setPreferredJob(JobType.BOTANIST, JobType.BIOLOGIST, JobType.CHEMIST);
		setPreferredRole(RoleType.SCIENCE_SPECIALIST, RoleType.CHIEF_OF_SCIENCE,
				RoleType.CHIEF_OF_SUPPLY_N_RESOURCES);
		setTrait(TaskTrait.ARTISTIC, TaskTrait.RELAXATION);
        addPreferredRobot(RobotType.GARDENBOT);
	}

    
    /**
     * Gets the score for a Settlement task for a person. 
     * Considers the number of Person farmers and any personal preferences.
     * 
	 * @return The factor to adjust task score; 0 means task is not applicable
     */
    @Override
	public RatingScore assessPersonSuitability(SettlementTask t, Person p) {
        RatingScore factor = RatingScore.ZERO_RATING;
        if (p.isInSettlement()) {
			factor = super.assessPersonSuitability(t, p);
            if (factor.getScore() == 0) {
                return factor;
            }
            
            // Crowding modifier.
            Building b = ((AlgaeTaskJob)t).pond.getBuilding();
            assessBuildingSuitability(factor, b, p);
		}
		return factor;
	}

    /**
     * For a robot the over crowding probability is considered.
     * 
	 * @return The factor to adjust task score; 0 means task is not applicable
     */
	@Override
	public RatingScore assessRobotSuitability(SettlementTask t, Robot r)  {
        return TaskUtil.assessRobot(t, r);
    }

    /**
     * Scans the settlement tanks for any that need tending. 
     * Creates one task per applicable AlgaeFarming function.
     * 
     * @param settlement Source to scan
     * @return List of applicable tasks
     */
    @Override
    public List<SettlementTask> getSettlementTasks(Settlement settlement) {
        List<SettlementTask> tasks = new ArrayList<>();

        for (Building building : settlement.getBuildingManager().getBuildingSet(FunctionType.ALGAE_FARMING)) {
            AlgaeFarming pond = building.getAlgae();
            
            RatingScore result = new RatingScore("base", BASE_SCORE);

            result.addBase("maintenance", 
            		2 * (200 - pond.getCleaningScore() - pond.getInspectionScore()));
    
            double ratio = pond.getSurplusRatio();
            result.addBase("surplus", ratio * 50);
            
            double foodDemand = pond.getNutrientDemand();     
            result.addBase("nutrient.demand", foodDemand * 200);
            
            double nutrientRatio = pond.getCurrentNutrientRatio();
            double defaultNRatio = AlgaeFarming.NUTRIENT_RATIO;
  
            result.addBase("nutrient.ratio", defaultNRatio / nutrientRatio * 50);
      
            if (result.getScore() > 0) {
                tasks.add(new AlgaeTaskJob(this, pond, result));
            }
        }

        return tasks;
    }
}
