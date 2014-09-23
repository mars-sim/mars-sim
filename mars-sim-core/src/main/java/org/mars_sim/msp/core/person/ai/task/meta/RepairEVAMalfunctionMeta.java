/**
 * Mars Simulation Project
 * RepairEVAMalfunctionMeta.java
 * @version 3.07 2014-09-18
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.Iterator;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.RepairEVAMalfunction;
import org.mars_sim.msp.core.person.ai.task.Task;

public class RepairEVAMalfunctionMeta implements MetaTask {
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.repairEVAMalfunction"); //$NON-NLS-1$
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new RepairEVAMalfunction(person);
    }

    @Override
    public double getProbability(Person person) {
        
        double result = 0D;

        // Total probabilities for all malfunctionable entities in person's local.
        Iterator<Malfunctionable> i = MalfunctionFactory.getMalfunctionables(person).iterator();
        while (i.hasNext()) {
            MalfunctionManager manager = i.next().getMalfunctionManager();
            Iterator<Malfunction> j = manager.getEVAMalfunctions().iterator();
            while (j.hasNext()) {
                Malfunction malfunction = j.next();
                try {
                    if (RepairEVAMalfunction.hasRepairPartsForMalfunction(person, person.getTopContainerUnit(), 
                            malfunction)) {
                        result += 100D;
                    }
                }
                catch (Exception e) {
                    e.printStackTrace(System.err);
                }
            }
        }

        // Check if an airlock is available
        if (EVAOperation.getWalkableAvailableAirlock(person) == null) {
            result = 0D;
        }

        // Check if it is night time.
        SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
        if (surface.getSurfaceSunlight(person.getCoordinates()) == 0) {
            if (!surface.inDarkPolarRegion(person.getCoordinates())) {
                result = 0D;
            }
        } 

        // Effort-driven task modifier.
        result *= person.getPerformanceRating();

        // Check if person is in vehicle.
        boolean inVehicle = LocationSituation.IN_VEHICLE == person.getLocationSituation();

        // Job modifier if not in vehicle.
        Job job = person.getMind().getJob();
        if ((job != null) && !inVehicle) {
            result *= job.getStartTaskProbabilityModifier(RepairEVAMalfunction.class);        
        }

        return result;
    }
}