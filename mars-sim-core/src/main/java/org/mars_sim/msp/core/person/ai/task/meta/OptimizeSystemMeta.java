/*
 * Mars Simulation Project
 * OptimizeSystem.java
 * @date 2022-08-01
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.task.Maintenance;
import org.mars_sim.msp.core.person.ai.task.OptimizeSystem;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskTrait;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for the OptimizeSystem task.
 */
public class OptimizeSystemMeta extends MetaTask {

	/** default logger. */
	private static final Logger logger = Logger.getLogger(OptimizeSystemMeta.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.optimizeSystem"); //$NON-NLS-1$

	private static final double FACTOR = 1D;
	
    public OptimizeSystemMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.OPERATION, FavoriteType.TINKERING);
		setTrait(TaskTrait.ACADEMIC);
		setPreferredJob(JobType.MECHANICS);
    }
    
	@Override
	public Task constructInstance(Person person) {
		return new OptimizeSystem(person);
	}

	@Override
	public double getProbability(Person person) {
		double result = 0D;
        
		if (person.isInSettlement()) {
            
			try {
				result += 0;
		        
			} catch (Exception e) {
				logger.log(Level.SEVERE, "getProbability()", e);
			}

			result = applyPersonModifier(result, person);
		}

		return result;
	}


	public double getSettlementProbability(Settlement settlement) {
		double result = 0D;
		return result;
	}
	

}
