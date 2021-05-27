/**
 * Mars Simulation Project
 * TendGreenhouseMeta.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.logging.Level;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.task.TendFishTank;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.ai.job.Gardenbot;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
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
		setFavorite(FavoriteType.TENDING_PLANTS);
		setPreferredJob(JobType.BIOLOGIST);
	}

    @Override
    public Task constructInstance(Person person) {
        return new TendFishTank(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;

        if (person.isInSettlement()) {
        	
            // Probability affected by the person's stress and fatigue.
            PhysicalCondition condition = person.getPhysicalCondition();
            double fatigue = condition.getFatigue();
            double stress = condition.getStress();
            double hunger = condition.getHunger();
            
            if (fatigue > 1000 || stress > 80 || hunger > 500)
            	return 0;
            
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

        if (robot.getBotMind().getRobotJob() instanceof Gardenbot && robot.isInSettlement()) {

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
	 * @param building
	 * @return
	 */
	private int getOutstandingTask(Building building) {
		Fishery fistTank = (Fishery) building.getFunction(FunctionType.FISHERY);
		
		return  fistTank.getUncleaned().size() + fistTank.getUninspected().size() + fistTank.getSurplusStock()
				+ fistTank.getWeedDemand();
	}
}
