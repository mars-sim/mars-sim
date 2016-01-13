/**
 * Mars Simulation Project
 * LoadVehicleEVAMeta.java
 * @version 3.08 2015-06-15
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.LoadVehicleEVA;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.ai.job.Deliverybot;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * Meta task for the LoadVehicleEVA task.
 */
public class LoadVehicleEVAMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.loadVehicleEVA"); //$NON-NLS-1$

    /** default logger. */
    private static Logger logger = Logger.getLogger(LoadVehicleEVAMeta.class.getName());

    private SurfaceFeatures surface;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new LoadVehicleEVA(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;

        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {

            // Check all vehicle missions occurring at the settlement.
            try {
                List<Mission> missions = LoadVehicleEVA.getAllMissionsNeedingLoading(person.getSettlement());
                result += 100D * missions.size();
            }
            catch (Exception e) {
                logger.log(Level.SEVERE, "Error finding loading missions.", e);
            }

            // Check if any rovers are in need of EVA suits to allow occupants to exit.
            if (LoadVehicleEVA.getRoversNeedingEVASuits(person.getSettlement()).size() > 0) {
                int numEVASuits = person.getSettlement().getInventory().findNumEmptyUnitsOfClass(EVASuit.class, false);
                if (numEVASuits >= 2) {
                    result += 100D;
                }
            }

            // Crowded settlement modifier
            Settlement settlement = person.getSettlement();
            if (settlement.getCurrentPopulationNum() > settlement.getPopulationCapacity()) {
                result *= 2D;
            }

            // Check if an airlock is available
            if (EVAOperation.getWalkableAvailableAirlock(person) == null) {
                result = 0D;
            }

            // Check if it is night time.
            if (surface == null) {
                surface = Simulation.instance().getMars().getSurfaceFeatures();
            }
            if (surface.getSolarIrradiance(person.getCoordinates()) == 0D) {
                if (!surface.inDarkPolarRegion(person.getCoordinates())) {
                    result = 0D;
                }
            }

            // Effort-driven task modifier.
            result *= person.getPerformanceRating();

            // Job modifier.
            Job job = person.getMind().getJob();
            if (job != null) {
                result *= job.getStartTaskProbabilityModifier(LoadVehicleEVA.class);
            }

            // Modify if operations is the person's favorite activity.
            if (person.getFavorite().getFavoriteActivity().equalsIgnoreCase("Operations")) {
                result *= 2D;
            }

            // 2015-06-07 Added Preference modifier
            if (result > 0) {
                result += person.getPreference().getPreferenceScore(this);
            }
            if (result < 0) {
                result = 0;
            }

        }

        return result;
    }

	@Override
	public Task constructInstance(Robot robot) {
		return new LoadVehicleEVA(robot);
	}

	@Override
	public double getProbability(Robot robot) {

        double result = 0D;

/*        
        if (robot.getBotMind().getRobotJob() instanceof Deliverybot)  {

            if (robot.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {

                // Check all vehicle missions occurring at the settlement.
                try {
                    List<Mission> missions = LoadVehicleEVA.getAllMissionsNeedingLoading(robot.getSettlement());
                    result += 100D * missions.size();
                }
                catch (Exception e) {
                    logger.log(Level.SEVERE, "Error finding loading missions.", e);
                }

                if (LoadVehicleEVA.getRoversNeedingEVASuits(robot.getSettlement()).size() > 0) {
                    int numEVASuits = robot.getSettlement().getInventory().findNumEmptyUnitsOfClass(EVASuit.class, false);
                    if (numEVASuits >= 2) {
                        result += 100D;
                    }
                }
            }

            // Effort-driven task modifier.
            result *= robot.getPerformanceRating();

            // Check if an airlock is available
            if (EVAOperation.getWalkableAvailableAirlock(robot) == null) {
                result = 0D;
            }

            // Check if it is night time.
            SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
            if (surface.getSolarIrradiance(robot.getCoordinates()) == 0) {
                if (!surface.inDarkPolarRegion(robot.getCoordinates())) {
                    result = 0D;
                }
            }

        }
*/
        return result;
    }
}