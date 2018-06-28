/**
 * Mars Simulation Project
 * ToggleResourceProcessMeta.java
 * @version 3.1.0 2017-10-23
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.person.ai.task.ToggleResourceProcess;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.ResourceProcess;

/**
 * Meta task for the ToggleResourceProcess task.
 */
public class ToggleResourceProcessMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

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

        if (person.isInSettlement()) {
        	
	    	Settlement settlement = person.getSettlement();
	        
	         // TODO: need to consider if a person is out there on Mars somewhere, out of the settlement
	         // and if he has to do a EVA to repair a broken vehicle.
	
	         	//2016-10-04 Checked for radiation events
	     	boolean[] exposed = settlement.getExposed();
	
	
	 		if (exposed[2])
	 			// SEP can give lethal dose of radiation, out won't go outside
	             return 0;
	 
	            // Check if an airlock is available
	        if (EVAOperation.getWalkableAvailableAirlock(person) == null)
	    		return 0;

            // Check if it is night time.
            SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
            if (surface.getSolarIrradiance(person.getCoordinates()) == 0D) {
                if (!surface.inDarkPolarRegion(person.getCoordinates())) {
                    return 0;
                }
            }

            boolean isEVA = false;

            // Check if settlement has resource process override set.
            if (!settlement.getResourceProcessOverride()) {
                try {
                    Building building = ToggleResourceProcess.getResourceProcessingBuilding(person);
                    if (building != null) {
                        ResourceProcess process = ToggleResourceProcess.getResourceProcess(building);
                        isEVA = !building.hasFunction(FunctionType.LIFE_SUPPORT);
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

            if (isEVA && settlement.getIndoorPeopleCount() > settlement.getPopulationCapacity()) {
                 result *= 2D;
            }

            // Effort-driven task modifier.
            result *= person.getPerformanceRating();

            // Job modifier.
            Job job = person.getMind().getJob();
            if (job != null) {
                result *= job.getStartTaskProbabilityModifier(ToggleResourceProcess.class);
            }

            // Modify if tinkering is the person's favorite activity.
            if (person.getFavorite().getFavoriteActivity() == FavoriteType.TINKERING) {
                result *= 2D;
            }

	        // 2015-06-07 Added Preference modifier
	        if (result > 0)
	         	result = result + result * person.getPreference().getPreferenceScore(this)/5D;

	    	if (exposed[0]) {
				result = result/2D;// Baseline can give a fair amount dose of radiation
			}

	    	if (exposed[1]) {// GCR can give nearly lethal dose of radiation
				result = result/4D;
			}

	        if (result < 0) result = 0;
        }

        return result;
    }

	@Override
	public Task constructInstance(Robot robot) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getProbability(Robot robot) {
		// TODO Auto-generated method stub
		return 0;
	}
}