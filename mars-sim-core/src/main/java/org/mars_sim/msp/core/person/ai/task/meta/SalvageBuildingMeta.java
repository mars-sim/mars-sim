/**
 * Mars Simulation Project
 * SalvageBuildingMeta.java
 * @version 3.07 2014-09-18
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.mission.BuildingSalvageMission;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.SalvageBuilding;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.ai.job.Constructionbot;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * Meta task for the SalvageBuilding task.
 */
public class SalvageBuildingMeta implements MetaTask {
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.salvageBuilding"); //$NON-NLS-1$
    
    /** default logger. */
    private static Logger logger = Logger.getLogger(SalvageBuildingMeta.class.getName());
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new SalvageBuilding(person);
    }

    @Override
    public double getProbability(Person person) {
        
        double result = 0D;
        

        // Check if an airlock is available
        if (EVAOperation.getWalkableAvailableAirlock(person) == null) {
            result = 0D;
        }

        // Check if it is night time.
        SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
        if (surface.getSurfaceSunlight(person.getCoordinates()) == 0) {
            if (!surface.inDarkPolarRegion(person.getCoordinates()))
                result = 0D;
        } 


        if (result != 0 && person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
            
            // Check all building salvage missions occurring at the settlement.
            try {
                List<BuildingSalvageMission> missions = SalvageBuilding.
                        getAllMissionsNeedingAssistance(person.getSettlement());
                result = 50D * missions.size();
                
                if (person.getFavorite().getFavoriteActivity().equals("Tinkering"))
                	result += 50D;
            }
            catch (Exception e) {
                logger.log(Level.SEVERE, "Error finding building salvage missions.", e);
            }
            
            // Crowded settlement modifier
            Settlement settlement = person.getSettlement();
            if (settlement.getCurrentPopulationNum() > settlement.getPopulationCapacity()) {
                result *= 2D;
            }
                      
            // Effort-driven task modifier.
            result *= person.getPerformanceRating();
            
            // Job modifier.
            Job job = person.getMind().getJob();
            if (job != null) {
                result *= job.getStartTaskProbabilityModifier(SalvageBuilding.class);        
            }
            
        }  
    
        return result;
    }

	@Override
	public Task constructInstance(Robot robot) {
        return new SalvageBuilding(robot);
	}

	@Override
	public double getProbability(Robot robot) {
	       
        double result = 0D;
        
        if (robot.getBotMind().getRobotJob() instanceof Constructionbot) {
     
	        // Check if it is night time.
	        SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
	        if (surface.getSurfaceSunlight(robot.getCoordinates()) == 0)
	            if (!surface.inDarkPolarRegion(robot.getCoordinates()))
	                result = 0D;
   	
	        if (robot.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
		        // Check if an airlock is available
		        if (EVAOperation.getWalkableAvailableAirlock(robot) == null)
		            result = 0D;
		        
		        if (result != 0)  { // if task penalty is not zero
		        	
		            // Check all building salvage missions occurring at the settlement.
		            try {
		                List<BuildingSalvageMission> missions = SalvageBuilding.
		                        getAllMissionsNeedingAssistance(robot.getSettlement());
		                result = 50D * missions.size();
		            }
		            catch (Exception e) {
		                logger.log(Level.SEVERE, "Error finding building salvage missions.", e);
		            }
		        }
	        
		        // Crowded settlement modifier
	            Settlement settlement = robot.getSettlement();
	            if (settlement.getCurrentPopulationNum() > settlement.getPopulationCapacity())
	                result *= 2D;
		        
		        // Effort-driven task modifier.
		        result *= robot.getPerformanceRating();
        
	        }
        
        }
        return result;
    }
}