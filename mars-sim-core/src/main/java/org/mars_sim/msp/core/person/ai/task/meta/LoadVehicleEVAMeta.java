/**
 * Mars Simulation Project
 * LoadVehicleEVAMeta.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.List;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.LoadVehicleEVA;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * Meta task for the LoadVehicleEVA task.
 */
public class LoadVehicleEVAMeta extends MetaTask {


    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.loadVehicleEVA"); //$NON-NLS-1$

    /** default logger. */
    private static SimLogger logger = SimLogger.getLogger(LoadVehicleEVAMeta.class.getName());
	
    public LoadVehicleEVAMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
	}


    @Override
    public Task constructInstance(Person person) {
        return new LoadVehicleEVA(person);
    }

    @Override
    public double getProbability(Person person) {
    	if (person.isOutside()) {
    		return 0;
    	}
    		
        // Check if an airlock is available
        if (EVAOperation.getWalkableAvailableAirlock(person) == null)
    		return 0;

        // Check if it is night time.
		if (EVAOperation.isGettingDark(person))
			return 0;

        // Checks if the person's settlement is at meal time and is hungry
        if (EVAOperation.isHungryAtMealTime(person))
        	return 0;
        
        // Checks if the person is physically drained
		if (EVAOperation.isExhausted(person))
			return 0;
		
		
    	double result = 0D;
        
    	Settlement settlement = CollectionUtils.findSettlement(person.getCoordinates());
    	
    	if (settlement != null) {
       	
//	        if (!LoadVehicleEVA.anyRoversNeedEVA(settlement)) {
//	        	return 0;
//	        }
	        
            // Probability affected by the person's stress and fatigue.
            PhysicalCondition condition = person.getPhysicalCondition();
            double fatigue = condition.getFatigue();
            double stress = condition.getStress();
            double hunger = condition.getHunger();
            
            if (fatigue > 500 || stress > 50 || hunger > 500)
            	return 0;
                 
        	boolean[] exposed = {false, false, false};
        	
        	if (settlement != null) {
        		// Check for radiation events
        		exposed = settlement.getExposed();
        	}
        	
			if (exposed[2]) {// SEP can give lethal dose of radiation
	            return 0;
			}
            
	        // Check all vehicle missions occurring at the settlement.
	        try {
	            List<Mission> missions = LoadVehicleEVA.getAllMissionsNeedingLoading(settlement);
                int num = missions.size();
               	if (num == 0)
               		return 0;
               	else 
               		result += 100D * num;
	        }
	        catch (Exception e) {
	            logger.severe(person, "Error finding loading missions.", e);
	        }
	        
	        // Check if any rovers are in need of EVA suits to allow occupants to exit.
	        if (LoadVehicleEVA.getRoversNeedingEVASuits(settlement).size() > 0) {
	            int numEVASuits = settlement.getInventory().findNumEVASuits(false, false);
	            if (numEVASuits == 0)
	            	return 0;
	            else if (numEVASuits >= 2) {
	                result += 100D;
	            }
	        }
				
//	        // Crowded settlement modifier
//	        if (settlement.getIndoorPeopleCount() > settlement.getPopulationCapacity())
//	            result *= 2D;
	
	        // Job modifier.
	        Job job = person.getMind().getJob();
	        if (job != null)
	            result *= job.getStartTaskProbabilityModifier(LoadVehicleEVA.class)
	            		* settlement.getGoodsManager().getTransportationFactor();
	
	        // Effort-driven task modifier.
	        result *= person.getPerformanceRating();
	
	        // Modify if operations is the person's favorite activity.
	        if (person.getFavorite().getFavoriteActivity() == FavoriteType.OPERATION)
	            result *= RandomUtil.getRandomDouble(2D);
	
	        // Add Preference modifier
	        if (result > 0D) {
	            result = result + result * person.getPreference().getPreferenceScore(this)/6D;
	        }
	
	    	if (exposed[0]) {
				result = result/3D;// Baseline can give a fair amount dose of radiation
			}
	
	    	if (exposed[1]) {// GCR can give nearly lethal dose of radiation
				result = result/6D;
			}

    	}
    	
        if (result < 0)
            result = 0;

        return result;
    }
}
