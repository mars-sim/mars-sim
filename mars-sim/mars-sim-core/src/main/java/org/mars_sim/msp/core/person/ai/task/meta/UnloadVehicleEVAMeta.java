/**
 * Mars Simulation Project
 * UnloadVehicleEVA.java
 * @version 3.07 2014-08-11
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.person.ai.task.UnloadVehicleEVA;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * Meta task for the UnloadVehicleEVA task.
 */
public class UnloadVehicleEVAMeta implements MetaTask {

    // TODO: Use enum instead of string for name for internationalization.
    private static final String NAME = "Unloading vehicle EVA";
    
    /** default logger. */
    private static Logger logger = Logger.getLogger(UnloadVehicleEVAMeta.class.getName());
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new UnloadVehicleEVA(person);
    }

    @Override
    public double getProbability(Person person) {
       
        double result = 0D;

        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {

            // Check all vehicle missions occurring at the settlement.
            try {
                int numVehicles = 0;
                numVehicles += UnloadVehicleEVA.getAllMissionsNeedingUnloading(person.getSettlement()).size();
                numVehicles += UnloadVehicleEVA.getNonMissionVehiclesNeedingUnloading(person.getSettlement()).size();
                result = 50D * numVehicles;
            }
            catch (Exception e) {
                logger.log(Level.SEVERE,"Error finding unloading missions. " + e.getMessage());
                e.printStackTrace(System.err);
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

        // Crowded settlement modifier
        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
            Settlement settlement = person.getSettlement();
            if (settlement.getCurrentPopulationNum() > settlement.getPopulationCapacity()) {
                result *= 2D;
            }
        }

        // Effort-driven task modifier.
        result *= person.getPerformanceRating();

        // Job modifier.
        Job job = person.getMind().getJob();
        if (job != null) {
            result *= job.getStartTaskProbabilityModifier(UnloadVehicleEVA.class);        
        }

        return result;
    }
}