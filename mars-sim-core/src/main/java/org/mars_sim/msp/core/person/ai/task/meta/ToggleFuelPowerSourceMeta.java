/**
 * Mars Simulation Project
 * ToggleFuelPowerSourceMeta.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.ToggleFuelPowerSource;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FuelPowerSource;
import org.mars_sim.msp.core.structure.building.function.FunctionType;

/**
 * Meta task for the ToggleFuelPowerSource task.
 */
public class ToggleFuelPowerSourceMeta extends MetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.toggleFuelPowerSource"); //$NON-NLS-1$

    public ToggleFuelPowerSourceMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		addFavorite(FavoriteType.TINKERING);
		setPreferredJob(JobType.TECHNICIAN);
	}


    @Override
    public Task constructInstance(Person person) {
        return new ToggleFuelPowerSource(person);
    }

    @Override
    public double getProbability(Person person) {

    	double result = 0D;
        
        if (person.isInSettlement()) {
        	
            // Probability affected by the person's stress and fatigue.
            PhysicalCondition condition = person.getPhysicalCondition();
            double fatigue = condition.getFatigue();
            double stress = condition.getStress();
            double hunger = condition.getHunger();
            
            if (fatigue > 1000 || stress > 50 || hunger > 500)
            	return 0;
            
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
	
	        result = applyPersonModifier(result, person);
	        
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
}
