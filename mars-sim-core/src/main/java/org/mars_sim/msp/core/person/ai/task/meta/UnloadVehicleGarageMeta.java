/*
 * Mars Simulation Project
 * UnloadVehicleGarageMeta.java
 * @date 2022-09-24
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.fav.FavoriteType;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.task.UnloadVehicleGarage;
import org.mars_sim.msp.core.person.ai.task.util.MetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * Meta task for the UnloadVehicleGarage task.
 */
public class UnloadVehicleGarageMeta extends MetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.unloadVehicleGarage"); //$NON-NLS-1$

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(UnloadVehicleGarageMeta.class.getName());
	
    public UnloadVehicleGarageMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.OPERATION);
		setTrait(TaskTrait.STRENGTH);
		setPreferredJob(JobType.LOADERS);
	}

    @Override
    public Task constructInstance(Person person) {
        return new UnloadVehicleGarage(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;
        
        Settlement settlement = person.getSettlement();
        
        if (settlement != null) {
        	
            // Probability affected by the person's stress and fatigue.
            if (!person.getPhysicalCondition().isFitByLevel(1000, 70, 1000))
            	return 0;

            // Check all vehicle missions occurring at the settlement.
            try {
                int numVehicles = 0;
                numVehicles += UnloadVehicleGarage.numMissionsNeedingUnloading(settlement, true);
                numVehicles += UnloadVehicleGarage.numNonMissionVehiclesNeedingUnloading(settlement);
                result = 100D * numVehicles;
            }
            catch (Exception e) {
                logger.severe(person, 4_000, "Error finding unloading missions. " + e.getMessage());
            }
            
            if (result <= 0) result = 0;

            // Settlement factors
            result *= person.getSettlement().getGoodsManager().getTransportationFactor();
            
            result = applyPersonModifier(result, person);
        }

        return result;
    }

	@Override
	public Task constructInstance(Robot robot) {
        return new UnloadVehicleGarage(robot);
	}

	@Override
	public double getProbability(Robot robot) {

        double result = 0D;

        Settlement settlement = robot.getSettlement();
        
        if (settlement != null) {

            // Check all vehicle missions occurring at the settlement.
            try {
                int numVehicles = 0;
                      
                numVehicles += UnloadVehicleGarage.numMissionsNeedingUnloading(settlement, true);
                numVehicles += UnloadVehicleGarage.numNonMissionVehiclesNeedingUnloading(settlement);
                result = 100D * numVehicles;
            }
            catch (Exception e) {
            	logger.severe(robot, 4_000, "Error finding unloading missions. " + e.getMessage());
            }

	        // Effort-driven task modifier.
	        result *= robot.getPerformanceRating();
        }
        
        return result;
    }
}
