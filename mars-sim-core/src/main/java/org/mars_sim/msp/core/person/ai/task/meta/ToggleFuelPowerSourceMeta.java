/**
 * Mars Simulation Project
 * ToggleFuelPowerSourceMeta.java
 * @version 3.1.0 2017-08-30
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.person.ai.task.ToggleFuelPowerSource;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.FuelPowerSource;

/**
 * Meta task for the ToggleFuelPowerSource task.
 */
public class ToggleFuelPowerSourceMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.toggleFuelPowerSource"); //$NON-NLS-1$

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new ToggleFuelPowerSource(person);
    }

    @Override
    public double getProbability(Person person) {

    	double result = 0D;
        
        if (person.isInSettlement()) {
        	
	    	Settlement settlement = person.getSettlement();
	        
	        // TODO: need to consider if a person is out there on Mars somewhere, out of the settlement
	        // and if he has to do a EVA to repair a broken vehicle.
	
	        // Check for radiation events
	    	boolean[]exposed = settlement.getExposed();
	
	
			if (exposed[2]) {
				// SEP can give lethal dose of radiation
	            return 0;
			}
	
	        // Check if an airlock is available
	        if (EVAOperation.getWalkableAvailableAirlock(person) == null)
	    		return 0;
	
	        //MarsClock clock = Simulation.instance().getMasterClock().getMarsClock();
	        //double millisols = clock.getMillisol();
	        
	        // Check if it is getting dark
	        //SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
	        //if (surface.getSolarIrradiance(person.getCoordinates()) < 60D && millisols > 500) {
	        //    if (!surface.inDarkPolarRegion(person.getCoordinates()))
	        //       return 0;
	        //}
	
	        boolean isEVA = false;        
	
	        try {
	            Building building = ToggleFuelPowerSource.getFuelPowerSourceBuilding(person);
	            if (building != null) {
	                FuelPowerSource powerSource = ToggleFuelPowerSource.getFuelPowerSource(building);
	                isEVA = !building.hasFunction(FunctionType.LIFE_SUPPORT);
	                double diff = ToggleFuelPowerSource.getValueDiff(settlement, powerSource);
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
	
	        if (isEVA) {
	            // Crowded settlement modifier
	            if (settlement.getIndoorPeopleCount() > settlement.getPopulationCapacity()) {
	                result *= 2D;
	            }
	        }
	
	        // Effort-driven task modifier.
	        result *= person.getPerformanceRating();
	
	        // Job modifier.
	        Job job = person.getMind().getJob();
	        if (job != null) {
	            result *= job.getStartTaskProbabilityModifier(ToggleFuelPowerSource.class);
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