/*
 * Mars Simulation Project
 * TendGreenhouseMeta.java
 * @date 2022-08-01
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.logging.Level;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.fav.FavoriteType;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.task.TendGreenhouse;
import org.mars_sim.msp.core.person.ai.task.util.MetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotType;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * Meta task for the Tend Greenhouse task.
 */
public class TendGreenhouseMeta extends MetaTask {

    private static final double VALUE = 2;
    
    private static final int CAP = 4_000;
    
	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(TendGreenhouseMeta.class.getName());

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.tendGreenhouse"); //$NON-NLS-1$

    public TendGreenhouseMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.ANY_HOUR);
		setFavorite(FavoriteType.TENDING_GARDEN);
		setPreferredJob(JobType.BOTANIST, JobType.BIOLOGIST);
		setTrait(TaskTrait.ARTISTIC, TaskTrait.RELAXATION);
	}

    @Override
    public Task constructInstance(Person person) {
        return new TendGreenhouse(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;

        if (person.isInSettlement()) {
            
            try {
                // See if there is an available greenhouse.
                Building farmingBuilding = TendGreenhouse.getAvailableGreenhouse(person);
                if (farmingBuilding != null) {
  	
                    int needyCropsNum = person.getSettlement().getCropsNeedingTending();
                    if (needyCropsNum == 0)
                    	return 0;
                    
                    double tendingNeed = person.getSettlement().getCropsTendingNeed();

                    result = tendingNeed * VALUE;
                    
                    if (result <= 0) result = 0;
                    
                    // Crowding modifier.
                    result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, farmingBuilding);
                    result *= TaskProbabilityUtil.getRelationshipModifier(person, farmingBuilding);

                    // Settlement factors
            		result *= (person.getSettlement().getGoodsManager().getCropFarmFactor()
            				+ person.getAssociatedSettlement().getGoodsManager().getTourismFactor());
            		
                    result = applyPersonModifier(result, person);
                    
                    if (result > CAP)
                    	result = CAP;
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
                    if (needyCropsNum == 0)
                    	return 0;
                    
                    double tendingNeed = robot.getSettlement().getCropsTendingNeed();
                    result = tendingNeed * VALUE;
                    
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
