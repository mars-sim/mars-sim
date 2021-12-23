/*
 * Mars Simulation Project
 * TendGreenhouseMeta.java
 * @date 2021-12-22
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.logging.Level;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.task.TendGreenhouse;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskTrait;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotType;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * Meta task for the Tend Greenhouse task.
 */
public class TendGreenhouseMeta extends MetaTask {

    private static final double VALUE = 4D;
    
	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(TendGreenhouseMeta.class.getName());

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.tendGreenhouse"); //$NON-NLS-1$

    public TendGreenhouseMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.TENDING_PLANTS);
		setTrait(TaskTrait.ARTISITC);
		setPreferredJob(JobType.BOTANIST, JobType.BIOLOGIST);
	}

    @Override
    public Task constructInstance(Person person) {
        return new TendGreenhouse(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;

        if (person.isInSettlement()) {
        	
            // Probability affected by the person's stress and fatigue.
            if (!person.getPhysicalCondition().isFitByLevel(1000, 80, 500))
            	return 0;
            
            try {
                // See if there is an available greenhouse.
                Building farmingBuilding = TendGreenhouse.getAvailableGreenhouse(person);
                if (farmingBuilding != null) {

                    int needyCropsNum = person.getSettlement().getCropsNeedingTending();
                    result = needyCropsNum * VALUE;

                    if (result <= 0) result = 0;
                    
                    // Crowding modifier.
                    result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, farmingBuilding);
                    result *= TaskProbabilityUtil.getRelationshipModifier(person, farmingBuilding);

                    // Settlement factors
            		result *= (person.getSettlement().getGoodsManager().getCropFarmFactor()
            				+ .5 * person.getAssociatedSettlement().getGoodsManager().getTourismFactor());
            		
                    result = applyPersonModifier(result, person);
                }
            }
            catch (Exception e) {
            	logger.log(Level.SEVERE, person + " cannot calculate probability : " + e.getMessage());
            }

        }

        return result;
    }

	@Override
	public Task constructInstance(Robot robot) {
        return new TendGreenhouse(robot);
	}

	@Override
	public double getProbability(Robot robot) {

        double result = 0D;

        if (robot.getRobotType() == RobotType.GARDENBOT && robot.isInSettlement()) {

            try {
                // See if there is an available greenhouse.
                Building farmingBuilding = TendGreenhouse.getAvailableGreenhouse(robot);
                if (farmingBuilding != null) {
 
                    int needyCropsNum = robot.getSettlement().getCropsNeedingTending();

                    result += needyCropsNum * 50D;
    	            // Effort-driven task modifier.
    	            result *= robot.getPerformanceRating();
                }
            }
            catch (Exception e) {
                logger.log(Level.SEVERE, robot + " cannot calculate probability : " + e.getMessage());
            }


        }

        return result;
	}
}
