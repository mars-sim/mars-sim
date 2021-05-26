/**
 * Mars Simulation Project
 * MaintenanceMeta.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
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
 * Meta task for the Maintenance task.
 */
public class MaintenanceMeta extends MetaTask {

	/** default logger. */
	private static Logger logger = Logger.getLogger(MaintenanceMeta.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.maintenance"); //$NON-NLS-1$

	private static final double FACTOR = 1D;
	
    public MaintenanceMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.WORK_HOUR);
		addFavorite(FavoriteType.OPERATION);
		addFavorite(FavoriteType.TINKERING);
		addTrait(TaskTrait.ACADEMIC);
		addTrait(TaskTrait.STRENGTH);
		setPreferredJob(JobType.ENGINEER, JobType.TECHNICIAN);
    }
    
	@Override
	public Task constructInstance(Person person) {
		return new Maintenance(person);
	}

	@Override
	public double getProbability(Person person) {
		double result = 0D;
        
		if (person.isInSettlement()) {
            
			try {
				// Total probabilities for all malfunctionable entities in person's local.
				Iterator<Malfunctionable> i = MalfunctionFactory.getLocalMalfunctionables(person).iterator();
				while (i.hasNext()) {
					Malfunctionable entity = i.next();
					
					boolean isVehicle = (entity instanceof Vehicle);
					if (isVehicle)
						return 0;
					
					boolean uninhabitableBuilding = false;
					if (entity instanceof Building) {
						uninhabitableBuilding = !((Building) entity).hasFunction(FunctionType.LIFE_SUPPORT);
					}
					if (uninhabitableBuilding)
						return 0;
					
					MalfunctionManager manager = entity.getMalfunctionManager();
					boolean hasMalfunction = manager.hasMalfunction();
					if (hasMalfunction) {
						return 0;
					}
					
					boolean hasParts = Maintenance.hasMaintenanceParts(person, entity);
					if (!hasParts) {
						return 0;
					}
					
					double effectiveTime = manager.getEffectiveTimeSinceLastMaintenance();
					boolean minTime = (effectiveTime >= 1000D);
					
					if (minTime) {
						double entityProb = effectiveTime / 1000D;
						if (entityProb > 100D) {
							entityProb = 100D;
						}
						result += entityProb * FACTOR;
					}
				}        
		        
			} catch (Exception e) {
				logger.log(Level.SEVERE, "getProbability()", e);
			}

			result = applyPersonModifier(result, person);
		}

		return result;
	}


	public double getSettlementProbability(Settlement settlement) {
		double result = 0D;

		try {
			// Total probabilities for all malfunctionable entities in person's local.
			Iterator<Malfunctionable> i = MalfunctionFactory.getMalfunctionables(settlement).iterator();
			while (i.hasNext()) {
				Malfunctionable entity = i.next();
				boolean isVehicle = (entity instanceof Vehicle);
				boolean uninhabitableBuilding = false;
				if (entity instanceof Building) {
					uninhabitableBuilding = !((Building) entity).hasFunction(FunctionType.LIFE_SUPPORT);
				}
				MalfunctionManager manager = entity.getMalfunctionManager();
				boolean hasMalfunction = manager.hasMalfunction();
				boolean hasParts = Maintenance.hasMaintenanceParts(settlement, entity);
				double effectiveTime = manager.getEffectiveTimeSinceLastMaintenance();
				boolean minTime = (effectiveTime >= 1000D);
				if (!hasMalfunction && !isVehicle && !uninhabitableBuilding && hasParts && minTime) {
					double entityProb = effectiveTime / 1000D;
					if (entityProb > 100D) {
						entityProb = 100D;
					}
					result += entityProb * FACTOR;
				}
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "getProbability()", e);
		}


		return result;
	}
	
	@Override
	public Task constructInstance(Robot robot) {
		return new Maintenance(robot);
	}

	@Override
	public double getProbability(Robot robot) {
		double result = 0D;

		if (robot.isInSettlement() && robot.getRobotType() == RobotType.REPAIRBOT) {
//		if (robot.getBotMind().getRobotJob() instanceof Repairbot && robot.isInside()) {

			try {
				// Total probabilities for all malfunctionable entities in robot's local.
				Iterator<Malfunctionable> i = MalfunctionFactory.getMalfunctionables(robot).iterator();
				while (i.hasNext()) {
					Malfunctionable entity = i.next();
					boolean isVehicle = (entity instanceof Vehicle);
					if (isVehicle)
						return 0;
					boolean uninhabitableBuilding = false;
					if (entity instanceof Building) {
						uninhabitableBuilding = !((Building) entity).hasFunction(FunctionType.LIFE_SUPPORT);
					}
					if (uninhabitableBuilding)
						return 0;
					
					MalfunctionManager manager = entity.getMalfunctionManager();
					boolean hasMalfunction = manager.hasMalfunction();
					if (hasMalfunction)
						return 0;
					
					boolean hasParts = Maintenance.hasMaintenanceParts(robot, entity);
					if (!hasParts)
						return 0;
					
					double effectiveTime = manager.getEffectiveTimeSinceLastMaintenance();
					boolean minTime = (effectiveTime >= 1000D);
					if (minTime) {
						double entityProb = effectiveTime / 1000D;
						if (entityProb > 100D) {
							entityProb = 100D;
						}
						result += entityProb * FACTOR;
					}
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, "getProbability()", e);
			}

			// Effort-driven task modifier.
			result *= robot.getPerformanceRating();

		}

		return result;
	}
}
