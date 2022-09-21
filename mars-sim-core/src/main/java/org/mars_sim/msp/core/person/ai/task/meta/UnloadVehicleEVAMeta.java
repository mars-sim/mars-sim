/*
 * Mars Simulation Project
 * UnloadVehicleEVA.java
 * @date 2022-08-06
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.fav.FavoriteType;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.UnloadVehicleEVA;
import org.mars_sim.msp.core.person.ai.task.util.MetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * Meta task for the UnloadVehicleEVA task.
 */
public class UnloadVehicleEVAMeta extends MetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.unloadVehicleEVA"); //$NON-NLS-1$

    /** default logger. */
    private static final Logger logger = Logger.getLogger(UnloadVehicleEVAMeta.class.getName());

    public UnloadVehicleEVAMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.OPERATION);
		setTrait(TaskTrait.STRENGTH);
		setPreferredJob(JobType.LOADERS);
	}

    @Override
    public Task constructInstance(Person person) {
        return new UnloadVehicleEVA(person);
    }

    @Override
    public double getProbability(Person person) {
        double result = 0D;

        Settlement settlement = person.getSettlement();

        if (settlement != null) {

	    	// Check for radiation events
	    	boolean[] exposed = settlement.getExposed();

			if (exposed[2]) // SEP can give lethal dose of radiation
	            return 0;

	        // Check if an airlock is available
	        if (EVAOperation.getWalkableAvailableEgressAirlock(person) == null)
	    		return 0;

	        // Check if it is night time.
			if (EVAOperation.isGettingDark(person))
				return 0;

            // Checks if the person's settlement is at meal time and is hungry
            if (EVAOperation.isHungryAtMealTime(person))
            	return 0;

            // Checks if the person is physically fit for heavy EVA tasks
    		if (!EVAOperation.isEVAFit(person))
    			return 0;

			double score = person.getPhysicalCondition().computeHealthScore();
			
	        // Check all vehicle missions occurring at the settlement.
	        try {
	            int numVehicles = 0;
	            numVehicles += UnloadVehicleEVA.getAllMissionsNeedingUnloading(settlement).size();
	            numVehicles += UnloadVehicleEVA.getNonMissionVehiclesNeedingUnloading(settlement).size();
	            result = score * numVehicles;
	        }
	        catch (Exception e) {
	            logger.log(Level.SEVERE,"Error finding unloading missions. " + e.getMessage());
	        }

	        if (result <= 0) result = 0;

	        // Crowded settlement modifier
	        if (settlement.getIndoorPeopleCount() > settlement.getPopulationCapacity()) {
	            result *= 2D;
	        }

	        // Settlement factor
	        result *= settlement.getGoodsManager().getTransportationFactor();

	        double shiftBonus = person.getTaskSchedule().obtainScoreAtStartOfShift();
	        
	        // Encourage to get this task done early in a work shift
	        result *= shiftBonus / 10;
	        
	        result = applyPersonModifier(result, person);

	    	if (exposed[0]) {
				result = result/3D;// Baseline can give a fair amount dose of radiation
			}

	    	if (exposed[1]) {// GCR can give nearly lethal dose of radiation
				result = result/6D;
			}

	        if (result < 0) result = 0;

        }

        return result;
    }
}
