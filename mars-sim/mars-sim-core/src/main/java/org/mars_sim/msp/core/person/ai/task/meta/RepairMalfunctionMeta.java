/**
 * Mars Simulation Project
 * RepairMalfunctionMeta.java
 * @version 3.07 2014-12-27
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.Iterator;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.Robot;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.RepairMalfunction;
import org.mars_sim.msp.core.person.ai.task.Task;

/**
 * Meta task for the RepairMalfunction task.
 */
public class RepairMalfunctionMeta implements MetaTask {
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.repairMalfunction"); //$NON-NLS-1$
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new RepairMalfunction(person);
    }

    @Override
    public double getProbability(Person person) {
        
        double result = 0D;

        // Add probability for all malfunctionable entities in person's local.
        Iterator<Malfunctionable> i = MalfunctionFactory.getMalfunctionables(person).iterator();
        while (i.hasNext()) {
            Malfunctionable entity = i.next();
            if (!RepairMalfunction.requiresEVA(person, entity)) {
                MalfunctionManager manager = entity.getMalfunctionManager();
                Iterator<Malfunction> j = manager.getNormalMalfunctions().iterator();
                while (j.hasNext()) {
                    Malfunction malfunction = j.next();
                    try {
                        if (RepairMalfunction.hasRepairPartsForMalfunction(person, malfunction)) {
                            result += 100D;
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace(System.err);
                    }
                }
            }
        }

        // Effort-driven task modifier.
        result *= person.getPerformanceRating();

        // Job modifier.
        Job job = person.getMind().getJob();
        if (job != null) {
            result *= job.getStartTaskProbabilityModifier(RepairMalfunction.class);        
        }

        return result;
    }

	@Override
	public Task constructInstance(Robot robot) {
        return new RepairMalfunction(robot);
	}

	@Override
	public double getProbability(Robot robot) {
	    
        double result = 0D;

        // Add probability for all malfunctionable entities in robot's local.
        Iterator<Malfunctionable> i = MalfunctionFactory.getMalfunctionables(robot).iterator();
        while (i.hasNext()) {
            Malfunctionable entity = i.next();
            if (!RepairMalfunction.requiresEVA(robot, entity)) {
                MalfunctionManager manager = entity.getMalfunctionManager();
                Iterator<Malfunction> j = manager.getNormalMalfunctions().iterator();
                while (j.hasNext()) {
                    Malfunction malfunction = j.next();
                    try {
                        if (RepairMalfunction.hasRepairPartsForMalfunction(robot, malfunction)) {
                            result += 100D;
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace(System.err);
                    }
                }
            }
        }

        // Effort-driven task modifier.
        result *= robot.getPerformanceRating();

        // Job modifier.
        Job job = robot.getMind().getJob();
        if (job != null) {
            result *= job.getStartTaskProbabilityModifier(RepairMalfunction.class);        
        }

        return result;
	}
}