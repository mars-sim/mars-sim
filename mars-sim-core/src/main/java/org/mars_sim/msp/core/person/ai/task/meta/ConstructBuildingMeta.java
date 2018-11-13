/**
 * Mars Simulation Project
 * ConstructBuildingMeta.java
 * @version 3.1.0 2017-10-23
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.mission.BuildingConstructionMission;
import org.mars_sim.msp.core.person.ai.task.ConstructBuilding;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * Meta task for the ConstructBuilding task.
 */
public class ConstructBuildingMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.constructBuilding"); //$NON-NLS-1$

    /** default logger. */
    private static Logger logger = Logger.getLogger(ConstructBuildingMeta.class.getName());

    private SurfaceFeatures surface;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new ConstructBuilding(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;

        // Check if an airlock is available
        if (EVAOperation.getWalkableAvailableAirlock(person) == null) {
            return 0;
        }

        // Check if it is night time.
        surface = Simulation.instance().getMars().getSurfaceFeatures();

        if (surface.getSolarIrradiance(person.getCoordinates()) == 0D) {
            if (!surface.inDarkPolarRegion(person.getCoordinates())) {
                return 0;
            }
        }

        if (person.isInSettlement()) {

            try {
                // Check all building construction missions occurring at the settlement.
                List<BuildingConstructionMission> missions = ConstructBuilding.
                        getAllMissionsNeedingAssistance(person.getSettlement());
                result = 100D * missions.size();

                // Crowded settlement modifier
                Settlement settlement = person.getSettlement();
                if (settlement.getIndoorPeopleCount() > settlement.getPopulationCapacity()) {
                    result *= 2D;
                }
            }
            catch (Exception e) {
                logger.log(Level.SEVERE, "Error finding building construction missions.", e);
            }
        }


        // Effort-driven task modifier.
        result *= person.getPerformanceRating();

        // Job modifier.
        Job job = person.getMind().getJob();
        if (job != null) {
            result *= job.getStartTaskProbabilityModifier(ConstructBuilding.class);
        }

        // Modify if construction is the person's favorite activity.
        if (person.getFavorite().getFavoriteActivity() == FavoriteType.TINKERING)
            result *= 1.5D;

        // 2015-06-07 Added Preference modifier
        if (result > 0D) {
            result = result + result * person.getPreference().getPreferenceScore(this)/5D;
        }

        if (result < 0D) {
            result = 0D;
        }


        return result;
    }

	public Task constructInstance(Robot robot) {
        return null;
	}

	public double getProbability(Robot robot) {
        return 0;
    }
}