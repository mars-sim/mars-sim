/**
 * Mars Simulation Project
 * MaintenanceMeta.java
 * @version 3.07 2014-09-18
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
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.Robot;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.job.RobotJob;
import org.mars_sim.msp.core.person.ai.task.Maintenance;
import org.mars_sim.msp.core.person.ai.task.MaintenanceEVA;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for the Maintenance task.
 */
public class MaintenanceMeta implements MetaTask {
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.maintenance"); //$NON-NLS-1$
    
    /** default logger. */
    private static Logger logger = Logger.getLogger(MaintenanceMeta.class.getName());
    
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

        try {
            // Total probabilities for all malfunctionable entities in person's local.
            Iterator<Malfunctionable> i = MalfunctionFactory.getMalfunctionables(person).iterator();
            while (i.hasNext()) {
                Malfunctionable entity = i.next();
                boolean isVehicle = (entity instanceof Vehicle);
                boolean uninhabitableBuilding = false;
                if (entity instanceof Building) 
                    uninhabitableBuilding = !((Building) entity).hasFunction(BuildingFunction.LIFE_SUPPORT);
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
                    result += entityProb;
                }
            }
        }
        catch (Exception e) {
            logger.log(Level.SEVERE,"getProbability()",e);
        }

        // Effort-driven task modifier.
        result *= person.getPerformanceRating();

        // Job modifier.
        Job job = person.getMind().getJob();
        if (job != null) {
            result *= job.getStartTaskProbabilityModifier(Maintenance.class);        
        }

        return result;
    }

	@Override
	public Task constructInstance(Robot robot) {
        return new Maintenance(robot);
	}

	@Override
	public double getProbability(Robot robot) {
        double result = 300D;

        try {
            // Total probabilities for all malfunctionable entities in robot's local.
            Iterator<Malfunctionable> i = MalfunctionFactory.getMalfunctionables(robot).iterator();
            while (i.hasNext()) {
                Malfunctionable entity = i.next();
                boolean isVehicle = (entity instanceof Vehicle);
                boolean uninhabitableBuilding = false;
                if (entity instanceof Building) 
                    uninhabitableBuilding = !((Building) entity).hasFunction(BuildingFunction.LIFE_SUPPORT);
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
                    result += entityProb;
                }
            }
        }
        catch (Exception e) {
            logger.log(Level.SEVERE,"getProbability()",e);
        }

        // Effort-driven task modifier.
        result *= robot.getPerformanceRating();

        // Job modifier.
        RobotJob robotJob = robot.getMind().getRobotJob();
        if (robotJob != null) {
            result *= robotJob.getStartTaskProbabilityModifier(Maintenance.class);
        }

        return result;
	}
}