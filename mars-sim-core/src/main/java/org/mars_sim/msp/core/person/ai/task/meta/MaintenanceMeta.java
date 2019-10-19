/**
 * Mars Simulation Project
 * MaintenanceMeta.java
 * @version 3.1.0 2018-09-29
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.Maintenance;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.ai.job.Repairbot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for the Maintenance task.
 */
public class MaintenanceMeta implements MetaTask, Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(MaintenanceMeta.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.maintenance"); //$NON-NLS-1$

	private static final double FACTOR = 1D;
	

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public Task constructInstance(Person person) {
		return new Maintenance(person);
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
            
            if (fatigue > 1000 || stress > 50 || hunger > 500)
            	return 0;
            
			try {
				// Total probabilities for all malfunctionable entities in person's local.
				Iterator<Malfunctionable> i = MalfunctionFactory.getMalfunctionables(person).iterator();
				while (i.hasNext()) {
					Malfunctionable entity = i.next();
					boolean isVehicle = (entity instanceof Vehicle);
					boolean uninhabitableBuilding = false;
					if (entity instanceof Building) {
						uninhabitableBuilding = !((Building) entity).hasFunction(FunctionType.LIFE_SUPPORT);
					}
					MalfunctionManager manager = entity.getMalfunctionManager();
					boolean hasMalfunction = manager.hasMalfunction();
					boolean hasParts = Maintenance.hasMaintenanceParts(person, entity);
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

			// Effort-driven task modifier.
			result *= person.getPerformanceRating();

			// Job modifier.
			Job job = person.getMind().getJob();
			if (job != null) {
				result *= job.getStartTaskProbabilityModifier(Maintenance.class);
			}

			// Modify if tinkering is the person's favorite activity.
			if (person.getFavorite().getFavoriteActivity() == FavoriteType.TINKERING) {
				result += RandomUtil.getRandomInt(1, 20);
			}

			// AddPreference modifier
			if (result > 0D) {
				result = result + result * person.getPreference().getPreferenceScore(this) / 5D;
			}

			if (result < 0)
				result = 0;

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

		if (robot.getBotMind().getRobotJob() instanceof Repairbot) {

			try {
				// Total probabilities for all malfunctionable entities in robot's local.
				Iterator<Malfunctionable> i = MalfunctionFactory.getMalfunctionables(robot).iterator();
				while (i.hasNext()) {
					Malfunctionable entity = i.next();
					boolean isVehicle = (entity instanceof Vehicle);
					boolean uninhabitableBuilding = false;
					if (entity instanceof Building) {
						uninhabitableBuilding = !((Building) entity).hasFunction(FunctionType.LIFE_SUPPORT);
					}
					MalfunctionManager manager = entity.getMalfunctionManager();
					boolean hasMalfunction = manager.hasMalfunction();
					boolean hasParts = Maintenance.hasMaintenanceParts(robot, entity);
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

			// Effort-driven task modifier.
			result *= robot.getPerformanceRating();

		}

		return result;
	}
}