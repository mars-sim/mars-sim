/**
 * Mars Simulation Project
 * UnloadVehicleGarageMeta.java
 * @version 3.07 2014-08-06
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.person.ai.task.UnloadVehicleGarage;

/**
 * Meta task for the UnloadVehicleGarage task.
 */
public class UnloadVehicleGarageMeta implements MetaTask {

    // TODO: Use enum instead of string for name for internationalization.
    private static final String NAME = "Unloading Vehicle";
    
    /** default logger. */
    private static Logger logger = Logger.getLogger(RelaxMeta.class.getName());
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new UnloadVehicleGarage(person);
    }

    @Override
    public double getProbability(Person person) {
        
        double result = 0D;

        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
            
            // Check all vehicle missions occurring at the settlement.
            try {
                int numVehicles = 0;
                numVehicles += UnloadVehicleGarage.getAllMissionsNeedingUnloading(person.getSettlement()).size();
                numVehicles += UnloadVehicleGarage.getNonMissionVehiclesNeedingUnloading(person.getSettlement()).size();
                result = 50D * numVehicles;
            }
            catch (Exception e) {
                logger.log(Level.SEVERE,"Error finding unloading missions. " + e.getMessage());
                e.printStackTrace(System.err);
            }
        }

        // Effort-driven task modifier.
        result *= person.getPerformanceRating();
        
        // Job modifier.
        Job job = person.getMind().getJob();
        if (job != null) {
            result *= job.getStartTaskProbabilityModifier(UnloadVehicleGarage.class);        
        }
    
        return result;
    }
}