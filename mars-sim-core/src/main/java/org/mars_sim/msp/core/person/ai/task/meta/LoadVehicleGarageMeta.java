/**
 * Mars Simulation Project
 * LoadVehicleGarageMeta.java
 * @version 3.07 2014-09-18
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.task.LoadVehicleGarage;
import org.mars_sim.msp.core.person.ai.task.Task;

/**
 * Meta task for the LoadVehicleGarage task.
 */
public class LoadVehicleGarageMeta implements MetaTask {
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.loadVehicleGarage"); //$NON-NLS-1$
    
    /** default logger. */
    private static Logger logger = Logger.getLogger(LoadVehicleGarageMeta.class.getName());
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new LoadVehicleGarage(person);
    }

    @Override
    public double getProbability(Person person) {
        
        double result = 0D;

        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
            
            // Check all vehicle missions occurring at the settlement.
            try {
                List<Mission> missions = LoadVehicleGarage.getAllMissionsNeedingLoading(person.getSettlement());
                result = 50D * missions.size();
            }
            catch (Exception e) {
                logger.log(Level.SEVERE, "Error finding loading missions.", e);
            }
        }

        // Effort-driven task modifier.
        result *= person.getPerformanceRating();
        
        // Job modifier.
        Job job = person.getMind().getJob();
        if (job != null) {
            result *= job.getStartTaskProbabilityModifier(LoadVehicleGarage.class);
        }
    
        return result;
    }
}