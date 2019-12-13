/**
 * Mars Simulation Project
 * UnloadVehicleEVA.java
 * @version 3.1.0 2017-09-12
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.UnloadVehicleEVA;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * Meta task for the UnloadVehicleEVA task.
 */
public class UnloadVehicleEVAMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.unloadVehicleEVA"); //$NON-NLS-1$

    /** default logger. */
    private static Logger logger = Logger.getLogger(UnloadVehicleEVAMeta.class.getName());

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new UnloadVehicleEVA(person);
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
            
        	Settlement settlement = CollectionUtils.findSettlement(person.getCoordinates());
            
	    	// Check for radiation events
	    	boolean[] exposed = settlement.getExposed();
	
			if (exposed[2]) // SEP can give lethal dose of radiation
	            return 0;
		
	        // Check if an airlock is available
	        if (EVAOperation.getWalkableAvailableAirlock(person) == null)
	    		return 0;
	
	        // Check if it is night time.
			if (EVAOperation.isGettingDark(person))
				return 0;
	        		
            // Checks if the person's settlement is at meal time and is hungry
            if ((EVAOperation.isHungryAtMealTime(person)))
            	return 0;
            
	        // Check all vehicle missions occurring at the settlement.
	        try {
	            int numVehicles = 0;
	            numVehicles += UnloadVehicleEVA.getAllMissionsNeedingUnloading(settlement).size();
	            numVehicles += UnloadVehicleEVA.getNonMissionVehiclesNeedingUnloading(settlement).size();
	            result = 100D * numVehicles;
	        }
	        catch (Exception e) {
	            logger.log(Level.SEVERE,"Error finding unloading missions. " + e.getMessage());
	            e.printStackTrace(System.err);
	        }
	
	        if (result <= 0) result = 0;
	        
	        // Crowded settlement modifier
	        if (settlement.getIndoorPeopleCount() > settlement.getPopulationCapacity()) {
	            result *= 2D;
	        }
	
	        // Effort-driven task modifier.
	        result *= person.getPerformanceRating();
	
	        // Job modifier.
	        Job job = person.getMind().getJob();
	        if (job != null) {
	            result *= job.getStartTaskProbabilityModifier(UnloadVehicleEVA.class)
	            		* settlement.getGoodsManager().getTransportationFactor();
	        }
	
	        // Modify if operation is the person's favorite activity.
	        if (person.getFavorite().getFavoriteActivity() == FavoriteType.OPERATION) {
	            result += RandomUtil.getRandomInt(1, 20);
	        }
	
	        // Add Preference modifier
	        if (result > 0D) {
	            result = result + result * person.getPreference().getPreferenceScore(this)/5D;
	        }
	
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

	@Override
	public Task constructInstance(Robot robot) {
		return null;//new UnloadVehicleEVA(robot);
	}

	@Override
	public double getProbability(Robot robot) {

        double result = 0D;
/*
        if (robot.getBotMind().getRobotJob() instanceof Deliverybot)  {

            if (robot.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {

                // Check all vehicle missions occurring at the settlement.
                try {
                    int numVehicles = 0;
                    numVehicles += UnloadVehicleEVA.getAllMissionsNeedingUnloading(robot.getSettlement()).size();
                    numVehicles += UnloadVehicleEVA.getNonMissionVehiclesNeedingUnloading(robot.getSettlement()).size();
                    result = 100D * numVehicles;
                }
                catch (Exception e) {
                    logger.log(Level.SEVERE,"Error finding unloading missions. " + e.getMessage());
                    e.printStackTrace(System.err);
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
            if (surface.getSolarIrradiance(robot.getCoordinates()) == 0D) {
                if (!surface.inDarkPolarRegion(robot.getCoordinates())) {
                    result = 0D;
                }
            }
        }
*/
        return result;
    }
}