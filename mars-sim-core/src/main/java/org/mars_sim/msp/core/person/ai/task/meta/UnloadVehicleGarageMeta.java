/**
 * Mars Simulation Project
 * UnloadVehicleGarageMeta.java
 * @version 3.08 2015-06-08
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.person.ai.task.UnloadVehicleEVA;
import org.mars_sim.msp.core.person.ai.task.UnloadVehicleGarage;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.ai.job.Deliverybot;

/**
 * Meta task for the UnloadVehicleGarage task.
 */
public class UnloadVehicleGarageMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.unloadVehicleGarage"); //$NON-NLS-1$

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
                result = 100D * numVehicles;
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

        // Modify if operations is the person's favorite activity.
        if (person.getFavorite().getFavoriteActivity().equalsIgnoreCase("Operations")) {
            result *= 2D;
        }

        // 2015-06-07 Added Preference modifier
        if (result > 0)
        	result += person.getPreference().getPreferenceScore(this);
        if (result < 0) result = 0;

        return result;
    }

	@Override
	public Task constructInstance(Robot robot) {
        return new UnloadVehicleGarage(robot);
	}

	@Override
	public double getProbability(Robot robot) {

        double result = 0D;

        if (robot.getBotMind().getRobotJob() instanceof Deliverybot)
	        if (robot.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {

	            // Check all vehicle missions occurring at the settlement.
	            try {
	                int numVehicles = 0;
	                numVehicles += UnloadVehicleGarage.getAllMissionsNeedingUnloading(robot.getSettlement()).size();
	                numVehicles += UnloadVehicleGarage.getNonMissionVehiclesNeedingUnloading(robot.getSettlement()).size();
	                result = 100D * numVehicles;
	            }
	            catch (Exception e) {
	                logger.log(Level.SEVERE,"Error finding unloading missions. " + e.getMessage());
	                e.printStackTrace(System.err);
	            }


	        // Effort-driven task modifier.
	        result *= robot.getPerformanceRating();

	        }

        return result;
    }
}