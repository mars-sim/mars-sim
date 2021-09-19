/*
 * Mars Simulation Project
 * ToggleFuelPowerSourceMeta.java
 * @date 2021-09-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.JobType;
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

    /** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(ToggleFuelPowerSourceMeta.class.getName());
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.toggleFuelPowerSource"); //$NON-NLS-1$

	private static final double FACTOR = 10_000D;
	
    public ToggleFuelPowerSourceMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.TINKERING);
		setPreferredJob(JobType.TECHNICIAN);
	}


    @Override
    public Task constructInstance(Person person) {
        return new ToggleFuelPowerSource(person);
    }

    @Override
    public double getProbability(Person person) {

    	double result = 0D;
        
		// A person can remotely toggle the fuel power source.
        if (person.isInSettlement()) {
        
	    	Settlement settlement = person.getSettlement();
	
	        try {
	            Building building = ToggleFuelPowerSource.getFuelPowerSourceBuilding(person);
	            if (building != null) {
	                FuelPowerSource powerSource = ToggleFuelPowerSource.getFuelPowerSource(building);
	                
	                double diff = ToggleFuelPowerSource.getValueDiff(settlement, powerSource);
	                double baseProb = diff * FACTOR;
	                if (baseProb > 10000) {
	                    baseProb = 10000;
	                }
	                result += baseProb;
	
	                if (building.hasFunction(FunctionType.LIFE_SUPPORT)) {
	                    // Factor in building crowding and relationship factors.
	                    result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, building);
	                    result *= TaskProbabilityUtil.getRelationshipModifier(person, building);
	                }
	            }
	        }
	        catch (Exception e) {
				logger.severe(person, "Trouble with getting the difference for a fuel power source: ", e);
	        }

	
	        result = applyPersonModifier(result, person);
	
	        if (result < 0) result = 0;

        }
        
        return result;
    }
}
