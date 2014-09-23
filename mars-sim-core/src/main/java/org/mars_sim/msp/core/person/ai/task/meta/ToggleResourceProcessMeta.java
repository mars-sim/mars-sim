/**
 * Mars Simulation Project
 * ToggleResourceProcessMeta.java
 * @version 3.07 2014-09-18
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.person.ai.task.ToggleResourceProcess;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.ResourceProcess;

/**
 * Meta task for the ToggleResourceProcess task.
 */
public class ToggleResourceProcessMeta implements MetaTask {
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.toggleResourceProcess"); //$NON-NLS-1$
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new ToggleResourceProcess(person);
    }

    @Override
    public double getProbability(Person person) {
        
        double result = 0D;

        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
            boolean isEVA = false;

            Settlement settlement = person.getSettlement();

            // Check if settlement has resource process override set.
            if (!settlement.getResourceProcessOverride()) {
                try {
                    Building building = ToggleResourceProcess.getResourceProcessingBuilding(person);
                    if (building != null) {
                        ResourceProcess process = ToggleResourceProcess.getResourceProcess(building);
                        isEVA = !building.hasFunction(BuildingFunction.LIFE_SUPPORT);
                        double diff = ToggleResourceProcess.getResourcesValueDiff(settlement, process);
                        double baseProb = diff * 10000D;
                        if (baseProb > 100D) {
                            baseProb = 100D;
                        }
                        result += baseProb;

                        if (!isEVA) {
                            // Factor in building crowding and relationship factors.
                            result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, building);
                            result *= TaskProbabilityUtil.getRelationshipModifier(person, building);
                        }
                    }
                }
                catch (Exception e) {
                    e.printStackTrace(System.err);
                }
            }

            if (isEVA) {
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
                    if (settlement.getCurrentPopulationNum() > settlement.getPopulationCapacity()) {
                        result *= 2D;
                    }
                }
            }

            // Effort-driven task modifier.
            result *= person.getPerformanceRating();

            // Job modifier.
            Job job = person.getMind().getJob();
            if (job != null) {
                result *= job.getStartTaskProbabilityModifier(ToggleResourceProcess.class);    
            }
        }

        return result;
    }
}