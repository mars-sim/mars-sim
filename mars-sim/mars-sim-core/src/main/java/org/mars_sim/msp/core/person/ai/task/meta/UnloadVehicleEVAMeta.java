/**
 * Mars Simulation Project
 * UnloadVehicleEVA.java
 * @version 3.08 2015-05-14
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.person.ai.task.UnloadVehicleEVA;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.ai.job.Deliverybot;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * Meta task for the UnloadVehicleEVA task.
 */
public class UnloadVehicleEVAMeta implements MetaTask {
    
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

        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {

            // Check all vehicle missions occurring at the settlement.
            try {
                int numVehicles = 0;
                numVehicles += UnloadVehicleEVA.getAllMissionsNeedingUnloading(person.getSettlement()).size();
                numVehicles += UnloadVehicleEVA.getNonMissionVehiclesNeedingUnloading(person.getSettlement()).size();
                result = 50D * numVehicles;
            }
            catch (Exception e) {
                logger.log(Level.SEVERE,"Error finding unloading missions. " + e.getMessage());
                e.printStackTrace(System.err);
            }
        }

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
            Settlement settlement = person.getSettlement();
            if (settlement.getCurrentPopulationNum() > settlement.getPopulationCapacity()) {
                result *= 2D;
            }
        }

        // Effort-driven task modifier.
        result *= person.getPerformanceRating();

        // Job modifier.
        Job job = person.getMind().getJob();
        if (job != null) {
            result *= job.getStartTaskProbabilityModifier(UnloadVehicleEVA.class);        
        }
        
        // Modify if operations is the person's favorite activity.
        if (person.getFavorite().getFavoriteActivity().equalsIgnoreCase("Operations")) {
            result *= 2D;
        }

        return result;
    }

	@Override
	public Task constructInstance(Robot robot) {
		return new UnloadVehicleEVA(robot);
	}

	@Override
	public double getProbability(Robot robot) {
	     
        double result = 0D;

        if (robot.getBotMind().getRobotJob() instanceof Deliverybot)  {

	        // TODO: should  mission continue at night time?
	        // Check if it is night time.
	        SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
	        if (surface.getSurfaceSunlight(robot.getCoordinates()) == 0)
	            if (!surface.inDarkPolarRegion(robot.getCoordinates()))
	                result = 0D;
        	   
        	if (robot.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
		
        		// Check if an airlock is available
        		if (EVAOperation.getWalkableAvailableAirlock(robot) == null)
		                result = 0D;
        		
        		if (result != 0) {
		        		
		            // Check all vehicle missions occurring at the settlement.
		            try {
		                int numVehicles = 0;
		                numVehicles += UnloadVehicleEVA.getAllMissionsNeedingUnloading(robot.getSettlement()).size();
		                numVehicles += UnloadVehicleEVA.getNonMissionVehiclesNeedingUnloading(robot.getSettlement()).size();
		                result = 50D * numVehicles;
		            }
		            catch (Exception e) {
		                logger.log(Level.SEVERE,"Error finding unloading missions. " + e.getMessage());
		                e.printStackTrace(System.err);
		            }
		       
		            Settlement settlement = robot.getSettlement();
		            if (settlement.getCurrentPopulationNum() > settlement.getPopulationCapacity()) {
		                result *= 2D;
		            }
		            
			        // Effort-driven task modifier.
			        result *= robot.getPerformanceRating();
		
		        }
        	}
        
        }
        return result;
    }
}