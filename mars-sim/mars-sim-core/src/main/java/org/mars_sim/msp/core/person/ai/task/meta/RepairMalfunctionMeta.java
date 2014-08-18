/**
 * Mars Simulation Project
 * RepairMalfunctionMeta.java
 * @version 3.07 2014-08-05
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.Iterator;

import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.RepairMalfunction;
import org.mars_sim.msp.core.person.ai.task.Task;

/**
 * Meta task for the RepairMalfunction task.
 */
public class RepairMalfunctionMeta implements MetaTask {

    // TODO: Use enum instead of string for name for internationalization.
    private static final String NAME = "Repairing Malfunction";
    
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

        // Total probabilities for all malfunctionable entities in person's local.
        Iterator<Malfunctionable> i = MalfunctionFactory.getMalfunctionables(person).iterator();
        while (i.hasNext()) {
            Malfunctionable entity = i.next();
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

        // Effort-driven task modifier.
        result *= person.getPerformanceRating();

        // Job modifier.
        Job job = person.getMind().getJob();
        if (job != null) {
            result *= job.getStartTaskProbabilityModifier(RepairMalfunction.class);        
        }

        return result;
    }
}