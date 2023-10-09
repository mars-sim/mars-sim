/*
 * Mars Simulation Project
 * TendAlgaePondMeta.java
 * @date 2023-09-19
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.ArrayList;
import java.util.List;

import org.mars.sim.tools.Msg;
import org.mars_sim.msp.core.data.RatingScore;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.fav.FavoriteType;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.task.TendAlgaePond;
import org.mars_sim.msp.core.person.ai.task.util.MetaTask;
import org.mars_sim.msp.core.person.ai.task.util.SettlementMetaTask;
import org.mars_sim.msp.core.person.ai.task.util.SettlementTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskProbabilityUtil;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.farming.AlgaeFarming;

/**
 * Meta task for tending algae pond.
 */
public class TendAlgaePondMeta extends MetaTask implements SettlementMetaTask {

    /**
     * Represents the job needed in an algae pond
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

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.tendAlgaePond"); //$NON-NLS-1$
	
    public TendAlgaePondMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.ANY_HOUR);
		setFavorite(FavoriteType.TENDING_GARDEN);
		setPreferredJob(JobType.BOTANIST, JobType.BIOLOGIST, JobType.CHEMIST);

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
        return TaskProbabilityUtil.assessRobot(t, r);
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

        for(Building building : settlement.getBuildingManager().getBuildingSet(FunctionType.ALGAE_FARMING)) {
            AlgaeFarming pond = building.getAlgae();
            RatingScore result = new RatingScore("maintenance",
                        (pond.getUncleaned().size() + pond.getUninspected().size()) * 3D);

            double ratio = pond.getSurplusRatio();
            		
            result.addBase("surplus", (1 - ratio) * 10D);
            
            double foodDemand = pond.getFoodDemand();
         
            result.addBase("goods", foodDemand * 10);
            
            double foodMass = pond.getFoodMass();
            double algaeMass = pond.getCurrentAlgae();
            
            if (foodMass < 0)
            	// Need to 
            	result.addBase("ratio", Math.exp(2 * (1 - foodMass)));
            else
            	result.addBase("ratio", algaeMass / foodMass / 5);
            
            if (result.getScore() > 0) {
                tasks.add(new AlgaeTaskJob(this, pond, result));
            }
        }

        return tasks;
    }
}
