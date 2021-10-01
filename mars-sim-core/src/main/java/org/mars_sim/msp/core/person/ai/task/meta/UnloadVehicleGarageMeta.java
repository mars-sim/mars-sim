/*
 * Mars Simulation Project
 * UnloadVehicleGarageMeta.java
 * @date 2021-08-28
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.task.UnloadVehicleGarage;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskTrait;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.ai.job.Deliverybot;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * Meta task for the UnloadVehicleGarage task.
 */
public class UnloadVehicleGarageMeta extends MetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.unloadVehicleGarage"); //$NON-NLS-1$

    /** default logger. */
    private static final Logger logger = Logger.getLogger(RelaxMeta.class.getName());

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

        if (person.isInSettlement()) {
        	
            // Probability affected by the person's stress and fatigue.
            if (!person.getPhysicalCondition().isFitByLevel(1000, 70, 1000))
            	return 0;
            
	    	Settlement settlement = person.getSettlement();
	  
            // Check all vehicle missions occurring at the settlement.
            try {
                int numVehicles = 0;
                numVehicles += UnloadVehicleGarage.getAllMissionsNeedingUnloading(settlement, true).size();
                numVehicles += UnloadVehicleGarage.getNonMissionVehiclesNeedingUnloading(settlement).size();
                result = 100D * numVehicles;
            }
            catch (Exception e) {
                logger.log(Level.SEVERE,"Error finding unloading missions. " + e.getMessage());
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

        if (robot.getBotMind().getRobotJob() instanceof Deliverybot)
	        if (robot.isInSettlement()) {

	            // Check all vehicle missions occurring at the settlement.
	            try {
	                int numVehicles = 0;
	                
	               	Settlement settlement = robot.getAssociatedSettlement();
	                
	                numVehicles += UnloadVehicleGarage.getAllMissionsNeedingUnloading(settlement, true).size();
	                numVehicles += UnloadVehicleGarage.getNonMissionVehiclesNeedingUnloading(settlement).size();
	                result = 100D * numVehicles;
	            }
	            catch (Exception e) {
	                logger.log(Level.SEVERE,"Error finding unloading missions. " + e.getMessage());
	            }

		        // Effort-driven task modifier.
		        result *= robot.getPerformanceRating();
	        }

        return result;
    }
}
