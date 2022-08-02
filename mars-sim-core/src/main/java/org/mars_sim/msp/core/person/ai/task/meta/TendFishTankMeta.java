/*
 * Mars Simulation Project
 * TendFishTankMeta.java
 * @date 2022-08-01
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.logging.Level;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.task.TendFishTank;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskTrait;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotType;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.farming.Fishery;

/**
 * Meta task for the Tend Fish Tank task.
 */
public class TendFishTankMeta extends MetaTask {

    private static final SimLogger logger = SimLogger.getLogger(TendFishTankMeta.class.getName());
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.tendFishTank"); //$NON-NLS-1$

    public TendFishTankMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.TENDING_GARDEN);
		setPreferredJob(JobType.BOTANIST, JobType.BIOLOGIST);
		setTrait(TaskTrait.ARTISTIC, TaskTrait.RELAXATION);
	}

    @Override
    public Task constructInstance(Person person) {
        return new TendFishTank(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;

        if (person.isInSettlement()) {

            try {
                // See if there is an available greenhouse.
                Building building = TendFishTank.getAvailableFishTank(person);
                if (building != null) {

                    int outstandingTasks = getOutstandingTask(building);

                    result += outstandingTasks * 2D;

                    if (result <= 0) result = 0;
                    
                    // Crowding modifier.
                    result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, building);
                    result *= TaskProbabilityUtil.getRelationshipModifier(person, building);

                    result = applyPersonModifier(result, person);
                }
            }
            catch (Exception e) {
                logger.log(Level.SEVERE, "Problem calculating Person probability", e);
            }
        }
        return result;
    }

	@Override
	public Task constructInstance(Robot robot) {
        return new TendFishTank(robot);
	}

	@Override
	public double getProbability(Robot robot) {

        double result = 0D;

        if (robot.getRobotType() == RobotType.GARDENBOT && robot.isInSettlement()) {

            try {
                // See if there is an available greenhouse.
                Building building = TendFishTank.getAvailableFishTank(robot);
                if (building != null) {
 
                    int outstandingTasks = getOutstandingTask(building);

                    result += outstandingTasks * 50D;
    	            // Effort-driven task modifier.
    	            result *= robot.getPerformanceRating();
                }
            }
            catch (Exception e) {
                logger.log(Level.SEVERE, "Problem calculating Robot probability", e);
            }
        }

        return result;
	}

	/**
	 * Basic approach of counting up things that can be done.
	 * 
	 * @param building
	 * @return
	 */
	private int getOutstandingTask(Building building) {
		Fishery fistTank = building.getFishery();
		
		return  fistTank.getUncleaned().size() + fistTank.getUninspected().size() + fistTank.getSurplusStock()
				+ fistTank.getWeedDemand();
	}
}
